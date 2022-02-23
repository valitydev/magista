package dev.vality.magista.util;

import dev.vality.damsel.domain.LifetimeInterval;

import java.time.LocalDateTime;

public class LifetimeIntervalThriftUtil {

    public static LocalDateTime getInvoiceValidUntil(
            LocalDateTime invoiceValidUntil,
            LifetimeInterval invoiceLifetime) {
        if (invoiceLifetime.isSetSeconds()) {
            invoiceValidUntil = invoiceValidUntil.plusSeconds(invoiceLifetime.getSeconds());
        }
        if (invoiceLifetime.isSetMinutes()) {
            invoiceValidUntil = invoiceValidUntil.plusMinutes(invoiceLifetime.getMinutes());
        }
        if (invoiceLifetime.isSetHours()) {
            invoiceValidUntil = invoiceValidUntil.plusHours(invoiceLifetime.getHours());
        }
        if (invoiceLifetime.isSetDays()) {
            invoiceValidUntil = invoiceValidUntil.plusDays(invoiceLifetime.getDays());
        }
        if (invoiceLifetime.isSetMonths()) {
            invoiceValidUntil = invoiceValidUntil.plusMonths(invoiceLifetime.getMonths());
        }
        if (invoiceLifetime.isSetYears()) {
            invoiceValidUntil = invoiceValidUntil.plusYears(invoiceLifetime.getYears());
        }
        return invoiceValidUntil;
    }
}
