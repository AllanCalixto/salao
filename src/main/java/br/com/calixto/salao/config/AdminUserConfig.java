package br.com.calixto.salao.config;

import br.com.calixto.salao.model.Usuario;
import br.com.calixto.salao.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class AdminUserConfig implements CommandLineRunner {

    private UsuarioRepository usuarioRepository;

    private BCryptPasswordEncoder passwordEncoder;

    public AdminUserConfig(UsuarioRepository usuarioRepository, BCryptPasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        var usuarioAdmin = usuarioRepository.findByLogin("admin");

        usuarioAdmin.ifPresentOrElse(
                usuario -> {
                    System.out.println("admin já existe!");
                },
                () -> {
                    Usuario usuario = new Usuario();
                    usuario.setLogin("admin");
                    usuario.setSenha(passwordEncoder.encode("123"));
                    usuarioRepository.save(usuario);
                }
        );



    }
}
