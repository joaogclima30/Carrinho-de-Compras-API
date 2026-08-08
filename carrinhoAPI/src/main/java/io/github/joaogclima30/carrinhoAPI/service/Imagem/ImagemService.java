package io.github.joaogclima30.carrinhoAPI.service.Imagem;

import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.ImagemNãoExiste;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Imagem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.ImagemRepository;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImagemService {

    private final ImagemRepository imagemRepository;
    private final ProdutoService produtoService;

    public List<ImagemDTO> salvarImagem(List<MultipartFile> files, Long produtoId){
        Produto produto = produtoService.obterPorId(produtoId)
                .orElseThrow(() -> new ProdutoNaoEncontrado("Produto não existe"));

        List<ImagemDTO> imagemSalvaDto = new ArrayList<>();
        for (MultipartFile file : files){
            try {
                Imagem imagem = new Imagem();
                imagem.setNomeArquivo(file.getOriginalFilename());
                imagem.setTipoArquivo(file.getContentType());
                imagem.setImagem(new SerialBlob(file.getBytes()));
                imagem.setProduto(produto);

                String buildDownloadUrl = "/api/v1/imagens/imagem/dowload";
                String downloadUrl = buildDownloadUrl + imagem.getId();
                imagem.setDownloadUrl(downloadUrl);
                Imagem imagemSalva =imagemRepository.save(imagem);

                imagemSalva.setDownloadUrl(buildDownloadUrl + imagemSalva.getId());
                imagemRepository.save(imagemSalva);

                ImagemDTO imagemDTO = new ImagemDTO();
                imagemDTO.setImagemId(imagemSalva.getId());
                imagemDTO.setImagemName(imagemSalva.getNomeArquivo());
                imagemDTO.setDownloadUrl(imagemSalva.getDownloadUrl());
                imagemSalvaDto.add(imagemDTO);
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return imagemSalvaDto;
    }

    public void atualizarImagem(MultipartFile file, Long imagemId){
        Imagem imagem = buscarImagem(imagemId);
        try {
            imagem.setTipoArquivo(file.getOriginalFilename());
            imagem.setNomeArquivo(file.getOriginalFilename());
            imagem.setImagem(new SerialBlob(file.getBytes()));
            imagemRepository.save(imagem);
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public Imagem buscarImagem(Long id){
        return imagemRepository.findById(id).orElseThrow(() -> new ImagemNãoExiste("Não há imagem encontrada com id: " + id));
    }

//    public void deletarImagemPorId(Long id){
//        var imagemEncontrada = imagemRepository.findById(id);
//        if (imagemEncontrada == null){
//            throw new ImagemNãoExiste("Não existe essa imagem");
//        }
//        imagemRepository.delete(imagemEncontrada);
//    }

    public void deletarImagemPorId(Long id){
        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ImagemNãoExiste("Não existe essa imagem"));
        imagemRepository.delete(imagem);
    }
}
