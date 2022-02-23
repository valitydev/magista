package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.InvoiceTemplateDao;
import dev.vality.magista.dao.impl.mapper.RecordRowMapper;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import dev.vality.magista.domain.tables.records.InvoiceTemplateRecord;
import dev.vality.magista.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.magista.domain.tables.InvoiceTemplate.INVOICE_TEMPLATE;

@Component
public class InvoiceTemplateDaoImpl extends AbstractDao implements InvoiceTemplateDao {

    private final RowMapper<InvoiceTemplate> invoiceTemplateRowMapper;

    public InvoiceTemplateDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.invoiceTemplateRowMapper = new RecordRowMapper<>(INVOICE_TEMPLATE, InvoiceTemplate.class);
    }

    @Override
    public InvoiceTemplate get(String invoiceTemplateId) throws DaoException {
        Query query = getDslContext().selectFrom(INVOICE_TEMPLATE)
                .where(INVOICE_TEMPLATE.INVOICE_TEMPLATE_ID.eq(invoiceTemplateId));
        return fetchOne(query, invoiceTemplateRowMapper);
    }

    @Override
    public void save(List<InvoiceTemplate> invoiceTemplates) throws DaoException {
        List<Query> queries = invoiceTemplates.stream()
                .map(this::buildInvoiceTemplateRecord)
                .map(invoiceTemplateRecord ->
                        getDslContext().insertInto(INVOICE_TEMPLATE)
                                .set(invoiceTemplateRecord)
                                .onConflict(INVOICE_TEMPLATE.INVOICE_TEMPLATE_ID)
                                .doUpdate()
                                .set(invoiceTemplateRecord)
                                .where(INVOICE_TEMPLATE.EVENT_ID.lessOrEqual(invoiceTemplateRecord.getEventId())))
                .collect(Collectors.toList());
        batchExecute(queries);
    }

    private InvoiceTemplateRecord buildInvoiceTemplateRecord(InvoiceTemplate invoiceTemplate) {
        InvoiceTemplateRecord invoiceTemplateRecord = getDslContext().newRecord(INVOICE_TEMPLATE, invoiceTemplate);
        invoiceTemplateRecord.changed(true);
        invoiceTemplateRecord.changed(INVOICE_TEMPLATE.ID, invoiceTemplateRecord.getId() != null);
        return invoiceTemplateRecord;
    }
}
