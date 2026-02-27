package br.com.nathan.desafiosergipetec.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.nathan.desafiosergipetec.entidades.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Repositório responsável pelas operações de persistência e consultas da entidade Cliente.
 * Atua como uma ponte entre a regra de negócio e o banco de dados relacional.
 */
@Repository
public interface RepositorioCliente extends JpaRepository<Cliente, Long> {

    /**
     * Listar clientes
     */
    @Query(value = "SELECT * FROM tb_clientes ORDER BY id ASC", nativeQuery = true)
    List<Cliente> listarTodosClientes();

    /**
     * Consultar cliente por identificador (ID)
     */
    @Query(value = "SELECT * FROM tb_clientes WHERE id = :id", nativeQuery = true)
    Optional<Cliente> buscarPorId(@Param("id") Long id);

    /**
     * Consultar cliente por Nome 
     */
    @Query(value = "SELECT * FROM tb_clientes WHERE nome ILIKE CONCAT('%', :nome, '%')", nativeQuery = true)
    List<Cliente> buscarPorNome(@Param("nome") String nome);

    /**
     * Consultar clientes por nome ou identificador
     * O CAST transforma o ID num texto para comparar com o que o usuário digitou.
     * O ILIKE e o CONCAT buscam a palavra em qualquer parte do nome (ignorando maiúsculas).
     */
    @Query(value = "SELECT * FROM tb_clientes WHERE CAST(id AS TEXT) = :identificador OR nome ILIKE CONCAT('%', :identificador, '%')", nativeQuery = true)
    List<Cliente> buscarPorNomeOuId(@Param("identificador") String identificador);
}