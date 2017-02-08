package si.majeric.smarthouse.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.majeric.smarthouse.Environment;
import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.dao.DBSHDaoFactory;
import si.majeric.smarthouse.pi.tools.Import;
import si.majeric.smarthouse.xstream.dao.SmartHouseConfigReadError;

/**
 * Created by Uros Majeric on 23/12/16.
 */
public class SmartHouseImportServlet extends AbstractHttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SmartHouseImportServlet.class);

    public SmartHouseImportServlet(SmartHouse smartHouse) {
        super(smartHouse);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!authorized(request, response)) return;

        logger.info("Import started");
        String configModelFile = Environment.getConfigModelFile();
        String fileName = request.getParameter("fileName");
        if (fileName != null) {
            configModelFile = fileName;
        }
        logger.info("Importing {}", configModelFile);
        try {
            DBSHDaoFactory dbDaoFactory = new DBSHDaoFactory();
            new Import(dbDaoFactory).doImport(configModelFile);
            _smartHouse.init();
        } catch (SmartHouseConfigReadError e) {
            response.getWriter().println(e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.getWriter().println("Import succeeded!");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
