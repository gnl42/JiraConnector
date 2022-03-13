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

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.connector.commons.remoteapi.TestHttpSessionCallbackImpl;
import com.atlassian.theplugin.api.AbstractSessionTest;
import com.atlassian.theplugin.bamboo.api.bamboomock.*;
import com.atlassian.theplugin.commons.BambooFileInfo;
import com.atlassian.theplugin.commons.bamboo.*;
import com.atlassian.theplugin.commons.remoteapi.ProductSession;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiMalformedUrlException;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.spartez.util.junit3.IAction;
import com.spartez.util.junit3.TestUtil;
import junit.framework.Assert;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jetbrains.annotations.Nullable;
import java.time.DateTime;
import java.time.DateTimeZone;
import java.time.format.ISODateTimeFormat;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.List;


/**
 * Test case for {#link BambooSessionImpl}
 */
public class BambooSessionTest extends AbstractSessionTest {
    public void testSuccessBambooLogin() throws Exception {

        BambooSession apiHandler = createBambooSession(mockBaseUrl);

        String[] usernames = {"user", "+-=&;<>", "", "a;&username=other", "!@#$%^&*()_-+=T "};
        String[] passwords = {"password", "+-=&;<>", "", "&password=other", ",./';[]\t\\ |}{\":><?"};

        for (int i = 0; i < usernames.length; ++i) {
            mockServer.expect("/api/rest/login.action", new LoginCallback(usernames[i], passwords[i]));
            mockServer.expect("/api/rest/logout.action", new LogoutCallback());

            apiHandler.login(usernames[i], passwords[i].toCharArray());
            assertTrue(apiHandler.isLoggedIn());
            apiHandler.logout();
            assertFalse(apiHandler.isLoggedIn());
        }

        mockServer.verify();
    }

