package no.nmdc.oaipmhharvester.service;

import java.util.Map;
import org.apache.camel.OutHeaders;

/**
 * Class that harvests OAI-PMH servers and stores the results to metadata
 * files
 *
 * @author sjurl
 */
public interface HarvestService {

    /**
     * Performs harvesting of OAI-PMH servers
     *
     */
    void harvest(@OutHeaders Map<String, Object> out);
}
