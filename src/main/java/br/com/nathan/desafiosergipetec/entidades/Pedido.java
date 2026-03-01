package br.com.nathan.desafiosergipetec.entidades;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa os pedido
 * Mapeada para a tabela 'tb_pedidos'
 */
@Entity
@Table(name = "tb_pedidos")
public class Pedido implements Serializable {

    // ATRIBUTOS
    // Encarrega a geração do ID para o BIGSERIAL do Postgres
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relacionamento N:1 com Cliente.
     * Um pedido sempre pertence a um único cliente. O 'nullable = false' 
     */
    @ManyToOne
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    /**
     * A data do pedido é gerada automaticamente pelo banco de dados (DEFAULT CURRENT_TIMESTAMP).
     * insertable = false e updatable = false impedem que a aplicação tente sobrescrever esse valor.
     */
    @Column(name = "data_pedido", insertable = false, updatable = false)
    private LocalDateTime dataPedido;

    /**
     * Relacionamento 1:N com ItemPedido.
     * * @JsonManagedReference: Resolve o problema de referência circular (StackOverflow) 
     * ao serializar o JSON, assumindo o papel de "classe pai".
     * * cascade = CascadeType.ALL: Garante que ao salvar um Pedido, todos os seus itens
     * sejam salvos em cascata na tabela tb_itens_pedido.
     * * orphanRemoval = true: Se um item for removido desta lista, o Hibernate
     * exclui o registro correspondente automaticamente do banco de dados.
     */
    @JsonManagedReference 
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();
    
    // Construtor vazio (obrigatório para o JPA conseguir instanciar a classe)
    public Pedido() {
    }

    // Construtor cheio (sem ID, pois o banco preenche esses dois sozinho)
    public Pedido(Cliente cliente) {
        this.cliente = cliente;
    }

    // MÉTODOS DE REGRA DE NEGÓCIO
    /**
     * Método auxiliar para adicionar itens ao pedido.
     * Ele amarra os dois lados do relacionamento na memória (o Pedido recebe o Item, 
     * e o Item recebe o Pedido) para que o Hibernate salve tudo corretamente no banco.
     */
    public void adicionarItem(ItemPedido item) {
        itens.add(item);
        item.setPedido(this);
    }

    // GETTERS E SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public List<ItemPedido> getItens() { return itens; }
    public void setItens(List<ItemPedido> itens) { this.itens = itens; }
}