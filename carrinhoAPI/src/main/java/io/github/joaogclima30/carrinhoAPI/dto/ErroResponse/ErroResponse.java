package io.github.joaogclima30.carrinhoAPI.dto.ErroResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponse;

public record ErroResponse(int status, String mensagem) {

    public static ErroResponse erroSalvarImagem(String mensagem){
        return new ErroResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), mensagem );
    }
}
