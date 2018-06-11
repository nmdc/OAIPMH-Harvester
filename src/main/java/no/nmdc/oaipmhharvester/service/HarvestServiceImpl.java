package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.dao.SolrDao;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import no.nmdc.oaipmhharvester.service.pojo.ListRecordsResponse;
import no.nmdc.oaipmhharvester.service.pojo.ParseAndWriteResponse;
import org.apache.camel.Exchange;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
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
import org.springframework.util.DigestUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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

    @Autowired
    private SolrDao solrDao;

    @PostConstruct
    public void init() {
        harvest(null);
    }

    @Transactional
    @Override
    public void harvest(Exchange exchange) {
        boolean hasFailed = false;
        Calendar startTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
        LOGGER.info("Start harvesting.");
        Map<String, Object> out = new HashMap<String, Object>();
        List<String> listHash = new ArrayList();
        List<String> servers = (List<String>) (List<?>) harvesterConfiguration.getList("servers.to.harvest");
        List<Dataset> datasetsPreHarvest = datasetDao.findAll();
        Set<String> preAllIdentifiers = getAllIdentifiers(datasetsPreHarvest);
        LOGGER.info("Harvesting servers {}.", servers.toArray());
        for (String server : servers) {
            List<String> metadataFormats = (List<String>) (List<?>) harvesterConfiguration.getList(server + ".metadata.formats");
            LOGGER.info("Starting from server {}", server);
            List<MetadataFormatType> metadataFormatTypes = null;
            String url = harvesterConfiguration.getString(server.concat(".baseurl"));
            String set = harvesterConfiguration.getString(server.concat(".set"));
            try {
                LOGGER.info("Getting metadata formats.");
                metadataFormatTypes = oaipmhService.getListMetadataFormat(url, null);
                LOGGER.info("Getting metadata formats {}.", metadataFormatTypes.toArray());
            } catch (XmlException | IOException ex) {
                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while getting metadata formats from: " + server, ex);
                hasFailed = true;
            }
            if (metadataFormatTypes != null) {
                for (MetadataFormatType mft : metadataFormatTypes) {
                    for (String metadataFormat : metadataFormats) {
                        LOGGER.info("Get metadata format {}.", metadataFormat);
                        if (mft.getMetadataPrefix().equalsIgnoreCase(metadataFormat)) {
                            try {
                                List<String> hashes = new ArrayList();
                                ParseAndWriteResponse res = parseAndWriteMetadata(url, mft, set, out, null, hashes, server, preAllIdentifiers);

                                if (!res.isFailed()) {
                                    listHash.addAll(hashes);
                                }
                            } catch (XmlException | IOException | OAIPMHException ex) {
                                LoggerFactory.getLogger(HarvestServiceImpl.class).error("Exception thrown while harvesting from: "
                                        + server + "\nUsing format: " + mft.getMetadataPrefix(), ex);
                                hasFailed = true;
                            }
                            break;
                        }
                    }
                }
            }
        }
        if (!hasFailed) {
            List<Dataset> datasets = datasetDao.getUpdatedOlderThan(startTime);
            for (Dataset dataset : datasets) {
                FileUtils.deleteQuietly(new File(dataset.getFilenameDif()));
                FileUtils.deleteQuietly(new File(dataset.getFilenamehtml()));
                FileUtils.deleteQuietly(new File(dataset.getFilenameNmdc()));
                FileUtils.deleteQuietly(new File(dataset.getFilenameHarvested()));
                datasetDao.deleteByIdentifier(dataset.getIdentifier());
                solrDao.delete(dataset.getIdentifier());
            }
            for (String identifier : preAllIdentifiers) {
                datasetDao.deleteByIdentifier(identifier);
            }
        }

    }

    private ParseAndWriteResponse parseAndWriteMetadata(String baseUrl, MetadataFormatType mft, String set, Map<String, Object> out, String resumptionToken, List<String> hashes, String providername, Set<String> preAllIdentifiers) throws XmlException, IOException, OAIPMHException {
        ParseAndWriteResponse res = new ParseAndWriteResponse();
        List<String> listHash = new ArrayList();
        LOGGER.info("List records");
        ListRecordsResponse records = oaipmhService.getListRecords(baseUrl, mft.getMetadataPrefix(), null, null, resumptionToken, set);
        LOGGER.info("Records retrieved");
        if (records != null && records.getRecords() != null) {
            for (RecordType record : records.getRecords()) {
                String identifier = new String(DigestUtils.md5DigestAsHex((record.getHeader().getIdentifier() + "." + providername).getBytes()));
                preAllIdentifiers.remove(identifier);
                String originalIdentifier = record.getHeader().getIdentifier();
                String hash = new String(DigestUtils.md5DigestAsHex(identifier.getBytes()));
                if (record.getHeader().getStatus() != DELETED) {
                    /**
                     * Insert record.
                     */
                    LOGGER.info("Record {}, {}", identifier, hash);
                    try {
                        String filenameHarvested = harvesterConfiguration.getString("dir.prefix.harvested") + hash + ".xml";
                        listHash.add(filenameHarvested);
                        String filenameDif = harvesterConfiguration.getString("dir.prefix.dif") + hash + ".xml";
                        listHash.add(filenameDif);
                        String filenameNmdc = harvesterConfiguration.getString("dir.prefix.nmdc") + hash + ".xml";
                        listHash.add(filenameNmdc);
                        String filenameHtml = harvesterConfiguration.getString("dir.prefix.html") + hash + ".xml";
                        listHash.add(filenameHtml);
                        if (record.getMetadata() != null) {
                            File file = new File(filenameHarvested);
                            LOGGER.info("Writing to file {}", file.getAbsolutePath());
                            LOGGER.info("Parent {}", file.getParentFile().getAbsolutePath());
                            LOGGER.info("Parent directory exists : {}, Parent isDirectory : {}, File exists : {}", file.getParentFile().exists(), file.getParentFile().isDirectory(), file.exists());
                            FileUtils.writeStringToFile(file, record.getMetadata().xmlText(), "UTF-8");
                        } else {
                            LoggerFactory.getLogger(HarvestServiceImpl.class).error("Error handling record ".concat(record.toString()));
                        }
                        if (datasetDao.notExists(identifier)) {
                            LOGGER.info("Inserting metadata in db.");
                            String originatingCenter = getOriginatingCenter(record.getMetadata().xmlText());
                            datasetDao.insert(baseUrl, identifier, set, mft.getMetadataNamespace(), filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter, providername, originalIdentifier);
                        } else {
                            LOGGER.info("Updating metadata in db.");
                            String originatingCenter = getOriginatingCenter(record.getMetadata().xmlText());
                            datasetDao.update(baseUrl, identifier, set, mft.getMetadataNamespace(), filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter, providername, originalIdentifier);
                        }
                        LOGGER.info("Identifier {}", identifier);
                        out.put("identifer", identifier);
                        LOGGER.info("Hash {}", hash);
                        out.put("hash", hash);
                    } catch (DuplicateKeyException dke) {
                        LOGGER.error("Duplikat identifikator {} : {}", baseUrl, identifier);
                    } catch (Exception dke) {
                        LOGGER.error("Warning for server {} : {}", baseUrl, identifier);
                        LOGGER.error("Warning exception", dke);
                    }
                }

            }
        }
        if (records.getResumptionToken() != null) {
            parseAndWriteMetadata(baseUrl, mft, set, out, records.getResumptionToken(), hashes, providername, preAllIdentifiers);
        }
        hashes.addAll(listHash);
        return res;
    }

    private String getOriginatingCenter(String xmlText) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
        if (isDif(xmlText)) {
            return getDifOriginatingCenter(xmlText);
        } else if (isIso19139(xmlText)) {
            return getIsoOriginatingCenter(xmlText);
        } else {
            return "";
        }
    }

    private boolean isDif(String xmlText) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(IOUtils.toInputStream(xmlText, "UTF-8"));
        Element root = doc.getDocumentElement();
        String namespace = root.getNamespaceURI();
        return namespace.equalsIgnoreCase("http://gcmd.gsfc.nasa.gov/Aboutus/xml/dif/");
    }

    private boolean isIso19139(String xmlText) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(IOUtils.toInputStream(xmlText, "UTF-8"));
        Element root = doc.getDocumentElement();
        String namespace = root.getNamespaceURI();
        return namespace.equalsIgnoreCase("http://www.isotc211.org/2005/gmd");
    }

    private String getIsoOriginatingCenter(String xmlText) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(IOUtils.toInputStream(xmlText, "UTF-8"));
        XPathExpression xp = XPathFactory.newInstance().newXPath().compile("//pointOfContact/CI_ResponsibleParty/organisationName/CharacterString");
        return xp.evaluate(doc);
    }

    private String getDifOriginatingCenter(String xmlText) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(IOUtils.toInputStream(xmlText, "UTF-8"));
        XPathExpression xp = XPathFactory.newInstance().newXPath().compile("//Originating_Center");
        return xp.evaluate(doc);
    }

    private Set<String> getAllIdentifiers(List<Dataset> datasets) {
        Set<String> identifiers = new HashSet();
        for (Dataset dataset : datasets) {
            identifiers.add(dataset.getIdentifier());
        }
        return identifiers;
    }

}
