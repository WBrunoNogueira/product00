package com.exercice.product00.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public class ProductRequestDTO {

    //validações dos dados recebidos Via dependencia do Spring
    //DTO NÃO TEM ID -- O DTO serve para separar os dados da API dos dados do banco. Tira a responsabildiade do Controller
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
