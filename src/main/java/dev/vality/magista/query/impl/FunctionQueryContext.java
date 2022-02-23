package dev.vality.magista.query.impl;

import dev.vality.magista.dao.DeprecatedSearchDao;
import dev.vality.magista.dao.StatisticsDao;
import dev.vality.magista.query.QueryContext;
import dev.vality.magista.service.DeprecatedTokenGenService;

/**
 * Created by vpankrashkin on 09.08.16.
 */
public class FunctionQueryContext implements QueryContext {

    private final StatisticsDao statisticsDao;

    private final DeprecatedSearchDao searchDao;

    private final DeprecatedTokenGenService tokenGenService;

    public FunctionQueryContext(
            StatisticsDao statisticsDao,
            DeprecatedSearchDao searchDao,
            DeprecatedTokenGenService tokenGenService) {
        this.statisticsDao = statisticsDao;
        this.searchDao = searchDao;
        this.tokenGenService = tokenGenService;
    }

    public StatisticsDao getStatisticsDao() {
        return statisticsDao;
    }

    public DeprecatedSearchDao getSearchDao() {
        return searchDao;
    }

    public DeprecatedTokenGenService getTokenGenService() {
        return tokenGenService;
    }
}
