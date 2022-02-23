package dev.vality.magista.dao.impl;

import dev.vality.magista.dao.EventDao;
import dev.vality.magista.exception.DaoException;
import org.jooq.Query;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Optional;

import static dev.vality.magista.domain.tables.PayoutData.PAYOUT_DATA;

@Component
public class EventDaoImpl extends AbstractDao implements EventDao {

    public EventDaoImpl(DataSource ds) {
        super(ds);
    }

    @Override
    public Optional<Long> getLastPayoutEventId() throws DaoException {
        Query query = getDslContext().select(DSL.max(PAYOUT_DATA.EVENT_ID)).from(PAYOUT_DATA);
        return Optional.ofNullable(fetchOne(query, Long.class));
    }
}
