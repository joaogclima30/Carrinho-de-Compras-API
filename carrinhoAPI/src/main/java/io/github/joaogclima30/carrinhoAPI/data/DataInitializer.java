package io.github.joaogclima30.carrinhoAPI.data;

import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UsuarioRepository repository;
    @Value("${DEV_SEED_PASSWORD:}")
    private String senha;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (senha.length() < 8 || senha.length() > 255 || senha.isBlank()) {
            throw new IllegalStateException("Configure DEV_SEED_PASSWORD com 8 a 255 caracteres para habilitar os usuários de exemplo");
        }
        for (int i = 0; i <= 5; i++) {
            String email = "usuario" + i + "@example.com";
            if (!repository.existsByEmail(email)) {
                repository.save(new Usuario("Usuário", "Exemplo " + i, email, senha));
                log.info("Usuário de desenvolvimento {} criado", i);
            }
        }
    }
}
