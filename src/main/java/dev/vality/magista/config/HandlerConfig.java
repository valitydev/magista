package dev.vality.magista.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.vality.magista.dao.DeprecatedSearchDao;
import dev.vality.magista.dao.StatisticsDao;
import dev.vality.magista.endpoint.StatisticsServletIface;
import dev.vality.magista.query.impl.QueryContextFactoryImpl;
import dev.vality.magista.query.impl.QueryProcessorImpl;
import dev.vality.magista.query.impl.builder.QueryBuilderImpl;
import dev.vality.magista.query.impl.parser.JsonQueryParser;
import dev.vality.magista.service.DeprecatedMerchantStatisticsHandler;
import dev.vality.magista.service.DeprecatedTokenGenService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by vpankrashkin on 10.08.16.
 */
@Configuration
public class HandlerConfig {

    @Bean
    public StatisticsServletIface statisticsHandler(
            StatisticsDao statisticsDao,
            DeprecatedSearchDao searchDao,
            DeprecatedTokenGenService tokenGenService) {
        return new DeprecatedMerchantStatisticsHandler(new QueryProcessorImpl(
                new JsonQueryParser() {
                    @Override
                    protected ObjectMapper getMapper() {
                        ObjectMapper mapper = super.getMapper();
                        mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
                        return mapper;
                    }
                },
                new QueryBuilderImpl(),
                new QueryContextFactoryImpl(statisticsDao, searchDao, tokenGenService)));
    }
}