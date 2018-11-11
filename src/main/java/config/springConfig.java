package config;

import dao.BusDataDao;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class springConfig {

    @Bean
    public BusDataDao busDataDao(){
        return new BusDataDao();
    }
}
