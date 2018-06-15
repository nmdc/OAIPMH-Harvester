package no.nmdc.oaipmhharvester.exchnage;

import java.io.File;
import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
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

    @Autowired
    @Qualifier("harvesterConf")
    private Configuration configuration;    
    
    @Transactional
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            LOGGER.info("Processing serviceexchange.");
            exchange.getOut().setHeaders(exchange.getIn().getHeaders());
            Dataset dataset = datasetDao.findByFilenameHarvested(exchange.getIn().getHeader("CamelFileAbsolutePath", String.class));
            if (dataset != null) {
                LOGGER.info("Found dataset {}.", dataset != null);
                exchange.getOut().setHeader("identifier", dataset.getIdentifier());
                exchange.getOut().setBody(exchange.getIn().getBody());
                LOGGER.info("Finishing serviceexchange.");
            } else {
                LOGGER.info("Dataset was not found {}.", exchange.getIn().getHeader("CamelFileAbsolutePath", String.class));
                exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE); 
                FileUtils.deleteQuietly(new  File(exchange.getIn().getHeader("CamelFileAbsolutePath", String.class)));
                FileUtils.deleteQuietly(new  File(configuration.getString("dir.prefix.nmdc") + File.separatorChar + exchange.getIn().getHeader("CamelFileNameOnly", String.class)));
                FileUtils.deleteQuietly(new  File(configuration.getString("dir.prefix.dif") + File.separatorChar + exchange.getIn().getHeader("CamelFileNameOnly", String.class)));
                FileUtils.deleteQuietly(new  File(configuration.getString("dir.prefix.html") + File.separatorChar + exchange.getIn().getHeader("CamelFileNameOnly", String.class)));
                throw new RuntimeException("Dataset with identifier " + exchange.getIn().getHeader("CamelFileAbsolutePath", String.class) + " was not found.");
            }
        } catch (DataAccessException ex) {
            LOGGER.error("DataAccessException occured.", ex);
            throw new RuntimeException("Dataset with identifier was not found.", ex);
        } catch (Exception ex) {
            LOGGER.error("Exception occured.", ex);
            throw new RuntimeException("Exception occured.", ex);
        }
    }
}
