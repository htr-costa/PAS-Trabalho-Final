package com.pasfinal.Adaptadores.Apresentacao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpSession;

import com.pasfinal.Adaptadores.Apresentacao.Presenters.CabecalhoCardapioPresenter;
import com.pasfinal.Adaptadores.Apresentacao.Presenters.CardapioPresenter;
import com.pasfinal.Aplicacao.RecuperaListaCardapiosUC;
import com.pasfinal.Aplicacao.RecuperarCardapioUC;
import com.pasfinal.Aplicacao.Responses.CardapioResponse;
import org.springframework.beans.factory.annotation.Value;
import com.pasfinal.Dominio.Entidades.Produto;
import com.pasfinal.Dominio.Entidades.Usuario;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/cardapio")
public class CardapioController {
    private RecuperarCardapioUC recuperaCardapioUC;
    private RecuperaListaCardapiosUC recuperaListaCardapioUC;
    @Value("${application.currentCardapioId}")
    private long currentCardapioId;

    public CardapioController(RecuperarCardapioUC recuperaCardapioUC,
                              RecuperaListaCardapiosUC recuperaListaCardapioUC) {
        this.recuperaCardapioUC = recuperaCardapioUC;
        this.recuperaListaCardapioUC = recuperaListaCardapioUC;
    }

    @GetMapping("/{id}")
    @CrossOrigin("*")
    public ResponseEntity<?> recuperaCardapio(@PathVariable(value="id")long id, HttpSession session){
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuário não autenticado. Faça login para acessar o cardápio.");
        }
        
        CardapioResponse cardapioResponse = recuperaCardapioUC.run(id);
        Set<Long> conjIdSugestoes = new HashSet<>(cardapioResponse.getSugestoesDoChef().stream()
            .map(produto->produto.getId())
            .toList());
        CardapioPresenter cardapioPresenter = new CardapioPresenter(cardapioResponse.getCardapio().getTitulo());
        for(Produto produto:cardapioResponse.getCardapio().getProdutos()){
            boolean sugestao = conjIdSugestoes.contains(produto.getId());
            cardapioPresenter.insereItem(produto.getId(), produto.getDescricao(), produto.getPreco(), sugestao);
        }
        return ResponseEntity.ok(cardapioPresenter);
    }

    @GetMapping("/lista")
    @CrossOrigin("*")
    public List<CabecalhoCardapioPresenter> recuperaListaCardapios(){
         List<CabecalhoCardapioPresenter> lstCardapios = 
            recuperaListaCardapioUC.run().cabecalhos().stream()
            .map(cabCar -> new CabecalhoCardapioPresenter(cabCar.id(),cabCar.titulo()))
            .toList();
         return lstCardapios;
    }
    
    @GetMapping
    @CrossOrigin("*")
    public ResponseEntity<?> recuperaCardapioAtual(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Usuário não autenticado. Faça login para acessar o cardápio.");
        }
        
        return recuperaCardapio(currentCardapioId, session);
    }
}
