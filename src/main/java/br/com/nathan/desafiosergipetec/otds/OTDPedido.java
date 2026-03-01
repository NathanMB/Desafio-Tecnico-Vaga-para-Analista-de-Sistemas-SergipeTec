package br.com.nathan.desafiosergipetec.otds;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * OTD (Objeto de transferência de dados) usado para montar a tela de Menu dos pedidos.
 * 
 * Carrega apenas as informações que a tabela precisa mostrar (ID, Cliente, Data e Total).
 * Evitando carregar a entidade completa, deixando o sistema mais rápido e leve.
 */
public class OTDPedido {

    private Long id;
    private String nomeCliente;
    private LocalDateTime dataPedido;
    private BigDecimal valorTotal;

    /**
     * Construtor usado pelo repositório para montar o DTO direto da consulta SQL.
     * O truque desta implementação:
     * - Tipo Genérico ('Number'): Evita erros de conversão (ClassCastException) que acontecem 
     * quando o SUM() do banco retorna tipos variados.
     * - Pega esse número genérico e o transforma no BigDecimal oficial 
     * e como complemento aplica as 2 casas decimais e o arredondamento (HALF_UP) na raiz.
     */
    public OTDPedido(Long id, String nomeCliente, LocalDateTime dataPedido, Number valorTotal) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.dataPedido = dataPedido;
        
        // Proteção contra NullPointerException (caso o pedido não tenha itens) e formatação financeira
        if (valorTotal != null) {
            this.valorTotal = new BigDecimal(valorTotal.toString()).setScale(2, RoundingMode.HALF_UP);
        } else {
            this.valorTotal = BigDecimal.ZERO;
        }
    }

    // GETTERS E SETTERS NATIVOS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public LocalDateTime getDataPedido() { return dataPedido; }
    public void setDataPedido(LocalDateTime dataPedido) { this.dataPedido = dataPedido; }

    public BigDecimal getValorTotal() { return valorTotal; }
    public void setValorTotal(BigDecimal valorTotal) { this.valorTotal = valorTotal; }
}
