package dev.vality.magista.dao;

import dev.vality.magista.config.PostgresqlSpringBootITest;
import dev.vality.magista.domain.tables.pojos.InvoiceTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static dev.vality.testcontainers.annotations.util.RandomBeans.random;
import static org.junit.jupiter.api.Assertions.assertEquals;

@PostgresqlSpringBootITest
public class InvoiceTemplateDaoTest {

    @Autowired
    private InvoiceTemplateDao invoiceTemplateDao;

    @Test
    public void saveAndGet() {
        Class<InvoiceTemplate> type = InvoiceTemplate.class;
        InvoiceTemplate expected = random(type);
        invoiceTemplateDao.save(Collections.singletonList(expected));
        InvoiceTemplate actual = invoiceTemplateDao.get(expected.getInvoiceTemplateId());
        assertEquals(expected, actual);
    }

    @Test
    public void updatePreviousEventTest() {
        InvoiceTemplate expected = random(InvoiceTemplate.class);
        invoiceTemplateDao.save(Collections.singletonList(expected));
        invoiceTemplateDao.save(Collections.singletonList(expected));
        InvoiceTemplate invoiceTemplateWithPreviousEventId = new InvoiceTemplate(expected);
        invoiceTemplateWithPreviousEventId.setEventId(expected.getEventId() - 1);
        invoiceTemplateDao.save(Collections.singletonList(invoiceTemplateWithPreviousEventId));
        InvoiceTemplate actual = invoiceTemplateDao.get(expected.getInvoiceTemplateId());
        assertEquals(expected, actual);
    }
}
