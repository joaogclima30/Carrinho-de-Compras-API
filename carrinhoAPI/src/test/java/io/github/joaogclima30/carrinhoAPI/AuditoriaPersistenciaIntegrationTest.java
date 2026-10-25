package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoItemService;
import io.github.joaogclima30.carrinhoAPI.service.Pedido.PedidoService;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import jakarta.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuditoriaPersistenciaIntegrationTest {
    @Autowired RoleRepository roles;
    @Autowired UsuarioRepository usuarios;
    @Autowired CategoriaRepository categorias;
    @Autowired ProdutoRepository produtos;
    @Autowired CarrinhoItemService itens;
    @Autowired PedidoService pedidos;
    @Autowired ProdutoService catalogo;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory factory;

    @Test void nomeDeRoleDeveSerUnicoNoBanco() {
        String nome = "ROLE_AUDIT_" + UUID.randomUUID();
        roles.saveAndFlush(new Role(nome));
        assertThrows(DataIntegrityViolationException.class, () -> roles.saveAndFlush(new Role(nome)),
                "Banco permite roles duplicadas, mas findByNome espera no máximo uma");
    }

    @Test void pedidoPreservaPrecoMesmoAposAlteracaoDoProduto() {
        Categoria categoria = categorias.save(new Categoria("Historico-" + UUID.randomUUID()));
        Produto produto = produtos.saveAndFlush(new Produto("Historico-" + UUID.randomUUID(), "Marca", new BigDecimal("10.25"), 10, "Desc", categoria));
        Usuario usuario = usuarios.saveAndFlush(new Usuario("Nome", "Teste", UUID.randomUUID() + "@example.com", "fixture-nao-usada-para-login"));
        itens.adicionarItem(usuario.getId(), null, produto.getId(), 2);
        var pedido = pedidos.fazerPedidoDto(usuario.getId());
        produto.setPrice(new BigDecimal("99.00")); em.flush(); em.clear();
        var salvo = pedidos.listarPedido(pedido.getPedidoId());
        assertEquals(0, new BigDecimal("20.50").compareTo(salvo.getTotalPedido()));
        assertEquals(0, new BigDecimal("10.25").compareTo(salvo.getItem().get(0).getPrice()));
    }

    @Test void medeConsultasDoCatalogoSemOpenInView() {
        for (int i = 0; i < 4; i++) {
            Categoria categoria = categorias.save(new Categoria("SQL-" + UUID.randomUUID()));
            produtos.save(new Produto("SQL-" + UUID.randomUUID(), "Marca", BigDecimal.TEN, 1, "Desc", categoria));
        }
        em.flush(); em.clear();
        var stats = factory.unwrap(org.hibernate.SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true); stats.clear();
        try {
            var resultado = catalogo.listarTodosDto();
            System.out.println("AUDIT_CATALOGO produtos=" + resultado.size() + " consultas=" + stats.getPrepareStatementCount());
            assertTrue(resultado.size() >= 4);
        } finally { stats.setStatisticsEnabled(false); }
    }
}
