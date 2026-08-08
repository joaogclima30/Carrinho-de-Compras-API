package io.github.joaogclima30.carrinhoAPI.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
//Classe usada para dar retorno ao front end
public class ApiResponse {
    private String mensagem;
    private Object data;

}
