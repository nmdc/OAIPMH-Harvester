package no.nmdc.oaipmhharvester.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.SingleRouteCamelConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

/**
 * The route copnfiguration for the validation.
 *
 * @author kjetilf
 */
@Configuration
public class CamelConfigGetUniqueMetadata extends SingleRouteCamelConfiguration implements InitializingBean {

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    private static final String HARVEST_PERIOD = "harvest.period";

    /**
     * The route 1. set up timer. 2. Harvest data from configured servers. 3.
     * Send them to the validation queue.
     *
     * @return The route.
     */
    @Override
    public RouteBuilder route() {
        return new RouteBuilder() {

            @Override
            public void configure() {

                from("file:" + harvesterConfiguration.getString("save.path") + "?noop=true&idempotentKey=${file:name}-${file:modified}")
                        .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                        .to("log:end?level=INFO")
                        .to("jms:queue:nmdc/harvest-validate");
            }
        };
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing.
    }

}
