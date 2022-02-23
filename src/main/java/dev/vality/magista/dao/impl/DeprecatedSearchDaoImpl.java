package dev.vality.magista.dao.impl;

import dev.vality.damsel.merch_stat.*;
import dev.vality.magista.dao.DeprecatedSearchDao;
import dev.vality.magista.dao.impl.field.ConditionParameterSource;
import dev.vality.magista.dao.impl.mapper.*;
import dev.vality.magista.domain.enums.PayoutStatus;
import dev.vality.magista.domain.enums.*;
import dev.vality.magista.query.impl.*;
import org.jooq.Condition;
import org.jooq.Operator;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.*;

import static dev.vality.geck.common.util.TypeUtil.toEnumField;
import static dev.vality.geck.common.util.TypeUtil.toEnumFields;
import static dev.vality.magista.domain.tables.ChargebackData.CHARGEBACK_DATA;
import static dev.vality.magista.domain.tables.InvoiceData.INVOICE_DATA;
import static dev.vality.magista.domain.tables.PaymentData.PAYMENT_DATA;
import static dev.vality.magista.domain.tables.Payout.PAYOUT;
import static dev.vality.magista.domain.tables.RefundData.REFUND_DATA;
import static dev.vality.magista.query.impl.Parameters.SHOP_ID_PARAM;
import static org.jooq.Comparator.*;

@Deprecated
@Component
public class DeprecatedSearchDaoImpl extends AbstractDao implements DeprecatedSearchDao {

    private final DeprecatedStatInvoiceMapper statInvoiceMapper;
    private final DeprecatedStatPaymentMapper statPaymentMapper;
    private final DeprecatedStatRefundMapper statRefundMapper;
    private final DeprecatedStatPayoutMapper statPayoutMapper;
    private final DeprecatedStatChargebackMapper statChargebackMapper;

    public DeprecatedSearchDaoImpl(DataSource ds) {
        super(ds);
        statInvoiceMapper = new DeprecatedStatInvoiceMapper();
        statPaymentMapper = new DeprecatedStatPaymentMapper();
        statRefundMapper = new DeprecatedStatRefundMapper();
        statPayoutMapper = new DeprecatedStatPayoutMapper();
        statChargebackMapper = new DeprecatedStatChargebackMapper();
    }

    @Override
    public Collection<Map.Entry<Long, StatInvoice>> getInvoices(
            InvoicesFunction.InvoicesParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    ) {

        Condition condition = appendDateTimeRangeConditions(
                appendConditions(DSL.trueCondition(), Operator.AND,
                        new ConditionParameterSource()
                                .addValue(
                                        INVOICE_DATA.PARTY_ID,
                                        Optional.ofNullable(parameters.getMerchantId())
                                                .map(UUID::fromString)
                                                .orElse(null),
                                        EQUALS)
                                .addValue(INVOICE_DATA.PARTY_SHOP_ID, parameters.getShopId(), EQUALS)
                                .addInConditionValue(INVOICE_DATA.PARTY_SHOP_ID, parameters.getShopIds())
                                .addValue(INVOICE_DATA.INVOICE_ID, parameters.getInvoiceId(), EQUALS)
                                .addInConditionValue(INVOICE_DATA.INVOICE_ID, parameters.getInvoiceIds())
                                .addValue(INVOICE_DATA.EXTERNAL_ID, parameters.getExternalId(), EQUALS)
                                .addValue(INVOICE_DATA.INVOICE_CREATED_AT, whereTime, LESS)
                                .addValue(
                                        INVOICE_DATA.INVOICE_STATUS,
                                        toEnumField(
                                                parameters.getInvoiceStatus(),
                                                dev.vality.magista.domain.enums.InvoiceStatus.class),
                                        EQUALS)
                                .addValue(INVOICE_DATA.INVOICE_AMOUNT, parameters.getInvoiceAmount(), EQUALS)),
                INVOICE_DATA.INVOICE_CREATED_AT,
                fromTime,
                toTime
        );

        ConditionParameterSource paymentParameterSource = new ConditionParameterSource();
        preparePaymentsCondition(paymentParameterSource, parameters);
        if (!paymentParameterSource.getConditionFields().isEmpty()
                || !paymentParameterSource.getOrConditions().isEmpty()) {
            prepareInvoicePaymentsCondition(paymentParameterSource, parameters);
            condition = condition.and(DSL.exists(getDslContext()
                    .select(DSL.field("1"))
                    .from(PAYMENT_DATA)
                    .where(
                            appendDateTimeRangeConditions(
                                    appendConditions(
                                            INVOICE_DATA.INVOICE_ID.eq(PAYMENT_DATA.INVOICE_ID),
                                            Operator.AND,
                                            paymentParameterSource),
                                    PAYMENT_DATA.PAYMENT_CREATED_AT,
                                    fromTime,
                                    toTime))));
        }

        Query query = getDslContext()
                .selectFrom(INVOICE_DATA)
                .where(condition)
                .orderBy(INVOICE_DATA.INVOICE_CREATED_AT.desc())
                .limit(limit);
        return fetch(query, statInvoiceMapper);
    }

