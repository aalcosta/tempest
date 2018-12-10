package tempest.jetty;

import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Starts the "JettyServer" in the local machine.
 * Could be used for integration tests or emulating the system (as a local container)
 *
 * Note: Can be customized by the System.Property "port"
 */
public class LocalRunner {

    public static void main(String[] args) throws Exception {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath("/");
        webAppContext.setDescriptor("src/main/resources/WEB-INF/web.xml");
        webAppContext.setResourceBase("src/main/resources/");
        webAppContext.setParentLoaderPriority(true);
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.setInitParameter("org.mortbay.jetty.servlet.Default.useFileMappedBuffer", "false");

        int port = Integer.parseInt(System.getProperty("port", "9090"));
        TestWebServer testServer = new TestWebServer(webAppContext);
        testServer.start(port);
        testServer.getServer().join();
    }

}
