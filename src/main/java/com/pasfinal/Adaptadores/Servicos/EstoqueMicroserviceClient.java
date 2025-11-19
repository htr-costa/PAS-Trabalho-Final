package com.pasfinal.Adaptadores.Servicos;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Cliente REST para comunicação síncrona com o microserviço de estoque
 */
@Service
public class EstoqueMicroserviceClient {

    private final RestTemplate restTemplate;
    
    @Value("${estoque.microservice.url:http://localhost:8081}")
    private String estoqueServiceUrl;

    public EstoqueMicroserviceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Lista todos os itens do estoque
     */
    public List<ItemEstoqueDTO> listarTodos() {
        String url = estoqueServiceUrl + "/api/estoque";
        ResponseEntity<List<ItemEstoqueDTO>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ItemEstoqueDTO>>() {}
        );
        return response.getBody();
    }

    /**
     * Busca item de estoque por ID do ingrediente
     */
    public ItemEstoqueDTO buscarPorIngredienteId(Long ingredienteId) {
        String url = estoqueServiceUrl + "/api/estoque/ingrediente/" + ingredienteId;
        try {
            ResponseEntity<ItemEstoqueDTO> response = restTemplate.getForEntity(url, ItemEstoqueDTO.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica se há disponibilidade de um ingrediente
     */
    public boolean verificarDisponibilidade(Long ingredienteId, Integer quantidadeNecessaria) {
        String url = estoqueServiceUrl + "/api/estoque/verificar-disponibilidade";
        VerificarDisponibilidadeRequest request = new VerificarDisponibilidadeRequest(
            ingredienteId, 
            quantidadeNecessaria
        );
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Boolean> body = response.getBody();
            return body != null && body.getOrDefault("disponivel", false);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Dá baixa no estoque (remove quantidade)
     */
    public ItemEstoqueDTO baixarEstoque(Long ingredienteId, Integer quantidade) {
        String url = estoqueServiceUrl + "/api/estoque/baixar";
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(ingredienteId, quantidade);
        
        try {
            ResponseEntity<ItemEstoqueDTO> response = restTemplate.postForEntity(
                url, 
                request, 
                ItemEstoqueDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao dar baixa no estoque: " + e.getMessage(), e);
        }
    }

    /**
     * Adiciona ao estoque (aumenta quantidade)
     */
    public ItemEstoqueDTO adicionarEstoque(Long ingredienteId, Integer quantidade) {
        String url = estoqueServiceUrl + "/api/estoque/adicionar";
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(ingredienteId, quantidade);
        
        try {
            ResponseEntity<ItemEstoqueDTO> response = restTemplate.postForEntity(
                url, 
                request, 
                ItemEstoqueDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar ao estoque: " + e.getMessage(), e);
        }
    }

    /**
     * Cria um novo item de estoque
     */
    public ItemEstoqueDTO criarItemEstoque(Long ingredienteId, Integer quantidade) {
        String url = estoqueServiceUrl + "/api/estoque";
        AtualizarEstoqueRequest request = new AtualizarEstoqueRequest(ingredienteId, quantidade);
        
        try {
            ResponseEntity<ItemEstoqueDTO> response = restTemplate.postForEntity(
                url, 
                request, 
                ItemEstoqueDTO.class
            );
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao criar item de estoque: " + e.getMessage(), e);
        }
    }
}
