-- V3__create_customers_full_view.sql

-- Remove a view antiga (caso exista)
DROP VIEW IF EXISTS customers_full;

-- Cria a view unificando customers + customer_endereco
CREATE OR REPLACE VIEW customers_full AS
SELECT
    c.customer_id,
    c.name,
    c.first_name,
    c.field_of_activity,
    c.is_active,
    c.status_empresa,
    e.rua,
    e.numero,
    e.bairro,
    e.cidade,
    e.estado,
    e.cep
FROM customers c
LEFT JOIN customer_endereco e ON e.customer_id = c.customer_id;
