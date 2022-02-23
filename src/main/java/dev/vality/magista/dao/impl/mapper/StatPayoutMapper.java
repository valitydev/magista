package dev.vality.magista.dao.impl.mapper;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.StatPayout;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static dev.vality.magista.domain.tables.Payout.PAYOUT;

public class StatPayoutMapper implements RowMapper<StatPayout> {

    @Override
    public StatPayout mapRow(ResultSet rs, int i) throws SQLException {
        StatPayout statPayout = new StatPayout();
        statPayout.setId(rs.getString(PAYOUT.PAYOUT_ID.getName()));
        statPayout.setPartyId(rs.getString(PAYOUT.PARTY_ID.getName()));
        statPayout.setShopId(rs.getString(PAYOUT.SHOP_ID.getName()));
        statPayout.setAmount(rs.getLong(PAYOUT.AMOUNT.getName()));
        statPayout.setStatus(MapperHelper.toPayoutStatus(rs));
        statPayout.setFee(rs.getLong(PAYOUT.FEE.getName()));
        statPayout.setCurrencySymbolicCode(rs.getString(PAYOUT.CURRENCY_CODE.getName()));
        statPayout.setCreatedAt(
                TypeUtil.temporalToString(rs.getObject(PAYOUT.CREATED_AT.getName(), LocalDateTime.class))
        );
        statPayout.setPayoutToolInfo(MapperHelper.toPayoutToolInfo(rs));

        return statPayout;
    }
}
