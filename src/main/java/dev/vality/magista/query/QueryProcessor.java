package dev.vality.magista.query;

import dev.vality.magista.exception.BadTokenException;

/**
 * Created by vpankrashkin on 29.08.16.
 */
public interface QueryProcessor<S, R> {
    R processQuery(S source) throws BadTokenException, QueryProcessingException;
}
