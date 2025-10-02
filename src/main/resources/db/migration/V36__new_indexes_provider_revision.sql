CREATE INDEX IF NOT EXISTS provider_id_revision_id_idx ON mst.payment_data USING BTREE (payment_provider_id, payment_domain_revision);
