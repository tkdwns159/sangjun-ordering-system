DROP SCHEMA IF EXISTS customer CASCADE;

CREATE SCHEMA customer;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE customer.customers
(
    id uuid NOT NULL,
    username varchar COLLATE pg_catalog."default" NOT NULL,
    first_name varchar COLLATE pg_catalog."default" NOT NULL,
    last_name varchar COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT customers_pkey PRIMARY KEY (id)
);

DROP MATERIALIZED VIEW IF EXISTS customer.order_customer_m_view;

CREATE MATERIALIZED VIEW customer.order_customer_m_view TABLESPACE pg_default
AS
    SELECT id, username, first_name, last_name FROM customer.customers
WITH DATA;

REFRESH MATERIALIZED VIEW customer.order_customer_m_view;

DROP FUNCTION IF EXISTS customer.refresh_order_customer_m_view;

CREATE OR REPLACE FUNCTION customer.refresh_order_customer_m_view()
RETURNS TRIGGER
AS '
BEGIN
    REFRESH MATERIALIZED VIEW customer.order_customer_m_view;
    return NULL;
END;
' LANGUAGE plpgsql; -- a procedural language for PostgreSQL. Specify only when defining function body.
-- 함수를 trigger 정의에 사용하고 싶으면 trigger을 반환해야한다.
-- 트리거 함수는 NULL이나 TRIGGER object를 반환해야한다.

DROP TRIGGER IF EXISTS refresh_order_customer_m_view_trigger ON customer.customers;

CREATE TRIGGER refresh_order_customer_m_view_trigger
AFTER INSERT OR UPDATE OR DELETE OR TRUNCATE ON customer.customers
    FOR EACH STATEMENT
    EXECUTE PROCEDURE customer.refresh_order_customer_m_view();
-- 트리거는 CRUD의 BEFORE 이나 AFTER에 실행되는데, 이때 트리거 함수가 데이터를 변형시키고 반환하면 그 값으로 덧씌워진다.
-- 반환값이 null인 경우는 그냥 넘어간다.
