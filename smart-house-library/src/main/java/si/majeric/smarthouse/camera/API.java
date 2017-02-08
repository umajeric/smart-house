package si.majeric.smarthouse.camera;

import com.thoughtworks.xstream.core.util.Base64Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Created by Uros Majeric on 26/10/15.
 * <p/>
 * <p/>
 * <p/>
 * Useful commands:
 * <p/>
 * SMTP attributes:
 * curl -X POST -u admin:duADM1n -H "Content-Type: application/x-www-form-urlencoded" 192.168.1.120:8080/web/cgi-bin/hi3510/param.cgi -d "cmd=getsmtpattr"
 * curl -X POST -u admin:duADM1n -H "Content-Type: application/x-www-form-urlencoded" 192.168.1.120:8080/web/cgi-bin/hi3510/param.cgi -d "cmd=setsmtpattr&-ma_to=uros@majeric.si;daniela.braunstein@gmail.com"
 * <p/>
 * TEST SMTP:
 * curl -X POST -u admin:duADM1n -H "Content-Type: application/x-www-form-urlencoded" 192.168.1.120:8080/web/cgi-bin/hi3510/param.cgi -d "cmd=testsmtp"
 * <p/>
 * SNAPSHOT:
 * curl -X POST -u admin:duADM1n -H "Content-Type: application/x-www-form-urlencoded" "http://192.168.1.120:8080/web/cgi-bin/hi3510/param.cgi" -d "cmd=snapimage&-chn=011"
 * http://192.168.1.120:8080/tmpfs/snap.jpg?usr=admin&pwd=duADM1n
 * <p/>
 * STREAM:
 * http://192.168.1.120:8080/iphone/11?admin:duADM1n&
 * http://admin:duADM1n@192.168.1.120:8080/web/cgi-bin/hi3510/mjpegstream.cgi?-chn=12
 */
public class API {
    static final Logger logger = LoggerFactory.getLogger(API.class);

    private static API INSTANCE;

    private Map<String, String> _config = new HashMap<String, String>();

    public API() {
    }

    public static API instance() {
        if (INSTANCE == null) {
            INSTANCE = new API();
        }
        return INSTANCE;
    }

    public API init(Map<String, String> config) {
        if (config != null) {
            _config.putAll(config);
        }
        return this;
    }

    public void turnOnAlarm() {
//        doPost("cmd=setplanrecattr&recswitch=&recstream=&planrec_time=&cmd=setscheduleex&-ename=md&-week0=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week1=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week2=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week3=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week4=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week5=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP&-week6=PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP");
    }

    public void turnOffAlarm() {
//        doPost("cmd=setplanrecattr&recswitch=&recstream=&planrec_time=&cmd=setscheduleex&-ename=md&-week0=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week1=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week2=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week3=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week4=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week5=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP&-week6=PPPPPPPPPPPPPPNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNPPPP");
    }

    public void sendTestEmail() {
        doPost("cmd=testsmtp");
    }

    public void snapImage() {
        doPost("cmd=snapimage&-chn=011");
    }

    public void saveSnapImage(String outputImagePath) throws IOException {
        // optimized version of saving image below
        //try (InputStream in = new URL(urlString).openStream()) {
        //    Files.copy(in, Paths.get(outputImagePath), StandardCopyOption.REPLACE_EXISTING);
        //}

        final byte[] snapImage = this.getSnapImage();

        FileOutputStream fos = new FileOutputStream(outputImagePath);
        fos.write(snapImage);
        fos.close();
    }

    public byte[] getSnapImage() throws IOException {
        String urlString = _config.get("host") + "/tmpfs/snap.jpg?usr=" + _config.get("username") + "&pwd=" + _config.get("password");

        URL url = new URL(urlString);
        InputStream in = new BufferedInputStream(url.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while (-1 != (n = in.read(buf))) {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        byte[] response = out.toByteArray();

        return response;
    }

    private void doPost(String rawData) {
        if (!_config.containsKey("host") || !_config.containsKey("path") || !_config.containsKey("username") || !_config.containsKey("password")) {
            logger.error("Missing host, path, username or password configuration (current: {})", _config.keySet());
            return;
        }
        HttpURLConnection conn = null;
        OutputStream os = null;
        try {
            URL url = new URL(_config.get("host") + _config.get("path"));
            String username = _config.get("username");
            String password = _config.get("password");
            String encodedData = rawData;
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            String type = "application/x-www-form-urlencoded";
            conn.setRequestProperty("Content-Type", type);
            // conn.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));

            String encodedBytes = new Base64Encoder().encode((username + ":" + password).getBytes());
            username = "Basic " + encodedBytes;
            conn.setRequestProperty("Authorization", username);

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(encodedData);
            wr.flush();
            wr.close();
            // os = conn.getOutputStream();
            // os.write(encodedData.getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuffer input = new StringBuffer();
            String in;
            while ((in = br.readLine()) != null) {
                input.append(in);
            }
            br.close();
            logger.info(input.toString());
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
        }
    }
}
