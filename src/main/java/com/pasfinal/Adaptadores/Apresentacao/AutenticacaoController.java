package com.pasfinal.Adaptadores.Apresentacao;

import com.pasfinal.Dominio.Entidades.Usuario;
import com.pasfinal.Dominio.Servicos.AutenticacaoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AutenticacaoController {
    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        Optional<Usuario> usuarioOpt = autenticacaoService.login(username, password);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Credenciais inválidas");
        }

        Usuario usuario = usuarioOpt.get();
        session.setAttribute("usuario", usuario);

        return ResponseEntity.ok(Map.of(
            "message", "Login realizado com sucesso",
            "usuario", usuario.getUsername(),
            "tipo", usuario.getTipo()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("username e password obrigatórios");
        }
        Usuario usuario = new Usuario(username, password, "USUARIO");
        boolean ok = autenticacaoService.registra(usuario);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Usuario já existe");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Usuário registrado", "id", usuario.getId()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("message", "Logout realizado com sucesso"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> status(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario == null) {
            return ResponseEntity.ok(Map.of("logado", false, "tipo", "PUBLICO"));
        }

        return ResponseEntity.ok(Map.of(
            "logado", true,
            "usuario", usuario.getUsername(),
            "tipo", usuario.getTipo()
        ));
    }
}