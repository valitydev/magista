package dev.vality.magista.query;


import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by vpankrashkin on 03.08.16.
 */
public class BaseCompositeQuery<T, CT> extends BaseQuery<T, CT> implements CompositeQuery<T, CT> {
    private final Function<QueryContext, QueryResult<T, CT>> execFunction;
    private final BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction;
    private List<Query> queries;

    public BaseCompositeQuery(Object descriptor, QueryParameters params,
                              Function<QueryContext, QueryResult<T, CT>> execFunction,
                              BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction) {
        super(descriptor, params);
        if (execFunction == null) {
            throw new NullPointerException("Null exec function is not allowed");
        }
        this.execFunction = execFunction;
        this.parallelExecFunction = parallelExecFunction;
    }

    public static <T, CT> BaseCompositeQuery<T, CT> newInstance(
            Object descriptor, QueryParameters params,
            List<Query> queries,
            Function<QueryContext, QueryResult<T, CT>> execFunction
    ) {
        return newInstance(descriptor, params, queries, execFunction, null);
    }

    public static <T, CT> BaseCompositeQuery<T, CT> newInstance(
            Object descriptor,
            QueryParameters params,
            List<Query> queries,
            Function<QueryContext, QueryResult<T, CT>> execFunction,
            BiFunction<QueryContext, List<QueryResult>, QueryResult<T, CT>> parallelExecFunction
    ) {
        BaseCompositeQuery<T, CT> compositeQuery =
                new BaseCompositeQuery<>(descriptor, params, execFunction, parallelExecFunction);
        compositeQuery.setChildQueries(queries);
        return compositeQuery;
    }

    public List<Query> getChildQueries() {
        return queries;
    }

    protected void setChildQueries(List<Query> queries) {
        this.queries = queries.stream().peek(query -> query.setParentQuery(this)).collect(Collectors.toList());
    }

    public boolean isParallel() {
        return parallelExecFunction != null;
    }

    @Override
    public QueryResult<T, CT> execute(QueryContext context) throws QueryExecutionException {
        return execFunction.apply(context);
    }

    @Override
    public QueryResult<T, CT> execute(QueryContext context, List<QueryResult> collectedResults)
            throws QueryExecutionException {
        if (parallelExecFunction != null) {
            return parallelExecFunction.apply(context, collectedResults);
        } else {
            return CompositeQuery.super.execute(context, collectedResults);
        }
    }
}
