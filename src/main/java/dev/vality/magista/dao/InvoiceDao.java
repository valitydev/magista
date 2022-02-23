package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.InvoiceData;

import java.util.List;

public interface InvoiceDao {

    InvoiceData get(String invoiceId);

    void insert(List<InvoiceData> invoices);

    void update(List<InvoiceData> invoices);

}
