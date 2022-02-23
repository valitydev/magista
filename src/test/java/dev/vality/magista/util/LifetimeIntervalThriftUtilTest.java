package dev.vality.magista.util;

import dev.vality.damsel.domain.LifetimeInterval;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class LifetimeIntervalThriftUtilTest {

    @Test
    public void testLifetimeInterval() {
        short expected = 1;
        LifetimeInterval lifetimeInterval = new LifetimeInterval()
                .setSeconds(expected)
                .setDays(expected)
                .setYears(expected);
        LocalDateTime expectedTime = LocalDateTime.MIN;
        LocalDateTime invoiceValidUntil = LifetimeIntervalThriftUtil.getInvoiceValidUntil(
                expectedTime, lifetimeInterval);

        assertThat(
                expectedTime
                        .plusSeconds(expected)
                        .plusHours(0)
                        .plusDays(expected)
                        .plusYears(expected))
                .isEqualTo(invoiceValidUntil);
    }
}
