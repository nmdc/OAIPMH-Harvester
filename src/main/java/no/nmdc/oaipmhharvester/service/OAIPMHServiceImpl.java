package no.nmdc.oaipmhharvester.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import no.nmdc.oaipmhharvester.service.pojo.ListIdentifiersResponse;
import no.nmdc.oaipmhharvester.service.pojo.ListRecordsResponse;
import no.nmdc.oaipmhharvester.service.pojo.ListSetsResponse;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.GetRecordType;
import org.openarchives.oai.x20.IdentifyType;
import org.openarchives.oai.x20.ListIdentifiersType;
import org.openarchives.oai.x20.ListMetadataFormatsType;
import org.openarchives.oai.x20.ListRecordsType;
import org.openarchives.oai.x20.ListSetsType;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.OAIPMHDocument;
import org.openarchives.oai.x20.RecordType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service
public class OAIPMHServiceImpl implements OAIPMHService {

    /**
     * Class logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(OAIPMHServiceImpl.class);

    @Override
    public List<MetadataFormatType> getListMetadataFormat(final String url, final String identifier) throws MalformedURLException, XmlException, IOException {
        LoggerFactory.getLogger(OAIPMHServiceImpl.class).debug("getListMetadataFormat");
        String privateUrl = url.concat("?verb=ListMetadataFormats");
        if (identifier != null) {
            privateUrl = privateUrl.concat("&identifier=").concat(identifier);
        }
        URL performUrl = new URL(privateUrl);
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        ListMetadataFormatsType formatlist = document.getOAIPMH().getListMetadataFormats();
        List<MetadataFormatType> metadataFormats = Arrays.asList(formatlist.getMetadataFormatArray());
        return metadataFormats;
    }

    @Override
    public ListRecordsResponse getListRecords(final String url, final String metadataPrefix, final Date from, final Date until, final String resumptionToken, final String set) throws MalformedURLException, OAIPMHException, XmlException, IOException {
        ListRecordsResponse listRecordsResponse = new ListRecordsResponse();
        boolean failed = false;
        LoggerFactory.getLogger(OAIPMHServiceImpl.class).debug("getListRecords");
        URL performUrl;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (resumptionToken != null) {
            performUrl = new URL(url.concat("?verb=ListRecords&resumptionToken=").concat(URLEncoder.encode(resumptionToken, "UTF-8")));
        } else {
            if (metadataPrefix == null) {
                throw new OAIPMHException("MetadataPrefix must be present when using ListRecords");
            }
            String privateUrl = url.concat("?verb=ListRecords&metadataPrefix=").concat(metadataPrefix);
            if (from != null) {
                privateUrl = privateUrl.concat("&from=").concat(sdf.format(from));
            }
            if (until != null) {
                privateUrl = privateUrl.concat("&until=").concat(sdf.format(until));
            }
            if (set != null) {
                privateUrl = privateUrl.concat("&set=").concat(set);
            }

            performUrl = new URL(privateUrl);
        }
        logger.info("Request url: {}", performUrl);

        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        logger.info("Document received.", performUrl);

        if (!(document.getOAIPMH().getErrorArray() != null && document.getOAIPMH().getErrorArray().length > 0)) {
            List<RecordType> records = null;
            ListRecordsType listrec = document.getOAIPMH().getListRecords();
            if (listrec != null) {
                logger.info("Getting list of records.", listrec.sizeOfRecordArray());

                records = Arrays.asList(listrec.getRecordArray());
                listRecordsResponse.setRecords(records);
                if (listrec.getResumptionToken() != null && listrec.getResumptionToken().getStringValue() != null && !listrec.getResumptionToken().getStringValue().isEmpty()) {
                    listRecordsResponse.setResumptionToken(listrec.getResumptionToken().getStringValue());
                }
            } else {
                logger.warn("Error getting records {}", document.toString());
            }
        } else {
            logger.info("Failed due to errors.");
            failed = true;
        }
        listRecordsResponse.setFailed(failed);
        return listRecordsResponse;
    }

    @Override
    public ListIdentifiersResponse getListIdentifiers(String url, String metadataPrefix, Date from, Date until, String set, String resumptionToken) throws MalformedURLException, OAIPMHException, XmlException, IOException {
        ListIdentifiersResponse listIdentifiersResponse = new ListIdentifiersResponse();
        URL performUrl;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (resumptionToken != null) {
            performUrl = new URL(url.concat("?verb=ListIdentifiers&resumptionToken=").concat(resumptionToken));
        } else {
            if (metadataPrefix == null) {
                throw new OAIPMHException("MetadataPrefix must be present when using ListIdentifiers without a resumption token");
            }
            String privateUrl = url.concat("?verb=ListIdentifiers&metadataPrefix=").concat(metadataPrefix);
            if (from != null) {
                privateUrl = privateUrl.concat("&from=").concat(sdf.format(from));
            }
            if (until != null) {
                privateUrl = privateUrl.concat("&until=").concat(sdf.format(until));
            }
            if (set != null) {
                privateUrl = privateUrl.concat("&set=").concat(set);
            }

            performUrl = new URL(privateUrl);
        }
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        ListIdentifiersType identifiersType = document.getOAIPMH().getListIdentifiers();
        listIdentifiersResponse.setIdentifiers(Arrays.asList(identifiersType.getHeaderArray()));
        if (identifiersType.getResumptionToken() != null && identifiersType.getResumptionToken().getStringValue() != null && !identifiersType.getResumptionToken().getStringValue().isEmpty()) {
            listIdentifiersResponse.setResumptionToken(identifiersType.getResumptionToken().getStringValue());
        }
        return listIdentifiersResponse;
    }

    @Override
    public IdentifyType getIdentify(final String url) throws MalformedURLException, XmlException, IOException {
        URL performUrl = new URL(url.concat("?verb=Identify"));
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        return document.getOAIPMH().getIdentify();
    }

    @Override
    public RecordType getRecord(final String url, final String identifier, final String metadataPrefix) throws OAIPMHException, MalformedURLException, XmlException, IOException {
        if (identifier == null || metadataPrefix == null) {
            throw new OAIPMHException("identifier and metadataPrefix are mandatory");
        }
        String privateUrl = url.concat(("?verb=GetRecord&identifier=")).concat(identifier).concat("&metadataPrefix=").concat(metadataPrefix);
        URL performUrl = new URL(privateUrl);
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        GetRecordType record = document.getOAIPMH().getGetRecord();
        return record.getRecord();
    }

    @Override
    public ListSetsResponse getListSets(String url, String resumptionToken) throws MalformedURLException, XmlException, IOException {
        ListSetsResponse listSetsResponse = new ListSetsResponse();
        String privateUrl = url.concat("?verb=ListSets");
        if (resumptionToken != null) {
            privateUrl = privateUrl.concat("&resumptionToken=").concat(resumptionToken);
        }

        URL performUrl = new URL(privateUrl);
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        ListSetsType setType = document.getOAIPMH().getListSets();
        listSetsResponse.setSets(Arrays.asList(setType.getSetArray()));
        if (setType.getResumptionToken() != null && setType.getResumptionToken().getStringValue() != null && !setType.getResumptionToken().getStringValue().isEmpty()) {
            listSetsResponse.setResumptionToken(setType.getResumptionToken().getStringValue());
        }
        return listSetsResponse;
    }

}
