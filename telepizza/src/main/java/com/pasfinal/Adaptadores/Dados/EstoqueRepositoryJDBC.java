package com.pasfinal.Adaptadores.Dados;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.pasfinal.Dominio.Dados.EstoqueRepository;

@Repository
public class EstoqueRepositoryJDBC implements EstoqueRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public EstoqueRepositoryJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public int recuperaQuantidade(long ingredienteId) {
        Integer qtd = jdbcTemplate.queryForObject(
            "SELECT quantidade FROM itensEstoque WHERE ingrediente_id = ?",
            new Object[]{ingredienteId}, Integer.class);
        return qtd == null ? 0 : qtd;
    }
}