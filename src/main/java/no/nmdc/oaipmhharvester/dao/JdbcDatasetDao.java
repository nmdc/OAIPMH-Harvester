package no.nmdc.oaipmhharvester.dao;

import java.util.Calendar;
import java.util.TimeZone;
import javax.sql.DataSource;
import org.openarchives.oai.x20.RecordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kjetilf
 */
@Repository
public class JdbcDatasetDao extends JdbcDaoSupport implements DatasetDao {

    @Autowired
    public JdbcDatasetDao(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    @Override
    public void insert(String filename, String baseUrl, RecordType record, String set, String format, String identifer) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("INSERT INTO nmdc_v1.dataset(\n"
                + "            id, filename, providerurl, xmldata, schema, updated_by, inserted_by, \n"
                + "            updated_time, inserted_time, set, identifier)\n"
                + "    VALUES (?, ?, ?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?);", id, filename, baseUrl, record.getMetadata().xmlText(), format, "nmdc", "nmdc", updatedTime, updatedTime, set, identifer);
    }

    @Override
    public void deleteAll() {
        getJdbcTemplate().update("delete from nmdc_v1.dataset");
        getJdbcTemplate().update("delete from nmdc_v1.dataset_failed");
    }

    @Override
    public void insert(String providerurl, String identifier, String reason, String set, String format) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("INSERT INTO nmdc_v1.dataset_failed(\n"
                + "            id, providerurl, identifier, reason, updated_by, updated_time, \n"
                + "            inserted_by, inserted_time, set, format)\n"
                + "    VALUES (?, ?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?);", id, providerurl, identifier, reason, "nmdc", updatedTime, "nmdc", updatedTime, set, format);
    }

    @Override
    public boolean notExists(String identifer) {
       return getJdbcTemplate().queryForObject("select COUNT(*) <= 0 FROM nmdc_v1.dataset WHERE identifier = ?", Boolean.class, identifer);
    }

}
