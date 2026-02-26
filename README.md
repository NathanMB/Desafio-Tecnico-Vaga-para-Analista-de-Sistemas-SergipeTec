# Desafio Técnico - Analista de Sistemas (SergipeTec)

Este repositório contém a solução para o desafio técnico de desenvolvimento de uma aplicação simples de cadastro e consulta de Clientes, Produtos e Pedidos. O foco principal deste projeto é demonstrar boas práticas de Orientação a Objetos, organização de código e arquitetura de software.

## Tecnologias Utilizadas

* **Backend:** Java 25, Spring Boot 
* **Banco de Dados:** PostgreSQL (Relacional) 
* **Frontend:** HTML5, CSS3 (Bootstrap) e Vanilla JavaScript (API Fetch) 
* **Gerenciador de Dependências:** Maven
* **Versionamento:** Git

## Decisões Técnicas e Arquiteturais

1.  **Ausência de Geradores de Código (Lombok):** Em estrita conformidade com as regras do desafio, que proíbem o uso de geradores de código sob pena de desclassificação, ferramentas como o Lombok não foram utilizadas. Todos os construtores, *Getters* e *Setters* foram gerados nativamente no Java.
2.  **Uso Exclusivo de Native Queries:** Para o acesso ao banco de dados relacional, o uso do JPQL/HQL foi evitado, priorizando a anotação `@Query(nativeQuery = true)` no Spring Data JPA, cumprindo a exigência de usar *native query*.
3.  **Padrão DTO (Data Transfer Object):** Utilizado para isolar as Entidades do banco de dados (Models) das informações que são trafegadas para o Frontend, garantindo segurança e encapsulamento na criação e exibição dos pedidos.
4.  **Chaves Primárias (BIGSERIAL vs NUMERIC):** Optou-se pelo uso de `BIGSERIAL` (que resulta em `BIGINT` no PostgreSQL) ao invés de `NUMERIC` para os IDs. Isso melhora drasticamente a performance de busca nos índices B-Tree e nos `JOINs`, além de garantir a semântica correta de um identificador sequencial discreto.
5.  **DDL Comments:** Foram adicionados comentários (`COMMENT ON`) diretamente no script SQL para documentar o banco de dados no nível de infraestrutura, facilitando o entendimento do dicionário de dados.

## Pré-requisitos e Configuração do Banco de Dados

