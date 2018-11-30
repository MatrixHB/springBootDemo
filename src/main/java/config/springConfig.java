package config;

import com.alibaba.druid.pool.DruidDataSource;
import dao.BusDataDao;
import dao.BusDataDaoMyBatis;
import entities.BusData;
import mapper.BusDataMappper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class springConfig {

    @Bean
    public BusDataDao busDataDao(){
        return new BusDataDao();
    }

    @Bean
    public BusDataDaoMyBatis busDataDaoMyBatis(){
        return new BusDataDaoMyBatis();
    }
}
