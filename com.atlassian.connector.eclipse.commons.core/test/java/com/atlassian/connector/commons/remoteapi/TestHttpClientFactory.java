package com.atlassian.connector.commons.remoteapi;

import com.atlassian.theplugin.commons.exception.HttpProxySettingsException;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * @author Wojciech Seliga
 */
public final class TestHttpClientFactory {
	private static MultiThreadedHttpConnectionManager connectionManager;

	private static final int CONNECTION_MANAGER_TIMEOUT = 80000;
	private static final int CONNECTION_TIMEOUT = 30000;
	private static final int DATA_TIMOUT = 10000;

	private static final int TOTAL_MAX_CONNECTIONS = 50;

	private static final int DEFAULT_MAX_CONNECTIONS_PER_HOST = 3;
	private static int dataTimeout = DATA_TIMOUT;
	private static int connectionTimeout = CONNECTION_TIMEOUT;
	private static int connectionManagerTimeout = CONNECTION_MANAGER_TIMEOUT;

	static {
		connectionManager =	new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setConnectionTimeout(getConnectionTimeout());
		connectionManager.getParams().setMaxTotalConnections(TOTAL_MAX_CONNECTIONS);
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(DEFAULT_MAX_CONNECTIONS_PER_HOST);
	}

	///CLOVER:OFF
	private TestHttpClientFactory() {

	}


	public static void setDataTimeout(int dataTimeout) {
		TestHttpClientFactory.dataTimeout = dataTimeout;
	}

	protected static void setConnectionTimeout(int connectionTimeout) {
		TestHttpClientFactory.connectionTimeout = connectionTimeout;
	}

	protected static void setConnectionManagerTimeout(int connectionManagerTimeout) {
		TestHttpClientFactory.connectionManagerTimeout = connectionManagerTimeout;
	}
	///CLOVER:ON

	public static HttpClient getClient() throws HttpProxySettingsException {
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setConnectionManagerTimeout(getConnectionManagerTimeout());
		httpClient.getParams().setSoTimeout(getDataTimeout());
		return httpClient;
	}


	private static int getConnectionManagerTimeout() {
		return connectionManagerTimeout;
	}

	private static int getDataTimeout() {
		return dataTimeout;
	}

	private static int getConnectionTimeout() {
		return connectionTimeout;
	}


	public static MultiThreadedHttpConnectionManager getConnectionManager() {
		return connectionManager;
	}

}

