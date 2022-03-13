package com.atlassian.theplugin.commons.remoteapi.rest;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Interface for the callback used by AbstractHttpSession for HttpClient setup
 *
 * @author Shawn Minto
 */
public interface HttpSessionCallback {

	HttpClient getHttpClient(ConnectionCfg server) throws HttpProxySettingsException;

	void configureHttpMethod(AbstractHttpSession session, HttpMethod method);

    void disposeClient(ConnectionCfg server);

    Cookie[] getCookiesHeaders(ConnectionCfg server);
}
