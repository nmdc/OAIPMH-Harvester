package no.nmdc.oaipmhharvester.config;

import no.nmdc.oaipmhharvester.service.HarvestService;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author kjetilf
 */
@Service
public class RestHarvestRoute extends RouteBuilder {

    @Autowired
    private HarvestService harvestService;
    
    @Override
    public void configure() throws Exception {
        
        onException(Exception.class).log(LoggingLevel.ERROR, "Error during harvest rest route.");
        
        from("jms:queue:nmdc/start-harvest")
            .to("log:start_harvest?level=INFO")
            .bean(harvestService, "harvest")   
            .to("log:end?level=INFO");
        
    }

}
