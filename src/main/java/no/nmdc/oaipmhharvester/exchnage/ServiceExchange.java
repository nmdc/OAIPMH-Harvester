package no.nmdc.oaipmhharvester.exchnage;

import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Service;

/**
 *
 * @author kjetilf
 */
@Service
public class ServiceExchange implements Processor {

    private DatasetDao datasetDao;
    
    public ServiceExchange(DatasetDao datasetDao) {
        this.datasetDao = datasetDao;
    }

    public void process(Exchange exchange) throws Exception {
        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
        Dataset dataset = datasetDao.findByFilenameHarvested(exchange.getIn().getHeader("CamelFileAbsolutePath", String.class));
        exchange.getOut().setHeader("identifier", dataset.getIdentifier());
        exchange.getOut().setBody(exchange.getIn().getBody());
    }
}
