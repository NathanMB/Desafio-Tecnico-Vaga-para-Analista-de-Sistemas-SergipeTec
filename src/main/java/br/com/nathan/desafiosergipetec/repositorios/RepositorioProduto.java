package br.com.nathan.desafiosergipetec.repositorios;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import br.com.nathan.desafiosergipetec.entidades.Produto;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioProduto extends JpaRepository<Produto, Long> {

    /**
     * Listar produtos
     */
    @Query(value = "SELECT * FROM tb_produtos ORDER BY id ASC", nativeQuery = true)
    List<Produto> listarTodosProdutos();

    /**
     * Consultar produto por identificador (ID)
     */
    @Query(value = "SELECT * FROM tb_produtos WHERE id = :id", nativeQuery = true)
    Optional<Produto> buscarPorId(@Param("id") Long id);

    /**
     * Consultar produto por descricao
     */
    @Query(value = "SELECT * FROM tb_produtos WHERE descricao ILIKE CONCAT('%', :descricao, '%')", nativeQuery = true)
    List<Produto> buscarPorDescricao(@Param("descricao") String descricao);

    /**
     * Consultar produtos por descrição ou identificador
     * O CAST transforma o ID num texto para comparar com o que o usuário digitou.
     * O ILIKE e o CONCAT buscam a palavra em qualquer parte da descrição (ignorando
     * maiúsculas).
     */
    @Query(value = "SELECT * FROM tb_produtos WHERE CAST(id AS TEXT) = :identificador OR descricao ILIKE CONCAT('%', :identificador, '%')", nativeQuery = true)
    List<Produto> buscarPorDescricaoOuId(@Param("identificador") String identificador);

}