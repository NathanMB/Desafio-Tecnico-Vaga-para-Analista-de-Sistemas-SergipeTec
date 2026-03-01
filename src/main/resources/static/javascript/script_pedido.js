// URLs base para as APIs
const API_URL_CLIENTES = 'http://localhost:8080/api/clientes';
const API_URL_PRODUTOS = 'http://localhost:8080/api/produtos';
const API_URL_PEDIDOS = 'http://localhost:8080/api/pedidos';

/**
* Inicialmente vazio, vai sendo preenchido conforme o usuário adiciona produtos ao carrinho.
* Armazena os itens temporariamente na memória do navegador antes de enviar ao servidor.
*/
let itensPedido = []; 

// O evento DOMContentLoaded garante que o HTML carregou totalmente antes do JS rodar.
document.addEventListener("DOMContentLoaded", () => {
    // Se o elemento 'dataInicio' existe, sabemos que estamos na tela 'pedido-menu.html'
    if (document.getElementById('dataInicio')) {
        carregarPedidos();
    }
});

// FUNÇÕES DA TELA DE CADASTRO (pedido-cadastro.html)

/**
 * Busca um cliente na API usando o ID ou Nome digitado.
 * Preenche os campos da tela (nome e e-mail) e guarda o ID em um campo oculto para o momento de salvar.
 */
async function localizarCliente() {
    const identificadorCliente = document.getElementById('consultarCliente').value.trim();
    if (!identificadorCliente) 
        return alert("Digite o ID ou Nome do cliente");

    try {
        const response = await fetch(`${API_URL_CLIENTES}/${identificadorCliente}`);
        // Se a resposta for 200 OK, preenche os campos. Caso contrário, mostra alerta e limpa os campos.
        if (response.ok) {
            // A API retorna um array de clientes, mas como a busca é por ID ou nome completo, esperamos apenas um resultado.
            const clientes = await response.json();
            const cliente = clientes[0];

            document.getElementById('cliente-id-selecionado').value = cliente.id;
            document.getElementById('cliente-nome-completo').value = cliente.nome;
            document.getElementById('cliente-email-completo').value = cliente.email;
        } else {
            alert("Cliente não encontrado!");
            limparDadosCliente();
        }
    } catch (error) {
        console.error("Erro ao buscar cliente:", error);
        limparDadosCliente();
    }
}

// Limpa os dados do cliente da tela caso a busca falhe ou o usuário queira trocar de cliente.
function limparDadosCliente() {
    document.getElementById('cliente-id-selecionado').value = "";
    document.getElementById('cliente-nome-completo').value = "";
    document.getElementById('cliente-email-completo').value = "";
}

// Fluxo principal de adição de produtos ao carrinho.
async function adicionarProduto() {
    const identificadorProduto = document.getElementById('consultarProduto').value.trim();
    const quantidadeDigitada = parseInt(document.getElementById('quantidadeProduto').value) || 1;
    const descontoDigitado = parseInt(document.getElementById('descontoProduto').value) || 0;

    if (!identificadorProduto) 
        return alert("Digite o ID ou Descrição do produto");
    if (quantidadeDigitada < 1) 
        return alert("A quantidade deve ser maior que zero.");
    if (descontoDigitado < 0 || descontoDigitado > 100) 
        return alert("O desconto deve ser entre 0 e 100%.");

    // Busca o produto na API para validar existência e estoque antes de adicionar ao carrinho
    try {
        const response = await fetch(`${API_URL_PRODUTOS}/${identificadorProduto}`);
        // Se o produto for encontrado, valida as regras de negócio antes de desenhar na tela
        if (response.ok) {
            const produtos = await response.json();
            const produto = produtos[0];

            // Validações de Negócio
            const existe = itensPedido.find(item => item.produtoId === produto.id);
            if (existe) 
                return alert("Este produto já foi adicionado ao carrinho!");

            // Verifica se a quantidade solicitada é menor ou igual ao estoque disponível
            if (produto.quantidadeEstoque < quantidadeDigitada) {
                return alert(`Estoque insuficiente! O produto ${produto.descricao} só tem ${produto.quantidadeEstoque} unidades disponíveis.`);
            }

            // Passou nas validações, desenha na tela
            adicionarLinhaProduto(produto, quantidadeDigitada, descontoDigitado);
            
            // Limpa os campos de input para o próximo produto
            document.getElementById('consultarProduto').value = ""; 
            document.getElementById('quantidadeProduto').value = "1";
            document.getElementById('descontoProduto').value = "0";
        } else {
            alert("Produto não encontrado!");
        }
    } catch (error) {
        console.error("Erro ao buscar produto:", error);
    }
}

