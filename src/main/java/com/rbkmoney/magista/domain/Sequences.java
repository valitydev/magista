/*
 * This file is generated by jOOQ.
*/
package com.rbkmoney.magista.domain;


import javax.annotation.Generated;

import org.jooq.Sequence;
import org.jooq.impl.SequenceImpl;


/**
 * Convenience access to all sequences in mst
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Sequences {

    /**
     * The sequence <code>mst.invoice_event_stat_id_seq</code>
     */
    public static final Sequence<Long> INVOICE_EVENT_STAT_ID_SEQ = new SequenceImpl<Long>("invoice_event_stat_id_seq", Mst.MST, org.jooq.impl.SQLDataType.BIGINT.nullable(false));
}