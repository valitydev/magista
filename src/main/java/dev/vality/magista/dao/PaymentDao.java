package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.PaymentData;

import java.util.List;

public interface PaymentDao {

    PaymentData get(String invoiceId, String paymentId);

    void insert(List<PaymentData> payments);

    void update(List<PaymentData> payments);

}
