package io.github.joaogclima30.carrinhoAPI.exceptions;

import io.github.joaogclima30.carrinhoAPI.dto.ErroResponse.ErroResponse;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoiraJaCriada;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoriaNaoEncontrada;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ErroSalvarImagem;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ImagemNãoExiste;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.produtoJaCadastrado;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErroSalvarImagem.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse handleErroSalvarImagem(ErroSalvarImagem e){
        return new ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
    }

    @ExceptionHandler(ImagemNãoExiste.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleImagemNaoEncontrada(ImagemNãoExiste e){
        return new ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(ProdutoNaoEncontrado.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleProdutoNaoEncontrado(ProdutoNaoEncontrado e){
        return new ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(produtoJaCadastrado.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleProdutoJaCadastrado(produtoJaCadastrado e){
        return new ErroResponse(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    @ExceptionHandler(CategoriaNaoEncontrada.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleCategoriaNaoEncontrada(CategoriaNaoEncontrada e){
        return new ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(CategoiraJaCriada.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleCategoriaJaCriada(CategoiraJaCriada e){
        return new ErroResponse(HttpStatus.CONFLICT.value(), e.getMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleValidacao(MethodArgumentNotValidException e){
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Dados inválidos");
        return new ErroResponse(HttpStatus.BAD_REQUEST.value(), mensagem);
    }
}

