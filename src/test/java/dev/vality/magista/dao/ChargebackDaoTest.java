package dev.vality.magista.dao;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class ChargebackDaoTest {

    @Autowired
    private ChargebackDao chargebackDao;

    @Test
    public void saveAndGet() {
        ChargebackData chargebackData = random(ChargebackData.class);

        chargebackDao.save(Collections.singletonList(chargebackData));

        ChargebackData chargeback = chargebackDao
                .get(chargebackData.getInvoiceId(), chargebackData.getPaymentId(), chargebackData.getChargebackId());
        assertEquals(chargebackData, chargeback);
    }

    @Test
    public void updatePreviousEventTest() {
        ChargebackData chargebackData = random(ChargebackData.class);

        chargebackDao.save(List.of(chargebackData));
        chargebackDao.save(List.of(chargebackData));

        ChargebackData chargebackDataWithPreviousEventId = new ChargebackData(chargebackData);
        chargebackDataWithPreviousEventId.setEventId(chargebackData.getEventId() - 1);

        chargebackDao.save(List.of(chargebackDataWithPreviousEventId));
        ChargebackData expectedChargebackData = chargebackDao.get(
                chargebackData.getInvoiceId(), chargebackData.getPaymentId(), chargebackData.getChargebackId());
        assertEquals(chargebackData, expectedChargebackData);
    }

}
