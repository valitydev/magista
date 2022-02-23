package dev.vality.magista.query.impl.parser;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.query.impl.*;
import dev.vality.magista.query.parser.QueryParserException;
import dev.vality.magista.query.parser.QueryPart;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by vpankrashkin on 26.08.16.
 */
public class JsonQueryParserTest {

    JsonQueryParser parser = JsonQueryParser.newWeakJsonQueryParser();

    @Test
    public void testNoFunctionParse() {
        String json = "{'query': {'payments_geo_stat1': {}}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(0,
                queryParts.get(0).getChildren().size(),
                "root query has 0 parameter - no recognized function names");

    }

    @Test
    public void testNoQueryParse() {
        String json = "{'query1': {'invoices': {}}}";
        assertThrows(
                QueryParserException.class,
                () -> parser.parseQuery(json));
    }

    @Test
    public void testPaymentsParse() {
        String json =
                "{'query': {'payments': {'merchant_id': '1','shop_id': '2','invoice_id':'A','payment_id':'B', " +
                        "'payment_last4':'1212','from_time': '2016-03-22T00:12:00Z','to_time': " +
                        "'2016-03-22T01:12:00Z', 'payment_rrn': '2144325', 'payment_approval_code': '3414'}}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(1,
                queryParts.get(0).getParameters().getParametersMap().size(),
                "root query has 1 parameter - function name");
        assertEquals(1, queryParts.get(0).getChildren().size(),
                "child payments function");
        assertEquals(0,
                queryParts.get(0).getChildren().get(0).getChildren().size(),
                "payments function has no children");
        assertEquals(9,
                queryParts.get(0).getChildren().get(0).getParameters().getParametersMap().size(),
                "payments function has 9 parameters");

        assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
        assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(),
                PaymentsFunction.PaymentsParser.getMainDescriptor());

        PaymentsFunction.PaymentsParameters parameters =
                (PaymentsFunction.PaymentsParameters) queryParts.get(0).getChildren().get(0).getParameters();
        assertEquals("1", parameters.getMerchantId());
        assertEquals("2", parameters.getShopId());
        assertEquals("A", parameters.getInvoiceId());
        assertEquals("B", parameters.getPaymentId());
        assertEquals("1212", parameters.getPaymentBankCardLast4());
        assertEquals("2016-03-22T00:12:00Z", TypeUtil.temporalToString(parameters.getFromTime()));
        assertEquals("2016-03-22T01:12:00Z", TypeUtil.temporalToString(parameters.getToTime()));
        assertEquals("2144325", parameters.getPaymentRrn());
        assertEquals("3414", parameters.getPaymentApproveCode());
    }

    @Test
    public void testPaymentsParseWithPagination() {
        String json =
                "{'query': {'payments': {'merchant_id': '1','shop_id': '2','invoice_id':'A','payment_id':'B'," +
                        " 'payment_last4':'1212','from_time': '2016-03-22T00:12:00Z','to_time': " +
                        "'2016-03-22T01:12:00Z'}, 'size':'2', 'from':'1'}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(3,
                queryParts.get(0).getParameters().getParametersMap().size(),
                "root query has 3 parameters - function name, pagination");
        assertEquals(1, queryParts.get(0).getChildren().size(),
                "child payments function");
        assertEquals(0,
                queryParts.get(0).getChildren().get(0).getChildren().size(),
                "payments function has no children");
        assertEquals(7,
                queryParts.get(0).getChildren().get(0).getParameters().getParametersMap().size(),
                "payments function has 7 parameters");

        assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
        assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(),
                PaymentsFunction.PaymentsParser.getMainDescriptor());

        PaymentsFunction.PaymentsParameters parameters =
                (PaymentsFunction.PaymentsParameters) queryParts.get(0).getChildren().get(0).getParameters();
        assertEquals("1", parameters.getMerchantId());
        assertEquals("2", parameters.getShopId());
        assertEquals("A", parameters.getInvoiceId());
        assertEquals("B", parameters.getPaymentId());
        assertEquals("1212", parameters.getPaymentBankCardLast4());
        assertEquals("2016-03-22T00:12:00Z", TypeUtil.temporalToString(parameters.getFromTime()));
        assertEquals("2016-03-22T01:12:00Z", TypeUtil.temporalToString(parameters.getToTime()));
        assertEquals((Integer) 2, parameters.getSize());

    }

    @Test
    public void testPaymentsPanParseError() {
        String json =
                "{'query': {'payments': {'merchant_id': '1','shop_id': '2','invoice_id':'A','payment_id':'B'," +
                        " 'payment_last4':'12**12!','from_time': '2016-03-22T00:12:00Z','to_time': " +
                        "'2016-03-22T01:12:00Z'}}}";
        assertThrows(
                QueryParserException.class,
                () -> parser.parseQuery(json));
    }

    @Test
    public void testWithoutShopAndMerchantId() {
        String json =
                "{'query': {'payments': {'invoice_id':'A','payment_id':'B', 'payment_pan_mask':'12**12'," +
                        "'from_time': '2016-03-22T00:12:00Z','to_time': '2016-03-22T01:12:00Z'}}}";
        parser.parseQuery(json);
    }

    @Test
    public void testWithoutMerchantButWithShopId() {
        String json =
                "{'query': {'payments': {'shop_id':'C','invoice_id':'A','payment_id':'B', 'payment_pan_mask':" +
                        "'12**12','from_time': '2016-03-22T00:12:00Z','to_time': '2016-03-22T01:12:00Z'}}}";
        assertThrows(
                QueryParserException.class,
                () -> parser.parseQuery(json));
    }

    @Test
    public void testShopCategoryId() {
        String json = "{'query': {'payments': {'shop_category_ids': [1, 2, 3, 4, 5]}}}";
        PaymentsFunction.PaymentsParameters paymentsParameters =
                (PaymentsFunction.PaymentsParameters) parser.parseQuery(json).get(0).getChildren().get(0)
                        .getParameters();
        assertEquals(Arrays.asList(1, 2, 3, 4, 5), paymentsParameters.getShopCategoryIds());
    }

    @Test
    public void testInvoicesParse() {
        String json =
                "{'query': {'invoices': {'merchant_id': '1','shop_id': '2','invoice_id':'A'," +
                        "'invoice_status':'paid','from_time': '2016-03-22T00:12:00Z'}}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(1,
                queryParts.get(0).getParameters().getParametersMap().size(),
                "root query has 1 parameter - function name");
        assertEquals(1, queryParts.get(0).getChildren().size(), "child payments function");
        assertEquals(0,
                queryParts.get(0).getChildren().get(0).getChildren().size(),
                "payments function has no children");
        assertEquals(5,
                queryParts.get(0).getChildren().get(0).getParameters().getParametersMap().size(),
                "payments function has 5 parameters");

        assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
        assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(),
                InvoicesFunction.InvoicesParser.getMainDescriptor());

        InvoicesFunction.InvoicesParameters parameters =
                (InvoicesFunction.InvoicesParameters) queryParts.get(0).getChildren().get(0).getParameters();
        assertEquals("1", parameters.getMerchantId());
        assertEquals("2", parameters.getShopId());
        assertEquals("A", parameters.getInvoiceId());
        assertEquals("paid", parameters.getInvoiceStatus());
        assertEquals("2016-03-22T00:12:00Z", TypeUtil.temporalToString(parameters.getFromTime()));
        assertNull(parameters.getToTime());
    }

    @Test
    public void testInvoicesParseWithPagination() {
        String json =
                "{'query': {'invoices': {'merchant_id': '1','shop_id': '2','invoice_id':'A','invoice_status':'paid'," +
                        "'from_time': '2016-03-22T00:12:00Z','to_time': '2016-03-22T01:12:00Z', 'from':'1', " +
                        "'size':'2'}}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(1,
                queryParts.get(0).getParameters().getParametersMap().size(),
                "root query has 1 parameter - function name");
        assertEquals(1, queryParts.get(0).getChildren().size(), "child payments function");
        assertEquals(0,
                queryParts.get(0).getChildren().get(0).getChildren().size(),
                "payments function has no children");
        assertEquals(8,
                queryParts.get(0).getChildren().get(0).getParameters().getParametersMap().size(),
                "payments function has 8 parameters");

        assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
        assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(),
                InvoicesFunction.InvoicesParser.getMainDescriptor());

        InvoicesFunction.InvoicesParameters parameters =
                (InvoicesFunction.InvoicesParameters) queryParts.get(0).getChildren().get(0).getParameters();
        assertEquals("1", parameters.getMerchantId());
        assertEquals("2", parameters.getShopId());
        assertEquals("A", parameters.getInvoiceId());
        assertEquals("paid", parameters.getInvoiceStatus());
        assertEquals(2, parameters.getSize().intValue());
        assertEquals("2016-03-22T00:12:00Z", TypeUtil.temporalToString(parameters.getFromTime()));
        assertEquals("2016-03-22T01:12:00Z", TypeUtil.temporalToString(parameters.getToTime()));
        assertEquals((Integer) 2, parameters.getSize());
    }

    @Test
    public void testInvoicesTimeParseError() {
        String json =
                "{'query': {'invoices': {'merchant_id': '1','shop_id': '2','invoice_id':'A','payment_id':'B'," +
                        "'from_time': '2016-03-22T00:12:00Z','to_time': '2016-03-22T00:00:00Z'}}}";
        assertThrows(
                QueryParserException.class,
                () -> parser.parseQuery(json));
    }

    @Test
    public void testCustomersRateStatParse() {
        String json =
                "{'query': {'customers_rate_stat': {'merchant_id': '1','shop_id': '2', 'split_interval':'1'," +
                        "'from_time': '2016-03-22T00:12:00Z', 'to_time': '2016-03-22T01:00:00Z'}}}";
        List<QueryPart> queryParts = parser.parseQuery(json);
        assertEquals(1, queryParts.size(), "root query");
        assertEquals(1,
                queryParts.get(0).getParameters().getParametersMap().size(),
                "root query has 1 parameter - function name");
        assertEquals(1, queryParts.get(0).getChildren().size(), "child payments function");
        assertEquals(0,
                queryParts.get(0).getChildren().get(0).getChildren().size(),
                "payments function has no children");
        assertEquals(5,
                queryParts.get(0).getChildren().get(0).getParameters().getParametersMap().size(),
                "payments function has 5 parameters");

        assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
        assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(),
                CustomersRateStatFunction.CustomersRateStatParser.getMainDescriptor());

        StatBaseFunction.StatBaseParameters parameters =
                (StatBaseFunction.StatBaseParameters) queryParts.get(0).getChildren().get(0).getParameters();
        assertEquals("1", parameters.getMerchantId());
        assertEquals("2", parameters.getShopId());
        assertEquals((Integer) 1, parameters.getSplitInterval());
        assertEquals("2016-03-22T00:12:00Z", TypeUtil.temporalToString(parameters.getFromTime()));
        assertEquals("2016-03-22T01:00:00Z", TypeUtil.temporalToString(parameters.getToTime()));
    }

    @Test
    public void testStatFunctionsMatch() {
        String[] functionNames = {
                CustomersRateStatFunction.CustomersRateStatParser.getMainDescriptor(),
                PaymentsConversionStatFunction.PaymentsConversionStatParser.getMainDescriptor(),
                PaymentsGeoStatFunction.PaymentsGeoStatParser.getMainDescriptor(),
                PaymentsTurnoverStatFunction.PaymentsTurnoverStatParser.getMainDescriptor()

        };
        String json =
                "{'query': {'%fname%': {'merchant_id': '1','shop_id': '2', 'split_interval':'1'," +
                        "'from_time': '2016-03-22T00:12:00Z', 'to_time': '2016-03-22T01:00:00Z'}}}";

        for (String name : functionNames) {
            List<QueryPart> queryParts = parser.parseQuery(json.replaceAll("%fname%", name));
            assertEquals(RootQuery.RootParser.getMainDescriptor(), queryParts.get(0).getDescriptor());
            assertEquals(queryParts.get(0).getChildren().get(0).getDescriptor(), name);

        }
    }
}
