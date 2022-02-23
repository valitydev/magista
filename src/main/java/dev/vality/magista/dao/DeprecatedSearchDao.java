package dev.vality.magista.dao;

import dev.vality.damsel.merch_stat.*;
import dev.vality.magista.query.impl.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

@Deprecated
public interface DeprecatedSearchDao {

    Collection<Map.Entry<Long, StatInvoice>> getInvoices(
            InvoicesFunction.InvoicesParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    );

    Collection<Map.Entry<Long, StatPayment>> getPayments(
            PaymentsFunction.PaymentsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    );

    Collection<Map.Entry<Long, StatRefund>> getRefunds(
            RefundsFunction.RefundsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    );

    Collection<Map.Entry<Long, StatPayout>> getPayouts(
            PayoutsFunction.PayoutsParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    );

    Collection<Map.Entry<Long, StatChargeback>> getChargebacks(
            ChargebacksFunction.ChargebacksParameters parameters,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            LocalDateTime whereTime,
            int limit
    );

}
