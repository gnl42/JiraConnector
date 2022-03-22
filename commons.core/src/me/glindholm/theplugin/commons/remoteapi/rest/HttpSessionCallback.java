package me.glindholm.theplugin.commons.remoteapi.rest;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;

import me.glindholm.connector.commons.api.ConnectionCfg;
import me.glindholm.theplugin.commons.exception.HttpProxySettingsException;

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
