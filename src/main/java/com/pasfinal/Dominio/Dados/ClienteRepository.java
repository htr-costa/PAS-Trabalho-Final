package com.pasfinal.Dominio.Dados;

import com.pasfinal.Dominio.Entidades.Cliente;

public interface ClienteRepository {
    Cliente recuperaPorCpf(String cpf);
}
