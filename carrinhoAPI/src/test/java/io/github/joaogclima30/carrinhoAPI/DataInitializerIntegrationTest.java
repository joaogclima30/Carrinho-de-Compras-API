package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.data.DataInitializer;
import io.github.joaogclima30.carrinhoAPI.model.Role;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.RoleRepository;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(showSql = false)
@ActiveProfiles("test")
class DataInitializerIntegrationTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired RoleRepository roles;
    @Autowired EntityManager entityManager;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private DataInitializer initializer;

    @BeforeEach
    void preparar() {
        initializer = new DataInitializer(usuarios, roles, encoder);
        ReflectionTestUtils.setField(initializer, "senha", "senha-seed-teste");
    }

    @Test
    void criaRolesEUsuariosComSenhaCodificadaSemDuplicarAoRepetir() {
        initializer.onApplicationEvent(null);
        initializer.onApplicationEvent(null);
        entityManager.flush();
        entityManager.clear();

        assertEquals(2, roles.count());
        assertTrue(roles.findByNome("ROLE_ADMIN").isPresent());
        assertEquals(9, usuarios.count());
        for (int i = 0; i <= 5; i++) {
            Usuario usuario = usuarios.findByEmail("usuario" + i + "@gmail.com");
            assertNotNull(usuario);
            assertTrue(encoder.matches("senha-seed-teste", usuario.getSenha()));
            assertEquals(1, usuario.getRoles().size());
            assertEquals("ROLE_USER", usuario.getRoles().iterator().next().getNome());
        }
        for (int i = 0; i <= 2; i++) {
            Usuario admin = usuarios.findByEmail("admin" + i + "@gmail.com");
            assertNotNull(admin);
            assertTrue(encoder.matches("senha-seed-teste", admin.getSenha()));
            assertEquals("ROLE_ADMIN", admin.getRoles().iterator().next().getNome());
        }
    }

    @Test
    void preservaDadosERolesDoUsuarioJaExistente() {
        Role admin = roles.save(new Role("ROLE_ADMIN"));
        Usuario existente = new Usuario("Nome original", "Sobrenome original",
                "usuario0@gmail.com", encoder.encode("senha-original"));
        existente.getRoles().add(admin);
        usuarios.save(existente);

        initializer.onApplicationEvent(null);
        entityManager.flush();
        entityManager.clear();

        Usuario atualizado = usuarios.findByEmail("usuario0@gmail.com");
        assertEquals("Nome original", atualizado.getPrimeiroNome());
        assertTrue(encoder.matches("senha-original", atualizado.getSenha()));
        assertEquals(1, atualizado.getRoles().size());
        assertTrue(atualizado.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getNome())));
    }

    @Test
    void exigeSenhaConfiguradaAntesDeCriarUsuarios() {
        ReflectionTestUtils.setField(initializer, "senha", " ");

        IllegalStateException erro = assertThrows(IllegalStateException.class,
                () -> initializer.onApplicationEvent(null));

        assertTrue(erro.getMessage().contains("DEV_SEED_PASSWORD"));
        assertEquals(0, usuarios.count());
    }
}
