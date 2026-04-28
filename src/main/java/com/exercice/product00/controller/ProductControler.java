package com.exercice.product00.controller;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.model.Product;
import com.exercice.product00.service.ProductService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {
        ProductResponseDTO product = service.findById(id);
        return ResponseEntity.ok(product);
    }

        //create POST
    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody   ProductRequestDTO dto) {
        ProductResponseDTO createdProduct = service.create(dto);
        return ResponseEntity.status(201).body(createdProduct);
    }

        //UPDATE PUT
    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @Valid  @RequestBody ProductRequestDTO dto
    ) {
        Product updatedProduct = service.update(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }
        //Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

}
