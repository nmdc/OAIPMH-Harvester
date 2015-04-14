/*
 */
package no.nmdc.oaipmhharvester.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import no.nmdc.oaipmhharvester.exception.OAIPMHException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.xmlbeans.XmlException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.Mockito.*;
import org.openarchives.oai.x20.MetadataFormatType;
import org.openarchives.oai.x20.RecordType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 *
 * @author sjurl
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
public class HarvestServiceImplTest {

    @Configuration
    static class ContextConfiguration {

        @Bean(name = "harvesterConf")
        public PropertiesConfiguration harvesterConfiguration() {
            PropertiesConfiguration pc = new PropertiesConfiguration();
            pc.addProperty("base.url", Arrays.asList("test"));
            pc.addProperty("metadata.format", Arrays.asList("test.format"));
            pc.addProperty("save.path", System.getProperty("java.io.tmpdir").concat(System.getProperty("file.separator")));
            return pc;
        }

        @Bean
        public OAIPMHService oaiPMHservice() {
            OAIPMHService service = mock(OAIPMHService.class);
            List<MetadataFormatType> mfts = new ArrayList<>();
            MetadataFormatType mft = mock(MetadataFormatType.class);
            when(mft.getMetadataPrefix()).thenReturn("test.format");
            mfts.add(mft);
            try {
                when(service.getListMetadataFormat(eq("test"), isNull(String.class))).thenReturn(mfts);
            } catch (XmlException | IOException ex) {
                fail("exception when calling mocked object");
            }

            List<RecordType> records = new ArrayList<>();
            RecordType record = RecordType.Factory.newInstance();
            record.addNewHeader();
            record.getHeader().setIdentifier("testFile");
            record.addNewMetadata();
            records.add(record);
            try {
                when(service.getListRecords(eq("test"), eq("test.format"), isNull(Date.class), isNull(Date.class), isNull(String.class), isNull(String.class))).thenReturn(records);
            } catch (OAIPMHException | XmlException | IOException ex) {
                fail("exception when calling mocked object");
            }

            when(service.getCurrentResumptionToken()).thenReturn(null);

            return service;
        }

        @Bean
        public HarvestService harvestService() {
            HarvestService hs = new HarvestServiceImpl();
            return hs;
        }
    }

    public HarvestServiceImplTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Autowired
    private HarvestService instance;

    /**
     * Test of harvest method, of class HarvestServiceImpl.
     */
    @Test
    public void testHarvest() {
        System.out.println("harvest");
        instance.harvest();
        File file = new File(System.getProperty("java.io.tmpdir").concat(System.getProperty("file.separator")).concat("testFile.xml"));
        assertTrue(file.exists());
        file.delete();

    }

}
