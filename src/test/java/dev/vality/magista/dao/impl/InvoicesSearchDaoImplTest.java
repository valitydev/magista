package dev.vality.magista.dao.impl;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.*;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlSpringBootITest
@Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
public class InvoicesSearchDaoImplTest {

    @Autowired
    private SearchDaoImpl searchDao;

    @Test
    public void testInvoices() {
        InvoiceSearchQuery searchQuery = buildInvoicesSearch();
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        assertEquals(3, invoices.size());
    }

    @Test
    public void testIfNotPresentInvoices() {
        InvoiceSearchQuery searchQuery = new InvoiceSearchQuery()
                .setCommonSearchQueryParams(new CommonSearchQueryParams()
                        .setPartyId("6954b4d1-f39f-4cc1-8843-eae834e6f849")
                        .setShopIds(List.of("2"))
                        .setFromTime("2016-10-25T15:45:20Z")
                        .setToTime("3018-10-25T18:10:10Z")
                        .setLimit(2))
                .setInvoiceStatus(InvoiceStatus.paid)
                .setPaymentParams(new PaymentParams()
                        .setPaymentId("1")
                        .setPaymentStatus(dev.vality.magista.InvoicePaymentStatus.pending));
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        assertEquals(0, invoices.size());
    }

    @Test
    public void testOrderByEventIdInvoices() {
        InvoiceSearchQuery searchQuery = buildInvoicesSearch();
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        Instant instant = Instant.MAX;
        for (StatInvoice statInvoice : invoices) {
            Instant statInstant = Instant.from(TypeUtil.stringToTemporal(statInvoice.getCreatedAt()));
            assertTrue(statInstant.isBefore(instant));
            instant = statInstant;
        }
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_domain_revision_search_data.sql")
    public void testSearchByPaymentDomainRevision() {
        InvoiceSearchQuery searchQuery = buildInvoicesSearch();
        searchQuery.getPaymentParams().setPaymentDomainRevision(1);
        searchQuery.getCommonSearchQueryParams().setLimit(1);
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        assertEquals(0, invoices.size());

        searchQuery.getPaymentParams().setPaymentDomainRevision(2);
        invoices = searchDao.getInvoices(searchQuery);
        assertEquals(1, invoices.size());

        searchQuery.getPaymentParams().unsetPaymentDomainRevision();
        searchQuery.getPaymentParams().setFromPaymentDomainRevision(1);
        invoices = searchDao.getInvoices(searchQuery);
        assertEquals(1, invoices.size());

        searchQuery.getPaymentParams().unsetFromPaymentDomainRevision();
        searchQuery.getPaymentParams().setToPaymentDomainRevision(1);
        invoices = searchDao.getInvoices(searchQuery);
        assertEquals(0, invoices.size());

        searchQuery.getPaymentParams().setFromPaymentDomainRevision(1);
        searchQuery.getPaymentParams().setToPaymentDomainRevision(2);
        invoices = searchDao.getInvoices(searchQuery);
        assertEquals(1, invoices.size());
    }

    @Test
    @Sql("classpath:data/sql/search/recurrent_payments_search_data.sql")
    public void testRecurrentPayments() {
        InvoiceSearchQuery searchQuery = buildInvoicesSearch();
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        assertEquals(1, invoices.size());
    }

    @Test
    public void testSearchByBankCardPaymentToolAndPaymentId() {
        InvoiceSearchQuery searchQuery = buildInvoicesSearch();
        searchQuery.setInvoiceIds(List.of("INVOICE_ID_1"));
        searchQuery.getPaymentParams().setPaymentId("PAYMENT_ID_1");
        searchQuery.getPaymentParams().setPaymentTool(PaymentToolType.bank_card);
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        assertEquals(1, invoices.size());
    }

    private InvoiceSearchQuery buildInvoicesSearch() {
        return new InvoiceSearchQuery()
                .setCommonSearchQueryParams(new CommonSearchQueryParams()
                        .setPartyId("db79ad6c-a507-43ed-9ecf-3bbd88475b32")
                        .setShopIds(List.of("SHOP_ID"))
                        .setFromTime("2016-10-25T15:45:20Z")
                        .setToTime("3018-10-25T18:10:10Z"))
                .setPaymentParams(new PaymentParams());
    }
}