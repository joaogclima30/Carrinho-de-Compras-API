package io.github.joaogclima30.carrinhoAPI.exceptions;

public class ProdutoNaoEncontrado extends RuntimeException {
    public ProdutoNaoEncontrado(String message) {
        super(message);
    }
}
