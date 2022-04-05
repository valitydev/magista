package dev.vality.magista.query.impl.search;

import dev.vality.damsel.merch_stat.*;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.config.testconfiguration.QueryProcessorConfig;
import dev.vality.magista.exception.BadTokenException;
import dev.vality.magista.query.QueryProcessor;
import dev.vality.magista.query.parser.QueryParserException;
import dev.vality.magista.util.DamselUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;

import static dev.vality.damsel.merch_stat.TerminalPaymentProvider.euroset;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

@PostgresqlSpringBootITest
@Import(QueryProcessorConfig.class)
public class PaymentSearchQueryTest {

    @Autowired
    private QueryProcessor<StatRequest, StatResponse> queryProcessor;

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testPayments() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(3, statResponse.getData().getPayments().size());
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testPaymentAdditionalInfoWithInvoiceSearch() {
        String json =
                "{'query': {'invoices': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'payment_rrn': '43253', 'payment_approval_code': '5324'}}}";
        final StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getInvoices().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testPaymentAdditionalInfo() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32', 'payment_rrn': '43253', 'payment_approval_code': '5324'}}}";
        final StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testPaymentExcludeField() {
        String json =
                "{'query': {'payments': {'merchant_id': 'b9c3bf7f-f62a-4675-8489-2da7775024bb', 'from_time': '2016-10-25T15:45:20Z', 'to_time': '3018-10-25T18:10:10Z', 'exclude': {'shop_id': ['6789']}}}}";
        final StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_amount_from_to.sql")
    public void testPaymentFromTo() {
        String json =
                "{'query':{'payments':{'exclude':{'shop_id':['']},'from_time':'2019-12-31T21:00:00Z', 'to_time': '3018-10-25T18:10:10Z', 'payment_amount_from':10000,'payment_amount_to':30000}},'size':20}";
        final StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(3, statResponse.getData().getPayments().size());
    }

    @Test
    public void testWhenSizeOverflow() {
        String json = "{'query': {'payments': {'size': 1001}}}";
        assertThrows(
                QueryParserException.class,
                () -> queryProcessor.processQuery(new StatRequest(json)));
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_provider_and_terminal_id.sql")
    public void testPaymentSearchByTerminalId() {
        StatResponse statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_terminal_id': 120}}}")
        );
        assertEquals(1, statResponse.getData().getPayments().size());

        statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_terminal_id': 111}}}")
        );
        assertTrue(statResponse.getData().getPayments().isEmpty());
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_provider_and_terminal_id.sql")
    public void testPaymentSearchByProviderId() {
        StatResponse statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_provider_id': 115}}}")
        );
        assertEquals(1, statResponse.getData().getPayments().size());

        statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_provider_id': 111}}}")
        );
        assertTrue(statResponse.getData().getPayments().isEmpty());
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_provider_and_terminal_id.sql")
    public void testPaymentSearchByProviderAndTerminalId() {
        StatResponse statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_terminal_id': 120, 'payment_provider_id': 115}}}")
        );
        assertEquals(1, statResponse.getData().getPayments().size());

        statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_terminal_id': 120, 'payment_provider_id': 111}}}")
        );
        assertTrue(statResponse.getData().getPayments().isEmpty());

