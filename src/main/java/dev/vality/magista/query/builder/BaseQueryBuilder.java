package dev.vality.magista.query.builder;

import dev.vality.magista.exception.BadTokenException;
import dev.vality.magista.query.Query;
import dev.vality.magista.query.QueryContext;
import dev.vality.magista.query.parser.QueryPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by vpankrashkin on 24.08.16.
 */
public abstract class BaseQueryBuilder implements QueryBuilder {
    private final Logger log = LoggerFactory.getLogger(this.getClass());


    private final List<QueryBuilder> builders;

    public BaseQueryBuilder(List<QueryBuilder> builders) {
        this.builders = new ArrayList<>(builders);
    }

    @Override
    public Query buildQuery(QueryContext queryContext, List<QueryPart> queryParts, String continuationToken,
                            QueryPart parentQueryPart, QueryBuilder baseBuilder) throws QueryBuilderException {
        try {
            List<Query> queries = queryParts.isEmpty() ? Collections.emptyList() : builders.stream()
                    .filter(
                            builder -> builder.apply(queryParts, parentQueryPart))
                    .map(
                            builder -> builder.buildQuery(queryContext, queryParts, continuationToken, parentQueryPart,
                                    baseBuilder == null ? this : baseBuilder)
                    )
                    .collect(Collectors.toList());
            if (queries.size() > 1) {
                throw new QueryBuilderException("Build result has more than one query");
            } else if (queries.size() == 1) {
                return queries.get(0);
            } else {
                log.warn("No builders matched following query parts: {}", queryParts);
                throw new QueryBuilderException("Can't build query, no match to process");
            }
        } catch (BadTokenException | QueryBuilderException e) {
            throw e;
        } catch (IllegalArgumentException iae) {
            throw new QueryBuilderException(iae.getMessage(), iae);
        }
    }

}
