package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.exceptions.DadosInvalidosException;
import io.github.joaogclima30.carrinhoAPI.exceptions.GlobalExceptionHandler;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtAccessDeniedHandler;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtAuthEntryPoint;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = SecurityErrorResponseIntegrationTest.Config.class)
@WebAppConfiguration
class SecurityErrorResponseIntegrationTest {
    @Autowired WebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
    }

    @Test
    void anonimoEmMetodoAdminRecebeMensagemPersonalizada401() throws Exception {
        mvc.perform(get("/teste/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Voce precisa logar e tentar novamente"));
    }

    @Test
    void usuarioSemRoleAdminRecebeMensagem403() throws Exception {
        mvc.perform(get("/teste/admin").header("X-Test-Role", "ROLE_USER"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$", aMapWithSize(2)))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Você não tem permissão para realizar esta operação"));
    }

    @Test
    void adminContinuaComAcessoAoMetodo() throws Exception {
        mvc.perform(get("/teste/admin").header("X-Test-Role", "ROLE_ADMIN"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.resultado").value("ok"));
    }

    @Test
    void anonimoEmRotaProtegidaTambemRecebeMensagem401() throws Exception {
        mvc.perform(get("/teste/protegido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Voce precisa logar e tentar novamente"));
    }

    @Test
    void erroDeNegocioContinua400() throws Exception {
        mvc.perform(get("/teste/invalido"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Dados inválidos no teste"));
    }

    @Test
    void erroInesperadoContinua500() throws Exception {
        mvc.perform(get("/teste/inesperado"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.mensagem").value("Erro interno ao processar a requisição"));
    }

    @TestConfiguration
    @EnableWebMvc
    @EnableWebSecurity
    @EnableMethodSecurity
    @Import({GlobalExceptionHandler.class, JwtAuthEntryPoint.class,
            JwtAccessDeniedHandler.class, TestController.class})
    static class Config {
        @Bean
        SecurityFilterChain testChain(HttpSecurity http, JwtAuthEntryPoint entryPoint,
                                     JwtAccessDeniedHandler deniedHandler) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable)
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .exceptionHandling(errors -> errors.authenticationEntryPoint(entryPoint)
                            .accessDeniedHandler(deniedHandler))
                    .authorizeHttpRequests(auth -> auth.requestMatchers("/teste/protegido").authenticated()
                            .anyRequest().permitAll());
            // Autenticação sintética exclusiva deste teste; nenhum token ou banco real é utilizado.
            http.addFilterBefore((request, response, chain) -> {
                String role = ((jakarta.servlet.http.HttpServletRequest) request).getHeader("X-Test-Role");
                if (role != null) {
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken("teste", null,
                                    List.of(new SimpleGrantedAuthority(role))));
                }
                chain.doFilter(request, response);
            }, AnonymousAuthenticationFilter.class);
            return http.build();
        }
    }

    @RestController
    @TestComponent
    static class TestController {
        @GetMapping("/teste/admin")
        @PreAuthorize("hasRole('ADMIN')")
        public Map<String, String> admin() { return Map.of("resultado", "ok"); }

        @GetMapping("/teste/protegido")
        public Map<String, String> protegido() { return Map.of("resultado", "ok"); }

        @GetMapping("/teste/invalido")
        public void invalido() { throw new DadosInvalidosException("Dados inválidos no teste"); }

        @GetMapping("/teste/inesperado")
        public void inesperado() { throw new IllegalStateException("Falha simulada no teste"); }
    }
}
