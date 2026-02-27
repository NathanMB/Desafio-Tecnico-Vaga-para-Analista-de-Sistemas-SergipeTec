package br.com.nathan.desafiosergipetec.entidades;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity // Indica ao Spring/Hibernate que esta classe representa uma tabela
@Table(name = "tb_clientes") // Sinaliza que a classe está associada ao nome da tabela
public class Cliente implements Serializable {

    // Delega a geração do ID para o BIGSERIAL do Postgres
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campo obrigatório e com limite de 150 caracteres
    @Column(nullable = false, length = 150)
    private String nome;

    // Campo obrigatório, único e com limite de 150 caracteres
    @Column(nullable = false, length = 150, unique = true)
    private String email;

    // insertable=false e updatable=false dizem ao Java para deixar o DEFAULT CURRENT_TIMESTAMP do Postgres agir
    @Column(name = "data_cadastro", insertable = false, updatable = false)
    private LocalDateTime dataCadastro;

    // Construtor vazio (Exigência arquitetural do JPA para conseguir instanciar a classe)
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