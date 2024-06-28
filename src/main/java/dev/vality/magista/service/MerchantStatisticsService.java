package dev.vality.magista.service;

import dev.vality.magista.*;
import dev.vality.magista.dao.SearchDao;
import dev.vality.magista.util.TokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantStatisticsService {

    private final SearchDao searchDao;
    private final TokenGenService tokenGenService;

    public StatInvoiceResponse getInvoices(InvoiceSearchQuery searchQuery) {
        var queryCopyWithNullToken = new InvoiceSearchQuery(searchQuery);
        queryCopyWithNullToken.getCommonSearchQueryParams().setContinuationToken(null);
        tokenGenService.validateToken(
                queryCopyWithNullToken,
                searchQuery.getCommonSearchQueryParams().getContinuationToken());
        List<StatInvoice> invoices = searchDao.getInvoices(searchQuery);
        return new StatInvoiceResponse()
                .setInvoices(invoices)
                .setContinuationToken(
                        tokenGenService.generateToken(
                                queryCopyWithNullToken,
                                searchQuery.getCommonSearchQueryParams(),
                                invoices,
                                TokenUtil::getLastElement,
                                StatInvoice::getCreatedAt));
    }

    public StatPaymentResponse getPayments(PaymentSearchQuery searchQuery) {
        var queryCopyWithNullToken = new PaymentSearchQuery(searchQuery);
        queryCopyWithNullToken.getCommonSearchQueryParams().setContinuationToken(null);
        tokenGenService.validateToken(
                queryCopyWithNullToken,
                searchQuery.getCommonSearchQueryParams().getContinuationToken());
        List<StatPayment> payments = searchDao.getPayments(searchQuery);
        return new StatPaymentResponse()
                .setPayments(payments)
                .setContinuationToken(
                        tokenGenService.generateToken(
                                queryCopyWithNullToken,
                                searchQuery.getCommonSearchQueryParams(),
                                payments,
                                TokenUtil::getLastElement,
                                StatPayment::getCreatedAt));
    }

    public StatRefundResponse getRefunds(RefundSearchQuery searchQuery) {
        var queryCopyWithNullToken = new RefundSearchQuery(searchQuery);
        queryCopyWithNullToken.getCommonSearchQueryParams().setContinuationToken(null);
        tokenGenService.validateToken(
                queryCopyWithNullToken,
                searchQuery.getCommonSearchQueryParams().getContinuationToken());
        List<StatRefund> refunds = searchDao.getRefunds(searchQuery);
        return new StatRefundResponse()
                .setRefunds(refunds)
                .setContinuationToken(
                        tokenGenService.generateToken(
                                queryCopyWithNullToken,
                                searchQuery.getCommonSearchQueryParams(),
                                refunds,
                                TokenUtil::getLastElement,
                                StatRefund::getCreatedAt));
    }

    public StatChargebackResponse getChargebacks(ChargebackSearchQuery searchQuery) {
        var queryCopyWithNullToken = new ChargebackSearchQuery(searchQuery);
        queryCopyWithNullToken.getCommonSearchQueryParams().setContinuationToken(null);
        tokenGenService.validateToken(
                queryCopyWithNullToken,
                searchQuery.getCommonSearchQueryParams().getContinuationToken());
        List<StatChargeback> chargebacks = searchDao.getChargebacks(searchQuery);
        return new StatChargebackResponse()
                .setChargebacks(chargebacks)
                .setContinuationToken(
                        tokenGenService.generateToken(
                                queryCopyWithNullToken,
                                searchQuery.getCommonSearchQueryParams(),
                                chargebacks,
                                TokenUtil::getLastElement,
                                StatChargeback::getCreatedAt)
                );
    }

    public StatInvoiceTemplateResponse getInvoiceTemplates(InvoiceTemplateSearchQuery searchQuery) {
        var queryCopyWithNullToken = new InvoiceTemplateSearchQuery(searchQuery);
        queryCopyWithNullToken.getCommonSearchQueryParams().setContinuationToken(null);
        tokenGenService.validateToken(
                queryCopyWithNullToken,
                searchQuery.getCommonSearchQueryParams().getContinuationToken());
        List<StatInvoiceTemplate> invoiceTemplates = searchDao.getInvoiceTemplates(searchQuery);
        return new StatInvoiceTemplateResponse()
                .setInvoiceTemplates(invoiceTemplates)
                .setContinuationToken(
                        tokenGenService.generateToken(
                                queryCopyWithNullToken,
                                searchQuery.getCommonSearchQueryParams(),
                                invoiceTemplates,
                                TokenUtil::getLastElement,
                                StatInvoiceTemplate::getEventCreatedAt));
    }
}
