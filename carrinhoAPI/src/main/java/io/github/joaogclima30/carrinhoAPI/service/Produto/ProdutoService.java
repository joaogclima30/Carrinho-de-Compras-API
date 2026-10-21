package io.github.joaogclima30.carrinhoAPI.service.Produto;

import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoResponseDTO;
import io.github.joaogclima30.carrinhoAPI.mapper.DtoMapper;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.CategoriaRepository;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import io.github.joaogclima30.carrinhoAPI.validator.ProdutoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@org.springframework.transaction.annotation.Transactional
@RequiredArgsConstructor
@org.springframework.validation.annotation.Validated
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoValidator produtoValidator;

    public Produto salvarProduto(@jakarta.validation.Valid ProdutoRequestDTO produtoRequestDTO){
        produtoValidator.validarProduto(produtoRequestDTO);

        Produto produto = new Produto(
                produtoRequestDTO.getNome(),
                produtoRequestDTO.getMarca(),
                produtoRequestDTO.getPreco(),
                produtoRequestDTO.getEstoque(),
                produtoRequestDTO.getDescricao(),
                new Categoria(produtoRequestDTO.getCategoria().getNome())
        );
        produto.setCategoria(resolverCategoria(produto.getCategoria()));
        return produtoRepository.save(produto);
    }

    private Categoria resolverCategoria(Categoria categoriaDoProduto){
        return categoriaRepository.findByNome(categoriaDoProduto.getNome())
                .orElseGet(() -> categoriaRepository.save(new Categoria(categoriaDoProduto.getNome())));
    }

    //Verificar melhor forma para fazer verificação de produto não encontrado
    public Optional<Produto> obterProduto(Long id){
        return Optional.of(produtoRepository.findById(id).orElseThrow(()->new ProdutoNaoEncontrado("Produto não existe")));
    }

    public Produto obterPorId(Long id){
        return produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontrado("Produto não existe"));
    }

    public void deletarProduto(Long id){
        produtoRepository.deleteById(id);
    }


    public Produto atualizarProduto(@jakarta.validation.Valid ProdutoRequestDTO produtoAtualizado, Long id){
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontrado("Produto não existe"));

        produtoValidator.validarAtualizacao(produtoAtualizado, id);
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setMarca(produtoAtualizado.getMarca());
        produtoExistente.setPrice(produtoAtualizado.getPreco());
        produtoExistente.setEstoque(produtoAtualizado.getEstoque());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setCategoria(resolverCategoria(new Categoria(produtoAtualizado.getCategoria().getNome())));

        return produtoRepository.save(produtoExistente);
    }

    public List<Produto> listarTodosProdutos(){
        return produtoRepository.findAll();
    }

    public List<Produto> listarProdutosPorCategoria(String categoria){
        return produtoRepository.findByCategoriaNome(categoria);
    }

    public List<Produto> listarProdutosPorMarca(String marca){
        return produtoRepository.findByMarca(marca);
    }

    public List<Produto> listarProdutoPorCategoriaAndMarca(String categoria,String marca){
        return produtoRepository.findByCategoriaNomeAndMarca(categoria,marca);
    }

    public List<Produto> listarProdutoPorNome(String nome){
        return produtoRepository.findByNome(nome);
    }

    public List<Produto> listarProdutoPorMarcaAndNome(String marca, String nome){
        return produtoRepository.findByMarcaAndNome(marca, nome);
    }

     public Long contadorProdutosPorMarcaAndNome(String marca, String nome){
        return produtoRepository.countByMarcaAndNome(marca,nome);
     }

     public List<ProdutoResponseDTO> listarProdutosConvertidos(List<Produto> produtos){
        return produtos.stream().map(this::converterParaDto).toList();
     }

    public ProdutoResponseDTO converterParaDto(Produto produto) {
        return DtoMapper.produto(produto);
    }
}
