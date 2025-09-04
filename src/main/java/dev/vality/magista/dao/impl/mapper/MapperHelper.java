package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.domain.InvoicePaymentRefundStatus;
import dev.vality.damsel.domain.InvoicePaymentStatus;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.*;
import dev.vality.magista.InvoicePaymentFlow;
import dev.vality.magista.InvoicePaymentFlowHold;
import dev.vality.magista.InvoicePaymentFlowInstant;
import dev.vality.magista.OnHoldExpiration;
import dev.vality.magista.Payer;
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
                    .setPaymentToken(
                            Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_BANK_CARD_TOKEN_PROVIDER.getName()))
                                    .map(BankCardTokenServiceRef::new)
                                    .orElse(null)));
            case payment_terminal -> PaymentTool.payment_terminal(new PaymentTerminal()
                    .setPaymentService(Optional.ofNullable(
                                    rs.getString(PAYMENT_DATA.PAYMENT_TERMINAL_PAYMENT_SERVICE_REF_ID.getName()))
                            .map(PaymentServiceRef::new)
                            .orElse(Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_TERMINAL_PROVIDER.getName()))
                                    .map(PaymentServiceRef::new)
                                    .orElse(null))));
            case digital_wallet -> PaymentTool.digital_wallet(new DigitalWallet()
                    .setId(rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_ID.getName()))
                    .setPaymentService(Optional.ofNullable(
                                    rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_SERVICE_REF_ID.getName()))
                            .map(PaymentServiceRef::new)
                            .orElse(Optional.ofNullable(
                                            rs.getString(PAYMENT_DATA.PAYMENT_DIGITAL_WALLET_PROVIDER.getName()))
                                    .map(PaymentServiceRef::new)
                                    .orElse(null))));
            case crypto_currency -> PaymentTool.crypto_currency(
                    new CryptoCurrencyRef(rs.getString(PAYMENT_DATA.CRYPTO_CURRENCY.getName())));
            case mobile_commerce -> PaymentTool.mobile_commerce(new MobileCommerce()
                    .setPhone(new MobilePhone(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CC.getName()),
                            rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_PHONE_CTN.getName())))
                    .setOperator(Optional.ofNullable(rs.getString(PAYMENT_DATA.PAYMENT_MOBILE_OPERATOR.getName()))
                            .map(MobileOperatorRef::new)
                            .orElse(null)));
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
            case recurrent -> Payer.recurrent(new RecurrentPayer()
                    .setContactInfo(buildContactInfo(rs))
                    .setRecurrentParent(buildRecurrentParent(rs))
                    .setPaymentTool(buildPaymentTool(rs)));
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
            case system_set -> invoicePaymentChargebackReason.setCategory(
                    InvoicePaymentChargebackCategory.system_set(new InvoicePaymentChargebackCategorySystemSet()));
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
        };
    }
}
