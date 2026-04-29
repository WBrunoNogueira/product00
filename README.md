# CRUD de Produtos com Spring Boot

Este README serve como material de consulta para o projeto de estudo de um CRUD REST usando **Spring Boot**, **Spring Data JPA**, **H2 Database**, **DTOs**, **Mapper**, **validações** e **tratamento de erros**.

---

## 1. Objetivo do projeto

Criar uma API REST para cadastro de produtos, permitindo:

- listar produtos;
- buscar produto por ID;
- criar produto;
- atualizar produto;
- deletar produto;
- validar dados de entrada;
- tratar erros de forma adequada;
- separar entidade de banco dos dados de entrada e saída usando DTOs;
- centralizar conversões usando Mapper.

---

## 2. Estrutura do projeto

Estrutura final sugerida:

```text
com.exercice.product00
 ├── controller
 │   └── ProductControler.java
 ├── dto
 │   ├── ProductRequestDTO.java
 │   └── ProductResponseDTO.java
 ├── exception
 │   ├── ResourceNotFoundException.java
 │   └── GlobalExceptionHandler.java
 ├── mapper
 │   └── ProductMapper.java
 ├── model
 │   └── Product.java
 ├── repository
 │   └── ProductRepository.java
 └── service
     └── ProductService.java
```

---

## 3. Configuração do banco H2

Arquivo:

```text
src/main/resources/application.yml
```

Exemplo usando H2 em memória:

```yaml
spring:
  application:
    name: crud-produto

  datasource:
    url: jdbc:h2:mem:produtosdb
    driver-class-name: org.h2.Driver
    username: root
    password: admin

  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true

  h2:
    console:
      enabled: true
      path: /h2-console
```

Acesso ao console H2:

```text
http://localhost:8080/h2-console
```

Dados de conexão:

```text
JDBC URL: jdbc:h2:mem:produtosdb
User Name: root
Password: admin
```

Se quiser acessar melhor pelo Database Plugin do IntelliJ, prefira H2 em arquivo:

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/produtosdb
    driver-class-name: org.h2.Driver
    username: root
    password: admin
```

No IntelliJ:

```text
Database → + → Data Source → H2
URL: jdbc:h2:file:./data/produtosdb
User: root
Password: admin
```

---

## 4. Entity / Model

A classe `Product` representa a tabela do banco de dados.

Arquivo:

```text
model/Product.java
```

Exemplo:

```java
package com.exercice.product00.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;

    public Product() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }
}
```

Observação:

- `Product` é a entidade do banco.
- As validações de entrada ficam no `ProductRequestDTO`, não na entidade.

---

## 5. Repository

Arquivo:

```text
repository/ProductRepository.java
```

Código:

```java
package com.exercice.product00.repository;

import com.exercice.product00.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

Ao estender `JpaRepository`, o Spring já fornece métodos prontos:

```java
findAll();
findById(id);
save(product);
delete(product);
existsById(id);
```

---

## 6. DTOs

DTO significa **Data Transfer Object**.

No projeto, os DTOs separam os dados da API da entidade do banco.

Resumo:

```text
ProductRequestDTO  → dados que chegam na API
ProductResponseDTO → dados que saem da API
Product            → entidade do banco
```

---

## 6.1 ProductRequestDTO

Usado no `POST` e no `PUT`.

Ele representa os dados enviados pelo cliente.

Arquivo:

```text
dto/ProductRequestDTO.java
```

Código:

```java
package com.exercice.product00.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class ProductRequestDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String nome;

    private String descricao;

    @DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
    private BigDecimal preco;

    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
    private Integer quantidadeEstoque;

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
}
```

Por que o `ProductRequestDTO` não tem `id`?

Porque o `id` é gerado automaticamente pelo banco.

Exemplo de JSON de entrada:

```json
{
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

---

## 6.2 ProductResponseDTO

Usado para devolver dados ao cliente.

Arquivo:

```text
dto/ProductResponseDTO.java
```

Código:

```java
package com.exercice.product00.dto;

import java.math.BigDecimal;

public class ProductResponseDTO {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private Integer quantidadeEstoque;

