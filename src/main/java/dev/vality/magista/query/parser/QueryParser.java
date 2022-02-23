package dev.vality.magista.query.parser;

import java.util.List;

/**
 * Created by vpankrashkin on 24.08.16.
 */
public interface QueryParser<S> {
    List<QueryPart> parseQuery(S source, QueryPart parent) throws QueryParserException;

    boolean apply(S source, QueryPart parent);
}
