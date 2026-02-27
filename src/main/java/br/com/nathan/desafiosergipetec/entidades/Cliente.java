package br.com.nathan.desafiosergipetec.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entidade que representa os clientes
 * Mapeada para a tabela 'tb_clientes'
 */
@Entity
@Table(name = "tb_clientes")
public class Cliente implements Serializable {

    // Encarrega a geração do ID para o BIGSERIAL do Postgres
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo obrigatório e com limite de 150 caracteres
    @Column(nullable = false, length = 150)
    private String nome;

    // Campo obrigatório, único e com limite de 150 caracteres
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    /**
     * A data do cliente é gerada automaticamente pelo banco de dados (DEFAULT CURRENT_TIMESTAMP).
     * insertable = false e updatable = false impedem que a aplicação tente sobrescrever esse valor.
     */
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Construtor vazio (obrigatório para o JPA conseguir instanciar a classe)
    public Cliente() {
    }

    // Construtor customizado (sem ID e dataCadastro, pois são gerados automaticamente)
    public Cliente(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // GETTERS E SETTERS
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }
}