package br.com.nathan.desafiosergipetec.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.nathan.desafiosergipetec.entidades.Produto;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioProduto;

import java.util.List;

/**
 * Controlador REST responsável pelo gerenciamento dos produtos. 
 * Atua como a camada de gerenciamento, recebendo as requisições do frontend, 
 * validando regras de negócio e devolvendo os resultados.
 */
@RestController // Define que é uma API REST (retorna JSON)
@RequestMapping("/api/produtos") // Prefixo da URL
@CrossOrigin("*") // Permite que o Frontend acesse sem bloqueio (CORS)
public class ControladorProduto {

    private final RepositorioProduto repository;

    public ControladorProduto(RepositorioProduto repository) {
        this.repository = repository;
    }

    // POST: Cadastrar produto
    @PostMapping
    public ResponseEntity<Produto> cadastrar(@RequestBody Produto produto) {
        Produto novoProduto = repository.save(produto);

        // Retorna 201 Created com o produto criado no corpo da resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto); 
    }

    // GET: Listar todos
    @GetMapping
    public ResponseEntity<List<Produto>> listarTodos() {
        List<Produto> produtos = repository.listarTodosProdutos();

        // Retorna 200 OK com a lista de produtos
        return ResponseEntity.ok(produtos);
    }

    // GET: Consultar produtos por identificador (ID ou Descrição)
    @GetMapping("/{identificador}")
    public ResponseEntity<List<Produto>> consultaProduto(@PathVariable String identificador) {
        List<Produto> encontrados = repository.buscarPorDescricaoOuId(identificador);

        if (encontrados.isEmpty()) {
            // Retorna 404 se não achar nada
            return ResponseEntity.notFound().build();
        }

        // Retorna 200 com a lista de resultados
        return ResponseEntity.ok(encontrados);
    }
}