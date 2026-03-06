ALTER TABLE mst.refund_data
    ADD COLUMN provider_id integer;

ALTER TABLE mst.adjustment_data
    ADD COLUMN provider_id integer;

ALTER TABLE mst.chargeback_data
    ADD COLUMN provider_id integer;