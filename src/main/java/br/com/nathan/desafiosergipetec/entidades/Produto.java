package br.com.nathan.desafiosergipetec.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Indica ao Spring que esta classe é uma tabela no banco
@Entity
@Table(name = "tb_produtos")
public class Produto implements Serializable {

    // Encarrega a geração do ID para o BIGSERIAL do Postgres
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo obrigatório e com limite de 200 caracteres
    @Column(nullable = false, length = 200)
    private String descricao;

    // Campo obrigatório e determinado com 10 dígitos no total, sendo 2 após a vírgula
    @Column(nullable = false, precision = 10, scale = 2) 
    private BigDecimal valor;

    // Campo obrigatório
    @Column(name = "quantidade_estoque", nullable = false) 
    private Integer quantidadeEstoque;

    // insertable=false e updatable=false deixam o DEFAULT CURRENT_TIMESTAMP do Postgres trabalhar
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Construtor vazio (obrigatório para o JPA conseguir instanciar a classe)
    public Produto() {
    }

    // Construtor cheio (sem ID e sem Data, pois o banco preenche esses dois sozinho)
    public Produto(String descricao, BigDecimal valor, Integer quantidadeEstoque) {
        this.descricao = descricao;
        this.valor = valor;
        this.quantidadeEstoque = quantidadeEstoque;
    }

    // GETTERS E SETTERS NATIVOS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getQuantidadeEstoque() {
        return quantidadeEstoque;
    }

    public void setQuantidadeEstoque(Integer quantidadeEstoque) {
        this.quantidadeEstoque = quantidadeEstoque;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}