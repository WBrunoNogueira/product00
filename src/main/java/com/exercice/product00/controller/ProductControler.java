package com.exercice.product00.controller;

import com.exercice.product00.model.Product;
import com.exercice.product00.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// responsavel por receber as requisiçoes http pela rota /products
@RestController
@RequestMapping("/products")
public class ProductControler {

    private final ProductService service;

    public ProductControler(ProductService service) {
        this.service = service;
    }

    //endPonts
        //findAll GET
    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }
        //findById  GET
    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product product = service.findById(id);
        return ResponseEntity.ok(product);
    }

        //create POST
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product createdProduct = service.create(product);
        return ResponseEntity.status(201).body(createdProduct);
    }

        //UPDATE PUT
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestBody Product product
    ) {
        Product updatedProduct = service.update(id, product);
        return ResponseEntity.ok(updatedProduct);
    }
        //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
