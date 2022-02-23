package dev.vality.magista.config.testconfiguration;

import dev.vality.damsel.merch_stat.StatRequest;
import dev.vality.damsel.merch_stat.StatResponse;
import dev.vality.magista.dao.DeprecatedSearchDao;
import dev.vality.magista.dao.StatisticsDao;
import dev.vality.magista.query.QueryProcessor;
import dev.vality.magista.query.impl.QueryContextFactoryImpl;
import dev.vality.magista.query.impl.QueryProcessorImpl;
import dev.vality.magista.query.impl.builder.QueryBuilderImpl;
import dev.vality.magista.query.impl.parser.JsonQueryParser;
import dev.vality.magista.service.DeprecatedTokenGenService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class QueryProcessorConfig {

    @Bean
    public QueryProcessor<StatRequest, StatResponse> queryProcessor(
            StatisticsDao statisticsDao,
            DeprecatedSearchDao searchDao,
            DeprecatedTokenGenService tokenGenService) {
        var contextFactory = new QueryContextFactoryImpl(statisticsDao, searchDao, tokenGenService);
        return new QueryProcessorImpl(
                JsonQueryParser.newWeakJsonQueryParser(),
                new QueryBuilderImpl(),
                contextFactory);
    }
}