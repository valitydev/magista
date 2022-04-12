package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.domain.InvoicePaymentRefundStatus;
import dev.vality.damsel.domain.InvoicePaymentStatus;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.domain.*;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.CustomerPayer;
import dev.vality.magista.InvoicePaymentFlow;
import dev.vality.magista.InvoicePaymentFlowHold;
import dev.vality.magista.InvoicePaymentFlowInstant;
import dev.vality.magista.OnHoldExpiration;
import dev.vality.magista.Payer;
import dev.vality.magista.PayoutStatus;
import dev.vality.magista.*;
import dev.vality.magista.domain.enums.*;
import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.util.DamselUtil;
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

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperHelper {

    static dev.vality.damsel.domain.InvoiceStatus mapInvoiceStatus(
            ResultSet rs,
            dev.vality.magista.domain.enums.InvoiceStatus invoiceStatusType) throws SQLException {
        return switch (invoiceStatusType) {
            case cancelled -> dev.vality.damsel.domain.InvoiceStatus.cancelled(new InvoiceCancelled()
                    .setDetails(rs.getString(INVOICE_DATA.INVOICE_STATUS_DETAILS.getName())));
            case unpaid -> dev.vality.damsel.domain.InvoiceStatus.unpaid(new InvoiceUnpaid());
            case paid -> dev.vality.damsel.domain.InvoiceStatus.paid(new InvoicePaid());
            case fulfilled -> dev.vality.damsel.domain.InvoiceStatus.fulfilled(new InvoiceFulfilled()
                    .setDetails(rs.getString(INVOICE_DATA.INVOICE_STATUS_DETAILS.getName())));
            default -> throw new NotFoundException(
                    String.format("Invoice status '%s' not found", invoiceStatusType.getLiteral()));
        };
    }

    static PaymentTool buildPaymentTool(ResultSet rs) throws SQLException {
        var paymentToolType = TypeUtil.toEnumField(
                rs.getString(PAYMENT_DATA.PAYMENT_TOOL.getName()),
                dev.vality.magista.domain.enums.PaymentTool.class
        );

        return switch (paymentToolType) {
            case bank_card -> PaymentTool.bank_card(new BankCard()
                    .setToken(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN.getName()))
                    .setBin(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_FIRST6.getName()))
                    .setLastDigits(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_LAST4.getName()))
                    .setPaymentSystem(Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.getName()))
                            .map(PaymentSystemRef::new)
                            .orElse(null))
                    .setPaymentSystemDeprecated(
                            Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.getName()))
                                    .map(bankCardSystem ->
                                            TypeUtil.toEnumField(bankCardSystem, LegacyBankCardPaymentSystem.class))
                                    .orElse(null)
                    )
                    .setPaymentToken(
                            Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER.getName()))
                                    .map(BankCardTokenServiceRef::new)
                                    .orElse(null))
                    .setTokenProviderDeprecated(
                            Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER.getName()))
                                    .map(tokenProvider ->
                                            TypeUtil.toEnumField(tokenProvider, LegacyBankCardTokenProvider.class))
                                    .orElse(null)
                    )
            );
            case payment_terminal -> PaymentTool.payment_terminal(new PaymentTerminal()
                    .setTerminalTypeDeprecated(
                            Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER.getName()))
                                    .map(terminalProvider -> TypeUtil.toEnumField(terminalProvider,
                                            LegacyTerminalPaymentProvider.class))
                                    .orElse(null)
                    )
                    .setPaymentService(Optional.ofNullable(
                                    rs.getString(PAYMENT_DATA.PAYMENT_TERMINAL_PAYMENT_SERVICE_REF_ID.getName()))
                            .map(PaymentServiceRef::new)
                            .orElse(null))
            );
            case digital_wallet -> PaymentTool.digital_wallet(new DigitalWallet()
                    .setId(rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_ID.getName()))
                    .setProviderDeprecated(Optional.ofNullable(
                                    rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_PROVIDER.getName()))
                            .map(digitalWalletProvider -> TypeUtil.toEnumField(digitalWalletProvider,
                                    LegacyDigitalWalletProvider.class)
                            )
                            .orElse(null)
                    )
                    .setPaymentService(Optional.ofNullable(
                                    rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_SERVICE_REF_ID.getName()))
                            .map(PaymentServiceRef::new)
                            .orElse(null))
            );
            case crypto_currency -> PaymentTool.crypto_currency_deprecated(
                    TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.CRYPTO_CURRENCY.getName()),
                                LegacyCryptoCurrency.class));
            case mobile_commerce -> PaymentTool.mobile_commerce(new MobileCommerce()
                        .setOperatorDeprecated(
                                Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_OPERATOR.getName()))
                                        .map(mobileOperator ->
                                                TypeUtil.toEnumField(mobileOperator, LegacyMobileOperator.class))
                                        .orElse(null))
                        .setPhone(new MobilePhone(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CC.getName()),
                                rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CTN.getName())))
                );
            default -> throw new NotFoundException(String.format("Payment tool '%s' not found", paymentToolType));
        };
    }

    static Payer buildPayer(ResultSet rs) throws SQLException {
        PaymentPayerType paymentPayerType = TypeUtil.toEnumField(
                rs.getString(PAYMENT_DATA.PAYMENT_PAYER_TYPE.getName()),
                PaymentPayerType.class
        );

        return switch (paymentPayerType) {
            case payment_resource -> Payer.payment_resource(
                        new PaymentResourcePayer()
                                .setContactInfo(buildContactInfo(rs))
                                .setResource(new DisposablePaymentResource()
                                        .setPaymentTool(buildPaymentTool(rs))
                                        .setClientInfo(buildClientInfo(rs))));
            case customer -> Payer.customer(new CustomerPayer()
                        .setCustomerId(rs.getString(PAYMENT_DATA.PAYMENT_CUSTOMER_ID.getName()))
                        .setPaymentTool(buildPaymentTool(rs))
                        .setContactInfo(buildContactInfo(rs)));
            case recurrent -> Payer.recurrent(new RecurrentPayer()
                        .setContactInfo(buildContactInfo(rs))
                        .setRecurrentParent(buildRecurrentParent(rs))
                        .setPaymentTool(buildPaymentTool(rs)));
            default -> throw new NotFoundException(String.format("Payment type '%s' not found", paymentPayerType));
        };
    }

    private static RecurrentParentPayment buildRecurrentParent(ResultSet rs) throws SQLException {
        return new RecurrentParentPayment()
                .setInvoiceId(rs.getString(PAYMENT_DATA.PAYMENT_RECURRENT_PAYER_PARENT_INVOICE_ID.getName()))
                .setPaymentId(rs.getString(PAYMENT_DATA.PAYMENT_RECURRENT_PAYER_PARENT_PAYMENT_ID.getName()));
    }

    private static ClientInfo buildClientInfo(ResultSet rs) throws SQLException {
        return new ClientInfo()
                .setIpAddress(rs.getString(PAYMENT_DATA.PAYMENT_IP.getName()))
                .setFingerprint(
                        rs.getString(PAYMENT_DATA.PAYMENT_FINGERPRINT.getName()));
    }

    private static ContactInfo buildContactInfo(ResultSet rs) throws SQLException {
        return new ContactInfo()
                .setEmail(rs.getString(PAYMENT_DATA.PAYMENT_EMAIL.getName()))
                .setPhoneNumber(rs.getString(PAYMENT_DATA.PAYMENT_PHONE_NUMBER.getName()));
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
            dev.vality.magista.domain.enums.InvoicePaymentStatus invoicePaymentStatus) throws SQLException {
        return switch (invoicePaymentStatus) {
            case pending -> InvoicePaymentStatus.pending(new InvoicePaymentPending());
            case cancelled -> InvoicePaymentStatus.cancelled(new InvoicePaymentCancelled());
            case failed -> InvoicePaymentStatus.failed(new InvoicePaymentFailed()
                    .setFailure(DamselUtil.toOperationFailure(
                            TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_OPERATION_FAILURE_CLASS.getName()),
                                    FailureClass.class),
                            rs.getString(PAYMENT_DATA.PAYMENT_EXTERNAL_FAILURE.getName()),
                            rs.getString(PAYMENT_DATA.PAYMENT_EXTERNAL_FAILURE_REASON.getName())
                    )));
            case captured -> InvoicePaymentStatus.captured(new InvoicePaymentCaptured());
            case refunded -> InvoicePaymentStatus.refunded(new InvoicePaymentRefunded());
            case processed -> InvoicePaymentStatus.processed(new InvoicePaymentProcessed());
            case charged_back -> InvoicePaymentStatus.charged_back(new InvoicePaymentChargedBack());
            default -> throw new NotFoundException(
                    String.format("Payment status '%s' not found", invoicePaymentStatus.getLiteral()));
        };
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
                                            rs.getString(
                                                    PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_NAME.getName()),
                                            CountryCode.class))
                                    .setBic(rs.getString(
                                            PAYOUT.PAYOUT_TOOL_INTERNATIONAL_BANK_ACCOUNT_BANK_BIC.getName()))
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
            default -> throw new NotFoundException(
                    String.format("Payout type %s not found", payoutType));
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
            default -> throw new NotFoundException(
                    String.format("Payout status %s not found", payoutStatus));
        };
    }

    static InvoicePaymentRefundStatus toRefundStatus(ResultSet rs) throws SQLException {
        RefundStatus refundStatus =
                TypeUtil.toEnumField(rs.getString(REFUND_DATA.REFUND_STATUS.getName()), RefundStatus.class);
        return switch (refundStatus) {
            case pending -> InvoicePaymentRefundStatus.pending(new InvoicePaymentRefundPending());
            case succeeded -> InvoicePaymentRefundStatus.succeeded(new InvoicePaymentRefundSucceeded());
            case failed -> InvoicePaymentRefundStatus.failed(new InvoicePaymentRefundFailed(
                    DamselUtil.toOperationFailure(
                            TypeUtil.toEnumField(rs.getString(REFUND_DATA.REFUND_OPERATION_FAILURE_CLASS.getName()),
                                    FailureClass.class),
                            rs.getString(REFUND_DATA.REFUND_EXTERNAL_FAILURE.getName()),
                            rs.getString(REFUND_DATA.REFUND_EXTERNAL_FAILURE_REASON.getName())
                    )
            ));
            default -> throw new NotFoundException(
                    String.format("Refund status %s not found", refundStatus));
        };
    }

    public static InvoicePaymentChargebackReason toInvoicePaymentChargebackReason(ResultSet rs) throws SQLException {
        InvoicePaymentChargebackReason invoicePaymentChargebackReason = new InvoicePaymentChargebackReason();
        invoicePaymentChargebackReason.setCode(rs.getString(CHARGEBACK_DATA.CHARGEBACK_REASON.getName()));
        ChargebackCategory chargebackCategory =
                TypeUtil.toEnumField(rs.getString(
                        CHARGEBACK_DATA.CHARGEBACK_REASON_CATEGORY.getName()),
                        ChargebackCategory.class);
        return switch (chargebackCategory) {
            case fraud -> invoicePaymentChargebackReason.setCategory(
                    InvoicePaymentChargebackCategory.fraud(new InvoicePaymentChargebackCategoryFraud()));
            case dispute -> invoicePaymentChargebackReason.setCategory(
                    InvoicePaymentChargebackCategory.dispute(new InvoicePaymentChargebackCategoryDispute()));
            case authorisation -> invoicePaymentChargebackReason.setCategory(
                    InvoicePaymentChargebackCategory.authorisation(
                            new InvoicePaymentChargebackCategoryAuthorisation()));
            case processing_error -> invoicePaymentChargebackReason.setCategory(InvoicePaymentChargebackCategory
                    .processing_error(new InvoicePaymentChargebackCategoryProcessingError()));
            default -> throw new NotFoundException(
                    String.format("Chargeback category %s not found", chargebackCategory));
        };
    }

    public static InvoicePaymentChargebackStatus toInvoicePaymentChargebackStatus(ResultSet rs)
            throws SQLException {
        ChargebackStatus chargebackStatus = TypeUtil.toEnumField(rs.getString(
                CHARGEBACK_DATA.CHARGEBACK_STATUS.getName()),
                ChargebackStatus.class);
        return switch (chargebackStatus) {
            case pending -> InvoicePaymentChargebackStatus.pending(new InvoicePaymentChargebackPending());
            case accepted -> InvoicePaymentChargebackStatus.accepted(new InvoicePaymentChargebackAccepted());
            case rejected -> InvoicePaymentChargebackStatus.rejected(new InvoicePaymentChargebackRejected());
            case cancelled -> InvoicePaymentChargebackStatus.cancelled(new InvoicePaymentChargebackCancelled());
            default -> throw new NotFoundException(String.format("Chargeback status %s not found", chargebackStatus));
        };
    }

    public static InvoicePaymentChargebackStage toInvoicePaymentChargebackStage(ResultSet rs) throws SQLException {
        ChargebackStage stage = TypeUtil.toEnumField(rs.getString(
                CHARGEBACK_DATA.CHARGEBACK_STAGE.getName()),
                ChargebackStage.class);
        return switch (stage) {
            case chargeback -> InvoicePaymentChargebackStage.chargeback(new InvoicePaymentChargebackStageChargeback());
            case pre_arbitration -> InvoicePaymentChargebackStage.pre_arbitration(
                    new InvoicePaymentChargebackStagePreArbitration());
            case arbitration -> InvoicePaymentChargebackStage.arbitration(
                    new InvoicePaymentChargebackStageArbitration());
            default -> throw new NotFoundException(String.format("Chargeback stage %s not found", stage));
        };
    }
}
