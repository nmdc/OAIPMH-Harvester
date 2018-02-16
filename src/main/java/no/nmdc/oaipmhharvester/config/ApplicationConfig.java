package no.nmdc.oaipmhharvester.config;

import no.nmdc.oaipmhharvester.service.HarvestService;
import no.nmdc.oaipmhharvester.service.HarvestServiceImpl;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This imports and prepares the configuration files.
 *
 * @author kjetilf
 */
@Configuration
public class ApplicationConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);
    
    /**
     * Import the general activemq configuration.
     *
     * @return  The activemq configuration properties.
     * @throws ConfigurationException   Could not import.
     */
    @Bean(name = "activeMQConf")
    public PropertiesConfiguration getActiveMQConfiguration() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty("catalina.base") + "/conf/activemq.properties");
        conf.setReloadingStrategy(new FileChangedReloadingStrategy());
        return conf;
    }

    /**
     * Import this modules configuration.
     *
     * @return  Configuration properties.
     * @throws ConfigurationException       Could not import.
     */
    @Bean(name = "harvesterConf")
    public PropertiesConfiguration getHarvesterConfig() throws ConfigurationException {
        PropertiesConfiguration conf = new PropertiesConfiguration(System.getProperty("catalina.base") + "/conf/oaipmh-harvester.properties");
        LOGGER.info("Importing {}", conf.getFileName());
        conf.setReloadingStrategy(new FileChangedReloadingStrategy());
        return conf;
    }
    
    @Bean
    public HarvestService harvestService() throws ConfigurationException {
        HarvestService harvestService = new HarvestServiceImpl();
        return harvestService;
    }
}
