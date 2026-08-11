package io.github.joaogclima30.carrinhoAPI.service.Produto;

import io.github.joaogclima30.carrinhoAPI.dto.ImagemDTO.ImagemDTO;
import io.github.joaogclima30.carrinhoAPI.dto.ProdutoDTO.ProdutoRequestDTO;
import io.github.joaogclima30.carrinhoAPI.exceptions.ExceptionsProdutos.ProdutoNaoEncontrado;
import io.github.joaogclima30.carrinhoAPI.model.Categoria;
import io.github.joaogclima30.carrinhoAPI.model.Imagem;
import io.github.joaogclima30.carrinhoAPI.model.Produto;
import io.github.joaogclima30.carrinhoAPI.repository.CategoriaRepository;
import io.github.joaogclima30.carrinhoAPI.repository.ImagemRepository;
import io.github.joaogclima30.carrinhoAPI.repository.ProdutoRepository;
import io.github.joaogclima30.carrinhoAPI.validator.ProdutoValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoValidator produtoValidator;
    private final ModelMapper modelMapper;
    private final ImagemRepository imagemRepository;

    public Produto salvarProduto(ProdutoRequestDTO produtoRequestDTO){
        produtoValidator.validarProduto(produtoRequestDTO);

        Produto produto = new Produto(
                produtoRequestDTO.getNome(),
                produtoRequestDTO.getMarca(),
                produtoRequestDTO.getPreco(),
                produtoRequestDTO.getEstoque(),
                produtoRequestDTO.getDescricao(),
                produtoRequestDTO.getCategoria()
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
        return produtoRepository.findById(id);
    }

    public void deletarProduto(Long id){
        produtoRepository.deleteById(id);
    }


    public Produto atualizarProduto(ProdutoRequestDTO produtoAtualizado, Long id){
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoNaoEncontrado("Produto não existe"));

        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setMarca(produtoAtualizado.getMarca());
        produtoExistente.setPrice(produtoAtualizado.getPreco());
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

     public List<ProdutoRequestDTO> listarProdutosConvertidos(List<Produto> produtos){
        return produtos.stream().map(this::converterParaDto).toList();
     }

     public ProdutoRequestDTO converterParaDto(Produto produto){
        ProdutoRequestDTO produtoRequestDTO = modelMapper.map(produto,ProdutoRequestDTO.class);
        List<Imagem> imagems = imagemRepository.findByProdutoId(produto.getId());
        List<ImagemDTO> imagemDTOS = imagems.stream().map(imagem -> modelMapper.map(imagem, ImagemDTO.class)).toList();
        produtoRequestDTO.setImagensDto(imagemDTOS);
        return produtoRequestDTO;
    }
}
