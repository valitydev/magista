package dev.vality.magista.dao;

import dev.vality.magista.*;

import java.util.List;

public interface SearchDao {

    List<StatInvoice> getInvoices(InvoiceSearchQuery invoiceSearchQuery);

    List<StatPayment> getPayments(PaymentSearchQuery paymentSearchQuery);

    List<StatRefund> getRefunds(RefundSearchQuery refundSearchQuery);

    List<StatPayout> getPayouts(PayoutSearchQuery payoutSearchQuery);

    List<StatChargeback> getChargebacks(ChargebackSearchQuery chargebackSearchQuery);

    List<StatInvoiceTemplate> getInvoiceTemplates(InvoiceTemplateSearchQuery invoiceTemplateSearchQuery);

}
