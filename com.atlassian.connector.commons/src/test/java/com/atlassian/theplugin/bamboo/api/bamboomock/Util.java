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

package com.atlassian.theplugin.bamboo.api.bamboomock;

import static junit.framework.Assert.assertEquals;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooProject;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.commons.util.ResourceUtil;
import org.apache.commons.httpclient.HttpStatus;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import junit.framework.Assert;

public abstract class Util {

	public static final String RESOURCE_BASE_1_2_4 = "/mock/bamboo/1_2_4/api/rest/";
	public static final String RESOURCE_BASE_2_1_5 = "/mock/bamboo/2_1_5/api/rest/";
	public static final String RESOURCE_BASE_2_3 = "/mock/bamboo/2_3/api/rest/";
    public static final String RESOURCE_BASE_3_0 = "/mock/bamboo/3_0/api/rest/";
	public static final String RESOURCE_BASE_2_7 = "/mock/bamboo/2_7/api/rest/";
	public static final String RESOURCE_BASE_4_0 = "/mock/bamboo/4_0/api/rest/";


	private Util() {
	}

	public static void copyResource(OutputStream outputStream, String resource) {
		ResourceUtil.copyResource(outputStream, Util.class, RESOURCE_BASE_1_2_4 + resource);
	}

	public static void copyResourceWithFullPath(OutputStream outputStream, String resourceFullPath) {
		ResourceUtil.copyResource(outputStream, Util.class, resourceFullPath);
	}

	public static void verifySuccessfulBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertNotNull(build);
		Assert.assertEquals("TP-DEF", build.getPlanKey());
		Assert.assertEquals(140, build.getNumber());
		//todo: sginter: What should go here? bamboo-provided status or the BuildStatus.toString()
		//assertEquals("Successful", build.getStatus());
		Assert.assertSame(BuildStatus.SUCCESS, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals(baseUrl, build.getServerUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-140", build.getResultUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getBuildUrl());
		Assert.assertNull(build.getErrorMessage());
	}

	public static void verifyFailedBuildResult(BambooBuild build, String baseUrl) {
		Assert.assertNotNull(build);
		Assert.assertEquals("TP-DEF", build.getPlanKey());
		Assert.assertEquals(141, build.getNumber());
		//todo: sginter: What should go here? bamboo-provided status or the BuildStatus.toString()
		//assertEquals("Failed", build.getStatus());
		Assert.assertSame(BuildStatus.FAILURE, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals(baseUrl, build.getServerUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF-141", build.getResultUrl());
		Assert.assertEquals(baseUrl + "/browse/TP-DEF", build.getBuildUrl());
		Assert.assertNull(build.getErrorMessage());
	}

	public static void verifyErrorBuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("The user does not have sufficient permissions to perform this action.\n",
				build.getErrorMessage());
	}

	public static void verifyLoginErrorBuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		Assert.assertEquals("The user does not have sufficient permissions to perform this action.\n",
				build.getErrorMessage());
	}

	public static void verifyError400BuildResult(BambooBuild build) {
		Assert.assertSame(BuildStatus.UNKNOWN, build.getStatus());
		Assert.assertTrue(build.getPollingTime().getTime() - System.currentTimeMillis() < 5000);
		final String errorMessage = build.getErrorMessage();
		Assert.assertNotNull(errorMessage);
		Assert.assertTrue(errorMessage.startsWith("HTTP " + HttpStatus.SC_BAD_REQUEST + " ("
				+ HttpStatus.getStatusText(HttpStatus.SC_BAD_REQUEST) + ")"));
	}

	private static final String[][] expectedProjects = { { "PO", "Project One" }, { "PT", "Project Two" },
			{ "PEMPTY", "Project Three - Empty" } };

	public static void verifyProjectListResult(Collection<BambooProject> projects) {
		Assert.assertEquals(expectedProjects.length, projects.size());

		Iterator<BambooProject> iterator = projects.iterator();
		for (String[] pair : expectedProjects) {
			BambooProject project = iterator.next();
			Assert.assertEquals(pair[0], project.getProjectKey());
			Assert.assertEquals(pair[1], project.getProjectName());
		}
	}

	private static final String[][] expectedPlans = { { "PO-FP", "First Project - First Plan", "true" },
			{ "PO-SECPLAN", "First Project - Second Plan", "true" }, { "PO-TP", "First Project - Third Plan", "true" },
			{ "PT-TOP", "Second Project - The Only Plan", "false" } };

	public static void verifyPlanListResult(Collection<BambooPlan> plans) {
		assertEquals(expectedPlans.length, plans.size());
		Iterator<BambooPlan> iterator = plans.iterator();
		for (String[] pair : expectedPlans) {
			BambooPlan plan = iterator.next();
			assertEquals(pair[0], plan.getKey());
			assertEquals(pair[1], plan.getName());
			assertEquals(Boolean.parseBoolean(pair[2]), plan.isEnabled());
		}
	}

	private static final String[][] expectedFavouritePlans = { { "PO-FP", "First Project - First Plan" },
			{ "PT-TOP", "Second Project - The Only Plan" } };

	public static void verifyFavouriteListResult(Collection<String> plans) {
		assertEquals(expectedFavouritePlans.length, plans.size());
		Iterator<String> iterator = plans.iterator();
		for (String[] pair : expectedFavouritePlans) {
			String plan = iterator.next();
			assertEquals(pair[0], plan);
		}
	}

	private static final String[][] expectedPlansWithFavourites = { { "PO-FP", "First Project - First Plan", "true" },
			{ "PO-SECPLAN", "First Project - Second Plan", "false" },
			{ "PO-TP", "First Project - Third Plan", "false" }, { "PT-TOP", "Second Project - The Only Plan", "true" } };

	public static void verifyPlanListWithFavouritesResult(Collection<BambooPlan> plans) {
		assertEquals(expectedPlansWithFavourites.length, plans.size());
		Iterator<BambooPlan> iterator = plans.iterator();
		for (String[] pair : expectedPlansWithFavourites) {
			BambooPlan plan = iterator.next();
			assertEquals(pair[0], plan.getKey());
			assertEquals(pair[1], plan.getName());
			if ("true".equalsIgnoreCase(pair[2])) {
				Assert.assertTrue(plan.isFavourite());
			}
		}
	}

	public static void verifyBuildCompletedDate(final BambooBuild build, Date expectedDate) {
		Assert.assertNotNull(build);
		Assert.assertEquals(expectedDate, build.getCompletionDate());
	}
}
