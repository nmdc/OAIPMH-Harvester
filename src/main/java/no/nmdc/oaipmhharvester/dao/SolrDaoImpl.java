package no.nmdc.oaipmhharvester.dao;

import java.nio.charset.Charset;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.impl.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 *
 * @author kjetilf
 */
@Service
public class SolrDaoImpl implements SolrDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolrDaoImpl.class);

    @Autowired
    @Qualifier("harvesterConf")
    private PropertiesConfiguration harvesterConfiguration;

    @Override
    public void delete(String id) {
        LOGGER.info("Deleteing {}", id);
        HttpHeaders headers = new HttpHeaders();
        addAuthHeaders(headers, harvesterConfiguration.getString("solr.username"), harvesterConfiguration.getString("solr.password"));      
        headers.setContentType(MediaType.TEXT_XML);
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>("<delete><id>" + id + "</id></delete>", headers);
        ResponseEntity<Void> responseEntity = restTemplate.postForEntity(harvesterConfiguration.getString("solr.url"), request, Void.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            LOGGER.info("Deleted id {}", id);
        } else {
            LOGGER.warn("Failed to delete id {}", id);
        }
    }

    private void addAuthHeaders(HttpHeaders headers, final String username, final String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.encode(auth.getBytes(Charset.forName("US-ASCII")));
        headers.set("Authorization", "Basic " + new String(encodedAuth));
    }

}
