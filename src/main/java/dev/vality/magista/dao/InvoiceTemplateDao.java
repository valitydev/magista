package dev.vality.magista.dao;

import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import dev.vality.magista.exception.DaoException;

import java.util.List;

public interface InvoiceTemplateDao {

    InvoiceTemplate get(String invoiceTemplateId) throws DaoException;

    void save(List<InvoiceTemplate> invoiceTemplates) throws DaoException;

}
