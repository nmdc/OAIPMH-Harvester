package no.nmdc.oaipmhharvester.dao;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import javax.sql.DataSource;
import no.nmdc.oaipmhharvester.dao.dto.Dataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

/**
 *
 * @author kjetilf
 */
@Repository
public class JdbcDatasetDao extends JdbcDaoSupport implements DatasetDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcDatasetDao.class);
    
    @Autowired
    public JdbcDatasetDao(DataSource dataSource) {
        this.setDataSource(dataSource);
    }

    @Override
    public void insert(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml, String hash, String originatingCenter, String providername, String originalAIIdentifier) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("INSERT INTO nmdc_v1.dataset(\n"
                + "            id, providerurl, schema, updated_by, inserted_by, \n"
                + "            updated_time, inserted_time, set, identifier, filename_harvested, filename_dif, filename_nmdc, \n"
                + "            filename_html, hash, originating_center, providername, original_oaipmh_identifier)\n"
                + "    VALUES (?, ?, ?, ?, ?, \n"
                + "            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", id, providerurl, format, "nmdc", "nmdc", updatedTime, updatedTime, set, identifier, filenameHarvested, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter, providername, originalAIIdentifier);
    }

    @Override
    public void update(String providerurl, String identifier, String set, String format, String filenameHarvested, String filenameDif, String filenameNmdc, String filenameHtml, String hash, String originatingCenter, String providername, String originalAIIdentifier) {
        String id = java.util.UUID.randomUUID().toString();
        Calendar updatedTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        getJdbcTemplate().update("UPDATE nmdc_v1.dataset\n"
                + "   SET filename_harvested=?, providerurl=?, schema=?, updated_by=?, \n"
                + "       updated_time=?, set=?, \n"
                + "       filename_dif=?, filename_nmdc=?, filename_html=?, hash=?, originating_center=?, providername, original_oaipmh_identifier, providername=?, original_oaipmh_identifier=?\n"
                + " WHERE identifier=?;", filenameHarvested, providerurl, format, "nmdc", updatedTime, set, filenameDif, filenameNmdc, filenameHtml, hash, originatingCenter, identifier, providername, originalAIIdentifier);
    }

    @Override
    public boolean notExists(String identifer) {
        return getJdbcTemplate().queryForObject("select COUNT(*) <= 0 FROM nmdc_v1.dataset WHERE identifier = ?", Boolean.class, identifer);
    }

    @Override
    public Dataset findByFilenameHarvested(String filenameHarvested) {
        LOGGER.info("Checking if file exist in db. {}", filenameHarvested);
        LOGGER.info("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center FROM nmdc_v1.dataset where filename_harvested={}", filenameHarvested);
        return getJdbcTemplate().queryForObject("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center, providername, original_oaipmh_identifier FROM nmdc_v1.dataset where filename_harvested=?", new DatasetRowMapper(), filenameHarvested);
    }

    @Override
    public Dataset findByIdentifier(String identifier) {
        return getJdbcTemplate().queryForObject("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center, providername, original_oaipmh_identifier FROM nmdc_v1.dataset where identifier=?", new DatasetRowMapper(), identifier);
    }

    @Override
    public void deleteByIdentifier(String identifier) {
        getJdbcTemplate().update("delete from nmdc_v1.dataset\n"
                + " WHERE identifier=?;", identifier);
    }

    @Override
    public List<Dataset> getUpdatedOlderThan(Calendar startTime) {
        return getJdbcTemplate().query("SELECT id, filename_harvested, providerurl, schema, updated_by, inserted_by, updated_time, inserted_time, set, identifier, filename_dif, filename_nmdc, filename_html, hash, originating_center, providername, original_oaipmh_identifier FROM nmdc_v1.dataset where updated_time < ?", new Object[] {startTime}, new DatasetRowMapper());
    }

}
