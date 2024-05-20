CREATE INDEX IF NOT EXISTS payment_data_party_shop_by_created_date_idx
    ON mst.payment_data USING BTREE (party_id, party_shop_id, payment_created_at);

CREATE INDEX IF NOT EXISTS invoice_data_party_shop_by_created_date_idx
    ON mst.invoice_data USING BTREE (party_id, party_shop_id, invoice_created_at);