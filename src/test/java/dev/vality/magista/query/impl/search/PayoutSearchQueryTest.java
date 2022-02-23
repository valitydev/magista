package dev.vality.magista.query.impl.search;

import dev.vality.damsel.merch_stat.StatRequest;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.config.testconfiguration.QueryProcessorConfig;
import dev.vality.magista.query.QueryProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
@Import(QueryProcessorConfig.class)
@Sql("classpath:data/sql/search/payouts_search_data.sql")
public class PayoutSearchQueryTest {

    @Autowired
    private QueryProcessor<StatRequest, StatResponse> queryProcessor;

    @Test
    public void testPayouts() {
        String json =
                "{'query': {'payouts': " +
                        "{'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1','from_time': '2016-10-25T15:45:20Z'," +
                        "'to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(4, statResponse.getData().getPayouts().size());
    }

    @Test
    public void shouldFilterByBankAccount() {
        String json =
                "{'query': {'payouts': " +
                        "{'payout_type': 'bank_account'," +
                        "'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1'," +
                        "'from_time': '2016-10-25T15:45:20Z'," +
                        "'to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getPayouts().size());
    }

    @Test
    public void shouldFilterByWalletInfo() {
        String json =
                "{'query': {'payouts': " +
                        "{'payout_type': 'wallet_info'," +
                        "'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1'," +
                        "'from_time': '2016-10-25T15:45:20Z'," +
                        "'to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayouts().size());
    }

    @Test
    public void shouldFilterByPaymentInstitutionAccount() {
        String json =
                "{'query': {'payouts': " +
                        "{'payout_type': 'payment_institution_account'," +
                        "'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1'," +
                        "'from_time': '2016-10-25T15:45:20Z'," +
                        "'to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayouts().size());
    }

    @Test
    public void shouldFilterByUnpaid() {
        String json =
                "{'query': {'payouts': " +
                        "{'payout_status': 'unpaid'," +
                        "'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1'," +
                        "'from_time': '2016-10-25T15:45:20Z'," +
                        "'to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getPayouts().size());
    }
}
