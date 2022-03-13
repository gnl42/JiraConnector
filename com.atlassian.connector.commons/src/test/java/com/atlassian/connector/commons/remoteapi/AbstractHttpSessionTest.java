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

package com.atlassian.connector.commons.remoteapi;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.ServerType;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.atlassian.theplugin.commons.remoteapi.rest.AbstractHttpSession;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import junit.framework.TestCase;
import org.apache.commons.httpclient.HttpMethod;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.mortbay.jetty.Server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @author lguminski
 */
public class AbstractHttpSessionTest extends TestCase {
	private Server httpServer;
	private JettyMockServer mockServer;
	private static final String SOME_URL = "/some_url";

	@Override
	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

//		ConfigurationFactory.setConfiguration(new IdeaPluginConfigurationBean());
		mockServer = new JettyMockServer(httpServer);
	}

	public void testRetrieveGetResponseWithDataTransferTimeout()
			throws RemoteApiMalformedUrlException, IOException, RemoteApiSessionExpiredException, JDOMException {
		int timeout; // 7 sec
		long t1;
		final String mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort() + SOME_URL;
		mockServer.expect(SOME_URL, new TimeoutingOnDataTransferCallback());
		TestHttpSession session = new TestHttpSession(new com.atlassian.theplugin.commons.cfg.Server() {

			private final ServerIdImpl serverId = new ServerIdImpl();

			public ServerIdImpl getServerId() {
				return serverId;
			}

			public String getName() {
				return null;
			}

			public String getUrl() {
				return mockBaseUrl;
			}

			public String getUsername() {
				return null;
			}

			public String getPassword() {
				return null;
			}

			public ServerType getServerType() {
				return null;
			}

            public boolean isDontUseBasicAuth() {
                return false;
            }

            public boolean isUseSessionCookies() {
                return false;
            }

            public UserCfg getBasicHttpUser() {
                return null;  
            }

            public boolean isShared() {
                return false;
            }

            public void setShared(boolean global) {
            }

            public boolean isEnabled() {
				return true;
			}

			public boolean isUseDefaultCredentials() {
				return false;
			}
		}, new TestHttpSessionCallbackImpl());

		timeout = 100;
		TestHttpClientFactory.setDataTimeout(timeout);
		t1 = System.currentTimeMillis();
		try {
			session.retrieveGetResponse(mockBaseUrl);
			fail("It should fail but it didn't1");
		} catch (SocketTimeoutException e) {
			long diff = System.currentTimeMillis() - t1;
			if (diff > timeout * 20) { //too slow
				fail("Timeout doesn't work.");
			}
		}
	}

	private class TimeoutingOnDataTransferCallback implements JettyMockServer.Callback {
		public void onExpectedRequest(String s, HttpServletRequest request, HttpServletResponse response) throws Exception {
			response.setContentType("text/xml");
			response.getOutputStream().write("<a/>".getBytes("UTF-8"));
			response.getOutputStream().flush();
			Thread.sleep(10000);
		}
	}

	private class TestHttpSession extends AbstractHttpSession {

		@Override
		protected Document retrieveGetResponse(String urlString)
				throws IOException, JDOMException, RemoteApiSessionExpiredException {
			return super.retrieveGetResponse(urlString);
		}

		@Override
		protected Document retrievePostResponse(String urlString, Document request) throws IOException, JDOMException,
				RemoteApiException {
			return super.retrievePostResponse(urlString, request);
		}


		private TestHttpSession(final com.atlassian.theplugin.commons.cfg.Server server, final HttpSessionCallback callback)
				throws RemoteApiMalformedUrlException {
			super(getServerData(server), callback);
		}

		@Override
		protected void adjustHttpHeader(HttpMethod method) {

		}

		@Override
		protected void preprocessResult(Document doc) throws JDOMException, RemoteApiSessionExpiredException {

		}

        @Override
        protected void preprocessMethodResult(HttpMethod method) {            
        }

    }

	private ConnectionCfg getServerData(final com.atlassian.theplugin.commons.cfg.Server serverCfg) {
		return new ConnectionCfg(serverCfg.getServerId().getId(), serverCfg.getUrl(), serverCfg.getUsername(), serverCfg
				.getPassword());
	}
}



