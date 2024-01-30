CREATE INDEX payment_data_failure_index
    on mst.payment_data (payment_external_failure, payment_external_failure_reason);