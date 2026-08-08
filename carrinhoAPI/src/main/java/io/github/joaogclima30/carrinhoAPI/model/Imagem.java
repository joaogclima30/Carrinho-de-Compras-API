package io.github.joaogclima30.carrinhoAPI.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Blob;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Imagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String nomeArquivo;

    private String tipoArquivo;

    @Lob
    //Tipo blob serve para armazenar imagens, documentos
    private Blob imagem;

    private String downloadUrl;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;
}
