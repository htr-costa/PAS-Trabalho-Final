package com.pasfinal.Aplicacao.Requests;

import java.util.List;

public class SubmeterPedidoRequest {
    private long id;
    private String clienteCpf;
    private String enderecoEntrega;
    private List<ItemPedidoRequest> itens;

    public SubmeterPedidoRequest() {
    }

    public SubmeterPedidoRequest(long id, String clienteCpf, String enderecoEntrega, List<ItemPedidoRequest> itens) {
        this.id = id;
        this.clienteCpf = clienteCpf;
        this.enderecoEntrega = enderecoEntrega;
        this.itens = itens;
    }

    public long getId() {
        return id;
    }

    public String getClienteCpf() {
        return clienteCpf;
    }

    public String getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public List<ItemPedidoRequest> getItens() {
        return itens;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setClienteCpf(String clienteCpf) {
        this.clienteCpf = clienteCpf;
    }

    public void setEnderecoEntrega(String enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
    }

    public void setItens(List<ItemPedidoRequest> itens) {
        this.itens = itens;
    }
}