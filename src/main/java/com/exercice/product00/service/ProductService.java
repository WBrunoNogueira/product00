package com.exercice.product00.service;

import com.exercice.product00.dto.ProductRequestDTO;
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
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
    }

    //POST OR create
    public Product create(ProductRequestDTO dto){
        Product product = new Product();

        product.setNome(dto.getNome());
        product.setDescricao(dto.getDescricao());
        product.setPreco(dto.getPreco());
        product.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        return repository.save(product);
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
