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
public class PaymentsCardTypesStatFunction extends StatBaseFunction {

    public static final String FUNC_NAME = "payments_pmt_cards_stat";

    private PaymentsCardTypesStatFunction(Object descriptor, QueryParameters params) {
        super(descriptor, params, FUNC_NAME);
    }

    @Override
    public QueryResult<Map<String, String>, StatResponse> execute(QueryContext context) throws QueryExecutionException {
        try {
            Collection<Map<String, String>> result = getContext(context).getStatisticsDao().getPaymentsCardTypesStat(
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

    public static class PaymentsCardTypesStatParser extends StatBaseParser {

        public PaymentsCardTypesStatParser() {
            super(FUNC_NAME);
        }

        public static String getMainDescriptor() {
            return FUNC_NAME;
        }
    }

    public static class PaymentsCardTypesStatBuilder extends StatBaseBuilder {

        @Override
        protected Query createQuery(QueryPart queryPart) {
            return new PaymentsCardTypesStatFunction(queryPart.getDescriptor(), queryPart.getParameters());
        }

        @Override
        protected Object getDescriptor(List<QueryPart> queryParts) {
            return PaymentsCardTypesStatParser.getMainDescriptor();
        }
    }

}
