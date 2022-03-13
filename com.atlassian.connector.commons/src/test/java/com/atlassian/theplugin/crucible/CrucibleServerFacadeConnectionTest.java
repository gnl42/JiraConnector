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

package com.atlassian.theplugin.crucible;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.crucible.CrucibleServerFacade2;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.crucible.CrucibleServerFacadeImpl;
import com.atlassian.theplugin.commons.crucible.api.CrucibleLoginException;
import com.atlassian.theplugin.commons.crucible.api.model.CrucibleUserCacheImpl;
import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginFailedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.LoginCallback;
import com.atlassian.theplugin.crucible.api.rest.cruciblemock.VersionInfoCallback;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import org.ddsteps.mock.httpserver.JettyMockServer;
import junit.framework.TestCase;

public class CrucibleServerFacadeConnectionTest extends TestCase {
	private static final String USER_NAME = "someUser";
	private static final String PASSWORD = "somePassword";

	private org.mortbay.jetty.Server httpServer;
	private JettyMockServer mockServer;
	private String mockBaseUrl;
	public static final String INVALID_PROJECT_KEY = "INVALID project key";
	private CrucibleServerFacade2 testedCrucibleServerFacade;
	private ConnectionCfg crucibleServerCfg;

	@Override
	protected void setUp() throws Exception {
		httpServer = new org.mortbay.jetty.Server(0);
		httpServer.start();

		mockBaseUrl = "http://localhost:" + httpServer.getConnectors()[0].getLocalPort();

		mockServer = new JettyMockServer(httpServer);
		crucibleServerCfg = createCrucibleTestConfiguration(mockBaseUrl, true);
		testedCrucibleServerFacade = new CrucibleServerFacadeImpl(LoggerImpl.getInstance(), new CrucibleUserCacheImpl(),
				new TestHttpSessionCallbackImpl());
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());
	}

	private static ConnectionCfg createCrucibleTestConfiguration(String serverUrl, boolean isPassInitialized) {
		return new ConnectionCfg("id", serverUrl, USER_NAME, isPassInitialized ? PASSWORD : "");
	}

	@Override
	protected void tearDown() throws Exception {
		mockServer = null;
		mockBaseUrl = null;
		httpServer.stop();

		testedCrucibleServerFacade = null;
	}

	public void testFailedLoginGetReviewsForFilter() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL));

		TestUtil.assertThrows(RemoteApiLoginFailedException.class, new IAction() {
			public void run() throws Throwable {
				testedCrucibleServerFacade.getReviewsForFilter(crucibleServerCfg, PredefinedFilter.Open);
			}
		});

		mockServer.verify();
	}

	public void testConnectionTestSucceed() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		testedCrucibleServerFacade.testServerConnection(crucibleServerCfg);
		mockServer.verify();
	}

	public void testConnectionTestFailed() throws Exception {
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(true));
		mockServer.expect("/rest-service/auth-v1/login", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/rest-service/reviews-v1/versionInfo", new VersionInfoCallback(false));

		try {
			testedCrucibleServerFacade.testServerConnection(crucibleServerCfg);
			fail();
		} catch (RemoteApiLoginFailedException e) {
			// expected
		} catch (CrucibleLoginException e) {
			// expected
		}

		mockServer.verify();
	}

	public void testConnectionTestFailedNullUser() throws Exception {
		try {
			ConnectionCfg server =
					new ConnectionCfg(crucibleServerCfg.getId(), crucibleServerCfg.getUrl(), null, crucibleServerCfg
							.getPassword());
			testedCrucibleServerFacade.testServerConnection(server);
			fail();
		} catch (RemoteApiLoginException e) {
			// expected
		}
	}


	public void testConnectionTestFailedNullPassword() throws Exception {
		try {
			ConnectionCfg server =
					new ConnectionCfg(crucibleServerCfg.getId(), crucibleServerCfg.getUrl(), crucibleServerCfg.getUsername(),
							null);
			testedCrucibleServerFacade.testServerConnection(server);
			fail();
		} catch (RemoteApiLoginException e) {
			// expected
		}
	}

	public void testConnectionTestFailedNullUrl() throws Exception {
		try {
			ConnectionCfg server =
					new ConnectionCfg(crucibleServerCfg.getId(), null, crucibleServerCfg.getUsername(), crucibleServerCfg
							.getPassword());
			testedCrucibleServerFacade.testServerConnection(server);
			fail();
		} catch (RemoteApiMalformedUrlException e) {
			// expected
		}
		mockServer.verify();
	}
}