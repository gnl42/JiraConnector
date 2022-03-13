/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.commons.bamboo.api;

import com.atlassian.theplugin.bamboo.api.bamboomock.LoginCallback;
import com.atlassian.theplugin.bamboo.api.bamboomock.LogoutCallback;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfiguration;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.thirdparty.apache.EasySSLProtocolSocketFactory;
import junit.framework.TestCase;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.security.SslSocketConnector;

public class BambooSessionSslTest extends TestCase {
	private static final String USER_NAME = "someSslUser";
	private static final String PASSWORD = "SomeSslPass";


	private Server server;
	private JettyMockServer mockServer;
	private String mockBaseUrl;

	@Override
	protected void setUp() throws Exception {
		Protocol.registerProtocol("https", new Protocol(
				"https", (ProtocolSocketFactory) new EasySSLProtocolSocketFactory(),
				EasySSLProtocolSocketFactory.SSL_PORT));
		PluginConfiguration configuration = new PluginConfigurationBean();
		ConfigurationFactory.setConfiguration(configuration);


		String keystoreLocation = getClass().getResource("/mock/selfSigned.keystore").toExternalForm();
		SslSocketConnector sslConnector = new SslSocketConnector();

		sslConnector.setPort(0);
		sslConnector.setKeystore(keystoreLocation);
		sslConnector.setPassword("password");
		sslConnector.setKeyPassword("password");

		server = new Server();

		server.addConnector(sslConnector);
		server.start();

		mockBaseUrl = "https://localhost:" + sslConnector.getLocalPort();

		mockServer = new JettyMockServer(server);
	}

	@Override
	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		//server.stop();
	}

	public void testSuccessBambooLoginOnSSL() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

		BambooSession apiHandler = BambooSessionTest.createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		assertTrue(apiHandler.isLoggedIn());
		apiHandler.logout();
		assertFalse(apiHandler.isLoggedIn());

		mockServer.verify();
	}

}
