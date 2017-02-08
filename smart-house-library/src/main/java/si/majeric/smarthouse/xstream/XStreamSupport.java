package si.majeric.smarthouse.xstream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import si.majeric.smarthouse.model.Address;
import si.majeric.smarthouse.model.Configuration;
import si.majeric.smarthouse.model.Floor;
import si.majeric.smarthouse.model.House;
import si.majeric.smarthouse.model.Room;
import si.majeric.smarthouse.model.Switch;
import si.majeric.smarthouse.model.TriggerConfig;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.enums.EnumConverter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public class XStreamSupport {
	private static final Logger logger = LoggerFactory.getLogger(XStreamSupport.class);

	static {
		try {
			Class.forName("com.thoughtworks.xstream.XStream");
		} catch (ClassNotFoundException e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T deserialize(String xml) {
		return (T) deserialize(new ByteArrayInputStream(xml.getBytes()));
	}

	@SuppressWarnings("unchecked")
	public <T> T deserializeResource(String res) {
		final InputStream is = this.getClass().getResourceAsStream(res);
		return (T) deserialize(is);
	}

	public <T> T deserialize(final InputStream is) {
		XStream xstream = new XStream();
		setUpXStream(xstream);
		final Object fromXML = xstream.fromXML(is);
		@SuppressWarnings("unchecked")
		final T mem = (T) fromXML;
		return mem;
	}

	/**
	 * By default we are compressing the XML (removing spaces and new lines) - (around 30% smaller files)
	 */
	public String serialize(Object object) {
		return serialize(object, false);
	}

	/**
	 *
	 * @param request
	 *            - object that hast to be converted to XML
	 * @param compress
	 *            - if the XML should be compressed (removed spaces and new lines)
	 * @return
	 */
	public String serialize(Object request, boolean compress) {
		XStream xstream = new XStream();
		setUpXStream(xstream);
		if (compress) {
			/* new stuff - removing spaces and new lines */
			StringWriter sw = new StringWriter();
			xstream.marshal(request, new CompactWriter(sw));
			return sw.toString();
		} else {
			/* Old stuff - without removing spaces and new lines */
			return xstream.toXML(request);
		}
	}

	private void setUpXStream(XStream xstream) {
		xstream.autodetectAnnotations(true);
		
		/* aliases */
		xstream.alias("linked-hash-set", LinkedHashSet.class);
		xstream.alias("house", House.class);
		xstream.alias("configuration", Configuration.class);
		xstream.alias("floor", Floor.class);
		xstream.alias("room", Room.class);
		xstream.alias("switch", Switch.class);
		xstream.alias("address", Address.class);
		xstream.alias("triggerConfig", TriggerConfig.class);

		/* converters */
		xstream.registerConverter(new CustomEnumConverter());
	}

	public static class CustomEnumConverter extends EnumConverter {
		@Override
		public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
			if (source != null) {
				super.marshal(source, writer, context);
			}
		}
	}

}