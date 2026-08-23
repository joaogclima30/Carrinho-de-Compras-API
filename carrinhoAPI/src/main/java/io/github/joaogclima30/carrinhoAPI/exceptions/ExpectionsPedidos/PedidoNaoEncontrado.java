package io.github.joaogclima30.carrinhoAPI.exceptions.ExpectionsPedidos;

public class PedidoNaoEncontrado extends RuntimeException {
    public PedidoNaoEncontrado(String message) {
        super(message);
    }
}
