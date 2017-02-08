package si.majeric.smarthouse;

import com.amazon.speech.speechlet.servlet.SpeechletServlet;

import java.net.BindException;
import java.util.Map;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.dao.DBSHDaoFactory;
import si.majeric.smarthouse.http.LightsSpeechlet;
import si.majeric.smarthouse.http.SmartHouseHttpServlet;
import si.majeric.smarthouse.http.SmartHouseImportServlet;
import si.majeric.smarthouse.pi.PiSmartHouse;

/**
 * @author Uros Majeric
 */
public class SmartHouseServer {
    private static final Logger logger = LoggerFactory.getLogger(SmartHouseServer.class);
    private static DBSHDaoFactory _persistenceDaoFactory = new DBSHDaoFactory();

    public static void main(String[] args) throws Exception {
        try {
            logger.info("Starting server - Version {} ...", JarUtils.getImplementationVersion());

            Server server = new Server((ThreadPool) null);

            final int securePort = Environment.getBindPort() + 1;
            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setSecureScheme("https");
            http_config.setSecurePort(securePort);

            ServerConnector http = new ServerConnector(server, new HttpConnectionFactory(http_config));
            http.setHost(Environment.getBindAddress());
            http.setPort(Environment.getBindPort());
            http.setIdleTimeout(30000);
            server.addConnector(http);

            final Map<String, String> sslConf = Environment.getPropertyMap("ssl");
            // if ssl is enabled add connector for it
            if (Boolean.valueOf(sslConf.get("enabled"))) {

                // SSL Context Factory
                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStorePath(sslConf.get("keyStorePath"));
                sslContextFactory.setKeyStorePassword(sslConf.get("keyStorePassword"));
                sslContextFactory.setKeyManagerPassword(sslConf.get("keyManagerPassword"));
                sslContextFactory.setTrustStorePath(sslConf.get("trustStorePath"));
                sslContextFactory.setTrustStorePassword(sslConf.get("trustStorePassword"));
                sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                        "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                        "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                        "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                        "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

                // SSL HTTP Configuration
                HttpConfiguration https_config = new HttpConfiguration(http_config);
                https_config.addCustomizer(new SecureRequestCustomizer());

                // SSL Connector
                final SslConnectionFactory sslConnFactory = new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString());
                ServerConnector sslConnector = new ServerConnector(server, sslConnFactory, new HttpConnectionFactory(https_config));
                sslConnector.setPort(securePort);
                server.addConnector(sslConnector);

                logger.info("SSL running on {}:{}", Environment.getBindAddress(), securePort);
            }

            logger.info("Server running on {}:{}", Environment.getBindAddress(), Environment.getBindPort());

            final SmartHouse smartHouse = new PiSmartHouse(_persistenceDaoFactory);
            smartHouse.init();

            ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath("/");
            server.setHandler(context);

            SmartHouseHttpServlet shServlet = new SmartHouseHttpServlet(smartHouse);
            context.addServlet(new ServletHolder(shServlet), "/");

            SmartHouseImportServlet shImportServlet = new SmartHouseImportServlet(smartHouse);
            context.addServlet(new ServletHolder(shImportServlet), "/import");

            SpeechletServlet servlet = new SpeechletServlet();
            servlet.setSpeechlet(new LightsSpeechlet(smartHouse));
            context.addServlet(new ServletHolder(servlet), "/alexa");

            server.start();
            server.join();
        } catch (BindException e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

}