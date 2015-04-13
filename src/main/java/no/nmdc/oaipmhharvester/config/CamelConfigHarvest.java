package no.nmdc.oaipmhharvester.config;

import java.util.Arrays;
import java.util.List;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * The route configuration for oai-pmh harvesting.
 *
 * @author sjurl
 */
@Configuration
public class CamelConfigHarvest extends CamelConfiguration implements InitializingBean {

    @Autowired
    private GetUniqueMetadataRoute getUniqueMetadataRoute;

    @Autowired
    private HarvestRoute harvestRoute;

    /**
     * Adds all routes to the camel config
     *
     * @return The route.
     */
    @Override
    public List<RouteBuilder> routes() {
        return Arrays.asList(getUniqueMetadataRoute, harvestRoute);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Do nothing.
    }

}
