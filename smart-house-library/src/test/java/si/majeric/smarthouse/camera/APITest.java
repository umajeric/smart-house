package si.majeric.smarthouse.camera;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Uros Majeric
 */
public class APITest extends TestCase {
    static final Logger logger = LoggerFactory.getLogger(APITest.class);
    static final Map<String, String> config = new HashMap<String, String>() {
        {
            put("host", "http://192.168.1.120:8080");
            put("path", "/web/cgi-bin/hi3510/param.cgi");
            put("username", "admin");
            put("password", "duADM1n");
        }
    };

    API cameraAPI;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cameraAPI = API.instance().init(config);
    }

    public void testTrigger() throws IOException {
        cameraAPI.sendTestEmail();
    }

    public void testSaveSnapImage() throws IOException {
        cameraAPI.saveSnapImage("/tmp/image1.jpg");
    }

}
