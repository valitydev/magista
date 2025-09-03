package dev.vality.magista.service;

import dev.vality.damsel.domain.InvoiceTemplate;
import dev.vality.machinegun.eventsink.MachineEvent;
import dev.vality.magista.CommonSearchQueryParams;
import dev.vality.magista.InvoiceTemplateSearchQuery;
import dev.vality.magista.InvoiceTemplateStatus;
import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.listener.InvoiceTemplateListener;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static dev.vality.magista.util.InvoiceTemplateGenerator.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class MerchantStatisticsServiceTest {

    @Autowired
    private InvoiceTemplateListener invoiceTemplateListener;
    @Autowired
    private MerchantStatisticsService merchantStatisticsService;

    private InvoiceTemplate invoiceTemplate;
    private String invoiceTemplateId;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        invoiceTemplateId = "invoiceTemplateId";
        invoiceTemplate = getInvoiceTemplate(getCart());
        MachineEvent message = getEvent(
                invoiceTemplateId,
                1,
                getCreated(invoiceTemplate));
        invoiceTemplateListener.handleMessages(List.of(message));
    }

    @Test
    public void shouldSearchInvoiceTemplates() {
        var commonSearchQueryParams = new CommonSearchQueryParams();
        commonSearchQueryParams.setFromTime(Instant.now().minusSeconds(60).toString());
        commonSearchQueryParams.setToTime(Instant.now().plusSeconds(60).toString());
        commonSearchQueryParams.setPartyId(invoiceTemplate.getPartyRef().id);
        commonSearchQueryParams.setShopIds(List.of(invoiceTemplate.getShopRef().id));
        commonSearchQueryParams.setLimit(1000);
        commonSearchQueryParams.setContinuationToken(null);
        var searchQuery = new InvoiceTemplateSearchQuery();
        searchQuery.setCommonSearchQueryParams(commonSearchQueryParams);
        searchQuery.setInvoiceTemplateId(invoiceTemplateId);
        searchQuery.setInvoiceValidUntil(Instant.now().plusSeconds(30).toString());
        searchQuery.setProduct(invoiceTemplate.getProduct());
        searchQuery.setName(invoiceTemplate.getName());
        searchQuery.setInvoiceTemplateStatus(InvoiceTemplateStatus.created);
        var statInvoiceTemplateResponse = merchantStatisticsService.getInvoiceTemplates(searchQuery);
        assertEquals(1, statInvoiceTemplateResponse.getInvoiceTemplates().size());
        searchQuery.setInvoiceTemplateStatus(InvoiceTemplateStatus.deleted);
        statInvoiceTemplateResponse = merchantStatisticsService.getInvoiceTemplates(searchQuery);
        assertEquals(0, statInvoiceTemplateResponse.getInvoiceTemplates().size());
        searchQuery.setInvoiceTemplateStatus(null);
        searchQuery.setInvoiceValidUntil(null);
        statInvoiceTemplateResponse = merchantStatisticsService.getInvoiceTemplates(searchQuery);
        assertEquals(1, statInvoiceTemplateResponse.getInvoiceTemplates().size());
    }
}
