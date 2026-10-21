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

public final class DtoMapper {
    private DtoMapper() {}

    public static CategoriaResponseDTO categoria(Categoria categoria) {
        return categoria == null ? null : new CategoriaResponseDTO(categoria.getId(), categoria.getNome());
    }

    public static ImagemDTO imagem(Imagem imagem) {
        ImagemDTO dto = new ImagemDTO();
        dto.setId(imagem.getId());
        dto.setArquivoNome(imagem.getNomeArquivo());
        dto.setDownloadUrl(imagem.getDownloadUrl());
        return dto;
    }

    public static ProdutoResponseDTO produto(Produto produto) {
        return new ProdutoResponseDTO(produto.getId(), produto.getNome(), produto.getMarca(),
                produto.getPrice(), produto.getEstoque(), produto.getDescricao(), categoria(produto.getCategoria()),
                produto.getImagens() == null ? List.of() : produto.getImagens().stream().map(DtoMapper::imagem).toList());
    }

    public static CarrinhoItemDto item(CarrinhoItem item) {
        CarrinhoItemDto dto = new CarrinhoItemDto();
        dto.setItemId(item.getId());
        dto.setQuantidade(item.getQuantidade());
        dto.setPrecoUnitario(item.getPrecoUnico());
        dto.setProduto(produto(item.getProduto()));
        return dto;
    }

    public static CarrinhoDto carrinho(Carrinho carrinho) {
        CarrinhoDto dto = new CarrinhoDto();
        dto.setCarrinhoId(carrinho.getId());
        dto.setPrecoTotal(carrinho.getQuantidadeTotal());
        dto.setItens(carrinho.getCarrinhoItems().stream().map(DtoMapper::item).collect(Collectors.toSet()));
        return dto;
    }

    public static PedidoRequestDTO pedido(Pedido pedido) {
        PedidoRequestDTO dto = new PedidoRequestDTO();
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

    public static UsuarioResponseDTO usuario(Usuario usuario) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(usuario.getId());
        dto.setPrimeiroNome(usuario.getPrimeiroNome());
        dto.setSobrenome(usuario.getSobrenome());
        dto.setEmail(usuario.getEmail());
        dto.setCarrinho(usuario.getCarrinho() == null ? null : carrinho(usuario.getCarrinho()));
        dto.setPedidos(usuario.getPedidos() == null ? List.of() : usuario.getPedidos().stream().map(DtoMapper::pedido).toList());
        return dto;
    }
}
