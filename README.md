# Guia de Estudo — CRUD com Spring Boot, H2, Validações e DTOs

Este material resume o passo a passo feito durante a criação de uma API REST simples de **CRUD de produtos** usando **Spring Boot**, **Spring Data JPA**, **H2 Database**, **Bean Validation**, **tratamento de erros**, **DTOs** e **Postman**.

O objetivo é servir como consulta para revisar depois o código e entender o papel de cada camada.

---

## 1. Objetivo do projeto

Criar uma API REST para gerenciar produtos.

A API permite:

- Listar todos os produtos
- Buscar produto por ID
- Criar produto
- Atualizar produto
- Deletar produto
- Validar dados de entrada
- Retornar erro `404 Not Found` quando o produto não existir
- Usar DTOs para separar entrada/saída da entidade do banco

Endpoints finais:

```http
GET     /products
GET     /products/{id}
POST    /products
PUT     /products/{id}
DELETE  /products/{id}
```

---

## 2. Estrutura final do projeto

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
 ├── model
 │   └── Product.java
 ├── repository
 │   └── ProductRepository.java
 ├── service
 │   └── ProductService.java
 └── Product00Application.java
```

Responsabilidade de cada camada:

| Camada | Responsabilidade |
|---|---|
| `model` | Representa a entidade/tabela do banco |
| `dto` | Representa os dados de entrada e saída da API |
| `repository` | Faz comunicação com o banco de dados |
| `service` | Contém as regras e operações do sistema |
| `controller` | Expõe os endpoints REST |
| `exception` | Centraliza os tratamentos de erro |

Fluxo geral da aplicação:

```text
Postman
  ↓
Controller
  ↓
Service
  ↓
Repository
  ↓
H2 Database
```

---

## 3. Dependências principais

No `pom.xml`, foram usadas dependências como:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

---

## 4. Configuração do H2 no `application.yml`

Arquivo:

```text
src/main/resources/application.yml
```

Configuração usada:

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

Consulta para testar:

```sql
SELECT * FROM PRODUCTS;
```

Observação: como foi usado `jdbc:h2:mem`, o banco existe apenas enquanto a aplicação estiver rodando.

---

## 5. Entidade `Product`

Arquivo:

```text
model/Product.java
```

A entidade representa a tabela do banco.

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

Pontos importantes:

- `@Entity`: informa que a classe é uma entidade JPA.
- `@Table(name = "products")`: define o nome da tabela.
- `@Id`: define a chave primária.
- `@GeneratedValue(strategy = GenerationType.IDENTITY)`: deixa o banco gerar o ID automaticamente.

As validações foram removidas daqui e colocadas no DTO de entrada.

---

## 6. Repository

Arquivo:

```text
repository/ProductRepository.java
```

```java
package com.exercice.product00.repository;

import com.exercice.product00.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
```

Ao estender `JpaRepository<Product, Long>`, o Spring já fornece métodos prontos:

```java
findAll();
findById(id);
save(product);
delete(product);
existsById(id);
```

---

## 7. DTOs

DTO significa **Data Transfer Object**.

Ele serve para transportar dados entre a API e o cliente, sem expor diretamente a entidade do banco.

Neste projeto ficaram três papéis:

```text
Product
→ entidade do banco

ProductRequestDTO
→ dados que entram na API

ProductResponseDTO
→ dados que saem da API
```

Resumo mental:

```text
RequestDTO = o que chega
Product    = o que vai para o banco
ResponseDTO = o que volta para o cliente
```

---

## 8. `ProductRequestDTO`

Arquivo:

```text
dto/ProductRequestDTO.java
```

Esse DTO é usado no `POST` e no `PUT`, ou seja, nos dados que chegam na API.

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

Por que ele não tem `id`?

Porque o cliente não deve informar o ID ao criar um produto. O ID é gerado pelo banco.

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

## 9. `ProductResponseDTO`

Arquivo:

```text
dto/ProductResponseDTO.java
```

Esse DTO é usado para devolver dados ao cliente.

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

Exemplo de JSON de saída:

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

## 10. Explicação humana sobre DTO

Pense que existem três caixas:

```text
1. ProductRequestDTO
   Caixa que recebe o pedido do cliente.

