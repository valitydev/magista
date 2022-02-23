package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.AdjustmentData;

import java.util.List;

public interface AdjustmentDao {

    AdjustmentData get(String invoiceId, String paymentId, String adjustmentId);

    void save(List<AdjustmentData> adjustments);

}
