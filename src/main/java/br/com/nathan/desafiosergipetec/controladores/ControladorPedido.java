package br.com.nathan.desafiosergipetec.controladores;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import br.com.nathan.desafiosergipetec.entidades.Cliente;
import br.com.nathan.desafiosergipetec.entidades.ItemPedido;
import br.com.nathan.desafiosergipetec.entidades.Pedido;
import br.com.nathan.desafiosergipetec.entidades.Produto;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioCliente;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioPedido;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioProduto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controlador REST responsável pelo gerenciamento dos pedidos.
 * Atua como a camada de gerenciamento, recebendo as requisições do frontend,
 * validando regras de negócio (como estoque) e devolvendo os resultados.
 */
@RestController // Define que é uma API REST (retorna JSON)
@RequestMapping("/api/pedidos") // Prefixo da URL
@CrossOrigin("*") // Permite que o Frontend acesse sem bloqueio (CORS)
public class ControladorPedido {

    private final RepositorioPedido repositorioPedido;
    private final RepositorioCliente repositorioCliente;
    private final RepositorioProduto repositorioProduto;

    public ControladorPedido(RepositorioPedido repositorioPedido,
            RepositorioCliente repositorioCliente,
            RepositorioProduto repositorioProduto) {
        this.repositorioPedido = repositorioPedido;
        this.repositorioCliente = repositorioCliente;
        this.repositorioProduto = repositorioProduto;
    }

    /**
     * POST: Cadastrar pedido
     * 
     * @Transactional: Garante que se o estoque de qualquer produto falhar no meio
     *                 do processo, o Spring cancela
     *                 a transação inteira (rollback automático), evitando salvar um
     *                 pedido pela metade.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Pedido> salvar(@RequestBody PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();

        // Busca o cliente
        Cliente cliente = repositorioCliente.findById(dto.getClienteId());

        // Verifica se o cliente foi encontrado
        if (cliente != null) {
            pedido.setCliente(cliente);
        } else {
            throw new RuntimeException("Cliente não encontrado");
        }

        for (ProdutoRequestDTO itemDto : dto.getItens()) {
            // Busca o produto
            Produto produto = repositorioProduto.findById(itemDto.getProdutoId());

            // Verifica se o produto foi encontrado
            if (produto == null) {
                throw new RuntimeException("Produto não encontrado: ID " + itemDto.getProdutoId());
            }

            // Validação e Baixa de Estoque
            if (produto.getQuantidadeEstoque() < itemDto.getQuantidade()) {
                throw new RuntimeException("Estoque insuficiente para o produto: " + produto.getDescricao());
            }
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - itemDto.getQuantidade());

            // Cria o ItemPedido e preenche os dados
            ItemPedido item = new ItemPedido();
            item.setProduto(produto);
            item.setQuantidadeItens(itemDto.getQuantidade());
            item.setDesconto(itemDto.getDesconto() != null ? itemDto.getDesconto() : 0);
            item.setValor(produto.getValor());

            // Adiciona o item ao pedido
            pedido.adicionarItem(item);
        }

        // Salva o pedido e retorna 201 Created com o pedido criado no corpo da resposta
        Pedido pedidoSalvo = repositorioPedido.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoSalvo);
    }

    /**
     * Endpoint de busca avançada para alimentar o Dashboard/Relatório.
     * Utiliza uma lógica de "Smart Parsing" (try-catch) para descobrir se o termo
     * digitado pelo usuário na barra de pesquisa é um ID (número) ou um
     * Nome/Descrição (texto).
     */
    @GetMapping("/buscar")
    public ResponseEntity<ResumoGeralDTO> buscarPedidos(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "cliente", required = false) String clienteIdentificador,
            @RequestParam(name = "produto", required = false) String produtoIdentificador,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim) {

        Long idCliente = null;
        String nomeCliente = null;
        Long idProduto = null;
        String descricaoProduto = null;

        // Smart Parsing para Cliente
        if (clienteIdentificador != null && !clienteIdentificador.isBlank()) {
            try {
                idCliente = Long.parseLong(clienteIdentificador);
            } catch (NumberFormatException e) {
                nomeCliente = clienteIdentificador;
            }
        }

        // Smart Parsing para Produto
        if (produtoIdentificador != null && !produtoIdentificador.isBlank()) {
            try {
                idProduto = Long.parseLong(produtoIdentificador);
            } catch (NumberFormatException e) {
                descricaoProduto = produtoIdentificador;
            }
        }

        // Normalização de Datas: Cobre do primeiro segundo do dia de início ao último
        // segundo do dia de fim.
        LocalDateTime inicio = (dataInicio != null) ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = (dataFim != null) ? dataFim.atTime(23, 59, 59) : null;

        List<PedidoDTO> pedidosEncontrados = repositorioPedido.buscarComFiltros(
                id, idCliente, nomeCliente, idProduto, descricaoProduto, inicio, fim);

        return ResponseEntity.ok(new ResumoGeralDTO(pedidosEncontrados));
    }

    // GET: Consultar todos os pedidos (sem filtros)
    @GetMapping("/todos")
    public ResponseEntity<ResumoGeralDTO> listarTodos() {
        List<PedidoDTO> todosOsPedidos = repositorioPedido.buscarComFiltros(
                null, null, null, null, null, null, null);

        // Retorna 200 OK com a lista de pedidos encontrados no corpo da resposta
        return ResponseEntity.ok(new ResumoGeralDTO(todosOsPedidos));
    }

    // GET: Consultar pedido ID
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        List<Pedido> encontradosPedidos = repositorioPedido.findById(id);

        if (encontradosPedidos.isEmpty()) {
            // Retorna 404 se não achar nada
            return ResponseEntity.notFound().build();
        }

        // Retorna 200 com a lista de resultados
        return ResponseEntity.ok(encontrados);
    }
}
