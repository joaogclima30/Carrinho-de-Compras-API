package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Sem @Transactional: verifica as transações reais de cada requisição e DTOs sem open-in-view.
@SpringBootTest
@ActiveProfiles("test")
class FluxosHttpIntegrationTest {
    @Autowired UsuarioRepository usuarios;
    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired CarrinhoRepository carrinhos;
    @Autowired PedidoRepository pedidos;
    @Autowired ImagemRepository imagens;
    @Autowired CarrinhoItemService itemService;
    @Autowired CarrinhoService carrinhoService;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired WebApplicationContext context;
    @Autowired ObjectMapper json;
    @Autowired EntityManager em;
    @Autowired RoleRepository roles;
    @Autowired io.github.joaogclima30.carrinhoAPI.security.jwt.JwtUtils jwtUtils;
    @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
    MockMvc mvc;
    TransactionTemplate tx;

    @BeforeEach void preparar() {
        tx = new TransactionTemplate(transactionManager);
        String token = tx.execute(status -> {
            Role role = roles.findByNome("ROLE_ADMIN").orElseGet(() -> roles.save(new Role("ROLE_ADMIN")));
            Usuario admin = new Usuario("Admin", "Teste", UUID.randomUUID() + "@example.com", encoder.encode("senha-teste"));
            admin.getRoles().add(role);
            usuarios.saveAndFlush(admin);
            var principal = io.github.joaogclima30.carrinhoAPI.security.user.UsuarioLojaDto.buildUserDetails(admin);
            return jwtUtils.generateTokenForUser(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities()));
        });
        // Este grupo testa contratos funcionais com um administrador autenticado.
        // A auditoria de propriedade e acesso anônimo fica em AuditoriaSecurityIntegrationTest.
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter.class))
                .defaultRequest(get("/").header("Authorization", "Bearer " + token)).build();
    }

    record Dados(Long usuarioId, Long produtoId, Long categoriaId, String nome, String categoria) {}

    private Dados dados() {
        return tx.execute(status -> {
            String sufixo = UUID.randomUUID().toString();
            Usuario usuario = usuarios.save(new Usuario("Nome", "Sobrenome", sufixo + "@example.com", "senha-teste"));
            Categoria categoria = categorias.save(new Categoria("Categoria-" + sufixo));
            Produto produto = produtos.save(new Produto("Produto-" + sufixo, "Marca", BigDecimal.TEN, 5, "Descrição", categoria));
            return new Dados(usuario.getId(), produto.getId(), categoria.getId(), produto.getNome(), categoria.getNome());
        });
    }

    private MvcResult adicionar(Dados dados) throws Exception {
        return mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("usuarioId", dados.usuarioId().toString())
                .param("produtoId", dados.produtoId().toString()).param("quantidade", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.carrinhoId").isNumber())
                .andExpect(jsonPath("$.data.itens[0].itemId").isNumber()).andReturn();
    }

    private long id(MvcResult result, String campo) throws Exception {
        return json.readTree(result.getResponse().getContentAsString()).get("data").get(campo).asLong();
    }

    private byte[] png() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "png", output);
        return output.toByteArray();
    }

    private MvcResult upload(Dados dados, String nome, byte[] bytes) throws Exception {
        return mvc.perform(multipart("/api/v1/imagens/upload")
                .file(new MockMultipartFile("files", nome, "image/png", bytes))
                .param("produtoId", dados.produtoId().toString())).andExpect(status().isOk()).andReturn();
    }

    private long imagemId(MvcResult result) throws Exception {
        return json.readTree(result.getResponse().getContentAsString()).get("data").get(0).get("id").asLong();
    }

    @Test void inclusaoRespeitaCarrinhoInformadoERetornaDto() throws Exception {
        Dados primeiro = dados(); Dados segundo = dados();
        long primeiroCart = id(adicionar(primeiro), "carrinhoId");
        long segundoCart = id(adicionar(segundo), "carrinhoId");
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("carrinhoId", Long.toString(segundoCart))
                .param("produtoId", segundo.produtoId().toString()).param("quantidade", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.carrinhoId").value(segundoCart))
                .andExpect(jsonPath("$.data.itens[0].quantidade").value(2));
        assertEquals(1, carrinhoService.listarCarrinhoDto(primeiroCart).getItens().iterator().next().getQuantidade());
    }

    @Test void rejeitaUsuarioDiferenteDoDonoDoCarrinho() throws Exception {
        Dados primeiro = dados(); Dados outro = dados();
        long cart = id(adicionar(primeiro), "carrinhoId");
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("carrinhoId", Long.toString(cart))
                .param("usuarioId", outro.usuarioId().toString()).param("produtoId", primeiro.produtoId().toString())
                .param("quantidade", "1")).andExpect(status().isConflict()).andExpect(jsonPath("$.status").value(409));
    }

    @Test void rejeitaInclusaoSemUsuarioNemCarrinho() throws Exception {
        Dados dados = dados();
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("produtoId", dados.produtoId().toString())
                .param("quantidade", "1")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @ParameterizedTest @ValueSource(ints = {0, -1})
    void quantidadeInvalidaNaoCriaCarrinho(int quantidade) throws Exception {
        Dados dados = dados();
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("usuarioId", dados.usuarioId().toString())
                .param("produtoId", dados.produtoId().toString()).param("quantidade", Integer.toString(quantidade)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        assertTrue(carrinhos.findByUsuarioId(dados.usuarioId()).isEmpty());
    }

    @Test void produtoInexistenteDesfazCriacaoDoCarrinho() throws Exception {
        Dados dados = dados();
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("usuarioId", dados.usuarioId().toString())
                .param("produtoId", Long.toString(Long.MAX_VALUE)).param("quantidade", "1"))
                .andExpect(status().isNotFound());
        assertTrue(carrinhos.findByUsuarioId(dados.usuarioId()).isEmpty());
    }

    @Test void estoqueInsuficienteDesfazCriacaoDoCarrinho() throws Exception {
        Dados dados = dados();
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem").param("usuarioId", dados.usuarioId().toString())
                .param("produtoId", dados.produtoId().toString()).param("quantidade", "6"))
                .andExpect(status().isConflict());
        assertTrue(carrinhos.findByUsuarioId(dados.usuarioId()).isEmpty());
    }

    @Test void duasPrimeirasInclusoesCriamSomenteUmCarrinho() throws Exception {
        Dados dados = dados(); CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Callable<Long> tarefa = () -> { inicio.await(); return itemService.adicionarItem(dados.usuarioId(), null, dados.produtoId(), 1).getCarrinhoId(); };
            Future<Long> primeiro = executor.submit(tarefa); Future<Long> segundo = executor.submit(tarefa);
            inicio.countDown();
            assertEquals(primeiro.get(25, TimeUnit.SECONDS), segundo.get(25, TimeUnit.SECONDS));
        }
        long cart = carrinhos.findByUsuarioId(dados.usuarioId()).orElseThrow().getId();
        assertEquals(2, carrinhoService.listarCarrinhoDto(cart).getItens().iterator().next().getQuantidade());
    }

    @Test void inicializacaoSincronizaUsuarioECarrinhoNaMesmaTransacao() {
        Dados dados = dados();
        tx.executeWithoutResult(status -> {
            Usuario usuario = usuarios.findById(dados.usuarioId()).orElseThrow();
            Carrinho carrinho = carrinhoService.initializeNovoCarrinho(usuario);
            assertSame(carrinho, usuario.getCarrinho());
            assertEquals(usuario.getId(), carrinho.getUsuario().getId());
        });
    }

    @Test void fluxoHttpCompletoPreservaPedidoAoTentarExcluirUsuario() throws Exception {
        Dados dados = dados();
        upload(dados, "produto.png", png());
        long cart = id(adicionar(dados), "carrinhoId");
        mvc.perform(get("/api/v1/usuarios/{id}/listarUsuario", dados.usuarioId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.senha").doesNotExist())
                .andExpect(jsonPath("$.data.carrinho.carrinhoId").value(cart));
        mvc.perform(get("/api/v1/carrinhoItem/{id}/item/{produto}/listarCarrinho", cart, dados.produtoId())).andExpect(status().isOk());
        mvc.perform(put("/api/v1/carrinhoItem/{id}/item/{produto}/atualizar", cart, dados.produtoId()).param("quantidade", "2")).andExpect(status().isOk());
        MvcResult pedido = mvc.perform(post("/api/v1/pedidos/{id}", dados.usuarioId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.item[0].quantidade").value(2)).andReturn();
        long pedidoId = id(pedido, "pedidoId");
        mvc.perform(get("/api/v1/pedidos/{id}", pedidoId)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/pedidos/usuario/{id}", dados.usuarioId())).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/usuarios/{id}/deletar", dados.usuarioId()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.mensagem").value("Usuário possui pedidos e não pode ser excluído"));
        assertTrue(pedidos.existsById(pedidoId)); assertTrue(usuarios.existsById(dados.usuarioId()));
        assertTrue(carrinhoService.listarCarrinhoDto(cart).getItens().isEmpty());
    }

    @Test void usuarioSemPedidosPodeSerExcluido() throws Exception {
        Dados dados = dados(); adicionar(dados);
        mvc.perform(delete("/api/v1/usuarios/{id}/deletar", dados.usuarioId())).andExpect(status().isOk());
        assertFalse(usuarios.existsById(dados.usuarioId()));
    }

    @Test void cadastroNormalizaEmailERejeitaDuplicado() throws Exception {
        String email = UUID.randomUUID() + "@EXAMPLE.COM";
        String body = json.writeValueAsString(Map.of("primeiroNome", "Nome", "sobrenome", "Sobrenome", "email", "  " + email + "  ", "senha", "senha-teste"));
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(body))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.email").value(email.toLowerCase(Locale.ROOT)))
                .andExpect(jsonPath("$.data.senha").doesNotExist());
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(body))
                .andExpect(status().isConflict());
        assertNotNull(usuarios.findByEmail(email));
    }

    @Test void buscaReconheceEmailLegadoComEspacosEMaiusculas() {
        Dados dados = dados(); String email = UUID.randomUUID() + "@EXAMPLE.COM";
        tx.executeWithoutResult(status -> em.createNativeQuery("update usuario set email = :email where id = :id")
                .setParameter("email", "  " + email + "  ").setParameter("id", dados.usuarioId()).executeUpdate());
        assertTrue(usuarios.existsByEmail(email.toLowerCase(Locale.ROOT)));
        assertEquals(dados.usuarioId(), usuarios.findByEmail(email).getId());
    }

    @ParameterizedTest @ValueSource(strings = {"primeiroNome", "sobrenome", "senha"})
    void cadastroRejeitaCamposLongos(String campo) throws Exception {
        Map<String, String> body = new HashMap<>(Map.of("primeiroNome", "Nome", "sobrenome", "Sobrenome", "email", UUID.randomUUID() + "@example.com", "senha", "senha-teste"));
        body.put(campo, "a".repeat(256));
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content(json.writeValueAsString(body)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void imagensValidasTemDownloadAtualELegado() throws Exception {
        Dados dados = dados(); byte[] bytes = png();
        MvcResult result = upload(dados, "foto.png", bytes);
        long id = imagemId(result);
        assertEquals("/api/v1/imagens/imagem/download/" + id,
                json.readTree(result.getResponse().getContentAsString()).get("data").get(0).get("downloadUrl").asString());
        for (String caminho : List.of("download", "dowload")) {
            mvc.perform(get("/api/v1/imagens/imagem/" + caminho + "/{id}", id))
                    .andExpect(status().isOk()).andExpect(content().contentType("image/png"))
                    .andExpect(content().bytes(bytes));
        }
    }

    @Test void metadadosAntigosGanhamUrlCalculada() throws Exception {
        Dados dados = dados(); long id = imagemId(upload(dados, "foto.png", png()));
        tx.executeWithoutResult(status -> { Imagem imagem = imagens.findById(id).orElseThrow(); imagem.setDownloadUrl("/url-antiga-invalida"); });
        mvc.perform(get("/api/v1/produtos/{id}/listarPorId", dados.produtoId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.imagensDto[0].downloadUrl").value("/api/v1/imagens/imagem/download/" + id));
    }

    @Test void nomeDeDownloadRemoveCaminhoEQuebrasDeLinha() throws Exception {
        Dados dados = dados(); MvcResult result = upload(dados, "..\\pasta\\fo\r\nto.png", png());
        assertEquals("foto.png", json.readTree(result.getResponse().getContentAsString()).get("data").get(0).get("arquivoNome").asString());
        String header = mvc.perform(get("/api/v1/imagens/imagem/download/{id}", imagemId(result)))
                .andExpect(status().isOk()).andReturn().getResponse().getHeader("Content-Disposition");
        assertFalse(header.contains("\r")); assertFalse(header.contains("\n")); assertFalse(header.contains("pasta"));
    }

    @Test void downloadAceitaTipoAntigoAusenteComOctetStream() throws Exception {
        Dados dados = dados(); long id = imagemId(upload(dados, "foto.png", png()));
        tx.executeWithoutResult(status -> imagens.findById(id).orElseThrow().setTipoArquivo(null));
        mvc.perform(get("/api/v1/imagens/imagem/download/{id}", id)).andExpect(status().isOk())
                .andExpect(content().contentType("application/octet-stream"));
    }

    @Test void arquivoDisfarcadoDeImagemEhRejeitado() throws Exception {
        Dados dados = dados();
        mvc.perform(multipart("/api/v1/imagens/upload").file(new MockMultipartFile("files", "fake.png", "image/png", "texto".getBytes()))
                .param("produtoId", dados.produtoId().toString())).andExpect(status().isBadRequest());
        assertTrue(imagens.findByProdutoId(dados.produtoId()).isEmpty());
    }

    @Test void loteComArquivoInvalidoNaoSalvaParteDasImagens() throws Exception {
        Dados dados = dados();
        mvc.perform(multipart("/api/v1/imagens/upload")
                .file(new MockMultipartFile("files", "foto.png", "image/png", png()))
                .file(new MockMultipartFile("files", "fake.png", "image/png", "texto".getBytes()))
                .param("produtoId", dados.produtoId().toString())).andExpect(status().isBadRequest());
        assertTrue(imagens.findByProdutoId(dados.produtoId()).isEmpty());
    }

    @Test void uploadRejeitaArquivoVazio() throws Exception {
        Dados dados = dados();
        mvc.perform(multipart("/api/v1/imagens/upload").file(new MockMultipartFile("files", "foto.png", "image/png", new byte[0]))
                .param("produtoId", dados.produtoId().toString())).andExpect(status().isBadRequest());
    }

    @Test void uploadRespeitaLimiteDeQuantidade() throws Exception {
        Dados dados = dados(); var request = multipart("/api/v1/imagens/upload").param("produtoId", dados.produtoId().toString());
        for (int i = 0; i < 6; i++) request.file(new MockMultipartFile("files", "foto.png", "image/png", png()));
        mvc.perform(request).andExpect(status().isBadRequest());
        assertTrue(imagens.findByProdutoId(dados.produtoId()).isEmpty());
    }

    @Test void uploadRespeitaLimiteDeTamanho() throws Exception {
        Dados dados = dados();
        mvc.perform(multipart("/api/v1/imagens/upload").file(new MockMultipartFile("files", "foto.png", "image/png", new byte[5 * 1024 * 1024 + 1]))
                .param("produtoId", dados.produtoId().toString())).andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.status").value(413));
    }

    @Test void atualizarEExcluirImagem() throws Exception {
        Dados dados = dados(); long id = imagemId(upload(dados, "foto.png", png()));
        mvc.perform(multipart("/api/v1/imagens/imagem/{id}/atualizar", id)
                .file(new MockMultipartFile("file", "nova.png", "image/png", png()))
                .with(request -> { request.setMethod("PUT"); return request; })).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/imagens/imagem/{id}/deletar", id)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/imagens/imagem/download/{id}", id)).andExpect(status().isNotFound());
    }

    @Test void consultasDeCatalogoFuncionamSemOpenInView() throws Exception {
        Dados dados = dados(); upload(dados, "foto.png", png());
        for (String url : List.of("/api/v1/produtos/all", "/api/v1/produtos/" + dados.produtoId() + "/listarPorId",
                "/api/v1/produtos/produto/by-marca?marca=Marca", "/api/v1/produtos/" + dados.nome() + "/produtos",
                "/api/v1/produtos/by/marca-and-nomeProduto?marca=Marca&nomeProduto=" + dados.nome(),
                "/api/v1/produtos/by/categoria-and-marca?categoria=" + dados.categoria() + "&marca=Marca",
                "/api/v1/produtos/produto/" + dados.categoria() + "/all/produtos", "/api/v1/categorias/todas")) {
            mvc.perform(get(url)).andExpect(status().isOk());
        }
    }

    @Test void jsonInvalidoTemErroPadronizado() throws Exception {
        mvc.perform(post("/api/v1/usuarios").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400)).andExpect(jsonPath("$.mensagem").isString());
    }

    @Test void parametroInvalidoTemErroPadronizado() throws Exception {
        mvc.perform(get("/api/v1/usuarios/abc/listarUsuario"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void parametroAusenteTemErroPadronizado() throws Exception {
        mvc.perform(post("/api/v1/carrinhoItem/adicionarItem"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
    }

    @Test void uploadComContentTypeErradoRetorna415() throws Exception {
        mvc.perform(post("/api/v1/imagens/upload").contentType("application/json").content("{}"))
                .andExpect(status().isUnsupportedMediaType()).andExpect(jsonPath("$.status").value(415));
    }

    @Test void rotaInexistenteRetorna404Padronizado() throws Exception {
        mvc.perform(get("/api/v1/rota-inexistente"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.status").value(404));
    }
}
