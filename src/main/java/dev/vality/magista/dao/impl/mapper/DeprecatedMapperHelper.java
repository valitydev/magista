package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.merch_stat.BankCard;
import dev.vality.damsel.merch_stat.CryptoCurrency;
import dev.vality.damsel.merch_stat.CustomerPayer;
import dev.vality.damsel.merch_stat.DigitalWallet;
import dev.vality.damsel.merch_stat.InvoiceCancelled;
import dev.vality.damsel.merch_stat.InvoiceFulfilled;
import dev.vality.damsel.merch_stat.InvoicePaid;
import dev.vality.damsel.merch_stat.InvoicePaymentCancelled;
import dev.vality.damsel.merch_stat.InvoicePaymentCaptured;
import dev.vality.damsel.merch_stat.InvoicePaymentChargedBack;
import dev.vality.damsel.merch_stat.InvoicePaymentFailed;
import dev.vality.damsel.merch_stat.InvoicePaymentFlow;
import dev.vality.damsel.merch_stat.InvoicePaymentFlowHold;
import dev.vality.damsel.merch_stat.InvoicePaymentFlowInstant;
import dev.vality.damsel.merch_stat.InvoicePaymentPending;
import dev.vality.damsel.merch_stat.InvoicePaymentProcessed;
import dev.vality.damsel.merch_stat.InvoicePaymentRefundFailed;
import dev.vality.damsel.merch_stat.InvoicePaymentRefundPending;
import dev.vality.damsel.merch_stat.InvoicePaymentRefundStatus;
import dev.vality.damsel.merch_stat.InvoicePaymentRefundSucceeded;
import dev.vality.damsel.merch_stat.InvoicePaymentRefunded;
import dev.vality.damsel.merch_stat.InvoicePaymentStatus;
import dev.vality.damsel.merch_stat.InvoiceStatus;
import dev.vality.damsel.merch_stat.InvoiceUnpaid;
import dev.vality.damsel.merch_stat.MobileCommerce;
import dev.vality.damsel.merch_stat.MobileOperator;
import dev.vality.damsel.merch_stat.MobilePhone;
import dev.vality.damsel.merch_stat.OnHoldExpiration;
import dev.vality.damsel.merch_stat.OperationFailure;
import dev.vality.damsel.merch_stat.Payer;
import dev.vality.damsel.merch_stat.PaymentResourcePayer;
import dev.vality.damsel.merch_stat.PaymentTerminal;
import dev.vality.damsel.merch_stat.PaymentTool;
import dev.vality.damsel.merch_stat.PayoutStatus;
import dev.vality.damsel.merch_stat.RecurrentParentPayment;
import dev.vality.damsel.merch_stat.RecurrentPayer;
import dev.vality.damsel.merch_stat.*;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.domain.enums.*;
import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.util.DamselUtil;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

import static dev.vality.magista.domain.Tables.CHARGEBACK_DATA;
import static dev.vality.magista.domain.tables.InvoiceData.INVOICE_DATA;
import static dev.vality.magista.domain.tables.PaymentData.PAYMENT_DATA;
import static dev.vality.magista.domain.tables.Payout.PAYOUT;
import static dev.vality.magista.domain.tables.RefundData.REFUND_DATA;

