package com.pasfinal.Adaptadores.Apresentacao;

import java.util.List;

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
import com.pasfinal.Aplicacao.CancelarPedidoUC;
import com.pasfinal.Aplicacao.Responses.StatusPedidoResponse;
import com.pasfinal.Dominio.Entidades.Pedido;
import org.springframework.web.bind.annotation.RequestBody;
import com.pasfinal.Aplicacao.Requests.SubmeterPedidoRequest;
import com.pasfinal.Aplicacao.Responses.SubmeterPedidoResponse;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private RecuperarStatusPedidoUC recuperarStatusUC;
    private PagarPedidoUC pagarPedidoUC;
    private com.pasfinal.Aplicacao.SubmeterPedidoUC submeterPedidoUC;
    private CancelarPedidoUC cancelarPedidoUC;

    public PedidoController(RecuperarStatusPedidoUC recuperarStatusUC, PagarPedidoUC pagarPedidoUC, com.pasfinal.Aplicacao.SubmeterPedidoUC submeterPedidoUC, CancelarPedidoUC cancelarPedidoUC){
        this.recuperarStatusUC = recuperarStatusUC;
        this.pagarPedidoUC = pagarPedidoUC;
        this.submeterPedidoUC = submeterPedidoUC;
        this.cancelarPedidoUC = cancelarPedidoUC;
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
                .body(new StatusPedidoResponse(id, "ERRO", "Não foi possível processar o pagamento. Verifique se o pedido existe e está em estado APROVADO."));
        }
        
        Pedido.Status st = recuperarStatusUC.run(id);
        StatusPedidoResponse body = new StatusPedidoResponse(id, st.name(), "Pagamento confirmado com sucesso! Pedido enviado para a cozinha.");
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
            case CANCELADO: return "Pedido cancelado";
            default: return st.name();
        }
    }

    @PostMapping("/{id}/cancelamento")
    @CrossOrigin("*")
    public ResponseEntity<StatusPedidoResponse> cancela(@PathVariable("id") long id){
        boolean ok = cancelarPedidoUC.run(id);
        if(!ok){
            Pedido.Status st = recuperarStatusUC.run(id);
            if(st==null){
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new StatusPedidoResponse(id, "NAO_ENCONTRADO", "Pedido não encontrado"));
            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new StatusPedidoResponse(id, st.name(), "Pedido não pode ser cancelado no status atual"));
        }
        return ResponseEntity.ok(new StatusPedidoResponse(id, Pedido.Status.CANCELADO.name(), "Pedido cancelado"));
    }
    
    @PostMapping
    @CrossOrigin("*")
    public ResponseEntity<SubmeterPedidoResponse> submeterPedido(@RequestBody SubmeterPedidoRequest req) {
        try {
            SubmeterPedidoResponse resp = submeterPedidoUC.run(req);
            if ("NEGADO".equals(resp.getStatus())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(SubmeterPedidoResponse.pedidoNegado(req.getId(), List.of()));
        }
    }
}
