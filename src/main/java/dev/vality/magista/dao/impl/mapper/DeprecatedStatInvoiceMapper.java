package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.InvoiceCart;
import dev.vality.damsel.merch_stat.InvoiceStatus;
import dev.vality.damsel.merch_stat.StatInvoice;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.util.DamselUtil;
import org.springframework.jdbc.core.RowMapper;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.Map;

import static dev.vality.magista.domain.tables.InvoiceData.INVOICE_DATA;

@Deprecated
public class DeprecatedStatInvoiceMapper implements RowMapper<Map.Entry<Long, StatInvoice>> {

    @Override
    public Map.Entry<Long, StatInvoice> mapRow(ResultSet rs, int i) throws SQLException {
        StatInvoice statInvoice = new StatInvoice();
        statInvoice.setId(rs.getString(INVOICE_DATA.INVOICE_ID.getName()));
        statInvoice.setOwnerId(rs.getString(INVOICE_DATA.PARTY_ID.getName()));
        statInvoice.setShopId(rs.getString(INVOICE_DATA.PARTY_SHOP_ID.getName()));
        statInvoice.setAmount(rs.getLong(INVOICE_DATA.INVOICE_AMOUNT.getName()));
        statInvoice.setCurrencySymbolicCode(rs.getString(INVOICE_DATA.INVOICE_CURRENCY_CODE.getName()));
        statInvoice.setProduct(rs.getString(INVOICE_DATA.INVOICE_PRODUCT.getName()));
        statInvoice.setDescription(rs.getString(INVOICE_DATA.INVOICE_DESCRIPTION.getName()));
        statInvoice.setExternalId(rs.getString(INVOICE_DATA.EXTERNAL_ID.getName()));
        statInvoice.setCreatedAt(
                TypeUtil.temporalToString(
                        rs.getObject(INVOICE_DATA.INVOICE_CREATED_AT.getName(), LocalDateTime.class)
                )
        );
        statInvoice.setDue(
                TypeUtil.temporalToString(
                        rs.getObject(INVOICE_DATA.INVOICE_DUE.getName(), LocalDateTime.class)
                )
        );

        dev.vality.magista.domain.enums.InvoiceStatus invoiceStatusType = TypeUtil.toEnumField(
                rs.getString(INVOICE_DATA.INVOICE_STATUS.getName()),
                dev.vality.magista.domain.enums.InvoiceStatus.class
        );

        String eventCreatedAtString = TypeUtil.temporalToString(
                rs.getObject(INVOICE_DATA.EVENT_CREATED_AT.getName(), LocalDateTime.class)
        );

        InvoiceStatus invoiceStatus = DeprecatedMapperHelper.mapInvoiceStatus(
                rs, invoiceStatusType, eventCreatedAtString);
        statInvoice.setStatus(invoiceStatus);

        String invoiceCartJson = rs.getString(INVOICE_DATA.INVOICE_CART_JSON.getName());
        if (invoiceCartJson != null) {
            statInvoice.setCart(DamselUtil.fromJson(invoiceCartJson, InvoiceCart.class));
        }

        byte[] context = rs.getBytes(INVOICE_DATA.INVOICE_CONTEXT.getName());
        if (context != null) {
            statInvoice.setContext(
                    new Content(
                            rs.getString(INVOICE_DATA.INVOICE_CONTEXT_TYPE.getName()),
                            ByteBuffer.wrap(context)
                    )
            );
        }
        return new AbstractMap.SimpleEntry<>(rs.getLong(INVOICE_DATA.ID.getName()), statInvoice);
    }

}