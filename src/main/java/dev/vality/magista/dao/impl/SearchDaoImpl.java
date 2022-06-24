package dev.vality.magista.dao.impl;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.*;
import dev.vality.magista.constant.SearchConstant;
import dev.vality.magista.dao.SearchDao;
import dev.vality.magista.dao.impl.field.ConditionParameterSource;
import dev.vality.magista.dao.impl.mapper.*;
import dev.vality.magista.domain.enums.InvoicePaymentStatus;
import dev.vality.magista.domain.enums.PayoutStatus;
import dev.vality.magista.domain.enums.PayoutToolType;
import dev.vality.magista.domain.enums.*;
import dev.vality.magista.service.TimeHolder;
import dev.vality.magista.service.TokenGenService;
import org.jooq.Condition;
import org.jooq.Operator;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static dev.vality.geck.common.util.TypeUtil.toEnumField;
import static dev.vality.geck.common.util.TypeUtil.toEnumFields;
import static dev.vality.magista.domain.tables.ChargebackData.CHARGEBACK_DATA;
import static dev.vality.magista.domain.tables.InvoiceData.INVOICE_DATA;
import static dev.vality.magista.domain.tables.InvoiceTemplate.INVOICE_TEMPLATE;
import static dev.vality.magista.domain.tables.PaymentData.PAYMENT_DATA;
import static dev.vality.magista.domain.tables.Payout.PAYOUT;
import static dev.vality.magista.domain.tables.RefundData.REFUND_DATA;
import static org.jooq.Comparator.*;

@Component
public class SearchDaoImpl extends AbstractDao implements SearchDao {

    private final StatInvoiceMapper statInvoiceMapper;
    private final StatPaymentMapper statPaymentMapper;
    private final StatRefundMapper statRefundMapper;
    private final StatPayoutMapper statPayoutMapper;
    private final StatChargebackMapper statChargebackMapper;
    private final StatInvoiceTemplateMapper statInvoiceTemplateMapper;
    private final TokenGenService tokenGenService;

    public SearchDaoImpl(DataSource ds, TokenGenService tokenGenService) {
        super(ds);
        this.tokenGenService = tokenGenService;
        statInvoiceMapper = new StatInvoiceMapper();
        statPaymentMapper = new StatPaymentMapper();
        statRefundMapper = new StatRefundMapper();
        statPayoutMapper = new StatPayoutMapper();
        statChargebackMapper = new StatChargebackMapper();
        statInvoiceTemplateMapper = new StatInvoiceTemplateMapper();
    }

