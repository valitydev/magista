package dev.vality.magista.event.mapper.impl;

import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.magista.domain.enums.PayoutStatus;
import dev.vality.magista.domain.tables.pojos.Payout;
import dev.vality.magista.event.handler.PayoutHandler;
import dev.vality.magista.service.PayoutService;
import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.PayoutChange;
import dev.vality.payout.manager.PayoutStatusChanged;
import org.springframework.stereotype.Component;

@Component
public class PayoutStatusChangedHandler implements PayoutHandler {

    private final PayoutService payoutEventService;

    public PayoutStatusChangedHandler(PayoutService payoutEventService) {
        this.payoutEventService = payoutEventService;
    }

    @Override
    public void handle(PayoutChange change, Event event) {
        Payout payout = payoutEventService.getPayout(event.getPayoutId());

        payout.setEventCreatedAt(TypeUtil.stringToLocalDateTime(event.getCreatedAt()));
        payout.setSequenceId(event.getSequenceId());

        PayoutStatusChanged statusChanged = change.getStatusChanged();
        payout.setStatus(TBaseUtil.unionFieldToEnum(statusChanged.getStatus(), PayoutStatus.class));

        if (statusChanged.getStatus().isSetCancelled()) {
            payout.setCancelledDetails(statusChanged.getStatus().getCancelled().getDetails());
        }
        payoutEventService.updatePayout(payout);
    }

    @Override
    public boolean accept(PayoutChange change) {
        return change.isSetStatusChanged();
    }
}
