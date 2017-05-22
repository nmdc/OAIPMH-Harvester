
package no.nmdc.oaipmhharvester.dao.dto;

import java.util.Calendar;

/**
 *
 * @author kjetilf
 */
public class Dataset {
    private String id;
    
    
    private String providerurl;
    
    private String xmldata;
    
    private String schema;
            
    private String updatedBy;
    
    private String insertedBy;
    
    private Calendar updatedTime;
    
    private Calendar uinsertedTime;
    
    private String set;
    
    private Boolean valid;
    
    private String filenameHarvested;
    
    private String filenameDif;
    
    private String filenameNmdc;

    private String filenamehtml;    
    
    private String identifier; 
    
    private String originatingCenter;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilenameHarvested() {
        return filenameHarvested;
    }

    public void setFilenameHarvested(String filenameHarvested) {
        this.filenameHarvested = filenameHarvested;
    }

    public String getFilenameDif() {
        return filenameDif;
    }

    public void setFilenameDif(String filenameDif) {
        this.filenameDif = filenameDif;
    }

    public String getFilenameNmdc() {
        return filenameNmdc;
    }

    public void setFilenameNmdc(String filenameNmdc) {
        this.filenameNmdc = filenameNmdc;
    }

    public String getFilenamehtml() {
        return filenamehtml;
    }

    public void setFilenamehtml(String filenamehtml) {
        this.filenamehtml = filenamehtml;
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

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOriginatingCenter() {
        return originatingCenter;
    }

    public void setOriginatingCenter(String originatingCenter) {
        this.originatingCenter = originatingCenter;
    }
    
    
    
    
}
