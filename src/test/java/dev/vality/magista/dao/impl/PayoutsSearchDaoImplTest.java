package dev.vality.magista.dao.impl;

import dev.vality.magista.*;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
@Sql("classpath:data/sql/search/payouts_search_data.sql")
public class PayoutsSearchDaoImplTest {

    @Autowired
    private SearchDaoImpl searchDao;

    @Test
    public void testPayouts() {
        PayoutSearchQuery payoutSearchQuery = buildPayoutSearchQuery();
        List<StatPayout> payouts = searchDao.getPayouts(payoutSearchQuery);
        assertEquals(4, payouts.size());
    }


    @Test
    public void shouldFilterByBankAccount() {
        PayoutSearchQuery payoutSearchQuery = buildPayoutSearchQuery();
        payoutSearchQuery.setPayoutType(PayoutToolType.payout_account);
        List<StatPayout> payouts = searchDao.getPayouts(payoutSearchQuery);
        assertEquals(2, payouts.size());
    }

    @Test
    public void shouldFilterByWalletInfo() {
        PayoutSearchQuery payoutSearchQuery = buildPayoutSearchQuery();
        payoutSearchQuery.setPayoutType(PayoutToolType.wallet);
        List<StatPayout> payouts = searchDao.getPayouts(payoutSearchQuery);
        assertEquals(1, payouts.size());
    }

    @Test
    public void shouldFilterByPaymentInstitutionAccount() {
        PayoutSearchQuery payoutSearchQuery = buildPayoutSearchQuery();
        payoutSearchQuery.setPayoutType(PayoutToolType.payment_institution_account);
        List<StatPayout> payouts = searchDao.getPayouts(payoutSearchQuery);
        assertEquals(1, payouts.size());
    }

    @Test
    public void shouldFilterByUnpaid() {
        PayoutSearchQuery payoutSearchQuery = buildPayoutSearchQuery();
        payoutSearchQuery.setPayoutStatusTypes(List.of(PayoutStatusType.unpaid));
        List<StatPayout> payouts = searchDao.getPayouts(payoutSearchQuery);
        assertEquals(2, payouts.size());
    }

    private PayoutSearchQuery buildPayoutSearchQuery() {
        return new PayoutSearchQuery()
                .setCommonSearchQueryParams(new CommonSearchQueryParams()
                        .setPartyId("PARTY_ID_1")
                        .setShopIds(List.of("SHOP_ID_1"))
                        .setFromTime("2016-10-25T15:45:20Z")
                        .setToTime("3018-10-25T18:10:10Z"));
    }

}
