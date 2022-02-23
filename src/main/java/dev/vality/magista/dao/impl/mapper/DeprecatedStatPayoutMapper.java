package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.merch_stat.StatPayout;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;

import static dev.vality.magista.domain.tables.Payout.PAYOUT;

@Deprecated
public class DeprecatedStatPayoutMapper implements RowMapper<Map.Entry<Long, StatPayout>> {

    @Override
    public Map.Entry<Long, StatPayout> mapRow(ResultSet rs, int i) throws SQLException {
        StatPayout statPayout = new StatPayout();
        statPayout.setId(rs.getString(PAYOUT.PAYOUT_ID.getName()));
        statPayout.setPartyId(rs.getString(PAYOUT.PARTY_ID.getName()));
        statPayout.setShopId(rs.getString(PAYOUT.SHOP_ID.getName()));
        statPayout.setAmount(rs.getLong(PAYOUT.AMOUNT.getName()));
        statPayout.setStatus(DeprecatedMapperHelper.toPayoutStatus(rs));
        statPayout.setFee(rs.getLong(PAYOUT.FEE.getName()));
        statPayout.setCurrencySymbolicCode(rs.getString(PAYOUT.CURRENCY_CODE.getName()));
        statPayout.setCreatedAt(
                TypeUtil.temporalToString(rs.getObject(PAYOUT.CREATED_AT.getName(), LocalDateTime.class))
        );
        statPayout.setPayoutToolInfo(DeprecatedMapperHelper.toPayoutToolInfo(rs));

        return new AbstractMap.SimpleEntry<>(rs.getLong(PAYOUT.ID.getName()), statPayout);
    }
}