Antes de executar a aplicação, certifique-se de ter instalado:
* [Java JDK 25+](https://adoptium.net/)
* [PostgreSQL](https://www.postgresql.org/)

### 1. Criar o Banco de Dados
No seu servidor PostgreSQL (via pgAdmin ou DBeaver), crie um banco de dados chamado `desafiotecnico_db`:
```sql
CREATE DATABASE desafiotecnico_db;
```

### 2. Scripts de Criação das Tabelas (DDL)
Abaixo estão os scripts para a criação das tabelas, que também se encontram no arquivo `src/main/resources/schema.sql`. Execute-os no banco `desafiotecnico_db`:
```sql
-- Criação da tabela de Clientes
CREATE TABLE tb_clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_clientes IS 'Tabela responsável por armazenar os dados dos clientes.';
COMMENT ON COLUMN tb_clientes.id IS 'Identificador único do cliente (Chave Primária).';
COMMENT ON COLUMN tb_clientes.nome IS 'Nome completo do cliente.';
COMMENT ON COLUMN tb_clientes.email IS 'Endereço de e-mail do cliente (deve ser único).';
COMMENT ON COLUMN tb_clientes.data_cadastro IS 'Data e hora em que o cliente foi registrado no sistema.';

-- Criação da tabela de Produtos
CREATE TABLE tb_produtos (
    id BIGSERIAL PRIMARY KEY,
    descricao VARCHAR(200) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    quantidade_estoque INTEGER NOT NULL,
    data_cadastro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE tb_produtos IS 'Tabela responsável por armazenar o catálogo de produtos e controle de estoque.';
COMMENT ON COLUMN tb_produtos.id IS 'Identificador único do produto (Chave Primária).';
COMMENT ON COLUMN tb_produtos.descricao IS 'Nome ou descrição detalhada do produto.';
COMMENT ON COLUMN tb_produtos.valor IS 'Preço atual de venda do produto.';
COMMENT ON COLUMN tb_produtos.quantidade_estoque IS 'Quantidade física atual disponível para venda.';
COMMENT ON COLUMN tb_produtos.data_cadastro IS 'Data e hora em que o produto foi cadastrado no catálogo.';

-- Criação da tabela de Pedidos
CREATE TABLE tb_pedidos (
    id BIGSERIAL PRIMARY KEY,
    cliente_id BIGINT NOT NULL,
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pedido_cliente FOREIGN KEY (cliente_id) REFERENCES tb_clientes(id)
);

COMMENT ON TABLE tb_pedidos IS 'Tabela principal de pedidos realizados pelos clientes.';
COMMENT ON COLUMN tb_pedidos.id IS 'Identificador único do pedido (Chave Primária).';
COMMENT ON COLUMN tb_pedidos.cliente_id IS 'Chave estrangeira referenciando o cliente que realizou o pedido.';
COMMENT ON COLUMN tb_pedidos.data_pedido IS 'Data e hora em que o pedido foi finalizado.';

-- Criação da tabela de Itens do Pedido (A relação entre Pedido e Produto)
CREATE TABLE tb_itens_pedido (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    quantidade INTEGER NOT NULL,
    desconto NUMERIC(10, 2) DEFAULT 0.00,
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES tb_produtos(id)
);

COMMENT ON TABLE tb_itens_pedido IS 'Tabela associativa que guarda os produtos comprados dentro de um pedido específico.';
COMMENT ON COLUMN tb_itens_pedido.id IS 'Identificador único do item do pedido (Chave Primária).';
COMMENT ON COLUMN tb_itens_pedido.pedido_id IS 'Chave estrangeira referenciando o pedido a qual este item pertence.';
COMMENT ON COLUMN tb_itens_pedido.produto_id IS 'Chave estrangeira referenciando qual produto foi comprado.';
COMMENT ON COLUMN tb_itens_pedido.valor IS 'Valor unitário do produto travado no momento exato da compra.';
COMMENT ON COLUMN tb_itens_pedido.quantidade IS 'Quantidade comprada deste produto no pedido.';
COMMENT ON COLUMN tb_itens_pedido.desconto IS 'Valor do desconto aplicado especificamente neste item.';
```

## 3. Como Executar a Aplicação
Não é necessário ter o Maven instalado na máquina. O projeto utiliza o Maven Wrapper para baixar as dependências e rodar a aplicação automaticamente.
1. Clone o repositório:
`git clone [https://github.com/NathanMB/Desafio-Tecnico-Vaga-para-Analista-de-Sistemas-SergipeTec.git](https://github.com/NathanMB/Desafio-Tecnico-Vaga-para-Analista-de-Sistemas-SergipeTec.git)`

2. Configurar Credenciais
Ajuste as credenciais do banco no arquivo `src/main/resources/application.properties`:
`spring.datasource.url=jdbc:postgresql://localhost:5432/desafiotecnico_db`
`spring.datasource.username=postgres`
`spring.datasource.password=sua_senha_aqui`

3. Acesse a pasta:
`cd Desafio-Tecnico-Vaga-para-Analista-de-Sistemas-SergipeTec`

4. Execute o projeto via Maven Wrapper:
No Windows (Prompt ou PowerShell):
`mvnw.cmd spring-boot:run`

No Linux / macOS (Terminal):
`./mvnw spring-boot:run`

5. Acesse o sistema: Abra o navegador na URL http://localhost:8080/menu.html.

## 4. Funcionalidades Implementadas
Clientes

* [x] Cadastrar cliente (Nome, E-mail, Data de cadastro).

* [x] Listar clientes.

* [x] Consultar cliente por Nome ou identificador (Native Query).

Produtos

* [x] Cadastrar produto (Descrição, Valor, Quantidade em estoque, Data de cadastro).

* [x] Listar produtos.

* [x] Consultar produtos por descrição ou identificador (Native Query).

Pedidos

* [x] Criar pedido contendo produtos (registrando valor, quantidade e desconto individual).

* [x] Atualizar a quantidade em estoque dos produtos ao criar o pedido.

* [x] Listar pedidos de um cliente.

* [x] Listar pedidos que contenham um produto específico.

* [x] Consultar o valor total de pedidos por cliente.

* [x] Consultar pedidos por identificador ou período de datas.