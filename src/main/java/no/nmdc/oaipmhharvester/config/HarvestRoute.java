package no.nmdc.oaipmhharvester.config;

import no.nmdc.oaipmhharvester.service.HarvestService;
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

    @Autowired
    private HarvestService harvestService;
    
    private static final String HARVEST_CRON = "harvest.cron";

    @Override
    public void configure() throws Exception {
        String harvestcron = harvesterConfiguration.getString(HARVEST_CRON);

        from(harvestcron)
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .bean(harvestService,"harvest")                
                .to("log:end?level=INFO");               
    }
}
