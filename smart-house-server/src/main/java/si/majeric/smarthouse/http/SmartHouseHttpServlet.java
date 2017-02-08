package si.majeric.smarthouse.http;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import si.majeric.smarthouse.RequestHandler;
import si.majeric.smarthouse.SmartHouse;
import si.majeric.smarthouse.tpt.Response;
import si.majeric.smarthouse.xstream.XStreamSupport;

/**
 * Created by Uros Majeric on 23/12/16.
 */
public class SmartHouseHttpServlet extends AbstractHttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SmartHouseHttpServlet.class);
    private RequestHandler _requestHandler;

    public SmartHouseHttpServlet(SmartHouse smartHouse) {
        super(smartHouse);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!authorized(request, response)) return;

        response.setStatus(HttpServletResponse.SC_OK);
        handleXStreamObjectRequest(request, response.getOutputStream());
    }

    protected void handleXStreamObjectRequest(HttpServletRequest request, ServletOutputStream outStream) throws IOException {
        final InputStream inputStream = request.getInputStream();
        StringBuffer strReq = getStringContent(inputStream);
        // logger.info(strReq.toString());

        final Response response;
        if (strReq == null || strReq.toString().trim().isEmpty()) {
            response = new Response();
            response.getStatus().setSucceeded(false);
            response.getStatus().setMessage("No request or empty request");
        } else {
            response = process(strReq.toString());
        }

        String servletResponse;
        final String accept = request.getHeader("Accept");
        if ("application/json".equals(accept)) {
            JSONObject jsonObject = new JSONObject(response);
            servletResponse = jsonObject.toString();
        } else {
            XStreamSupport xs = new XStreamSupport();
            servletResponse = xs.serialize(response);
        }

        // logger.info(servletResponse);
        Writer writer = new OutputStreamWriter(outStream, "UTF-8");
        writer.write(servletResponse);
        writer.flush();
    }

    private Response process(String xml) {
        // PHP webapp send = sign before actual xml. It must be removed.
        if (xml.startsWith("="))
            xml = xml.substring(1);
        // PHP webapp encodes the request. Must perform decode.
        try {
            xml = URLDecoder.decode(xml, "UTF-8");
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        // System.out.println(xml);
        Response response;
        try {
            XStreamSupport xs = new XStreamSupport();
            si.majeric.smarthouse.tpt.Request request = (si.majeric.smarthouse.tpt.Request) xs.deserialize(xml);
            response = getRequestHandler().handleRequest(request);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
            response = new Response();
            response.getStatus().setSucceeded(false);
            response.getStatus().setMessage(e.getMessage());
        }
        return response;
    }

    private StringBuffer getStringContent(final InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String message = null;
        StringBuffer strRes = new StringBuffer();
        while ((message = reader.readLine()) != null) {
            strRes.append(message);
        }
        return strRes;
    }

    private RequestHandler getRequestHandler() {
        if (_requestHandler == null) {
            _requestHandler = new RequestHandler(_smartHouse);
        }
        return _requestHandler;
    }

}
