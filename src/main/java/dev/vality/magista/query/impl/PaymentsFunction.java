package dev.vality.magista.query.impl;

import dev.vality.damsel.merch_stat.StatPayment;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.damsel.merch_stat.StatResponseData;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.domain.enums.PaymentTool;
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

/**
 * Created by vpankrashkin on 03.08.16.
 */
public class PaymentsFunction extends PagedBaseFunction<Map.Entry<Long, StatPayment>, StatResponse>
        implements CompositeQuery<Map.Entry<Long, StatPayment>, StatResponse> {

    public static final String FUNC_NAME = "payments";

    private final CompositeQuery<QueryResult, List<QueryResult>> subquery;

    private PaymentsFunction(Object descriptor, QueryParameters params, String continuationToken,
                             CompositeQuery<QueryResult, List<QueryResult>> subquery) {
        super(descriptor, params, FUNC_NAME, continuationToken);
        this.subquery = subquery;
    }

    private static PaymentsFunction createPaymentsFunction(Object descriptor, QueryParameters queryParameters,
                                                           String continuationToken,
                                                           CompositeQuery<QueryResult, List<QueryResult>> subquery) {
        PaymentsFunction paymentsFunction =
                new PaymentsFunction(descriptor, queryParameters, continuationToken, subquery);
        subquery.setParentQuery(paymentsFunction);
        return paymentsFunction;
    }

    @Override
    public QueryResult<Map.Entry<Long, StatPayment>, StatResponse> execute(QueryContext context)
            throws QueryExecutionException {
        QueryResult<QueryResult, List<QueryResult>> collectedResults = subquery.execute(context);

        return execute(context, collectedResults.getCollectedStream());
    }

    @Override
    public QueryResult<Map.Entry<Long, StatPayment>, StatResponse> execute(QueryContext context,
                                                                           List<QueryResult> collectedResults)
            throws QueryExecutionException {
        QueryResult<Map.Entry<Long, StatPayment>, List<Map.Entry<Long, StatPayment>>> paymentsResult =
                (QueryResult<Map.Entry<Long, StatPayment>, List<Map.Entry<Long, StatPayment>>>) collectedResults.get(0);

        return new BaseQueryResult<>(
                () -> paymentsResult.getDataStream(),
                () -> {
                    StatResponseData statResponseData = StatResponseData.payments(paymentsResult.getDataStream()
                            .map(paymentResponse -> paymentResponse.getValue()).collect(Collectors.toList()));
                    StatResponse statResponse = new StatResponse(statResponseData);
                    List<Map.Entry<Long, StatPayment>> payments = paymentsResult.getCollectedStream();
                    if (!payments.isEmpty() && getQueryParameters().getSize() == payments.size()) {
                        String createdAt = payments.get(payments.size() - 1).getValue().getCreatedAt();
                        String token = getContext(context)
                                .getTokenGenService()
                                .generateToken(getQueryParameters(), TypeUtil.stringToLocalDateTime(createdAt));
                        statResponse.setContinuationToken(token);
                    }
                    return statResponse;
                }
        );
    }

    @Override
    public PaymentsParameters getQueryParameters() {
        return (PaymentsParameters) super.getQueryParameters();
    }

    @Override
    protected QueryParameters createQueryParameters(QueryParameters parameters, QueryParameters derivedParameters) {
        return new PaymentsParameters(parameters, derivedParameters);
    }

    @Override
    public List<Query> getChildQueries() {
        return subquery.getChildQueries();
    }

    @Override
    public boolean isParallel() {
        return subquery.isParallel();
    }

    public static class PaymentsParameters extends PagedBaseParameters {

        public PaymentsParameters(Map<String, Object> parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public PaymentsParameters(QueryParameters parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
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

        public String getPaymentStatus() {
            return getStringParameter(Parameters.PAYMENT_STATUS_PARAM, false);
        }

        public TemporalAccessor getFromTime() {
            return getTimeParameter(Parameters.FROM_TIME_PARAM, false);
        }

        public TemporalAccessor getToTime() {
            return getTimeParameter(Parameters.TO_TIME_PARAM, false);
        }

        public String getPaymentEmail() {
            return getStringParameter(Parameters.PAYMENT_EMAIL_PARAM, false);
        }

        public String getPaymentIp() {
            return getStringParameter(Parameters.PAYMENT_IP_PARAM, false);
        }

        public String getPaymentFingerprint() {
            return getStringParameter(Parameters.PAYMENT_FINGERPRINT_PARAM, false);
        }

        public String getPaymentFlow() {
            return getStringParameter(Parameters.PAYMENT_FLOW_PARAM, false);
        }

        public PaymentTool getPaymentMethod() {
            return TypeUtil.toEnumField(
                    getStringParameter(Parameters.PAYMENT_METHOD_PARAM, false),
                    PaymentTool.class
            );
        }

        public void setPaymentMethod(PaymentTool paymentMethod) {
            setParameter(Parameters.PAYMENT_METHOD_PARAM, paymentMethod.getLiteral());
        }

        public String getPaymentTerminalProvider() {
            return getStringParameter(Parameters.PAYMENT_TERMINAL_PROVIDER_PARAM, false);
        }

        public Long getPaymentDomainRevision() {
            return getLongParameter(Parameters.PAYMENT_DOMAIN_REVISION_PARAM, false);
        }

        public Long getFromPaymentDomainRevision() {
            return getLongParameter(Parameters.FROM_PAYMENT_DOMAIN_REVISION_PARAM, false);
        }

        public Long getToPaymentDomainRevision() {
            return getLongParameter(Parameters.TO_PAYMENT_DOMAIN_REVISION_PARAM, false);
        }

        public String getPaymentCustomerId() {
            return getStringParameter(Parameters.PAYMENT_CUSTOMER_ID_PARAM, false);
        }

        public Integer getPaymentProviderId() {
            return getIntParameter(Parameters.PAYMENT_PROVIDER_ID_PARAM, false);
        }

        public Integer getPaymentTerminalId() {
            return getIntParameter(Parameters.PAYMENT_TERMINAL_ID_PARAM, false);
        }

        public Long getPaymentAmount() {
            return getLongParameter(Parameters.PAYMENT_AMOUNT_PARAM, false);
        }

        public String getPaymentBankCardFirst6() {
            return getStringParameter(Parameters.PAYMENT_BANK_CARD_FIRST6, false);
        }

        public String getPaymentBankCardLast4() {
            return getStringParameter(Parameters.PAYMENT_BANK_CARD_LAST4, false);
        }


        public String getPaymentBankCardSystem() {
            return getStringParameter(Parameters.PAYMENT_BANK_CARD_PAYMENT_SYSTEM_PARAM, false);
        }

        public String getPaymentBankCardTokenProvider() {
            return getStringParameter(Parameters.PAYMENT_BANK_CARD_TOKEN_PROVIDER_PARAM, false);
        }

        public String getPaymentRrn() {
            return getStringParameter(Parameters.PAYMENT_RRN_PARAM, false);
        }

        public String getPaymentApproveCode() {
            return getStringParameter(Parameters.PAYMENT_APPROVAL_CODE_PARAM, false);
        }

        public String getExternalId() {
            return getStringParameter(Parameters.EXTERNAL_ID_PARAM, false);
        }

        public Object getExclude() {
            return getParameter(Parameters.EXCLUDE_PARAM, false);
        }

        public Long getPaymentAmountFrom() {
            return getLongParameter(Parameters.PAYMENT_AMOUNT_FROM, false);
        }

        public Long getPaymentAmountTo() {
            return getLongParameter(Parameters.PAYMENT_AMOUNT_TO, false);
        }
    }

    public static class PaymentsValidator extends PagedBaseValidator {

        @Override
        public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
            super.validateParameters(parameters);
            PaymentsParameters paymentsParameters = super.checkParamsType(parameters, PaymentsParameters.class);

            String cardFirst6 = paymentsParameters.getPaymentBankCardFirst6();
            if (cardFirst6 != null && !cardFirst6.matches("^\\d{6,8}$")) {
                checkParamsResult(true, Parameters.PAYMENT_BANK_CARD_FIRST6,
                        RootQuery.RootValidator.DEFAULT_ERR_MSG_STRING);
            }

            String cardLast4 = paymentsParameters.getPaymentBankCardLast4();
            if (cardLast4 != null && !cardLast4.matches("^\\d{2,4}$")) {
                checkParamsResult(true,
                        Parameters.PAYMENT_BANK_CARD_LAST4, RootQuery.RootValidator.DEFAULT_ERR_MSG_STRING);
            }

            validateTimePeriod(paymentsParameters.getFromTime(), paymentsParameters.getToTime());

            if (paymentsParameters.getPaymentMethod() == null) {
                fillCorrectPaymentMethod(paymentsParameters);
            } else {
                validatePaymentToolCorrectness(paymentsParameters);
            }
        }

        private void fillCorrectPaymentMethod(PaymentsParameters paymentsParameters) {
            if (paymentsParameters.getPaymentBankCardTokenProvider() != null) {
                paymentsParameters.setPaymentMethod(PaymentTool.bank_card);
            }
            if (paymentsParameters.getPaymentTerminalProvider() != null) {
                paymentsParameters.setPaymentMethod(PaymentTool.payment_terminal);
            }
        }

        private void validatePaymentToolCorrectness(PaymentsFunction.PaymentsParameters parameters) {
            boolean bankCardMismatch = parameters.getPaymentTerminalProvider() != null
                    && PaymentTool.payment_terminal != parameters.getPaymentMethod();
            boolean terminalMismatch = parameters.getPaymentBankCardTokenProvider() != null
                    && PaymentTool.bank_card != parameters.getPaymentMethod();
            if (bankCardMismatch || terminalMismatch) {
                String provider = parameters.getPaymentTerminalProvider() != null
                        ? Parameters.PAYMENT_TERMINAL_PROVIDER_PARAM
                        : Parameters.PAYMENT_BANK_CARD_TOKEN_PROVIDER_PARAM;
                checkParamsResult(
                        true,
                        provider,
                        RootQuery.RootValidator.DEFAULT_ERR_MSG_STRING,
                        String.format("Incorrect PaymentMethod %s and provider %s", parameters.getPaymentMethod(),
                                provider)
                );
            }
        }
    }

    public static class PaymentsParser extends AbstractQueryParser {
        private final PaymentsValidator validator = new PaymentsValidator();

        public static String getMainDescriptor() {
            return FUNC_NAME;
        }

        @Override
        public List<QueryPart> parseQuery(Map<String, Object> source, QueryPart parent) throws QueryParserException {
            Map<String, Object> funcSource = (Map) source.get(FUNC_NAME);
            PaymentsParameters parameters =
                    getValidatedParameters(funcSource, parent, PaymentsParameters::new, validator);

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

    public static class PaymentsBuilder extends AbstractQueryBuilder {
        private final PaymentsValidator validator = new PaymentsValidator();

        @Override
        public Query buildQuery(QueryContext queryContext, List<QueryPart> queryParts, String continuationToken,
                                QueryPart parentQueryPart, QueryBuilder baseBuilder) throws QueryBuilderException {
            Query resultQuery = buildSingleQuery(PaymentsParser.getMainDescriptor(), queryParts,
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
            return createPaymentsFunction(queryPart.getDescriptor(), queryPart.getParameters(), continuationToken,
                    compositeQuery);
        }

        @Override
        public boolean apply(List<QueryPart> queryParts, QueryPart parent) {
            return getMatchedPartsStream(PaymentsParser.getMainDescriptor(), queryParts).findFirst().isPresent();
        }
    }

    private static class GetDataFunction
            extends PagedBaseFunction<Map.Entry<Long, StatPayment>, Collection<Map.Entry<Long, StatPayment>>> {
        private static final String FUNC_NAME = PaymentsFunction.FUNC_NAME + "_data";

        public GetDataFunction(Object descriptor, QueryParameters params, String continuationToken) {
            super(descriptor, params, FUNC_NAME, continuationToken);
        }

        @Override
        public QueryResult<Map.Entry<Long, StatPayment>, Collection<Map.Entry<Long, StatPayment>>> execute(
                QueryContext context) throws QueryExecutionException {
            FunctionQueryContext functionContext = getContext(context);
            PaymentsParameters parameters =
                    new PaymentsParameters(getQueryParameters(), getQueryParameters().getDerivedParameters());
            try {
                Collection<Map.Entry<Long, StatPayment>> result = functionContext.getSearchDao().getPayments(
                        parameters,
                        TypeUtil.toLocalDateTime(parameters.getFromTime()),
                        TypeUtil.toLocalDateTime(parameters.getToTime()),
                        getTime(functionContext),
                        parameters.getSize()
                );
                return new BaseQueryResult<>(() -> result.stream(), () -> result);
            } catch (DaoException e) {
                throw new QueryExecutionException(e);
            }
        }
    }

}