    @Override
    public Collection<Map.Entry<Long, StatPayment>> getPayments(
            PaymentsFunction.PaymentsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    ) {
        ConditionParameterSource conditionParameterSource = new ConditionParameterSource();
        prepareInvoicePaymentsCondition(conditionParameterSource, parameters);
        preparePaymentsCondition(conditionParameterSource, parameters);
        conditionParameterSource.addValue(PAYMENT_DATA.PAYMENT_CREATED_AT, whereTime, LESS);

        SelectConditionStep<org.jooq.Record> conditionStep = getDslContext()
                .select()
                .from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(DSL.trueCondition(), Operator.AND, conditionParameterSource),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                );
        Condition excludeCondition = prepareExcludeCondition(parameters);
        if (excludeCondition != null) {
            conditionStep.and(excludeCondition);
        }
        Query query = conditionStep.orderBy(PAYMENT_DATA.PAYMENT_CREATED_AT.desc())
                .limit(limit);

        return fetch(query, statPaymentMapper);
    }

    @Override
    public Collection<Map.Entry<Long, StatRefund>> getRefunds(
            RefundsFunction.RefundsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    ) {
        ConditionParameterSource refundParameterSource = prepareRefundCondition(parameters, whereTime);

        Query query = getDslContext().selectFrom(REFUND_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(DSL.trueCondition(), Operator.AND, refundParameterSource),
                                REFUND_DATA.REFUND_CREATED_AT,
                                fromTime,
                                toTime
                        )
                )
                .orderBy(REFUND_DATA.REFUND_CREATED_AT.desc())
                .limit(limit);
        return fetch(query, statRefundMapper);
    }

    private ConditionParameterSource prepareRefundCondition(RefundsFunction.RefundsParameters parameters,
                                                            LocalDateTime whereTime) {
        return new ConditionParameterSource()
                .addValue(REFUND_DATA.PARTY_ID, parameters.getMerchantId(), EQUALS)
                .addValue(REFUND_DATA.PARTY_SHOP_ID, parameters.getShopId(), EQUALS)
                .addInConditionValue(REFUND_DATA.PARTY_SHOP_ID, parameters.getShopIds())
                .addValue(REFUND_DATA.INVOICE_ID, parameters.getInvoiceId(), EQUALS)
                .addInConditionValue(REFUND_DATA.INVOICE_ID, parameters.getInvoiceIds())
                .addValue(REFUND_DATA.PAYMENT_ID, parameters.getPaymentId(), EQUALS)
                .addValue(REFUND_DATA.REFUND_ID, parameters.getRefundId(), EQUALS)
                .addValue(REFUND_DATA.EXTERNAL_ID, parameters.getExternalId(), EQUALS)
                .addValue(REFUND_DATA.REFUND_CREATED_AT, whereTime, LESS)
                .addValue(REFUND_DATA.REFUND_STATUS,
                        toEnumField(parameters.getRefundStatus(), RefundStatus.class),
                        EQUALS);
    }

    @Override
    public Collection<Map.Entry<Long, StatPayout>> getPayouts(
            PayoutsFunction.PayoutsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    ) {
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(
                                        DSL.trueCondition(),
                                        Operator.AND,
                                        preparePayoutCondition(parameters, whereTime)
                                ),
                                PAYOUT.CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).orderBy(PAYOUT.CREATED_AT.desc())
                .limit(limit);

        return fetch(query, statPayoutMapper);
    }

    private ConditionParameterSource preparePayoutCondition(PayoutsFunction.PayoutsParameters parameters,
                                                            LocalDateTime whereTime) {
        ConditionParameterSource conditionParameterSource = new ConditionParameterSource()
                .addValue(PAYOUT.PARTY_ID, parameters.getMerchantId(), EQUALS)
                .addValue(PAYOUT.SHOP_ID, parameters.getShopId(), EQUALS)
                .addInConditionValue(PAYOUT.SHOP_ID, parameters.getShopIds())
                .addValue(PAYOUT.PAYOUT_ID, parameters.getPayoutId(), EQUALS)
                .addValue(PAYOUT.STATUS,
                        toEnumField(parameters.getPayoutStatus(), PayoutStatus.class),
                        EQUALS)
                .addInConditionValue(PAYOUT.STATUS,
                        toEnumFields(parameters.getPayoutStatuses(),
                                PayoutStatus.class))
                .addValue(PAYOUT.CREATED_AT, whereTime, LESS);
        if (parameters.getPayoutType() != null) {
            switch (parameters.getPayoutType()) {
                case "bank_account" -> conditionParameterSource.addOrCondition(
                        PAYOUT.PAYOUT_TOOL_TYPE.eq(PayoutToolType.russian_bank_account),
                        PAYOUT.PAYOUT_TOOL_TYPE.eq(PayoutToolType.international_bank_account));
                case "wallet_info", "payment_institution_account", "russian_bank_account",
                        "international_bank_account" -> conditionParameterSource.addValue(
                        PAYOUT.PAYOUT_TOOL_TYPE,
                        toEnumField(parameters.getPayoutType(), PayoutToolType.class),
                        EQUALS);
                default -> throw new IllegalArgumentException("Unknown payout_type " + parameters.getPayoutType());
            }
        }
        return conditionParameterSource;
    }

    @Override
    public Collection<Map.Entry<Long, StatChargeback>> getChargebacks(
            ChargebacksFunction.ChargebacksParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit) {
        Query query = getDslContext().selectFrom(CHARGEBACK_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(
                                        DSL.trueCondition(),
                                        Operator.AND,
                                        new ConditionParameterSource()
                                                .addValue(CHARGEBACK_DATA.PARTY_ID, parameters.getMerchantId(), EQUALS)
                                                .addValue(CHARGEBACK_DATA.PARTY_SHOP_ID, parameters.getShopId(), EQUALS)
                                                .addInConditionValue(CHARGEBACK_DATA.PARTY_SHOP_ID,
                                                        parameters.getShopIds())
                                                .addValue(CHARGEBACK_DATA.INVOICE_ID, parameters.getInvoiceId(), EQUALS)
                                                .addInConditionValue(
                                                        CHARGEBACK_DATA.INVOICE_ID,
                                                        parameters.getInvoiceIds())
                                                .addValue(CHARGEBACK_DATA.PAYMENT_ID, parameters.getPaymentId(), EQUALS)
                                                .addValue(CHARGEBACK_DATA.CHARGEBACK_ID, parameters.getChargebackId(),
                                                        EQUALS)
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_STATUS,
                                                        toEnumFields(parameters.getChargebackStatuses(),
                                                                ChargebackStatus.class))
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_STAGE,
                                                        toEnumFields(parameters.getChargebackStages(),
                                                                ChargebackStage.class))
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_REASON_CATEGORY,
                                                        toEnumFields(parameters.getChargebackCategories(),
                                                                ChargebackCategory.class))
                                                .addValue(CHARGEBACK_DATA.CHARGEBACK_CREATED_AT, whereTime, LESS)
                                ),
                                CHARGEBACK_DATA.CHARGEBACK_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).orderBy(CHARGEBACK_DATA.CHARGEBACK_CREATED_AT.desc())
                .limit(limit);

        return fetch(query, statChargebackMapper);
    }

    private ConditionParameterSource preparePaymentsCondition(
            ConditionParameterSource conditionParameterSource,
            PaymentsFunction.PaymentsParameters parameters) {
        conditionParameterSource
                .addValue(PAYMENT_DATA.PAYMENT_ID, parameters.getPaymentId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_STATUS,
                        toEnumField(parameters.getPaymentStatus(),
                                dev.vality.magista.domain.enums.InvoicePaymentStatus.class),
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_FLOW,
                        toEnumField(parameters.getPaymentFlow(), PaymentFlow.class),
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_TOOL, parameters.getPaymentMethod(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER, parameters.getPaymentTerminalProvider(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_EMAIL, parameters.getPaymentEmail(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_IP, parameters.getPaymentIp(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_FINGERPRINT, parameters.getPaymentFingerprint(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_BANK_CARD_FIRST6, parameters.getPaymentBankCardFirst6(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM, parameters.getPaymentBankCardSystem(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_BANK_CARD_LAST4, parameters.getPaymentBankCardLast4(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_CUSTOMER_ID, parameters.getPaymentCustomerId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_PROVIDER_ID, parameters.getPaymentProviderId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_TERMINAL_ID, parameters.getPaymentTerminalId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT, parameters.getPaymentAmount(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION, parameters.getPaymentDomainRevision(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION, parameters.getFromPaymentDomainRevision(),
                        GREATER_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION, parameters.getToPaymentDomainRevision(), LESS_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_RRN, parameters.getPaymentRrn(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_APPROVAL_CODE, parameters.getPaymentApproveCode(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT, parameters.getPaymentAmountFrom(), GREATER_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT, parameters.getPaymentAmountTo(), LESS_OR_EQUAL)
                .addValue(PAYMENT_DATA.EXTERNAL_ID, parameters.getExternalId(), EQUALS);
        if (!ObjectUtils.isEmpty(parameters.getPaymentBankCardTokenProvider())) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER
                            .eq(parameters.getPaymentBankCardTokenProvider()),
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER_LEGACY.eq(
                            toEnumField(parameters.getPaymentBankCardTokenProvider(),
                                    dev.vality.magista.domain.enums.BankCardTokenProvider.class)));
        }
        return conditionParameterSource;
    }

    private ConditionParameterSource prepareInvoicePaymentsCondition(
            ConditionParameterSource conditionParameterSource,
            PaymentsFunction.PaymentsParameters parameters) {
        conditionParameterSource
                .addValue(
                        PAYMENT_DATA.PARTY_ID,
                        Optional.ofNullable(parameters.getMerchantId())
                                .map(UUID::fromString)
                                .orElse(null),
                        EQUALS
                )
                .addValue(PAYMENT_DATA.PARTY_SHOP_ID, parameters.getShopId(), EQUALS)
                .addInConditionValue(PAYMENT_DATA.PARTY_SHOP_ID, parameters.getShopIds())
                .addValue(PAYMENT_DATA.INVOICE_ID, parameters.getInvoiceId(), EQUALS)
                .addInConditionValue(PAYMENT_DATA.INVOICE_ID, parameters.getInvoiceIds());
        return conditionParameterSource;
    }

    private Condition prepareExcludeCondition(PaymentsFunction.PaymentsParameters parameters) {
        Object paramObject = parameters.getExclude();
        Map excludeParam = paramObject instanceof Map ? ((Map) paramObject) : null;
        if (excludeParam != null) {
            Object shopIdParam = excludeParam.get(SHOP_ID_PARAM);
            List<String> excludeShopIds = shopIdParam instanceof List ? ((List) shopIdParam) : null;
            if (excludeShopIds != null && !excludeShopIds.isEmpty()) {
                return PAYMENT_DATA.PARTY_SHOP_ID.notIn(excludeShopIds);
            }
        }
        return null;
    }
}