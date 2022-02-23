package dev.vality.magista.query.impl.builder;

import dev.vality.magista.config.properties.TokenGenProperties;
import dev.vality.magista.query.Query;
import dev.vality.magista.query.builder.QueryBuilder;
import dev.vality.magista.query.builder.QueryBuilderException;
import dev.vality.magista.query.impl.FunctionQueryContext;
import dev.vality.magista.query.impl.RootQuery;
import dev.vality.magista.query.impl.parser.JsonQueryParser;
import dev.vality.magista.query.parser.QueryPart;
import dev.vality.magista.service.DeprecatedTokenGenService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by vpankrashkin on 28.08.16.
 */
public class QueryBuilderImplTest {
    JsonQueryParser parser = JsonQueryParser.newWeakJsonQueryParser();

    private final QueryBuilder builder = new QueryBuilderImpl();

    private final FunctionQueryContext queryContext;

    public QueryBuilderImplTest() {
        TokenGenProperties tokenGenPropertiesMock = mock(TokenGenProperties.class);
        when(tokenGenPropertiesMock.getKey())
                .thenReturn("jXnZr4u7x!A%D*G-KaPvSgVkYp3s5v8t/B?E(H+MbQeThWmZq4t7w9z$C&F)J@Nc");
        DeprecatedTokenGenService tokenGenService = new DeprecatedTokenGenService(tokenGenPropertiesMock);
        this.queryContext = mock(FunctionQueryContext.class);
        when(queryContext.getTokenGenService()).thenReturn(tokenGenService);
    }

    @Test
    public void test() {
        String json =
                "{'query': {'payments': {'merchant_id': '1','shop_id': '2','invoice_id':'A','payment_id':'B', 'pan_mask':'12**12','from_time': '2016-03-22T00:12:00Z','to_time': '2016-03-22T01:12:00Z'}}}";
        Query query = buildQuery(json);
        assertTrue(query instanceof RootQuery);
        query.getDescriptor();
    }

    @Test
    public void testNoFunctionParse() {
        String json = "{'query': {'payments_geo_stat1': {}}}";
        assertThrows(
                QueryBuilderException.class,
                () -> buildQuery(json));
    }

    Query buildQuery(String json) {
        List<QueryPart> queryParts = parser.parseQuery(json);
        return builder.buildQuery(queryContext, queryParts, null, null, null);
    }
}