    @Override
    public List<StatInvoice> getInvoices(InvoiceSearchQuery searchQuery) {
        CommonSearchQueryParams commonParams = searchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        Condition condition = appendDateTimeRangeConditions(
                appendConditions(DSL.trueCondition(), Operator.AND,
                        new ConditionParameterSource()
                                .addValue(
                                        INVOICE_DATA.PARTY_ID,
                                        Optional.ofNullable(commonParams.getPartyId())
                                                .map(UUID::fromString)
                                                .orElse(null),
                                        EQUALS)
                                .addInConditionValue(INVOICE_DATA.PARTY_SHOP_ID, commonParams.getShopIds())
                                .addInConditionValue(INVOICE_DATA.INVOICE_ID, searchQuery.getInvoiceIds())
                                .addValue(INVOICE_DATA.EXTERNAL_ID, searchQuery.getExternalId(), EQUALS)
                                .addValue(INVOICE_DATA.INVOICE_CREATED_AT, timeHolder.getWhereTime(), LESS)
                                .addValue(
                                        INVOICE_DATA.INVOICE_STATUS,
                                        searchQuery.isSetInvoiceStatus()
                                                ? TypeUtil.toEnumField(
                                                searchQuery.getInvoiceStatus().name(),
                                                dev.vality.magista.domain.enums.InvoiceStatus.class)
                                                : null,
                                        EQUALS)
                                .addValue(INVOICE_DATA.INVOICE_AMOUNT,
                                        searchQuery.isSetInvoiceAmount() ? searchQuery.getInvoiceAmount() : null,
                                        EQUALS)
                ),
                INVOICE_DATA.INVOICE_CREATED_AT,
                timeHolder.getFromTime(),
                timeHolder.getToTime()
        );

        ConditionParameterSource paymentParameterSource = new ConditionParameterSource();
        preparePaymentsCondition(paymentParameterSource, searchQuery.getPaymentParams(), searchQuery.getExternalId());
        if (!paymentParameterSource.getConditionFields().isEmpty()
                || !paymentParameterSource.getOrConditions().isEmpty()) {
            prepareInvoicePaymentsCondition(paymentParameterSource, commonParams, searchQuery.getInvoiceIds());
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
                                    timeHolder.getFromTime(),
                                    timeHolder.getToTime()))));
        }

        Query query = getDslContext()
                .selectFrom(INVOICE_DATA)
                .where(condition)
                .orderBy(INVOICE_DATA.INVOICE_CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);
        return fetch(query, statInvoiceMapper);
    }

    @Override
    public List<StatPayment> getPayments(PaymentSearchQuery searchQuery) {
        CommonSearchQueryParams commonParams = searchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        PaymentParams paymentParams = searchQuery.getPaymentParams();
        ConditionParameterSource conditionParameterSource = new ConditionParameterSource();
        prepareInvoicePaymentsCondition(conditionParameterSource, commonParams, searchQuery.getInvoiceIds());
        preparePaymentsCondition(conditionParameterSource, paymentParams, searchQuery.getExternalId());
        conditionParameterSource.addValue(PAYMENT_DATA.PAYMENT_CREATED_AT, timeHolder.getWhereTime(), LESS);

        SelectConditionStep<org.jooq.Record> conditionStep = getDslContext()
                .select()
                .from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(DSL.trueCondition(), Operator.AND, conditionParameterSource),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                timeHolder.getFromTime(),
                                timeHolder.getToTime()
                        )
                );
        if (searchQuery.isSetExcludedShopIds()) {
            conditionStep.and(PAYMENT_DATA.PARTY_SHOP_ID.notIn(searchQuery.getExcludedShopIds()));
        }
        Query query = conditionStep.orderBy(PAYMENT_DATA.PAYMENT_CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);

        return fetch(query, statPaymentMapper);
    }

    @Override
    public List<StatRefund> getRefunds(RefundSearchQuery refundSearchQuery) {
        CommonSearchQueryParams commonParams = refundSearchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        ConditionParameterSource refundParameterSource = prepareRefundCondition(refundSearchQuery, timeHolder);
        Query query = getDslContext().selectFrom(REFUND_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(DSL.trueCondition(), Operator.AND, refundParameterSource),
                                REFUND_DATA.REFUND_CREATED_AT,
                                timeHolder.getFromTime(),
                                timeHolder.getToTime()
                        )
                )
                .orderBy(REFUND_DATA.REFUND_CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);
        return fetch(query, statRefundMapper);
    }

    @Override
    public List<StatPayout> getPayouts(PayoutSearchQuery payoutSearchQuery) {
        CommonSearchQueryParams commonParams = payoutSearchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(
                        appendDateTimeRangeConditions(
                                appendConditions(
                                        DSL.trueCondition(),
                                        Operator.AND,
                                        preparePayoutCondition(payoutSearchQuery, timeHolder)
                                ),
                                PAYOUT.CREATED_AT,
                                timeHolder.getFromTime(),
                                timeHolder.getToTime()
                        )
                ).orderBy(PAYOUT.CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);

        return fetch(query, statPayoutMapper);
    }

    @Override
    public List<StatChargeback> getChargebacks(ChargebackSearchQuery chargebackSearchQuery) {
        CommonSearchQueryParams commonParams = chargebackSearchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        Query query = getDslContext().selectFrom(CHARGEBACK_DATA).where(
                        appendDateTimeRangeConditions(appendConditions(
                                        DSL.trueCondition(),
                                        Operator.AND,
                                        new ConditionParameterSource()
                                                .addValue(CHARGEBACK_DATA.PARTY_ID, commonParams.getPartyId(), EQUALS)
                                                .addInConditionValue(CHARGEBACK_DATA.PARTY_SHOP_ID,
                                                        commonParams.getShopIds())
                                                .addInConditionValue(
                                                        CHARGEBACK_DATA.INVOICE_ID,
                                                        chargebackSearchQuery.getInvoiceIds())
                                                .addValue(CHARGEBACK_DATA.PAYMENT_ID,
                                                        chargebackSearchQuery.getPaymentId(), EQUALS)
                                                .addValue(CHARGEBACK_DATA.CHARGEBACK_ID,
                                                        chargebackSearchQuery.getChargebackId(),
                                                        EQUALS)
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_STATUS,
                                                        chargebackSearchQuery.isSetChargebackStatuses()
                                                                ? toEnumFields(
                                                                chargebackSearchQuery.getChargebackStatuses()
                                                                        .stream()
                                                                        .map(cs -> cs.getSetField().getFieldName())
                                                                        .collect(Collectors.toList()),
                                                                ChargebackStatus.class)
                                                                : null)
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_STAGE,
                                                        chargebackSearchQuery.isSetChargebackStages()
                                                                ? toEnumFields(
                                                                chargebackSearchQuery.getChargebackStages()
                                                                        .stream()
                                                                        .map(cs -> cs.getSetField().getFieldName())
                                                                        .collect(Collectors.toList()),
                                                                ChargebackStage.class)
                                                                : null)
                                                .addInConditionValue(CHARGEBACK_DATA.CHARGEBACK_REASON_CATEGORY,
                                                        chargebackSearchQuery.isSetChargebackCategories()
                                                                ? toEnumFields(
                                                                chargebackSearchQuery.getChargebackCategories()
                                                                        .stream()
                                                                        .map(cc -> cc.getSetField().getFieldName())
                                                                        .collect(Collectors.toList()),
                                                                ChargebackCategory.class)
                                                                : null)
                                                .addValue(CHARGEBACK_DATA.CHARGEBACK_CREATED_AT,
                                                        timeHolder.getWhereTime(), LESS)
                                ),
                                CHARGEBACK_DATA.CHARGEBACK_CREATED_AT,
                                timeHolder.getFromTime(),
                                timeHolder.getToTime()
                        )
                ).orderBy(CHARGEBACK_DATA.CHARGEBACK_CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);

        return fetch(query, statChargebackMapper);
    }

    @Override
    public List<StatInvoiceTemplate> getInvoiceTemplates(InvoiceTemplateSearchQuery invoiceTemplateSearchQuery) {
        CommonSearchQueryParams commonParams = invoiceTemplateSearchQuery.getCommonSearchQueryParams();
        TimeHolder timeHolder = buildTimeHolder(commonParams);
        Condition invoiceTemplateStatusCondition = buildInvoiceTemplateStatusCondition(invoiceTemplateSearchQuery);
        Query query = getDslContext()
                .selectFrom(INVOICE_TEMPLATE)
                .where(appendDateTimeRangeConditions(
                        appendConditions(
                                invoiceTemplateStatusCondition,
                                Operator.AND,
                                new ConditionParameterSource()
                                        .addValue(INVOICE_TEMPLATE.PARTY_ID, commonParams.getPartyId(), EQUALS)
                                        .addInConditionValue(INVOICE_TEMPLATE.SHOP_ID, commonParams.getShopIds())
                                        .addValue(
                                                INVOICE_TEMPLATE.INVOICE_TEMPLATE_ID,
                                                invoiceTemplateSearchQuery.getInvoiceTemplateId(),
                                                EQUALS)
                                        .addValue(
                                                INVOICE_TEMPLATE.INVOICE_VALID_UNTIL,
                                                invoiceTemplateSearchQuery.isSetInvoiceValidUntil()
                                                        ? TypeUtil.stringToLocalDateTime(
                                                        invoiceTemplateSearchQuery.getInvoiceValidUntil())
                                                        : null,
                                                GREATER_OR_EQUAL)
                                        .addValue(
                                                INVOICE_TEMPLATE.PRODUCT,
                                                invoiceTemplateSearchQuery.getProduct(),
                                                EQUALS)
                                        .addValue(INVOICE_TEMPLATE.EVENT_CREATED_AT, timeHolder.getWhereTime(), LESS)
                                        .addValue(INVOICE_TEMPLATE.NAME, invoiceTemplateSearchQuery.getName(), EQUALS)),
                        INVOICE_TEMPLATE.EVENT_CREATED_AT,
                        timeHolder.getFromTime(),
                        timeHolder.getToTime()))
                .orderBy(INVOICE_TEMPLATE.EVENT_CREATED_AT.desc())
                .limit(commonParams.isSetLimit() ? commonParams.getLimit() : SearchConstant.LIMIT);
        return fetch(query, statInvoiceTemplateMapper);
    }

    private ConditionParameterSource prepareRefundCondition(RefundSearchQuery searchQuery, TimeHolder timeHolder) {
        CommonSearchQueryParams commonParams = searchQuery.getCommonSearchQueryParams();
        return new ConditionParameterSource()
                .addValue(REFUND_DATA.PARTY_ID, commonParams.getPartyId(), EQUALS)
                .addInConditionValue(REFUND_DATA.PARTY_SHOP_ID, commonParams.getShopIds())
                .addInConditionValue(REFUND_DATA.INVOICE_ID, searchQuery.getInvoiceIds())
                .addValue(REFUND_DATA.PAYMENT_ID, searchQuery.getPaymentId(), EQUALS)
                .addValue(REFUND_DATA.REFUND_ID, searchQuery.getRefundId(), EQUALS)
                .addValue(REFUND_DATA.EXTERNAL_ID, searchQuery.getExternalId(), EQUALS)
                .addValue(REFUND_DATA.REFUND_CREATED_AT, timeHolder.getWhereTime(), LESS)
                .addValue(REFUND_DATA.REFUND_STATUS,
                        searchQuery.isSetRefundStatus()
                                ? TypeUtil.toEnumField(searchQuery.getRefundStatus().name(), RefundStatus.class)
                                : null,
                        EQUALS);
    }

    private ConditionParameterSource preparePayoutCondition(PayoutSearchQuery payoutSearchQuery,
                                                            TimeHolder timeHolder) {
        CommonSearchQueryParams commonParams = payoutSearchQuery.getCommonSearchQueryParams();
        ConditionParameterSource conditionParameterSource = new ConditionParameterSource()
                .addValue(PAYOUT.PARTY_ID, commonParams.getPartyId(), EQUALS)
                .addInConditionValue(PAYOUT.SHOP_ID, commonParams.getShopIds())
                .addValue(PAYOUT.PAYOUT_ID, payoutSearchQuery.getPayoutId(), EQUALS)
                .addValue(PAYOUT.CREATED_AT, timeHolder.getWhereTime(), LESS);
        if (payoutSearchQuery.getPayoutStatusTypes() != null
                && !payoutSearchQuery.getPayoutStatusTypes().isEmpty()) {
            conditionParameterSource.addInConditionValue(
                    PAYOUT.STATUS,
                    toEnumFields(payoutSearchQuery.getPayoutStatusTypes().stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toList()),
                            PayoutStatus.class));
        }
        if (payoutSearchQuery.getPayoutType() != null) {
            switch (payoutSearchQuery.getPayoutType()) {
                case payout_account -> conditionParameterSource.addOrCondition(
                        PAYOUT.PAYOUT_TOOL_TYPE.eq(PayoutToolType.russian_bank_account),
                        PAYOUT.PAYOUT_TOOL_TYPE.eq(PayoutToolType.international_bank_account));
                case wallet -> conditionParameterSource.addValue(
                        PAYOUT.PAYOUT_TOOL_TYPE,
                        PayoutToolType.wallet_info,
                        EQUALS);
                case payment_institution_account -> conditionParameterSource.addValue(
                        PAYOUT.PAYOUT_TOOL_TYPE,
                        PayoutToolType.payment_institution_account,
                        EQUALS);
                default -> throw new IllegalArgumentException("Unknown payout_type " +
                        payoutSearchQuery.getPayoutType());
            }
        }
        return conditionParameterSource;
    }

    private ConditionParameterSource preparePaymentsCondition(ConditionParameterSource conditionParameterSource,
                                                              PaymentParams paymentParams,
                                                              String externalId) {
        conditionParameterSource
                .addValue(PAYMENT_DATA.PAYMENT_ID, paymentParams.getPaymentId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_STATUS,
                        paymentParams.isSetPaymentStatus()
                                ? TypeUtil.toEnumField(paymentParams.getPaymentStatus().name(),
                                InvoicePaymentStatus.class)
                                : null,
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_FLOW,
                        paymentParams.isSetPaymentFlow()
                                ? TypeUtil.toEnumField(paymentParams.getPaymentFlow().name(), PaymentFlow.class)
                                : null,
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_TOOL,
                        paymentParams.isSetPaymentTool()
                                ? TypeUtil.toEnumField(paymentParams.getPaymentTool().name(), PaymentTool.class)
                                : null,
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_EMAIL, paymentParams.getPaymentEmail(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_IP, paymentParams.getPaymentIp(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_FINGERPRINT, paymentParams.getPaymentFingerprint(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_BANK_CARD_FIRST6, paymentParams.getPaymentFirst6(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_BANK_CARD_LAST4, paymentParams.getPaymentLast4(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_CUSTOMER_ID, paymentParams.getPaymentCustomerId(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_PROVIDER_ID,
                        paymentParams.isSetPaymentProviderId()
                                ? Integer.valueOf(paymentParams.getPaymentProviderId())
                                : null, EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_TERMINAL_ID,
                        paymentParams.isSetPaymentTerminalId()
                                ? Integer.valueOf(paymentParams.getPaymentTerminalId())
                                : null, EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT,
                        paymentParams.isSetPaymentAmount() ? paymentParams.getPaymentAmount() : null, EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION,
                        paymentParams.isSetPaymentDomainRevision()
                                ? paymentParams.getPaymentDomainRevision()
                                : null,
                        EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION,
                        paymentParams.isSetFromPaymentDomainRevision()
                                ? paymentParams.getFromPaymentDomainRevision()
                                : null,
                        GREATER_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION,
                        paymentParams.isSetToPaymentDomainRevision()
                                ? paymentParams.getToPaymentDomainRevision()
                                : null,
                        LESS_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_RRN, paymentParams.getPaymentRrn(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_APPROVAL_CODE, paymentParams.getPaymentApprovalCode(), EQUALS)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT,
                        paymentParams.isSetPaymentAmountFrom()
                                ? paymentParams.getPaymentAmountFrom()
                                : null,
                        GREATER_OR_EQUAL)
                .addValue(PAYMENT_DATA.PAYMENT_AMOUNT,
                        paymentParams.isSetPaymentAmountTo()
                                ? paymentParams.getPaymentAmountTo()
                                : null,
                        LESS_OR_EQUAL)
                .addValue(PAYMENT_DATA.EXTERNAL_ID, externalId, EQUALS);
        if (paymentParams.isSetPaymentTokenProvider()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER
                            .eq(paymentParams.getPaymentTokenProvider().getId()),
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER_LEGACY.eq(
                            toEnumField(paymentParams.getPaymentTokenProvider().getId(),
                                    dev.vality.magista.domain.enums.BankCardTokenProvider.class)));
        }
        if (paymentParams.isSetLegacyPaymentTokenProvider()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER
                            .eq(paymentParams.getLegacyPaymentTokenProvider().name()),
                    PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER_LEGACY.eq(
                            toEnumField(paymentParams.getLegacyPaymentTokenProvider().name(),
                                    dev.vality.magista.domain.enums.BankCardTokenProvider.class)));
        }
        if (paymentParams.isSetPaymentTerminalProvider()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER
                            .eq(paymentParams.getPaymentTerminalProvider().getId()));
        }
        if (paymentParams.isSetLegacyPaymentTerminalProvider()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER
                            .eq(paymentParams.getLegacyPaymentTerminalProvider().name()));
        }
        if (paymentParams.isSetPaymentSystem()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM
                            .eq(paymentParams.getPaymentSystem().getId()));
        }
        if (paymentParams.isSetLegacyPaymentSystem()) {
            conditionParameterSource.addOrCondition(
                    PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM
                            .eq(paymentParams.getLegacyPaymentSystem().name()));
        }
        return conditionParameterSource;
    }

    private ConditionParameterSource prepareInvoicePaymentsCondition(ConditionParameterSource paymentParameterSource,
                                                                     CommonSearchQueryParams commonParams,
                                                                     List<String> invoiceIds) {
        paymentParameterSource
                .addValue(PAYMENT_DATA.PARTY_ID, UUID.fromString(commonParams.getPartyId()), EQUALS)
                .addInConditionValue(PAYMENT_DATA.PARTY_SHOP_ID, commonParams.getShopIds())
                .addInConditionValue(PAYMENT_DATA.INVOICE_ID, invoiceIds);
        return paymentParameterSource;
    }

    private Condition buildInvoiceTemplateStatusCondition(InvoiceTemplateSearchQuery invoiceTemplateSearchQuery) {
        Condition invoiceTemplateStatus = DSL.trueCondition();
        if (invoiceTemplateSearchQuery.isSetInvoiceTemplateStatus()) {
            switch (invoiceTemplateSearchQuery.getInvoiceTemplateStatus()) {
                case created -> invoiceTemplateStatus = INVOICE_TEMPLATE.EVENT_TYPE.in(
                        InvoiceTemplateEventType.INVOICE_TEMPLATE_CREATED,
                        InvoiceTemplateEventType.INVOICE_TEMPLATE_UPDATED);
                case deleted -> invoiceTemplateStatus = INVOICE_TEMPLATE.EVENT_TYPE.eq(
                        InvoiceTemplateEventType.INVOICE_TEMPLATE_DELETED);
                default -> throw new IllegalArgumentException("Unknown enum type " +
                        invoiceTemplateSearchQuery.getInvoiceTemplateStatus());
            }
        }
        return invoiceTemplateStatus;
    }

    private TimeHolder buildTimeHolder(CommonSearchQueryParams commonParams) {
        return TimeHolder.builder()
                .fromTime(TypeUtil.stringToLocalDateTime(commonParams.getFromTime()))
                .toTime(TypeUtil.stringToLocalDateTime(commonParams.getToTime()))
                .whereTime(tokenGenService.extractTime(commonParams.getContinuationToken()))
                .build();
    }
}
