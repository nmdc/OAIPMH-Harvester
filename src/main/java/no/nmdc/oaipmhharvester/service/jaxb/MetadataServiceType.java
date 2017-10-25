package no.nmdc.oaipmhharvester.service.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author kjetilf
 */
@XmlRootElement(name = "metadata", namespace = "http://www.nmdc.no/commons/metadata/v1")
@XmlAccessorType(XmlAccessType.FIELD)
public class MetadataServiceType {

    @XmlElement(name = "hash", namespace = "http://www.nmdc.no/commons/metadata/v1")
    private String hash;
    @XmlElement(name = "landingPage", namespace = "http://www.nmdc.no/commons/metadata/v1")
    private String landingPage;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getLandingPage() {
        return landingPage;
    }

    public void setLandingPage(String landingPage) {
        this.landingPage = landingPage;
    }
        
}
