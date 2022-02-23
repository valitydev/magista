package dev.vality.magista.service;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.enums.AdjustmentStatus;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.InvoicePaymentStatus;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static dev.vality.testcontainers.annotations.util.RandomBeans.randomStreamOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class AdjustmentServiceTest {

    @Autowired
    public PaymentAdjustmentService paymentAdjustmentService;

    @Autowired
    public PaymentService paymentService;

    @Autowired
    public InvoiceService invoiceService;

    @Test
    public void testSaveAdjustment() {
        InvoiceData invoiceData = random(InvoiceData.class);
        invoiceData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        invoiceService.saveInvoices(Collections.singletonList(invoiceData));
        PaymentData paymentData = random(PaymentData.class);
        paymentData.setInvoiceId(invoiceData.getInvoiceId());
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        paymentService.savePayments(Collections.singletonList(paymentData));
        List<AdjustmentData> adjustmentDataList = randomStreamOf(10, AdjustmentData.class)
                .peek(adjustmentData -> {
                    adjustmentData.setInvoiceId(paymentData.getInvoiceId());
                    adjustmentData.setPaymentId(paymentData.getPaymentId());
                    adjustmentData.setAdjustmentId("adjustmentId");
                })
                .collect(Collectors.toList());
        adjustmentDataList.get(0).setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);

        paymentAdjustmentService.saveAdjustments(adjustmentDataList);
    }

    @Test
    public void testPaymentStatusAdjustment() {
        InvoiceData invoiceData = random(InvoiceData.class);
        invoiceData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        invoiceService.saveInvoices(Collections.singletonList(invoiceData));
        PaymentData paymentData = random(PaymentData.class);
        paymentData.setPaymentStatus(InvoicePaymentStatus.processed);
        paymentData.setInvoiceId(invoiceData.getInvoiceId());
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        paymentService.savePayments(Collections.singletonList(paymentData));

        AdjustmentData adjustmentData = random(AdjustmentData.class);
        adjustmentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        adjustmentData.setInvoiceId(paymentData.getInvoiceId());
        adjustmentData.setPaymentId(paymentData.getPaymentId());
        adjustmentData.setAdjustmentStatus(AdjustmentStatus.captured);
        adjustmentData.setPaymentStatus(InvoicePaymentStatus.failed);
        paymentAdjustmentService.saveAdjustments(Collections.singletonList(adjustmentData));

        PaymentData savedPaymentData =
                paymentService.getPaymentData(paymentData.getInvoiceId(), paymentData.getPaymentId());
        assertEquals(adjustmentData.getPaymentStatus(), savedPaymentData.getPaymentStatus());
    }

    @Test
    public void testNullPaymentStatusAdjustment() {
        InvoiceData invoiceData = random(InvoiceData.class);
        invoiceData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        invoiceService.saveInvoices(Collections.singletonList(invoiceData));
        PaymentData paymentData = random(PaymentData.class);
        paymentData.setPaymentStatus(InvoicePaymentStatus.processed);
        paymentData.setInvoiceId(invoiceData.getInvoiceId());
        paymentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_STARTED);
        paymentService.savePayments(Collections.singletonList(paymentData));

        AdjustmentData adjustmentData = random(AdjustmentData.class);
        adjustmentData.setEventType(InvoiceEventType.INVOICE_PAYMENT_ADJUSTMENT_CREATED);
        adjustmentData.setInvoiceId(paymentData.getInvoiceId());
        adjustmentData.setPaymentId(paymentData.getPaymentId());
        adjustmentData.setAdjustmentStatus(AdjustmentStatus.captured);
        adjustmentData.setPaymentStatus(null);
        paymentAdjustmentService.saveAdjustments(Collections.singletonList(adjustmentData));

        PaymentData savedPaymentData =
                paymentService.getPaymentData(paymentData.getInvoiceId(), paymentData.getPaymentId());
        assertEquals(paymentData.getPaymentStatus(), savedPaymentData.getPaymentStatus());
    }
}
