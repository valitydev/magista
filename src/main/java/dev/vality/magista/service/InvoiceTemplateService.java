package dev.vality.magista.service;

import dev.vality.magista.dao.InvoiceTemplateDao;
import dev.vality.magista.domain.enums.InvoiceTemplateEventType;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
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
public class InvoiceTemplateService {

    private final InvoiceTemplateDao invoiceTemplateDao;

    public InvoiceTemplate get(String invoiceTemplateId) {
        try {
            InvoiceTemplate invoiceTemplate = invoiceTemplateDao.get(invoiceTemplateId);
            if (invoiceTemplate == null) {
                throw new NotFoundException(
                        String.format("InvoiceTemplate not found, invoiceTemplateId='%s'",
                                invoiceTemplateId));
            }
            return invoiceTemplate;
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format("Failed to get InvoiceTemplate, invoiceTemplateId='%s'",
                            invoiceTemplateId),
                    ex);
        }
    }

    public void save(List<InvoiceTemplate> invoiceTemplates) {
        log.info("Trying to save InvoiceTemplate events, size={}", invoiceTemplates.size());
        Map<String, InvoiceTemplate> invoiceTemplatesMap = new HashMap<>();
        List<InvoiceTemplate> enriched = invoiceTemplates.stream()
                .peek(invoiceTemplate -> {
                    String invoiceTemplateId = invoiceTemplate.getInvoiceTemplateId();
                    if (invoiceTemplate.getEventType() != InvoiceTemplateEventType.INVOICE_TEMPLATE_CREATED) {
                        InvoiceTemplate previousInvoiceTemplate = invoiceTemplatesMap.computeIfAbsent(
                                invoiceTemplateId,
                                key -> get(invoiceTemplateId));
                        BeanUtil.merge(previousInvoiceTemplate, invoiceTemplate);
                    }
                })
                .peek(invoiceTemplate -> invoiceTemplatesMap.put(
                        invoiceTemplate.getInvoiceTemplateId(),
                        invoiceTemplate))
                .collect(Collectors.toList());

        try {
            invoiceTemplateDao.save(enriched);
            log.info("InvoiceTemplate events have been saved, size={}", enriched.size());
        } catch (DaoException ex) {
            throw new StorageException(
                    String.format("Failed to save chargeback events, size=%d", enriched.size()), ex);
        }
    }
}
