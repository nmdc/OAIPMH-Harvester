package no.nmdc.oaipmhharvester.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.GetRecordType;
import org.openarchives.oai.x20.HeaderType;
import org.openarchives.oai.x20.IdentifyType;
import org.openarchives.oai.x20.ListIdentifiersType;
import org.openarchives.oai.x20.ListMetadataFormatsType;
import org.openarchives.oai.x20.ListRecordsType;
import org.openarchives.oai.x20.ListSetsType;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.OAIPMHDocument;
import org.openarchives.oai.x20.RecordType;
import org.openarchives.oai.x20.SetType;
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

    private static String currentResumptionToken = null;

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
    public List<RecordType> getListRecords(final String url, final String metadataPrefix, final Date from, final Date until, final String resumptionToken, final String set) throws MalformedURLException, OAIPMHException, XmlException, IOException {
        LoggerFactory.getLogger(OAIPMHServiceImpl.class).debug("getListRecords");
        URL performUrl;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (resumptionToken != null) {
            performUrl = new URL(url.concat("?verb=ListRecords&resumptionToken=").concat(resumptionToken));
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

        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        ListRecordsType listrec = document.getOAIPMH().getListRecords();
        List<RecordType> records = null;
        if (listrec != null) {
            records = Arrays.asList(listrec.getRecordArray());
            if (listrec.getResumptionToken() != null && listrec.getResumptionToken().getStringValue() != null && !listrec.getResumptionToken().getStringValue().isEmpty()) {
                currentResumptionToken = listrec.getResumptionToken().getStringValue();
            } else {
                currentResumptionToken = null;
            }
        } else {
            logger.warn("Error getting records {}", document.toString());
        }
        return records;
    }

    @Override
    public List<HeaderType> getListIdentifiers(String url, String metadataPrefix, Date from, Date until, String set, String resumptionToken) throws MalformedURLException, OAIPMHException, XmlException, IOException {
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
        List<HeaderType> headers = Arrays.asList(identifiersType.getHeaderArray());
        if (identifiersType.getResumptionToken() != null && identifiersType.getResumptionToken().getStringValue() != null && !identifiersType.getResumptionToken().getStringValue().isEmpty()) {
            currentResumptionToken = identifiersType.getResumptionToken().getStringValue();
        } else {
            currentResumptionToken = null;
        }
        return headers;
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
    public List<SetType> getListSets(String url, String resumptionToken) throws MalformedURLException, XmlException, IOException {
        String privateUrl = url.concat("?verb=ListSets");
        if (resumptionToken != null) {
            privateUrl = privateUrl.concat("&resumptionToken=").concat(resumptionToken);
        }

        URL performUrl = new URL(privateUrl);
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(performUrl);
        ListSetsType setType = document.getOAIPMH().getListSets();
        List<SetType> sets = Arrays.asList(setType.getSetArray());
        if (setType.getResumptionToken() != null && setType.getResumptionToken().getStringValue() != null && !setType.getResumptionToken().getStringValue().isEmpty()) {
            currentResumptionToken = setType.getResumptionToken().getStringValue();
        } else {
            currentResumptionToken = null;
        }
        return sets;
    }

    @Override
    public String getCurrentResumptionToken() {
        return currentResumptionToken;
    }

    @Override
    public void setCurrentResumptionToken(String string) {
        currentResumptionToken = string;
    }
}
