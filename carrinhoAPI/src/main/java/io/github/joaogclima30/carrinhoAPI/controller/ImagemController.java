package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Imagem.ImagemService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/imagens")
public class ImagemController {
    private final ImagemService service;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> salvarImagem(@RequestParam List<MultipartFile> files, @RequestParam @Positive Long produtoId) {
        return ResponseEntity.ok(new ApiResponse("Imagens salvas", service.salvarImagem(files, produtoId)));
    }

    @GetMapping({"/imagem/download/{imagemId}", "/imagem/dowload/{imagemId}"})
    public ResponseEntity<Resource> baixarImagem(@PathVariable @Positive Long imagemId) {
        var download = service.buscarImagemParaDownload(imagemId);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(download.tipoArquivo()))
                .contentLength(download.bytes().length)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(download.nomeArquivo(), StandardCharsets.UTF_8).build().toString())
                .body(new ByteArrayResource(download.bytes()));
    }

    @PutMapping(value = "/imagem/{imagemId}/atualizar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> atualizarImagem(@PathVariable @Positive Long imagemId, @RequestParam MultipartFile file) {
        service.atualizarImagem(file, imagemId);
        return ResponseEntity.ok(new ApiResponse("Imagem atualizada", null));
    }

    @DeleteMapping("/imagem/{imagemId}/deletar")
    public ResponseEntity<ApiResponse> deletarImagem(@PathVariable @Positive Long imagemId) {
        service.deletarImagemPorId(imagemId);
        return ResponseEntity.ok(new ApiResponse("Imagem excluída", null));
    }
}
