package io.github.joaogclima30.carrinhoAPI.controller;

import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import io.github.joaogclima30.carrinhoAPI.model.Imagem;
import io.github.joaogclima30.carrinhoAPI.response.ApiResponse;
import io.github.joaogclima30.carrinhoAPI.service.Imagem.ImagemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/imagens")
public class ImagemController {

    private final ImagemService imagemService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse> salvarImagem(@Valid @RequestParam List<MultipartFile> files, @RequestParam Long produtoId){
        List<ImagemDTO> imagemDtos = imagemService.salvarImagem(files,produtoId);
        return ResponseEntity.ok(new ApiResponse("Salvo com Sucesso!", imagemDtos));
    }

    @GetMapping("/imagem/dowload/{imagemId}")
    public ResponseEntity<Resource> abaixarImagem(@PathVariable Long imagemId){
        Imagem imagem = imagemService.buscarImagem(imagemId);
        ByteArrayResource resource = new ByteArrayResource(imagem.getImagem());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(imagem.getTipoArquivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imagem.getNomeArquivo() + "\"")
                .body(resource);
    }

    @PutMapping("/imagem/{imagemId}/atualizar")
    public ResponseEntity<ApiResponse> atualizarImagem(@Valid @PathVariable Long imagemId, @RequestParam MultipartFile file){
        Imagem imagem = imagemService.buscarImagem(imagemId);

        if(imagem != null) {
            imagemService.atualizarImagem(file, imagemId);
            return ResponseEntity.ok(new ApiResponse("Atualizado com sucesso!", null));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Não é possivel Atualizar", HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @DeleteMapping("/imagem/{imagemId}/deletar")
    public ResponseEntity<ApiResponse> deletarImagem(@Valid @PathVariable Long imagemId){
        Imagem imagem = imagemService.buscarImagem(imagemId);
        if(imagem != null) {
            imagemService.deletarImagemPorId(imagemId);
            return ResponseEntity.ok(new ApiResponse("Deletado com sucesso!", null));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse("Não é possivel Delw", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}
