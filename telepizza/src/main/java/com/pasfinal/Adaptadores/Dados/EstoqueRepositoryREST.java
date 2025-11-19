package com.pasfinal.Adaptadores.Dados;

import com.pasfinal.Adaptadores.Servicos.EstoqueFeignClient;
import com.pasfinal.Adaptadores.Servicos.ItemEstoqueDTO;
import com.pasfinal.Dominio.Dados.EstoqueRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

/**
 * Implementação do EstoqueRepository que usa o microserviço de estoque via REST
 * A anotação @Primary garante que esta implementação será usada no lugar da JDBC
 */
@Repository
@Primary
public class EstoqueRepositoryREST implements EstoqueRepository {
    
    private final EstoqueFeignClient estoqueClient;

    public EstoqueRepositoryREST(EstoqueFeignClient estoqueClient) {
        this.estoqueClient = estoqueClient;
    }

    @Override
    public int recuperaQuantidade(long ingredienteId) {
        ItemEstoqueDTO item = estoqueClient.buscarPorIngredienteId(ingredienteId);
        if (item == null) {
            return 0;
        }
        return item.getQuantidade();
    }
}
