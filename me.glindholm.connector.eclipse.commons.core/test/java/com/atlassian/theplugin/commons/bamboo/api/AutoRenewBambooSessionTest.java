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

package me.glindholm.theplugin.commons.bamboo.api;

import me.glindholm.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooBuildInfo;
import com.atlassian.theplugin.commons.bamboo.BambooChangeSet;
import com.atlassian.theplugin.commons.bamboo.BambooJob;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildDetails;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.bamboo.TestDetails;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiLoginException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiSessionExpiredException;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;
import org.easymock.EasyMock;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"ThrowableInstanceNeverThrown"})
public class AutoRenewBambooSessionTest extends TestCase {
	private BambooSession testedSession;
	private BambooSession mockDelegate;
	private static final String LOGIN = "login";
	private static final char[] A_PASSWORD = "password".toCharArray();
	private static final int BUILD_NUMBER = 1;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		mockDelegate = EasyMock.createStrictMock(BambooSession.class);
		testedSession = new AutoRenewBambooSession(mockDelegate);
		// new BambooServerCfg("mockbamboo", "http://whatever", new ServerId()),
//				new HttpSessionCallbackImpl());

	}

	public void testLogin() throws Exception {
		mockDelegate.login(LOGIN, A_PASSWORD);
		EasyMock.expectLastCall().andThrow(new RemoteApiLoginException(""));
		EasyMock.replay(mockDelegate);

		try {
			testedSession.login(LOGIN, A_PASSWORD);
			fail();
		} catch (RemoteApiLoginException e) {
			//expected
		}

		EasyMock.verify(mockDelegate);
	}

	public void testLogout() throws Exception {
		mockDelegate.login(LOGIN, A_PASSWORD);
		EasyMock.expectLastCall();
		mockDelegate.logout();
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.logout();

		EasyMock.verify(mockDelegate);
	}

	public void testListProjectNames() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listProjectNames();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.listProjectNames();
		EasyMock.expectLastCall().andReturn(Arrays.asList(new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}, new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}, new BambooProject() {
			public String getProjectName() {
				return "project1";
			}

			public String getProjectKey() {
				return "key1";
			}
		}));
		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		List<BambooProject> projects = testedSession.listProjectNames();

		assertNotNull(projects);
		assertEquals(3, projects.size());

		EasyMock.verify(mockDelegate);
	}

	public void testListPlanNames() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getPlanList();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getPlanList();
		final BambooPlan bp1 = new BambooPlan("planName1", "planKey", null, false, false);
		final BambooPlan bp2 = new BambooPlan("planName2", "planKey2", null, false, false);
		EasyMock.expectLastCall().andReturn(Arrays.asList(bp1, bp2));

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		Collection<BambooPlan> plans = testedSession.getPlanList();
		assertNotNull(plans);
		TestUtil.assertHasOnlyElements(plans, bp1, bp2);

		EasyMock.verify(mockDelegate);
	}

	public void testGetLatestBuildForPlan() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getLatestBuildForPlan("planKey", 0);
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		EasyMock.expect(mockDelegate.getLatestBuildForPlan("planKey", 0)).andReturn(
				new BambooBuildInfo.Builder("planKey", null, new ConnectionCfg("mybamboo", "", "", ""), null, 123,
						BuildStatus.SUCCESS)
						.build());


		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		BambooBuild build = testedSession.getLatestBuildForPlan("planKey", 0);
		assertNotNull(build);

		EasyMock.verify(mockDelegate);
	}

	public void testGetFavouriteUserPlans() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getFavouriteUserPlans();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getFavouriteUserPlans();
		EasyMock.expectLastCall().andReturn(Arrays.asList("plan1", "plan2", "plan3"));

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		List<String> plans = testedSession.getFavouriteUserPlans();
		assertNotNull(plans);
		assertEquals(3, plans.size());

		EasyMock.verify(mockDelegate);

	}

	public void testGetBuildResultDetails() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBuildResultDetails("buildKey", BUILD_NUMBER);
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBuildResultDetails("buildKey", BUILD_NUMBER);
		EasyMock.expectLastCall().andReturn(new BuildDetails() {

			public String getVcsRevisionKey() {
				return null;
			}

			public List<TestDetails> getSuccessfulTestDetails() {
				return null;
			}

			public List<TestDetails> getFailedTestDetails() {
				return null;
			}

			public List<BambooChangeSet> getCommitInfo() {
				return null;
			}

			public List<BambooJob> getJobs() {
				return null;
			}

			public List<BambooJob> getEnabledJobs() {
				// ignore
				return null;
			}
		});

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		BuildDetails build = testedSession.getBuildResultDetails("buildKey", BUILD_NUMBER);
		assertNotNull(build);

		EasyMock.verify(mockDelegate);
	}

	public void testAddLabelToBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addLabelToBuild("buildKey", BUILD_NUMBER, "label");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addLabelToBuild("buildKey", BUILD_NUMBER, "label");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.addLabelToBuild("buildKey", BUILD_NUMBER, "label");

		EasyMock.verify(mockDelegate);
	}

	public void testAddCommentToBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addCommentToBuild("buildKey", BUILD_NUMBER, "comment");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.addCommentToBuild("buildKey", BUILD_NUMBER, "comment");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.addCommentToBuild("buildKey", BUILD_NUMBER, "comment");

		EasyMock.verify(mockDelegate);
	}

	public void testExecuteBuild() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.executeBuild("buildKey");
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.executeBuild("buildKey");
		EasyMock.expectLastCall();

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.executeBuild("buildKey");

		EasyMock.verify(mockDelegate);
	}

	public void testGetBambooBuildNumber() throws Exception {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBamboBuildNumber();
		EasyMock.expectLastCall().andThrow(new RemoteApiSessionExpiredException(""));
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.getBamboBuildNumber();
		EasyMock.expectLastCall().andReturn(770);

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		testedSession.getBamboBuildNumber();

		EasyMock.verify(mockDelegate);
	}


	public void testIsLoggedIn() throws RemoteApiLoginException {
		mockDelegate.login(EasyMock.eq("login"), EasyMock.isA(char[].class));
		EasyMock.expectLastCall();
		mockDelegate.isLoggedIn();
		EasyMock.expectLastCall().andReturn(true);
		mockDelegate.logout();
		mockDelegate.isLoggedIn();
		EasyMock.expectLastCall().andReturn(false);

		EasyMock.replay(mockDelegate);

		testedSession.login(LOGIN, A_PASSWORD);
		assertTrue(testedSession.isLoggedIn());
		testedSession.logout();
		assertFalse(testedSession.isLoggedIn());

		EasyMock.verify(mockDelegate);
	}

	public List<BambooJob> getEnabledJobs() {
		return null;
	}

}
