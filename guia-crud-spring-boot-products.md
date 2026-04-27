# Guia de Estudo — CRUD com Spring Boot, H2 e Postman

Este material resume o passo a passo feito durante a criação de uma API REST simples de **CRUD de produtos** usando **Spring Boot**, **Spring Data JPA**, **H2 Database** e **Postman**.

O objetivo é servir como consulta rápida para estudar depois.

---

## 1. Objetivo do projeto

Criar uma API REST para gerenciar produtos.

A API permite:

- Listar todos os produtos
- Buscar produto por ID
- Criar produto
- Atualizar produto
- Deletar produto

Endpoints finais:

```http
GET     /products
GET     /products/{id}
POST    /products
PUT     /products/{id}
DELETE  /products/{id}
```

---

## 2. Estrutura do projeto

Estrutura sugerida dos pacotes:

```text
com.exercice.product00
 ├── controller
 │   └── ProductControler.java
 ├── service
 │   └── ProductService.java
 ├── repository
 │   └── ProductRepository.java
 ├── model
 │   └── Product.java
 └── Product00Application.java
```

Cada camada tem uma responsabilidade:

| Camada | Responsabilidade |
|---|---|
| `model` | Representa a entidade/tabela do banco |
| `repository` | Faz comunicação com o banco de dados |
| `service` | Contém as regras e operações do sistema |
| `controller` | Expõe os endpoints da API REST |

Fluxo da aplicação:

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

## 3. Dependências necessárias

No `pom.xml`, use as dependências principais:

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
```

Essas dependências fazem o seguinte:

| Dependência | Para que serve |
|---|---|
| `spring-boot-starter-web` | Criar API REST com endpoints HTTP |
| `spring-boot-starter-data-jpa` | Usar JPA/Hibernate e repositories |
| `h2` | Banco de dados em memória para testes |

---

## 4. Configuração do H2 no `application.yml`

Arquivo:

```text
src/main/resources/application.yml
```

Configuração:

```yaml
spring:
  application:
    name: product00

  datasource:
    url: jdbc:h2:mem:productsdb
    driver-class-name: org.h2.Driver
    username: sa
    password:

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

### Como acessar o H2

Depois de subir a aplicação, acesse:

```text
http://localhost:8080/h2-console
```

Dados de conexão:

```text
JDBC URL: jdbc:h2:mem:productsdb
User Name: sa
Password: deixe vazio
```

Para consultar a tabela:

```sql
SELECT * FROM PRODUCT;
```

ou, dependendo do nome definido com `@Table`:

```sql
SELECT * FROM PRODUCTS;
```

---

## 5. Criando o Model `Product`

O `model` representa a tabela do banco.

Exemplo:

```java
package com.exercice.product00.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
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

### Explicação das anotações

```java
@Entity
```

Diz que essa classe representa uma tabela no banco.

```java
@Id
```

Diz que o campo é a chave primária da tabela.

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Diz que o ID será gerado automaticamente pelo banco.

Resumo:

```text
Product.java = entidade que vira tabela no banco
```

---

## 6. Criando o Repository

O repository é responsável por conversar com o banco de dados.

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

Ao estender `JpaRepository<Product, Long>`, o Spring já fornece métodos prontos:

```java
findAll();
findById(id);
save(product);
delete(product);
existsById(id);
```

Resumo:

```text
Repository = camada que acessa o banco
```

---

## 7. Criando o Service

O service contém as operações do CRUD.

Arquivo:

```text
service/ProductService.java
```

Código completo:

```java
package com.exercice.product00.service;

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

    public List<Product> findAll() {
        return repository.findAll();
    }

    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
    }

    public Product create(Product product) {
        return repository.save(product);
    }

    public Product update(Long id, Product product) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        entity.setNome(product.getNome());
        entity.setDescricao(product.getDescricao());
        entity.setPreco(product.getPreco());
        entity.setQuantidadeEstoque(product.getQuantidadeEstoque());

        return repository.save(entity);
    }

    public void delete(Long id) {
        Product entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

        repository.delete(entity);
    }
}
```

---

## 8. Explicando os métodos do Service

### `findAll()`

```java
public List<Product> findAll() {
    return repository.findAll();
}
```

Busca todos os produtos cadastrados no banco.

Resumo:

```text
findAll = listar todos os produtos
```

---

### `findById(Long id)`

```java
public Product findById(Long id) {
    return repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));
}
```

Busca um produto pelo ID.

Se encontrar, retorna o produto.

Se não encontrar, lança erro.

Resumo:

```text
findById = buscar um produto específico pelo ID
```

---

### `create(Product product)`

```java
public Product create(Product product) {
    return repository.save(product);
}
```

Salva um novo produto no banco.

Como o produto ainda não tem ID, o banco gera o ID automaticamente.

Resumo:

```text
create = salvar novo produto
```

---

### `update(Long id, Product product)`

```java
public Product update(Long id, Product product) {
    Product entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

    entity.setNome(product.getNome());
    entity.setDescricao(product.getDescricao());
    entity.setPreco(product.getPreco());
    entity.setQuantidadeEstoque(product.getQuantidadeEstoque());

    return repository.save(entity);
}
```

Esse método atualiza um produto existente.

Fluxo:

```text
recebe o ID
busca o produto no banco
se não encontrar, lança erro
se encontrar, altera os campos
salva novamente
retorna o produto atualizado
```

Diferença entre `entity` e `product`:

| Variável | Significado |
|---|---|
| `entity` | Produto antigo, encontrado no banco |
| `product` | Produto novo, vindo da requisição |

Resumo:

```text
update = atualizar um produto já existente
```

---

### `delete(Long id)`

```java
public void delete(Long id) {
    Product entity = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado com id: " + id));

    repository.delete(entity);
}
```

Esse método deleta um produto pelo ID.

Fluxo:

```text
recebe o ID
busca o produto no banco
se não encontrar, lança erro
se encontrar, deleta
```

Resumo:

```text
delete = remover produto do banco
```

---

## 9. Criando o Controller

O controller expõe os endpoints da API.

Arquivo:

```text
controller/ProductControler.java
```

Código completo:

```java
package com.exercice.product00.controller;

