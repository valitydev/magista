package dev.vality.magista.query.impl;

import dev.vality.damsel.merch_stat.StatInvoice;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vpankrashkin on 03.08.16.
 */
public class InvoicesFunction extends PagedBaseFunction<Map.Entry<Long, StatInvoice>, StatResponse>
        implements CompositeQuery<Map.Entry<Long, StatInvoice>, StatResponse> {

    public static final String FUNC_NAME = "invoices";

    private final CompositeQuery<QueryResult, List<QueryResult>> subquery;

    private InvoicesFunction(Object descriptor, QueryParameters params, String continuationToken,
                             CompositeQuery subquery) {
        super(descriptor, params, FUNC_NAME, continuationToken);
        this.subquery = subquery;
    }

    private static InvoicesFunction createInvoicesFunction(Object descriptor, QueryParameters queryParameters,
                                                           String continuationToken,
                                                           CompositeQuery<QueryResult, List<QueryResult>> subquery) {
        InvoicesFunction invoicesFunction =
                new InvoicesFunction(descriptor, queryParameters, continuationToken, subquery);
        subquery.setParentQuery(invoicesFunction);
        return invoicesFunction;
    }

    @Override
    public QueryResult<Map.Entry<Long, StatInvoice>, StatResponse> execute(QueryContext context)
            throws QueryExecutionException {
        QueryResult<QueryResult, List<QueryResult>> collectedResults = subquery.execute(context);

        return execute(context, collectedResults.getCollectedStream());
    }

    @Override
    public QueryResult<Map.Entry<Long, StatInvoice>, StatResponse> execute(QueryContext context,
                                                                           List<QueryResult> collectedResults)
            throws QueryExecutionException {
        QueryResult<Map.Entry<Long, StatInvoice>, List<Map.Entry<Long, StatInvoice>>> invoicesResult =
                (QueryResult<Map.Entry<Long, StatInvoice>, List<Map.Entry<Long, StatInvoice>>>) collectedResults.get(0);

        return new BaseQueryResult<>(
                () -> invoicesResult.getDataStream(),
                () -> {
                    StatResponseData statResponseData = StatResponseData.invoices(
                            invoicesResult.getDataStream()
                                    .map(Map.Entry::getValue)
                                    .collect(Collectors.toList())
                    );
                    StatResponse statResponse = new StatResponse(statResponseData);

                    List<Map.Entry<Long, StatInvoice>> invoiceStats = invoicesResult.getCollectedStream();
                    if (!invoicesResult.getCollectedStream().isEmpty()
                            && getQueryParameters().getSize() == invoiceStats.size()) {
                        final String createdAt = invoiceStats.get(invoiceStats.size() - 1).getValue().getCreatedAt();
                        final String token = getContext(context)
                                .getTokenGenService()
                                .generateToken(getQueryParameters(), TypeUtil.stringToLocalDateTime(createdAt));
                        statResponse.setContinuationToken(token);
                    }
                    return statResponse;
                });
    }

    @Override
    public InvoicesParameters getQueryParameters() {
        return (InvoicesParameters) super.getQueryParameters();
    }

    @Override
    protected QueryParameters createQueryParameters(QueryParameters parameters, QueryParameters derivedParameters) {
        return new InvoicesParameters(parameters, derivedParameters);
    }

    @Override
    public List<Query> getChildQueries() {
        return subquery.getChildQueries();
    }

    @Override
    public boolean isParallel() {
        return subquery.isParallel();
    }

    public static class InvoicesParameters extends PaymentsFunction.PaymentsParameters {

        public InvoicesParameters(Map<String, Object> parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public InvoicesParameters(QueryParameters parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public String getInvoiceId() {
            return getStringParameter(Parameters.INVOICE_ID_PARAM, false);
        }

        public String getInvoiceStatus() {
            return getStringParameter(Parameters.INVOICE_STATUS_PARAM, false);
        }

        public Long getInvoiceAmount() {
            return getLongParameter(Parameters.INVOICE_AMOUNT_PARAM, false);
        }

        public String getExternalId() {
            return getStringParameter(Parameters.EXTERNAL_ID_PARAM, false);
        }

        public List<String> getInvoiceIds() {
            return getArrayParameter(Parameters.INVOICE_IDS_PARAM, false);
        }

    }

    public static class InvoicesValidator extends PagedBaseValidator {

        @Override
        public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
            super.validateParameters(parameters);
            InvoicesParameters paymentsParameters = super.checkParamsType(parameters, InvoicesParameters.class);

            validateTimePeriod(paymentsParameters.getFromTime(), paymentsParameters.getToTime());

            if (paymentsParameters.getShopId() != null && paymentsParameters.getShopIds() != null) {
                checkParamsResult(true,
                        String.format("Need to specify only one parameter: %s or %s",
                                Parameters.SHOP_ID_PARAM, Parameters.SHOP_IDS_PARAM));
            }
        }
    }

    public static class InvoicesParser extends AbstractQueryParser {
        private final InvoicesValidator validator = new InvoicesValidator();

        public static String getMainDescriptor() {
            return FUNC_NAME;
        }

        @Override
        public List<QueryPart> parseQuery(Map<String, Object> source, QueryPart parent) throws QueryParserException {
            Map<String, Object> funcSource = (Map) source.get(FUNC_NAME);
            InvoicesParameters parameters =
                    getValidatedParameters(funcSource, parent, InvoicesParameters::new, validator);

            return Stream.of(
                    new QueryPart(FUNC_NAME, parameters, parent)
            )
                    .collect(Collectors.toList());
        }

        @Override
        public boolean apply(Map source, QueryPart parent) {
            return parent != null
                    && RootQuery.RootParser.getMainDescriptor().equals(parent.getDescriptor())
                    && (source.get(FUNC_NAME) instanceof Map);
        }
    }

    public static class InvoicesBuilder extends AbstractQueryBuilder {
        private final InvoicesValidator validator = new InvoicesValidator();

        @Override
        public Query buildQuery(QueryContext queryContext, List<QueryPart> queryParts, String continuationToken,
                                QueryPart parentQueryPart, QueryBuilder baseBuilder) throws QueryBuilderException {
            Query resultQuery = buildSingleQuery(InvoicesParser.getMainDescriptor(), queryParts,
                    queryPart -> createQuery(queryPart, continuationToken));
            validator.validateQuery(resultQuery, queryContext);
            return resultQuery;
        }

        private CompositeQuery createQuery(QueryPart queryPart, String continuationToken) {
            List<Query> queries = Arrays.asList(
                    new GetDataFunction(queryPart.getDescriptor() + ":" + GetDataFunction.FUNC_NAME,
                            queryPart.getParameters(), continuationToken)
            );
            CompositeQuery<QueryResult, List<QueryResult>> compositeQuery = createCompositeQuery(
                    queryPart.getDescriptor(),
                    getParameters(queryPart.getParent()),
                    queries
            );
            return createInvoicesFunction(queryPart.getDescriptor(), queryPart.getParameters(), continuationToken,
                    compositeQuery);
        }

        @Override
        public boolean apply(List<QueryPart> queryParts, QueryPart parent) {
            return getMatchedPartsStream(InvoicesParser.getMainDescriptor(), queryParts).findFirst().isPresent();
        }

    }

    private static class GetDataFunction extends PagedBaseFunction {
        private static final String FUNC_NAME = PaymentsFunction.FUNC_NAME + "_data";

        public GetDataFunction(Object descriptor, QueryParameters params, String continuationToken) {
            super(descriptor, params, FUNC_NAME, continuationToken);
        }

        @Override
        public QueryResult<Map.Entry<Long, StatInvoice>, Collection<Map.Entry<Long, StatInvoice>>> execute(
                QueryContext context) throws QueryExecutionException {
            FunctionQueryContext functionContext = getContext(context);
            InvoicesParameters parameters =
                    new InvoicesParameters(getQueryParameters(), getQueryParameters().getDerivedParameters());
            try {
                Collection<Map.Entry<Long, StatInvoice>> result = functionContext.getSearchDao().getInvoices(
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
