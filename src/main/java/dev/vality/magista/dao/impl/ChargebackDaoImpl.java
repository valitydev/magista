package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.ChargebackDao;
import dev.vality.magista.dao.impl.mapper.RecordRowMapper;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.domain.tables.records.ChargebackDataRecord;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.magista.domain.tables.ChargebackData.CHARGEBACK_DATA;

@Component
public class ChargebackDaoImpl extends AbstractDao implements ChargebackDao {

    private final RowMapper<ChargebackData> chargebackRowMapper;

    public ChargebackDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.chargebackRowMapper = new RecordRowMapper<>(CHARGEBACK_DATA, ChargebackData.class);
    }

    @Override
    public ChargebackData get(String invoiceId, String paymentId, String chargebackId) {
        Query query = getDslContext().selectFrom(CHARGEBACK_DATA)
                .where(
                        CHARGEBACK_DATA.INVOICE_ID.eq(invoiceId)
                                .and(CHARGEBACK_DATA.PAYMENT_ID.eq(paymentId))
                                .and(CHARGEBACK_DATA.CHARGEBACK_ID.eq(chargebackId))
                );
        return fetchOne(query, chargebackRowMapper);
    }

    @Override
    public void save(List<ChargebackData> chargebackDataList) {
        List<Query> queries = chargebackDataList.stream()
                .map(
                        refundData -> {
                            ChargebackDataRecord chargebackDataRecord =
                                    getDslContext().newRecord(CHARGEBACK_DATA, refundData);
                            chargebackDataRecord.changed(true);
                            chargebackDataRecord.changed(CHARGEBACK_DATA.ID, chargebackDataRecord.getId() != null);
                            return chargebackDataRecord;
                        }
                )
                .map(
                        chargebackDataRecord ->
                                getDslContext().insertInto(CHARGEBACK_DATA)
                                        .set(chargebackDataRecord)
                                        .onConflict(CHARGEBACK_DATA.INVOICE_ID, CHARGEBACK_DATA.PAYMENT_ID,
                                                CHARGEBACK_DATA.CHARGEBACK_ID)
                                        .doUpdate()
                                        .set(chargebackDataRecord)
                                        .where(CHARGEBACK_DATA.EVENT_ID.le(chargebackDataRecord.getEventId()))
                )
                .collect(Collectors.toList());
        batchExecute(queries);

    }
}
