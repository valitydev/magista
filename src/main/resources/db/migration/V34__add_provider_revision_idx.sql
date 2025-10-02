CREATE INDEX CONCURRENTLY IF NOT EXISTS provider_id_revision_id_idx
    ON mst.payment_data USING BTREE (invoice_id, payment_id, payment_provider_id, payment_domain_revision);