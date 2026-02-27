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
    desconto_percentual INTEGER,
    CONSTRAINT fk_item_pedido FOREIGN KEY (pedido_id) REFERENCES tb_pedidos(id) ON DELETE CASCADE,
    CONSTRAINT fk_item_produto FOREIGN KEY (produto_id) REFERENCES tb_produtos(id)
);

COMMENT ON TABLE tb_itens_pedido IS 'Tabela associativa que guarda os produtos comprados dentro de um pedido específico.';
COMMENT ON COLUMN tb_itens_pedido.id IS 'Identificador único do item do pedido (Chave Primária).';
COMMENT ON COLUMN tb_itens_pedido.pedido_id IS 'Chave estrangeira referenciando o pedido a qual este item pertence.';
COMMENT ON COLUMN tb_itens_pedido.produto_id IS 'Chave estrangeira referenciando qual produto foi comprado.';
COMMENT ON COLUMN tb_itens_pedido.valor IS 'Valor unitário do produto travado no momento exato da compra.';
COMMENT ON COLUMN tb_itens_pedido.quantidade IS 'Quantidade comprada deste produto no pedido.';
COMMENT ON COLUMN tb_itens_pedido.desconto_percentual IS 'Porcentagem do desconto aplicado especificamente neste item.';