// Desenhando a linha do produto na tabela do pedido.
function adicionarLinhaProduto(produto, quantidade, desconto) {
    const tbody = document.getElementById('tabela-corpo-carrinho') || document.getElementById('tabela-corpo');

    // Adiciona o item ao array de estado 'itensPedido' para manter a lógica de negócio e cálculos centralizados no JavaScript,
    // sem depender do HTML.
    const novoItem = {
        produtoId: produto.id,
        valorUnitario: produto.valor,
        quantidade: quantidade,
        desconto: desconto
    };
    itensPedido.push(novoItem);

    const subtotal = (produto.valor * quantidade) * (1 - (desconto / 100));

    // Cria a linha da tabela usando e já inclui os eventos onchange para atualizar os dados do item.
    const tr = document.createElement('tr');
    tr.id = `linha-${produto.id}`;
    tr.innerHTML = `
        <td>${produto.id}</td>
        <td>${produto.descricao}</td>
        <td>R$ ${produto.valor.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
        <td>
            <input type="number" class="form-control form-control-sm" style="width: 70px;"
                value="${quantidade}" min="1" max="${produto.quantidadeEstoque}" 
                onchange="atualizarDadosItem(${produto.id}, 'quantidade', this.value)">
        </td>
        <td>
            <input type="number" class="form-control form-control-sm" style="width: 70px;"
                value="${desconto}" min="0" max="100" 
                onchange="atualizarDadosItem(${produto.id}, 'desconto', this.value)">
        </td>
        <td id="subtotal-${produto.id}" class="fw-bold">R$ ${subtotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
        <td class="text-center">
            <button class="btn btn-outline-danger btn-sm" onclick="removerItem(${produto.id})">Remover</button>
        </td>
    `;
    tbody.appendChild(tr);

    // Após adicionar o produto, recalcula o total geral para refletir a nova adição.
    atualizarTotalProdutos();
}

/**
 * Disparada sempre que o usuário altera a quantidade ou desconto direto no input da tabela.
 * Atualiza o array de estado e recalcula apenas o subtotal daquela linha específica.
 */
function atualizarDadosItem(id, campo, valorConvertido) {
    // Encontra o item correspondente no array 'itensPedido' e atualiza o campo alterado (quantidade ou desconto).
    const item = itensPedido.find(i => i.produtoId === id);

    if (item) {
        item[campo] = parseFloat(valorConvertido);
        const subtotal = (item.valorUnitario * item.quantidade) * (1 - (item.desconto / 100));
        
        // Atualiza o subtotal apenas da linha alterada, sem precisar redesenhar toda a tabela.
        const celulaSubtotal = document.getElementById(`subtotal-${id}`);
        if (celulaSubtotal) {
            celulaSubtotal.innerText = `R$ ${subtotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
        }
        atualizarTotalProdutos();
    }
}

// O array é filtrado para criar um novo array sem o item removido, e a linha correspondente na tabela é deletada.
function removerItem(id) {
    itensPedido = itensPedido.filter(i => i.produtoId !== id);
    document.getElementById(`linha-${id}`).remove();
    atualizarTotalProdutos();
}

/**
 * Percorre o array 'itensPedido', calcula a soma matemática de todos os subtotais 
 * e exibe o valor formatado na tela de cadastro.
 */
function atualizarTotalProdutos() {
    // Calcula o total geral somando o subtotal de cada item, já aplicando o desconto.
    const totalGeral = itensPedido.reduce((acc, item) => {
        const totalItem = (item.valorUnitario * item.quantidade) * (1 - (item.desconto / 100));
        return acc + totalItem;
    }, 0);

    // Atualiza o valor total exibido na tela.
    const elementoTotal = document.getElementById('valor-total-pedido');
    if (elementoTotal) {
        elementoTotal.innerText = `R$ ${totalGeral.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
    }
}

/**
 * Monta o OTD esperado pelo Backend, dispara o POST e redireciona o usuário em caso de sucesso.
 */
async function salvarPedido() {
    const clienteId = document.getElementById('cliente-id-selecionado').value;
    
    if (!clienteId)
        return alert("Por favor, busque e selecione um cliente antes de confirmar o pedido.");
    if (itensPedido.length === 0) 
        return alert("Adicione pelo menos um produto ao carrinho!");

    // Monta o OTD seguindo a estrutura esperada pelo backend, usando os IDs e valores atuais do estado.
    const dadosPedido = {
        clienteId: parseInt(clienteId),
        itens: itensPedido
    };

    // Envia os dados do pedido para a API. O backend irá validar o estoque novamente antes de confirmar a venda.
    try {
        const response = await fetch(API_URL_PEDIDOS, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dadosPedido)
        });

        // Se a resposta for positiva, mostra alerta de sucesso e redireciona para o menu de pedidos. 
        // Caso contrário, exibe o erro retornado pelo backend.
        if (response.ok) {
            alert("Pedido realizado com sucesso! O estoque foi atualizado.");
            window.location.href = 'pedido-menu.html';
        } else {
            // Se o backend devolver erro
            const errorMsg = await response.text();
            alert(`Erro ao confirmar pedido: ${errorMsg}`);
        }
    } catch (error) {
        console.error("Erro ao enviar pedido:", error);
        alert("Erro de conexão ao tentar enviar o pedido.");
    }
}


