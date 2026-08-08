package io.github.joaogclima30.carrinhoAPI.exceptions;

import io.github.joaogclima30.carrinhoAPI.dto.ErroResponse.ErroResponse;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ErroSalvarImagem;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ImagemNãoExiste;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ErroSalvarImagem.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErroResponse handleErroSalvarImagem(ErroSalvarImagem e){
        return new
                ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                e.getMessage());
    }

    @ExceptionHandler(ImagemNãoExiste.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleImagemNaoEncontrada(ImagemNãoExiste e){
        return new
                ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }
}
