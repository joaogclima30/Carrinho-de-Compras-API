package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.exceptions.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.*;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditoriaCarrinhoUnitTest {
    CarrinhoRepository repository;
    ProdutoService produtos;
    CarrinhoItemService service;
    Carrinho carrinho;
    Produto produto;

    @BeforeEach void preparar() {
        repository = mock(CarrinhoRepository.class);
        produtos = mock(ProdutoService.class);
        service = new CarrinhoItemService(repository, produtos, mock(CarrinhoService.class),
                new DtoMapper("/api/v1"), mock(EntityManager.class));
        carrinho = new Carrinho(); carrinho.setId(1);
        produto = new Produto("Produto", "Marca", new BigDecimal("10.35"), 10, "Descrição", new Categoria("Categoria"));
        produto.setId(2L);
        when(repository.buscarParaAtualizar(1L)).thenReturn(Optional.of(carrinho));
        when(produtos.obterPorId(2L)).thenReturn(produto);
    }

    @ParameterizedTest @ValueSource(ints = {0, -1, Integer.MIN_VALUE})
    void quantidadeInvalidaNaoAlteraEstado(int quantidade) {
        assertThrows(DadosInvalidosException.class, () -> service.salvarItem(1L, 2L, quantidade));
        assertTrue(carrinho.getCarrinhoItems().isEmpty());
        verifyNoInteractions(repository, produtos);
    }

    @Test void mesmaMercadoriaSomaQuantidadeSemDuplicarLinha() {
        service.salvarItem(1L, 2L, 2); service.salvarItem(1L, 2L, 3);
        assertEquals(1, carrinho.getCarrinhoItems().size());
        assertEquals(5, carrinho.getCarrinhoItems().iterator().next().getQuantidade());
        assertEquals(0, new BigDecimal("51.75").compareTo(carrinho.getQuantidadeTotal()));
    }

    @Test void removeUltimoItemZeraTotalSemExcluirProduto() {
        service.salvarItem(1L, 2L, 2); service.removerItemDoCarrinho(1L, 2L);
        assertTrue(carrinho.getCarrinhoItems().isEmpty());
        assertEquals(0, carrinho.getQuantidadeTotal().signum());
        verify(produtos, never()).deletarProduto(anyLong());
    }

    @Test void atualizacaoRecalculaSubtotalETotal() {
        service.salvarItem(1L, 2L, 2); service.atualizarQuantidadeItem(1L, 2L, 3);
        assertEquals(0, new BigDecimal("31.05").compareTo(carrinho.getQuantidadeTotal()));
        assertEquals(0, new BigDecimal("31.05").compareTo(carrinho.getCarrinhoItems().iterator().next().getPrecoTotal()));
    }

    @Test void estoqueInsuficientePreservaQuantidadeAnterior() {
        service.salvarItem(1L, 2L, 2);
        assertThrows(ConflitoException.class, () -> service.salvarItem(1L, 2L, 9));
        assertEquals(2, carrinho.getCarrinhoItems().iterator().next().getQuantidade());
    }

    @Test void carrinhoInexistenteRetornaExcecaoDeDominio() {
        assertThrows(CarrinhoNaoEncontrado.class, () -> service.salvarItem(99L, 2L, 1));
        verifyNoInteractions(produtos);
    }

    @Test void documentaPoliticaAtualAdicionarPreservaPrecoMasAtualizarQuantidadeReprecifica() {
        service.salvarItem(1L, 2L, 1);
        produto.setPrice(new BigDecimal("20.00"));
        service.salvarItem(1L, 2L, 1);
        assertEquals(0, new BigDecimal("20.70").compareTo(carrinho.getQuantidadeTotal()));
        service.atualizarQuantidadeItem(1L, 2L, 2);
        assertEquals(0, new BigDecimal("40.00").compareTo(carrinho.getQuantidadeTotal()));
    }
}
