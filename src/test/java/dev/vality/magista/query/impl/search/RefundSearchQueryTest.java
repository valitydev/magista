package dev.vality.magista.query.impl.search;

import dev.vality.damsel.merch_stat.StatRequest;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.config.testconfiguration.QueryProcessorConfig;
import dev.vality.magista.query.QueryProcessor;
import dev.vality.magista.util.DamselUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PostgresqlSpringBootITest
@Import(QueryProcessorConfig.class)
@Sql("classpath:data/sql/search/refund_search_data.sql")
public class RefundSearchQueryTest {

    @Autowired
    private QueryProcessor<StatRequest, StatResponse> queryProcessor;

    @Test
    public void testRefunds() {
        String json =
                "{'query': {'refunds': {'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(3, statResponse.getData().getRefunds().size());
        DamselUtil.toJson(statResponse);
    }

    @Test
    public void testRefundsWithoutMerchantAndShopId() {
        String json = "{'query': {'refunds': {'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-26T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(4, statResponse.getData().getRefunds().size());
        DamselUtil.toJson(statResponse);
    }

    @Test
    public void testExternalFailure() {
        String json =
                "{'query': {'refunds': {'merchant_id': 'PARTY_ID_1','shop_id': 'SHOP_ID_1', 'refund_status': 'failed', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-28T18:10:10Z', 'size':'1'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(statResponse.getData().getRefunds().stream().allMatch(
                refund -> refund.getStatus().isSetFailed()
                        && refund.getStatus().getFailed().getFailure().isSetFailure()
        ));
        DamselUtil.toJson(statResponse);
    }

    @Test
    public void testSearchByInvoiceIds() {
        String json = "{'query': {'refunds': {'invoice_ids': ['INVOICE_ID_1', 'INVOICE_ID_2', 'INVOICE_ID']}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getRefunds().size());
    }

}
