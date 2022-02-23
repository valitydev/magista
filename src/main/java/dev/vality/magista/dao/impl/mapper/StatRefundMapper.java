package dev.vality.magista.dao.impl.mapper;

import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.StatRefund;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static dev.vality.magista.domain.tables.RefundData.REFUND_DATA;

public class StatRefundMapper implements RowMapper<StatRefund> {

    @Override
    public StatRefund mapRow(ResultSet rs, int i) throws SQLException {
        StatRefund statRefund = new StatRefund();
        statRefund.setId(rs.getString(REFUND_DATA.REFUND_ID.getName()));
        statRefund.setInvoiceId(rs.getString(REFUND_DATA.INVOICE_ID.getName()));
        statRefund.setPaymentId(rs.getString(REFUND_DATA.PAYMENT_ID.getName()));
        statRefund.setOwnerId(rs.getString(REFUND_DATA.PARTY_ID.getName()));
        statRefund.setShopId(rs.getString(REFUND_DATA.PARTY_SHOP_ID.getName()));
        statRefund.setCurrencySymbolicCode(rs.getString(REFUND_DATA.REFUND_CURRENCY_CODE.getName()));
        statRefund.setStatusChangedAt(
                TypeUtil.temporalToString(rs.getObject(REFUND_DATA.EVENT_CREATED_AT.getName(), LocalDateTime.class))
        );
        statRefund.setStatus(MapperHelper.toRefundStatus(rs));
        statRefund.setAmount(rs.getLong(REFUND_DATA.REFUND_AMOUNT.getName()));
        statRefund.setFee(rs.getLong(REFUND_DATA.REFUND_FEE.getName()));
        statRefund.setReason(rs.getString(REFUND_DATA.REFUND_REASON.getName()));
        statRefund.setCreatedAt(
                TypeUtil.temporalToString(rs.getObject(REFUND_DATA.REFUND_CREATED_AT.getName(), LocalDateTime.class)));
        statRefund.setExternalId(rs.getString(REFUND_DATA.EXTERNAL_ID.getName()));
        return statRefund;
    }
}