        statResponse = queryProcessor.processQuery(
                new StatRequest("{'query': {'payments': {'payment_terminal_id': 111, 'payment_provider_id': 115}}}")
        );
        assertTrue(statResponse.getData().getPayments().isEmpty());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testContinuationTokenWithPayments() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'size': 1}}}";
        StatRequest statRequest = new StatRequest(json);
        StatResponse statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());
        assertNotNull(statResponse.getContinuationToken());
        DamselUtil.toJson(statResponse);

        statRequest.setContinuationToken(statResponse.getContinuationToken());
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());
        assertNotNull(statResponse.getContinuationToken());

        statRequest.setContinuationToken(statResponse.getContinuationToken());
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());

        statRequest.setContinuationToken(statResponse.getContinuationToken());
        statResponse = queryProcessor.processQuery(statRequest);

        assertNull(statResponse.getContinuationToken());
    }

    @Test
    public void testBadToken() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': '6789','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatRequest statRequest = new StatRequest(json);
        statRequest.setContinuationToken("3gOc9TNJDkE1dOoK5oy6bJvgShunXxk2rTZuCn3SBts=1560155771740");
        assertThrows(
                BadTokenException.class,
                () -> queryProcessor.processQuery(statRequest));
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testOrderByEventIdPayments() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(3, statResponse.getData().getPayments().size());
        Instant instant = Instant.MAX;
        for (StatPayment statPayment : statResponse.getData().getPayments()) {
            Instant statInstant = Instant.from(TypeUtil.stringToTemporal(statPayment.getCreatedAt()));
            assertTrue(statInstant.isBefore(instant));
            instant = statInstant;
        }
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_operation_timeout_search_data.sql")
    public void testOperationTimeout() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-28T18:10:10Z', 'payment_status': 'failed', 'size':'1'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(statResponse.getData().getPayments().stream().allMatch(
                payment -> payment.getStatus().isSetFailed()
                        && payment.getStatus().getFailed().getFailure().isSetOperationTimeout()
        ));
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_holds_search_data.sql")
    public void testHoldAndInstantFlow() {
        //capture
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'payment_id': 'PAYMENT_ID_1', 'payment_flow': 'hold', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(
                statResponse.getData().getPayments().stream()
                        .allMatch(
                                payment -> payment.getFlow().isSetHold()
                                        && payment.getFlow().getHold().getOnHoldExpiration()
                                        .equals(OnHoldExpiration.capture)
                        )
        );
        DamselUtil.toJson(statResponse);

        //cancel
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'payment_id': 'PAYMENT_ID_2', 'payment_flow': 'hold', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(
                statResponse.getData().getPayments().stream()
                        .allMatch(
                                payment -> payment.getFlow().isSetHold()
                                        && payment.getFlow().getHold().getOnHoldExpiration()
                                        .equals(OnHoldExpiration.cancel)
                        )
        );
        DamselUtil.toJson(statResponse);

        //instant
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'payment_id': 'PAYMENT_ID_3', 'payment_flow': 'instant', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(
                statResponse.getData().getPayments().stream()
                        .allMatch(
                                payment -> payment.getFlow().isSetInstant()
                        )
        );
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testPaymentsWithoutMerchantAndShopId() {
        String json =
                "{'query': {'payments': {'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-26T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(6, statResponse.getData().getPayments().size());
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_terminal_provider_search_data.sql")
    public void testFindByPaymentMethodAndTerminalProvider() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'payment_method': 'payment_terminal', 'payment_terminal_provider':'euroset', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-26T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
        assertEquals(euroset,
                statResponse.getData().getPayments().get(0).getPayer().getPaymentResource().getPaymentTool()
                        .getPaymentTerminal().getTerminalTypeDeprecated());
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_customer_id_search_data.sql")
    public void testFindByCustomerId() {
        String json =
                "{'query': {'payments': {'payment_customer_id': 'test', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-26T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
        assertEquals("test", statResponse.getData().getPayments().get(0).getPayer().getCustomer().getCustomerId());
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_external_failure_search_data.sql")
    public void testExternalFailure() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'payment_status': 'failed', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-28T18:10:10Z', 'size':'1'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertTrue(statResponse.getData().getPayments().stream().allMatch(
                payment -> payment.getStatus().isSetFailed()
                        && payment.getStatus().getFailed().getFailure().isSetFailure()
        ));
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/payment_with_domain_revision_search_data.sql")
    public void testSearchByPaymentDomainRevision() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'payment_domain_revision': 2, 'size': 1}}}";
        StatRequest statRequest = new StatRequest(json);
        StatResponse statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'payment_domain_revision': 1, 'size': 1}}}";
        statRequest = new StatRequest(json);
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(0, statResponse.getData().getPayments().size());
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'from_payment_domain_revision': 1, 'size': 1}}}";
        statRequest = new StatRequest(json);
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'to_payment_domain_revision': 1, 'size': 1}}}";
        statRequest = new StatRequest(json);
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(0, statResponse.getData().getPayments().size());
        json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'from_payment_domain_revision': 1, 'to_payment_domain_revision': 2, 'size': 1}}}";
        statRequest = new StatRequest(json);
        statResponse = queryProcessor.processQuery(statRequest);
        assertEquals(1, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/recurrent_payments_search_data.sql")
    public void testRecurrentPayments() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_id': 'SHOP_ID', 'invoice_id': 'INVOICE_ID_1', 'payment_id': 'PAYMENT_ID_1'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());

        StatPayment statPayment = statResponse.getData().getPayments().get(0);
        assertTrue(statPayment.isMakeRecurrent());
        assertTrue(statPayment.isSetPayer());
        assertTrue(statPayment.getPayer().isSetRecurrent());
        RecurrentPayer recurrentPayer = statPayment.getPayer().getRecurrent();
        assertEquals("PARENT_INVOICE_ID_1", recurrentPayer.getRecurrentParent().getInvoiceId());
        assertEquals("PARENT_PAYMENT_ID_1", recurrentPayer.getRecurrentParent().getPaymentId());
        DamselUtil.toJson(statResponse);
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testSearchByShopIds() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','shop_ids': ['SHOP_ID'],'from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(3, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testSearchByInvoiceIds() {
        String json = "{'query': {'payments': {'invoice_ids': ['INVOICE_NEW_ID_3', 'INVOICE_NEW_ID_4']}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(2, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testSearchByBankCardTokenProvider() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'payment_token_provider': 'applepay'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
    }

    @Test
    @Sql("classpath:data/sql/search/invoice_and_payment_search_data.sql")
    public void testSearchByBankCardPaymentSystem() {
        String json =
                "{'query': {'payments': {'merchant_id': 'db79ad6c-a507-43ed-9ecf-3bbd88475b32','from_time': '2016-10-25T15:45:20Z','to_time': '3018-10-25T18:10:10Z', 'payment_system': 'mastercard'}}}";
        StatResponse statResponse = queryProcessor.processQuery(new StatRequest(json));
        assertEquals(1, statResponse.getData().getPayments().size());
    }

}
