package dev.vality.magista.query.impl;

import dev.vality.magista.dao.DeprecatedSearchDao;
import dev.vality.magista.dao.StatisticsDao;
import dev.vality.magista.query.QueryContext;
import dev.vality.magista.query.QueryContextFactory;
import dev.vality.magista.service.DeprecatedTokenGenService;

/**
 * Created by vpankrashkin on 29.08.16.
 */
public class QueryContextFactoryImpl implements QueryContextFactory {

    private final StatisticsDao statisticsDao;

    private final DeprecatedSearchDao searchDao;

    private final DeprecatedTokenGenService tokenGenService;

    public QueryContextFactoryImpl(
            StatisticsDao statisticsDao,
            DeprecatedSearchDao searchDao,
            DeprecatedTokenGenService tokenGenService) {
        this.statisticsDao = statisticsDao;
        this.searchDao = searchDao;
        this.tokenGenService = tokenGenService;
    }

    @Override
    public QueryContext getContext() {
        return new FunctionQueryContext(statisticsDao, searchDao, tokenGenService);
    }
}
