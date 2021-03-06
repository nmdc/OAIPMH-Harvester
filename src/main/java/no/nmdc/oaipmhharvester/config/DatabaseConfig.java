
package no.nmdc.oaipmhharvester.config;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.beans.PropertyVetoException;
import javax.sql.DataSource;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 * @author kjetilf
 */
@Configuration
@EnableTransactionManagement
public class DatabaseConfig {
        
    /**
     * Class logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
    
    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration configuration;
    
    @Bean(destroyMethod = "close")
    public DataSource dataSource() throws PropertyVetoException, ConfigurationException {
        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        LOGGER.info("Driver {}", configuration.getString("jdbc.driverClassName"));
        dataSource.setDriverClass(configuration.getString("jdbc.driverClassName"));
        LOGGER.info("URL {}", configuration.getString("jdbc.url"));
        dataSource.setJdbcUrl(configuration.getString("jdbc.url"));
        LOGGER.info("username {}", configuration.getString("jdbc.username"));
        dataSource.setUser(configuration.getString("jdbc.username"));
        dataSource.setPassword(configuration.getString("jdbc.password"));
        LOGGER.info("maxPoolSize {}", configuration.getString("jdbc.maxPoolSize"));
        dataSource.setMaxPoolSize(configuration.getInt("jdbc.maxPoolSize"));
        LOGGER.info("minPoolSize {}", configuration.getString("jdbc.minPoolSize"));
        dataSource.setMinPoolSize(configuration.getInt("jdbc.minPoolSize"));
        dataSource.setAcquireIncrement(configuration.getInt("jdbc.acquireIncrement"));
        dataSource.setIdleConnectionTestPeriod(configuration.getInt("jdbc.idleConnectionTestPeriod"));
        return dataSource;
    }
    
    @Bean
    public PlatformTransactionManager transactionManager() throws PropertyVetoException, ConfigurationException {
        return new DataSourceTransactionManager(dataSource());
    }

   
}
