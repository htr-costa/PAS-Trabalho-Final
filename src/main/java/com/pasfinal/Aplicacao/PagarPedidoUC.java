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
        Pedido pedido = pedidoRepository.recuperaPorId(idPedido);
        if(pedido == null) {
            return false;
        }

        if(pedido.getStatus() != Pedido.Status.APROVADO){
            return false;
        }

        pedido.setStatus(Pedido.Status.PAGO);
        pedidoRepository.salva(pedido);
        pedido.setStatus(Pedido.Status.AGUARDANDO);
        pedidoRepository.salva(pedido);
        cozinhaService.chegadaDePedido(pedido);

        return true;
    }
}
