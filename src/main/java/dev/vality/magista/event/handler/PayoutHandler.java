package dev.vality.magista.event.handler;

import dev.vality.payout.manager.Event;
import dev.vality.payout.manager.PayoutChange;

public interface PayoutHandler extends Handler<PayoutChange, Event> {
}
