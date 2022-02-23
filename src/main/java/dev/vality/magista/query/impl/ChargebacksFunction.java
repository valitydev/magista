package dev.vality.magista.query.impl;

import dev.vality.damsel.merch_stat.StatChargeback;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.damsel.merch_stat.StatResponseData;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.exception.DaoException;
import dev.vality.magista.query.*;
import dev.vality.magista.query.builder.QueryBuilder;
import dev.vality.magista.query.builder.QueryBuilderException;
import dev.vality.magista.query.impl.builder.AbstractQueryBuilder;
import dev.vality.magista.query.impl.parser.AbstractQueryParser;
import dev.vality.magista.query.parser.QueryParserException;
import dev.vality.magista.query.parser.QueryPart;

import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChargebacksFunction
        extends PagedBaseFunction<Map.Entry<Long, StatChargeback>, StatResponse>
        implements CompositeQuery<Map.Entry<Long, StatChargeback>, StatResponse> {

    public static final String FUNC_NAME = "chargebacks";

    private final CompositeQuery<QueryResult, List<QueryResult>> subquery;

    private ChargebacksFunction(Object descriptor,
                                QueryParameters params,
                                String continuationToken,
                                CompositeQuery subquery) {
        super(descriptor, params, FUNC_NAME, continuationToken);
        this.subquery = subquery;
    }

    private static ChargebacksFunction createChargebacksFunction(
            Object descriptor,
            QueryParameters queryParameters,
            String continuationToken,
            CompositeQuery<QueryResult, List<QueryResult>> subquery
    ) {
        ChargebacksFunction chargebacksFunction = new ChargebacksFunction(
                descriptor, queryParameters, continuationToken, subquery
        );
        subquery.setParentQuery(chargebacksFunction);
        return chargebacksFunction;
    }

    @Override
    public QueryResult<Map.Entry<Long, StatChargeback>, StatResponse> execute(
            QueryContext context) // TODO: StatChargeback
            throws QueryExecutionException {
        QueryResult<QueryResult, List<QueryResult>> collectedResults = subquery.execute(context);

        return execute(context, collectedResults.getCollectedStream());
    }

    @Override
    public QueryResult<Map.Entry<Long, StatChargeback>, StatResponse> execute(
            QueryContext context,
            List<QueryResult> collectedResults
    ) throws QueryExecutionException {
        var chargebacksResult =
                (QueryResult<Map.Entry<Long, StatChargeback>, List<Map.Entry<Long, StatChargeback>>>) collectedResults
                        .get(0);

        return new BaseQueryResult<>(
                () -> chargebacksResult.getDataStream(),
                () -> {
                    StatResponseData statResponseData = StatResponseData.chargebacks(chargebacksResult.getDataStream()
                            .map(chargebackResponse -> chargebackResponse.getValue())
                            .collect(Collectors.toList()));

                    StatResponse statResponse = new StatResponse(statResponseData);
                    List<Map.Entry<Long, StatChargeback>> chargebacks = chargebacksResult.getCollectedStream();
                    if (!chargebacks.isEmpty() && getQueryParameters().getSize() == chargebacks.size()) {
                        final String createdAt = chargebacks.get(chargebacks.size() - 1).getValue().getCreatedAt();
                        final String token = getContext(context)
                                .getTokenGenService()
                                .generateToken(getQueryParameters(), TypeUtil.stringToLocalDateTime(createdAt));
                        statResponse.setContinuationToken(token);
                    }
                    return statResponse;
                });
    }

    @Override
    public ChargebacksParameters getQueryParameters() {
        return (ChargebacksParameters) super.getQueryParameters();
    }

    @Override
    protected QueryParameters createQueryParameters(QueryParameters parameters,
                                                    QueryParameters derivedParameters) {
        return new ChargebacksParameters(parameters, derivedParameters);
    }

    @Override
    public List<Query> getChildQueries() {
        return subquery.getChildQueries();
    }

    @Override
    public boolean isParallel() {
        return subquery.isParallel();
    }

    public static class ChargebacksParameters extends PagedBaseParameters {

        public ChargebacksParameters(Map<String, Object> parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public ChargebacksParameters(QueryParameters parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public TemporalAccessor getFromTime() {
            return getTimeParameter(Parameters.FROM_TIME_PARAM, false);
        }

        public TemporalAccessor getToTime() {
            return getTimeParameter(Parameters.TO_TIME_PARAM, false);
        }

        public String getInvoiceId() {
            return getStringParameter(Parameters.INVOICE_ID_PARAM, false);
        }

        public List<String> getInvoiceIds() {
            return getArrayParameter(Parameters.INVOICE_IDS_PARAM, false);
        }

        public String getPaymentId() {
            return getStringParameter(Parameters.PAYMENT_ID_PARAM, false);
        }

        public String getChargebackId() {
            return getStringParameter(Parameters.CHARGEBACK_ID_PARAM, false);
        }

        public List<String> getChargebackStatuses() {
            return getArrayParameter(Parameters.CHARGEBACK_STATUSES_PARAM, false);
        }

        public List<String> getChargebackCategories() {
            return getArrayParameter(Parameters.CHARGEBACK_CATEGORIES_PARAM, false);
        }

        public List<String> getChargebackStages() {
            return getArrayParameter(Parameters.CHARGEBACK_STAGES_PARAM, false);
        }

    }

    public static class ChargebacksValidator extends PagedBaseValidator {

        @Override
        public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
            super.validateParameters(parameters);
            ChargebacksParameters chargebacksParameters =
                    super.checkParamsType(parameters, ChargebacksParameters.class);

            validateTimePeriod(chargebacksParameters.getFromTime(), chargebacksParameters.getToTime());
        }
    }

    public static class ChargebacksParser extends AbstractQueryParser {
        private final ChargebacksValidator validator = new ChargebacksValidator();

        public static String getMainDescriptor() {
            return FUNC_NAME;
        }

        @Override
        public List<QueryPart> parseQuery(Map<String, Object> source, QueryPart parent)
                throws QueryParserException {
            Map<String, Object> funcSource = (Map) source.get(FUNC_NAME);
            ChargebacksParameters parameters =
                    getValidatedParameters(funcSource, parent, ChargebacksParameters::new, validator);

            return Stream.of(new QueryPart(FUNC_NAME, parameters, parent))
                    .collect(Collectors.toList());
        }

        @Override
        public boolean apply(Map source, QueryPart parent) {
            return parent != null
                    && RootQuery.RootParser.getMainDescriptor().equals(parent.getDescriptor())
                    && (source.get(FUNC_NAME) instanceof Map);
        }
    }

    public static class ChargebacksBuilder extends AbstractQueryBuilder {
        private final ChargebacksValidator validator = new ChargebacksValidator();

        @Override
        public Query buildQuery(QueryContext queryContext,
                                List<QueryPart> queryParts,
                                String continuationToken,
                                QueryPart parentQueryPart,
                                QueryBuilder baseBuilder) throws QueryBuilderException {
            Query resultQuery = buildSingleQuery(
                    ChargebacksParser.getMainDescriptor(),
                    queryParts,
                    queryPart -> createQuery(queryPart, continuationToken)
            );
            validator.validateQuery(resultQuery, queryContext);
            return resultQuery;
        }

        private CompositeQuery createQuery(QueryPart queryPart, String continuationToken) {
            List<Query> queries = Arrays.asList(
                    new GetDataFunction(queryPart.getDescriptor() + ":" +
                            GetDataFunction.FUNC_NAME, queryPart.getParameters(), continuationToken)
            );
            CompositeQuery<QueryResult, List<QueryResult>> compositeQuery = createCompositeQuery(
                    queryPart.getDescriptor(),
                    getParameters(queryPart.getParent()),
                    queries
            );
            return createChargebacksFunction(
                    queryPart.getDescriptor(),
                    queryPart.getParameters(),
                    continuationToken,
                    compositeQuery
            );
        }

        @Override
        public boolean apply(List<QueryPart> queryParts, QueryPart parent) {
            return getMatchedPartsStream(ChargebacksParser.getMainDescriptor(), queryParts).findFirst().isPresent();
        }
    }

    private static class GetDataFunction
            extends PagedBaseFunction<Map.Entry<Long, StatChargeback>, Collection<Map.Entry<Long, StatChargeback>>> {
        private static final String FUNC_NAME = ChargebacksFunction.FUNC_NAME + "_data";

        public GetDataFunction(Object descriptor, QueryParameters params, String continuationToken) {
            super(descriptor, params, FUNC_NAME, continuationToken);
        }

        @Override
        public QueryResult<Map.Entry<Long, StatChargeback>, Collection<Map.Entry<Long, StatChargeback>>> execute(
                QueryContext context
        ) throws QueryExecutionException {
            FunctionQueryContext functionContext = getContext(context);
            ChargebacksParameters parameters = new ChargebacksParameters(
                    getQueryParameters(), getQueryParameters().getDerivedParameters()
            );
            try {
                Collection<Map.Entry<Long, StatChargeback>> result = functionContext.getSearchDao().getChargebacks(
                        parameters,
                        TypeUtil.toLocalDateTime(parameters.getFromTime()),
                        TypeUtil.toLocalDateTime(parameters.getToTime()),
                        getTime(functionContext),
                        parameters.getSize()
                );
                return new BaseQueryResult<>(result::stream, () -> result);
            } catch (DaoException e) {
                throw new QueryExecutionException(e);
            }
        }
    }

}
