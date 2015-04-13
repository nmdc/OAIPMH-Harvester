package no.nmdc.oaipmhharvester.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Route that harvests all configured servers every x minutes
 *
 * @author sjurl
 */
@Service
public class HarvestRoute extends RouteBuilder {

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    private static final String HARVEST_PERIOD = "harvest.period";

    @Override
    public void configure() throws Exception {
        String harvestperiod = "3600000";
        if (harvesterConfiguration.getString(HARVEST_PERIOD) != null && !harvesterConfiguration.getString(HARVEST_PERIOD).isEmpty()) {
            harvestperiod = harvesterConfiguration.getString(HARVEST_PERIOD);
        }

        from("timer://harvesttimer?fixedRate=true&period=".concat(harvestperiod))
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .to("harvestService")
                .to("log:end?level=INFO");
    }
}
