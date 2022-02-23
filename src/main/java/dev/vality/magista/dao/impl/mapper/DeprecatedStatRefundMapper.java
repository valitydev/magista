package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.merch_stat.StatRefund;
import dev.vality.geck.common.util.TypeUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;

import static dev.vality.magista.domain.tables.RefundData.REFUND_DATA;

@Deprecated
public class DeprecatedStatRefundMapper implements RowMapper<Map.Entry<Long, StatRefund>> {

    @Override
    public Map.Entry<Long, StatRefund> mapRow(ResultSet rs, int i) throws SQLException {
        StatRefund statRefund = new StatRefund();
        statRefund.setId(rs.getString(REFUND_DATA.REFUND_ID.getName()));
        statRefund.setInvoiceId(rs.getString(REFUND_DATA.INVOICE_ID.getName()));
        statRefund.setPaymentId(rs.getString(REFUND_DATA.PAYMENT_ID.getName()));
        statRefund.setOwnerId(rs.getString(REFUND_DATA.PARTY_ID.getName()));
        statRefund.setShopId(rs.getString(REFUND_DATA.PARTY_SHOP_ID.getName()));
        statRefund.setCurrencySymbolicCode(rs.getString(REFUND_DATA.REFUND_CURRENCY_CODE.getName()));
        statRefund.setStatus(DeprecatedMapperHelper.toRefundStatus(rs));
        statRefund.setAmount(rs.getLong(REFUND_DATA.REFUND_AMOUNT.getName()));
        statRefund.setFee(rs.getLong(REFUND_DATA.REFUND_FEE.getName()));
        statRefund.setReason(rs.getString(REFUND_DATA.REFUND_REASON.getName()));
        statRefund.setCreatedAt(
                TypeUtil.temporalToString(rs.getObject(REFUND_DATA.REFUND_CREATED_AT.getName(), LocalDateTime.class)));
        statRefund.setExternalId(rs.getString(REFUND_DATA.EXTERNAL_ID.getName()));
        return new AbstractMap.SimpleEntry<>(rs.getLong(REFUND_DATA.ID.getName()), statRefund);
    }

}