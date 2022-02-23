package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.AdjustmentDao;
import dev.vality.magista.dao.impl.mapper.RecordRowMapper;
import dev.vality.magista.domain.tables.pojos.AdjustmentData;
import dev.vality.magista.domain.tables.records.AdjustmentDataRecord;
import dev.vality.magista.exception.DaoException;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.magista.domain.Tables.ADJUSTMENT_DATA;

@Component
public class AdjustmentDaoImpl extends AbstractDao implements AdjustmentDao {

    private final RowMapper<AdjustmentData> adjustmentRowMapper;

    public AdjustmentDaoImpl(DataSource dataSource) {
        super(dataSource);
        adjustmentRowMapper = new RecordRowMapper<>(ADJUSTMENT_DATA, AdjustmentData.class);
    }

    @Override
    public AdjustmentData get(String invoiceId, String paymentId, String adjustmentId) throws DaoException {
        Query query = getDslContext().selectFrom(ADJUSTMENT_DATA)
                .where(
                        ADJUSTMENT_DATA.INVOICE_ID.eq(invoiceId)
                                .and(ADJUSTMENT_DATA.PAYMENT_ID.eq(paymentId))
                                .and(ADJUSTMENT_DATA.ADJUSTMENT_ID.eq(adjustmentId))
                );
        return fetchOne(query, adjustmentRowMapper);
    }

    @Override
    public void save(List<AdjustmentData> adjustments) throws DaoException {
        List<Query> queries = adjustments.stream()
                .map(
                        adjustmentData -> {
                            AdjustmentDataRecord adjustmentDataRecord =
                                    getDslContext().newRecord(ADJUSTMENT_DATA, adjustmentData);
                            adjustmentDataRecord.changed(true);
                            adjustmentDataRecord.changed(ADJUSTMENT_DATA.ID, adjustmentDataRecord.getId() != null);
                            return adjustmentDataRecord;
                        }
                )
                .map(
                        adjustmentDataRecord ->
                                getDslContext().insertInto(ADJUSTMENT_DATA)
                                        .set(adjustmentDataRecord)
                                        .onConflict(ADJUSTMENT_DATA.INVOICE_ID, ADJUSTMENT_DATA.PAYMENT_ID,
                                                ADJUSTMENT_DATA.ADJUSTMENT_ID)
                                        .doUpdate()
                                        .set(adjustmentDataRecord)
                                        .where(ADJUSTMENT_DATA.EVENT_ID.le(adjustmentDataRecord.getEventId()))
                )
                .collect(Collectors.toList());
        batchExecute(queries);
    }

}
