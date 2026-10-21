package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaRequestDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioCriarDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoriaJaCriada;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.*;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.*;
import io.github.joaogclima30.carrinhoAPI.service.Pedido.PedidoService;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import io.github.joaogclima30.carrinhoAPI.service.Categoria.CategoriaService;
import io.github.joaogclima30.carrinhoAPI.service.Usuario.UsuarioService;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RegrasCompraIntegrationTest {
    @Autowired CarrinhoRepository carrinhos;
    @Autowired CarrinhoItemRepository itens;
    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired PedidoRepository pedidos;
    @Autowired CarrinhoItemService itemService;
    @Autowired CarrinhoService carrinhoService;
    @Autowired PedidoService pedidoService;
    @Autowired ProdutoService produtoService;
    @Autowired CategoriaService categoriaService;
    @Autowired UsuarioService usuarioService;
    @Autowired EntityManager em;
    @Autowired WebApplicationContext context;
    MockMvc mvc;

    @BeforeEach void prepararHttp() { mvc = MockMvcBuilders.webAppContextSetup(context).build(); }

    private Produto produto(int estoque) {
        Categoria categoria = categorias.save(new Categoria("Categoria-" + UUID.randomUUID()));
        return produtos.saveAndFlush(new Produto("Produto-" + UUID.randomUUID(), "Marca", new BigDecimal("10.00"), estoque, "Descrição", categoria));
    }

    private Carrinho carrinho() {
        Usuario usuario = usuarios.save(new Usuario("Nome", "Sobrenome", UUID.randomUUID() + "@example.com", "senha-teste"));
        Carrinho carrinho = new Carrinho();
        carrinho.setUsuario(usuario);
        usuario.setCarrinho(carrinho);
        return carrinhos.saveAndFlush(carrinho);
    }

    private ProdutoRequestDTO dto(String nome, String marca, String categoria) {
        ProdutoRequestDTO dto = new ProdutoRequestDTO();
        dto.setNome(nome); dto.setMarca(marca); dto.setPreco(new BigDecimal("10.00")); dto.setEstoque(5);
        CategoriaRequestDTO cat = new CategoriaRequestDTO(); cat.setNome(categoria); dto.setCategoria(cat);
        return dto;
    }

    @ParameterizedTest @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void rejeitaQuantidadeInvalidaSemAlterarCarrinho(int quantidade) {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        assertThrows(DadosInvalidosException.class, () -> itemService.salvarItem(carrinho.getId(), produto.getId(), quantidade));
        assertTrue(carrinho.getCarrinhoItems().isEmpty());
    }

    @Test void rejeitaSomaDeQuantidadesAcimaDoEstoque() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 3);
        assertThrows(ConflitoException.class, () -> itemService.salvarItem(carrinho.getId(), produto.getId(), 3));
        assertEquals(3, carrinho.getCarrinhoItems().iterator().next().getQuantidade());
    }

    @Test void rejeitaAtualizacaoInvalidaEItemInexistente() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 1);
        assertThrows(DadosInvalidosException.class, () -> itemService.atualizarQuantidadeItem(carrinho.getId(), produto.getId(), 0));
        assertThrows(ConflitoException.class, () -> itemService.atualizarQuantidadeItem(carrinho.getId(), produto.getId(), 6));
        assertThrows(ProdutoNaoEncontrado.class, () -> itemService.atualizarQuantidadeItem(carrinho.getId(), Long.MAX_VALUE, 1));
    }

    @Test void somaDeQuantidadesNaoEstouraInteiro() {
        Produto produto = produto(Integer.MAX_VALUE); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), Integer.MAX_VALUE);
        assertThrows(ConflitoException.class, () -> itemService.salvarItem(carrinho.getId(), produto.getId(), 1));
        assertEquals(Integer.MAX_VALUE, carrinho.getCarrinhoItems().iterator().next().getQuantidade());
    }

    @Test void rejeitaPedidoVazio() {
        Carrinho carrinho = carrinho();
        assertThrows(DadosInvalidosException.class, () -> pedidoService.fazerPedido(carrinho.getUsuario().getId()));
    }

    @Test void revalidaEstoqueNoFechamento() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 4);
        produto.setEstoque(2); em.flush();
        assertThrows(ConflitoException.class, () -> pedidoService.fazerPedido(carrinho.getUsuario().getId()));
        assertEquals(2, produto.getEstoque());
        assertFalse(carrinho.getCarrinhoItems().isEmpty());
    }

    @Test void fechaPedidoELimpaItensETotal() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 2); em.flush();
        Long id = carrinho.getId();
        Pedido pedido = pedidoService.fazerPedido(carrinho.getUsuario().getId());
        em.flush(); em.clear();
        assertEquals(3, produtos.findById(produto.getId()).orElseThrow().getEstoque());
        assertEquals(0, new BigDecimal("20.00").compareTo(pedido.getTotalPedido()));
        Carrinho vazio = carrinhos.findById(id).orElseThrow();
        assertTrue(vazio.getCarrinhoItems().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(vazio.getQuantidadeTotal()));
    }

    @Test void limpaCarrinhoPersistidoSemExcluirPai() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 2); em.flush();
        Long id = carrinho.getId();
        carrinhoService.limparCarrinho(id); em.flush(); em.clear();
        assertTrue(carrinhos.findById(id).orElseThrow().getCarrinhoItems().isEmpty());
        assertEquals(0, carrinhos.findById(id).orElseThrow().getQuantidadeTotal().signum());
        assertTrue(produtos.existsById(produto.getId()));
    }

    @Test void alteraQuantidadeEIdSemCorromperHashSet() {
        Produto produto = produto(10); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 2); em.flush();
        itemService.atualizarQuantidadeItem(carrinho.getId(), produto.getId(), 3);
        itemService.removerItemDoCarrinho(carrinho.getId(), produto.getId()); em.flush();
        assertTrue(carrinho.getCarrinhoItems().isEmpty());
        assertTrue(carrinhos.existsById(carrinho.getId()));
    }

    @Test void excluirProdutoPreservaCategoriaCompartilhada() {
        Produto primeiro = produto(5);
        Produto segundo = produtos.saveAndFlush(new Produto("Outro", "Marca", BigDecimal.TEN, 5, "Descrição", primeiro.getCategoria()));
        Long categoriaId = primeiro.getCategoria().getId();
        produtoService.deletarProduto(primeiro.getId()); em.flush(); em.clear();
        assertTrue(categorias.existsById(categoriaId));
        assertTrue(produtos.existsById(segundo.getId()));
    }

    @Test void naoPermiteRenomearCategoriaParaNomeExistente() {
        Categoria primeira = categorias.saveAndFlush(new Categoria("Primeira-" + UUID.randomUUID()));
        Categoria segunda = categorias.saveAndFlush(new Categoria("Segunda-" + UUID.randomUUID()));
        CategoriaRequestDTO dto = new CategoriaRequestDTO(); dto.setNome(primeira.getNome());
        assertThrows(CategoriaJaCriada.class, () -> categoriaService.atualizarCategoria(dto, segunda.getId()));
    }

    @Test void naoPermiteDuplicarProdutoNaAtualizacao() {
        Produto primeiro = produto(5); Produto segundo = produto(5);
        assertThrows(ProdutoJaCadastradoException.class, () -> produtoService.atualizarProduto(dto(primeiro.getNome(), primeiro.getMarca(), primeiro.getCategoria().getNome()), segundo.getId()));
    }

    @Test void permiteAtualizacaoSemMudarChaveDoProduto() {
        Produto produto = produto(5);
        produtoService.atualizarProduto(dto(produto.getNome(), produto.getMarca(), produto.getCategoria().getNome()), produto.getId());
        em.flush();
    }

    @Test void validaProdutoNoServiceInclusiveCategoriaAninhada() {
        ProdutoRequestDTO invalido = dto("Nome", "Marca", " "); invalido.setPreco(BigDecimal.ZERO); invalido.setEstoque(-1);
        assertThrows(ConstraintViolationException.class, () -> produtoService.salvarProduto(invalido));
    }

    @Test void rejeitaEmailDuplicadoNoService() {
        Carrinho carrinho = carrinho();
        UsuarioCriarDTO dto = new UsuarioCriarDTO();
        dto.setPrimeiroNome("Nome"); dto.setSobrenome("Sobrenome"); dto.setEmail(carrinho.getUsuario().getEmail()); dto.setSenha("senha-teste");
        assertThrows(ConflitoException.class, () -> usuarioService.criarUsuario(dto));
    }

    @Test void getUserByIdRetornaUsuarioEncontrado() {
        Usuario usuario = carrinho().getUsuario();
        assertEquals(usuario.getId(), usuarioService.getUserById(usuario.getId()).getId());
    }

    @Test void getUserByIdRejeitaUsuarioInexistente() {
        assertThrows(io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado.class,
                () -> usuarioService.getUserById(Long.MAX_VALUE));
    }

    @Test void bancoImpedeEmailDuplicadoMesmoSemService() {
        Carrinho carrinho = carrinho();
        assertThrows(DataIntegrityViolationException.class, () -> usuarios.saveAndFlush(new Usuario("Outro", "Nome", carrinho.getUsuario().getEmail(), "senha-teste")));
    }

    @Test void bancoImpedeCategoriaDuplicadaMesmoSemService() {
        Categoria categoria = categorias.saveAndFlush(new Categoria("Unica-" + UUID.randomUUID()));
        assertThrows(DataIntegrityViolationException.class, () -> categorias.saveAndFlush(new Categoria(categoria.getNome())));
    }

    @Test void bancoImpedeProdutoDuplicadoMesmoSemService() {
        Produto produto = produto(5);
        assertThrows(DataIntegrityViolationException.class, () -> produtos.saveAndFlush(new Produto(produto.getNome(), produto.getMarca(), BigDecimal.TEN, 1, "Outro", produto.getCategoria())));
    }

    @Test void bancoImpedeMesmoProdutoEmDuasLinhasDoCarrinho() {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 1); em.flush();
        CarrinhoItem repetido = new CarrinhoItem();
        repetido.setProduto(produto); repetido.setCarrinho(carrinho);
        repetido.setQuantidade(1); repetido.setPrecoUnico(BigDecimal.TEN); repetido.setTotalPrice();
        assertThrows(DataIntegrityViolationException.class, () -> itens.saveAndFlush(repetido));
    }

    @Test void respostaProdutoComImagemNaoExpoeBlobNemRelacionamentos() throws Exception {
        Produto produto = produto(5);
        Imagem imagem = new Imagem(); imagem.setProduto(produto); imagem.setNomeArquivo("foto.png");
        imagem.setTipoArquivo("image/png"); imagem.setDownloadUrl("/foto");
        imagem.setImagem(new javax.sql.rowset.serial.SerialBlob(new byte[]{1, 2, 3}));
        em.persist(imagem); em.flush(); em.clear();
        mvc.perform(get("/api/v1/produtos/{id}/listarPorId", produto.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.preco").value(10.0))
                .andExpect(jsonPath("$.data.imagensDto[0].arquivoNome").value("foto.png"))
                .andExpect(jsonPath("$.data.imagensDto[0].produto").doesNotExist())
                .andExpect(jsonPath("$.data.imagensDto[0].imagem").doesNotExist())
                .andExpect(jsonPath("$.data.categoria.produtos").doesNotExist());
    }

    @Test void endpointsRetornamDtosSemCiclos() throws Exception {
        Produto produto = produto(5); Carrinho carrinho = carrinho();
        itemService.salvarItem(carrinho.getId(), produto.getId(), 2); em.flush();
        mvc.perform(get("/api/v1/carrinhos/{id}/listarCarrinho", carrinho.getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.carrinhoId").value(carrinho.getId()))
                .andExpect(jsonPath("$.data.itens[0].produto.preco").value(10.0))
                .andExpect(jsonPath("$.data.itens[0].carrinho").doesNotExist());
        mvc.perform(get("/api/v1/usuarios/{id}/listarUsuario", carrinho.getUsuario().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.senha").doesNotExist());
        mvc.perform(post("/api/v1/pedidos/{id}", carrinho.getUsuario().getId()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.status").value("PENDENTE"))
                .andExpect(jsonPath("$.data.item[0].quantidade").value(2))
                .andExpect(jsonPath("$.data.usuario").doesNotExist());
    }

    @Test void httpRejeitaProdutoInvalido() throws Exception {
        mvc.perform(post("/api/v1/produtos/salvar").contentType("application/json")
                .content("{\"nome\":\"Produto\",\"marca\":\"Marca\",\"preco\":-1,\"estoque\":-1}"))
                .andExpect(status().isBadRequest());
    }

    @Test void httpRejeitaNomeVazioNaAtualizacaoCategoria() throws Exception {
        Categoria categoria = categorias.saveAndFlush(new Categoria("Valida-" + UUID.randomUUID()));
        mvc.perform(put("/api/v1/categorias/categoria/{id}/atualizar", categoria.getId()).contentType("application/json")
                .content("{\"nome\":\" \"}"))
                .andExpect(status().isBadRequest());
    }
}
