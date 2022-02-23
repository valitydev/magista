package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.StatisticsDao;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.enums.InvoicePaymentStatus;
import org.jooq.DatePart;
import org.jooq.Field;
import org.jooq.Query;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static dev.vality.magista.domain.tables.PaymentData.PAYMENT_DATA;

/**
 * Created by vpankrashkin on 10.08.16.
 */
@Component
public class StatisticsDaoImpl extends AbstractDao implements StatisticsDao {

    public StatisticsDaoImpl(DataSource ds) {
        super(ds);
    }

    @Override
    public Collection<Map<String, String>> getPaymentsTurnoverStat(String merchantId, String shopId,
                                                                   LocalDateTime fromTime, LocalDateTime toTime,
                                                                   int splitInterval) {
        Field currencyCodeField = PAYMENT_DATA.PAYMENT_CURRENCY_CODE.as("currency_symbolic_code");
        Field amountWithFeeField =
                DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT.minus(PAYMENT_DATA.PAYMENT_FEE)).as("amount_with_fee");
        Field amountWithoutFeeField = DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT).as("amount_without_fee");
        Field spValField = buildSpValField(PAYMENT_DATA.PAYMENT_CREATED_AT, fromTime, splitInterval);

        Query query = getDslContext().select(
                currencyCodeField,
                amountWithFeeField,
                amountWithoutFeeField,
                spValField
        ).from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                PAYMENT_DATA.PARTY_ID.eq(UUID.fromString(merchantId))
                                        .and(PAYMENT_DATA.PARTY_SHOP_ID.eq(shopId))
                                        .and(PAYMENT_DATA.EVENT_TYPE
                                                .eq(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED))
                                        .and(PAYMENT_DATA.PAYMENT_STATUS.in(InvoicePaymentStatus.captured)),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).groupBy(
                        spValField,
                        PAYMENT_DATA.PAYMENT_CURRENCY_CODE
                ).orderBy(spValField);

        return fetch(query, (rs, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("offset", (rs.getLong(spValField.getName()) * splitInterval) + "");
            map.put(currencyCodeField.getName(), rs.getString(currencyCodeField.getName()));
            map.put(amountWithFeeField.getName(), rs.getString(amountWithFeeField.getName()));
            map.put(amountWithoutFeeField.getName(), rs.getString(amountWithoutFeeField.getName()));
            return map;
        });
    }

    @Override
    public Collection<Map<String, String>> getPaymentsGeoStat(String merchantId, String shopId, LocalDateTime fromTime,
                                                              LocalDateTime toTime, int splitInterval) {
        Field countryIdField = PAYMENT_DATA.PAYMENT_COUNTRY_ID.as("country_id");
        Field cityIdField = PAYMENT_DATA.PAYMENT_CITY_ID.as("city_id");
        Field currencyCodeField = PAYMENT_DATA.PAYMENT_CURRENCY_CODE.as("currency_symbolic_code");
        Field amountWithFeeField =
                DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT.minus(PAYMENT_DATA.PAYMENT_FEE)).as("amount_with_fee");
        Field amountWithoutFeeField = DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT).as("amount_without_fee");
        Field spValField = buildSpValField(PAYMENT_DATA.PAYMENT_CREATED_AT, fromTime, splitInterval);

        Query query = getDslContext().select(
                countryIdField,
                cityIdField,
                currencyCodeField,
                amountWithFeeField,
                amountWithoutFeeField,
                spValField
        )
                .from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                PAYMENT_DATA.PARTY_ID.eq(UUID.fromString(merchantId))
                                        .and(PAYMENT_DATA.PARTY_SHOP_ID.eq(shopId))
                                        .and(PAYMENT_DATA.PAYMENT_COUNTRY_ID.isNotNull())
                                        .and(PAYMENT_DATA.PAYMENT_CITY_ID.isNotNull())
                                        .and(PAYMENT_DATA.EVENT_TYPE
                                                .eq(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED))
                                        .and(PAYMENT_DATA.PAYMENT_STATUS.in(InvoicePaymentStatus.captured)),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).groupBy(
                        spValField,
                        PAYMENT_DATA.PAYMENT_COUNTRY_ID,
                        PAYMENT_DATA.PAYMENT_CITY_ID,
                        PAYMENT_DATA.PAYMENT_CURRENCY_CODE
                ).orderBy(spValField);

        return fetch(query, (rs, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("offset", (rs.getLong(spValField.getName()) * splitInterval) + "");
            map.put(countryIdField.getName(), rs.getString(countryIdField.getName()));
            map.put(cityIdField.getName(), rs.getString(cityIdField.getName()));
            map.put(currencyCodeField.getName(), rs.getString(currencyCodeField.getName()));
            map.put(amountWithFeeField.getName(), rs.getString(amountWithFeeField.getName()));
            map.put(amountWithoutFeeField.getName(), rs.getString(amountWithoutFeeField.getName()));
            return map;
        });
    }

    @Override
    public Collection<Map<String, String>> getPaymentsConversionStat(String merchantId, String shopId,
                                                                     LocalDateTime fromTime, LocalDateTime toTime,
                                                                     int splitInterval) {
        Field totalCountField = DSL.sum(
                DSL.when(PAYMENT_DATA.PAYMENT_STATUS.in(InvoicePaymentStatus.captured, InvoicePaymentStatus.failed), 1)
                        .otherwise(0)
        ).as("total_count");
        Field successfulCountField = DSL.sum(
                DSL.when(PAYMENT_DATA.PAYMENT_STATUS.eq(InvoicePaymentStatus.captured), 1)
                        .otherwise(0)
        ).as("successful_count");
        Field spValField = buildSpValField(PAYMENT_DATA.PAYMENT_CREATED_AT, fromTime, splitInterval);

        Table countTable = getDslContext().select(
                totalCountField,
                successfulCountField,
                spValField
        ).from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                PAYMENT_DATA.PARTY_ID.eq(UUID.fromString(merchantId))
                                        .and(PAYMENT_DATA.PARTY_SHOP_ID.eq(shopId))
                                        .and(PAYMENT_DATA.EVENT_TYPE
                                                .eq(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED))
                                        .and(PAYMENT_DATA.PAYMENT_STATUS
                                                .in(InvoicePaymentStatus.captured, InvoicePaymentStatus.failed)),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).groupBy(spValField)
                .orderBy(spValField)
                .asTable();
        Field conversionField = countTable.field(successfulCountField).cast(SQLDataType.FLOAT)
                .divide(DSL.greatest(countTable.field(totalCountField), 1)).as("conversion");
        Query query = getDslContext().select(
                conversionField,
                countTable.field(successfulCountField),
                countTable.field(totalCountField),
                countTable.field(spValField)
        ).from(countTable);

        return fetch(query, (rs, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("offset", (rs.getLong(spValField.getName()) * splitInterval) + "");
            map.put(conversionField.getName(), rs.getString(conversionField.getName()));
            map.put(totalCountField.getName(), rs.getString(totalCountField.getName()));
            map.put(successfulCountField.getName(), rs.getString(successfulCountField.getName()));
            return map;
        });
    }

    @Override
    public Collection<Map<String, String>> getCustomersRateStat(String merchantId, String shopId,
                                                                LocalDateTime fromTime, LocalDateTime toTime,
                                                                int splitInterval) {
        Field uniqCountField = DSL.count(PAYMENT_DATA.PAYMENT_FINGERPRINT).as("unic_count");
        Field spValField = buildSpValField(PAYMENT_DATA.PAYMENT_CREATED_AT, fromTime, splitInterval);

        Query query = getDslContext().select(
                uniqCountField,
                spValField
        ).from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                PAYMENT_DATA.PARTY_ID.eq(UUID.fromString(merchantId))
                                        .and(PAYMENT_DATA.PARTY_SHOP_ID.eq(shopId))
                                        .and(PAYMENT_DATA.PAYMENT_FINGERPRINT.isNotNull()),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).groupBy(spValField)
                .orderBy(spValField);

        return fetch(query, (rs, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("offset", (rs.getLong(spValField.getName()) * splitInterval) + "");
            map.put(uniqCountField.getName(), rs.getString(uniqCountField.getName()));
            return map;
        });
    }

    @Override
    public Collection<Map<String, String>> getPaymentsCardTypesStat(String merchantId, String shopId,
                                                                    LocalDateTime fromTime, LocalDateTime toTime,
                                                                    int splitInterval) {
        Field totalCountField = DSL.count(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM).as("total_count");
        Field paymentSystemField = PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.as("payment_system");
        Field amountWithFeeField =
                DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT.minus(PAYMENT_DATA.PAYMENT_FEE)).as("amount_with_fee");
        Field amountWithoutFeeField = DSL.sum(PAYMENT_DATA.PAYMENT_AMOUNT).as("amount_without_fee");
        Field spValField = buildSpValField(PAYMENT_DATA.PAYMENT_CREATED_AT, fromTime, splitInterval);

        Query query = getDslContext().select(
                totalCountField,
                paymentSystemField,
                amountWithFeeField,
                amountWithoutFeeField,
                spValField
        ).from(PAYMENT_DATA)
                .where(
                        appendDateTimeRangeConditions(
                                PAYMENT_DATA.PARTY_ID.eq(UUID.fromString(merchantId))
                                        .and(PAYMENT_DATA.PARTY_SHOP_ID.eq(shopId))
                                        .and(PAYMENT_DATA.PAYMENT_BANK_CARD_SYSTEM.isNotNull())
                                        .and(PAYMENT_DATA.EVENT_TYPE
                                                .eq(InvoiceEventType.INVOICE_PAYMENT_STATUS_CHANGED))
                                        .and(PAYMENT_DATA.PAYMENT_STATUS.in(InvoicePaymentStatus.captured)),
                                PAYMENT_DATA.PAYMENT_CREATED_AT,
                                fromTime,
                                toTime
                        )
                ).groupBy(spValField, paymentSystemField)
                .orderBy(spValField);

        return fetch(query, (rs, i) -> {
            Map<String, String> map = new HashMap<>();
            map.put("offset", (rs.getLong(spValField.getName()) * splitInterval) + "");
            map.put(totalCountField.getName(), rs.getString(totalCountField.getName()));
            map.put(paymentSystemField.getName(), rs.getString(paymentSystemField.getName()));
            map.put(amountWithFeeField.getName(), rs.getString(amountWithFeeField.getName()));
            map.put(amountWithoutFeeField.getName(), rs.getString(amountWithoutFeeField.getName()));
            return map;
        });
    }

    private Field buildSpValField(Field<LocalDateTime> dateTimeField, LocalDateTime fromTime, int splitInterval) {
        return DSL.field(
                DSL.sql("trunc({0})", DSL.extract(
                        DSL.localDateTimeDiff(dateTimeField, fromTime), DatePart.EPOCH)
                        .divide(splitInterval)
                )
        ).as("sp_val");
    }

}
