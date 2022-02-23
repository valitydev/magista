package dev.vality.magista.query;

/**
 * Created by vpankrashkin on 23.08.16.
 */
public interface QueryValidator {
    void validateParameters(QueryParameters parameters) throws IllegalArgumentException;

    default void validateQuery(Query query, QueryContext queryContext) throws IllegalArgumentException {
        validateParameters(query.getQueryParameters());
    }
}