@SuppressWarnings("DuplicatedCode")
@Deprecated
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DeprecatedMapperHelper {

    static InvoiceStatus mapInvoiceStatus(ResultSet rs,
                                          dev.vality.magista.domain.enums.InvoiceStatus invoiceStatusType,
                                          String eventCreatedAtString) throws SQLException {
        InvoiceStatus invoiceStatus;
        switch (invoiceStatusType) {
            case cancelled -> {
                InvoiceCancelled invoiceCancelled = new InvoiceCancelled();
                invoiceCancelled.setDetails(rs.getString(INVOICE_DATA.INVOICE_STATUS_DETAILS.getName()));
                invoiceCancelled.setAt(eventCreatedAtString);
                invoiceStatus = InvoiceStatus.cancelled(invoiceCancelled);
            }
            case unpaid -> invoiceStatus = InvoiceStatus.unpaid(new InvoiceUnpaid());
            case paid -> {
                InvoicePaid invoicePaid = new InvoicePaid();
                invoicePaid.setAt(eventCreatedAtString);
                invoiceStatus = InvoiceStatus.paid(invoicePaid);
            }
            case fulfilled -> {
                InvoiceFulfilled invoiceFulfilled = new InvoiceFulfilled();
                invoiceFulfilled.setAt(eventCreatedAtString);
                invoiceFulfilled.setDetails(rs.getString(INVOICE_DATA.INVOICE_STATUS_DETAILS.getName()));
                invoiceStatus = InvoiceStatus.fulfilled(invoiceFulfilled);
            }
            default -> throw new NotFoundException(
                    String.format("Invoice status '%s' not found", invoiceStatusType.getLiteral()));
        }
        return invoiceStatus;
    }

    static PaymentTool buildPaymentTool(ResultSet rs) throws SQLException {
        dev.vality.magista.domain.enums.PaymentTool paymentToolType = TypeUtil.toEnumField(
                rs.getString(PAYMENT_DATA.PAYMENT_TOOL.getName()),
                dev.vality.magista.domain.enums.PaymentTool.class
        );

        switch (paymentToolType) {
            case bank_card:
                BankCard bankCard = new BankCard(
                        rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN.getName()),
                        rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_FIRST6.getName()),
                        rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_LAST4.getName())
                );
                bankCard.setPaymentSystem(
                        new PaymentSystemRef(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.getName()))
                );
                bankCard.setPaymentSystemDeprecated(
                        Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.getName()))
                                .map(bankCardSystem ->
                                        TypeUtil.toEnumField(bankCardSystem, LegacyBankCardPaymentSystem.class))
                                .orElse(null)
                );
                String tokenProvider = rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER.getName());
                if (StringUtils.isNotEmpty(tokenProvider)) {
                    bankCard.setPaymentToken(new BankCardTokenServiceRef(tokenProvider));
                }
                bankCard.setTokenProviderDeprecated(
                        Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER.getName()))
                                .map(bankCardTokenProvider -> TypeUtil
                                        .toEnumField(bankCardTokenProvider, LegacyBankCardTokenProvider.class))
                                .orElse(null)
                );
                return PaymentTool.bank_card(bankCard);
            case payment_terminal:
                PaymentTerminal paymentTerminal = new PaymentTerminal();
                paymentTerminal.setTerminalTypeDeprecated(
                        Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER.getName()))
                                .map(paymentTerminalProvider -> TypeUtil.toEnumField(
                                        paymentTerminalProvider,
                                        TerminalPaymentProvider.class))
                                .orElse(null));

                return PaymentTool.payment_terminal(paymentTerminal);
            case digital_wallet:
                return PaymentTool.digital_wallet(new DigitalWallet(
                        TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_PROVIDER.getName()),
                                DigitalWalletProvider.class),
                        rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_ID.getName())
                ));
            case crypto_currency:
                return PaymentTool.crypto_currency(
                        TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.CRYPTO_CURRENCY.getName()),
                                CryptoCurrency.class));
            case mobile_commerce:
                MobilePhone mobilePhone = new MobilePhone(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CC.getName()),
                        rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CTN.getName()));
                MobileOperator mobileOperator =
                        TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_OPERATOR.getName()),
                                MobileOperator.class);
                return PaymentTool.mobile_commerce(new MobileCommerce(mobileOperator, mobilePhone));
            default:
                throw new NotFoundException(String.format("Payment tool '%s' not found", paymentToolType));
        }
    }

    static Payer buildPayer(ResultSet rs) throws SQLException {
        PaymentPayerType paymentPayerType = TypeUtil.toEnumField(
                rs.getString(PAYMENT_DATA.PAYMENT_PAYER_TYPE.getName()),
                PaymentPayerType.class
        );

        switch (paymentPayerType) {
            case payment_resource -> {
                PaymentResourcePayer paymentResourcePayer = new PaymentResourcePayer();
                paymentResourcePayer.setIpAddress(rs.getString(PAYMENT_DATA.PAYMENT_IP.getName()));
                paymentResourcePayer.setFingerprint(rs.getString(PAYMENT_DATA.PAYMENT_FINGERPRINT.getName()));
                paymentResourcePayer.setPhoneNumber(rs.getString(PAYMENT_DATA.PAYMENT_PHONE_NUMBER.getName()));
                paymentResourcePayer.setEmail(rs.getString(PAYMENT_DATA.PAYMENT_EMAIL.getName()));
                paymentResourcePayer.setSessionId(rs.getString(PAYMENT_DATA.PAYMENT_SESSION_ID.getName()));
                paymentResourcePayer.setPaymentTool(buildPaymentTool(rs));
                return Payer.payment_resource(paymentResourcePayer);
            }
            case customer -> {
                CustomerPayer customerPayer = new CustomerPayer();
                customerPayer.setCustomerId(rs.getString(PAYMENT_DATA.PAYMENT_CUSTOMER_ID.getName()));
                customerPayer.setPaymentTool(buildPaymentTool(rs));
                customerPayer.setEmail(rs.getString(PAYMENT_DATA.PAYMENT_EMAIL.getName()));
                customerPayer.setPhoneNumber(rs.getString(PAYMENT_DATA.PAYMENT_PHONE_NUMBER.getName()));
                return Payer.customer(customerPayer);
            }
            case recurrent -> {
                RecurrentPayer recurrentPayer = new RecurrentPayer();
                recurrentPayer.setEmail(rs.getString(PAYMENT_DATA.PAYMENT_EMAIL.getName()));
                recurrentPayer.setPhoneNumber(rs.getString(PAYMENT_DATA.PAYMENT_PHONE_NUMBER.getName()));
                recurrentPayer.setPaymentTool(buildPaymentTool(rs));
                RecurrentParentPayment recurrentParentPayment = new RecurrentParentPayment();
                recurrentParentPayment
                        .setInvoiceId(rs.getString(PAYMENT_DATA.PAYMENT_RECURRENT_PAYER_PARENT_INVOICE_ID.getName()));
                recurrentParentPayment
                        .setPaymentId(rs.getString(PAYMENT_DATA.PAYMENT_RECURRENT_PAYER_PARENT_PAYMENT_ID.getName()));
                recurrentPayer.setRecurrentParent(recurrentParentPayment);
                return Payer.recurrent(recurrentPayer);
            }
            default -> throw new NotFoundException(String.format("Payment type '%s' not found", paymentPayerType));
        }
    }

    static void buildStatPaymentFlow(ResultSet rs, StatPayment statPayment, PaymentFlow paymentFlow)
            throws SQLException {
        switch (paymentFlow) {
            case hold -> {
                InvoicePaymentFlowHold invoicePaymentFlowHold = new InvoicePaymentFlowHold(
                        TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_HOLD_ON_EXPIRATION.getName()),
                                OnHoldExpiration.class),
                        TypeUtil.temporalToString(
                                rs.getObject(PAYMENT_DATA.PAYMENT_HOLD_UNTIL.getName(), LocalDateTime.class)
                        )
                );
                statPayment.setFlow(InvoicePaymentFlow.hold(invoicePaymentFlowHold));
            }
            case instant -> statPayment.setFlow(InvoicePaymentFlow.instant(new InvoicePaymentFlowInstant()));
            default -> throw new NotFoundException(
                    String.format("Payment flow '%s' not found", paymentFlow.getLiteral()));
        }
    }

    static InvoicePaymentStatus buildInvoicePaymentStatus(
            ResultSet rs,
            String eventCreatedAtString,
            dev.vality.magista.domain.enums.InvoicePaymentStatus invoicePaymentStatus) throws SQLException {
        InvoicePaymentStatus paymentStatus;
        switch (invoicePaymentStatus) {
            case pending -> paymentStatus = InvoicePaymentStatus.pending(new InvoicePaymentPending());
            case cancelled -> {
                InvoicePaymentCancelled invoicePaymentCancelled = new InvoicePaymentCancelled();
                invoicePaymentCancelled.setAt(eventCreatedAtString);
                paymentStatus = InvoicePaymentStatus.cancelled(invoicePaymentCancelled);
            }
            case failed -> {
                InvoicePaymentFailed invoicePaymentFailed = new InvoicePaymentFailed();
                invoicePaymentFailed.setAt(eventCreatedAtString);
                OperationFailure operationFailure = DamselUtil.toOperationFailureDeprecated(
                        TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_OPERATION_FAILURE_CLASS.getName()),
                                FailureClass.class),
                        rs.getString(PAYMENT_DATA.PAYMENT_EXTERNAL_FAILURE.getName()),
                        rs.getString(PAYMENT_DATA.PAYMENT_EXTERNAL_FAILURE_REASON.getName())
                );
                invoicePaymentFailed.setFailure(operationFailure);
                paymentStatus = InvoicePaymentStatus.failed(invoicePaymentFailed);
            }
            case captured -> {
                InvoicePaymentCaptured invoicePaymentCaptured = new InvoicePaymentCaptured();
                invoicePaymentCaptured.setAt(eventCreatedAtString);
                paymentStatus = InvoicePaymentStatus.captured(invoicePaymentCaptured);
            }
            case refunded -> {
                InvoicePaymentRefunded invoicePaymentRefunded = new InvoicePaymentRefunded();
                invoicePaymentRefunded.setAt(eventCreatedAtString);
                paymentStatus = InvoicePaymentStatus.refunded(invoicePaymentRefunded);
            }
            case processed -> {
                InvoicePaymentProcessed invoicePaymentProcessed = new InvoicePaymentProcessed();
                invoicePaymentProcessed.setAt(eventCreatedAtString);
                paymentStatus = InvoicePaymentStatus.processed(invoicePaymentProcessed);
            }
            case charged_back -> {
                InvoicePaymentProcessed invoicePaymentChargeback = new InvoicePaymentProcessed();
                invoicePaymentChargeback.setAt(eventCreatedAtString);
                paymentStatus = InvoicePaymentStatus.charged_back(new InvoicePaymentChargedBack());
            }
            default -> throw new NotFoundException(
                    String.format("Payment status '%s' not found", invoicePaymentStatus.getLiteral()));
        }
        return paymentStatus;
    }

    static PayoutToolInfo toPayoutToolInfo(ResultSet rs) throws SQLException {
        var payoutType = TypeUtil.toEnumField(rs.getString(PAYOUT.PAYOUT_TOOL_TYPE.getName()),
                dev.vality.magista.domain.enums.PayoutToolType.class);
        return switch (payoutType) {
            case russian_bank_account -> PayoutToolInfo.russian_bank_account(new RussianBankAccount()
                    .setAccount(rs.getString(PAYOUT.PAYOUT_TOOL_RUSSIAN_BANK_ACCOUNT_ACCOUNT.getName()))
                    .setBankName(rs.getString(PAYOUT.PAYOUT_TOOL_RUSSIAN_BANK_ACCOUNT_BANK_NAME.getName()))
                    .setBankBik(rs.getString(PAYOUT.PAYOUT_TOOL_RUSSIAN_BANK_ACCOUNT_BANK_BIK.getName()))
                    .setBankPostAccount(
                            rs.getString(PAYOUT.PAYOUT_TOOL_RUSSIAN_BANK_ACCOUNT_BANK_POST_ACCOUNT.getName()))
            );
            case international_bank_account -> PayoutToolInfo.international_bank_account(
                    new InternationalBankAccount()
                            .setBank(new InternationalBankDetails()
                                    .setName(rs.getString(
                                            PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_NAME.getName()))
                                    .setCountry(TypeUtil.toEnumField(
                                            rs.getString(PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_NAME
                                                    .getName()),
                                            CountryCode.class))
                                    .setBic(rs.getString(PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_BIC
                                            .getName()))
                                    .setAddress(rs.getString(
                                            PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_ADDRESS.getName()))
                                    .setAbaRtn(rs.getString(
                                            PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_ABA_RTN.getName())))
                            .setIban(rs.getString(PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_IBAN.getName()))
                            .setNumber(rs.getString(PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_NUMBER.getName()))
                            .setCorrespondentAccount(new InternationalBankAccount()
                                    .setNumber(rs.getString(
                                            PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_CORR_ACCOUNT.getName())))
            );
            case wallet_info -> PayoutToolInfo.wallet_info(
                    new WalletInfo(rs.getString(PAYOUT.PAYOUT_TOOL_WALLET_ID.getName())));
            case payment_institution_account -> PayoutToolInfo.payment_institution_account(
                    new PaymentInstitutionAccount());
        };
    }

    static PayoutStatus toPayoutStatus(ResultSet rs) throws SQLException {
        var payoutStatus = TypeUtil.toEnumField(rs.getString(PAYOUT.STATUS.getName()),
                dev.vality.magista.domain.enums.PayoutStatus.class);
        return switch (payoutStatus) {
            case unpaid -> PayoutStatus.unpaid(new PayoutUnpaid());
            case paid -> PayoutStatus.paid(new PayoutPaid());
            case cancelled -> PayoutStatus
                    .cancelled(new PayoutCancelled(rs.getString(PAYOUT.CANCELLED_DETAILS.getName())));
            case confirmed -> PayoutStatus.confirmed(new PayoutConfirmed());
        };
    }

    static InvoicePaymentRefundStatus toRefundStatus(ResultSet rs) throws SQLException {
        RefundStatus refundStatus =
                TypeUtil.toEnumField(rs.getString(REFUND_DATA.REFUND_STATUS.getName()), RefundStatus.class);
        return switch (refundStatus) {
            case pending -> InvoicePaymentRefundStatus.pending(new InvoicePaymentRefundPending());
            case succeeded -> InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded(
                    TypeUtil.temporalToString(
                            rs.getObject(REFUND_DATA.EVENT_CREATED_AT.getName(), LocalDateTime.class))
            ));
            case failed -> InvoicePaymentRefundStatus.failed(new InvoicePaymentRefundFailed(
                    DamselUtil.toOperationFailureDeprecated(
                            TypeUtil.toEnumField(rs.getString(REFUND_DATA.REFUND_OPERATION_FAILURE_CLASS.getName()),
                                    FailureClass.class),
                            rs.getString(REFUND_DATA.REFUND_EXTERNAL_FAILURE.getName()),
                            rs.getString(REFUND_DATA.REFUND_EXTERNAL_FAILURE_REASON.getName())
                    ),
                    TypeUtil.temporalToString(
                            rs.getObject(REFUND_DATA.EVENT_CREATED_AT.getName(), LocalDateTime.class))
            ));
        };
    }

    public static InvoicePaymentChargebackReason toInvoicePaymentChargebackReason(ResultSet rs) throws SQLException {
        InvoicePaymentChargebackReason invoicePaymentChargebackReason = new InvoicePaymentChargebackReason();
        invoicePaymentChargebackReason.setCode(rs.getString(CHARGEBACK_DATA.CHARGEBACK_REASON.getName()));
        ChargebackCategory chargebackCategory =
                TypeUtil.toEnumField(rs.getString(
                                CHARGEBACK_DATA.CHARGEBACK_REASON_CATEGORY.getName()),
                        ChargebackCategory.class);
        InvoicePaymentChargebackCategory invoicePaymentChargebackCategory = new InvoicePaymentChargebackCategory();
        switch (chargebackCategory) {
            case fraud -> invoicePaymentChargebackCategory.setFraud(new InvoicePaymentChargebackCategoryFraud());
            case dispute -> invoicePaymentChargebackCategory.setDispute(new InvoicePaymentChargebackCategoryDispute());
            case authorisation -> invoicePaymentChargebackCategory.setAuthorisation(
                    new InvoicePaymentChargebackCategoryAuthorisation());
            case processing_error -> invoicePaymentChargebackCategory
                    .setProcessingError(new InvoicePaymentChargebackCategoryProcessingError());
            default -> throw new NotFoundException(
                    String.format("Chargeback category %s not found", chargebackCategory));
        }
        invoicePaymentChargebackReason.setCategory(invoicePaymentChargebackCategory);

        return invoicePaymentChargebackReason;
    }

    public static InvoicePaymentChargebackStatus toInvoicePaymentChargebackStatus(ResultSet rs)
            throws SQLException {
        InvoicePaymentChargebackStatus invoicePaymentChargebackStatus = new InvoicePaymentChargebackStatus();
        ChargebackStatus chargebackStatus = TypeUtil.toEnumField(rs.getString(
                        CHARGEBACK_DATA.CHARGEBACK_STATUS.getName()),
                ChargebackStatus.class);
        switch (chargebackStatus) {
            case pending -> invoicePaymentChargebackStatus.setPending(new InvoicePaymentChargebackPending());
            case accepted -> invoicePaymentChargebackStatus.setAccepted(new InvoicePaymentChargebackAccepted());
            case rejected -> invoicePaymentChargebackStatus.setRejected(new InvoicePaymentChargebackRejected());
            case cancelled -> invoicePaymentChargebackStatus.setCancelled(new InvoicePaymentChargebackCancelled());
            default -> throw new NotFoundException(String.format("Chargeback status %s not found", chargebackStatus));
        }
        return invoicePaymentChargebackStatus;
    }

    public static InvoicePaymentChargebackStage toInvoicePaymentChargebackStage(ResultSet rs) throws SQLException {
        InvoicePaymentChargebackStage chargebackStage = new InvoicePaymentChargebackStage();
        ChargebackStage stage = TypeUtil.toEnumField(rs.getString(
                        CHARGEBACK_DATA.CHARGEBACK_STAGE.getName()),
                ChargebackStage.class);
        switch (stage) {
            case chargeback -> chargebackStage.setChargeback(new InvoicePaymentChargebackStageChargeback());
            case pre_arbitration -> chargebackStage.setPreArbitration(
                    new InvoicePaymentChargebackStagePreArbitration());
            case arbitration -> chargebackStage.setArbitration(new InvoicePaymentChargebackStageArbitration());
            default -> throw new NotFoundException(String.format("Chargeback stage %s not found", stage));
        }
        return chargebackStage;
    }
}