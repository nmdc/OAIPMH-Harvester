package no.nmdc.oaipmhharvester.service.pojo;

import java.util.ArrayList;
import java.util.List;
import org.openarchives.oai.x20.RecordType;

/**
 *
 * @author kjetilf
 */
public class ListRecordsResponse {

    boolean failed;
    private List<RecordType> records;
    private String resumptionToken;
    
    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public List<RecordType> getRecords() {
        return records;
    }

    public void setRecords(List<RecordType> records) {
        this.records = records;
    }

    public void addHash(RecordType recordType) {
        if (this.records == null) {
            this.records = new ArrayList<>();
        }
        this.records.add(recordType);
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }
    
    
}
