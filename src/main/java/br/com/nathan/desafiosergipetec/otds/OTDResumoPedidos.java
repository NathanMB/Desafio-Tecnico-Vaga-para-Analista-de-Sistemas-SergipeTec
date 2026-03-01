package br.com.nathan.desafiosergipetec.otds;


import java.math.BigDecimal;
import java.util.List;

/**
 * OTD (Objeto de transferência de dados) responsável por empacotar o resultado da busca de pedidos para a tela de Resumo de Pedidos.
 * 
 * Além de agrupar a lista de pedidos, ele também agrega um valor total (faturamento) para facilitar o consumo no Frontend,
 * evitando que o mesmo precise fazer laços de repetição para somar valores em tela.
 */
public class OTDResumoPedidos {
    
    private List<OTDPedido> pedidos;
    
    //Soma de todos os pedidos da lista atual. 
    private BigDecimal faturamentoTotal;

    /**
     * Recebe a lista de pedidos do banco de dados e já 
     * calcula o somatório financeiro no momento da sua criação.
     */
    public OTDResumoPedidos(List<OTDPedido> pedidos) {
        this.pedidos = pedidos;
        
        // Iniciamos com ZERO para garantir que a API nunca devolva 'null' no faturamento
        BigDecimal soma = BigDecimal.ZERO;
        
        // Valida se é uma lista vazia ou nula.
        if (pedidos != null) {
            for (OTDPedido pedido : pedidos) {
                // Valida e ignora, para o somatorio, pedidos que não tenham valor para evitar erros de cálculo.
                if (pedido.getValorTotal() != null) {
                    // É somado o valor total de cada pedido, garantindo que o resultado seja sempre um BigDecimal válido.
                    soma = soma.add(pedido.getValorTotal());
                }
            }
        }
        
        this.faturamentoTotal = soma;
    }

    // GETTERS E SETTERS
    public List<OTDPedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<OTDPedido> pedidos) {
        this.pedidos = pedidos;
    }

    public BigDecimal getFaturamentoTotal() {
        return faturamentoTotal;
    }

    public void setFaturamentoTotal(BigDecimal faturamentoTotal) {
        this.faturamentoTotal = faturamentoTotal;
    }
}