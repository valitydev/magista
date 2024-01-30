DROP INDEX IF EXISTS payment_data_failure_index;

CREATE INDEX IF NOT EXISTS payment_data_payment_external_failure_by_created_date_idx
    ON mst.payment_data USING BTREE (party_id, payment_external_failure, payment_external_failure_reason, payment_created_at)
    WHERE payment_external_failure IS NOT NULL or payment_external_failure_reason IS NOT NULL;