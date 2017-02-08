package si.majeric;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import junit.framework.TestCase;
import si.majeric.smarthouse.xstream.XStreamSupport;

public class ModelReadTest extends TestCase {
	private static final String MODEL_FILE = "src/test/resources/smart-house-model.xml";

	public void testModel() throws FileNotFoundException {
		XStreamSupport xstream = new XStreamSupport();
		InputStream is = new FileInputStream(MODEL_FILE);
		final Object deserialized = xstream.deserialize(is);
		assertNotNull(deserialized);
	}
}
