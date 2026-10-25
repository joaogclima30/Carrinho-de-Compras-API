package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.security.jwt.JwtUtils;
import io.github.joaogclima30.carrinhoAPI.security.user.UsuarioLojaDto;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.persistence.EntityManager;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Testes de contrato de segurança. Falhas são mantidas como evidências dos defeitos. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditoriaSecurityIntegrationTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired RoleRepository roles;
    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired PedidoRepository pedidos;
    @Autowired ImagemRepository imagens;
    @Autowired CarrinhoItemService itens;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtUtils jwt;
    @Autowired EntityManager em;
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper json;
    @Value("${auth.token.jwtSecret}") String secret;
    MockMvc mvc;
    Usuario alice, bob;
    Produto produto;
    long cartBob;

    @BeforeEach void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class)).build();
        Role role = roles.findByNome("ROLE_USER").orElseGet(() -> roles.save(new Role("ROLE_USER")));
        alice = usuario(role); bob = usuario(role);
        Categoria categoria = categorias.save(new Categoria("Audit-" + UUID.randomUUID()));
        produto = produtos.saveAndFlush(new Produto("Audit-" + UUID.randomUUID(), "Marca", new BigDecimal("19.99"), 10, "Descrição", categoria));
        cartBob = itens.adicionarItem(bob.getId(), null, produto.getId(), 1).getCarrinhoId();
        em.flush();
    }

    Usuario usuario(Role role) {
        Usuario usuario = new Usuario("Nome", "Teste", UUID.randomUUID() + "@example.com", encoder.encode("senha-auditoria"));
        usuario.getRoles().add(role);
        return usuarios.saveAndFlush(usuario);
    }

    String token(Usuario usuario) {
        var principal = UsuarioLojaDto.buildUserDetails(usuario);
        return jwt.generateTokenForUser(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    String tokenAssinado(String email, boolean expirado) {
        long now = System.currentTimeMillis();
        return Jwts.builder().setSubject(email).setIssuedAt(new Date(now - 120000))
                .setExpiration(new Date(expirado ? now - 60000 : now + 60000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret)), SignatureAlgorithm.HS256).compact();
    }

    @ParameterizedTest @ValueSource(strings = {"consultarUsuario", "alterarUsuario", "excluirUsuario", "listarPedidos", "finalizarPedido", "criarCategoria", "alterarCategoria", "excluirCategoria"})
    void operacaoSensivelSemAutenticacaoDeveRetornar401(String operacao) throws Exception {
        MockHttpServletRequestBuilder request = switch (operacao) {
            case "consultarUsuario" -> get("/api/v1/usuarios/{id}/listarUsuario", bob.getId());
            case "alterarUsuario" -> put("/api/v1/usuarios/{id}/atualizar", bob.getId()).contentType("application/json").content("{\"primeiroNome\":\"Alterado\",\"sobrenome\":\"Teste\"}");
            case "excluirUsuario" -> delete("/api/v1/usuarios/{id}/deletar", bob.getId());
            case "listarPedidos" -> get("/api/v1/pedidos/usuario/{id}", bob.getId());
            case "finalizarPedido" -> post("/api/v1/pedidos/{id}", bob.getId());
            case "criarCategoria" -> post("/api/v1/categorias/salvar").contentType("application/json").content("{\"nome\":\"Anon-" + UUID.randomUUID() + "\"}");
            case "alterarCategoria" -> put("/api/v1/categorias/categoria/{id}/atualizar", produto.getCategoria().getId()).contentType("application/json").content("{\"nome\":\"Alterada-" + UUID.randomUUID() + "\"}");
            default -> delete("/api/v1/categorias/categoria/{id}/delete", categorias.saveAndFlush(new Categoria("Vazia-" + UUID.randomUUID())).getId());
        };
        mvc.perform(request).andExpect(status().isUnauthorized());
    }

    @ParameterizedTest @ValueSource(strings = {"consultar", "total", "limpar", "consultarItem", "atualizarItem", "removerItem"})
    void usuarioNaoPodeOperarCarrinhoAlheio(String operacao) throws Exception {
        MockHttpServletRequestBuilder request = switch (operacao) {
            case "consultar" -> get("/api/v1/carrinhos/{id}/listarCarrinho", cartBob);
            case "total" -> get("/api/v1/carrinhos/{id}/total", cartBob);
            case "limpar" -> delete("/api/v1/carrinhos/{id}/limparCarrinho", cartBob);
            case "consultarItem" -> get("/api/v1/carrinhoItem/{id}/item/{produto}/listarCarrinho", cartBob, produto.getId());
            case "atualizarItem" -> put("/api/v1/carrinhoItem/{id}/item/{produto}/atualizar", cartBob, produto.getId()).param("quantidade", "2");
            default -> delete("/api/v1/carrinhoItem/{id}/item/{produto}/remover", cartBob, produto.getId());
        };
        mvc.perform(request.header("Authorization", "Bearer " + token(alice))).andExpect(status().isForbidden());
    }

    @Test void naoPodeAdicionarAoCarrinhoDeOutroUsuario() throws Exception {
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").header("Authorization", "Bearer " + token(alice))
                        .param("usuarioId", bob.getId().toString()).param("produtoId", produto.getId().toString()).param("quantidade", "1"))
                .andExpect(status().isForbidden());
    }

    @Test void tokenDeveSerSuficienteParaIdentificarCarrinhoNaInclusao() throws Exception {
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").header("Authorization", "Bearer " + token(alice))
                        .param("produtoId", produto.getId().toString()).param("quantidade", "1"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest @ValueSource(strings = {"ausente", "invalido", "expirado", "adulterado", "usuarioRemovido"})
    void falhaDeTokenDeveTer401EJson(String tipo) throws Exception {
        var request = get("/api/v1/carrinhos/{id}/listarCarrinho", cartBob);
        String value = switch (tipo) {
            case "invalido" -> "token-invalido";
            case "expirado" -> tokenAssinado(alice.getEmail(), true);
            case "usuarioRemovido" -> tokenAssinado("removido@example.com", false);
            case "adulterado" -> {
                String valid = token(alice); int pos = valid.lastIndexOf('.') + 1;
                yield valid.substring(0, pos) + (valid.charAt(pos) == 'A' ? 'B' : 'A') + valid.substring(pos + 1);
            }
            default -> null;
        };
        if (value != null) request.header("Authorization", "Bearer " + value);
        mvc.perform(request).andExpect(status().isUnauthorized()).andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test void jwtValidoPermiteConsultarProprioCarrinho() throws Exception {
        mvc.perform(get("/api/v1/carrinhos/{id}/listarCarrinho", cartBob).header("Authorization", "Bearer " + token(bob)))
                .andExpect(status().isOk());
    }

    String produtoJson() throws Exception {
        return json.writeValueAsString(Map.of("nome", "Novo-" + UUID.randomUUID(), "marca", "Marca", "preco", 10,
                "estoque", 3, "categoria", Map.of("nome", produto.getCategoria().getNome())));
    }

    @Test void usuarioComumNaoPodeCadastrarProduto() throws Exception {
        mvc.perform(post("/api/v1/produtos/salvar").header("Authorization", "Bearer " + token(alice))
                .contentType("application/json").content(produtoJson())).andExpect(status().isForbidden());
    }

    @Test void administradorPodeCadastrarProduto() throws Exception {
        Role admin = roles.findByNome("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));
        alice.getRoles().add(admin); usuarios.saveAndFlush(alice);
        mvc.perform(post("/api/v1/produtos/salvar").header("Authorization", "Bearer " + token(alice))
                .contentType("application/json").content(produtoJson())).andExpect(status().isOk());
    }

    @Test void cadastroDeveAtribuirRoleUserSemExporSenha() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "primeiroNome", "Novo", "sobrenome", "Teste", "email", email, "senha", "senha-auditoria"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.senha").doesNotExist());
        Usuario usuario = usuarios.findByEmail(email);
        assertTrue(encoder.matches("senha-auditoria", usuario.getSenha()));
        assertTrue(usuario.getRoles().stream().anyMatch(role -> "ROLE_USER".equals(role.getNome())), "Cadastro público deixou usuário sem ROLE_USER");
    }

    @Test void cadastroNaoAceitaEscaladaDeRolePorMassAssignment() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        var result = mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(json.writeValueAsString(Map.of(
                "primeiroNome", "Novo", "sobrenome", "Teste", "email", email, "senha", "senha-auditoria",
                "roles", List.of(Map.of("nome", "ROLE_ADMIN")))))).andReturn();
        int status = result.getResponse().getStatus();
        assertTrue(status == 200 || status == 400, "Esperado ignorar ou rejeitar campo não permitido");
        if (status == 200) {
            assertTrue(usuarios.findByEmail(email).getRoles().stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getNome())));
        }
    }

    @ParameterizedTest @ValueSource(strings = {"ascii", "unicode"})
    void senhaAcimaDoLimiteBcryptDeveRetornar400(String tipo) throws Exception {
        String senha = tipo.equals("ascii") ? "a".repeat(80) : "á".repeat(40);
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(json.writeValueAsString(Map.of(
                        "primeiroNome", "Novo", "sobrenome", "Teste", "email", UUID.randomUUID() + "@example.com", "senha", senha))))
                .andExpect(status().isBadRequest());
    }

    @Test void loginRealRetornaJwtUtilizavel() throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login").contentType("application/json").content(json.writeValueAsString(
                        Map.of("email", alice.getEmail(), "senha", "senha-auditoria"))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.token").isString()).andReturn();
        assertTrue(jwt.validateToken(json.readTree(result.getResponse().getContentAsString()).path("data").path("token").asText()));
    }

    @ParameterizedTest @ValueSource(strings = {"upload", "atualizar", "excluir"})
    void mutacaoDeImagemSemAutenticacaoDeveRetornar401(String operacao) throws Exception {
        var output = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", output);
        byte[] bytes = output.toByteArray();
        Imagem imagem = new Imagem(); imagem.setProduto(produto); imagem.setTipoArquivo("image/png");
        imagem.setNomeArquivo("teste.png"); imagem.setImagem(new javax.sql.rowset.serial.SerialBlob(bytes));
        imagens.saveAndFlush(imagem);
        if (operacao.equals("excluir")) {
            mvc.perform(delete("/api/v1/imagens/imagem/{id}/deletar", imagem.getId())).andExpect(status().isUnauthorized());
        } else {
            var request = operacao.equals("upload") ? multipart("/api/v1/imagens/upload")
                    : multipart(org.springframework.http.HttpMethod.PUT, "/api/v1/imagens/imagem/{id}/atualizar", imagem.getId());
            request.file(new org.springframework.mock.web.MockMultipartFile(operacao.equals("upload") ? "files" : "file", "teste.png", "image/png", bytes));
            if (operacao.equals("upload")) request.param("produtoId", produto.getId().toString());
            mvc.perform(request).andExpect(status().isUnauthorized());
        }
    }
}
