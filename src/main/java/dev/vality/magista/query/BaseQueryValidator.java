package dev.vality.magista.query;

import dev.vality.magista.query.impl.FunctionQueryContext;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;

/**
 * Created by vpankrashkin on 23.08.16.
 */
public abstract class BaseQueryValidator implements QueryValidator {
    @Override
    public void validateParameters(QueryParameters parameters) throws IllegalArgumentException {
        if (parameters == null) {
            checkParamsResult(true, "Parameters're not defined");
        }
    }

    protected void validateTimePeriod(TemporalAccessor from, TemporalAccessor to) throws IllegalArgumentException {
        if (from != null && to != null) {
            Instant fromInstant = Instant.from(from);
            Instant toInstant = Instant.from(to);
            if (fromInstant.compareTo(toInstant) > 0) {
                checkParamsResult(true, "TimeRange is not valid [from: " + fromInstant + ", to: " + toInstant + "]");
            }
        }
    }

    protected <T extends QueryParameters> T checkParamsType(QueryParameters parameters,
                                                            Class<? extends QueryParameters> expectedType) {
        if (!expectedType.isAssignableFrom(parameters.getClass())) {
            checkParamsResult(true,
                    "Parameters has wrong type:" + parameters.getClass() + ", expected: " + expectedType);
        }
        return (T) parameters;
    }

    protected void checkParamsResult(boolean hasError, String msg) {
        if (hasError) {
            throw new IllegalArgumentException(msg);
        }
    }

    protected void checkParamsResult(boolean hasError, String fieldName, String msg) {
        if (hasError) {
            checkParamsResult(hasError, "Validation failed for field: " + fieldName + ": " + msg);
        }
    }

    protected void checkParamsResult(boolean hasError, String fieldName, String msg, String additionalInfo) {
        if (hasError) {
            checkParamsResult(hasError, "Validation failed for field: " + fieldName + ": " + msg +
                    " (" + additionalInfo + ")");
        }
    }

    protected FunctionQueryContext getContext(QueryContext context) {
        if (FunctionQueryContext.class.isAssignableFrom(context.getClass())) {
            return (FunctionQueryContext) context;
        } else {
            throw new QueryExecutionException("Wrong context type");
        }

    }
}
