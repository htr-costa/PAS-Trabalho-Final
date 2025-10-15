package com.pasfinal.Adaptadores.Apresentacao;

import com.pasfinal.Dominio.Dados.ClienteRepository;
import com.pasfinal.Dominio.Entidades.Cliente;
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
    private final ClienteRepository clienteRepository;

    public AutenticacaoController(AutenticacaoService autenticacaoService, ClienteRepository clienteRepository) {
        this.autenticacaoService = autenticacaoService;
        this.clienteRepository = clienteRepository;
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
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "email e password são obrigatórios"));
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(email);
        usuario.setPassword(password);
        usuario.setTipo("USUARIO");

        boolean ok = autenticacaoService.registra(usuario);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email já cadastrado"));
        }

        String cpf = body.get("cpf");
        String nome = body.get("nome");
        if (cpf != null && nome != null) {
            try {
                Cliente existente = clienteRepository.recuperaPorCpf(cpf);
                if (existente == null) {
                    Cliente c = new Cliente(cpf, nome,
                        body.getOrDefault("celular", ""),
                        body.getOrDefault("endereco", ""),
                        email);
                    clienteRepository.salva(c);
                }
            } catch (Exception ignored) {
                // não atrapalhar o fluxo principal; opcional log
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Usuário registrado", "username", email));
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