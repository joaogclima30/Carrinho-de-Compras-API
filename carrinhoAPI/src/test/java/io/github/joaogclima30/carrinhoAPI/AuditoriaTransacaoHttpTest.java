package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtUtils;
import io.github.joaogclima30.carrinhoAPI.security.user.UsuarioLojaDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Sem transação de teste: verifica o que realmente fica commitado após uma requisição que falha. */
@SpringBootTest
@ActiveProfiles("test")
class AuditoriaTransacaoHttpTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired RoleRepository roles;
    @Autowired CategoriaRepository categorias;
    @Autowired ProdutoRepository produtos;
    @Autowired CarrinhoRepository carrinhos;
    @Autowired JwtUtils jwt;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager manager;
    @Autowired WebApplicationContext context;

    @ParameterizedTest @ValueSource(strings = {"produtoInexistente", "estoqueInsuficiente"})
    void falhaNaInclusaoNaoDeveCommitarCarrinhoVazio(String motivo) throws Exception {
        Object[] fixture = new TransactionTemplate(manager).execute(status -> {
            Role role = roles.findByNome("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
            Usuario usuario = new Usuario("Nome", "Teste", UUID.randomUUID() + "@example.com", encoder.encode("senha-auditoria"));
            usuario.getRoles().add(role); usuarios.saveAndFlush(usuario);
            Categoria categoria = categorias.save(new Categoria("Tx-" + UUID.randomUUID()));
            Produto produto = produtos.saveAndFlush(new Produto("Tx-" + UUID.randomUUID(), "Marca", BigDecimal.TEN, 1, "Desc", categoria));
            var principal = UsuarioLojaDto.buildUserDetails(usuario);
            String token = jwt.generateTokenForUser(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
            return new Object[]{usuario.getId(), produto.getId(), token};
        });
        MockMvc mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter.class)).build();
        boolean inexistente = motivo.equals("produtoInexistente");
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").header("Authorization", "Bearer " + fixture[2])
                        .param("usuarioId", fixture[0].toString())
                        .param("produtoId", inexistente ? Long.toString(Long.MAX_VALUE) : fixture[1].toString())
                        .param("quantidade", inexistente ? "1" : "2"))
                .andExpect(inexistente ? status().isNotFound() : status().isConflict());
        assertTrue(carrinhos.findByUsuarioId((Long) fixture[0]).isEmpty(),
                "O controller commitou a criação antes de a inclusão falhar");
    }
}