2. Product
   Caixa que o banco entende.

3. ProductResponseDTO
   Caixa organizada para devolver a resposta ao cliente.
```

Fluxo no `POST`:

```text
Cliente manda JSON pelo Postman
        ↓
Controller recebe como ProductRequestDTO
        ↓
Service cria um Product com esses dados
        ↓
Repository salva Product no banco
        ↓
Banco gera o ID
        ↓
Service monta ProductResponseDTO
        ↓
Controller devolve a resposta
```

DTO não é a mesma coisa que getter/setter.

```text
DTO
→ classe usada para transportar dados

get/set
→ métodos usados para pegar ou colocar valores dentro de um objeto
```

---

## 11. Exception personalizada

Arquivo:

```text
exception/ResourceNotFoundException.java
```

```java
package com.exercice.product00.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

Essa exception representa o erro de recurso não encontrado.

Exemplo:

```text
Produto não encontrado com id: 999
```

---

## 12. Tratamento global de erros

Arquivo:

```text
exception/GlobalExceptionHandler.java
```

Exemplo de implementação:

```java
package com.exercice.product00.exception;

import org.springframework.http.HttpStatus;
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

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
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

O que ele faz:

- `ResourceNotFoundException` vira `404 Not Found`.
- Erros de validação viram `400 Bad Request`.

Exemplo de erro `404`:

```json
{
  "message": "Produto não encontrado com id: 999"
}
```

Exemplo de erro `400`:

```json
{
  "nome": "O nome é obrigatório",
  "preco": "O preço deve ser maior que zero",
  "quantidadeEstoque": "A quantidade em estoque não pode ser negativa"
}
```

---

## 13. Service com DTOs

Arquivo:

```text
service/ProductService.java
```

```java
package com.exercice.product00.service;

import com.exercice.product00.dto.ProductRequestDTO;
import com.exercice.product00.dto.ProductResponseDTO;
import com.exercice.product00.exception.ResourceNotFoundException;
import com.exercice.product00.model.Product;
import com.exercice.product00.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(product -> new ProductResponseDTO(
                        product.getId(),
                        product.getNome(),
                        product.getDescricao(),
                        product.getPreco(),
                        product.getQuantidadeEstoque()
                ))
                .toList();
    }

    public ProductResponseDTO findById(Long id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        return new ProductResponseDTO(
                product.getId(),
                product.getNome(),
                product.getDescricao(),
                product.getPreco(),
                product.getQuantidadeEstoque()
        );
    }

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

    public ProductResponseDTO update(Long id, ProductRequestDTO dto) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        entity.setNome(dto.getNome());
        entity.setDescricao(dto.getDescricao());
        entity.setPreco(dto.getPreco());
        entity.setQuantidadeEstoque(dto.getQuantidadeEstoque());

        Product updatedProduct = repository.save(entity);

        return new ProductResponseDTO(
                updatedProduct.getId(),
                updatedProduct.getNome(),
                updatedProduct.getDescricao(),
                updatedProduct.getPreco(),
                updatedProduct.getQuantidadeEstoque()
        );
    }

    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
```

### O que acontece no `create`

```text
ProductRequestDTO dto
        ↓
new Product()
        ↓
copia os campos do DTO para a entidade Product
        ↓
repository.save(product)
        ↓
Product salvo com ID
        ↓
new ProductResponseDTO(...)
        ↓
