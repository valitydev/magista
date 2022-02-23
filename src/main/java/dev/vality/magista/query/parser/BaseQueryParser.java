package dev.vality.magista.query.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by vpankrashkin on 25.08.16.
 */
public abstract class BaseQueryParser implements QueryParser<Map<String, Object>> {

    private final List<QueryParser<Map<String, Object>>> parsers;

    public BaseQueryParser(List<QueryParser<Map<String, Object>>> parsers) {
        this.parsers = new ArrayList<>(parsers);
    }

    @Override
    public List<QueryPart> parseQuery(Map<String, Object> source, QueryPart parent) throws QueryParserException {
        try {
            return source.isEmpty() ? Collections.emptyList() : parsers.stream()
                    .filter(
                            parser -> parser.apply(source, parent))
                    .flatMap(
                            parser -> parser.parseQuery(source, parent).stream()
                    )
                    .peek(
                            queryPart -> queryPart.setChildren(
                                    parseQuery(queryPart.getParameters().getParametersMap(), queryPart)
                            )
                    )
                    .filter(
                            queryPart -> !queryPart.isEmpty()
                    )
                    .collect(Collectors.toList());
        } catch (QueryParserException e) {
            throw e;
        } catch (IllegalArgumentException iae) {
            throw new QueryParserException(iae.getMessage(), iae);
        }
    }

}
