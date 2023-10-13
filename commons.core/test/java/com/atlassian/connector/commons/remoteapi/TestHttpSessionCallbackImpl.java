package me.glindholm.connector.commons.remoteapi;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholmtheplugin.commons.exception.HttpProxySettingsException;
import me.glindholmtheplugin.commons.remoteapi.rest.HttpSessionCallbackImpl;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.params.HttpMethodParams;

/**
 * @author Wojciech Seliga
 */
public class TestHttpSessionCallbackImpl extends HttpSessionCallbackImpl {

	public HttpClient getHttpClient(final ConnectionCfg server) throws HttpProxySettingsException {
		final HttpClient client = TestHttpClientFactory.getClient();
		client.getParams().setParameter(HttpMethodParams.USER_AGENT, "JiraConnector Commons test agent");
		return client;
	}

    public void disposeClient(ConnectionCfg server) {        
    }

    public Cookie[] getCookiesHeaders(ConnectionCfg server) {
        return new Cookie[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
