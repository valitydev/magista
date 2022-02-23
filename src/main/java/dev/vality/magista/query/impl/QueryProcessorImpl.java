package dev.vality.magista.query.impl;

import dev.vality.damsel.merch_stat.StatRequest;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.magista.exception.BadTokenException;
import dev.vality.magista.query.*;
import dev.vality.magista.query.builder.QueryBuilder;
import dev.vality.magista.query.impl.builder.QueryBuilderImpl;
import dev.vality.magista.query.impl.parser.JsonQueryParser;
import dev.vality.magista.query.parser.QueryParser;
import dev.vality.magista.query.parser.QueryPart;

import java.util.List;

public class QueryProcessorImpl implements QueryProcessor<StatRequest, StatResponse> {
    private final QueryParser<String> sourceParser;
    private final QueryBuilder queryBuilder;
    private final QueryContextFactory queryContextFactory;

    public QueryProcessorImpl(QueryContextFactory queryContextFactory) {
        this(new JsonQueryParser(), new QueryBuilderImpl(), queryContextFactory);
    }

    public QueryProcessorImpl(QueryParser<String> sourceParser,
                              QueryBuilder queryBuilder,
                              QueryContextFactory queryContextFactory) {
        this.sourceParser = sourceParser;
        this.queryBuilder = queryBuilder;
        this.queryContextFactory = queryContextFactory;
    }

    @Override
    public StatResponse processQuery(StatRequest source) throws BadTokenException, QueryProcessingException {
        List<QueryPart> queryParts = sourceParser.parseQuery(source.getDsl(), null);
        QueryContext queryContext = queryContextFactory.getContext();
        Query query = queryBuilder.buildQuery(
                queryContext,
                queryParts,
                source.getContinuationToken(),
                null,
                null
        );
        QueryResult queryResult = query.execute(queryContext);
        Object result = queryResult.getCollectedStream();
        if (result instanceof StatResponse) {
            return (StatResponse) result;
        } else {
            throw new QueryProcessingException("QueryResult has wrong type: " + result.getClass().getName());
        }
    }
}
