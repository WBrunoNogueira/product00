package com.exercice.product00.mapper;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequestDTO dto) {
        Product product = new Product();

        product.setNome(dto.getNome());
        product.setDescricao(dto.getDescricao());
        product.setPreco(dto.getPreco());
        product.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        return product;
    }
    public ProductResponseDTO toResponseDTO(Product product) {
        return new ProductResponseDTO(
                product.getId(),
                product.getNome(),
                product.getDescricao(),
                product.getPreco(),
                product.getQuantidadeEstoque()
        );
    }

    public void updateEntity(Product product, ProductRequestDTO dto) {
        product.setNome(dto.getNome());
        product.setDescricao(dto.getDescricao());
        product.setPreco(dto.getPreco());
        product.setQuantidadeEstoque(dto.getQuantidadeEstoque());
    }
}
