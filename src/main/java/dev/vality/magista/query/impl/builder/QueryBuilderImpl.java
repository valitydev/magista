package dev.vality.magista.query.impl.builder;

import dev.vality.magista.query.builder.BaseQueryBuilder;
import dev.vality.magista.query.builder.QueryBuilder;
import dev.vality.magista.query.impl.*;
import dev.vality.magista.query.parser.QueryPart;

import java.util.Arrays;
import java.util.List;

/**
 * Created by vpankrashkin on 28.08.16.
 */
public class QueryBuilderImpl extends BaseQueryBuilder {
    public QueryBuilderImpl() {
        this(
                Arrays.asList(
                        new RootQuery.RootBuilder(),
                        new PaymentsFunction.PaymentsBuilder(),
                        new InvoicesFunction.InvoicesBuilder(),
                        new RefundsFunction.RefundsBuilder(),
                        new PayoutsFunction.PayoutsBuilder(),
                        new ChargebacksFunction.ChargebacksBuilder(),
                        new PaymentsGeoStatFunction.PaymentsGeoStatBuilder(),
                        new PaymentsCardTypesStatFunction.PaymentsCardTypesStatBuilder(),
                        new CustomersRateStatFunction.CustomersRateStatBuilder(),
                        new PaymentsTurnoverStatFunction.PaymentsTurnoverStatBuilder(),
                        new PaymentsConversionStatFunction.PaymentsConversionStatBuilder()
                )
        );
    }

    public QueryBuilderImpl(List<QueryBuilder> parsers) {
        super(parsers);
    }

    @Override
    public boolean apply(List<QueryPart> queryParts, QueryPart parent) {
        return true;
    }
}
