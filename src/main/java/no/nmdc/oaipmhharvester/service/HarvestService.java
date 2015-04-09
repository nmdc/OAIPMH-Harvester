package no.nmdc.oaipmhharvester.service;

import java.io.IOException;
import java.util.Map;
import org.apache.xmlbeans.XmlException;

/**
 * Service interface for harvesting OAI-PMH servers
 *
 * @author sjurl
 */
public interface HarvestService {

    /**
     * Performs harvesting of OAI-PMH servers
     *
     * @return
     * @throws XmlException
     * @throws IOException
     */
    Map<String, Object> harvest() throws XmlException, IOException;
}
