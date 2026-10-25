package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.controller.AuthController;
import io.github.joaogclima30.carrinhoAPI.exceptions.GlobalExceptionHandler;
import io.github.joaogclima30.carrinhoAPI.model.Role;
import io.github.joaogclima30.carrinhoAPI.model.Usuario;
import io.github.joaogclima30.carrinhoAPI.repository.UsuarioRepository;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtUtils;
import io.github.joaogclima30.carrinhoAPI.security.user.UsuarioLojaDtoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerLoginTest {
    private MockMvc mvc;
    private JwtUtils jwtUtils;
    private UsuarioLojaDtoService userDetailsService;

    @BeforeEach
    void preparar() {
        var encoder = new BCryptPasswordEncoder();
        var repository = mock(UsuarioRepository.class);
        Usuario usuario = new Usuario("Nome", "Teste", "login@example.com", encoder.encode("senha-teste"));
        usuario.setId(10L);
        usuario.setRoles(Set.of(new Role("ROLE_USER")));
        when(repository.findByEmail("login@example.com")).thenReturn(usuario);
        userDetailsService = new UsuarioLojaDtoService(repository);
        var provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(encoder);
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret",
                java.util.Base64.getEncoder().encodeToString("01234567890123456789012345678901".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        ReflectionTestUtils.setField(jwtUtils, "expirationTime", 60000);
        var controller = new AuthController(new ProviderManager(provider), jwtUtils);
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .addPlaceholderValue("api.prefix", "/api/v1")
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @AfterEach
    void limparContexto() { SecurityContextHolder.clearContext(); }

    @Test
    void loginCorretoRetornaIdETokenValido() throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"email\":\"login@example.com\",\"senha\":\"senha-teste\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.senha").doesNotExist()).andReturn();
        String token = new ObjectMapper().readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("login@example.com", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void senhaErradaRetorna401Generico() throws Exception {
        verificarCredenciaisInvalidas("login@example.com", "incorreta");
    }

    @Test
    void emailInexistenteRetornaOMesmo401() throws Exception {
        verificarCredenciaisInvalidas("inexistente@example.com", "senha-teste");
    }

    private void verificarCredenciaisInvalidas(String email, String senha) throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(java.util.Map.of("email", email, "senha", senha))))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void emailEmBrancoRetorna400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"email\":\"\",\"senha\":\"senha-teste\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void senhaAusenteRetorna400() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType("application/json")
                        .content("{\"email\":\"login@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscaDeUsuarioInexistenteLancaExcecaoDeAutenticacao() {
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("inexistente@example.com"));
    }
}
