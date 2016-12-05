package no.nmdc.oaipmhharvester.dao;

import java.util.Calendar;
import java.util.TimeZone;
import javax.sql.DataSource;
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
    public void insert(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("INSERT INTO nmdc_v1.dataset(\n"
                + "            id, providerurl, schema, updated_by, inserted_by, \n"
                + "            updated_time, inserted_time, set, identifier, filename_harvested, filename_dif, filename_nmdc, \n"
                + "            filename_html)\n"
                + "    VALUES (?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, ?, ?);", id, providerurl, format, "nmdc", "nmdc", updatedTime, updatedTime, set, identifier, filenameHarvested, filenameDif, filenameNmdc, filenameHtml);
    }

    @Override
    public void deleteAll() {
        getJdbcTemplate().update("delete from nmdc_v1.dataset");
    }

    @Override
    public boolean notExists(String identifer) {
        return getJdbcTemplate().queryForObject("select COUNT(*) <= 0 FROM nmdc_v1.dataset WHERE identifier = ?", Boolean.class, identifer);
    }

}
