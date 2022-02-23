package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.PayoutDao;
import dev.vality.magista.dao.impl.mapper.RecordRowMapper;
import dev.vality.magista.domain.tables.pojos.Payout;
import dev.vality.magista.domain.tables.records.PayoutRecord;
import org.jooq.Query;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.magista.domain.tables.Payout.PAYOUT;

@Component
public class PayoutDaoImpl extends AbstractDao implements PayoutDao {

    public final RowMapper<Payout> payoutEventStatRowMapper;

    public PayoutDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.payoutEventStatRowMapper = new RecordRowMapper<>(PAYOUT, Payout.class);
    }

    @Override
    public Payout get(String payoutId) {
        Query query = getDslContext().selectFrom(PAYOUT)
                .where(PAYOUT.PAYOUT_ID.eq(payoutId));
        return fetchOne(query, payoutEventStatRowMapper);
    }

    @Override
    public void save(Payout payout) {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        Query query = getDslContext().insertInto(PAYOUT)
                .set(payoutRecord)
                .onConflict(PAYOUT.PAYOUT_ID)
                .doNothing();
        execute(query);
    }

    @Override
    public void update(Payout payout) {
        PayoutRecord payoutRecord = getDslContext().newRecord(PAYOUT, payout);
        Query query = getDslContext().update(PAYOUT)
                .set(payoutRecord)
                .where(PAYOUT.PAYOUT_ID.eq(payout.getPayoutId()))
                .and(PAYOUT.SEQUENCE_ID.lessThan(payout.getSequenceId()));
        execute(query);
    }
}
