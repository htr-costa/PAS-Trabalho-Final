package com.pasfinal.Adaptadores.Dados;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pasfinal.Dominio.Dados.PedidoRepository;
import com.pasfinal.Dominio.Entidades.Cliente;
import com.pasfinal.Dominio.Entidades.Pedido;

@Repository
public class PedidoRepositoryJDBC implements PedidoRepository {
    private JdbcTemplate jdbcTemplate;

    public PedidoRepositoryJDBC(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<Pedido> mapperPedidoBasico = new RowMapper<Pedido>(){
        @Override
        public Pedido mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String status = rs.getString("status");
            Pedido.Status st = Pedido.Status.valueOf(status);
            // Para simplificar, não carregamos cliente nem itens agora
            return new Pedido(id, new Cliente(rs.getString("cliente_cpf"), "", "", "", ""), 
                (LocalDateTime)null, List.of(), st, 0,0,0,0);
        }
    };

    @Override
    public Pedido recuperaPorId(long id) {
        List<Pedido> lst = jdbcTemplate.query("select id, cliente_cpf, status from pedidos where id=?", mapperPedidoBasico, id);
        if(lst.isEmpty()) return null;
        return lst.get(0);
    }

    @Override
    public void salva(Pedido pedido) {
        int updated = jdbcTemplate.update("update pedidos set status=? where id=?", pedido.getStatus().name(), pedido.getId());
        if(updated==0){
            jdbcTemplate.update("insert into pedidos(id,cliente_cpf,status) values(?,?,?)", pedido.getId(), pedido.getCliente().getCpf(), pedido.getStatus().name());
        }
    }
}
