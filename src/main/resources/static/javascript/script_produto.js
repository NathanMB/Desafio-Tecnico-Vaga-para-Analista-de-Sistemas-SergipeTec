// URL base da API REST (Controller do Spring Boot)
const API_URL = 'http://localhost:8080/api/produtos';

// O evento DOMContentLoaded garante que o HTML carregou totalmente antes do JS rodar.
document.addEventListener("DOMContentLoaded", () => {
    // Se o elemento 'tabela-corpo' existe, sabemos que estamos na tela 'produto-menu.html'
    if (document.getElementById('tabela-corpo')) {
        carregarProdutos();
    }
});

// FUNÇÕES DA TELA DE MENU (produto-menu.html)
// 1. GET: Buscar todos os produtos da API e preencher a tabela
async function carregarProdutos() {
    try {
        // Faz a requisição GET
        const resposta = await fetch(API_URL);
        // Converte o JSON recebido 
        const produtos = await resposta.json(); 

        // Chama a função auxiliar para desenhar o HTML
        preencherTabela(produtos);
    } catch (error) {
        console.error("Erro ao carregar produtos:", error);
    }
}

// 2. GET: Consultar produto específico por ID ou Descricao
async function localizarProduto() {
    const consulta = document.getElementById('consultarProduto').value.trim();
    
     // Se a busca estiver vazia, recarrega todos os produtos
    if (consulta === '') {
        carregarProdutos();
        return;
    }

    try {
        const urlComFiltro = `${API_URL}/${consulta}`;
        const resposta = await fetch(urlComFiltro);

        if (!resposta.ok) {
            alert('Nenhum produto encontrado com esse termo!');
             // Limpa a tabela caso não encontre
            preencherTabela([]);
            return;
        }

        const texto = await resposta.text();
        if (!texto) {
            alert("Nenhum produto retornado.");
            return;
        }

        const produtos = JSON.parse(texto); 
        // O Controlador retorna uma Lista e passa os dados para o preencherTabela
        preencherTabela(produtos); 

    } catch (error) {
        console.error("Erro na consulta:", error);
        alert("Erro ao realizar a busca.");
    }
}

// Função Auxiliar para desenhar as linhas da tabela no HTML
function preencherTabela(produtos) {
    const tabela = document.getElementById('tabela-corpo');
    // Limpa os dados antigos
    tabela.innerHTML = ''; 

    // Para cada produto, cria uma linha na tabela
    produtos.forEach(produto => {
        // Formata o valor para moeda (Ex: 1500.5 -> 1.500,50)
        const valorFormatado = parseFloat(produto.valor).toLocaleString('pt-BR', { minimumFractionDigits: 2 });
        
        const linha = `
            <tr>
                <td>${produto.id}</td>
                <td>${produto.descricao}</td>
                <td>${produto.quantidadeEstoque}</td>
                <td>R$ ${valorFormatado}</td>
            </tr>
        `;
        tabela.innerHTML += linha;
    });
}

// FUNÇÕES DA TELA DE CADASTRO (produto-cadastro.html)
// 3. POST: Enviar dados do novo produto para a API
async function salvarProduto() {
    const descricaoInput = document.getElementById('descricao').value;
    const quantidadeInput = document.getElementById('quantidadeEstoque').value;
    const valorInput = document.getElementById('valor').value;

    // Validação básica do frontend
    if (descricaoInput.trim() === '' || valorInput.trim() === '' || quantidadeInput.trim() === '') {
        alert('Preencha todos os campos!');
        return;
    }

    const dados = {
        descricao: descricaoInput,
        quantidadeEstoque: parseInt(quantidadeInput),
        valor: parseFloat(valorInput)
    };

    try {
        const resposta = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            // Transforma o objeto JS em texto JSON
            body: JSON.stringify(dados) 
        });

        if (resposta.status === 201 || resposta.ok) {
            alert('Produto salvo com sucesso!');
            // Redireciona de volta para a tela de menu
            window.location.href = 'produto-menu.html';
        } else {
            alert('Erro ao salvar produto.');
        }
    } catch (error) {
        console.error("Erro ao salvar:", error);
    }
}

// Função para o botão "Desistir/Cancelar" voltar uma página ou ir para o menu
async function desistir() {
    if (window.history.length > 1) {
        // Se tem histórico, volta para a lista
        window.history.back();
    } else {
        // Se não tem (ex: abriu em nova aba), vai para a Home
        window.location.href = 'menu.html';
    }
}