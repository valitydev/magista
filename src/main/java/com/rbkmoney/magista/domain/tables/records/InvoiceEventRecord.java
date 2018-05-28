/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.magista.domain.tables.records;


import com.rbkmoney.magista.domain.enums.InvoiceEventType;
import com.rbkmoney.magista.domain.enums.InvoiceStatus;
import com.rbkmoney.magista.domain.tables.InvoiceEvent;

import java.time.LocalDateTime;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class InvoiceEventRecord extends UpdatableRecordImpl<InvoiceEventRecord> implements Record7<Long, Long, LocalDateTime, InvoiceEventType, String, InvoiceStatus, String> {

    private static final long serialVersionUID = -905012230;

    /**
     * Setter for <code>mst.invoice_event.id</code>.
     */
    public void setId(Long value) {
        set(0, value);
    }

    /**
     * Getter for <code>mst.invoice_event.id</code>.
     */
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>mst.invoice_event.event_id</code>.
     */
    public void setEventId(Long value) {
        set(1, value);
    }

    /**
     * Getter for <code>mst.invoice_event.event_id</code>.
     */
    public Long getEventId() {
        return (Long) get(1);
    }

    /**
     * Setter for <code>mst.invoice_event.event_created_at</code>.
     */
    public void setEventCreatedAt(LocalDateTime value) {
        set(2, value);
    }

    /**
     * Getter for <code>mst.invoice_event.event_created_at</code>.
     */
    public LocalDateTime getEventCreatedAt() {
        return (LocalDateTime) get(2);
    }

    /**
     * Setter for <code>mst.invoice_event.event_type</code>.
     */
    public void setEventType(InvoiceEventType value) {
        set(3, value);
    }

    /**
     * Getter for <code>mst.invoice_event.event_type</code>.
     */
    public InvoiceEventType getEventType() {
        return (InvoiceEventType) get(3);
    }

    /**
     * Setter for <code>mst.invoice_event.invoice_id</code>.
     */
    public void setInvoiceId(String value) {
        set(4, value);
    }

    /**
     * Getter for <code>mst.invoice_event.invoice_id</code>.
     */
    public String getInvoiceId() {
        return (String) get(4);
    }

    /**
     * Setter for <code>mst.invoice_event.invoice_status</code>.
     */
    public void setInvoiceStatus(InvoiceStatus value) {
        set(5, value);
    }

    /**
     * Getter for <code>mst.invoice_event.invoice_status</code>.
     */
    public InvoiceStatus getInvoiceStatus() {
        return (InvoiceStatus) get(5);
    }

    /**
     * Setter for <code>mst.invoice_event.invoice_status_details</code>.
     */
    public void setInvoiceStatusDetails(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>mst.invoice_event.invoice_status_details</code>.
     */
    public String getInvoiceStatusDetails() {
        return (String) get(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<Long, Long, LocalDateTime, InvoiceEventType, String, InvoiceStatus, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<Long, Long, LocalDateTime, InvoiceEventType, String, InvoiceStatus, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field1() {
        return InvoiceEvent.INVOICE_EVENT.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Long> field2() {
        return InvoiceEvent.INVOICE_EVENT.EVENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<LocalDateTime> field3() {
        return InvoiceEvent.INVOICE_EVENT.EVENT_CREATED_AT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<InvoiceEventType> field4() {
        return InvoiceEvent.INVOICE_EVENT.EVENT_TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field5() {
        return InvoiceEvent.INVOICE_EVENT.INVOICE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<InvoiceStatus> field6() {
        return InvoiceEvent.INVOICE_EVENT.INVOICE_STATUS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return InvoiceEvent.INVOICE_EVENT.INVOICE_STATUS_DETAILS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long value2() {
        return getEventId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime value3() {
        return getEventCreatedAt();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventType value4() {
        return getEventType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value5() {
        return getInvoiceId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceStatus value6() {
        return getInvoiceStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getInvoiceStatusDetails();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value1(Long value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value2(Long value) {
        setEventId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value3(LocalDateTime value) {
        setEventCreatedAt(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value4(InvoiceEventType value) {
        setEventType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value5(String value) {
        setInvoiceId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value6(InvoiceStatus value) {
        setInvoiceStatus(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord value7(String value) {
        setInvoiceStatusDetails(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvoiceEventRecord values(Long value1, Long value2, LocalDateTime value3, InvoiceEventType value4, String value5, InvoiceStatus value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached InvoiceEventRecord
     */
    public InvoiceEventRecord() {
        super(InvoiceEvent.INVOICE_EVENT);
    }

    /**
     * Create a detached, initialised InvoiceEventRecord
     */
    public InvoiceEventRecord(Long id, Long eventId, LocalDateTime eventCreatedAt, InvoiceEventType eventType, String invoiceId, InvoiceStatus invoiceStatus, String invoiceStatusDetails) {
        super(InvoiceEvent.INVOICE_EVENT);

        set(0, id);
        set(1, eventId);
        set(2, eventCreatedAt);
        set(3, eventType);
        set(4, invoiceId);
        set(5, invoiceStatus);
        set(6, invoiceStatusDetails);
    }
}
