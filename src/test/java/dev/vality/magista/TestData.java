package dev.vality.magista;

import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackChangePayload;
import dev.vality.damsel.payment_processing.InvoicePaymentChargebackCreated;
import dev.vality.geck.common.util.TypeUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestData {

    public static InvoicePaymentChargebackChangePayload buildInvoiceChargebackChangePayload() {
        InvoicePaymentChargebackChangePayload invoicePaymentChargebackChangePayload =
                new InvoicePaymentChargebackChangePayload();
        InvoicePaymentChargebackCreated invoicePaymentChargebackCreated = new InvoicePaymentChargebackCreated();
        invoicePaymentChargebackCreated.setChargeback(TestData.buildChargebackCreate());
        invoicePaymentChargebackChangePayload.setInvoicePaymentChargebackCreated(invoicePaymentChargebackCreated);

        return invoicePaymentChargebackChangePayload;
    }

    public static InvoicePaymentChargeback buildChargebackCreate() {
        InvoicePaymentChargeback invoicePaymentChargeback = random(
                InvoicePaymentChargeback.class, "status", "reason", "stage", "created_at", "context");
        invoicePaymentChargeback
                .setStatus(InvoicePaymentChargebackStatus.accepted(new InvoicePaymentChargebackAccepted()));
        invoicePaymentChargeback.setReason(
                new InvoicePaymentChargebackReason()
                        .setCode("653")
                        .setCategory(InvoicePaymentChargebackCategory.fraud(
                                new InvoicePaymentChargebackCategoryFraud()
                        ))
        );
        invoicePaymentChargeback.setStage(
                InvoicePaymentChargebackStage.pre_arbitration(new InvoicePaymentChargebackStagePreArbitration())
        );
        invoicePaymentChargeback.setCreatedAt(TypeUtil.temporalToString(Instant.now()));
        return invoicePaymentChargeback;
    }

}
