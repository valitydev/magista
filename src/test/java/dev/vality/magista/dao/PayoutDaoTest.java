package dev.vality.magista.dao;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.enums.PayoutStatus;
import dev.vality.magista.domain.enums.PayoutToolType;
import dev.vality.magista.domain.tables.pojos.Payout;
import dev.vality.magista.exception.DaoException;
import io.github.benas.randombeans.EnhancedRandomBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@PostgresqlSpringBootITest
public class PayoutDaoTest {

    @Autowired
    private PayoutDao payoutDao;

    @Test
    public void insertUpdateAndFindPayoutEventTest() throws DaoException {
        Payout payoutData = random(Payout.class);

        payoutDao.save(payoutData);

        assertEquals(payoutData, payoutDao.get(payoutData.getPayoutId()));

        payoutData.setStatus(PayoutStatus.cancelled);
        payoutData.setCancelledDetails("kek");
        payoutData.setSequenceId(payoutData.getSequenceId() + 1);
        payoutDao.update(payoutData);

        assertEquals(payoutData, payoutDao.get(payoutData.getPayoutId()));
    }

    @Test
    public void insertOnlyNotNullFields() throws DaoException {
        Payout payoutData = new Payout();
        payoutData.setPartyId(UUID.randomUUID().toString());
        payoutData.setEventCreatedAt(LocalDateTime.now());
        payoutData.setSequenceId(random(Integer.class));
        payoutData.setShopId(random(String.class));
        payoutData.setPayoutId(random(String.class));
        payoutData.setPartyId(random(String.class));
        payoutData.setPayoutToolId("hhhh");
        payoutData.setCurrencyCode("RUB");
        payoutData.setCreatedAt(LocalDateTime.now());
        payoutData.setStatus(PayoutStatus.paid);
        payoutData.setAmount(Long.MAX_VALUE);
        payoutData.setPayoutToolId("ee");
        payoutData.setPayoutToolType(PayoutToolType.wallet_info);
        payoutDao.save(payoutData);
        payoutDao.get(payoutData.getPayoutId());
    }

    @Test
    public void testDuplicate() {
        Payout payout = EnhancedRandomBuilder.aNewEnhancedRandom().nextObject(Payout.class);
        payoutDao.save(payout);
        long newAmount = 123L;
        payout.setAmount(newAmount);
        payoutDao.save(payout);
        assertNotEquals(newAmount, payoutDao.get(payout.getPayoutId()).getAmount());
        payout.setSequenceId(payout.getSequenceId() - 1);
        payout.setCurrencyCode("USD");
        payoutDao.update(payout);
        assertNotEquals(payout.getCurrencyCode(), payoutDao.get(payout.getPayoutId()).getCurrencyCode());
    }
}
