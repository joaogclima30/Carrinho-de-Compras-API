package io.github.joaogclima30.carrinhoAPI.service.Pedido;

import io.github.joaogclima30.carrinhoAPI.dto.PedidoDTO.PedidoResponseDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsCarrinho.CarrinhoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsPedidos.PedidoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Carrinho;
import io.github.joaogclima30.carrinhoAPI.model.Enum.PedidoStatus;
import io.github.joaogclima30.carrinhoAPI.model.Pedido;
import io.github.joaogclima30.carrinhoAPI.model.PedidoItem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.CarrinhoRepository;
import io.github.joaogclima30.carrinhoAPI.repository.PedidoRepository;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import io.github.joaogclima30.carrinhoAPI.service.Carrinho.CarrinhoService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.exceptions.DadosInvalidosException;
import io.github.joaogclima30.carrinhoAPI.exceptions.ConflitoException;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final CarrinhoRepository carrinhoRepository;
    private final CarrinhoService carrinhoService;
    private final DtoMapper mapper;
    private final jakarta.persistence.EntityManager entityManager;


    @Transactional
    public Pedido fazerPedido(Long usuarioId){
        Carrinho carrinhoEncontrado = carrinhoRepository
                .buscarPorUsuarioParaAtualizar(usuarioId)
                .orElseThrow(() -> new CarrinhoNaoEncontrado("Carrinho não encontrado"));

        if (carrinhoEncontrado.getCarrinhoItems().isEmpty()) {
            throw new DadosInvalidosException("Não é possível finalizar um carrinho vazio");
        }
        Pedido pedido = criarPedido(carrinhoEncontrado);
        List<PedidoItem> pedidoItems = criarPedidoItem(pedido,carrinhoEncontrado);

        pedido.setPedidoItems(new HashSet<>(pedidoItems));
        pedido.setTotalPedido(calcularQuantidadeTotal(pedidoItems));
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        carrinhoService.limparCarrinho(carrinhoEncontrado.getId());

        return pedidoSalvo;
    }

    @Transactional
    public PedidoResponseDTO fazerPedidoDto(Long usuarioId) {
        return mapper.pedido(fazerPedido(usuarioId));
    }

    public Pedido criarPedido(Carrinho carrinho){
        Pedido pedido = new Pedido();

        pedido.setUsuario(carrinho.getUsuario());

        pedido.setPedidoStatus(PedidoStatus.PENDENTE);
        pedido.setPedidoHorario(LocalDateTime.now());
        return pedido;
    }

    private List<PedidoItem> criarPedidoItem (Pedido pedido, Carrinho carrinho){
        List<Long> ids = carrinho.getCarrinhoItems().stream()
                .map(item -> item.getProduto().getId()).distinct().sorted().toList();
        Map<Long, Produto> produtos = produtoRepository.buscarParaAtualizarEstoque(ids).stream()
                .collect(Collectors.toMap(Produto::getId, Function.identity()));
        // O carrinho pode ter carregado produtos antes da aquisição dos bloqueios.
        // Atualiza o contexto de persistência com os saldos efetivamente bloqueados.
        produtos.values().forEach(produto -> entityManager.refresh(produto, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE));
        // Valida todos os itens antes de alterar qualquer saldo.
        for (var item : carrinho.getCarrinhoItems()) {
            if (item.getQuantidade() <= 0 || item.getPrecoUnico() == null || item.getPrecoUnico().signum() <= 0) {
                throw new DadosInvalidosException("Item do carrinho com quantidade ou preço inválido");
            }
            Produto produto = produtos.get(item.getProduto().getId());
            if (produto == null || item.getQuantidade() > produto.getEstoque()) {
                throw new ConflitoException("Estoque insuficiente para finalizar o pedido");
            }
        }
        return carrinho.getCarrinhoItems().stream().map(carrinhoItem -> {
            Produto produto = produtos.get(carrinhoItem.getProduto().getId());
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

    public PedidoResponseDTO listarPedido(Long pedidoId){
        return pedidoRepository.findById(pedidoId)
                .map(this::converterToDTO)
                .orElseThrow(() -> new PedidoNaoEncontrado("Pedido não Encontrado"));
    }

    public List<PedidoResponseDTO> listarPedidosPorUsuario(Long usuarioId){
        List<Pedido> pedidos = pedidoRepository.findByUsuarioId(usuarioId);
        return pedidos.stream().map(this ::converterToDTO).toList();
    }

    public PedidoResponseDTO converterToDTO(Pedido pedido){
        return mapper.pedido(pedido);
    }

}
