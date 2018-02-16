
package no.nmdc.oaipmhharvester.service.pojo;

import java.util.List;
import org.openarchives.oai.x20.HeaderType;

/**
 *
 * @author kjetilf
 */
public class ListIdentifiersResponse {

    private String resumptionToken;
    private List<HeaderType> identifiers;
       
    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }

    public void setIdentifiers(List<HeaderType> identifiers) {
        this.identifiers = identifiers;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

    public List<HeaderType> getIdentifiers() {
        return identifiers;
    }       
    
}
