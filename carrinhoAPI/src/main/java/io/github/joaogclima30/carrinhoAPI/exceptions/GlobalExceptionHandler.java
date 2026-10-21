package io.github.joaogclima30.carrinhoAPI.exceptions;

import io.github.joaogclima30.carrinhoAPI.dto.ErroResponse.ErroResponse;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCategoria.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsUsuario.UsuarioNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsPedidos.PedidoNaoEncontrado;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler({CarrinhoNaoEncontrado.class, CategoriaNaoEncontrada.class, ImagemNaoExiste.class,
            ProdutoNaoEncontrado.class, UsuarioNaoEncontrado.class, PedidoNaoEncontrado.class})
    public ResponseEntity<ErroResponse> naoEncontrado(RuntimeException e) { return erro(404, e.getMessage()); }

    @ExceptionHandler({ConflitoException.class, CategoriaJaCriada.class, ProdutoJaCadastradoException.class})
    public ResponseEntity<ErroResponse> conflito(RuntimeException e) { return erro(409, e.getMessage()); }

    @ExceptionHandler(DadosInvalidosException.class)
    public ResponseEntity<ErroResponse> dadosInvalidos(DadosInvalidosException e) { return erro(400, e.getMessage()); }

    @ExceptionHandler(ArquivoMuitoGrandeException.class)
    public ResponseEntity<ErroResponse> arquivoGrande(ArquivoMuitoGrandeException e) { return erro(413, e.getMessage()); }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResponse> validacao(ConstraintViolationException e) {
        String mensagem = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage()).sorted()
                .collect(java.util.stream.Collectors.joining("; "));
        return erro(400, mensagem);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException e) {
        log.warn("Operação rejeitada por restrição de integridade");
        return erro(409, "Operação viola uma restrição de unicidade ou vínculo entre registros");
    }

    @ExceptionHandler({ConcurrencyFailureException.class, jakarta.persistence.OptimisticLockException.class,
            jakarta.persistence.PessimisticLockException.class, jakarta.persistence.LockTimeoutException.class})
    public ResponseEntity<ErroResponse> concorrencia(RuntimeException e) {
        return erro(409, "Conflito com outra operação. Atualize os dados e tente novamente");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErroResponse> multipart(MultipartException e) { return erro(400, "Envio multipart inválido"); }

    @ExceptionHandler(ErroSalvarImagem.class)
    public ResponseEntity<ErroResponse> imagem(ErroSalvarImagem e) {
        log.error("Falha de armazenamento ou leitura de imagem", e);
        return erro(500, "Não foi possível processar a imagem");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String mensagem = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage()).sorted()
                .collect(java.util.stream.Collectors.joining("; "));
        return new ResponseEntity<>(new ErroResponse(400, mensagem), headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception e, Object body, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String mensagem = switch (status.value()) {
            case 400 -> "Requisição inválida: verifique o JSON, os parâmetros e os arquivos enviados";
            case 404 -> "Recurso não encontrado";
            case 405 -> "Método HTTP não permitido para esta rota";
            case 413 -> "Envio excede o tamanho máximo permitido";
            case 415 -> "Tipo de conteúdo não suportado por esta rota";
            default -> status.is5xxServerError() ? "Erro interno ao processar a requisição" : "Não foi possível atender à requisição";
        };
        if (status.is5xxServerError()) log.error("Falha HTTP interna", e);
        return new ResponseEntity<>(new ErroResponse(status.value(), mensagem), headers, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception e) {
        log.error("Erro inesperado ao processar requisição", e);
        return erro(500, "Erro interno ao processar a requisição");
    }

    private ResponseEntity<ErroResponse> erro(int status, String mensagem) {
        return ResponseEntity.status(status).body(new ErroResponse(status, mensagem));
    }
}
