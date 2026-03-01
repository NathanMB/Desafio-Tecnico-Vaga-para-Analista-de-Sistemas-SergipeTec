package br.com.nathan.desafiosergipetec.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.nathan.desafiosergipetec.entidades.Pedido;
import br.com.nathan.desafiosergipetec.otds.OTDPedido;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório responsável pelas operações de persistência e consultas da entidade Pedido.
 * Atua como uma ponte entre a regra de negócio e o banco de dados relacional.
 */
@Repository
public interface RepositorioPedido extends JpaRepository<Pedido, Long> {

    /*
     * Detalhes de otimização desta consulta:
     * 1. Projeção com DTO (Constructor Expression): Retorna apenas os dados necessários direto na consulta. 
     * Isso poupa memória e evita o clássico problema de N+1 consultas do Hibernate.
     * 
     * 2. Processamento no Banco (SUM): O cálculo do valor total com descontos é feito no próprio PostgreSQL,
     * o que é muito mais rápido e eficiente do que calcular no Java.
     * 
     * 3. Prevenção de Nulos (COALESCE): Impede que o cálculo quebre caso um item não tenha desconto, 
     * tratando o valor vazio automaticamente como 0%.
     * 
     * 4. Filtros Dinâmicos (IS NULL OR...): Uma única query atende a diversas combinações de busca, 
     * ignorando de forma inteligente os filtros que o usuário deixar em branco.
     */
    @Query("SELECT new br.com.nathan.desafiosergipetec.otds.OTDPedido(" +
            "p.id, p.cliente.nome, p.dataPedido, SUM(item.quantidadeItens * item.valor * (100 - COALESCE(item.desconto, 0)) / 100.0)) "
            +
            "FROM Pedido p LEFT JOIN p.itens item " +
            "WHERE (:id IS NULL OR p.id = :id) " +
            "AND (:clienteId IS NULL OR p.cliente.id = :clienteId) " +
            "AND (:nomeCliente IS NULL OR LOWER(p.cliente.nome) LIKE LOWER(CONCAT('%', CAST(:nomeCliente AS string), '%'))) "
            +
            "AND (:produtoId IS NULL OR item.produto.id = :produtoId) " +
            "AND (:descricaoProduto IS NULL OR LOWER(item.produto.descricao) LIKE LOWER(CONCAT('%', CAST(:descricaoProduto AS string), '%'))) "
            +
            "AND (CAST(:dataInicio AS timestamp) IS NULL OR p.dataPedido >= :dataInicio) " +
            "AND (CAST(:dataFim AS timestamp) IS NULL OR p.dataPedido <= :dataFim) " +
            "GROUP BY p.id, p.cliente.nome, p.dataPedido")
    List<OTDPedido> buscarComFiltros(
            @Param("id") Long id,
            @Param("clienteId") Long clienteId,
            @Param("nomeCliente") String nomeCliente,
            @Param("produtoId") Long produtoId,
            @Param("descricaoProduto") String descricaoProduto,
            @Param("dataInicio") LocalDateTime dataInicio,
            @Param("dataFim") LocalDateTime dataFim);
}