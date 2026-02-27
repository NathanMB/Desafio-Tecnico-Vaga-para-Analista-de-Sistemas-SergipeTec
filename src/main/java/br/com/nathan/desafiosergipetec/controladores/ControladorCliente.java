package br.com.nathan.desafiosergipetec.controladores;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import br.com.nathan.desafiosergipetec.entidades.Cliente;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioCliente;

import java.util.List;

@RestController // Define que é uma API REST (retorna JSON)
@RequestMapping("/api/clientes") // Prefixo da URL
@CrossOrigin("*") // Permite que o Frontend acesse sem bloqueio (CORS)
public class ControladorCliente {

    private final RepositorioCliente repository;

    public ControladorCliente(RepositorioCliente repository) {
        this.repository = repository;
    }

    // POST: Cadastrar cliente
    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@RequestBody Cliente cliente) {
        Cliente novoCliente = repository.save(cliente);

        // Retorna 201 Created com o cliente criado no corpo da resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(novoCliente);
    }

    // GET: Listar clientes
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = repository.listarTodosClientes();
         
        // Retorna 200 OK com a lista de clientes
        return ResponseEntity.ok(clientes);
    }

    // GET: Consultar cliente por Nome ou ID (identificador)
    @GetMapping("/{identificador}")
    public ResponseEntity<List<Cliente>> consultaCliente(@PathVariable String identificador) {
        List<Cliente> encontrados = repository.buscarPorNomeOuId(identificador);

        if (encontrados.isEmpty()) {
            // Retorna 404 se não achar nada
            return ResponseEntity.notFound().build();
        }

        // Retorna 200 com a lista de resultados
        return ResponseEntity.ok(encontrados); 
    }
}