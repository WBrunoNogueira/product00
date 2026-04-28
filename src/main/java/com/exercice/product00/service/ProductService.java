package com.exercice.product00.service;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.exception.ResourceNotFoundException;
import com.exercice.product00.model.Product;
import com.exercice.product00.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.plaf.PanelUI;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    ProductRepository repository;

    //GET findAll
    public List<Product> findAll(){
        return repository.findAll();
    }

    //GET or findById
    public ProductResponseDTO findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
        return  new ProductResponseDTO(
                product.getId(),
                product.getNome(),
                product.getDescricao(),
                product.getPreco(),
                product.getQuantidadeEstoque()
        );
    }

    //POST OR create
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = new Product();

        product.setNome(dto.getNome());
        product.setDescricao(dto.getDescricao());
        product.setPreco(dto.getPreco());
        product.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        Product savedProduct = repository.save(product);

        return new ProductResponseDTO(
                savedProduct.getId(),
                savedProduct.getNome(),
                savedProduct.getDescricao(),
                savedProduct.getPreco(),
                savedProduct.getQuantidadeEstoque()
        );
    }

    //PUT OR UPDATE
    public  Product update( long id, ProductRequestDTO dto){
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));


        entity.setNome(dto.getNome());
        entity.setDescricao(dto.getDescricao());
        entity.setPreco(dto.getPreco());
        entity.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        return repository.save(entity);
    }

    //DELETE
    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
