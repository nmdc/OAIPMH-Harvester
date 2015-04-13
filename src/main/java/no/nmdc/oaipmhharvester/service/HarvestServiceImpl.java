package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.RecordType;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 *
 * @author sjurl
 */
@Service(value = "harvestService")
public class HarvestServiceImpl implements HarvestService {

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    @Autowired
    private OAIPMHService oaipmhService;

    @Override
    public void harvest() {
        List<String> baseUrls = (List<String>) (List<?>) harvesterConfiguration.getList("base.url");
        List<String> metadataFormats = (List<String>) (List<?>) harvesterConfiguration.getList("metadata.format");

        for (String baseUrl : baseUrls) {
            List<MetadataFormatType> metadataFormatTypes = null;
            try {
                metadataFormatTypes = oaipmhService.getListMetadataFormat(baseUrl, null);
            } catch (XmlException | IOException ex) {
                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while getting metadata formats from: " + baseUrl, ex);
            }
            if (metadataFormatTypes != null) {
                for (MetadataFormatType mft : metadataFormatTypes) {
                    for (String metadataFormat : metadataFormats) {
                        if (mft.getMetadataPrefix().equalsIgnoreCase(metadataFormat)) {
                            try {
                                parseAndWriteMetadata(baseUrl, mft);
                            } catch (XmlException | IOException | OAIPMHException ex) {
                                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while harvesting from: "
                                        + baseUrl + "\nUsing format: " + mft.getMetadataPrefix(), ex);
                            }
                            oaipmhService.setCurrentResumptionToken(null);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void parseAndWriteMetadata(String baseUrl, MetadataFormatType mft) throws XmlException, IOException, OAIPMHException {
        List<RecordType> records = oaipmhService.getListRecords(baseUrl, mft.getMetadataPrefix(), null, null, oaipmhService.getCurrentResumptionToken(), null);
        for (RecordType record : records) {
            File file = new File(harvesterConfiguration.getString("save.path").concat(record.getHeader().getIdentifier()).concat(".xml"));
            record.save(file);
        }
        if (oaipmhService.getCurrentResumptionToken() != null) {
            parseAndWriteMetadata(baseUrl, mft);
        }
    }
}