    public void testSuccessBambooLoginURLWithSlash() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback(LoginCallback.AUTH_TOKEN));

        BambooSession apiHandler = createBambooSession(mockBaseUrl + "/");
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        assertTrue(apiHandler.isLoggedIn());
        apiHandler.logout();
        assertFalse(apiHandler.isLoggedIn());

        mockServer.verify();
    }

    public void testNullParamsLogin() throws Exception {
        try {
            BambooSession apiHandler = createBambooSession(null);
            apiHandler.login(null, null);
            fail();
        } catch (RemoteApiException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }

    @Override
    protected ProductSession getProductSession(final String url) throws RemoteApiMalformedUrlException {
        return createBambooSession(url);
    }


    @Override
    protected JettyMockServer.Callback getLoginCallback(final boolean isFail) {
        return new LoginCallback(USER_NAME, PASSWORD, LoginCallback.ALWAYS_FAIL);
    }

    @Override
    protected String getLoginUrl() {
        return "/api/rest/login.action";
    }

    public void testWrongParamsBambooLogin() throws Exception {
        try {
            BambooSession apiHandler = createBambooSession("");
            apiHandler.login("", "".toCharArray());
            fail();
        } catch (RemoteApiException ex) {
            System.out.println("Exception: " + ex.getMessage());
        }
    }


    public void testProjectList() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        List<BambooProject> projects = apiHandler.listProjectNames();
        apiHandler.logout();

        Util.verifyProjectListResult(projects);

        mockServer.verify();
    }

    public void testGetPlanList() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        Collection<BambooPlan> plans = apiHandler.getPlanList();
        apiHandler.logout();

        Util.verifyPlanListResult(plans);
        mockServer.verify();
    }

	public void testGetPlanList_40() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalbackHttp500());
		mockServer.expect("/rest/api/latest/info", new BamboBuildNumberCalback40());
		mockServer.expect("/rest/api/latest/plan", new PlanListCallback40());
		mockServer.expect("/rest/api/latest/plan/", new FavouritePlanListCallback40());
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		Collection<BambooPlan> plans = apiHandler.getPlanList();
		apiHandler.logout();

		Util.verifyPlanListResult(plans);
		mockServer.verify();
	}

    public void testFavouritePlanList() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getLatestUserBuilds.action", new FavouritePlanListCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        List<String> plans = apiHandler.getFavouriteUserPlans();
        apiHandler.logout();

        Util.verifyFavouriteListResult(plans);
        mockServer.verify();
    }

	public void testBuildDetailsFor1CommitSuccessTests() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));

		mockServer.expect("/api/rest/getBambooBuildNumber.action",
				new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
		mockServer.expect("/api/rest/getBuildResultsDetails.action",
				new BuildDetailsResultCallback("buildResult-1Commit-SuccessfulTests.xml", "100"));

		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
		apiHandler.logout();

		mockServer.verify();

		assertNotNull(build);
		assertEquals("13928", build.getVcsRevisionKey());
		// commit
		assertEquals(1, build.getCommitInfo().size());
		assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
		assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
		assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
		assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
		assertEquals("13928",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
		assertEquals(
				"/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
				build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

        // failed tests
		assertEquals(0, build.getFailedTestDetails().size());

		// successful tests
		assertEquals(117, build.getSuccessfulTestDetails().size());
		assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
				build.getSuccessfulTestDetails().iterator().next().getTestClassName());
		assertEquals("testProjectList",
				build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
		assertEquals(0.046,
				build.getSuccessfulTestDetails().iterator().next().getTestDuration());
		assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
		assertEquals(TestResult.TEST_SUCCEED,
				build.getSuccessfulTestDetails().iterator().next().getTestResult());
	}

    public void testBuildForPlanSuccessNoTimezone() throws Exception {
        implTestBuildForPlanSuccess(0);
    }

    public void testBuildForPlanSuccessNegativeTimezone() throws Exception {
        implTestBuildForPlanSuccess(-5);
    }

    public void testBuildForPlanSuccessPositiveTimezone() throws Exception {
        implTestBuildForPlanSuccess(7);
    }

    private void implTestBuildForPlanSuccess(int timezoneOffset) throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        ConnectionCfg bambooServerCfg = createServerData();
        BambooSession apiHandler =
                new BambooSessionImpl(bambooServerCfg, new TestHttpSessionCallbackImpl(), LoggerImpl.getInstance());
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", timezoneOffset);
        apiHandler.logout();

        Util.verifySuccessfulBuildResult(build, mockBaseUrl);
        assertEquals(30, build.getTestsPassed());
        assertEquals(10, build.getTestsFailed());
        final DateTime expectedDate = new DateTime(2008, 1, 29, 14, 49, 36, 0).plusHours(timezoneOffset);
        assertEquals(expectedDate.toDate(), build.getCompletionDate());
        assertEquals(expectedDate.toDate(), build.getStartDate());

        mockServer.verify();
    }

    private ConnectionCfg createServerData() {
        return new ConnectionCfg("mybamboo", mockBaseUrl, "", "");
    }

    public void testGetLatestBuildForNeverExecutedPlan() throws RemoteApiException {
        implTestGetLatestBuildForNeverExecutedPlan(
                "/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse-never-executed.xml");
    }

    public void testGetLatestBuildForNeverExecutedPlan2() throws RemoteApiException {
        implTestGetLatestBuildForNeverExecutedPlan(
                "/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse-never-executed2.xml");
    }

    private void implTestGetLatestBuildForNeverExecutedPlan(final String fullFilePath) throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("", fullFilePath));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        Date now = new Date();
        ConnectionCfg bambooServerCfg = createServerData();
        BambooSession apiHandler =
                new BambooSessionImpl(bambooServerCfg, new TestHttpSessionCallbackImpl(), LoggerImpl.getInstance());
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        final BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", 0);
        apiHandler.logout();
        Assert.assertEquals(BuildStatus.UNKNOWN, build.getStatus());
        TestUtil.assertThrows(UnsupportedOperationException.class, new IAction() {
            public void run() throws Throwable {
                build.getNumber();
            }
        });
        assertNull(build.getStartDate());
        assertNull(build.getCompletionDate());
        TestUtil.assertHasOnlyElements(build.getCommiters());
        assertNull(build.getDurationDescription());
        assertEquals(mockBaseUrl + "/browse/TP-DEF", build.getBuildUrl());
        assertNull(build.getTestSummary());
        assertEquals(bambooServerCfg, build.getServer());
        assertTrue(build.getEnabled());
        assertNull(build.getProjectName());
        assertTrue(build.getPollingTime().getTime() >= now.getTime()
                && build.getPollingTime().getTime() <= new Date().getTime());
        assertNull(build.getErrorMessage());
        assertEquals("Never built", build.getReason());
    }

    public void testGetLatestBuildForPlanBamboo2x1x5() throws RemoteApiException {
        implTestGetLatestBuildForPlanBamboo2x1x5(0);
    }

    private void implTestGetLatestBuildForPlanBamboo2x1x5(int timezoneOffset) throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("",
                "/mock/bamboo/2_1_5/api/rest/getLatestBuildForPlanResponse.xml"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        ConnectionCfg bambooServerCfg = createServerData();
        BambooSession apiHandler =
                new BambooSessionImpl(bambooServerCfg, new TestHttpSessionCallbackImpl(), LoggerImpl.getInstance());
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", timezoneOffset);
        apiHandler.logout();

        Assert.assertEquals("ACC-TST", build.getPlanKey());
        Assert.assertEquals(193, build.getNumber());
        assertEquals(BuildStatus.SUCCESS, build.getStatus());
        Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
        Assert.assertEquals(mockBaseUrl, build.getServerUrl());
        Assert.assertEquals(mockBaseUrl + "/browse/ACC-TST-193", build.getResultUrl());
        Assert.assertEquals(mockBaseUrl + "/browse/ACC-TST", build.getBuildUrl());
        assertEquals("Atlassian Connector Commons", build.getProjectName());
        assertEquals(267, build.getTestsPassed());
        assertEquals(0, build.getTestsFailed());
        assertEquals("Code has changed", build.getReason());
        assertEquals("267 passed", build.getTestSummary());
        assertEquals("3 minutes ago", build.getRelativeBuildDate());
        assertEquals("28 seconds", build.getDurationDescription());
        assertEquals(new DateTime(2009, 2, 9, 7, 38, 36, 0, DateTimeZone.forOffsetHours(-6)).toDate(),
                build.getCompletionDate());
        TestUtil.assertHasOnlyElements(build.getCommiters(), "wseliga", "mwent");
        mockServer.verify();
    }

    public void testGetLatestBuildForPlanBamboo2x1x5WithTimeZoneOffset() throws RemoteApiException {
        implTestGetLatestBuildForPlanBamboo2x1x5(3);
    }


    public void testBuildForPlanFailure() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("FAILED"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", 0);
        apiHandler.logout();

        Util.verifyFailedBuildResult(build, mockBaseUrl);

        mockServer.verify();
    }

    public void test30BuildForPlanAndNumber() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/3_0/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getRecentlyCompletedBuildResultsForBuild.action",
                new RecentCompletedBuildResultsCallback());
        mockServer.expect("/rest/api/latest/result/TP-DEF-140", new BuildForPlanAndNumberCallback(Util.RESOURCE_BASE_3_0,
                "/rest/api/latest/result"));
        mockServer.expect("/rest/api/latest/plan/TP-DEF", new LatestPlanCallback("TP-DEF", "bamboo-plan-tp-def.xml",
                LatestPlanCallback.NON_ERROR, Util.RESOURCE_BASE_3_0));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getBuildForPlanAndNumber("TP-DEF", 140, 0);
        apiHandler.logout();
        Util.verifySuccessfulBuildResult(build, mockBaseUrl);
        assertEquals("Atlassian Eclipse Connector", build.getProjectName());
        assertEquals("Atlassian Eclipse Connector - Deploy Nightly Build", build.getPlanName());
    }

    public void testBuildForPlanAndNumber() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getRecentlyCompletedBuildResultsForBuild.action",
                new RecentCompletedBuildResultsCallback());
        mockServer.expect("/rest/api/latest/build/TP-DEF/140", new BuildForPlanAndNumberCallback(Util.RESOURCE_BASE_2_3,
                "/rest/api/latest/build"));
        mockServer.expect("/rest/api/latest/plan/TP-DEF", new LatestPlanCallback("TP-DEF", "bamboo-plan-tp-def.xml",
                LatestPlanCallback.NON_ERROR, Util.RESOURCE_BASE_2_3));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getBuildForPlanAndNumber("TP-DEF", 140, 0);
        apiHandler.logout();
        Util.verifySuccessfulBuildResult(build, mockBaseUrl);
        assertEquals("Atlassian Eclipse Connector", build.getProjectName());
        assertEquals("Atlassian Eclipse Connector - Deploy Nightly Build", build.getPlanName());
    }

    public void testBuildForNonExistingPlan() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultCallback("WRONG"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BambooBuild build = apiHandler.getLatestBuildForPlan("TP-DEF", 0);
        apiHandler.logout();

        Util.verifyErrorBuildResult(build);

        mockServer.verify();
    }

    public void testBuildDetailsFor1CommitFailedSuccessTests() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("buildResult-1Commit-FailedTests-SuccessfulTests.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
        apiHandler.logout();

        mockServer.verify();

        assertNotNull(build);
        assertEquals("13928", build.getVcsRevisionKey());
        // commit
        assertEquals(1, build.getCommitInfo().size());
        assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
        assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
        assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
        assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
        assertEquals("13928",
                build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
        assertEquals(
                "/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
                build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

        // failed tests
        assertEquals(2, build.getFailedTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
                build.getFailedTestDetails().iterator().next().getTestClassName());
        assertEquals("testSingleSuccessResultForDisabledBuild",
                build.getFailedTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.012,
                build.getFailedTestDetails().iterator().next().getTestDuration());
        assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_FAILED,
                build.getFailedTestDetails().iterator().next().getTestResult());

        // successful tests
        assertEquals(117, build.getSuccessfulTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
                build.getSuccessfulTestDetails().iterator().next().getTestClassName());
        assertEquals("testProjectList",
                build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.046,
                build.getSuccessfulTestDetails().iterator().next().getTestDuration());
        assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_SUCCEED,
                build.getSuccessfulTestDetails().iterator().next().getTestResult());
    }


    public void testBuildDetailsFor3_0() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));

        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/3_0/api/rest/bambooBuildNumberResponse.xml"));
        //?expand=stages.stage.plans
        mockServer.expect("/rest/api/latest/plan/COMMON-BB",
                new JobKeyForChainCallback("/mock/bamboo/3_0/api/rest/jobKeyForChain.xml", "COMMON-BB"));

        mockServer.expect("/rest/api/latest/result/COMMON-BB-JOB1-5",
                new BuildDetails30ResultCallback("/mock/bamboo/3_0/api/rest/buildResultDetails.xml", "COMMON-BB-JOB1-5"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("COMMON-BB", 5);
        apiHandler.logout();

        mockServer.verify();

        assertNotNull(build);
        // commit
        assertEquals(0, build.getCommitInfo().size());

        // failed tests
        assertEquals(2, build.getFailedTestDetails().size());
        assertEquals("com.atlassian.bitbucket.ChangeSetBuilderTest",
                build.getFailedTestDetails().iterator().next().getTestClassName());
        assertEquals("testFindChangeSetsForRepository",
                build.getFailedTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.191,
                build.getFailedTestDetails().iterator().next().getTestDuration());
        assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_FAILED,
                build.getFailedTestDetails().iterator().next().getTestResult());

        // successful tests
        assertEquals(0, build.getSuccessfulTestDetails().size());
    }

    public void testBuildDetailsFor1CommitFailedTests() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("buildResult-1Commit-FailedTests.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
        apiHandler.logout();

        mockServer.verify();

        assertNotNull(build);
        assertEquals("13928", build.getVcsRevisionKey());
        // commit
        assertEquals(1, build.getCommitInfo().size());
        assertEquals("author", build.getCommitInfo().iterator().next().getAuthor());
        assertNotNull(build.getCommitInfo().iterator().next().getCommitDate());
        assertEquals("commit comment", build.getCommitInfo().iterator().next().getComment());
        assertEquals(3, build.getCommitInfo().iterator().next().getFiles().size());
        assertEquals("13928",
                build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getRevision());
        assertEquals(
                "/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
                build.getCommitInfo().iterator().next().getFiles().iterator().next().getFileDescriptor().getUrl());

        // failed tests
        assertEquals(2, build.getFailedTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
                build.getFailedTestDetails().iterator().next().getTestClassName());
        assertEquals("testSingleSuccessResultForDisabledBuild",
                build.getFailedTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.012,
                build.getFailedTestDetails().iterator().next().getTestDuration());
        assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_FAILED,
                build.getFailedTestDetails().iterator().next().getTestResult());

        // successful tests
        assertEquals(0, build.getSuccessfulTestDetails().size());
    }

	public void testGetJobsForPlanBamboo27() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		// mockServer.expect("/api/rest/getBambooBuildNumber.action",
		// new BamboBuildNumberCalback("/mock/bamboo/2_7/api/rest/bambooBuildNumberResponse.xml"));
		mockServer.expect("/rest/api/latest/plan/PLAN", new JobsForPlanCallback(
				Util.RESOURCE_BASE_2_7));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		List<BambooJobImpl> jobs = apiHandler.getJobsForPlan("PLAN");
		apiHandler.logout();
		assertEquals(3, jobs.size());
	}

	public void testGetBuildDetailsBamboo27() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action",
				new BamboBuildNumberCalback("/mock/bamboo/2_7/api/rest/bambooBuildNumberResponse.xml"));
		mockServer.expect("/rest/api/latest/plan/PLAN", new JobsForPlanCallback(
				Util.RESOURCE_BASE_2_7));
		// there are two jobs J1 and J2 returned by response for PLAN
		mockServer.expect("/rest/api/latest/result/J1-1", new BuildDetailsResultCallback27(Util.RESOURCE_BASE_2_7,
				"J1-result.xml"));
		mockServer.expect("/rest/api/latest/result/J2-1", new BuildDetailsResultCallback27(Util.RESOURCE_BASE_2_7,
				"J2-result.xml"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("PLAN", 1);
		apiHandler.logout();

		assertEquals(3, build.getJobs().size());
		assertEquals(2, build.getEnabledJobs().size());

		assertEquals(1, build.getFailedTestDetails().size());
		assertEquals(1, build.getJobs().get(0).getFailedTests().size());

		assertEquals(3, build.getSuccessfulTestDetails().size());
		assertEquals(1, build.getJobs().get(0).getSuccessfulTests().size());
		assertEquals(2, build.getJobs().get(1).getSuccessfulTests().size());
	}

	public void testGetBuildDetailsBamboo30() throws Exception {
		mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
		mockServer.expect("/api/rest/getBambooBuildNumber.action",
				new BamboBuildNumberCalback("/mock/bamboo/3_0/api/rest/bambooBuildNumberResponse.xml"));
		mockServer.expect("/rest/api/latest/plan/PLAN", new JobsForPlanCallback(
				Util.RESOURCE_BASE_2_7));
		// there are two jobs J1 and J2 returned by response for PLAN
		mockServer.expect("/rest/api/latest/result/J1-1", new BuildDetailsResultCallback27(Util.RESOURCE_BASE_3_0,
				"J1-result.xml"));
		mockServer.expect("/rest/api/latest/result/J2-1", new BuildDetailsResultCallback27(Util.RESOURCE_BASE_3_0,
				"J2-result.xml"));
		mockServer.expect("/api/rest/logout.action", new LogoutCallback());

		BambooSession apiHandler = createBambooSession(mockBaseUrl);
		apiHandler.login(USER_NAME, PASSWORD.toCharArray());
		BuildDetails build = apiHandler.getBuildResultDetails("PLAN", 1);
		apiHandler.logout();

		assertEquals(3, build.getJobs().size());
		assertEquals(2, build.getEnabledJobs().size());

		assertEquals(2, build.getFailedTestDetails().size());
		assertEquals(2, build.getJobs().get(0).getFailedTests().size());

		assertEquals(2, build.getSuccessfulTestDetails().size());
		assertEquals(0, build.getJobs().get(0).getSuccessfulTests().size());
		assertEquals(2, build.getJobs().get(1).getSuccessfulTests().size());
	}

    public void testBuildDetailsFor3CommitFailedSuccessTests() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("buildResult-3Commit-FailedTests-SuccessfulTests.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
        apiHandler.logout();

        mockServer.verify();

        assertNotNull(build);
        assertEquals("13928", build.getVcsRevisionKey());
        // commit
        assertEquals(3, build.getCommitInfo().size());
        assertEquals("author", build.getCommitInfo().get(0).getAuthor());
        assertNotNull(build.getCommitInfo().get(0).getCommitDate());
        assertEquals("commit comment", build.getCommitInfo().get(0).getComment());
        assertEquals(3, build.getCommitInfo().get(0).getFiles().size());
        assertEquals("13928", build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getRevision());
        assertEquals(
                "/PL/trunk/ThePlugin/src/main/java/com/atlassian/theplugin/bamboo/HtmlBambooStatusListener.java",
                build.getCommitInfo().get(0).getFiles().iterator().next().getFileDescriptor().getUrl());
        assertEquals(2, build.getCommitInfo().get(1).getFiles().size());
        assertEquals(1, build.getCommitInfo().get(2).getFiles().size());

        // failed tests
        assertEquals(2, build.getFailedTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
                build.getFailedTestDetails().iterator().next().getTestClassName());
        assertEquals("testSingleSuccessResultForDisabledBuild",
                build.getFailedTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.012,
                build.getFailedTestDetails().iterator().next().getTestDuration());
        assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_FAILED,
                build.getFailedTestDetails().iterator().next().getTestResult());

        assertEquals("error 1", build.getFailedTestDetails().get(0).getErrors());
        assertEquals("error 2", build.getFailedTestDetails().get(1).getErrors());

        // successful tests
        assertEquals(117, build.getSuccessfulTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
                build.getSuccessfulTestDetails().iterator().next().getTestClassName());
        assertEquals("testProjectList",
                build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.046,
                build.getSuccessfulTestDetails().iterator().next().getTestDuration());
        assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_SUCCEED,
                build.getSuccessfulTestDetails().iterator().next().getTestResult());

        assertEquals("com.atlassian.theplugin.crucible.CrucibleServerFacadeConnectionTest",
                build.getSuccessfulTestDetails().get(116).getTestClassName());
        assertEquals("testConnectionTestFailedNullPassword",
                build.getSuccessfulTestDetails().get(116).getTestMethodName());
        assertEquals(0.001,
                build.getSuccessfulTestDetails().get(116).getTestDuration());
        assertNull(build.getSuccessfulTestDetails().get(116).getErrors());
        assertEquals(TestResult.TEST_SUCCEED,
                build.getSuccessfulTestDetails().get(116).getTestResult());
    }

    public void testBuildDetailsForNoCommitFailedSuccessTests() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("buildResult-NoCommit-FailedTests-SuccessfulTests.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
        apiHandler.logout();

        mockServer.verify();

        assertNotNull(build);
        assertEquals("13928", build.getVcsRevisionKey());
        // commit
        assertEquals(0, build.getCommitInfo().size());

        // failed tests
        assertEquals(2, build.getFailedTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.HtmlBambooStatusListenerTest",
                build.getFailedTestDetails().iterator().next().getTestClassName());
        assertEquals("testSingleSuccessResultForDisabledBuild",
                build.getFailedTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.012,
                build.getFailedTestDetails().iterator().next().getTestDuration());
        assertNotNull(build.getFailedTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_FAILED,
                build.getFailedTestDetails().iterator().next().getTestResult());

        // successful tests
        assertEquals(117, build.getSuccessfulTestDetails().size());
        assertEquals("com.atlassian.theplugin.commons.bamboo.BambooServerFacadeTest",
                build.getSuccessfulTestDetails().iterator().next().getTestClassName());
        assertEquals("testProjectList",
                build.getSuccessfulTestDetails().iterator().next().getTestMethodName());
        assertEquals(0.046,
                build.getSuccessfulTestDetails().iterator().next().getTestDuration());
        assertNull(build.getSuccessfulTestDetails().iterator().next().getErrors());
        assertEquals(TestResult.TEST_SUCCEED,
                build.getSuccessfulTestDetails().iterator().next().getTestResult());
    }

    public void testBuildDetailsForNonExistingBuild() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("buildNotExistsResponse.xml", "200"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            apiHandler.getBuildResultDetails("TP-DEF", 200);
            fail();
        } catch (RemoteApiException e) {
            // expected
        }
        apiHandler.logout();

        mockServer.verify();
    }

    public void testBuildDetailsMalformedResponse() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("malformedBuildResult.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            apiHandler.getBuildResultDetails("TP-DEF", 100);
            fail();
        } catch (RemoteApiException e) {
            assertEquals("org.jdom.JDOMException", e.getCause().getClass().getName());
        }
        apiHandler.logout();

        mockServer.verify();
    }

    public void testBuildDetailsEmptyResponse() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action",
                new BamboBuildNumberCalback("/mock/bamboo/2_3/api/rest/bambooBuildNumberResponse.xml"));
        mockServer.expect("/api/rest/getBuildResultsDetails.action",
                new BuildDetailsResultCallback("emptyResponse.xml", "100"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        BuildDetails build = apiHandler.getBuildResultDetails("TP-DEF", 100);
        apiHandler.logout();

        assertEquals(0, build.getCommitInfo().size());
        assertEquals(0, build.getSuccessfulTestDetails().size());
        assertEquals(0, build.getFailedTestDetails().size());

        mockServer.verify();
    }

    public void testAddSimpleLabel() throws Exception {
        String label = "label siple text";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addLabelToBuild("TP-DEF", 100, label);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddEmptyLabel() throws Exception {
        String label = "";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addLabelToBuild("TP-DEF", 100, label);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddMultiLineLabel() throws Exception {
        String label = "Label first line\nLabel second line	\nLabel third line";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addLabelToBuildResults.action", new AddLabelToBuildCallback(label));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addLabelToBuild("TP-DEF", 100, label);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddLabelToNonExistingBuild() throws Exception {
        String label = "Label";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addLabelToBuildResults.action",
                new AddLabelToBuildCallback(label, "200", AddLabelToBuildCallback.NON_EXIST_FAIL));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            apiHandler.addLabelToBuild("TP-DEF", 200, label);
            fail();
        } catch (RemoteApiException e) {

        }
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddComment() throws Exception {
        String comment = "comment siple text";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addCommentToBuild("TP-DEF", 100, comment);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddEmptyComment() throws Exception {
        String comment = "";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addCommentToBuild("TP-DEF", 100, comment);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddMultiLineComment() throws Exception {
        String comment = "Comment first line\nComment ; second line	\nComment third line";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addCommentToBuildResults.action", new AddCommentToBuildCallback(comment));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.addCommentToBuild("TP-DEF", 100, comment);
        apiHandler.logout();

        mockServer.verify();
    }

    public void testAddCommentToNonExistingBuild() throws Exception {
        String comment = "Comment";

        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/addCommentToBuildResults.action",
                new AddCommentToBuildCallback(comment, "200", AddCommentToBuildCallback.NON_EXIST_FAIL));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            apiHandler.addCommentToBuild("TP-DEF", 200, comment);
            fail();
        } catch (RemoteApiException e) {

        }
        apiHandler.logout();

        mockServer.verify();
    }

    public void testExecuteBuild() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
        mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.executeBuild("TP-DEF");
        apiHandler.logout();

        mockServer.verify();
    }

    public void testExecuteBuildFailed() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
        mockServer.expect("/api/rest/executeBuild.action", new ExecuteBuildCallback(ExecuteBuildCallback.NON_EXIST_FAIL));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler = createBambooSession(mockBaseUrl);
        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        try {
            apiHandler.executeBuild("TP-DEF");
            fail();
        } catch (RemoteApiException e) {
            // expected
        }
        apiHandler.logout();

        mockServer.verify();
    }

    public void testRenewSession() throws Exception {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
        mockServer.expect("/api/rest/listProjectNames.action", new ErrorMessageCallback("authExpiredResponse.xml"));
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listProjectNames.action", new ProjectListCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession apiHandler =
                new AutoRenewBambooSession(createServerData(), new TestHttpSessionCallbackImpl(), LoggerImpl.getInstance());

        apiHandler.login(USER_NAME, PASSWORD.toCharArray());
        apiHandler.listProjectNames();
        List<BambooProject> projects = apiHandler.listProjectNames();
        apiHandler.logout();

        Util.verifyProjectListResult(projects);

        mockServer.verify();
    }

    public void testOutOfRangePort() {
        try {
            BambooSession apiHandler = createBambooSession("http://localhost:80808");
            apiHandler.login(USER_NAME, PASSWORD.toCharArray());
            fail("Exception expected");
        } catch (RemoteApiException e) {
            assertTrue("MalformedURLException expected", e.getCause() instanceof IOException);
        }

    }

    public void testEnabledStatus() throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultVelocityCallback("PO-TP", 123));
        mockServer.expect("/api/rest/listBuildNames.action", new PlanListCallback());
        mockServer.expect("/api/rest/getLatestBuildResults.action", new LatestBuildResultVelocityCallback("PT-TOP", 45));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());

        BambooBuild bbi1 = session.getLatestBuildForPlan("PO-TP", 0);
        assertEquals(123, bbi1.getNumber());
        assertTrue(bbi1.getEnabled());

        BambooBuild bbi2 = session.getLatestBuildForPlan("PT-TOP", 0);
        assertEquals(45, bbi2.getNumber());
        assertFalse(bbi2.getEnabled());
        session.logout();

        mockServer.verify();

    }

    public void testGetBuildLogs() throws RemoteApiException, UnsupportedEncodingException {
        final String TEXT = "ĄŚĆŹ$&#";
        final String charset1 = "UTF-8";
        final String charset2 = "UTF-16";
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/api/rest/getBambooBuildNumber.action", new BamboBuildNumberCalback());
        mockServer.expect("/download/myplan/build_logs/myplan-123.log", new BuildLogCallback(TEXT, charset1));
        mockServer.expect("/download/myplan/build_logs/myplan-123.log", new BuildLogCallback(TEXT, charset2));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        final BambooSession session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());

        assertEquals(TEXT, session.getBuildLogs("myplan", 123));
        assertEquals(TEXT, session.getBuildLogs("myplan", 123));
        session.logout();
        mockServer.verify();

    }

    private static class BuildLogCallback implements JettyMockServer.Callback {
        private final String text;
        private final String charsetName;

        public BuildLogCallback(final String text, final String charsetName) {
            this.text = text;
            this.charsetName = charsetName;
        }

        public void onExpectedRequest(final String target, final HttpServletRequest request,
                                      final HttpServletResponse response) throws Exception {
            final ServletOutputStream out = response.getOutputStream();
            response.setContentType("text/plain; charset=" + charsetName + "");
            out.write(text.getBytes(charsetName));
            out.close();
        }
    }

    public static BambooSessionImpl createBambooSession(@Nullable String url) throws RemoteApiMalformedUrlException {
        return new BambooSessionImpl(new ConnectionCfg("", url, "", ""), new TestHttpSessionCallbackImpl(), LoggerImpl
                .getInstance());
    }


    public void testGetPlanDetails() throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/rest/api/latest/plan/ECL-DPL", new LatestPlanCallback());
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());

        BambooPlan plan = session.getPlanDetails("ECL-DPL");
        assertEquals("Atlassian Eclipse Connector", plan.getProjectName());
        assertTrue(plan.isEnabled());

        session.logout();

        mockServer.verify();
    }

    public void testGetPlanDetailsDisabled() throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/rest/api/latest/plan/ECL-DPL", new LatestPlanCallback("planResponseDisabled.xml"));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());

        BambooPlan plan = session.getPlanDetails("ECL-DPL");
        assertEquals("Atlassian Eclipse Connector", plan.getProjectName());
        assertFalse(plan.isEnabled());

        session.logout();

        mockServer.verify();
    }

    public void testGetPlanDetails404() throws RemoteApiException {
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        mockServer.expect("/rest/api/latest/plan/ECL-DPL", new LatestPlanCallback(LatestPlanCallback.NON_EXIST_FAIL));
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());

        BambooSession session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());

        try {
            BambooPlan plan = session.getPlanDetails("ECL-DPL");
            fail("RemoteApiException excpected");
        } catch (RemoteApiException e) {
            assertEquals("Malformed server reply: no 'plan' element", e.getMessage());
        }

        session.logout();

        mockServer.verify();
    }

    @Nullable
    private TestDetails findTestDetails(Collection<TestDetails> testDetails, String testName, String className) {
        for (TestDetails details : testDetails) {
            if (details.getTestMethodName().equals(testName)
                    && details.getTestClassName().equals(className)) {
                return details;
            }
        }
        return null;
    }

    public void testGetBuildTestResultForBamboo2x6() throws RemoteApiException {
        String expectedUri = "/rest/api/latest/build/ECL-TST/1635";// ?expand=testResults.successful.testResult";
        mockServer.expect("/api/rest/login.action", new LoginCallback(USER_NAME, PASSWORD));
        GenericResourceCallback callback = new GenericResourceCallback(
                "/mock/bamboo/2_6/api/rest/buildDetailsWithExpandedTestResults.xml", expectedUri);
        mockServer.expect(expectedUri, callback);
        mockServer.expect("/api/rest/logout.action", new LogoutCallback());
        BambooSessionImpl session = createBambooSession(mockBaseUrl);
        session.login(USER_NAME, PASSWORD.toCharArray());
        final BuildDetails buildDetails = session.getBuildResultDetailsMoreRestish("ECL-TST", 1635);
        assertEquals(4, buildDetails.getFailedTestDetails().size());
        assertEquals(207, buildDetails.getSuccessfulTestDetails().size());
        assertEquals(4, buildDetails.getCommitInfo().size());

        {
            final BambooChangeSet cs1 = buildDetails.getCommitInfo().get(0);
            assertEquals("pniewiadomski", cs1.getAuthor());
            assertEquals(ISODateTimeFormat.dateTimeParser().parseDateTime("2010-05-20T05:44:20.543-05:00").toDate(),
                    cs1.getCommitDate());
            assertEquals(4, cs1.getFiles().size());
            final BambooFileInfo file1 = cs1.getFiles().get(0);
            assertEquals("/PLE/trunk/com.atlassian.connector.eclipse.releng/bamboo.properties",
                    file1.getFileDescriptor().getUrl());
            assertEquals("60323", file1.getFileDescriptor().getRevision());

            final BambooFileInfo file4 = cs1.getFiles().get(3);
            assertEquals("/PLE/trunk/com.atlassian.connector.eclipse.releng/bamboo-release.properties",
                    file4.getFileDescriptor().getUrl());
            assertEquals("60323", file4.getFileDescriptor().getRevision());
        }

        {
            final BambooChangeSet cs4 = buildDetails.getCommitInfo().get(3);
            assertEquals("NONE: switching to e3.5", cs4.getComment());
            assertEquals("pniewiadomski", cs4.getAuthor());
            assertEquals(ISODateTimeFormat.dateTimeParser().parseDateTime("2010-05-20T06:08:26.969-05:00").toDate(),
                    cs4.getCommitDate());
            assertEquals(1, cs4.getFiles().size());
            final BambooFileInfo file1 = cs4.getFiles().get(0);
            assertEquals("/PLE/trunk/com.atlassian.connector.eclipse.releng/maps/mylyn_e3.5.map",
                    file1.getFileDescriptor().getUrl());
            assertEquals("60326", file1.getFileDescriptor().getRevision());
        }

        final TestDetails test1 = findTestDetails(buildDetails.getSuccessfulTestDetails(), "testChangeCredentials",
                "com.atlassian.connector.eclipse.jira.tests.core.JiraClientFactoryTest");
        assertNotNull(test1);
        assertEquals(0.494, test1.getTestDuration(), 0.001);
        assertEquals("", test1.getErrors());
        assertEquals(TestResult.TEST_SUCCEED, test1.getTestResult());

        final TestDetails test2 = findTestDetails(buildDetails.getFailedTestDetails(), "testAttachFile",
                "com.atlassian.connector.eclipse.jira.tests.core.JiraTaskAttachmentHandlerTest");
        assertNotNull(test2);
        assertEquals(0.947, test2.getTestDuration(), 0.001);
//        assertTrue(test2.getErrors().contains("org.eclipse.core.runtime.CoreException:\n"));
//        assertTrue(test2.getErrors().contains(
//                "com.atlassian.connector.eclipse.internal."
//                        + "jira.core.JiraTaskAttachmentHandler.postContent(JiraTaskAttachmentHandler.java:109)"));
//        assertTrue(test2.getErrors().trim().endsWith("at org.eclipse.core.launcher.Main.main(Main.java:34)"));

        assertEquals(TestResult.TEST_FAILED, test2.getTestResult());
        session.logout();

        mockServer.verify();
    }
}
