package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.InvoiceCart;
import dev.vality.damsel.domain.InvoiceTemplateDetails;
import dev.vality.damsel.domain.InvoiceTemplateProduct;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.InvoiceTemplateStatus;
import dev.vality.magista.StatInvoiceTemplate;
import dev.vality.magista.domain.enums.InvoiceTemplateEventType;
import dev.vality.magista.util.DamselUtil;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static dev.vality.magista.domain.tables.InvoiceTemplate.INVOICE_TEMPLATE;

public class StatInvoiceTemplateMapper implements RowMapper<StatInvoiceTemplate> {

    @Override
    public StatInvoiceTemplate mapRow(ResultSet rs, int i) throws SQLException {
        StatInvoiceTemplate statInvoiceTemplate = new StatInvoiceTemplate()
                .setEventCreatedAt(TypeUtil.temporalToString(
                        rs.getObject(INVOICE_TEMPLATE.EVENT_CREATED_AT.getName(), LocalDateTime.class)))
                .setPartyId(rs.getString(INVOICE_TEMPLATE.PARTY_ID.getName()))
                .setShopId(rs.getString(INVOICE_TEMPLATE.SHOP_ID.getName()))
                .setInvoiceTemplateId(rs.getString(INVOICE_TEMPLATE.INVOICE_TEMPLATE_ID.getName()))
                .setInvoiceValidUntil(TypeUtil.temporalToString(
                        rs.getObject(INVOICE_TEMPLATE.INVOICE_VALID_UNTIL.getName(), LocalDateTime.class)))
                .setProduct(rs.getString(INVOICE_TEMPLATE.PRODUCT.getName()))
                .setDescription(rs.getString(INVOICE_TEMPLATE.DESCRIPTION.getName()));
        String invoiceDetailsCartJson = rs.getString(INVOICE_TEMPLATE.INVOICE_DETAILS_CART_JSON.getName());
        String invoiceDetailsProductJson = rs.getString(INVOICE_TEMPLATE.INVOICE_DETAILS_PRODUCT_JSON.getName());
        if (invoiceDetailsCartJson != null) {
            statInvoiceTemplate.setDetails(InvoiceTemplateDetails.cart(
                    DamselUtil.fromJson(invoiceDetailsCartJson, InvoiceCart.class)));
        } else if (invoiceDetailsProductJson != null) {
            statInvoiceTemplate.setDetails(InvoiceTemplateDetails.product(
                    DamselUtil.fromJson(invoiceDetailsProductJson, InvoiceTemplateProduct.class)));
        }
        String invoiceContextType = rs.getString(INVOICE_TEMPLATE.INVOICE_CONTEXT_TYPE.getName());
        byte[] invoiceContextData = rs.getBytes(INVOICE_TEMPLATE.INVOICE_CONTEXT_DATA.getName());
        if (invoiceContextType != null && invoiceContextData != null) {
            Content context = new Content();
            context.setType(invoiceContextType);
            context.setData(invoiceContextData);
            statInvoiceTemplate.setContext(context);
        }
        statInvoiceTemplate.setName(rs.getString(INVOICE_TEMPLATE.NAME.getName()));
        switch (TypeUtil.toEnumField(
                rs.getString(INVOICE_TEMPLATE.EVENT_TYPE.getName()),
                InvoiceTemplateEventType.class)) {
            case INVOICE_TEMPLATE_CREATED, INVOICE_TEMPLATE_UPDATED -> statInvoiceTemplate.setInvoiceTemplateStatus(
                    InvoiceTemplateStatus.created);
            case INVOICE_TEMPLATE_DELETED -> statInvoiceTemplate.setInvoiceTemplateStatus(
                    InvoiceTemplateStatus.deleted);
            default -> throw new IllegalArgumentException("Unknown enum type");
        }
        LocalDateTime invoiceTemplateCreatedAt = rs.getObject(
                INVOICE_TEMPLATE.INVOICE_TEMPLATE_CREATED_AT.getName(),
                LocalDateTime.class);
        if (invoiceTemplateCreatedAt != null) {
            statInvoiceTemplate.setInvoiceTemplateCreatedAt(TypeUtil.temporalToString(invoiceTemplateCreatedAt));
        }
        return statInvoiceTemplate;
    }
}
