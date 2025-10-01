package com.pasfinal.Adaptadores.Apresentacao;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pasfinal.Aplicacao.RecuperarStatusPedidoUC;
import com.pasfinal.Aplicacao.CancelarPedidoUC;
import com.pasfinal.Aplicacao.Responses.StatusPedidoResponse;
import com.pasfinal.Dominio.Entidades.Pedido;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {
    private RecuperarStatusPedidoUC recuperarStatusUC;
    private CancelarPedidoUC cancelarPedidoUC;

    public PedidoController(RecuperarStatusPedidoUC recuperarStatusUC, CancelarPedidoUC cancelarPedidoUC){
        this.recuperarStatusUC = recuperarStatusUC;
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
}
