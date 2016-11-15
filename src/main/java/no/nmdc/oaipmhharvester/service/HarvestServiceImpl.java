package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.camel.OutHeaders;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.XmlException;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.RecordType;
import static org.openarchives.oai.x20.StatusType.DELETED;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author sjurl
 */
@Service(value = "harvestService")
public class HarvestServiceImpl implements HarvestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestServiceImpl.class);

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    @Autowired
    private OAIPMHService oaipmhService;

    @Autowired
    private DatasetDao datasetDao;

    @Transactional
    @Override
    public synchronized void harvest(@OutHeaders Map<String, Object> out) {
        List<String> servers = (List<String>) (List<?>) harvesterConfiguration.getList("servers.to.harvest");
        List<String> metadataFormats = (List<String>) (List<?>) harvesterConfiguration.getList("metadata.format");
        datasetDao.deleteAll();
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
                                parseAndWriteMetadata(url, mft, set, out);
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

    private void parseAndWriteMetadata(String baseUrl, MetadataFormatType mft, String set, Map<String, Object> out) throws XmlException, IOException, OAIPMHException {
        List<RecordType> records = oaipmhService.getListRecords(baseUrl, mft.getMetadataPrefix(), null, null, oaipmhService.getCurrentResumptionToken(), set);
        if (records != null) {
            for (RecordType record : records) {
                if (record.getHeader().getStatus() != DELETED) {
                    String identifier = record.getHeader().getIdentifier();
                    identifier = identifier.replace(":", "_");
                    identifier = identifier.replace("/", "-");
                    File file = new File(harvesterConfiguration.getString("save.path").concat(identifier).concat(".xml"));
                    /**
                     * Insert record.
                     */
                    try {
                        if (datasetDao.notExists(record.getHeader().getIdentifier())) {
                            datasetDao.insert(file.getAbsolutePath(), baseUrl, record, set, mft.getMetadataNamespace(), record.getHeader().getIdentifier());
                            if (record.getMetadata() != null) {
                                record.getMetadata().save(file);
                            } else {
                                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Error handling record ".concat(record.toString()));
                            }
                        }
                    } catch (DuplicateKeyException dke) {
                        datasetDao.insert(baseUrl, identifier, "Duplicate identifier.", set, mft.getMetadataNamespace());
                        LOGGER.info("Duplikat identifikator {} : {}", baseUrl, identifier);
                    } catch (Exception dke) {
                        datasetDao.insert(baseUrl, identifier, set, set, mft.getMetadataNamespace());
                        LOGGER.info("Warning for server {} : {}", baseUrl, identifier);
                        LOGGER.info("Warning exception", dke);
                    }

                }
                if (oaipmhService.getCurrentResumptionToken() != null) {
                    parseAndWriteMetadata(baseUrl, mft, set, out);
                }
            }
        }
    }

}
