package dev.vality.magista.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

public interface StatisticsDao {

    Collection<Map<String, String>> getPaymentsTurnoverStat(
            String merchantId,
            String shopId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int splitInterval
    );

    Collection<Map<String, String>> getPaymentsGeoStat(
            String merchantId,
            String shopId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int splitInterval
    );

    Collection<Map<String, String>> getPaymentsConversionStat(
            String merchantId,
            String shopId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int splitInterval
    );

    Collection<Map<String, String>> getCustomersRateStat(
            String merchantId,
            String shopId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int splitInterval
    );

    Collection<Map<String, String>> getPaymentsCardTypesStat(
            String merchantId,
            String shopId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            int splitInterval
    );

}
