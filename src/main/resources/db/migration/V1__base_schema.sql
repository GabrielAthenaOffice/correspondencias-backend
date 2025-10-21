-- ================================
-- V1__base_schema.sql
-- Athena Office Correspondências
-- ================================

-- === TABELAS PRINCIPAIS ===

CREATE TABLE aditivo_contratual (
    id BIGSERIAL PRIMARY KEY,
    data_inicio_contrato DATE,
    data_criacao VARCHAR(255),
    pessoa_fisica_nome VARCHAR(255),
    pessoa_fisica_cpf VARCHAR(255),
    pessoa_fisica_endereco VARCHAR(255),
    pessoa_juridica_nome VARCHAR(255),
    pessoa_juridica_cnpj VARCHAR(255),
    pessoa_juridica_endereco VARCHAR(255),
    unidade_nome VARCHAR(255),
    unidade_cnpj VARCHAR(255),
    unidade_endereco VARCHAR(255),
    empresa_id BIGINT
);

CREATE TABLE correspondencias (
    id BIGSERIAL PRIMARY KEY,
    remetente VARCHAR(255),
    nome_empresa_conexa VARCHAR(255),
    status_corresp VARCHAR(50)
        CHECK (status_corresp IN ('AVISADA','DEVOLVIDA','USO_INDEVIDO','ANALISE')),
    data_recebimento TIMESTAMP,
    data_aviso_conexa DATE
);

CREATE TABLE correspondencia_anexos (
    correspondencia_id BIGINT NOT NULL REFERENCES correspondencias(id) ON DELETE CASCADE,
    arquivo_url VARCHAR(255)
);

CREATE TABLE customers (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(255),
    first_name VARCHAR(255),
    field_of_activity VARCHAR(255),
    is_active BOOLEAN,
    bairro VARCHAR(255),
    cep VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(255),
    rua VARCHAR(255),
    numero VARCHAR(255),
    cnpj VARCHAR(255),
    foundation_date VARCHAR(255),
    municipal_inscription VARCHAR(255),
    state_inscription VARCHAR(255),
    status_empresa VARCHAR(255)
);

CREATE TABLE customer_emails_message (
    customer_customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    emails_message VARCHAR(255)
);

CREATE TABLE customer_phones (
    customer_customer_id BIGINT NOT NULL REFERENCES customers(customer_id) ON DELETE CASCADE,
    phones VARCHAR(255)
);

CREATE TABLE empresas (
    id BIGSERIAL PRIMARY KEY,
    nome_empresa VARCHAR(255),
    cnpj VARCHAR(255),
    unidade VARCHAR(255),
    mensagem VARCHAR(255),
    status_empresa VARCHAR(50)
        CHECK (status_empresa IN ('ENVIO_DIGITALIZADO','FALTA_ADITIVO','RESCINDIU','AGUARDANDO')),
    situacao VARCHAR(50)
        CHECK (situacao IN ('ATRASO','ADIMPLENTE','INADIMPLENTE','PROTESTADO','CPF','CNPJ')),
    rua VARCHAR(255),
    numero VARCHAR(255),
    bairro VARCHAR(255),
    cidade VARCHAR(255),
    estado VARCHAR(255),
    cep VARCHAR(255),
    email VARCHAR(255)[],
    telefone VARCHAR(255)[]
);

CREATE TABLE historico_interacoes (
    id BIGSERIAL PRIMARY KEY,
    entidade VARCHAR(100),
    entidade_id BIGINT,
    acao_realizada VARCHAR(255),
    detalhe VARCHAR(255),
    data_hora TIMESTAMP
);

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(255),
    email VARCHAR(255),
    senha VARCHAR(255),
    cargo VARCHAR(255),
    role VARCHAR(50)
        CHECK (role IN ('ADMIN','FUNCIONARIO','ESTAGIARIO')),
    criado_em TIMESTAMP
);

-- =========================
-- RELACIONAMENTOS (FKs)
-- =========================

ALTER TABLE correspondencia_anexos
    ADD CONSTRAINT fk_corresp_anexos FOREIGN KEY (correspondencia_id)
    REFERENCES correspondencias(id) ON DELETE CASCADE;

ALTER TABLE customer_emails_message
    ADD CONSTRAINT fk_customer_emails FOREIGN KEY (customer_customer_id)
    REFERENCES customers(customer_id) ON DELETE CASCADE;

ALTER TABLE customer_phones
    ADD CONSTRAINT fk_customer_phones FOREIGN KEY (customer_customer_id)
    REFERENCES customers(customer_id) ON DELETE CASCADE;
