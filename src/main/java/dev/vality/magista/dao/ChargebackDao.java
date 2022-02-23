package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.ChargebackData;

import java.util.List;

public interface ChargebackDao {

    ChargebackData get(String invoiceId, String paymentId, String chargebackId);

    void save(List<ChargebackData> chargebackDataList);

}
