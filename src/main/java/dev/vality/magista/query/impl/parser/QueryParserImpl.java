package dev.vality.magista.query.impl.parser;

import dev.vality.magista.query.impl.*;
import dev.vality.magista.query.parser.BaseQueryParser;
import dev.vality.magista.query.parser.QueryParser;
import dev.vality.magista.query.parser.QueryPart;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by vpankrashkin on 26.08.16.
 */
public class QueryParserImpl extends BaseQueryParser {

    public QueryParserImpl() {
        this(
                Arrays.asList(
                        new RootQuery.RootParser(),
                        new PaymentsFunction.PaymentsParser(),
                        new InvoicesFunction.InvoicesParser(),
                        new RefundsFunction.RefundsParser(),
                        new PayoutsFunction.PayoutsParser(),
                        new ChargebacksFunction.ChargebacksParser(),
                        new CustomersRateStatFunction.CustomersRateStatParser(),
                        new PaymentsConversionStatFunction.PaymentsConversionStatParser(),
                        new PaymentsGeoStatFunction.PaymentsGeoStatParser(),
                        new PaymentsCardTypesStatFunction.PaymentsCardTypesStatParser(),
                        new PaymentsTurnoverStatFunction.PaymentsTurnoverStatParser()
                )
        );
    }

    public QueryParserImpl(List<QueryParser<Map<String, Object>>> parsers) {
        super(parsers);
    }

    @Override
    public boolean apply(Map source, QueryPart parent) {
        return true;
    }
}
