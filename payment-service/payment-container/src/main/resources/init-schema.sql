DROP SCHEMA IF EXISTS payment CASCADE;

CREATE SCHEMA payment;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

DROP TYPE IF EXISTS PAYMENT_STATUS;

CREATE TYPE payment_status AS ENUM('CANCELLED', 'FAILED', 'COMPLETED');

DROP TABLE IF EXISTS "payment".payments CASCADE;

CREATE TABLE "payment".payments
(
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    order_id UUID NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL, -- timestamp에 timezone을 포함해서 저장. ZonedDateTime과 호환.
    status PAYMENT_STATUS NOT NULL,
    CONSTRAINT payments_pkey PRIMARY KEY (id)
);

DROP TABLE IF EXISTS "payment".credit_entry CASCADE;

CREATE TABLE "payment".credit_entry
(
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    total_credit_amount NUMERIC(10,2) NOT NULL,
    CONSTRAINT credit_entry_pkey PRIMARY KEY (id)
);

DROP TYPE IF EXISTS TRANSACTION_TYPE;

CREATE TYPE TRANSACTION_TYPE AS ENUM('DEBIT', 'CREDIT');

DROP TABLE IF EXISTS "payment".credit_history;

CREATE TABLE "payment".credit_history
(
    id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(10, 2) NOT NULL,
    type TRANSACTION_TYPE NOT NULL,
    CONSTRAINT credit_history_pkey PRIMARY KEY (id)
);
