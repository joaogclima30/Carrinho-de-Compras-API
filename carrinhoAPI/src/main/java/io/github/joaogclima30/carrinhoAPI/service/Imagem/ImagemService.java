package io.github.joaogclima30.carrinhoAPI.service.Imagem;

import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.*;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsImagem.*;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.repository.ImagemRepository;
import io.github.joaogclima30.carrinhoAPI.service.Produto.ProdutoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.sql.SQLException;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ImagemService {
    private final ImagemRepository repository;
    private final ProdutoService produtoService;
    private final DtoMapper mapper;
    private final long maxSize;
    private final int maxCount;
    private final long maxPixels;

    public ImagemService(ImagemRepository repository, ProdutoService produtoService, DtoMapper mapper,
            @Value("${app.images.max-size:5MB}") DataSize maxSize,
            @Value("${app.images.max-count:5}") int maxCount,
            @Value("${app.images.max-pixels:20000000}") long maxPixels) {
        this.repository = repository; this.produtoService = produtoService; this.mapper = mapper;
        this.maxSize = maxSize.toBytes(); this.maxCount = maxCount; this.maxPixels = maxPixels;
        if (this.maxSize <= 0 || maxCount <= 0 || maxPixels <= 0) throw new IllegalArgumentException("Limites de imagem devem ser positivos");
    }

    @Transactional
    public List<ImagemDTO> salvarImagem(List<MultipartFile> files, Long produtoId) {
        if (files == null || files.isEmpty()) throw new DadosInvalidosException("Envie ao menos uma imagem");
        if (files.size() > maxCount) throw new DadosInvalidosException("Quantidade máxima de imagens por envio: " + maxCount);
        List<ArquivoValidado> arquivos = files.stream().map(this::validar).toList();
        Produto produto = produtoService.obterPorId(produtoId);
        List<ImagemDTO> resultados = new ArrayList<>();
        for (ArquivoValidado arquivo : arquivos) {
            Imagem imagem = new Imagem();
            imagem.setProduto(produto);
            preencher(imagem, arquivo);
            repository.save(imagem);
            imagem.setDownloadUrl(mapper.imagem(imagem).getDownloadUrl());
            resultados.add(mapper.imagem(imagem));
        }
        return resultados;
    }

    @Transactional
    public void atualizarImagem(MultipartFile file, Long imagemId) {
        ArquivoValidado arquivo = validar(file);
        Imagem imagem = buscarImagem(imagemId);
        preencher(imagem, arquivo);
        imagem.setDownloadUrl(mapper.imagem(imagem).getDownloadUrl());
    }

    private void preencher(Imagem imagem, ArquivoValidado arquivo) {
        try {
            imagem.setNomeArquivo(arquivo.nome());
            imagem.setTipoArquivo(arquivo.tipo());
            imagem.setImagem(new SerialBlob(arquivo.bytes()));
        } catch (SQLException e) {
            throw new ErroSalvarImagem("Não foi possível armazenar a imagem", e);
        }
    }

    public Imagem buscarImagem(Long id) {
        return repository.findById(id).orElseThrow(() -> new ImagemNaoExiste("Imagem não encontrada"));
    }

    public ImagemDownload buscarImagemParaDownload(Long id) {
        Imagem imagem = buscarImagem(id);
        try {
            if (imagem.getImagem() == null) throw new DadosInvalidosException("Imagem não possui conteúdo");
            long tamanho = imagem.getImagem().length();
            if (tamanho > maxSize || tamanho > Integer.MAX_VALUE) throw new ArquivoMuitoGrandeException("Imagem excede o limite de leitura configurado");
            String tipo = ("image/png".equals(imagem.getTipoArquivo()) || "image/jpeg".equals(imagem.getTipoArquivo())) ? imagem.getTipoArquivo() : "application/octet-stream";
            return new ImagemDownload(imagem.getImagem().getBytes(1, (int) tamanho), tipo, nomeSeguro(imagem.getNomeArquivo(), "bin"));
        } catch (SQLException e) {
            throw new ErroSalvarImagem("Não foi possível ler a imagem", e);
        }
    }

    @Transactional
    public void deletarImagemPorId(Long id) { repository.delete(buscarImagem(id)); }

    private ArquivoValidado validar(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new DadosInvalidosException("Arquivo de imagem vazio");
        if (file.getSize() > maxSize) throw new ArquivoMuitoGrandeException("Imagem excede o tamanho máximo permitido");
        byte[] bytes;
        try { bytes = file.getBytes(); }
        catch (IOException e) { throw new ErroSalvarImagem("Não foi possível ler o arquivo enviado", e); }
        if (bytes.length > maxSize) throw new ArquivoMuitoGrandeException("Imagem excede o tamanho máximo permitido");
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new DadosInvalidosException("Arquivo deve conter uma imagem PNG ou JPEG válida");
            ImageReader reader = readers.next();
            try {
                reader.setInput(input);
                String formato = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!Set.of("png", "jpeg", "jpg").contains(formato)) throw new DadosInvalidosException("Apenas imagens PNG e JPEG são permitidas");
                if ((long) reader.getWidth(0) * reader.getHeight(0) > maxPixels) throw new DadosInvalidosException("Dimensões da imagem excedem o limite permitido");
                if (reader.read(0) == null) throw new DadosInvalidosException("Conteúdo de imagem inválido");
                boolean png = formato.equals("png");
                return new ArquivoValidado(bytes, png ? "image/png" : "image/jpeg", nomeSeguro(file.getOriginalFilename(), png ? "png" : "jpg"));
            } finally { reader.dispose(); }
        } catch (IOException | IllegalArgumentException e) {
            throw new DadosInvalidosException("Arquivo de imagem inválido ou corrompido");
        }
    }

    private String nomeSeguro(String nome, String extensao) {
        if (nome == null) return "imagem." + extensao;
        String limpo = nome.replace('\\', '/');
        limpo = limpo.substring(limpo.lastIndexOf('/') + 1).replaceAll("[\\p{Cntrl}]", "").strip();
        if (limpo.isBlank()) return "imagem." + extensao;
        return limpo.substring(0, Math.min(limpo.length(), 200));
    }

    private record ArquivoValidado(byte[] bytes, String tipo, String nome) {}
}
