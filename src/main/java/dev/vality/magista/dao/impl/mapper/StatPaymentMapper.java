package dev.vality.magista.dao.impl.mapper;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.AdditionalTransactionInfo;
import dev.vality.damsel.domain.ProviderRef;
import dev.vality.damsel.domain.TerminalRef;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.StatPayment;
import dev.vality.magista.domain.enums.PaymentFlow;
import org.springframework.jdbc.core.RowMapper;

import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static dev.vality.magista.domain.tables.PaymentData.PAYMENT_DATA;

public class StatPaymentMapper implements RowMapper<StatPayment> {

    @Override
    public StatPayment mapRow(ResultSet rs, int i) throws SQLException {
        StatPayment statPayment = new StatPayment();
        statPayment.setId(rs.getString(PAYMENT_DATA.PAYMENT_ID.getName()));
        statPayment.setInvoiceId(rs.getString(PAYMENT_DATA.INVOICE_ID.getName()));
        statPayment.setOwnerId(rs.getString(PAYMENT_DATA.PARTY_ID.getName()));
        statPayment.setShopId(rs.getString(PAYMENT_DATA.PARTY_SHOP_ID.getName()));
        statPayment.setAmount(rs.getLong(PAYMENT_DATA.PAYMENT_AMOUNT.getName()));
        statPayment.setFee(rs.getLong(PAYMENT_DATA.PAYMENT_FEE.getName()));
        statPayment.setCurrencySymbolicCode(rs.getString(PAYMENT_DATA.PAYMENT_CURRENCY_CODE.getName()));
        statPayment.setCreatedAt(
                TypeUtil.temporalToString(
                        rs.getObject(PAYMENT_DATA.PAYMENT_CREATED_AT.getName(), LocalDateTime.class)
                )
        );
        statPayment.setDomainRevision(rs.getLong(PAYMENT_DATA.PAYMENT_DOMAIN_REVISION.getName()));

        statPayment.setStatusChangedAt(
                TypeUtil.temporalToString(rs.getObject(PAYMENT_DATA.EVENT_CREATED_AT.getName(), LocalDateTime.class))
        );
        dev.vality.magista.domain.enums.InvoicePaymentStatus invoicePaymentStatus = TypeUtil.toEnumField(
                rs.getString(PAYMENT_DATA.PAYMENT_STATUS.getName()),
                dev.vality.magista.domain.enums.InvoicePaymentStatus.class
        );

        statPayment.setStatus(MapperHelper.buildInvoicePaymentStatus(rs, invoicePaymentStatus));
        statPayment.setPayer(MapperHelper.buildPayer(rs));

        PaymentFlow paymentFlow =
                TypeUtil.toEnumField(rs.getString(PAYMENT_DATA.PAYMENT_FLOW.getName()), PaymentFlow.class);

        MapperHelper.buildStatPaymentFlow(rs, statPayment, paymentFlow);

        statPayment.setMakeRecurrent(rs.getBoolean(PAYMENT_DATA.PAYMENT_MAKE_RECURRENT_FLAG.getName()));

        statPayment.setShortId(rs.getString(PAYMENT_DATA.PAYMENT_SHORT_ID.getName()));

        statPayment.setExternalId(rs.getString(PAYMENT_DATA.EXTERNAL_ID.getName()));

        byte[] context = rs.getBytes(PAYMENT_DATA.PAYMENT_CONTEXT.getName());
        if (context != null) {
            statPayment.setContext(
                    new Content(
                            rs.getString(PAYMENT_DATA.PAYMENT_CONTEXT_TYPE.getName()),
                            ByteBuffer.wrap(context)
                    )
            );
        }

        AdditionalTransactionInfo additionalTransactionInfo = new AdditionalTransactionInfo();
        additionalTransactionInfo.setRrn(rs.getString(PAYMENT_DATA.PAYMENT_RRN.getName()));
        additionalTransactionInfo.setApprovalCode(rs.getString(PAYMENT_DATA.PAYMENT_APPROVAL_CODE.getName()));
        if (TBaseUtil.getSetFieldsCount(additionalTransactionInfo) > 0) {
            statPayment.setAdditionalTransactionInfo(additionalTransactionInfo);
        }
        statPayment.setProviderId(new ProviderRef(rs.getInt(PAYMENT_DATA.PAYMENT_PROVIDER_ID.getName())));
        statPayment.setTerminalId(new TerminalRef(rs.getInt(PAYMENT_DATA.PAYMENT_TERMINAL_ID.getName())));
        return statPayment;
    }
}
