package io.github.joaogclima30.carrinhoAPI.exceptions;

import io.github.joaogclima30.carrinhoAPI.dto.ErroResponse.ErroResponse;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoiraJaCriada;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.CategoriaNaoEncontrada;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ErroSalvarImagem;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ImagemNãoExiste;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.produtoJaCadastrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExpectionsPedidos.PedidoNaoEncontrado;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleRestricoes(jakarta.validation.ConstraintViolationException e) {
        String mensagem = e.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
                .sorted().collect(java.util.stream.Collectors.joining("; "));
        return new ErroResponse(400, mensagem);
    }

    @ExceptionHandler(DadosInvalidosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErroResponse handleDadosInvalidos(DadosInvalidosException e) {
        return new ErroResponse(400, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleConflito(ConflitoException e) {
        return new ErroResponse(409, e.getMessage());
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleIntegridade(org.springframework.dao.DataIntegrityViolationException e) {
        return new ErroResponse(409, "Operação viola uma restrição de unicidade ou vínculo entre registros");
    }

    @ExceptionHandler(org.springframework.dao.ConcurrencyFailureException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleConcorrencia(org.springframework.dao.ConcurrencyFailureException e) {
        return new ErroResponse(409, "O registro foi alterado por outra operação. Tente novamente");
    }

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

    @ExceptionHandler(PedidoNaoEncontrado.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handlePedidoNaoEncontrado(PedidoNaoEncontrado e){
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

    @ExceptionHandler(CarrinhoNaoEncontrado.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleCarrinhoNaoEncontrado(CarrinhoNaoEncontrado e){
        return new ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(CategoiraJaCriada.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErroResponse handleCategoriaJaCriada(CategoiraJaCriada e){
        return new ErroResponse(HttpStatus.CONFLICT.value(), e.getMessage());
    }

    @ExceptionHandler(UsuarioNaoEncontrado.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErroResponse handleUsuarioNaoEncontrado(UsuarioNaoEncontrado e){
        return new ErroResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
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
