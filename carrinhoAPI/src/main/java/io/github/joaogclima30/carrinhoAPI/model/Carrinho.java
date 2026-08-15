package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Carrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private BigDecimal quantidadeTotal = BigDecimal.ZERO;

    @OneToMany(mappedBy = "carrinho", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CarrinhoItem> carrinhoItems;

    public void adicionarItem(CarrinhoItem item){
        this.carrinhoItems.add(item);
        item.setCarrinho(this);
        atualizarQuantidadeTotal();
    }

    public void removerItem(CarrinhoItem item){
        this.carrinhoItems.remove(item);
        item.setCarrinho(null);
        atualizarQuantidadeTotal();
    }

    public void atualizarQuantidadeTotal(){
        this.quantidadeTotal = carrinhoItems.stream().map(item -> {
            BigDecimal precoUnico = item.getPrecoUnico();
            if(precoUnico == null){
                return BigDecimal.ZERO;
            }
            return precoUnico.multiply(BigDecimal.valueOf(item.getQuantidade()));
        }).reduce(BigDecimal.ZERO,BigDecimal::add);
    }

}
