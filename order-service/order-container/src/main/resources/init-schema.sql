DROP SCHEMA IF EXISTS "p_order" CASCADE;

CREATE SCHEMA "p_order";

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
-- The uuid-ossp extension provides support for generating UUIDs (Universally Unique Identifiers)
-- using the OSSP library. UUIDs are commonly used in databases to provide a unique identifier
-- for each row in a table, without needing to rely on sequential IDs or other mechanisms that may result
-- in collisions or gaps in the numbering.

DROP TYPE IF EXISTS order_status;
CREATE TYPE order_status AS ENUM('PENDING', 'PAID', 'APPROVED', 'CANCELLED', 'CANCELLING');

DROP TABLE IF EXISTS "p_order".p_orders CASCADE;

CREATE TABLE "p_order".p_orders
(
    id uuid NOT NULL,
    customer_id uuid NOT NULL,
    restaurant_id uuid NOT NULL,
    tracking_id uuid NOT NULL,
    price numeric(10,2) NOT NULL,
    -- numeric(precision, scale)
    -- Internally, the numeric data type stores the digits of the number as an array of decimal digits,
    -- with each digit represented by a single byte.

    order_status order_status NOT NULL,
    failure_messages varchar COLLATE pg_catalog."default", -- failure_messages varchar COLLATE pg_catalog."default"
    CONSTRAINT order_pkey PRIMARY KEY (id)
);
-- Collation refers to the rules for sorting and comparing strings in a particular language or character set.
-- character varying = varchar, character varying은 standard SQL syntax 용어이고 varchar은 PostgreSQL에서 그를 지칭하는 단어다.

DROP TABLE IF EXISTS "p_order".order_items CASCADE;

CREATE TABLE "p_order".order_items
(
    id bigint NOT NULL,
    order_id uuid NOT NULL,
    product_id uuid NOT NULL,
    price numeric(10, 2) NOT NULL,
    quantity integer NOT NULL,
    sub_total numeric(10, 2) NOT NULL,
    CONSTRAINT order_items_pkey PRIMARY KEY (id, order_id)
);

ALTER TABLE "p_order".order_items
    ADD CONSTRAINT "FK_ORDER_ID" -- constraint name

    -- constraint content #1
    FOREIGN KEY (order_id) REFERENCES "p_order".p_orders (id) MATCH SIMPLE
-- The MATCH SIMPLE clause indicates that when updating the foreign key,
-- the update should be simple and not cascade to other tables.
-- The default for MATCH is SIMPLE. But, it is recommended to specify the MATCH behavior explicitly.

    -- constraint content #2
    ON UPDATE NO ACTION
-- The ON UPDATE NO ACTION clause specifies that if the "id" value in the "orders" table is updated,
-- the "order_id" value in the "order_items" table should not be changed.

    -- constraint content #3
    ON DELETE CASCADE

    NOT VALID;
-- The NOT VALID clause indicates that the constraint is not currently being enforced,
-- but will be enforced in the future. This can be useful when adding constraints to existing tables
-- with pre-existing data, as it allows the constraint to be added without immediately checking all existing
-- rows for compliance.
-- To validate this constraint, do "ALTER TABLE table_name VALIDATE CONSTRAINT constraint_name;"

DROP TABLE IF EXISTS "p_order".order_address CASCADE;

CREATE TABLE "p_order".order_address
(
    id uuid NOT NULL,
    order_id uuid UNIQUE NOT NULL,
    street varchar COLLATE pg_catalog."default" NOT NULL,
    postal_code varchar COLLATE pg_catalog."default" NOT NULL,
    city varchar COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT order_address_pkey PRIMARY KEY (id, order_id)
);

ALTER TABLE "p_order".order_address
    ADD CONSTRAINT "FK_ORDER_ID"
    FOREIGN KEY (order_id) REFERENCES "p_order".p_orders (id) MATCH SIMPLE -- constraint 1
    ON UPDATE NO ACTION -- constraint 2
    ON DELETE CASCADE -- constraint 3
    NOT VALID;

