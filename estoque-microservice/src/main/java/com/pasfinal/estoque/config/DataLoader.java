package com.pasfinal.estoque.config;

import com.pasfinal.estoque.entity.Ingrediente;
import com.pasfinal.estoque.entity.ItemEstoque;
import com.pasfinal.estoque.repository.IngredienteRepository;
import com.pasfinal.estoque.repository.ItemEstoqueRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(IngredienteRepository ingredienteRepository, 
                                   ItemEstoqueRepository itemEstoqueRepository) {
        return args -> {
            // Criar ingredientes
            Ingrediente disco = ingredienteRepository.save(new Ingrediente(null, "Disco de pizza"));
            Ingrediente tomate = ingredienteRepository.save(new Ingrediente(null, "Porcao de tomate"));
            Ingrediente mussarela = ingredienteRepository.save(new Ingrediente(null, "Porcao de mussarela"));
            Ingrediente presunto = ingredienteRepository.save(new Ingrediente(null, "Porcao de presunto"));
            Ingrediente calabresa = ingredienteRepository.save(new Ingrediente(null, "Porcao de calabresa"));
            Ingrediente molho = ingredienteRepository.save(new Ingrediente(null, "Molho de tomate (200ml)"));
            Ingrediente azeitona = ingredienteRepository.save(new Ingrediente(null, "Porcao de azeitona"));
            Ingrediente oregano = ingredienteRepository.save(new Ingrediente(null, "Porcao de oregano"));
            Ingrediente cebola = ingredienteRepository.save(new Ingrediente(null, "Porcao de cebola"));

            // Criar itens de estoque
            itemEstoqueRepository.save(new ItemEstoque(null, 30, disco));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, tomate));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, mussarela));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, presunto));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, calabresa));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, molho));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, azeitona));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, oregano));
            itemEstoqueRepository.save(new ItemEstoque(null, 30, cebola));

            System.out.println("✅ Dados de estoque inicializados com sucesso!");
        };
    }
}
