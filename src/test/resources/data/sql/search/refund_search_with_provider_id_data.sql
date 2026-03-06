insert into mst.refund_data (event_id, event_created_at, event_type, party_id, party_shop_id, invoice_id, payment_id, refund_id, refund_status,  refund_amount, refund_currency_code, refund_created_at, provider_id)
values (1, now(), 'INVOICE_PAYMENT_REFUND_CREATED', 'PARTY_ID_1', 'SHOP_ID_1', 'INVOICE_ID_1', '1', '1', 'pending', 5, 'RUB', now(), 1);

insert into mst.refund_data (event_id, event_created_at, event_type, party_id, party_shop_id, invoice_id, payment_id, refund_id, refund_status,  refund_amount, refund_currency_code, refund_created_at, provider_id)
values (2, now(), 'INVOICE_PAYMENT_REFUND_CREATED', 'PARTY_ID_1', 'SHOP_ID_1', 'INVOICE_ID_2', '1', '1', 'pending', 5, 'RUB', now(), 2);