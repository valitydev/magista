package dev.vality.magista.dao;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.tables.pojos.RefundData;
import dev.vality.magista.exception.DaoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class RefundDaoTest {

    @Autowired
    private RefundDao refundDao;

    @Test
    public void insertAndFindRefundEventTest() throws DaoException {
        RefundData refund = random(RefundData.class);

        refundDao.save(List.of(refund));

        assertEquals(refund, refundDao.get(refund.getInvoiceId(), refund.getPaymentId(), refund.getRefundId()));
    }

    @Test
    public void updatePreviousEventTest() {
        RefundData refundData = random(RefundData.class);

        refundDao.save(List.of(refundData));
        refundDao.save(List.of(refundData));

        RefundData refundDataWithPreviousEventId = new RefundData(refundData);
        refundDataWithPreviousEventId.setEventId(refundData.getEventId() - 1);

        refundDao.save(List.of(refundDataWithPreviousEventId));
        assertEquals(refundData,
                refundDao.get(refundData.getInvoiceId(), refundData.getPaymentId(), refundData.getRefundId()));
    }
}
