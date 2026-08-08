package io.github.joaogclima30.carrinhoAPI.service;

import io.github.joaogclima30.carrinhoAPI.exceptions.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.produtoJaCadastrado;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import io.github.joaogclima30.carrinhoAPI.validator.ProdutoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    private final ProdutoValidator produtoValidator;

    public Produto salvarProduto(Produto produto){
        produtoValidator.validarProduto(produto);
        return produtoRepository.save(produto);
    }

    //Verificar melhor forma para fazer verificação de produto não encontrado
    public Optional<Produto> obterProduto(Long id){
        return Optional.of(produtoRepository.findById(id).orElseThrow(()->new ProdutoNaoEncontrado("Produto não existe")));
    }

    public Optional<Produto> obterPorId(Long id){
        return produtoRepository.findById(id);
    }

    //Forma diferente feita deletando o objeto diretamente
    public void deletarProduto(Produto produto){
        if(!produtoValidator.existeProdutoCadastrado(produto)){
            throw new produtoJaCadastrado("Não é permitido excluir produto que não existe");
        }
        produtoRepository.delete(produto);
    }

    public Produto atualizarProduto(Produto produto){
        if(produto.getId() == null){
            throw new IllegalArgumentException("Objeto produto nulo");
        }
        produtoValidator.validarProduto(produto);
        return produtoRepository.save(produto);
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
        return produtoRepository.findByCategoriaAndMarca(categoria,marca);
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
}
