package com.rbkmoney.magista.config;

import com.rbkmoney.magista.dao.*;
import com.rbkmoney.magista.geo.dao.CityLocationsDao;
import com.rbkmoney.magista.geo.dao.CityLocationsDaoImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;

/**
 * Created by vpankrashkin on 10.08.16.
 */
@Configuration
public class DaoConfig {

    @Bean
    @DependsOn("dbInitializer")
    public StatisticsDao statisticsDao(DataSource ds) {
        return new StatisticsDaoImpl(ds);
    }

    @Bean
    @DependsOn("dbInitializer")
    public InvoiceDao invoiceDao(DataSource ds) {
        return new InvoiceDaoImpl(ds);
    }

    @Bean
    @DependsOn("dbInitializer")
    public PaymentDao paymentDao(DataSource ds) {
        return new PaymentDaoImpl(ds);
    }

    @Bean
    @DependsOn("dbInitializer")
    public CustomerDao customerDao(DataSource ds) {
        return new CustomerDaoImpl(ds);
    }

    @Bean
    @DependsOn("dbInitializer")
    public EventDao eventDao(DataSource ds) {
        return new EventDaoImpl(ds);
    }

    @Bean
    @DependsOn("dbInitializer")
    public CityLocationsDao cityLocationDao(DataSource ds) {
        return new CityLocationsDaoImpl(ds);
    }

}
