package com.pasfinal.Aplicacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pasfinal.Aplicacao.Requests.ItemPedidoRequest;
import com.pasfinal.Aplicacao.Requests.SubmeterPedidoRequest;
import com.pasfinal.Aplicacao.Responses.SubmeterPedidoResponse;
import com.pasfinal.Adaptadores.Servicos.EstoqueMicroserviceClient;
import com.pasfinal.Dominio.Dados.ClienteRepository;
import com.pasfinal.Dominio.Dados.PedidoRepository;
import com.pasfinal.Dominio.Dados.ProdutosRepository;
import com.pasfinal.Dominio.Dados.EstoqueRepository;
import com.pasfinal.Dominio.Entidades.Cliente;
import com.pasfinal.Dominio.Entidades.ItemPedido;
import com.pasfinal.Dominio.Entidades.Pedido;
import com.pasfinal.Dominio.Entidades.Produto;
import com.pasfinal.Dominio.Servicos.DescontosService;
import com.pasfinal.Dominio.Servicos.ImpostosService;

@Component
public class SubmeterPedidoUC {
    private final ProdutosRepository produtosRepo;
    private final EstoqueRepository estoqueRepo;
    private final PedidoRepository pedidoRepo;
    private final ClienteRepository clienteRepo;
    private final ImpostosService impostosService;
    private final DescontosService descontosService;
    private final EstoqueMicroserviceClient estoqueMicroserviceClient;

    public SubmeterPedidoUC(ProdutosRepository produtosRepo, EstoqueRepository estoqueRepo, 
            PedidoRepository pedidoRepo, ClienteRepository clienteRepo,
            ImpostosService impostosService, DescontosService descontosService,
            EstoqueMicroserviceClient estoqueMicroserviceClient) {
        this.produtosRepo = produtosRepo;
        this.estoqueRepo = estoqueRepo;
        this.pedidoRepo = pedidoRepo;
        this.clienteRepo = clienteRepo;
        this.impostosService = impostosService;
        this.descontosService = descontosService;
        this.estoqueMicroserviceClient = estoqueMicroserviceClient;
    }

    public SubmeterPedidoResponse run(SubmeterPedidoRequest req, String emailUsuario) {
        long id = req.getId();
        
        Pedido pedidoExistente = pedidoRepo.recuperaPorId(id);
        if (pedidoExistente != null) {
            throw new IllegalArgumentException("Já existe um pedido com o ID " + id);
        }
        
        Cliente cliente = clienteRepo.recuperaPorEmail(emailUsuario);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não encontrado para o email: " + emailUsuario);
        }
        
        String enderecoEntrega = cliente.getEndereco();
        String cpf = cliente.getCpf();
        
        List<ItemPedido> itens = new ArrayList<>();
        double valor = 0;
        List<Long> itensIndisponiveis = new ArrayList<>();
        
        Pedido pedidoNovo = new Pedido(id, cliente, enderecoEntrega, LocalDateTime.now(), 
                null, List.of(), Pedido.Status.NOVO, 0, 0, 0, 0);
        pedidoRepo.salva(pedidoNovo);
        
        for (ItemPedidoRequest ipr : req.getItens()) {
            Produto p = produtosRepo.recuperaProdutoPorid(ipr.getProdutoId());
            if (p == null) {
                itensIndisponiveis.add(ipr.getProdutoId());
                continue;
            }

            boolean disponivel = true;

            for (var ing : p.getReceita().getIngredientes()) {
                int estoque = estoqueRepo.recuperaQuantidade(ing.getId());
                if (estoque < ipr.getQuantidade()) {
                    disponivel = false;
                    break;
                }
            }
            if (!disponivel) {
                itensIndisponiveis.add(p.getId());
                continue;
            }
            itens.add(new ItemPedido(p, ipr.getQuantidade()));
            valor += p.getPreco() * ipr.getQuantidade();
        }
        
        if (!itensIndisponiveis.isEmpty()) {
            return SubmeterPedidoResponse.pedidoNegado(req.getId(), itensIndisponiveis);
        }
        
        // dá baixa no estoque no microserviço para cada ingrediente usado
        try {
            for (ItemPedido item : itens) {
                for (var ingrediente : item.getItem().getReceita().getIngredientes()) {
                    estoqueMicroserviceClient.baixarEstoque(
                        ingrediente.getId(), 
                        item.getQuantidade()
                    );
                }
            }
        } catch (Exception e) {
            // Se falhar ao dar baixa, retorna erro
            throw new RuntimeException("Erro ao processar baixa no estoque: " + e.getMessage(), e);
        }
        
        double desconto = descontosService.calcularDesconto(cpf, valor);
        double impostos = impostosService.calcularImpostos(valor);
        double valorCobrado = valor - desconto + impostos;
        
        Pedido pedidoAprovado = new Pedido(id, cliente, enderecoEntrega, LocalDateTime.now(), 
                null, itens, Pedido.Status.APROVADO, valor, impostos, desconto, valorCobrado);
        pedidoRepo.salva(pedidoAprovado);
        
        return SubmeterPedidoResponse.pedidoAprovado(id, valor, impostos, desconto, valorCobrado, enderecoEntrega);
    }
}