    public ProductResponseDTO(Long id, String nome, String descricao, BigDecimal preco, Integer quantidadeEstoque) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }
}
```

Exemplo de JSON de resposta:

```json
{
  "id": 1,
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

---

## 7. Mapper

O `Mapper` é uma classe responsável por converter objetos.

Ele evita que o `Service` fique cheio de código de conversão manual.

Resumo:

```text
ProductRequestDTO → Product
Product           → ProductResponseDTO
ProductRequestDTO → Product existente
```

Arquivo:

```text
mapper/ProductMapper.java
```

Código:

```java
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
```

Explicação simples:

```text
DTO = objeto usado pela API
Entity = objeto usado pelo banco
Mapper = classe que traduz um para o outro
```

---

## 8. Service usando DTOs e Mapper

Arquivo:

```text
service/ProductService.java
```

Exemplo:

```java
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

    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProductResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();
    }

    public ProductResponseDTO findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        return mapper.toResponseDTO(product);
    }

    public ProductResponseDTO create(ProductRequestDTO dto) {
        Product product = mapper.toEntity(dto);
        Product savedProduct = repository.save(product);
        return mapper.toResponseDTO(savedProduct);
    }

    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        mapper.updateEntity(entity, dto);

        Product updatedProduct = repository.save(entity);

        return mapper.toResponseDTO(updatedProduct);
    }

    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
```

Fluxo do `create`:

```text
ProductRequestDTO
        ↓
ProductMapper.toEntity(dto)
        ↓
Product
        ↓
repository.save(product)
        ↓
Product salvo com id
        ↓
ProductMapper.toResponseDTO(savedProduct)
        ↓
ProductResponseDTO
```

---

## 9. Controller usando DTOs

Arquivo:

```text
controller/ProductControler.java
```

Exemplo:

```java
package com.exercice.product00.controller;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductControler {

    private final ProductService service;

    public ProductControler(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        List<ProductResponseDTO> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) {
        ProductResponseDTO product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
        ProductResponseDTO createdProduct = service.create(dto);
        return ResponseEntity.status(201).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO dto
    ) {
        ProductResponseDTO updatedProduct = service.update(id, dto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

Resumo dos endpoints:

```text
GET     /products       → listar produtos
GET     /products/{id}  → buscar produto por ID
POST    /products       → criar produto
PUT     /products/{id}  → atualizar produto
DELETE  /products/{id}  → deletar produto
```

---

## 10. Tratamento de erro 404

Criar uma exception personalizada ajuda a representar melhor quando um produto não é encontrado.

Arquivo:

```text
exception/ResourceNotFoundException.java
```

Código:

```java
package com.exercice.product00.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

Essa exception é usada no service:

```java
.orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));
```

---

## 11. GlobalExceptionHandler

O `GlobalExceptionHandler` centraliza o tratamento de erros da API.

Arquivo:

```text
exception/GlobalExceptionHandler.java
```

Exemplo:

```java
package com.exercice.product00.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());

        return ResponseEntity.status(404).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(errors);
    }
}
```

Quando o produto não existe, a API retorna:

```http
404 Not Found
```

Exemplo:

```json
{
  "message": "Produto não encontrado com id: 999"
}
```

Quando os dados são inválidos, a API retorna:

```http
400 Bad Request
```

Exemplo:

```json
{
  "nome": "O nome é obrigatório",
  "preco": "O preço deve ser maior que zero",
  "quantidadeEstoque": "A quantidade em estoque não pode ser negativa"
}
```

---

## 12. Validações

Dependência no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

As validações ficam no `ProductRequestDTO`:

```java
@NotBlank(message = "O nome é obrigatório")
private String nome;

@DecimalMin(value = "0.01", message = "O preço deve ser maior que zero")
private BigDecimal preco;

@Min(value = 0, message = "A quantidade em estoque não pode ser negativa")
private Integer quantidadeEstoque;
```

No controller, a validação é ativada com `@Valid`:

```java
@PostMapping
public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO dto) {
    ProductResponseDTO createdProduct = service.create(dto);
    return ResponseEntity.status(201).body(createdProduct);
}
```

E também no `PUT`:

```java
@PutMapping("/{id}")
public ResponseEntity<ProductResponseDTO> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequestDTO dto
) {
    ProductResponseDTO updatedProduct = service.update(id, dto);
    return ResponseEntity.ok(updatedProduct);
}
```

---

## 13. Testes no Postman

### 13.1 Listar produtos

```http
GET http://localhost:8080/products
```

Esperado:

```http
200 OK
```

---

### 13.2 Criar produto

```http
POST http://localhost:8080/products
```

Body:

```json
{
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

Esperado:

```http
201 Created
```

Resposta:

```json
{
  "id": 1,
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

---

### 13.3 Buscar por ID

```http
GET http://localhost:8080/products/1
```

Esperado:

```http
200 OK
```

---

### 13.4 Atualizar produto

```http
PUT http://localhost:8080/products/1
```

Body:

```json
{
  "nome": "Mouse Atualizado",
  "descricao": "Mouse sem fio",
  "preco": 199.90,
  "quantidadeEstoque": 8
}
```

Esperado:

```http
200 OK
```

---

### 13.5 Deletar produto

```http
DELETE http://localhost:8080/products/1
```

Esperado:

```http
204 No Content
```

---

### 13.6 Testar produto inexistente

```http
GET http://localhost:8080/products/999
```

Esperado:

```http
404 Not Found
```

Exemplo:

```json
{
  "message": "Produto não encontrado com id: 999"
}
```

---

### 13.7 Testar validação

```http
POST http://localhost:8080/products
```

Body inválido:

```json
{
  "nome": "",
  "descricao": "Produto inválido",
  "preco": 0,
  "quantidadeEstoque": -1
}
```

Esperado:

```http
400 Bad Request
```

Exemplo:

```json
{
  "nome": "O nome é obrigatório",
  "preco": "O preço deve ser maior que zero",
  "quantidadeEstoque": "A quantidade em estoque não pode ser negativa"
}
```

---

## 14. Collection no Postman

Sugestão de collection:

```text
CRUD Products
```

Variável:

```text
baseUrl = http://localhost:8080
```

Requests:

```text
GET     {{baseUrl}}/products
GET     {{baseUrl}}/products/1
POST    {{baseUrl}}/products
PUT     {{baseUrl}}/products/1
DELETE  {{baseUrl}}/products/1
```

---

## 15. Erros encontrados e correções

### 15.1 Bean do Service não encontrado

Erro:

```text
Parameter 0 of constructor in ProductControler required a bean of type ProductService that could not be found
```

Causa comum:

```text
ProductService estava sem @Service
```

Correção:

```java
@Service
public class ProductService {
}
```

---

### 15.2 Ambiguous mapping

Erro:

```text
Ambiguous mapping. Cannot map 'productControler' method
```

Causa comum:

Dois endpoints com a mesma rota, por exemplo:

```java
@GetMapping
public ResponseEntity<List<ProductResponseDTO>> findAll() { }

@GetMapping
public ResponseEntity<ProductResponseDTO> findById(Long id) { }
```

Correção:

```java
@GetMapping
public ResponseEntity<List<ProductResponseDTO>> findAll() { }

@GetMapping("/{id}")
public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) { }
```

---

## 16. Situação atual do projeto

Até aqui, o projeto possui:

```text
CRUD completo
H2 configurado
Endpoints REST
Postman funcionando
Tratamento de erro 404
Tratamento de erro 400
Validações com Bean Validation
ProductRequestDTO
ProductResponseDTO
ProductMapper
Service usando Mapper
Controller usando DTOs
```

Fluxo atual da aplicação:

```text
Postman
  ↓
Controller
  ↓
ProductRequestDTO
  ↓
Service
  ↓
Mapper
  ↓
Product Entity
  ↓
Repository
  ↓
H2 Database
  ↓
ProductResponseDTO
  ↓
Resposta HTTP
```

---

## 17. Próximos passos recomendados

Depois dessa etapa, os próximos passos são:

1. Criar testes unitários para o `ProductService`.
2. Criar testes do `Controller` usando `MockMvc`.
3. Melhorar o padrão de resposta de erro com `timestamp`, `status` e `message`.
4. Substituir H2 por PostgreSQL usando Docker Compose.
5. Adicionar migrations com Flyway.
6. Criar documentação da API com Swagger/OpenAPI.

Ordem recomendada:

```text
1. Testes unitários do ProductService
2. Testes do Controller com MockMvc
3. Padronização avançada dos erros
4. PostgreSQL com Docker
5. Flyway
6. Swagger
```

---

## 18. Conceitos principais aprendidos

```text
REST
Controller
Service
Repository
Entity
DTO
Mapper
ResponseEntity
@PathVariable
@RequestBody
@Valid
Exception personalizada
GlobalExceptionHandler
H2 Database
Postman Collection
```

Resumo final:

```text
RequestDTO = o que chega na API
Product = o que vai para o banco
ResponseDTO = o que sai da API
Mapper = quem converte os objetos
Service = quem organiza o fluxo
Repository = quem acessa o banco
Controller = quem expõe os endpoints
```
