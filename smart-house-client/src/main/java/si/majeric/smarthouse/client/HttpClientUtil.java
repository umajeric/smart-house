package si.majeric.smarthouse.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Calendar;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import si.majeric.smarthouse.tpt.Request;
import si.majeric.smarthouse.tpt.Response;
import si.majeric.smarthouse.xstream.XStreamSupport;

/**
 *
 * @author uros (Oct 11, 2013)
 *
 */
public class HttpClientUtil {
	public static final Integer CONNECTION_TIMEOUT_MS = 10000;

	public Object sendRequest(URI uri, Request request, String username, String password) throws Exception {
		final HttpRequestBase httpRequest = prepareRequest(uri, request, username, password);
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTION_TIMEOUT_MS);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTION_TIMEOUT_MS);

		final HttpResponse response = client.execute(httpRequest);
		return handleResponse(response);
	}

	private Object handleResponse(HttpResponse httpResponse) throws ParseException, IOException {
		HttpEntity entity = httpResponse.getEntity();
		String responseString = EntityUtils.toString(entity, "utf-8");

		if (httpResponse.getStatusLine().getStatusCode() == 200) {

			XStreamSupport xs = new XStreamSupport();
			final Response response = xs.deserialize(responseString);

			final boolean succeeded = response.getStatus().isSucceeded();
			if (!succeeded) {
				final String message = response.getStatus().getMessage();
				throw new RuntimeException(message);
			}
			return response.getObject();
		}
		throw new RuntimeException(httpResponse.getStatusLine().toString());
	}

	protected HttpRequestBase prepareRequest(URI url, Object object, String username, String password) throws UnsupportedEncodingException {
		HttpPost post = new HttpPost(url);
		if (username != null) {
			if (password == null) {
				password = "";
			}
			// additional protection - each hour of day different password (client and server have to have time synchronized)
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			String basic = new String(Base64.encodeBase64((username + ":" + password + hour).getBytes("UTF-8"), false));
			post.addHeader("Authorization", "Basic " + basic);
		}
		/* convert the request object to a string that will be sent in the post */
		String xml = new XStreamSupport().serialize(object);
		HttpEntity entity = new StringEntity(xml, "UTF-8");
		post.setEntity(entity);
		return post;
	}
}