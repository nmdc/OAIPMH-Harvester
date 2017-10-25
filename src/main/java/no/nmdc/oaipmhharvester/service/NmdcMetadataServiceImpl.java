package no.nmdc.oaipmhharvester.service;

import no.nmdc.oaipmhharvester.service.jaxb.MetadataServiceType;
import org.apache.camel.Exchange;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 *
 * @author kjetilf
 */
@Service
public class NmdcMetadataServiceImpl implements NmdcMetadataService {

    @Autowired
    @Qualifier("harvesterConf")
    private Configuration configuration;

    @Override
    public void getNmdcMetadata(Exchange exchange) {
        String hash = new String(DigestUtils.md5DigestAsHex(exchange.getIn().getHeader("identifier", String.class).getBytes()));
        String landingPage = configuration.getString("pre.landingpage") + "/" + hash;
        MetadataServiceType metadataServiceType = new MetadataServiceType();
        metadataServiceType.setHash(hash);
        metadataServiceType.setLandingPage(landingPage);
        exchange.getOut().setBody(metadataServiceType);
        exchange.getOut().setHeader(Exchange.CONTENT_TYPE, "application/xml");
        exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, 200);
    }

}
