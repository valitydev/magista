package dev.vality.magista.event;

import dev.vality.geck.filter.Condition;
import dev.vality.geck.filter.Filter;
import dev.vality.geck.filter.PathConditionFilter;
import dev.vality.geck.filter.condition.IsNullCondition;
import dev.vality.geck.filter.rule.PathConditionRule;

/**
 * Created by tolkonepiu on 10/11/2016.
 */
public enum ChangeType {

    INVOICE_CREATED("invoice_created", new IsNullCondition().not()),
    INVOICE_STATUS_CHANGED("invoice_status_changed", new IsNullCondition().not()),
    INVOICE_PAYMENT_STARTED("invoice_payment_change.payload.invoice_payment_started", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_status_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_TERMINAL_RECIEPT(
            "invoice_payment_change.payload.invoice_payment_session_change.payload.session_interaction_requested.interaction.payment_terminal_reciept",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_ROUTE_CHANGED("invoice_payment_change.payload.invoice_payment_route_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CASH_FLOW_CHANGED("invoice_payment_change.payload.invoice_payment_cash_flow_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_ADJUSTMENT_CREATED(
            "invoice_payment_change.payload.invoice_payment_adjustment_change.payload.invoice_payment_adjustment_created",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_ADJUSTMENT_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_adjustment_change.payload.invoice_payment_adjustment_status_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_REFUND_CREATED(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_REFUND_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_TRANSACTION_BOUND(
            "invoice_payment_change.payload.invoice_payment_session_change.payload.session_transaction_bound",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CAPTURE_STARTED("invoice_payment_change.payload.invoice_payment_capture_started.params.cash",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_CREATED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_created",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_STATUS_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_status_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_BODY_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_body_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_LEVY_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_levy_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_STAGE_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_stage_changed",
            new IsNullCondition().not()),
    INVOICE_PAYMENT_CHARGEBACK_CASH_FLOW_CHANGED(
            "invoice_payment_change.payload.invoice_payment_chargeback_change.payload.invoice_payment_chargeback_cash_flow_changed",
            new IsNullCondition().not()),
    INVOICE_TEMPLATE_CREATED("invoice_template_created", new IsNullCondition().not()),
    INVOICE_TEMPLATE_UPDATED("invoice_template_updated", new IsNullCondition().not()),
    INVOICE_TEMPLATE_DELETED("invoice_template_deleted", new IsNullCondition().not());

    Filter filter;

    ChangeType(String path, Condition... conditions) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, conditions));
    }

    public Filter getFilter() {
        return filter;
    }
}
