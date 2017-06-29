package no.nmdc.oaipmhharvester.config;

import no.nmdc.oaipmhharvester.service.HarvestService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author kjetilf
 */
@Service
public class RestRoute extends RouteBuilder {
    
    @Override
    public void configure() throws Exception {
        
        rest("/harvest").get("/start").route()                
                .to("log:start?level=INFO")
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .to("jms:queue:nmdc/start-harvest")
                .setBody(constant("Job queued."))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .to("log:end?level=INFO");
       
        
    }

}
