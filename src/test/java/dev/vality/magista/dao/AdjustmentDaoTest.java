package dev.vality.magista.dao;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.exception.DaoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class AdjustmentDaoTest {

    @Autowired
    private AdjustmentDao adjustmentDao;

    @Test
    public void insertAndFindAdjustmentEventTest() throws DaoException {
        AdjustmentData adjustment = random(AdjustmentData.class);

        adjustmentDao.save(List.of(adjustment));

        assertEquals(adjustment,
                adjustmentDao.get(adjustment.getInvoiceId(), adjustment.getPaymentId(), adjustment.getAdjustmentId()));
    }

    @Test
    public void updatePreviousEventTest() {
        AdjustmentData adjustmentData = random(AdjustmentData.class);

        adjustmentDao.save(List.of(adjustmentData));
        adjustmentDao.save(List.of(adjustmentData));

        AdjustmentData adjustmentDataWithPreviousEventId = new AdjustmentData(adjustmentData);
        adjustmentDataWithPreviousEventId.setEventId(adjustmentData.getEventId() - 1);

        adjustmentDao.save(List.of(adjustmentDataWithPreviousEventId));
        assertEquals(adjustmentData, adjustmentDao
                .get(adjustmentData.getInvoiceId(), adjustmentData.getPaymentId(), adjustmentData.getAdjustmentId()));
    }

}
