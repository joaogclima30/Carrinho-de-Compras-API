package io.github.joaogclima30.carrinhoAPI.service.Pedido;

import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.PedidoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExpectionsPedidos.PedidoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.Enum.PedidoStatus;
import io.github.joaogclima30.carrinhoAPI.model.Pedido;
import io.github.joaogclima30.carrinhoAPI.model.PedidoItem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import io.github.joaogclima30.carrinhoAPI.repository.PedidoRepository;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final CarrinhoService carrinhoService;
    private final ModelMapper modelMapper;

    @Transactional
    public Pedido fazerPedido(Long usuarioId){
        Carrinho carrinhoEncontrado = carrinhoRepository
                .findByUsuarioId(usuarioId)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));

        Pedido pedido = criarPedido(carrinhoEncontrado);
        List<PedidoItem> pedidoItems = criarPedidoItem(pedido,carrinhoEncontrado);

        pedido.setPedidoItems(new HashSet<>(pedidoItems));
        pedido.setTotalPedido(calcularQuantidadeTotal(pedidoItems));
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        carrinhoService.limparCarrinho(carrinhoEncontrado.getId());

        return pedidoSalvo;
    }

    public Pedido criarPedido(Carrinho carrinho){
        Pedido pedido = new Pedido();

        pedido.setUsuario(carrinho.getUsuario());

        pedido.setPedidoStatus(PedidoStatus.PENDENTE);
        pedido.setPedidoHorario(LocalDateTime.now());
        return pedido;
    }

    private List<PedidoItem> criarPedidoItem (Pedido pedido, Carrinho carrinho){
        return carrinho.getCarrinhoItems().stream().map(carrinhoItem -> {
            Produto produto = carrinhoItem.getProduto();
            produto.setEstoque(produto.getEstoque() - carrinhoItem.getQuantidade());
            produtoRepository.save(produto);
            return new PedidoItem(
                    pedido,
                    produto,
                    carrinhoItem.getQuantidade(),
                    carrinhoItem.getPrecoUnico());
        }).toList();
    }

    private BigDecimal calcularQuantidadeTotal(List<PedidoItem> pedidoItemList){
        return pedidoItemList
                .stream()
                .map(item -> item.getPreco()
                        .multiply(new BigDecimal(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public PedidoRequestDTO listarPedido(Long pedidoId){
        return pedidoRepository.findById(pedidoId)
                .map(this::converterToDTO)
                .orElseThrow(() -> new PedidoNaoEncontrado("Pedido não Encontrado"));
    }

    public List<PedidoRequestDTO> listarPedidosPorUsuario(Long usuarioId){
        List<PedidoRequestDTO> pedidos = pedidoRepository.findByUsuarioId(usuarioId);
        return pedidos.stream().map(this ::converterToDTO).toList();
    }

    private PedidoRequestDTO converterToDTO(Pedido pedido){
        return modelMapper.map(pedido,PedidoRequestDTO.class);
    }

}
