
package no.nmdc.oaipmhharvester.dao.dto;

import java.util.Calendar;

/**
 *
 * @author kjetilf
 */
public class Dataset {
    private String id;
    
    private String filename;
    
    private String providerurl;
    
    private String xmldata;
    
    private String schema;
            
    private String updatedBy;
    
    private String insertedBy;
    
    private Calendar updatedTime;
    
    private Calendar uinsertedTime;
    
    private String set;
    
    private Boolean valid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getProviderurl() {
        return providerurl;
    }

    public void setProviderurl(String providerurl) {
        this.providerurl = providerurl;
    }

    public String getXmldata() {
        return xmldata;
    }

    public void setXmldata(String xmldata) {
        this.xmldata = xmldata;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getInsertedBy() {
        return insertedBy;
    }

    public void setInsertedBy(String insertedBy) {
        this.insertedBy = insertedBy;
    }

    public Calendar getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Calendar updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Calendar getUinsertedTime() {
        return uinsertedTime;
    }

    public void setUinsertedTime(Calendar uinsertedTime) {
        this.uinsertedTime = uinsertedTime;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public Boolean getValid() {
        return valid;
    }

    public void setValid(Boolean valid) {
        this.valid = valid;
    }
    
    
}
