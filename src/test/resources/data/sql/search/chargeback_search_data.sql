INSERT INTO mst.chargeback_data(
	event_id, event_created_at, event_type, invoice_id, payment_id, chargeback_id,
	party_id, party_shop_id, chargeback_status, chargeback_created_at, chargeback_reason, chargeback_reason_category,
	chargeback_domain_revision, chargeback_party_revision, chargeback_levy_amount, chargeback_levy_currency_code,
	chargeback_amount, chargeback_currency_code, chargeback_fee, chargeback_provider_fee, chargeback_external_fee,
	chargeback_stage, chargeback_context, external_id)
	VALUES (1, now(), 'INVOICE_PAYMENT_CHARGEBACK_CREATED', 'invoice_id_1', 'payment_id_1', 'chargeback_id_1',
	'party_id_1', 'party_shop_id_1', 'pending', now(), 'some reason', 'fraud',
	1, 1, 1000, 'RUB', 1000, 'USD', 10, 10, 10, 'chargeback', null, 'ext_1');

INSERT INTO mst.chargeback_data(
	event_id, event_created_at, event_type, invoice_id, payment_id, chargeback_id,
	party_id, party_shop_id, chargeback_status, chargeback_created_at, chargeback_reason, chargeback_reason_category,
	chargeback_domain_revision, chargeback_party_revision, chargeback_levy_amount, chargeback_levy_currency_code,
	chargeback_amount, chargeback_currency_code, chargeback_fee, chargeback_provider_fee, chargeback_external_fee,
	chargeback_stage, chargeback_context, external_id)
	VALUES (2, now(), 'INVOICE_PAYMENT_CHARGEBACK_CREATED', 'invoice_id_2', 'payment_id_1', 'chargeback_id_1',
	'party_id_1', 'party_shop_id_1', 'accepted', now(), 'some reason', 'dispute',
	1, 1, 1000, 'RUB', 1000, 'USD', 10, 10, 10, 'arbitration', null, 'ext_2');

INSERT INTO mst.chargeback_data(
	event_id, event_created_at, event_type, invoice_id, payment_id, chargeback_id,
	party_id, party_shop_id, chargeback_status, chargeback_created_at, chargeback_reason, chargeback_reason_category,
	chargeback_domain_revision, chargeback_party_revision, chargeback_levy_amount, chargeback_levy_currency_code,
	chargeback_amount, chargeback_currency_code, chargeback_fee, chargeback_provider_fee, chargeback_external_fee,
	chargeback_stage, chargeback_context, external_id)
	VALUES (3, now(), 'INVOICE_PAYMENT_CHARGEBACK_CREATED', 'invoice_id_3', 'payment_id_1', 'chargeback_id_1',
	'party_id_1', 'party_shop_id_1', 'cancelled', now(), 'some reason', 'authorisation',
	1, 1, 1000, 'RUB', 1000, 'USD', 10, 10, 10, 'pre_arbitration', null, 'ext_3');

INSERT INTO mst.chargeback_data(
    event_id, event_created_at, event_type, invoice_id, payment_id, chargeback_id,
    party_id, party_shop_id, chargeback_status, chargeback_created_at, chargeback_reason, chargeback_reason_category,
    chargeback_domain_revision, chargeback_party_revision, chargeback_levy_amount, chargeback_levy_currency_code,
    chargeback_amount, chargeback_currency_code, chargeback_fee, chargeback_provider_fee, chargeback_external_fee,
    chargeback_stage, chargeback_context, external_id)
VALUES (3, now(), 'INVOICE_PAYMENT_CHARGEBACK_CREATED', 'invoice_id_4', 'payment_id_1', 'chargeback_id_1',
        'party_id_1', 'party_shop_id_1', 'pending', now(), 'some reason', 'system_set',
        1, 1, 1000, 'RUB', 1000, 'USD', 10, 10, 10, 'pre_arbitration', null, 'ext_3');
