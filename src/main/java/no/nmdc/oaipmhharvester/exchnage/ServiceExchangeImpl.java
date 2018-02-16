package no.nmdc.oaipmhharvester.exchnage;

import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author kjetilf
 */
@Service
public class ServiceExchangeImpl implements Processor, ServiceExchange {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceExchangeImpl.class);    
    
    @Autowired
    private DatasetDao datasetDao;

    @Transactional
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
        LOGGER.info("Processing serviceexchange.");
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        Dataset dataset = datasetDao.findByFilenameHarvested(exchange.getIn().getHeader("CamelFileAbsolutePath", String.class));
        LOGGER.info("Found dataset {}.", dataset != null);
        exchange.getOut().setHeader("identifier", dataset.getIdentifier());
        exchange.getOut().setBody(exchange.getIn().getBody());
        LOGGER.info("Finishing serviceexchange.");
        } catch(Exception ex) {
            LOGGER.error("Exception occured.", ex);
        }
    }
}
