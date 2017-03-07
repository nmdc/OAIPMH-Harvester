package no.nmdc.oaipmhharvester.config;

import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.exchnage.ServiceExchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Route that adds any new or changed files that has been harvested to the
 * defined queue
 *
 * @author sjurl
 */
@Service
public class GetUniqueMetadataRoute extends RouteBuilder {

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;
    
    @Autowired
    private DatasetDao datasetDao;
            
    @Override
    public void configure() throws Exception {
        from("file:" + harvesterConfiguration.getString("dir.prefix.harvested") + "?noop=true&idempotentKey=${file:name}-${file:modified}")
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .to("log:end?level=INFO")
                .process(new ServiceExchange(datasetDao))
                .to("jms:queue:nmdc/harvest-validate");
    }

}
