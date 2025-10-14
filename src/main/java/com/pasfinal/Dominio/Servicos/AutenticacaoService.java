package com.pasfinal.Dominio.Servicos;

import com.pasfinal.Dominio.Dados.UsuarioRepository;
import com.pasfinal.Dominio.Entidades.Usuario;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarioRepository;

    public AutenticacaoService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Optional<Usuario> login(String username, String password) {
        return usuarioRepository.findByUsernameAndPassword(username, password);
    }

    public boolean isAdmin(Usuario usuario) {
        return usuario != null && usuario.isAdmin();
    }

    public boolean isUsuario(Usuario usuario) {
        return usuario != null && usuario.isUsuario();
    }
}