package com.pasfinal.Aplicacao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pasfinal.Aplicacao.Requests.ItemPedidoRequest;
import com.pasfinal.Aplicacao.Requests.SubmeterPedidoRequest;
import com.pasfinal.Aplicacao.Responses.SubmeterPedidoResponse;
import com.pasfinal.Dominio.Dados.PedidoRepository;
import com.pasfinal.Dominio.Dados.ProdutosRepository;
import com.pasfinal.Dominio.Dados.EstoqueRepository;
import com.pasfinal.Dominio.Entidades.Cliente;
import com.pasfinal.Dominio.Entidades.ItemPedido;
import com.pasfinal.Dominio.Entidades.Pedido;
import com.pasfinal.Dominio.Entidades.Produto;

@Component
public class SubmeterPedidoUC {
    private final ProdutosRepository produtosRepo;
    private final EstoqueRepository estoqueRepo;
    private final PedidoRepository pedidoRepo;

    public SubmeterPedidoUC(ProdutosRepository produtosRepo, EstoqueRepository estoqueRepo, PedidoRepository pedidoRepo) {
        this.produtosRepo = produtosRepo;
        this.estoqueRepo = estoqueRepo;
        this.pedidoRepo = pedidoRepo;
    }

    public SubmeterPedidoResponse run(SubmeterPedidoRequest req) {
        long id = req.getId();
        String cpf = req.getClienteCpf();
        List<ItemPedido> itens = new ArrayList<>();
        double valor = 0;
        List<Long> itensIndisponiveis = new ArrayList<>();
        for (ItemPedidoRequest ipr : req.getItens()) {
            Produto p = produtosRepo.recuperaProdutoPorid(ipr.getProdutoId());
            if (p == null) {
                itensIndisponiveis.add(ipr.getProdutoId());
                continue;
            }
            boolean disponivel = true;
            // verifica cada ingrediente da receita
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
    double impostos = valor * 0.1;
        double desconto = 0.0;
        double valorCobrado = valor - desconto + impostos;
        Cliente cliente = new Cliente(cpf, "", "", "", "");
        Pedido pedido = new Pedido(id, cliente, null, itens, Pedido.Status.APROVADO, valor, impostos, desconto, valorCobrado);
    pedidoRepo.salva(pedido);
    return SubmeterPedidoResponse.pedidoAprovado(id, valor, impostos, desconto, valorCobrado);
    }
}