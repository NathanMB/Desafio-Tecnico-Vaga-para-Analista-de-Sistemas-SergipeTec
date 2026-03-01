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
import br.com.nathan.desafiosergipetec.otds.OTDPedido;
import br.com.nathan.desafiosergipetec.otds.OTDPedidoRequest;
import br.com.nathan.desafiosergipetec.otds.OTDProdutoRequest;
import br.com.nathan.desafiosergipetec.otds.OTDResumoPedidos;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioCliente;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioPedido;
import br.com.nathan.desafiosergipetec.repositorios.RepositorioProduto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
     * do processo, o Spring cancela a transação inteira (rollback automático),
     * evitando salvar um pedido pela metade.
     */
    @PostMapping
    @Transactional
    public ResponseEntity<Pedido> salvar(@RequestBody OTDPedidoRequest dto) {
        Pedido pedido = new Pedido();

        // Busca o cliente
        Cliente cliente = repositorioCliente.buscarPorId(dto.getClienteId()).orElse(null);

        // Verifica se o cliente foi encontrado
        if (cliente != null) {
            pedido.setCliente(cliente);
        } else {
            throw new RuntimeException("Cliente não encontrado");
        }

        for (OTDProdutoRequest itemDto : dto.getItens()) {
            // Busca o produto
            Produto produto = repositorioProduto.buscarPorId(itemDto.getProdutoId()).orElse(null);

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
            item.setDescontoPercentual(itemDto.getDesconto() != null ? itemDto.getDesconto() : 0);
            item.setValor(produto.getValor());

            // Adiciona o item ao pedido
            pedido.adicionarItem(item);

            // Salva o produto atualizado (com estoque reduzido)
            Produto produtoSalvo = repositorioProduto.save(produto);
        }

        // Salva o pedido e retorna 201 Created com o pedido criado no corpo da resposta
        Pedido pedidoSalvo = repositorioPedido.save(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoSalvo);
    }

    /**
     * GET: Buscar pedidos com múltiplos filtros dinâmicos
     * 
     * O sistema testa (try-catch) se o termo pesquisado é um id, nome ou descrição.
     * Com isso permite usar um único campo no frontend para buscar coisas completamente diferentes.
     */
    @GetMapping("/buscar")
    public ResponseEntity<OTDResumoPedidos> buscarPedidos(
            @RequestParam(name = "id", required = false) Long id,
            @RequestParam(name = "cliente", required = false) String clienteIdentificador,
            @RequestParam(name = "produto", required = false) String produtoIdentificador,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dataFim) {

        Long idCliente = null;
        String nomeCliente = null;
        Long idProduto = null;
        String descricaoProduto = null;

        // Tenta converter o clienteIdentificador para ID, se falhar assume que é um nome do cliente
        if (clienteIdentificador != null && !clienteIdentificador.isBlank()) {
            try {
                idCliente = Long.parseLong(clienteIdentificador);
            } catch (NumberFormatException e) {
                nomeCliente = clienteIdentificador;
            }
        }

        // Tenta converter o produtoIdentificador para ID, se falhar assume que é uma descrição do produto
        if (produtoIdentificador != null && !produtoIdentificador.isBlank()) {
            try {
                idProduto = Long.parseLong(produtoIdentificador);
            } catch (NumberFormatException e) {
                descricaoProduto = produtoIdentificador;
            }
        }

        /**
        * Se a data for nula, o repositório deve interpretar como "sem filtro de data".
        * O LocalDate é convertido para LocalDateTime com o horário ajustado para o início ou fim do dia.
        * Isso garante que a busca inclua todo o dia selecionado.
        */
        LocalDateTime inicio = (dataInicio != null) ? dataInicio.atStartOfDay() : null;
        LocalDateTime fim = (dataFim != null) ? dataFim.atTime(23, 59, 59) : null;

        // Chama o repositório passando os filtros (que podem ser nulos)
        List<OTDPedido> pedidosEncontrados = repositorioPedido.buscarComFiltros(
                id, idCliente, nomeCliente, idProduto, descricaoProduto, inicio, fim);

        // Retorna 200 OK com a lista de pedidos encontrados no corpo da resposta
        return ResponseEntity.ok(new OTDResumoPedidos(pedidosEncontrados));
    }

    // GET: Consultar todos os pedidos (sem filtros)
    @GetMapping("/todos")
    public ResponseEntity<OTDResumoPedidos> listarTodos() {
        // Chama o repositório passando nulos para todos os filtros, o que deve retornar tudo
        List<OTDPedido> todosOsPedidos = repositorioPedido.buscarComFiltros(
                null, null, null, null, null, null, null);

        // Retorna 200 OK com a lista de pedidos encontrados no corpo da resposta
        return ResponseEntity.ok(new OTDResumoPedidos(todosOsPedidos));
    }

    // GET: Consultar pedido ID
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> buscarPorId(@PathVariable Long id) {
        // Chama o repositório passando o ID (e nulos para os outros filtros)
        Pedido pedidoEncontrado = repositorioPedido.findById(id)
                .orElse(null);

        if (pedidoEncontrado == null) {
            // Retorna 404 se não achar nada
            return ResponseEntity.notFound().build();
        }

        // Retorna 200 com o pedido encontrado
        return ResponseEntity.ok(pedidoEncontrado);
    }
}
