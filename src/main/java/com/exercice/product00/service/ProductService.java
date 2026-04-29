package com.exercice.product00.service;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.exception.ResourceNotFoundException;
import com.exercice.product00.mapper.ProductMapper;
import com.exercice.product00.model.Product;
import com.exercice.product00.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    //@Autowired
    //ProductRepository repository;
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    //GET findAll
    public List<ProductResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    //GET or findById
    public ProductResponseDTO findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
        return  mapper.toResponseDTO(product);
    }

    //POST OR create
    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = mapper.toEntity(dto);

        Product savedProduct = repository.save(product);
        return mapper.toResponseDTO(savedProduct);
    }

    //PUT OR UPDATE
    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        mapper.updateEntity(entity, dto);

        Product updatedProduct = repository.save(entity);

        return mapper.toResponseDTO(updatedProduct);
    }

    //DELETE
    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
