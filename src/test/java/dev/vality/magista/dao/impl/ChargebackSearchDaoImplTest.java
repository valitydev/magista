package dev.vality.magista.dao.impl;

import dev.vality.damsel.domain.*;
import dev.vality.magista.ChargebackSearchQuery;
import dev.vality.magista.CommonSearchQueryParams;
import dev.vality.magista.StatChargeback;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
class ChargebackSearchDaoImplTest {

    @Autowired
    private SearchDaoImpl searchDao;

    @Test
    @Sql("classpath:data/sql/search/chargeback_search_data.sql")
    void chargebackSearchTest() {
        ChargebackSearchQuery chargebackSearchQuery = new ChargebackSearchQuery()
                .setCommonSearchQueryParams(new CommonSearchQueryParams()
                        .setPartyId("party_id_1")
                        .setShopIds(List.of("party_shop_id_1"))
                        .setFromTime("2016-10-25T15:45:20Z")
                        .setToTime("3018-10-25T18:10:10Z"));
        List<StatChargeback> chargebacks = searchDao.getChargebacks(chargebackSearchQuery);
        assertEquals(4, chargebacks.size());

        chargebackSearchQuery
                .setChargebackCategories(List.of(
                        InvoicePaymentChargebackCategory.fraud(new InvoicePaymentChargebackCategoryFraud()),
                        InvoicePaymentChargebackCategory.dispute(new InvoicePaymentChargebackCategoryDispute())));

        chargebacks = searchDao.getChargebacks(chargebackSearchQuery);
        assertEquals(2, chargebacks.size());

        chargebackSearchQuery.unsetChargebackCategories();
        chargebackSearchQuery.setChargebackStatuses(List.of(
                InvoicePaymentChargebackStatus.pending(new InvoicePaymentChargebackPending()),
                InvoicePaymentChargebackStatus.cancelled(new InvoicePaymentChargebackCancelled())
        ));

        chargebacks = searchDao.getChargebacks(chargebackSearchQuery);
        assertEquals(3, chargebacks.size());

        chargebackSearchQuery.unsetChargebackStatuses();
        chargebackSearchQuery.setChargebackStages(List.of(
                InvoicePaymentChargebackStage.chargeback(new InvoicePaymentChargebackStageChargeback()),
                InvoicePaymentChargebackStage.arbitration(new InvoicePaymentChargebackStageArbitration())
        ));

        chargebacks = searchDao.getChargebacks(chargebackSearchQuery);
        assertEquals(2, chargebacks.size());

        chargebackSearchQuery.unsetChargebackStatuses();
        chargebackSearchQuery.unsetChargebackStages();
        chargebackSearchQuery
                .setChargebackCategories(List.of(
                        InvoicePaymentChargebackCategory.system_set(new InvoicePaymentChargebackCategorySystemSet())));

        chargebacks = searchDao.getChargebacks(chargebackSearchQuery);
        assertEquals(1, chargebacks.size());


    }
}
