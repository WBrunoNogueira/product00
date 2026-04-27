package com.exercice.product00.service;

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
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }
    //POST OR create
    public Product create(Product product){
        return repository.save(product);
    }

    //PUT OR UPDATE
    public  Product update( long id, Product product){
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        entity.setNome(product.getNome());
        entity.setDescricao(product.getDescricao());
        entity.setPreco(product.getPreco());
        entity.setQuantidadeEstoque(product.getQuantidadeEstoque());

        return repository.save(entity);
    }

    //DELETE
    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
