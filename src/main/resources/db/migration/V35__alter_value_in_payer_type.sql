create type payment_payer_type_new as enum ('payment_resource', 'recurrent');

ALTER TABLE payment_data
ALTER COLUMN payment_payer_type TYPE payment_payer_type_new
    USING payment_payer_type::text::payment_payer_type_new;

DROP TYPE payment_payer_type;