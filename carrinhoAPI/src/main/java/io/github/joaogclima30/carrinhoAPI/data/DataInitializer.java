package io.github.joaogclima30.carrinhoAPI.data;

import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UsuarioRepository usuarioRepository;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createDefaultUserIfNotExists();
    }

    private void createDefaultUserIfNotExists() {
        for (int i = 0; i <= 5; i++){
            String email = "usuario" +i+"@gmail.com";
            if(usuarioRepository.existsByEmail(email)){
                continue;
            }
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha("123");
            usuario.setPrimeiroNome("João");
            usuario.setSobrenome("lima" + i);
            usuarioRepository.save(usuario);
            System.out.println("Usuario Default " + i + "Criado com sucesso!");
        }
    }

}
