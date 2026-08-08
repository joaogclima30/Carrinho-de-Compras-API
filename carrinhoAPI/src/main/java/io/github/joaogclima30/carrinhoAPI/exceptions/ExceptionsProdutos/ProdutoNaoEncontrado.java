package io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos;

public class ProdutoNaoEncontrado extends RuntimeException {
    public ProdutoNaoEncontrado(String message) {
        super(message);
    }
}
