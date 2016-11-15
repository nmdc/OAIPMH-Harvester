
package no.nmdc.oaipmhharvester.dao;

import org.openarchives.oai.x20.RecordType;

/**
 *
 * @author kjetilf
 */
public interface DatasetDao {
    /**
     * 
     * @param filename
     * @param baseUrl
     * @param record
     * @param set 
     * @param format 
     * @param identifer 
     */
    void insert(String filename, String baseUrl, RecordType record, String set, String format, String identifer);
    /**
     * 
     * @param providerurl
     * @param identifier
     * @param reason
     * @param set
     * @param format 
     */
     void insert(String providerurl, String identifier, String reason, String set, String format);
    /**
     * This deletes everything.
     */
    void deleteAll();    

    /**
     * 
     * @param identifer
     * @return 
     */
    boolean notExists(String identifer);

}
