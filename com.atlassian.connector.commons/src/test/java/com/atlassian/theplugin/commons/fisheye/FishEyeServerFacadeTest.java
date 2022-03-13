package com.atlassian.theplugin.commons.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.fisheye.FishEyeServerFacade2;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.commons.cfg.FishEyeServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.configuration.ConfigurationFactory;
import com.atlassian.theplugin.commons.configuration.PluginConfigurationBean;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.crucible.api.rest.CharArrayEquals;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Arrays;
import java.util.Collection;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.replay;

/**
 * User: pmaruszak
 */
public class FishEyeServerFacadeTest extends TestCase {

	private static final String USER_NAME = "myname";
	private static final String PASSWORD = "mypassword";
	private final String URL = "http://localhost:9001";
	private FishEyeSession fishEyeSessionMock;
	private FishEyeServerFacadeImpl facade;


	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ConfigurationFactory.setConfiguration(new PluginConfigurationBean());

		fishEyeSessionMock = createMock(FishEyeSession.class);

		facade = new FishEyeServerFacadeImpl(new TestHttpSessionCallbackImpl()) {

			@Override
			public FishEyeSession getSession(ConnectionCfg server)
					throws RemoteApiMalformedUrlException {
				return fishEyeSessionMock;
			}
		};
	}


	public static Throwable eqException(char[] in) {
		EasyMock.reportMatcher(new CharArrayEquals(in));
		return null;
	}

	public static char[] charArrayContains(char[] expectedCharArray) {
		EasyMock.reportMatcher(new CharArrayEquals(expectedCharArray));
		return null;
	}

	public void testGetRepositories() throws ServerPasswordNotProvidedException, RemoteApiException {
		final ConnectionCfg server = new ConnectionCfg("id", URL, USER_NAME, PASSWORD);

		fishEyeSessionMock.login(EasyMock.eq(server.getUsername()), charArrayContains(PASSWORD.toCharArray()));


		fishEyeSessionMock.getRepositories();
		EasyMock.expectLastCall().andReturn(Arrays.asList(prepareRepositoryData(0), prepareRepositoryData(1)));
		fishEyeSessionMock.logout();

		replay(fishEyeSessionMock);

		// test call
		Collection<String> ret = facade.getRepositories(server);
		assertEquals(2, ret.size());

		int i = 0;
		for (String repoName : ret) {
			assertEquals("RepoName" + i++, repoName);
		}
		EasyMock.verify(fishEyeSessionMock);
	}


	private String prepareRepositoryData(final int i) {
		return "RepoName" + i;

	}

	// Regression for https://studio.atlassian.com/browse/ACC-40
	public void testConnectionTestInvalidUrlIncludesPassword() throws Exception {
		try {
			FishEyeServerFacade2 facade = new FishEyeServerFacadeImpl(new TestHttpSessionCallbackImpl());
            FishEyeServerCfg fishCfg = new FishEyeServerCfg(true, "fes", new ServerIdImpl());
            fishCfg.setUrl("http://invalid url");
            ServerData.Builder builder = new ServerData.Builder(fishCfg);
            builder.defaultUser(new UserCfg(USER_NAME, PASSWORD));
            builder.useDefaultUser(false);
			facade.testServerConnection(builder.build());
			fail("Should throw RemoteApiLoginException");
		} catch (RemoteApiException e) {
			assertFalse("Message should not include users's password", e.getMessage().contains(PASSWORD));
		}
	}

}
