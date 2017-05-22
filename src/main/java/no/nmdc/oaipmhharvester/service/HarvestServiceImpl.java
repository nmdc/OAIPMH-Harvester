package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import no.nmdc.oaipmhharvester.dao.DatasetDao;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.camel.OutHeaders;
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
import org.xml.sax.InputSource;
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

    @Transactional
    @Override
    public synchronized void harvest(@OutHeaders Map<String, Object> out) {
        List<String> listHash = new ArrayList();
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
                                listHash.addAll(parseAndWriteMetadata(url, mft, set, out));
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

    private List<String> parseAndWriteMetadata(String baseUrl, MetadataFormatType mft, String set, Map<String, Object> out) throws XmlException, IOException, OAIPMHException {
        List<String> listHash = new ArrayList();
        List<RecordType> records = oaipmhService.getListRecords(baseUrl, mft.getMetadataPrefix(), null, null, oaipmhService.getCurrentResumptionToken(), set);
        if (records != null) {
            for (RecordType record : records) {
                if (record.getHeader().getStatus() != DELETED) {
                    String identifier = record.getHeader().getIdentifier();
                    String hash = new String(DigestUtils.md5DigestAsHex(identifier.getBytes()));
                    /**
                     * Insert record.
                     */
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
                            FileUtils.writeStringToFile(file, record.getMetadata().xmlText(), "UTF-8");
                        } else {
                            LoggerFactory.getLogger(HarvestServiceImpl.class).error("Error handling record ".concat(record.toString()));
                        }
                        if (datasetDao.notExists(record.getHeader().getIdentifier())) {
                            String originatingCenter = getOriginatingCenter(record.getMetadata().xmlText());
                            datasetDao.insert(baseUrl, identifier, set, mft.getMetadataNamespace(), filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter);
                        } else {
                            String originatingCenter = getOriginatingCenter(record.getMetadata().xmlText());
                            datasetDao.update(baseUrl, identifier, set, mft.getMetadataNamespace(), filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter);                            
                        }
                        out.put("identifer", identifier);
                        out.put("hash", hash);
                    } catch (DuplicateKeyException dke) {
                        LOGGER.error("Duplikat identifikator {} : {}", baseUrl, identifier);
                    } catch (Exception dke) {
                        LOGGER.error("Warning for server {} : {}", baseUrl, identifier);
                        LOGGER.error("Warning exception", dke);
                    }
                }
                if (oaipmhService.getCurrentResumptionToken() != null) {
                    parseAndWriteMetadata(baseUrl, mft, set, out);
                }
            }
        }
        return listHash;
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

}
