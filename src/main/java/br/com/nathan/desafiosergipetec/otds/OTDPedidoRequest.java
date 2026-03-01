package br.com.nathan.desafiosergipetec.otds;

import java.util.List;

// OTD (Objeto de transferência de dados) responsável por receber os dados do Frontend na hora de salvar um novo Pedido.
public class OTDPedidoRequest {

    // ID do cliente que está realizando a compra.
    private Long clienteId;

    // Lista com os produtos que compõem este pedido.
    private List<OTDProdutoRequest> itens;

    // GETTERS E SETTERS
    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<OTDProdutoRequest> getItens() {
        return itens;
    }

    public void setItens(List<OTDProdutoRequest> itens) {
        this.itens = itens;
    }
}
