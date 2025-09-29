package com.pasfinal.Dominio.Dados;

import com.pasfinal.Dominio.Entidades.Pedido;

public interface PedidoRepository {
    Pedido recuperaPorId(long id);
    void salva(Pedido pedido);
}
