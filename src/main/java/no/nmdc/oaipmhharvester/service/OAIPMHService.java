package no.nmdc.oaipmhharvester.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.HeaderType;
import org.openarchives.oai.x20.IdentifyType;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.RecordType;
import org.openarchives.oai.x20.SetType;

/**
 * Service that implements all OAI-PMH service requests
 *
 * @author sjurl
 */
public interface OAIPMHService {

    /**
     * List all metadata formats that the server can deliver.
     *
     * If the identifier parameter is set the metadata formats for the
     * identified dataset will be returned.
     *
     * @param url base url for the server
     * @param identifier identifier for a single dataset - not mandatory
     * @return List of metadata formats
     * @throws MalformedURLException
     * @throws XmlException
     * @throws IOException
     */
    List<MetadataFormatType> getListMetadataFormat(final String url, final String identifier) throws MalformedURLException, XmlException, IOException;

    /**
     * Get a list of all metadata the server has, if the server uses resumption
     * tokens the resumption token variable will be set and this method will
     * need to be called again using the resumption token to get more metadata
     *
     * @param url base url to the server - mandatory
     * @param metadataPrefix metadata format prefix - mandatory if resumption
     * token is not used
     * @param from earliest date of updates for metadata that should be returned
     * - not mandatory
     * @param until latest date of updates for metadata that should be returned
     * - not mandatory
     * @param set set that metadata must be part of - not mandatory
     * @param resumptionToken resumption token - exclusive
     * @return List of header types that contains information about the datasets
     * at the server
     * @throws MalformedURLException
     * @throws OAIPMHException
     * @throws XmlException
     * @throws IOException
     */
    List<RecordType> getListRecords(final String url, final String metadataPrefix, final Date from, final Date until, final String resumptionToken, final String set) throws MalformedURLException, OAIPMHException, XmlException, IOException;

    /**
     * Get a list of HeaderTypes that contains information about the datasets
     * available from the server
     *
     * @param url base url to the server - mandatory
     * @param metadataPrefix metadata format prefix - mandatory if resumption
     * token is not used
     * @param from earliest date of updates for metadata that should be returned
     * - not mandatory
     * @param until latest date of updates for metadata that should be returned
     * - not mandatory
     * @param set set that metadata must be part of - not mandatory
     * @param resumptionToken resumption token - exclusive
     * @return List of header types that contains information about the datasets
     * at the server
     * @throws MalformedURLException
     * @throws OAIPMHException
     * @throws XmlException
     * @throws IOException
     */
    List<HeaderType> getListIdentifiers(String url, String metadataPrefix, Date from, Date until, String set, String resumptionToken) throws MalformedURLException, OAIPMHException, XmlException, IOException;

    /**
     * Perform the Identify verb on a OAI-PMH server
     *
     * @param url base url for the server
     * @return An object containing the identify information
     * @throws MalformedURLException
     * @throws XmlException
     * @throws IOException
     */
    IdentifyType getIdentify(final String url) throws MalformedURLException, XmlException, IOException;

    /**
     * Get a single record based on an identifier and a metadata prefix
     *
     * @param url base url for the server
     * @param identifier identifier for the dataset - mandatory
     * @param metadataPrefix metadata format - mandatory
     * @return A single record
     * @throws OAIPMHException
     * @throws MalformedURLException
     * @throws XmlException
     * @throws IOException
     */
    RecordType getRecord(final String url, final String identifier, final String metadataPrefix) throws OAIPMHException, MalformedURLException, XmlException, IOException;

    /**
     * Get a list of the Sets that the server has
     *
     * @param url base url of the server
     * @param resumptionToken resumption token
     * @return List of sets
     * @throws MalformedURLException
     * @throws XmlException
     * @throws IOException
     */
    List<SetType> getListSets(String url, String resumptionToken) throws MalformedURLException, XmlException, IOException;

    /**
     * Get the last resumption token retrieved. The system doesn't keep track of
     * what verb it was used with so the outside class must take care of that
     *
     * If a call that returned no resumption token has been performed this
     * method will return null;
     *
     * @return currently last retrieved resumption token
     */
    String getCurrentResumptionToken();

    /**
     * Set the resumption token
     *
     * @param string
     */
    void setCurrentResumptionToken(String string);
}
