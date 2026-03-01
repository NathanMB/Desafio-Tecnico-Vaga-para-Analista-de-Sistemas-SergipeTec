package br.com.nathan.desafiosergipetec.entidades;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidade que representa os itens de um pedido
 * Mapeada para a tabela 'tb_itens_pedido'
 */
@Entity
@Table(name = "tb_itens_pedido")
public class ItemPedido implements Serializable {

    // ATRIBUTOS
    // Encarrega a geração do ID para o BIGSERIAL do Postgres
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento N:1 com Produto.
     * Vários itens de pedido podem referenciar o mesmo produto do catálogo.
     */
    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

   /**
     * Relacionamento N:1 com Pedido.
     * A anotação @JsonBackReference evita o erro de "loop infinito" na API.
     * Ela avisa o conversor JSON para não carregar os dados do Pedido novamente ao 
     * ler este Item, quebrando o ciclo de repetição (StackOverflow).
     */
    @JsonBackReference 
    @ManyToOne
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    /**
     * Valor unitário do produto NO MOMENTO DA COMPRA.
     * precision = 10, scale = 2: Permite números com até 10 dígitos no total, sendo 2 após a vírgula.
     * Campo obrigatório
     */
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal valor;

    // Campo obrigatório
    @Column(name = "quantidade_itens", nullable = false)
    private Integer quantidadeItens;

    // Desconto percentual aplicado a este item específico.
    @Column(name = "desconto_percentual")
    private Integer descontoPercentual;

    // Construtor vazio (obrigatório para o JPA conseguir instanciar a classe)
    public ItemPedido() {
    }

    // Construtor cheio (sem ID, pois o banco preenche esse campo automaticamente)
    public ItemPedido(Produto produto, Pedido pedido, BigDecimal valor, Integer quantidadeItens, Integer descontoPercentual) {
        this.produto = produto;
        this.pedido = pedido;
        this.valor = valor;
        this.quantidadeItens = quantidadeItens;
        this.descontoPercentual = descontoPercentual;
    }

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public Integer getQuantidadeItens() { return quantidadeItens; }
    public void setQuantidadeItens(Integer quantidadeItens) { this.quantidadeItens = quantidadeItens; }

    public Integer getDescontoPercentual() { return descontoPercentual; }
    public void setDescontoPercentual(Integer descontoPercentual) { this.descontoPercentual = descontoPercentual; }
}
