package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.ListMetadataFormatsType;
import org.openarchives.oai.x20.ListRecordsType;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.OAIPMHDocument;
import org.openarchives.oai.x20.RecordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Class that harvests OAI-PMH servers and stores the results to a metadata
 * files and returns a map of all the files and full path to the files
 *
 * @author sjurl
 */
@Service(value = "harvestService")
public class HarvestServiceImpl implements HarvestService {

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    @Override
    public Map<String, Object> harvest() throws XmlException, IOException {
        List<String> baseUrls = (List<String>) (List<?>) harvesterConfiguration.getList("base.url");
        List<String> metadataFormats = (List<String>) (List<?>) harvesterConfiguration.getList("metadata.format");
        Map<String, Object> files = new HashMap<>();
        
        for (String baseUrl : baseUrls) {
            URL url = new URL(baseUrl.concat("?verb=ListMetadataFormats"));
            OAIPMHDocument document = OAIPMHDocument.Factory.parse(url);
            ListMetadataFormatsType formatlist = document.getOAIPMH().getListMetadataFormats();
            for (MetadataFormatType mft : formatlist.getMetadataFormatArray()) {
                for (String metadataFormat : metadataFormats) {
                    if (mft.getMetadataPrefix().equalsIgnoreCase(metadataFormat)) {
                        URL listrecords = new URL(baseUrl.concat("?verb=ListRecords&metadataPrefix=").concat(mft.getMetadataPrefix()));
                        parseAndWriteMetadata(baseUrl, mft, listrecords, files);
                        break;
                    }
                }
            }

        }
        return files;
    }

    private void parseAndWriteMetadata(String baseUrl, MetadataFormatType mft, URL listrecords, Map<String, Object> files) throws XmlException, MalformedURLException, IOException {
        OAIPMHDocument document = OAIPMHDocument.Factory.parse(listrecords);
        ListRecordsType listrec = document.getOAIPMH().getListRecords();
        for (RecordType record : listrec.getRecordArray()) {
            File file = new File(harvesterConfiguration.getString("save.path").concat(record.getHeader().getIdentifier()).concat(".xml"));
            record.save(file);
//            files.put(record.getHeader().getIdentifier(), file.getAbsolutePath());
        }
        if (listrec.getResumptionToken() != null && listrec.getResumptionToken().getStringValue() != null && !listrec.getResumptionToken().getStringValue().isEmpty()) {
            parseAndWriteMetadata(baseUrl, mft, new URL(baseUrl.concat("?verb=ListRecords").concat("&resumptionToken=").concat(listrec.getResumptionToken().getStringValue())), files);
        }
    }
}