// FUNÇÕES DA TELA DE MENU (pedido-menu.html)

// O endpoint GET /todos já retorna todos os pedidos e o valor total calculado, para otimizar a consulta.
async function carregarPedidos() {
    try {
        const resposta = await fetch(`${API_URL_PEDIDOS}/todos`);
        const dados = await resposta.json();
        
        // Preenche a tabela do menu com os pedidos e já atualiza o valor total usando o valor calculado no backend.
        preencherTabelaPedidos(dados.pedidos);
        atualizarFaturamentoDashboard(dados.faturamentoTotal);
    } catch (error) {
        console.error("Erro ao carregar pedidos:", error);
    }
}

/**
 * Endpoint de filtros. Monta a Query String dinamicamente com base 
 * apenas nos campos que o usuário preencheu na tela.
 */
async function buscarPedidos() {
    const idPedido = document.getElementById('pedidoId').value.trim();
    const identificadorCliente = document.getElementById('cliente').value.trim();
    const identificadorProduto = document.getElementById('produto').value.trim();
    const dataInicio = document.getElementById('dataInicio').value;
    const dataFim = document.getElementById('dataFim').value;

    // Monta a Query String dinamicamente, incluindo apenas os parâmetros que foram preenchidos.
    const params = new URLSearchParams();

    if (idPedido) params.append('id', idPedido);
    if (identificadorCliente) params.append('cliente', identificadorCliente);
    if (identificadorProduto) params.append('produto', identificadorProduto);
    if (dataInicio) params.append("dataInicio", dataInicio);
    if (dataFim) params.append("dataFim", dataFim);

    // Se apertar buscar com tudo vazio, recarrega todos
    if (params.toString() === "") {
        carregarPedidos();
        return;
    }

    try {
        const resposta = await fetch(`${API_URL_PEDIDOS}/buscar?${params.toString()}`);

        // Se a resposta não for 200 OK, mostra alerta e limpa a tabela e o faturamento para refletir que não há resultados.
        if (!resposta.ok) {
            alert("Nenhum pedido encontrado com esses filtros.");
            preencherTabelaPedidos([]);
            atualizarFaturamentoDashboard(0);
            return;
        }

        const dados = await resposta.json();
        
        // Mesmo que o backend retorne um array vazio, a resposta é 200 OK, 
        // então verificamos se há pedidos no array para mostrar o alerta.
        if (dados.pedidos.length === 0) {
            alert("Nenhum pedido encontrado com esses filtros.");
        }
        
        // Preenche a tabela do menu com os pedidos filtrados e atualiza o valor total usando o valor calculado no backend.
        preencherTabelaPedidos(dados.pedidos);
        atualizarFaturamentoDashboard(dados.faturamentoTotal);

    } catch (error) {
        console.error("Erro na busca:", error);
    }
}

