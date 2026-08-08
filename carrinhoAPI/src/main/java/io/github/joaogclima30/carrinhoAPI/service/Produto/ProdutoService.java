package io.github.joaogclima30.carrinhoAPI.service.Produto;

import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.produtoJaCadastrado;
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
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoValidator produtoValidator;

    public Produto salvarProduto(Produto produto){
        produtoValidator.validarProduto(produto);
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

    public Optional<Produto> obterPorId(Long id){
        return produtoRepository.findById(id);
    }

    //Forma diferente feita deletando o objeto diretamente
    /*@DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'GERENTE')")
    public ResponseEntity<Object> deleteLivro(@Valid @PathVariable("id") String id){
        var idLivro = UUID.fromString(id);
        Optional<Livro> livroOptional = livroService.obterPorId(idLivro);
        livroService.deleteLivro(livroOptional.get());
        if(livroOptional.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    } */
    public void deletarProduto(Produto produto){
        if(!produtoValidator.existeProdutoCadastrado(produto)){
            throw new produtoJaCadastrado("Não é permitido excluir produto que não existe");
        }
        produtoRepository.delete(produto);
    }


    public Produto atualizarProduto(Produto produtoAtualizado){
        Produto produtoExistente = produtoRepository.findById(produtoAtualizado.getId())
                .orElseThrow(() -> new ProdutoNaoEncontrado("Produto não existe"));

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setMarca(produtoAtualizado.getMarca());
        produtoExistente.setPrice(produtoAtualizado.getPrice());
        produtoExistente.setEstoque(produtoAtualizado.getEstoque());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setCategoria(resolverCategoria(produtoAtualizado.getCategoria()));

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
