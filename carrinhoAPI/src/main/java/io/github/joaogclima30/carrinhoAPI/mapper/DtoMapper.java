package io.github.joaogclima30.carrinhoAPI.mapper;

import io.github.joaogclima30.carrinhoAPI.model.*;
import io.github.joaogclima30.carrinhoAPI.dto.CategoriaDTO.CategoriaResponseDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoResponseDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import io.github.joaogclima30.carrinhoAPI.dto.CarrinhoDTO.*;
import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.*;
import io.github.joaogclima30.carrinhoAPI.dto.UsuarioDTO.UsuarioResponseDTO;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class DtoMapper {
    private final String prefix;

    public DtoMapper(@org.springframework.beans.factory.annotation.Value("${api.prefix:/api/v1}") String prefix) {
        this.prefix = prefix.replaceAll("/+$", "");
    }

    public CategoriaResponseDTO categoria(Categoria categoria) {
        return categoria == null ? null : new CategoriaResponseDTO(categoria.getId(), categoria.getNome());
    }

    public ImagemDTO imagem(Imagem imagem) {
        ImagemDTO dto = new ImagemDTO();
        dto.setId(imagem.getId());
        dto.setArquivoNome(imagem.getNomeArquivo());
        dto.setDownloadUrl(prefix + "/imagens/imagem/download/" + imagem.getId());
        return dto;
    }

    public ProdutoResponseDTO produto(Produto produto) {
        return new ProdutoResponseDTO(produto.getId(), produto.getNome(), produto.getMarca(),
                produto.getPrice(), produto.getEstoque(), produto.getDescricao(), categoria(produto.getCategoria()),
                produto.getImagens() == null ? List.of() : produto.getImagens().stream().map(this::imagem).toList());
    }

    public CarrinhoItemDto item(CarrinhoItem item) {
        CarrinhoItemDto dto = new CarrinhoItemDto();
        dto.setItemId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnico());
        dto.setProduto(produto(item.getProduto()));
        return dto;
    }

    public CarrinhoDto carrinho(Carrinho carrinho) {
        CarrinhoDto dto = new CarrinhoDto();
        dto.setCarrinhoId(carrinho.getId());
        dto.setPrecoTotal(carrinho.getQuantidadeTotal());
        dto.setItens(carrinho.getCarrinhoItems().stream().map(this::item).collect(Collectors.toSet()));
        return dto;
    }

    public PedidoResponseDTO pedido(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setPedidoId(pedido.getPedidoId());
        dto.setUsuarioId(pedido.getUsuario() == null ? null : pedido.getUsuario().getId());
        dto.setPedidoHorario(pedido.getPedidoHorario());
        dto.setTotalPedido(pedido.getTotalPedido());
        dto.setStatus(pedido.getPedidoStatus().name());
        dto.setItem(pedido.getPedidoItems().stream().map(item -> {
            PedidoItemDTO resultado = new PedidoItemDTO();
            resultado.setProdutoId(item.getProduto().getId());
            resultado.setProdutoNome(item.getProduto().getNome());
            resultado.setQuantidade(item.getQuantidade());
            resultado.setPrice(item.getPreco());
            return resultado;
        }).toList());
        return dto;
    }

    public UsuarioResponseDTO usuario(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setPrimeiroNome(usuario.getPrimeiroNome());
        dto.setSobrenome(usuario.getSobrenome());
        dto.setEmail(usuario.getEmail());
        dto.setCarrinho(usuario.getCarrinho() == null ? null : carrinho(usuario.getCarrinho()));
        dto.setPedidos(usuario.getPedidos() == null ? List.of() : usuario.getPedidos().stream().map(this::pedido).toList());
        return dto;
    }
}
