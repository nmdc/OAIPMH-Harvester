package no.nmdc.oaipmhharvester.init;

import javax.servlet.Registration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import org.apache.commons.io.FileUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Application initalization. This allows us to init without the web.xml file.
 *
 * @author kjetilf
 */
public class ApplicationInit extends AbstractDispatcherServletInitializer {

    @Override
    protected String[] getServletMappings() {
        return new String[]{"/request/*"};
    }

    @Override
    protected WebApplicationContext createRootApplicationContext() {
        AnnotationConfigWebApplicationContext cxt = new AnnotationConfigWebApplicationContext();
        cxt.scan("no.nmdc.oaipmhharvester.config", "no.nmdc.oaipmhharvester.service", "no.nmdc.oaipmhharvester.dao", "no.nmdc.oaipmhharvester.controller", "no.nmdc.oaipmhharvester.exchnage");
        return cxt;
    }

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        ServletRegistration.Dynamic servletReg = servletContext.addServlet("CamelServlet", org.apache.camel.component.servlet.CamelHttpTransportServlet.class);
        servletReg.addMapping("/rest/*");
        super.onStartup(servletContext);
    }

    @Override
    protected WebApplicationContext createServletApplicationContext() {
        AnnotationConfigWebApplicationContext cxt = new AnnotationConfigWebApplicationContext();
        return cxt;
    }
}
