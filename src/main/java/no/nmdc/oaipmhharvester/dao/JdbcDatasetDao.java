package no.nmdc.oaipmhharvester.dao;

import java.util.Calendar;
import java.util.TimeZone;
import javax.sql.DataSource;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public void insert(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml, String hash, String originatingCenter) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("INSERT INTO nmdc_v1.dataset(\n"
                + "            id, providerurl, schema, updated_by, inserted_by, \n"
                + "            updated_time, inserted_time, set, identifier, filename_harvested, filename_dif, filename_nmdc, \n"
                + "            filename_html, hash, originating_center)\n"
                + "    VALUES (?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", id, providerurl, format, "nmdc", "nmdc", updatedTime, updatedTime, set, identifier, filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter);
    }

    @Override
    public void update(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml, String hash, String originatingCenter) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("UPDATE nmdc_v1.dataset\n"
                + "   SET filename_harvested=?, providerurl=?, schema=?, updated_by=?, \n"
                + "       updated_time=?, set=?, \n"
                + "       filename_dif=?, filename_nmdc=?, filename_html=?, hash=?, originating_center=?\n"
                + " WHERE identifier=?;", filenameHarvested, providerurl, format, "nmdc", updatedTime, set, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter, identifier);
    }

    @Override
    public boolean notExists(String identifer) {
        return getJdbcTemplate().queryForObject("select COUNT(*) <= 0 FROM nmdc_v1.dataset WHERE identifier = ?", Boolean.class, identifer);
    }

    @Transactional
    @Override
    public Dataset findByFilenameHarvested(String filenameHarvested) {
        return getJdbcTemplate().queryForObject("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center FROM nmdc_v1.dataset where filename_harvested=?", new DatasetRowMapper(), filenameHarvested);
    }

    @Transactional
    @Override
    public Dataset findByIdentifier(String identifier) {
        return getJdbcTemplate().queryForObject("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center FROM nmdc_v1.dataset where identifier=?", new DatasetRowMapper(), identifier);
    }

}
