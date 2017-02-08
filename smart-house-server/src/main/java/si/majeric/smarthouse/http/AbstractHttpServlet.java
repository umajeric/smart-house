package si.majeric.smarthouse.http;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.SmartHouse;

/**
 * Created by Uros Majeric on 23/12/16.
 */
abstract class AbstractHttpServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpServlet.class);
    protected final SmartHouse _smartHouse;

    protected AbstractHttpServlet(SmartHouse smartHouse) {
        _smartHouse = smartHouse;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        request.setHandled(true);
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().println(//
                "<html>"//
                        + "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=UTF-8\">" //
                        + "<body style=\"text-align: center;\">" //
                        + "<h1>Smart House</h1>" //
                        + "<h2>© majeric.si</h2>" //
                        + "</body></html>");
    }

    protected boolean authorized(HttpServletRequest request, HttpServletResponse response) throws IOException {
        /* validate the request */
        String username = Environment.getUsername();
        if (username != null) {
            // validate if username equals credentials username and password
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            String password = Environment.getPassword() + hour;

            String authHeader = request.getHeader("Authorization");
            Credentials credentials = getCredentials(authHeader);
            // unauthorized if credentials not provided or not correct
            if (credentials == null || !username.equals(credentials.username)
                        || (password != null && !password.equals(credentials.password))) {
                logger.error("Unauthorized access: " + request.getRemoteAddr() + " (" + credentials + ")");
                response.getWriter().println("Wrong username or password.");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return false;
            }
        }
        return true;
    }

    Credentials getCredentials(String authorization) {
        Credentials credentials = null;
        if (authorization != null && authorization.startsWith("Basic")) {
            // Authorization: Basic base64credentials
            String base64Credentials = authorization.substring("Basic".length()).trim();
            String creds = new String(Base64.decodeBase64(base64Credentials), Charset.forName("UTF-8"));
            // creds = username:password
            final String[] values = creds.split(":", 2);

            if (values.length > 0) {
                credentials = new Credentials();
                credentials.username = values[0];
                if (values.length > 1) {
                    credentials.password = values[1];
                }
            }
        }
        return credentials;
    }

    static class Credentials {
        String username;
        String password;

        @Override
        public String toString() {
            return Credentials.class.getSimpleName() + "(" + username + ":" + password + ")";
        }
    }
}
