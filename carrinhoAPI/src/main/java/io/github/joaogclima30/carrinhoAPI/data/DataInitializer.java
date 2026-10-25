package io.github.joaogclima30.carrinhoAPI.data;

import io.github.joaogclima30.carrinhoAPI.model.Role;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.RoleRepository;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Component
@Profile("dev")
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {
    private final UsuarioRepository repository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${DEV_SEED_PASSWORD:}")
    private String senha;

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Set<String> defaultRoles = Set.of("ROLE_ADMIN", "ROLE_USER");
        createDefaultRoleIfNotExist(defaultRoles);
        createDefaultUserIfNotExist();
        createDefaultAdminIfNotExist();

    }

    private void createDefaultUserIfNotExist(){
        Role userRole = roleRepository.findByNome("ROLE_USER").get();
        for  (int i = 0; i <= 5; i++) {
            String email =  "usuario" + i + "@gmail.com";
            if (repository.existsByEmail(email)) {
                continue;
            }
            if (senha == null || senha.isBlank()) {
                throw new IllegalStateException(
                        "Configure DEV_SEED_PASSWORD para criar os usuários de desenvolvimento");
            }
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPrimeiroNome("João ");
            usuario.setSobrenome("Gabriel" + i);
            usuario.setRoles(Set.of(userRole));
            repository.save(usuario);
        }
    }

    private void createDefaultRoleIfNotExist(Set<String> roles){
        roles.stream()
                .filter(role -> roleRepository.findByNome(role).isEmpty())
                .map(Role :: new).forEach(roleRepository::save);

    }

    private void createDefaultAdminIfNotExist(){
        Role adminRole = roleRepository.findByNome("ROLE_ADMIN").get();
        for  (int i = 0; i <= 2; i++) {
            String email =  "admin" + i + "@gmail.com";
            if (repository.existsByEmail(email)) {
                continue;
            }
            if (senha == null || senha.isBlank()) {
                throw new IllegalStateException(
                        "Configure DEV_SEED_PASSWORD para criar os usuários de desenvolvimento");
            }
            Usuario usuario = new Usuario();
            usuario.setEmail(email);
            usuario.setSenha(passwordEncoder.encode(senha));
            usuario.setPrimeiroNome("Admin ");
            usuario.setSobrenome("Admin" + i);
            usuario.setRoles(Set.of(adminRole));
            repository.save(usuario);
            System.out.println("Default admin user " + i + " criado com sucesso" );
        }
    }
}
