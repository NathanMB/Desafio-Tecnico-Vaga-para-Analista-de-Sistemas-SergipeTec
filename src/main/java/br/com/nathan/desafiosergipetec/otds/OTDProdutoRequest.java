package br.com.nathan.desafiosergipetec.otds;

// OTD (Objeto de transferência de dados) que representa cada item dentro do Pedido recebido do Frontend.
public class OTDProdutoRequest {

    // ID do produto que o cliente deseja comprar.
    private Long produtoId;

    // Quantidade solicitada pelo cliente no momento da compra.
    private Integer quantidade;

    /**
     * Desconto percentual aplicado ao item específico.
     * Caso o Frontend não envie este dado, o Controlador assumirá 0% automaticamente.
     */
    private Integer desconto;

    // GETTERS E SETTERS
    public Long getProdutoId() { 
        return produtoId; 
    }
    
    public void setProdutoId(Long produtoId) { 
        this.produtoId = produtoId; 
    }

    public Integer getQuantidade() { 
        return quantidade; 
    }
    
    public void setQuantidade(Integer quantidade) { 
        this.quantidade = quantidade; 
    }

    public Integer getDesconto() { 
        return desconto; 
    }
    
    public void setDesconto(Integer desconto) { 
        this.desconto = desconto; 
    }
}
