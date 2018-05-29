
package no.nmdc.oaipmhharvester.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author kjetilf
 */
class DatasetRowMapper implements RowMapper<Dataset> {

    @Override
    public Dataset mapRow(ResultSet rs, int rowNum) throws SQLException {
        Dataset dataset = new Dataset();
        dataset.setId(rs.getString("id"));
        dataset.setFilenameHarvested(rs.getString("filename_harvested"));
        dataset.setProviderurl(rs.getString("providerurl"));
        dataset.setSchema(rs.getString("schema"));
        dataset.setUpdatedBy(rs.getString("updated_by"));
        dataset.setInsertedBy(rs.getString("inserted_by"));
        dataset.setSet(rs.getString("set"));
        dataset.setIdentifier(rs.getString("identifier"));
        dataset.setFilenameDif(rs.getString("filename_dif"));
        dataset.setFilenameNmdc(rs.getString("filename_nmdc"));
        dataset.setFilenamehtml(rs.getString("filename_html"));
        dataset.setOriginatingCenter(rs.getString("originating_center"));
        dataset.setOriginalOAOIPMHIdentifier(rs.getString("original_oaipmh_identifier"));
        dataset.setProvidername(rs.getString("providername"));
        return dataset;
    }    
}
