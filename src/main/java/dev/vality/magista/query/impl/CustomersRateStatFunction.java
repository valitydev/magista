package dev.vality.magista.query.impl;

import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.damsel.merch_stat.StatResponseData;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.exception.DaoException;
import dev.vality.magista.query.*;
import dev.vality.magista.query.parser.QueryPart;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vpankrashkin on 09.08.16.
 */
public class CustomersRateStatFunction extends StatBaseFunction {

    public static final String FUNC_NAME = "customers_rate_stat";

    private CustomersRateStatFunction(Object descriptor, QueryParameters params) {
        super(descriptor, params, FUNC_NAME);
    }

    @Override
    public QueryResult<Map<String, String>, StatResponse> execute(QueryContext context) throws QueryExecutionException {
        try {
            Collection<Map<String, String>> result = getContext(context).getStatisticsDao().getCustomersRateStat(
                    getQueryParameters().getMerchantId(),
                    getQueryParameters().getShopId(),
                    TypeUtil.toLocalDateTime(getQueryParameters().getFromTime()),
                    TypeUtil.toLocalDateTime(getQueryParameters().getToTime()),
                    getQueryParameters().getSplitInterval()
            );
            return new BaseQueryResult<>(() -> result.stream(),
                    () -> new StatResponse(StatResponseData.records(result.stream().collect(Collectors.toList()))));
        } catch (DaoException e) {
            throw new QueryExecutionException(e);
        }
    }

    public static class CustomersRateStatParser extends StatBaseParser {

        public CustomersRateStatParser() {
            super(FUNC_NAME);
        }

        public static String getMainDescriptor() {
            return FUNC_NAME;
        }
    }

    public static class CustomersRateStatBuilder extends StatBaseBuilder {

        @Override
        protected Query createQuery(QueryPart queryPart) {
            return new CustomersRateStatFunction(queryPart.getDescriptor(), queryPart.getParameters());
        }

        @Override
        protected Object getDescriptor(List<QueryPart> queryParts) {
            return CustomersRateStatParser.getMainDescriptor();
        }
    }

}
