package no.nmdc.oaipmhharvester.config;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author kjetilf
 */
@Configuration
public class ActiveMQConfig {

    /**
     * Active mq properties.
     */
    @Autowired
    @Qualifier("activeMQConf")
    private PropertiesConfiguration activeMQConfiguration;

    /**
     * This modules properties.
     */
    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    /**
     * Create connection factory to activemq.
     *
     * @return
     */
    @Bean
    public ActiveMQConnectionFactory jmsConnectionFactory() {
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL(activeMQConfiguration.getString("activemq.brokerurl"));
        return factory;
    }

    /**
     * Create pooled connection.
     *
     * @return Pooled connection factory.
     */
    @Bean
    public PooledConnectionFactory pooledConnectionFactory() {
        PooledConnectionFactory factory = new PooledConnectionFactory();
        factory.setMaxConnections(activeMQConfiguration.getInt("max.connections"));
        factory.setConnectionFactory(jmsConnectionFactory());
        factory.initConnectionsPool();
        return factory;
    }

    /**
     * Configure JMS.
     * @return
     */
    @Bean
    public JmsConfiguration jmsConfig() {
        JmsConfiguration conf = new JmsConfiguration();
        conf.setConnectionFactory(pooledConnectionFactory());
        conf.setConcurrentConsumers(harvesterConfiguration.getInt("max.consumers"));
        return conf;
    }

    /**
     * Get activeMQ component.
     *
     * @return component.
     */
    @Bean
    public ActiveMQComponent activemq() {
        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConfiguration(jmsConfig());
        return activeMQComponent;
    }


}
