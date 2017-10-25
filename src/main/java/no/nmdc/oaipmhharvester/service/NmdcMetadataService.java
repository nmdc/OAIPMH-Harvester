
package no.nmdc.oaipmhharvester.service;

import no.nmdc.oaipmhharvester.service.jaxb.MetadataServiceType;
import org.apache.camel.Exchange;

/**
 *
 * @author kjetilf
 */
public interface NmdcMetadataService {
    
    void getNmdcMetadata(Exchange exchange);
    
}
