package io.github.joaogclima30.carrinhoAPI;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.*;
import io.github.joaogclima30.carrinhoAPI.service.Imagem.ImagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("test")
class AuditoriaBlobPostgresTest {
    @Autowired DataSource source;
    @Autowired JdbcTemplate jdbc;
    @Autowired CategoriaRepository categorias;
    @Autowired ProdutoRepository produtos;
    @Autowired ImagemRepository imagens;
    @Autowired ImagemService service;
    @Autowired PlatformTransactionManager manager;

    @Test void excluirImagemDeveLiberarLargeObjectPostgres() throws Exception {
        try (var connection = source.getConnection()) {
            assumeTrue(connection.getMetaData().getDatabaseProductName().equals("PostgreSQL"),
                    "Large Objects OID são específicos do PostgreSQL; H2 não reproduz esse armazenamento");
        }
        long antes = jdbc.queryForObject("select count(*) from pg_largeobject_metadata", Long.class);
        long produtoId = new TransactionTemplate(manager).execute(status -> {
            Categoria categoria = categorias.save(new Categoria("Blob-" + UUID.randomUUID()));
            return produtos.saveAndFlush(new Produto("Blob-" + UUID.randomUUID(), "Marca", BigDecimal.TEN, 1, "Desc", categoria)).getId();
        });
        var output = new java.io.ByteArrayOutputStream();
        javax.imageio.ImageIO.write(new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB), "png", output);
        long imagemId = service.salvarImagem(List.of(new MockMultipartFile("files", "teste.png", "image/png", output.toByteArray())), produtoId).get(0).getId();
        long aposUpload = jdbc.queryForObject("select count(*) from pg_largeobject_metadata", Long.class);
        assertTrue(aposUpload > antes, "Upload deveria ter armazenado conteúdo");
        service.deletarImagemPorId(imagemId);
        assertFalse(imagens.existsById(imagemId));
        long aposExcluir = jdbc.queryForObject("select count(*) from pg_largeobject_metadata", Long.class);
        System.out.println("AUDIT_BLOB antes=" + antes + " aposUpload=" + aposUpload + " aposExcluir=" + aposExcluir);
        assertEquals(antes, aposExcluir,
                "A linha foi excluída, mas o Large Object ficou órfão no PostgreSQL");
    }
}