import com.exercice.product00.model.Product;
import com.exercice.product00.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductControler {

    private final ProductService service;

    public ProductControler(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Product>> findAll() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> findById(@PathVariable Long id) {
        Product product = service.findById(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody Product product) {
        Product createdProduct = service.create(product);
        return ResponseEntity.status(201).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(
            @PathVariable Long id,
            @RequestBody Product product
    ) {
        Product updatedProduct = service.update(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 10. Explicando as anotações do Controller

### `@RestController`

```java
@RestController
```

Diz que essa classe é um controller REST.

Ela recebe requisições HTTP e devolve respostas em JSON.

---

### `@RequestMapping("/products")`

```java
@RequestMapping("/products")
```

Define a rota base da classe.

Todos os endpoints começam com:

```http
/products
```

---

### `@GetMapping`

```java
@GetMapping
```

Cria um endpoint GET.

Quando usado sem valor, representa:

```http
GET /products
```

---

### `@GetMapping("/{id}")`

```java
@GetMapping("/{id}")
```

Cria um endpoint GET que recebe um ID pela URL.

Exemplo:

```http
GET /products/1
```

---

### `@PathVariable`

```java
@PathVariable Long id
```

Pega o valor da URL e coloca na variável `id`.

Exemplo:

```http
/products/1
```

Resultado:

```text
id = 1
```

---

### `@RequestBody`

```java
@RequestBody Product product
```

Pega o JSON enviado no corpo da requisição e transforma em um objeto Java.

Exemplo de JSON:

```json
{
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

O Spring transforma esse JSON em um objeto `Product`.

---

### `ResponseEntity`

`ResponseEntity` permite controlar a resposta HTTP.

Exemplos:

```java
ResponseEntity.ok(product);
```

Retorna:

```http
200 OK
```

com o produto no corpo.

```java
ResponseEntity.status(201).body(product);
```

Retorna:

```http
201 Created
```

com o produto criado no corpo.

```java
ResponseEntity.noContent().build();
```

Retorna:

```http
204 No Content
```

sem corpo na resposta.

---

## 11. Explicando cada endpoint

### 11.1 Listar produtos

```java
@GetMapping
public ResponseEntity<List<Product>> findAll() {
    List<Product> products = service.findAll();
    return ResponseEntity.ok(products);
}
```

Endpoint:

```http
GET /products
```

Serve para listar todos os produtos.

Resposta esperada:

```http
200 OK
```

Exemplo de body:

```json
[
  {
    "id": 1,
    "nome": "Mouse Gamer",
    "descricao": "Mouse RGB",
    "preco": 150.00,
    "quantidadeEstoque": 10
  }
]
```

---

### 11.2 Buscar produto por ID

```java
@GetMapping("/{id}")
public ResponseEntity<Product> findById(@PathVariable Long id) {
    Product product = service.findById(id);
    return ResponseEntity.ok(product);
}
```

Endpoint:

```http
GET /products/{id}
```

Exemplo:

```http
GET /products/1
```

Serve para buscar um produto específico.

Importante:

```java
@GetMapping("/{id}")
```

Esse `/{id}` diferencia o endpoint do `findAll`.

Se os dois estivessem apenas com `@GetMapping`, daria erro de rota duplicada, chamado **Ambiguous mapping**.

---

### 11.3 Criar produto

```java
@PostMapping
public ResponseEntity<Product> create(@RequestBody Product product) {
    Product createdProduct = service.create(product);
    return ResponseEntity.status(201).body(createdProduct);
}
```

Endpoint:

```http
POST /products
```

Serve para criar um novo produto.

Exemplo de body:

```json
{
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

Fluxo:

```text
Controller recebe o JSON
@RequestBody transforma JSON em Product
Service salva no banco
Repository executa o INSERT
Banco gera o ID
API retorna 201 Created
```

---

### 11.4 Atualizar produto

```java
@PutMapping("/{id}")
public ResponseEntity<Product> update(
        @PathVariable Long id,
        @RequestBody Product product
) {
    Product updatedProduct = service.update(id, product);
    return ResponseEntity.ok(updatedProduct);
}
```

Endpoint:

```http
PUT /products/{id}
```

Exemplo:

```http
PUT /products/1
```

Serve para atualizar um produto existente.

Exemplo de body:

```json
{
  "nome": "Mouse Gamer Atualizado",
  "descricao": "Mouse sem fio RGB",
  "preco": 199.90,
  "quantidadeEstoque": 8
}
```

Fluxo:

```text
pega o ID da URL
pega os novos dados no body
chama service.update(id, product)
busca o produto no banco
altera os campos
salva novamente
retorna 200 OK
```

---

### 11.5 Deletar produto

```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
}
```

Endpoint:

```http
DELETE /products/{id}
```

Exemplo:

```http
DELETE /products/1
```

Serve para remover um produto.

Retorno esperado:

```http
204 No Content
```

Significa:

```text
A operação deu certo, mas não existe corpo na resposta.
```

---

## 12. Testando no Postman

### 12.1 Criar uma Collection

1. Abra o Postman
2. Clique em **Collections**
3. Clique no botão **+**
4. Escolha **Blank collection**
5. Nomeie como:

```text
CRUD Products
```

6. Clique em **Create**

---

### 12.2 Criar variável `baseUrl`

Dentro da collection:

1. Clique na collection **CRUD Products**
2. Vá na aba **Variables**
3. Crie a variável:

| Variable | Initial value | Current value |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | `http://localhost:8080` |

4. Clique em **Save**

Assim, nas requisições, use:

```text
{{baseUrl}}
```

em vez de repetir:

```text
http://localhost:8080
```

---

## 13. Requisições no Postman

### 13.1 Listar produtos

Nome da request:

```text
Listar produtos
```

Método:

```http
GET
```

URL:

```http
{{baseUrl}}/products
```

Resultado esperado inicial:

```json
[]
```

Status:

```http
200 OK
```

---

### 13.2 Criar produto

Nome da request:

```text
Criar produto
```

Método:

```http
POST
```

URL:

```http
{{baseUrl}}/products
```

Body:

1. Vá na aba **Body**
2. Selecione **raw**
3. Escolha **JSON**
4. Cole:

```json
{
  "nome": "Mouse Gamer",
  "descricao": "Mouse RGB",
  "preco": 150.00,
  "quantidadeEstoque": 10
}
```

Resultado esperado:

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

### 13.3 Buscar produto por ID

Nome da request:

```text
Buscar produto por ID
```

Método:

```http
GET
```

URL:

```http
{{baseUrl}}/products/1
```

Resultado esperado:

```http
200 OK
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

### 13.4 Atualizar produto

Nome da request:

```text
Atualizar produto
```

Método:

```http
PUT
```

URL:

```http
{{baseUrl}}/products/1
```

Body:

```json
{
  "nome": "Mouse Gamer Atualizado",
  "descricao": "Mouse sem fio RGB",
  "preco": 199.90,
  "quantidadeEstoque": 8
}
```

Resultado esperado:

```http
200 OK
```

Resposta:

```json
{
  "id": 1,
  "nome": "Mouse Gamer Atualizado",
  "descricao": "Mouse sem fio RGB",
  "preco": 199.90,
  "quantidadeEstoque": 8
}
```

---

### 13.5 Deletar produto

Nome da request:

```text
Deletar produto
```

Método:

```http
DELETE
```

URL:

```http
{{baseUrl}}/products/1
```

Resultado esperado:

```http
204 No Content
```

Normalmente não retorna body.

---

### 13.6 Confirmar se deletou

Método:

```http
GET
```

URL:

```http
{{baseUrl}}/products/1
```

Como o produto foi deletado, a aplicação deve retornar erro.

No estado atual, usando `RuntimeException`, pode retornar:

```http
500 Internal Server Error
```

Depois, o ideal é melhorar isso para retornar:

```http
404 Not Found
```

---

## 14. Ordem correta dos testes

Execute no Postman nesta ordem:

```text
1. GET     /products
2. POST    /products
3. GET     /products/1
4. PUT     /products/1
5. DELETE  /products/1
6. GET     /products/1
```

Observação:

Se o `POST /products` não funcionar, o `GET /products/1`, o `PUT /products/1` e o `DELETE /products/1` também não vão funcionar, porque o produto ainda não existe.

---

## 15. Erros encontrados durante o desenvolvimento

### 15.1 Erro: `ProductService` não encontrado

Erro parecido com:

```text
Parameter 0 of constructor in ProductControler required a bean of type ProductService that could not be found.
```

Causa provável:

```text
A classe ProductService estava sem @Service.
```

Correção:

```java
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    ...
}
```

O `@Service` faz o Spring registrar essa classe como um bean gerenciado.

---

### 15.2 Erro: `Ambiguous mapping`

Erro parecido com:

```text
Ambiguous mapping. Cannot map 'productControler' method
```

Causa provável:

Dois endpoints tinham a mesma rota.

Exemplo errado:

```java
@GetMapping
public ResponseEntity<List<Product>> findAll() { ... }

@GetMapping
public ResponseEntity<Product> findById(@PathVariable Long id) { ... }
```

Os dois ficaram como:

```http
GET /products
```

Correção:

```java
@GetMapping
public ResponseEntity<List<Product>> findAll() { ... }

@GetMapping("/{id}")
public ResponseEntity<Product> findById(@PathVariable Long id) { ... }
```

Assim as rotas ficam diferentes:

```http
GET /products
GET /products/1
```

---

## 16. Conceitos importantes aprendidos

### REST

REST é um padrão para criar APIs usando URLs e métodos HTTP.

Exemplo:

```http
GET     /products      -> listar
GET     /products/1    -> buscar por ID
POST    /products      -> criar
PUT     /products/1    -> atualizar
DELETE  /products/1    -> deletar
```

No REST, a URL representa o recurso e o verbo HTTP representa a ação.

---

### Controller não deve chamar Repository diretamente

O ideal é seguir o fluxo:

```text
Controller → Service → Repository → Banco
```

Exemplo correto:

```java
Product createdProduct = service.create(product);
```

Dentro do service:

```java
public Product create(Product product) {
    return repository.save(product);
}
```

O controller cuida da requisição HTTP.

O service cuida da regra.

O repository cuida do banco.

---

### Por que o ID é gerado automaticamente?

Por causa dessas anotações no model:

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
```

O banco gera o ID automaticamente quando um novo produto é salvo.

---

### Diferença entre `POST` e `PUT`

| Método | Uso |
|---|---|
| `POST` | Criar novo recurso |
| `PUT` | Atualizar recurso existente |

Exemplo:

```http
POST /products
```

Cria um produto novo.

```http
PUT /products/1
```

Atualiza o produto de ID `1`.

---

### Diferença entre `PUT` e `DELETE`

| Método | Uso |
|---|---|
| `PUT` | Atualiza dados |
| `DELETE` | Remove dados |

Exemplo:

```http
PUT /products/1
```

Atualiza o produto.

```http
DELETE /products/1
```

Deleta o produto.

---

## 17. Próximo passo recomendado

Depois do CRUD básico funcionando, o próximo passo é melhorar o tratamento de erro.

Hoje, quando um produto não existe, o código lança:

```java
throw new RuntimeException("Produto não encontrado com id: " + id);
```

Isso pode retornar:

```http
500 Internal Server Error
```

O ideal é criar uma exceção personalizada:

```text
ResourceNotFoundException
```

E fazer a API retornar corretamente:

```http
404 Not Found
```

Esse será o próximo avanço do projeto.

---

## 18. Resumo final

Você criou um CRUD completo com Spring Boot.

O que foi feito:

```text
1. Criou o model Product
2. Configurou o banco H2
3. Criou o ProductRepository
4. Criou o ProductService
5. Criou os métodos findAll, findById, create, update e delete
6. Criou o ProductControler
7. Criou os endpoints REST
8. Testou a API no Postman
9. Corrigiu erros de bean e rotas duplicadas
```

CRUD final:

```http
GET     /products
GET     /products/{id}
POST    /products
PUT     /products/{id}
DELETE  /products/{id}
```

Esse projeto é uma boa base para estudar os fundamentos de API REST com Spring Boot.
