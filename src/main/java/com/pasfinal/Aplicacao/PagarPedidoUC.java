package com.pasfinal.Aplicacao;

import org.springframework.stereotype.Component;

import com.pasfinal.Dominio.Dados.PedidoRepository;
import com.pasfinal.Dominio.Entidades.Pedido;
import com.pasfinal.Dominio.Servicos.CozinhaService;

@Component
public class PagarPedidoUC {
    private PedidoRepository pedidoRepository;
    private CozinhaService cozinhaService;

    public PagarPedidoUC(PedidoRepository pedidoRepository, CozinhaService cozinhaService){
        this.pedidoRepository = pedidoRepository;
        this.cozinhaService = cozinhaService;
    }

    public boolean run(long idPedido){
        // Recupera o pedido
        Pedido pedido = pedidoRepository.recuperaPorId(idPedido);
        if(pedido == null) {
            return false;
        }

        // Verifica se o pedido está em um estado válido para pagamento
        if(pedido.getStatus() != Pedido.Status.NOVO && pedido.getStatus() != Pedido.Status.APROVADO){
            return false;
        }

        // Atualiza o status do pedido para PAGO
        pedido.setStatus(Pedido.Status.PAGO);
        pedidoRepository.salva(pedido);

        // Coloca o pedido em AGUARDANDO (aguardando início do preparo)
        pedido.setStatus(Pedido.Status.AGUARDANDO);
        pedidoRepository.salva(pedido);

        // Envia o pedido para a cozinha
        cozinhaService.chegadaDePedido(pedido);

        return true;
    }
}
