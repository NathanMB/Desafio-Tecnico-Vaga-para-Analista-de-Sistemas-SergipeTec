// URL base da API REST (Controller do Spring Boot)
const API_URL = 'http://localhost:8080/api/clientes';

// O evento DOMContentLoaded garante que o HTML carregou totalmente antes do JS rodar.
document.addEventListener("DOMContentLoaded", () => {
    // Se o elemento 'tabela-corpo' existe, sabemos que estamos na tela 'cliente-menu.html'
    if (document.getElementById('tabela-corpo')) {
        carregarClientes();
    }
});

// FUNÇÕES DA TELA DE MENU (cliente-menu.html)
// 1. GET: Buscar todos os clientes da API e preencher a tabela
async function carregarClientes() {
    try {
        // Faz a requisição GET
        const resposta = await fetch(API_URL);
        // Converte o JSON recebido
        const clientes = await resposta.json();
        
        // Chama a função auxiliar para desenhar o HTML
        preencherTabela(clientes);
    } catch (error) {
        console.error("Erro ao carregar clientes:", error);
        alert("Erro ao conectar com o servidor.");
    }
}

// 2. GET: Consultar cliente específico por ID ou Nome
async function localizarCliente() {
    const consulta = document.getElementById('consultarCliente').value.trim();
    
    // Se a busca estiver vazia, recarrega todos os clientes
    if (consulta === '') {
        carregarClientes();
        return;
    }

    const urlComFiltro = `${API_URL}/${consulta}`;

    try {
        const resposta = await fetch(urlComFiltro);

        if (!resposta.ok) {
            alert('Nenhum cliente encontrado com esse Identificador!');
            // Limpa a tabela caso não encontre
            preencherTabela([]); 
            return;
        }

        const texto = await resposta.text();
        if (!texto) {
            alert("Nenhum cliente retornado.");
            return;
        }

        const clientes = JSON.parse(texto);
        // O Controlador retorna uma Lista e passa os dados para o preencherTabela
        preencherTabela(clientes); 

    } catch (error) {
        console.error("Erro na consulta:", error);
        alert("Erro ao realizar a busca.");
    }
}

// Função Auxiliar para desenhar as linhas da tabela no HTML
function preencherTabela(clientes) {
    const tabela = document.getElementById('tabela-corpo');
    // Limpa os dados antigos
    tabela.innerHTML = '';

    // Para cada cliente, cria uma linha na tabela
    clientes.forEach(cliente => {
        const linha = `
            <tr>
                <td>${cliente.id}</td>
                <td>${cliente.nome}</td>
                <td>${cliente.email}</td>
            </tr>
        `;
        tabela.innerHTML += linha;
    });
}

// FUNÇÕES DA TELA DE CADASTRO (cliente-cadastro.html)
// 3. POST: Enviar dados do novo cliente para a API
async function salvarCliente() {
    const nomeInput = document.getElementById('nome').value;
    const emailInput = document.getElementById('email').value;

    // Validação básica do frontend
    if (nomeInput.trim() === '' || emailInput.trim() === '') {
        alert('Preencha todos os campos obrigatórios!');
        return;
    }

    const dados = {
        nome: nomeInput,
        email: emailInput
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
            alert('Cliente cadastrado com sucesso!');
            // Redireciona de volta para a tela de menu
            window.location.href = 'cliente-menu.html';
        } else {
            alert('Erro ao salvar cliente. Verifique se o e-mail já existe.');
        }
    } catch (error) {
        console.error("Erro ao salvar:", error);
        alert("Erro de conexão ao tentar salvar o cliente.");
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