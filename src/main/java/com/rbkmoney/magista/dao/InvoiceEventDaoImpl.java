package com.rbkmoney.magista.dao;

import com.rbkmoney.magista.domain.enums.InvoiceEventCategory;
import com.rbkmoney.magista.domain.tables.pojos.InvoiceEventStat;
import com.rbkmoney.magista.exception.DaoException;
import org.jooq.Condition;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import static com.rbkmoney.magista.domain.Tables.INVOICE_EVENT_STAT;

/**
 * Created by tolkonepiu on 26/05/2017.
 */
public class InvoiceEventDaoImpl extends AbstractDao implements InvoiceEventDao {

    public static final RowMapper<InvoiceEventStat> ROW_MAPPER = new RecordRowMapper<>(INVOICE_EVENT_STAT, InvoiceEventStat.class);

    public InvoiceEventDaoImpl(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Long getLastEventId() throws DaoException {
        Query query = getDslContext().select(INVOICE_EVENT_STAT.EVENT_ID.max()).from(INVOICE_EVENT_STAT);
        return fetchOne(query, Long.class);
    }

    @Override
    public InvoiceEventStat findAdjustmentByIds(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Condition condition = INVOICE_EVENT_STAT.INVOICE_ID.eq(invoiceId)
                .and(INVOICE_EVENT_STAT.PAYMENT_ID.eq(paymentId))
                .and(INVOICE_EVENT_STAT.PAYMENT_ADJUSTMENT_ID.eq(adjustmentId))
                .and(INVOICE_EVENT_STAT.EVENT_CATEGORY.eq(InvoiceEventCategory.ADJUSTMENT));
        Query query = getDslContext().selectFrom(INVOICE_EVENT_STAT)
                .where(INVOICE_EVENT_STAT.ID.eq(
                        getDslContext().select(DSL.max(INVOICE_EVENT_STAT.ID))
                                .from(INVOICE_EVENT_STAT).where(condition)
                        )
                );
        return fetchOne(query, ROW_MAPPER);
    }

    @Override
    public InvoiceEventStat findRefundByIds(String invoiceId, String paymentId, String refundId) throws DaoException {
        Condition condition = INVOICE_EVENT_STAT.INVOICE_ID.eq(invoiceId)
                .and(INVOICE_EVENT_STAT.PAYMENT_ID.eq(paymentId))
                .and(INVOICE_EVENT_STAT.PAYMENT_REFUND_ID.eq(refundId))
                .and(INVOICE_EVENT_STAT.EVENT_CATEGORY.eq(InvoiceEventCategory.REFUND));
        Query query = getDslContext().selectFrom(INVOICE_EVENT_STAT)
                .where(INVOICE_EVENT_STAT.ID.eq(
                        getDslContext().select(DSL.max(INVOICE_EVENT_STAT.ID))
                                .from(INVOICE_EVENT_STAT).where(condition)
                        )
                );
        return fetchOne(query, ROW_MAPPER);
    }

    @Override
    public InvoiceEventStat findPaymentByIds(String invoiceId, String paymentId) throws DaoException {
        Condition condition = INVOICE_EVENT_STAT.INVOICE_ID.eq(invoiceId)
                .and(INVOICE_EVENT_STAT.PAYMENT_ID.eq(paymentId))
                .and(INVOICE_EVENT_STAT.EVENT_CATEGORY.eq(InvoiceEventCategory.PAYMENT));

        Query query = getDslContext().selectFrom(INVOICE_EVENT_STAT)
                .where(condition
                        .and(INVOICE_EVENT_STAT.ID.eq(
                                getDslContext().select(DSL.max(INVOICE_EVENT_STAT.ID))
                                        .from(INVOICE_EVENT_STAT).where(condition)
                                )
                        )
                );
        return fetchOne(query, ROW_MAPPER);
    }

    @Override
    public InvoiceEventStat findInvoiceById(String invoiceId) throws DaoException {
        Condition condition = INVOICE_EVENT_STAT.INVOICE_ID.eq(invoiceId)
                .and(INVOICE_EVENT_STAT.EVENT_CATEGORY.eq(InvoiceEventCategory.INVOICE));
        Query query = getDslContext().selectFrom(INVOICE_EVENT_STAT)
                .where(INVOICE_EVENT_STAT.ID.eq(
                        getDslContext().select(DSL.max(INVOICE_EVENT_STAT.ID))
                                .from(INVOICE_EVENT_STAT).where(condition)
                        )
                );
        return fetchOne(query, ROW_MAPPER);
    }

    @Override
    public void insert(InvoiceEventStat invoiceEventStat) throws DaoException {
        Query query = getDslContext().insertInto(INVOICE_EVENT_STAT)
                .set(getDslContext().newRecord(INVOICE_EVENT_STAT, invoiceEventStat));

        executeOne(query);
    }

}
