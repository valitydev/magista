package dev.vality.magista.service;

import com.github.benmanes.caffeine.cache.Cache;
import dev.vality.magista.dao.InvoiceDao;
import dev.vality.magista.domain.enums.InvoiceEventType;
import dev.vality.magista.domain.tables.pojos.InvoiceData;
import dev.vality.magista.exception.DaoException;
import dev.vality.magista.exception.NotFoundException;
import dev.vality.magista.exception.StorageException;
import dev.vality.magista.util.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.vality.magista.domain.enums.InvoiceEventType.INVOICE_STATUS_CHANGED;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceDao invoiceDao;

    private final Cache<String, InvoiceData> invoiceDataCache;

    public InvoiceData getInvoiceData(String invoiceId) throws NotFoundException, StorageException {
        return invoiceDataCache.get(
                invoiceId,
                key -> {
                    try {
                        InvoiceData invoiceData = invoiceDao.get(key);
                        if (invoiceData == null) {
                            throw new NotFoundException(String.format("Invoice data not found, invoiceId='%s'", key));
                        }
                        return invoiceData;
                    } catch (DaoException ex) {
                        throw new StorageException(String.format("Failed to get invoice data, invoiceId='%s'", key),
                                ex);
                    }
                }
        );
    }

    public void saveInvoices(List<InvoiceData> invoiceEvents) throws NotFoundException, StorageException {
        Map<String, InvoiceData> invoiceDataCacheMap = new LinkedHashMap<>();
        List<InvoiceData> enrichedInvoiceEvents = invoiceEvents.stream()
                .map(invoiceData -> {
                    if (invoiceData.getEventType() == INVOICE_STATUS_CHANGED) {
                        InvoiceData previousInvoiceData = invoiceDataCacheMap.computeIfAbsent(
                                invoiceData.getInvoiceId(),
                                key -> getInvoiceData(key)
                        );
                        BeanUtil.merge(previousInvoiceData, invoiceData);
                    }
                    return invoiceData;
                })
                .peek(invoiceData -> invoiceDataCacheMap.put(invoiceData.getInvoiceId(), invoiceData))
                .peek(invoiceData -> invoiceDataCache.put(invoiceData.getInvoiceId(), invoiceData))
                .collect(Collectors.toList());

        List<InvoiceData> invoiceCreatedEvents = enrichedInvoiceEvents.stream()
                .filter(invoiceData -> invoiceData.getEventType() == InvoiceEventType.INVOICE_CREATED)
                .collect(Collectors.toList());
        List<InvoiceData> updatedInvoices = new ArrayList<>(invoiceDataCacheMap.values());
        updatedInvoices.removeAll(invoiceCreatedEvents);

        try {
            invoiceDao.insert(invoiceCreatedEvents);
            invoiceDao.update(updatedInvoices);
            log.info("Invoice events have been saved, batchSize={}, insertsCount={}, updatesCount={}",
                    invoiceEvents.size(), invoiceCreatedEvents.size(), updatedInvoices.size());
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format("Failed to save invoice events, batchSize=%d, insertsCount=%d, updatesCount=%d",
                            invoiceEvents.size(), invoiceCreatedEvents.size(), updatedInvoices.size()), ex);
        }
    }

}
