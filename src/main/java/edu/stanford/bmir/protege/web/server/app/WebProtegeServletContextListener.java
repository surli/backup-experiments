package edu.stanford.bmir.protege.web.server.app;

import edu.stanford.bmir.protege.web.server.filter.WebProtegeWebAppFilter;
import edu.stanford.bmir.protege.web.server.init.WebProtegeConfigurationException;
import edu.stanford.bmir.protege.web.server.inject.ApplicationComponent;
import edu.stanford.bmir.protege.web.server.inject.DaggerApplicationComponent;
import edu.stanford.bmir.protege.web.server.inject.ServletComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger.WebProtegeMarker;

public class WebProtegeServletContextListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(WebProtegeServletContextListener.class);

    public WebProtegeServletContextListener() {
    }

    public void contextInitialized(ServletContextEvent sce) {
        logger.info(WebProtegeMarker, "Initializing WebProtege");
        try {
            ApplicationComponent applicationComponent = DaggerApplicationComponent.create();
            ServletContext servletContext = sce.getServletContext();

            servletContext.setAttribute(ApplicationComponent.class.getName(), applicationComponent);

            ServletComponent servletComponent = applicationComponent.getServletComponent();

            servletContext.addServlet("DispatchService", servletComponent.getDispatchService())
                          .addMapping("/webprotege/dispatchservice");

            servletContext.addServlet("OntologyServiceImpl", servletComponent.getOntologyService())
                          .addMapping("/webprotege/ontology");

            servletContext.addServlet("OBOTextEditorService", servletComponent.getOBOTextEditorService())
                          .addMapping("/webprotege/obotexteditorservice");

            servletContext.addServlet("ProjectDownloadServlet", servletComponent.getFileDownloadServlet())
                          .addMapping("/download");

            servletContext.addServlet("FileUploadServlet", servletComponent.getFileUploadServlet())
                          .addMapping("/webprotege/submitfile");

            servletContext.addListener(applicationComponent.getSessionListener());

            applicationComponent.getWebProtegeConfigurationChecker().performConfiguration(servletContext);

            Runtime runtime = Runtime.getRuntime();
            logger.info("Max  Memory: {} MB", (runtime.maxMemory() / (1024 * 1024)));
            logger.info(WebProtegeMarker, "Initialized WebProtege");
        }
        catch (WebProtegeConfigurationException e) {
            logger.error(WebProtegeMarker, "Encountered a configuration error during initialization: {}", e.getMessage(), e);
            WebProtegeWebAppFilter.setConfigError(e);
        } catch (Throwable error) {
            logger.error(WebProtegeMarker, "Encountered an error during initialization: {}", error.getMessage(), error);
            WebProtegeWebAppFilter.setError(error);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info(WebProtegeMarker, "WebProtege Destroyed");
    }
}
