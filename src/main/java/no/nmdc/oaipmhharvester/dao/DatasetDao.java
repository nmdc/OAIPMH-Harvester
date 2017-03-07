
package no.nmdc.oaipmhharvester.dao;

import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author kjetilf
 */
public interface DatasetDao {
    /**
     * 
     * @param providerurl
     * @param identifier
     * @param set
     * @param format
     * @param filenameHarvested
     * @param filenameDif
     * @param filenameNmdc
     * @param filenameHtml 
     */
    void insert(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml, String hash);
    /**
     * 
     */
    void deleteAll();    

    /**
     * 
     * @param identifer
     * @return 
     */
    boolean notExists(String identifer);

    Dataset findByFilenameHarvested(String filenameHarvested);
}