// Desenha as linhas do OTD (OTDResumoPedidos) retornado pelo Backend na tela do Dashboard.
function preencherTabelaPedidos(pedidos) {
    const tabela = document.getElementById('tabela-lista-pedidos') || document.getElementById('tabela-corpo');
    
    if (!tabela)
        return; 
    tabela.innerHTML = ''; 

    // Para cada pedido, cria uma linha na tabela. O OTDResumoPedidos já vem com os dados formatados para exibição.
    pedidos.forEach(pedido => {
        const valorFormatado = parseFloat(pedido.valorTotal).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
        const dataObjeto = new Date(pedido.dataPedido);
        const dataFormatada = dataObjeto.toLocaleString('pt-BR', { dateStyle: 'short'});

        const linha = `
            <tr>
                <td>${pedido.id}</td>
                <td>${pedido.nomeCliente}</td>
                <td>${dataFormatada}</td>
                <td class="fw-bold">R$ ${valorFormatado}</td>
                <td>
                    <button class="btn btn-sm btn-outline-dark fw-bold" onclick="abrirDetalhes(${pedido.id})">
                        Detalhar Pedido
                    </button>
                </td>
            </tr>
        `;
        tabela.innerHTML += linha;
    });
}

// Exibe o faturamento total retornado pelo backend na tela de relatório.
function atualizarFaturamentoDashboard(valor) {
    const elementoTotal = document.getElementById('valor-total-geral');

    // Se o elemento existir, atualiza o texto com o valor formatado
    if (elementoTotal) {
        const total = valor ? parseFloat(valor) : 0;
        elementoTotal.innerText = `R$ ${total.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;
    }
}

/**
 * Consome o endpoint GET /{id} para exibir
 * o Modal de detalhamento com os itens, valores e descontos que formaram o pedido.
 */
async function abrirDetalhes(id) {
    try {
        const resposta = await fetch(`${API_URL_PEDIDOS}/${id}`);

        // Se a resposta não for 200 OK, mostra alerta e não tenta abrir o modal.
        if (!resposta.ok) {
            alert("Erro ao buscar os detalhes do pedido.");
            return;
        }
        
        const pedidoCompleto = await resposta.json();

        // Preenche o Cabeçalho do Modal
        document.getElementById('modal-pedido-id').innerText = pedidoCompleto.id;
        document.getElementById('modal-cliente-nome').innerText = pedidoCompleto.cliente.nome;
        
        const dataObjeto = new Date(pedidoCompleto.dataPedido);
        document.getElementById('modal-pedido-data').innerText = dataObjeto.toLocaleString('pt-BR', { dateStyle: 'short'});

        // Desenha os Itens no Modal
        const tbody = document.getElementById('tabela-itens-modal');
        tbody.innerHTML = ''; 
        let totalPedido = 0;

        // O OTDPedido já vem com os dados formatados para exibição, então aqui só é preciso iterar e desenhar.
        pedidoCompleto.itens.forEach(item => {
            const desconto = item.desconto || 0;
            const subtotal = (item.valor * item.quantidadeItens) * (1 - (desconto / 100));
            totalPedido += subtotal;

            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${item.produto.descricao}</td>
                <td>${item.quantidadeItens}</td>
                <td>R$ ${parseFloat(item.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
                <td>${desconto}%</td>
                <td class="fw-bold">R$ ${subtotal.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}</td>
            `;
            tbody.appendChild(tr);
        });

        // Atualiza o Rodapé do Modal
        document.getElementById('modal-valor-total').innerText = `R$ ${totalPedido.toLocaleString('pt-BR', { minimumFractionDigits: 2 })}`;

        // O Bootstrap já tem um componente Modal pré-construído, 
        // então aqui só é preciso chamar a função de mostrar o modal após preencher os dados.
        const modal = new bootstrap.Modal(document.getElementById('modalDetalhesPedido'));
        modal.show();

    } catch (error) {
        console.error("Erro ao carregar detalhes do pedido:", error);
    }
}