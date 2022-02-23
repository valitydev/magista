package dev.vality.magista.query.impl.statistics;

import dev.vality.damsel.merch_stat.StatRequest;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.config.testconfiguration.QueryProcessorConfig;
import dev.vality.magista.query.QueryProcessor;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
@Import(QueryProcessorConfig.class)
public class StatisticsQueryTest {

    @Autowired
    private QueryProcessor<StatRequest, StatResponse> queryProcessor;

    @Test
    @Sql("classpath:data/sql/statistics/payments_geo_stat_data.sql")
    public void testPaymentsGeoStat() {
        String json =
                "{'query': {'payments_geo_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getRecords().size());
        assertEquals(0, statResponse.getTotalCount());
    }

    @Test
    @Sql("classpath:data/sql/statistics/payments_with_empty_geo_stat_data.sql")
    public void testPaymentsWithEmptyGeoStat() throws TException {
        String json =
                "{'query': {'payments_geo_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(0, statResponse.getData().getRecords().size());
        assertEquals(0, statResponse.getTotalCount());
        assertEquals("{\"data\":{\"records\":[]}}",
                new TSerializer(new TSimpleJSONProtocol.Factory()).toString(statResponse));
    }

    @Test
    @Sql("classpath:data/sql/statistics/payments_pmt_cards_stat_data.sql")
    public void testPaymentsCardTypesStat() {
        String json =
                "{'query': {'payments_pmt_cards_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getRecords().size());
        assertEquals(0, statResponse.getTotalCount());
    }

    @Test
    @Sql("classpath:data/sql/statistics/payments_with_empty_pmt_cards_stat_data.sql")
    public void testPaymentsCardTypeWithEmptyPaymentSystem() throws TException {
        String json =
                "{'query': {'payments_pmt_cards_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(0, statResponse.getData().getRecords().size());
        assertEquals(0, statResponse.getTotalCount());
        assertEquals("{\"data\":{\"records\":[]}}",
                new TSerializer(new TSimpleJSONProtocol.Factory()).toString(statResponse));
    }

    @Test
    @Sql("classpath:data/sql/statistics/customers_rate_stat_data.sql")
    public void testCustomersRateStat() {
        String json =
                "{'query': {'customers_rate_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getRecords().size());
        assertEquals("2", statResponse.getData().getRecords().get(0).get("unic_count"));
        assertEquals(0, statResponse.getTotalCount());
    }

    @Test
    @Sql("classpath:data/sql/statistics/customers_with_empty_rate_stat_data.sql")
    public void testCustomersRateStatWithEmptyFingerprint() {
        String json =
                "{'query': {'customers_rate_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(0, statResponse.getData().getRecords().size());
        assertEquals(0, statResponse.getTotalCount());
    }

    @Test
    @Sql("classpath:data/sql/statistics/payments_turnover_stat_data.sql")
    public void testPaymentsTurnover() {
        String json =
                "{'query': {'payments_turnover': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60', 'from':'1', 'size':'2'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getRecords().size());
        assertEquals("50000", statResponse.getData().getRecords().get(0).get("amount_without_fee"));
        assertEquals("49965", statResponse.getData().getRecords().get(0).get("amount_with_fee"));
        assertEquals(0, statResponse.getTotalCount());
    }

    @Test
    @Disabled
    @Sql("classpath:data/sql/statistics/payments_conversion_stat_data.sql")
    public void testPaymentsConversionStat() {
        String json =
                "{'query': {'payments_conversion_stat': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'split_interval':'60'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getRecords().size());
        assertEquals("1", statResponse.getData().getRecords().get(0).get("successful_count"));
        assertEquals("2", statResponse.getData().getRecords().get(0).get("total_count"));
        assertEquals("0.5", statResponse.getData().getRecords().get(0).get("conversion"));
        assertEquals(0, statResponse.getTotalCount());
    }

}
