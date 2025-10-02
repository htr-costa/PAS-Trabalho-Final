package com.pasfinal.Adaptadores.Apresentacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pasfinal.Aplicacao.PagarPedidoUC;
import com.pasfinal.Aplicacao.RecuperarStatusPedidoUC;
import com.pasfinal.Aplicacao.Responses.StatusPedidoResponse;
import com.pasfinal.Dominio.Entidades.Pedido;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private RecuperarStatusPedidoUC recuperarStatusUC;
    private PagarPedidoUC pagarPedidoUC;

    public PedidoController(RecuperarStatusPedidoUC recuperarStatusUC, PagarPedidoUC pagarPedidoUC){
        this.recuperarStatusUC = recuperarStatusUC;
        this.pagarPedidoUC = pagarPedidoUC;
    }

    @GetMapping("/{id}/status")
    @CrossOrigin("*")
    public ResponseEntity<StatusPedidoResponse> recuperaStatus(@PathVariable("id") long id){
        Pedido.Status st = recuperarStatusUC.run(id);
        if(st==null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(StatusPedidoResponse.naoEncontrado(id));
        }
        StatusPedidoResponse body = new StatusPedidoResponse(id, st.name(), descricaoStatus(st));
        return ResponseEntity.ok(body);
    }

    @PostMapping("/{id}/pagamento")
    @CrossOrigin("*")
    public ResponseEntity<StatusPedidoResponse> pagarPedido(@PathVariable("id") long id){
        boolean sucesso = pagarPedidoUC.run(id);
        if(!sucesso) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new StatusPedidoResponse(id, "ERRO", "Não foi possível processar o pagamento. Verifique se o pedido existe e está em estado válido."));
        }
        
        // Recupera o status atualizado
        Pedido.Status st = recuperarStatusUC.run(id);
        StatusPedidoResponse body = new StatusPedidoResponse(id, st.name(), descricaoStatus(st));
        return ResponseEntity.ok(body);
    }

    private String descricaoStatus(Pedido.Status st){
        switch (st){
            case NOVO: return "Pedido criado aguardando aprovação";
            case APROVADO: return "Pedido aprovado";
            case PAGO: return "Pagamento confirmado";
            case AGUARDANDO: return "Aguardando início do preparo";
            case PREPARACAO: return "Em preparação";
            case PRONTO: return "Pronto para envio";
            case TRANSPORTE: return "Em transporte";
            case ENTREGUE: return "Entregue ao cliente";
            default: return st.name();
        }
    }
}
