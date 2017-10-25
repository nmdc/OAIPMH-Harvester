package no.nmdc.oaipmhharvester.config;

import no.imr.formats.nmdcommon.v2.ListType;
import no.nmdc.oaipmhharvester.service.NmdcMetadataService;
import no.nmdc.oaipmhharvester.service.jaxb.MetadataServiceType;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestParamType;
import org.openarchives.oai.x20.MetadataType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author kjetilf
 */
@Service
public class RestRoute extends RouteBuilder {

    @Autowired
    private NmdcMetadataService metadataService;

    @Override
    public void configure() throws Exception {

        onException(Exception.class).log(LoggingLevel.ERROR, "Error during rest route.");

        rest("/harvest").get("/start").route()
                .to("log:start?level=INFO")
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .to("jms:queue:nmdc/start-harvest")
                .setBody(constant("Job queued."))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("200"))
                .to("log:end?level=INFO");

        rest("/nmdc").get("/getnmdcmetadata").outType(MetadataServiceType.class)
                .param().name("identifier").type(RestParamType.query).endParam()
                .route()
                .to("log:start?level=INFO")
                .errorHandler(deadLetterChannel("jms:queue:dead").maximumRedeliveries(3).redeliveryDelay(30000))
                .bean(metadataService, "getNmdcMetadata")
                .to("log:end?level=INFO");

    }

}
