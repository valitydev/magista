package dev.vality.magista.service;

import dev.vality.magista.dao.ChargebackDao;
import dev.vality.magista.domain.enums.ChargebackStage;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.ChargebackData;
import dev.vality.magista.domain.tables.pojos.PaymentData;
import dev.vality.magista.exception.DaoException;
import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.exception.StorageException;
import dev.vality.magista.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentChargebackService {

    private final ChargebackDao chargebackDao;

    private final PaymentService paymentService;

    public ChargebackData getChargeback(String invoiceId, String paymentId, String chargebackId)
            throws NotFoundException, StorageException {
        try {
            ChargebackData chargebackData = chargebackDao.get(invoiceId, paymentId, chargebackId);
            if (chargebackData == null) {
                throw new NotFoundException(
                        String.format("Chargeback not found, invoiceId='%s', paymentId='%s', chargebackId='%s'",
                                invoiceId, paymentId, chargebackId));
            }
            return chargebackData;
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format("Failed to get chargeback, invoiceId='%s', paymentId='%s', chargebackId='%s'",
                            invoiceId, paymentId, chargebackId), ex);
        }
    }

    public void saveChargeback(List<ChargebackData> chargebackDataList) {
        log.info("Trying to save chargebackData events, size={}", chargebackDataList.size());
        Map<String, ChargebackData> chargebackDataMap = new HashMap<>();
        List<ChargebackData> enrichedChargebackEvents = chargebackDataList.stream()
                .map(chargeback -> {
                    if (chargeback.getEventType() == InvoiceEventType.INVOICE_PAYMENT_CHARGEBACK_CREATED) {
                        PaymentData paymentData =
                                paymentService.getPaymentData(chargeback.getInvoiceId(), chargeback.getPaymentId());
                        chargeback.setPartyId(paymentData.getPartyId().toString());
                        chargeback.setPartyShopId(paymentData.getPartyShopId());
                        if (chargeback.getChargebackStage() == null) {
                            chargeback.setChargebackStage(ChargebackStage.chargeback);
                        }
                        if (chargeback.getChargebackAmount() == null) {
                            chargeback.setChargebackAmount(paymentData.getPaymentAmount());
                            chargeback.setChargebackCurrencyCode(paymentData.getPaymentCurrencyCode());
                        }
                        return chargeback;
                    } else {
                        ChargebackData previousChargeback = chargebackDataMap.computeIfAbsent(
                                chargeback.getInvoiceId() + chargeback.getPaymentId() + chargeback.getChargebackId(),
                                key -> getChargeback(chargeback.getInvoiceId(), chargeback.getPaymentId(),
                                        chargeback.getChargebackId())
                        );
                        BeanUtil.merge(previousChargeback, chargeback);
                    }
                    return chargeback;
                })
                .peek(chargeback -> chargebackDataMap
                        .put(chargeback.getInvoiceId() + chargeback.getPaymentId() + chargeback.getChargebackId(),
                                chargeback))
                .collect(Collectors.toList());

        try {
            chargebackDao.save(enrichedChargebackEvents);
            log.info("Chargeback events have been saved, size={}", enrichedChargebackEvents.size());
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format("Failed to save chargeback events, size=%d", enrichedChargebackEvents.size()), ex);
        }
    }

}
