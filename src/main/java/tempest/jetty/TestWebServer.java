package tempest.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

/**
 * Created by alexandre on 10/14/16.
 */
public class TestWebServer {

    private Server server;
    private int port;
    private Handler webApp;

    public TestWebServer(Handler webAppContext) {
        webApp = webAppContext;
    }

    public Server getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public void start() throws Exception {
        start(0);
    }

    public synchronized void start(int port) throws Exception {
        server = new Server(port);
        server.setHandler(webApp);
        server.start();
        this.port = ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    public synchronized void stop() throws Exception {
        if (server != null)
            server.stop();
    }
}
