package dev.vality.magista.service;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.dao.ChargebackDao;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static dev.vality.testcontainers.annotations.util.RandomBeans.randomStreamOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@PostgresqlSpringBootITest
public class ChargebackServiceTest {

    @Autowired
    public PaymentChargebackService paymentChargebackService;

    @MockitoBean
    public PaymentService paymentService;

    @MockitoSpyBean
    public ChargebackDao chargebackDao;

    @BeforeEach
    public void setup() {
        given(paymentService.getPaymentData(any(), any()))
                .willReturn(random(PaymentData.class));
    }

    @Test
    public void saveChargeback() {
        List<ChargebackData> chargebackList = randomStreamOf(10, ChargebackData.class)
                .peek(chargebackData -> {
                    chargebackData.setPaymentId("testPaymentId");
                    chargebackData.setInvoiceId("tetInvoiceId");
                    chargebackData.setChargebackId("testChargebackId");
                })
                .collect(Collectors.toList());
        chargebackList.get(0).setEventType(InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_CREATED);
        paymentChargebackService.saveChargeback(chargebackList);

        ArgumentCaptor<List<ChargebackData>> captor = ArgumentCaptor.forClass(List.class);
        verify(chargebackDao, times(1)).save(anyList());
        verify(chargebackDao).save(captor.capture());
        List<ChargebackData> enrichedChargebackData = captor.getValue();
        assertEquals(chargebackList.size(), enrichedChargebackData.size());
    }
}
