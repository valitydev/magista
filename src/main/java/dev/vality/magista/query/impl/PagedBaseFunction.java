package dev.vality.magista.query.impl;

import dev.vality.magista.exception.BadTokenException;
import dev.vality.magista.query.Query;
import dev.vality.magista.query.QueryContext;
import dev.vality.magista.query.QueryParameters;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Created by vpankrashkin on 23.08.16.
 */
public abstract class PagedBaseFunction<T, CT> extends ScopedBaseFunction<T, CT> {

    public static final int MAX_SIZE_VALUE = 1000;

    private final String continuationToken;

    public PagedBaseFunction(Object descriptor, QueryParameters params, String name, String continuationToken) {
        super(descriptor, params, name);
        this.continuationToken = continuationToken;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public LocalDateTime getTime(QueryContext queryContext) {
        return getContext(queryContext).getTokenGenService().extractTime(continuationToken).orElse(null);
    }

    public static class PagedBaseParameters extends ScopedBaseParameters {

        public PagedBaseParameters(Map<String, Object> parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public PagedBaseParameters(QueryParameters parameters, QueryParameters derivedParameters) {
            super(parameters, derivedParameters);
        }

        public Integer getSize() {
            return Optional.ofNullable(getIntParameter(Parameters.SIZE_PARAMETER, true))
                    .orElse(MAX_SIZE_VALUE);
        }

    }

    public static class PagedBaseValidator extends ScopedBaseValidator {
        @Override
        public void validateQuery(Query query, QueryContext queryContext) throws IllegalArgumentException {
            super.validateQuery(query, queryContext);
            if (query instanceof PagedBaseFunction) {
                validateContinuationToken(queryContext, query.getQueryParameters(),
                        ((PagedBaseFunction) query).getContinuationToken());
            }
        }

        @Override
        public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
            super.validateParameters(parameters);
            PagedBaseParameters pagedBaseParameters = super.checkParamsType(parameters, PagedBaseParameters.class);
            checkParamsResult(pagedBaseParameters.getSize() > MAX_SIZE_VALUE,
                    String.format(
                            "Size must be less or equals to %d but was %d",
                            MAX_SIZE_VALUE,
                            pagedBaseParameters.getSize()
                    )
            );
        }

        private void validateContinuationToken(QueryContext queryContext, QueryParameters queryParameters,
                                               String continuationToken) throws BadTokenException {
            if (continuationToken != null) {
                final boolean validToken =
                        getContext(queryContext).getTokenGenService().validToken(queryParameters, continuationToken);
                if (!validToken) {
                    throw new BadTokenException("Token validation failure");
                }
            }
        }
    }
}
