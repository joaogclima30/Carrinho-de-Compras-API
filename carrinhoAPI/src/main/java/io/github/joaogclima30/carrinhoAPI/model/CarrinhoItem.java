package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_carrinho_produto", columnNames = {"cart_id", "produto_id"}))
public class CarrinhoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantidade;
    private BigDecimal precoUnico;
    private BigDecimal precoTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    private Carrinho carrinho;

    public void setTotalPrice(){
        this.precoTotal = this.precoUnico.multiply(new BigDecimal(quantidade));
    }
}
