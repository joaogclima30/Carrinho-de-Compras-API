package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.exceptions.ConflitoException;
import io.github.joaogclima30.carrinhoAPI.exceptions.DadosInvalidosException;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.github.joaogclima30.carrinhoAPI.service.Pedido.PedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ConcorrenciaPedidoIntegrationTest {
    @Autowired ProdutoRepository produtos;
    @Autowired CategoriaRepository categorias;
    @Autowired UsuarioRepository usuarios;
    @Autowired CarrinhoRepository carrinhos;
    @Autowired PedidoRepository pedidos;
    @Autowired CarrinhoItemService itemService;
    @Autowired PedidoService pedidoService;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    private Long criarCarrinho(Produto produto) {
        Usuario usuario = usuarios.save(new Usuario("Nome", "Sobrenome", UUID.randomUUID() + "@example.com", "senha-teste"));
        Carrinho carrinho = new Carrinho(); carrinho.setUsuario(usuario); carrinho = carrinhos.save(carrinho);
        itemService.salvarItem(carrinho.getId(), produto.getId(), 1);
        return usuario.getId();
    }

    @Test void comprasSimultaneasNaoVendemMesmaUnidade() throws Exception {
        verificarConcorrencia(false, 1);
    }

    @Test void comprasSimultaneasComSaldoSuficienteFinalizam() throws Exception {
        verificarConcorrencia(false, 2);
    }

    @Test void mesmoCarrinhoNaoGeraDoisPedidos() throws Exception {
        verificarConcorrencia(true, 1);
    }

    private void verificarConcorrencia(boolean mesmoCarrinho, int estoque) throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        long[] ids = tx.execute(status -> {
            Categoria categoria = categorias.save(new Categoria("Categoria-" + UUID.randomUUID()));
            Produto produto = produtos.save(new Produto("Produto-" + UUID.randomUUID(), "Marca", BigDecimal.TEN, estoque, "Descrição", categoria));
            Long primeiroUsuario = criarCarrinho(produto);
            Long segundoUsuario = mesmoCarrinho ? primeiroUsuario : criarCarrinho(produto);
            return new long[]{produto.getId(), primeiroUsuario, segundoUsuario};
        });
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<Boolean> primeiro = executor.submit(() -> finalizar(ids[1], inicio));
            Future<Boolean> segundo = executor.submit(() -> finalizar(ids[2], inicio));
            inicio.countDown();
            int sucessos = (primeiro.get(20, TimeUnit.SECONDS) ? 1 : 0) + (segundo.get(20, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(mesmoCarrinho ? 1 : Math.min(estoque, 2), sucessos);
        }
        int pedidosEsperados = mesmoCarrinho ? 1 : Math.min(estoque, 2);
        assertEquals(estoque - pedidosEsperados, produtos.findById(ids[0]).orElseThrow().getEstoque());
        assertEquals(pedidosEsperados, pedidos.findByUsuarioId(ids[1]).size() + (mesmoCarrinho ? 0 : pedidos.findByUsuarioId(ids[2]).size()));
    }

    private boolean finalizar(Long usuarioId, CountDownLatch inicio) throws InterruptedException {
        inicio.await();
        try {
            pedidoService.fazerPedido(usuarioId);
            return true;
        } catch (ConflitoException | DadosInvalidosException e) {
            return false;
        }
    }
}
