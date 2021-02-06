package config;

import Model.Database;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
public class SpringConfig {
    @Bean
    public Database getDatabase() throws SQLException {
        return new Database();
    }
}
