package dev.vality.magista.query.impl.builder;

import dev.vality.magista.query.*;
import dev.vality.magista.query.builder.QueryBuilder;
import dev.vality.magista.query.builder.QueryBuilderException;
import dev.vality.magista.query.parser.QueryPart;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by vpankrashkin on 28.08.16.
 */
public abstract class AbstractQueryBuilder implements QueryBuilder {

    protected Stream<QueryPart> getMatchedPartsStream(Object descriptor, List<QueryPart> queryParts) {
        return queryParts.stream().filter(queryPart -> descriptor.equals(queryPart.getDescriptor()));
    }


    protected <T, CT> CompositeQuery<T, CT> createCompositeQuery(
            Object descriptor,
            QueryParameters derivedParameters,
            List<Query> childQueries,
            Function<QueryContext, QueryResult<T, CT>> execFunction,
            BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction
    ) {
        return BaseCompositeQuery
                .newInstance(descriptor, new QueryParameters(Collections.emptyMap(), derivedParameters), childQueries,
                        execFunction, parallelExecFunction);
    }

    protected CompositeQuery createCompositeQuery(
            Object descriptor,
            QueryParameters derivedParameters,
            List<Query> childQueries
    ) {
        return createCompositeQuery(
                descriptor, derivedParameters, childQueries,
                context -> new BaseQueryResult<>(
                        () -> childQueries.stream().map(query -> query.execute(context)),
                        () -> childQueries.stream().map(query -> query.execute(context)).collect(Collectors.toList())
                ),
                (context, queryResults) -> new BaseQueryResult<>(
                        () -> queryResults.stream(),
                        () -> queryResults
                )

        );
    }

    protected <T, CT> CompositeQuery<T, CT> createCompositeQuery(
            Object descriptor,
            List<Query> childQueries,
            Function<QueryContext, QueryResult<T, CT>> execFunction,
            BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction
    ) {
        return createCompositeQuery(descriptor, new QueryParameters(Collections.emptyMap(), null), childQueries,
                execFunction, parallelExecFunction);
    }

    protected QueryParameters getParameters(QueryPart queryPart) {
        return queryPart == null ? null : queryPart.getParameters();
    }

    protected List<Query> buildQueries(Object matchDescriptor, List<QueryPart> queryParts,
                                       Function<QueryPart, Query> queryCreator) {
        List<QueryPart> matchedParts = getMatchedPartsStream(matchDescriptor, queryParts).collect(Collectors.toList());
        return matchedParts.stream().map(queryPart -> queryCreator.apply(queryPart)).collect(Collectors.toList());
    }

    protected Query buildSingleQuery(Object matchDescriptor, List<QueryPart> queryParts,
                                     Function<QueryPart, Query> queryCreator) {
        List<Query> queries = buildQueries(matchDescriptor, queryParts, queryCreator);
        if (queries.size() == 0) {
            throw new QueryBuilderException("No queries found in referred data");
        } else if (queries.size() > 1) {
            throw new QueryBuilderException("Only one query expected");
        } else {
            return queries.get(0);
        }
    }

    protected <T, CT> Query<T, CT> buildAndWrapQueries(
            Object matchDescriptor,
            List<QueryPart> queryParts,
            Function<QueryPart, Query> queryCreator,
            QueryParameters parentParameters,
            Function<QueryContext, QueryResult<T, CT>> execFunction,
            BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction
    ) {
        List<Query> queries = buildQueries(matchDescriptor, queryParts, queryCreator);
        return createCompositeQuery(queries.get(0).getDescriptor(), parentParameters, queries, execFunction,
                parallelExecFunction);
    }
}
