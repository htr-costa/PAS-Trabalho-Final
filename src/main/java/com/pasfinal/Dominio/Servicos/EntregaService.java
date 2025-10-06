package com.pasfinal.Dominio.Servicos;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.pasfinal.Dominio.Dados.PedidoRepository;
import com.pasfinal.Dominio.Entidades.Pedido;

@Service
public class EntregaService {
    private Queue<Pedido> filaEntrega;
    private Pedido emTransporte;

    private ScheduledExecutorService scheduler;
    private PedidoRepository pedidoRepository;

    public EntregaService(PedidoRepository pedidoRepository) {
        filaEntrega = new LinkedBlockingQueue<Pedido>();
        emTransporte = null;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        this.pedidoRepository = pedidoRepository;
    }

    private synchronized void colocaEmTransporte(Pedido pedido){
        pedido.setStatus(Pedido.Status.TRANSPORTE);
        pedidoRepository.salva(pedido);
        emTransporte = pedido;
        System.out.println("Pedido em transporte: "+pedido);
        // Agenda pedidoEntregue para ser chamado em 5 segundos
        scheduler.schedule(() -> pedidoEntregue(), 5, TimeUnit.SECONDS);
    }

    public synchronized void receberPedidoPronto(Pedido p) {
        filaEntrega.add(p);
        System.out.println("Pedido recebido para entrega: "+p);
        if (emTransporte == null) {
            colocaEmTransporte(filaEntrega.poll());
        }
    }

    public synchronized void pedidoEntregue() {
        emTransporte.setStatus(Pedido.Status.ENTREGUE);
        pedidoRepository.salva(emTransporte);
        System.out.println("Pedido entregue: "+emTransporte);
        emTransporte = null;
        // Se tem pedidos na fila, programa o transporte para daqui a 1 segundo
        if (!filaEntrega.isEmpty()){
            Pedido prox = filaEntrega.poll();
            scheduler.schedule(() -> colocaEmTransporte(prox), 1, TimeUnit.SECONDS);
        }
    }
}
