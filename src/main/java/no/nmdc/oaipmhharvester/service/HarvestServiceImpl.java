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
        List<String> servers = (List<String>) (List<?>) harvesterConfiguration.getList("servers.to.harvest");
        List<String> metadataFormats = (List<String>) (List<?>) harvesterConfiguration.getList("metadata.format");

        for (String server : servers) {
            List<MetadataFormatType> metadataFormatTypes = null;
            String url = harvesterConfiguration.getString(server.concat(".baseurl"));
            String set = harvesterConfiguration.getString(server.concat(".set"));
            try {
                metadataFormatTypes = oaipmhService.getListMetadataFormat(url, null);
            } catch (XmlException | IOException ex) {
                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while getting metadata formats from: " + server, ex);
            }
            if (metadataFormatTypes != null) {
                for (MetadataFormatType mft : metadataFormatTypes) {
                    for (String metadataFormat : metadataFormats) {
                        if (mft.getMetadataPrefix().equalsIgnoreCase(metadataFormat)) {
                            try {
                                parseAndWriteMetadata(url, mft, set);
                            } catch (XmlException | IOException | OAIPMHException ex) {
                                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while harvesting from: "
                                        + server + "\nUsing format: " + mft.getMetadataPrefix(), ex);
                            }
                            oaipmhService.setCurrentResumptionToken(null);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void parseAndWriteMetadata(String baseUrl, MetadataFormatType mft, String set) throws XmlException, IOException, OAIPMHException {
        List<RecordType> records = oaipmhService.getListRecords(baseUrl, mft.getMetadataPrefix(), null, null, oaipmhService.getCurrentResumptionToken(), set);
        if (records != null) {
            for (RecordType record : records) {
                String identifier = record.getHeader().getIdentifier();
                identifier = identifier.replace(":", "_");
                identifier = identifier.replace("/", "-");
                File file = new File(harvesterConfiguration.getString("save.path").concat(identifier).concat(".xml"));
                if (record.getMetadata() != null) {
                    record.getMetadata().save(file);
                } else {
                    LoggerFactory.getLogger(HarvestServiceImpl.class).error("Error handling record ".concat(record.toString()));
                }
            }
            if (oaipmhService.getCurrentResumptionToken() != null) {
                parseAndWriteMetadata(baseUrl, mft, set);
            }
        }
    }
}
