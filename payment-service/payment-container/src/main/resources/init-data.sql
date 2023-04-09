INSERT INTO "payment".credit_entry (id, customer_id, total_credit_amount)
    VALUES ('5c9a4adf-434a-445c-b396-b2baceb09f8b', '49f72dbd-ae1d-4f6b-b17c-122d402e55c5', 500.00);

INSERT INTO "payment".credit_history (id, customer_id, amount, type)
    VALUES ('2627a8fc-dcc1-46bc-8243-0d6de764130d', '49f72dbd-ae1d-4f6b-b17c-122d402e55c5', 100.00, 'CREDIT');

INSERT INTO "payment".credit_history (id, customer_id, amount, type)
    VALUES ('860f39dc-ba3b-4e69-8e60-8c4353859550', '49f72dbd-ae1d-4f6b-b17c-122d402e55c5', 600.00, 'CREDIT');

INSERT INTO "payment".credit_history (id, customer_id, amount, type)
    VALUES ('97b5ec06-6f08-48f3-8050-4f97b6f60b76', '49f72dbd-ae1d-4f6b-b17c-122d402e55c5', 200.00, 'DEBIT');

INSERT INTO "payment".credit_entry (id, customer_id, total_credit_amount)
    VALUES ('3fe51933-55e4-43e1-bb34-501590978b4b', '64aaf362-a155-45e3-9b78-a8bedcc7bbdf', 100.00);

INSERT INTO "payment".credit_history (id, customer_id, amount, type)
    VALUES ('4f5a5b86-ecd5-4fc9-8451-1b95edd1997b', '64aaf362-a155-45e3-9b78-a8bedcc7bbdf', 100.00, 'CREDIT');