retorna resposta para o Controller
```

---

## 14. Controller com DTOs

Arquivo:

```text
controller/ProductControler.java
```

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

Pontos importantes:

- `@RestController`: indica que a classe expõe endpoints REST.
- `@RequestMapping("/products")`: define a rota base.
- `@GetMapping`: lista ou busca produtos.
- `@PostMapping`: cria produto.
- `@PutMapping`: atualiza produto.
- `@DeleteMapping`: deleta produto.
- `@Valid`: ativa validações do `ProductRequestDTO`.
- `@RequestBody`: transforma JSON em objeto Java.
- `@PathVariable`: pega o ID da URL.

---

## 15. Testes no Postman

### 15.1 Listar produtos

```http
GET http://localhost:8080/products
```

Esperado:

```http
200 OK
```

Se não houver produtos:

```json
[]
```

---

### 15.2 Criar produto

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

### 15.3 Buscar por ID

```http
GET http://localhost:8080/products/1
```

Esperado:

```http
200 OK
```

---

### 15.4 Atualizar produto

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

### 15.5 Deletar produto

```http
DELETE http://localhost:8080/products/1
```

Esperado:

```http
204 No Content
```

---

### 15.6 Testar erro 404

```http
GET http://localhost:8080/products/999
```

Esperado:

```http
404 Not Found
```

Exemplo de body:

```json
{
  "message": "Produto não encontrado com id: 999"
}
```

---

### 15.7 Testar validação 400

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

Exemplo de body:

```json
{
  "nome": "O nome é obrigatório",
  "preco": "O preço deve ser maior que zero",
  "quantidadeEstoque": "A quantidade em estoque não pode ser negativa"
}
```

---

## 16. Collection no Postman

Foi criada uma collection para organizar os testes.

Nome sugerido:

```text
CRUD Products
```

Variável sugerida:

| Variable | Initial value | Current value |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |

URLs usando variável:

```http
{{baseUrl}}/products
{{baseUrl}}/products/1
```

Ordem recomendada de execução:

```text
1. GET     /products
2. POST    /products
3. GET     /products/1
4. PUT     /products/1
5. DELETE  /products/1
6. GET     /products/1
```

---

## 17. Erros corrigidos durante o processo

### 17.1 `ProductService` não encontrado como bean

Erro:

```text
Parameter 0 of constructor in ProductControler required a bean of type ProductService that could not be found
```

Causa:

```text
ProductService estava sem @Service ou fora do pacote escaneado pelo Spring.
```

Correção:

```java
@Service
public class ProductService {
}
```

---

### 17.2 `Ambiguous mapping`

Erro:

```text
Ambiguous mapping. Cannot map 'productControler' method
```

Causa:

Dois endpoints estavam com o mesmo mapeamento, por exemplo:

```java
@GetMapping
public ResponseEntity<List<ProductResponseDTO>> findAll() { ... }

@GetMapping
public ResponseEntity<ProductResponseDTO> findById(Long id) { ... }
```

Correção:

O `findById` precisa receber o `id` na rota:

```java
@GetMapping("/{id}")
public ResponseEntity<ProductResponseDTO> findById(@PathVariable Long id) { ... }
```

Resumo:

```text
findAll  → GET /products
findById → GET /products/{id}
```

---

## 18. Situação atual do projeto

Até aqui, o CRUD possui:

```text
Model
Repository
Service
Controller
H2 Database
Postman Collection
ResourceNotFoundException
GlobalExceptionHandler
Validações com Bean Validation
ProductRequestDTO
ProductResponseDTO
```

A API já consegue:

- Criar produto com `POST`
- Listar produtos com `GET`
- Buscar produto por ID com `GET /{id}`
- Atualizar produto com `PUT`
- Deletar produto com `DELETE`
- Retornar `404` quando produto não existe
- Retornar `400` quando dados inválidos são enviados
- Separar entidade de banco dos dados de entrada e saída da API

---

## 19. Próximo passo recomendado

O próximo passo recomendado é criar um **mapper** para evitar repetir conversão manual no service.

Hoje existem trechos repetidos como:

```java
return new ProductResponseDTO(
        product.getId(),
        product.getNome(),
        product.getDescricao(),
        product.getPreco(),
        product.getQuantidadeEstoque()
);
```

Um mapper centralizaria isso em métodos como:

```java
Product toEntity(ProductRequestDTO dto)
ProductResponseDTO toResponseDTO(Product product)
```

Isso deixa o service mais limpo e mais fácil de manter.
