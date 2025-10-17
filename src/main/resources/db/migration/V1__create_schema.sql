-- Tabela: empresas
CREATE TABLE empresas (
    id SERIAL PRIMARY KEY,
    nome_empresa VARCHAR(255),
    cnpj VARCHAR(20),
    unidade VARCHAR(255),
    mensagem TEXT,
    status_empresa VARCHAR(50),
    situacao VARCHAR(50)
);

CREATE TABLE empresa_email (
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    email VARCHAR(255)
);

CREATE TABLE empresa_telefone (
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    telefone VARCHAR(50)
);

-- Endereço (embeddable)
CREATE TABLE empresa_endereco (
    empresa_id BIGINT NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
    rua VARCHAR(255),
    numero VARCHAR(50),
    complemento VARCHAR(255),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(50),
    cep VARCHAR(20)
);

-- Tabela: correspondencias
CREATE TABLE correspondencias (
    id SERIAL PRIMARY KEY,
    remetente VARCHAR(255),
    nome_empresa_conexa VARCHAR(255),
    status_corresp VARCHAR(50),
    data_recebimento TIMESTAMP,
    data_aviso_conexa DATE
);

CREATE TABLE correspondencia_anexos (
    correspondencia_id BIGINT NOT NULL REFERENCES correspondencias(id) ON DELETE CASCADE,
    arquivo_url VARCHAR(255)
);

-- Tabela: historico_interacoes
CREATE TABLE historico_interacoes (
    id SERIAL PRIMARY KEY,
    entidade VARCHAR(100),
    entidade_id BIGINT,
    acao_realizada VARCHAR(255),
    detalhe TEXT,
    data_hora TIMESTAMP
);

-- Tabela: customers
CREATE TABLE customers (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    first_name VARCHAR(255),
    field_of_activity VARCHAR(255),
    is_active BOOLEAN,
    status_empresa VARCHAR(100)
);

CREATE TABLE customer_emails_message (
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    email VARCHAR(255)
);

CREATE TABLE customer_phones (
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    telefone VARCHAR(50)
);

-- Endereços e pessoa jurídica embutidos
CREATE TABLE customer_endereco (
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    rua VARCHAR(255),
    numero VARCHAR(50),
    complemento VARCHAR(255),
    bairro VARCHAR(100),
    cidade VARCHAR(100),
    estado VARCHAR(50),
    cep VARCHAR(20)
);

CREATE TABLE customer_legal_person (
    customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    razao_social VARCHAR(255),
    cnpj VARCHAR(20)
);
