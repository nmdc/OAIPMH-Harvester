
package no.nmdc.oaipmhharvester.service.pojo;

import java.util.List;
import org.openarchives.oai.x20.SetType;

/**
 *
 * @author kjetilf
 */
public class ListSetsResponse {

    private List<SetType> sets;
    private String resumptionToken;

    public void setSets(List<SetType> sets) {
        this.sets = sets;
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }

    public List<SetType> getSets() {
        return sets;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }
    
    
}
