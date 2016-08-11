package com.rbkmoney.magista.query;

/**
 * Created by vpankrashkin on 03.08.16.
 */
public abstract class LimitedQueryResult<T, RT> implements QueryResult<T, RT> {
    public abstract int getFullSize();
}