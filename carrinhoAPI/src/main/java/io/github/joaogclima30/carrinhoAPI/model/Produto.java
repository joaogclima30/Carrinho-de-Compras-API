package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_produto_nome_marca", columnNames = {"nome", "marca"}))
@Getter
@Setter
@NoArgsConstructor
public class Produto {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    @org.hibernate.annotations.ColumnDefault("0")
    private long versao;

    @Column(name = "nome", nullable = false)
    private String nome;

    @Column(name = "marca", nullable = false)
    private String marca;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "estoque", nullable = false)
    private int estoque;

    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Imagem> imagens;

    public Produto(String nome, String marca, BigDecimal preco, int estoque, String descricao, Categoria categoria) {
        this.nome = nome;
        this.marca = marca;
        this.price = preco;
        this.estoque = estoque;
        this.descricao = descricao;
        this.categoria = categoria;
    }
}
