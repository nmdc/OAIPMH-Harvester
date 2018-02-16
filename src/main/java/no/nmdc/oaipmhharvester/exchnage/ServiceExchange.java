
package no.nmdc.oaipmhharvester.exchnage;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author kjetilf
 */
public interface ServiceExchange extends Processor {
    
    @Override
    void process(Exchange exchange) throws Exception;
    
}
