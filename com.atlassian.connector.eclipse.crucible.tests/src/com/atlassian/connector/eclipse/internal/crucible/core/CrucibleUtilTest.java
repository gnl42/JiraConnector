/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.core;

import com.atlassian.theplugin.commons.crucible.api.model.PredefinedFilter;

import org.eclipse.mylyn.internal.tasks.core.RepositoryQuery;

import junit.framework.TestCase;

/**
 * @author Shawn Minto
 */
public class CrucibleUtilTest extends TestCase {

	public void testGetPredefinedFilter() {
		for (PredefinedFilter filter : PredefinedFilter.values()) {
			PredefinedFilter predefinedFilter = CrucibleUtil.getPredefinedFilter(filter.getFilterUrl());
			assertNotNull(predefinedFilter);
			assertEquals(filter, predefinedFilter);
		}

		assertNull(CrucibleUtil.getPredefinedFilter("NON-EXISTANT-FILTER-URL"));
	}

	public void testGetPermIdFromTaskId() {
		String permId = "CR-5";
		String taskId = CrucibleUtil.getPermIdFromTaskId(permId);

		// ensure decoding somtehing that is decoded works
		assertEquals(permId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(permId);
		assertNotSame(permId, taskId);

		String newPermId = CrucibleUtil.getPermIdFromTaskId(permId);
		assertEquals(permId, newPermId);
	}

	public void testGetTaskIdFromPermId() {
		String permId = "CR-5";
		String expectedTaskId = "CR%2D_5";

		String taskId = CrucibleUtil.getTaskIdFromPermId(permId);
		assertEquals(expectedTaskId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(taskId);
		assertEquals(expectedTaskId, taskId);

		taskId = CrucibleUtil.getTaskIdFromPermId(permId.replace("-", ""));
		assertEquals(permId.replace("-", ""), taskId);
	}

	public void testGetPredefinedFilterWebUrl() {
		String repositoryUrl = "http://crucible.atlassian.com";
		String filterUrl = "test";
		String expectedFilterWebUrl = repositoryUrl + "/cru/?filter=" + filterUrl;

		String filterWebUrl = CrucibleUtil.getPredefinedFilterWebUrl(repositoryUrl + "/", filterUrl);
		assertEquals(expectedFilterWebUrl, filterWebUrl);

		filterWebUrl = CrucibleUtil.getPredefinedFilterWebUrl(repositoryUrl, filterUrl);
		assertEquals(expectedFilterWebUrl, filterWebUrl);
	}

	public void testAddTrailingSlash() {
		String expectedUrl = "http://crucible.atlassian.com";

		String url = CrucibleUtil.addTrailingSlash(expectedUrl);
		assertEquals(expectedUrl + "/", url);

		url = CrucibleUtil.addTrailingSlash(expectedUrl + "/");
		assertEquals(expectedUrl + "/", url);

		url = CrucibleUtil.addTrailingSlash(expectedUrl + "//");
		assertEquals(expectedUrl + "//", url);
	}

	public void testGetReviewUrl() {
		String repositoryUrl = "http://crucible.atlassian.com";
		String permId = "CR-5";
		String expectedReviewWebUrl = repositoryUrl + "/cru/" + permId;

		String taskId = CrucibleUtil.getTaskIdFromPermId(permId);

		String reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl + "/", taskId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

		reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl, taskId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

		reviewWebUrl = CrucibleUtil.getReviewUrl(repositoryUrl + "/", permId);
		assertEquals(expectedReviewWebUrl, reviewWebUrl);

	}

	public void testGetTaskIdFromUrl() {
		String permId = "CR-5";
		String reviewWebUrl = "http://crucible.atlassian.com/cru/" + permId;

		String expectedTaskId = CrucibleUtil.getTaskIdFromPermId(permId);

		String taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertEquals(expectedTaskId, taskId);

		reviewWebUrl = "http://crucible.atlassian.com/" + permId;
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/" + permId + "/test";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru/" + permId + "/test";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru/";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

		reviewWebUrl = "http://crucible.atlassian.com/cru//";
		taskId = CrucibleUtil.getTaskIdFromUrl(reviewWebUrl);
		assertNull(taskId);

	}

	public void testGetRepositoryUrlFromUrl() {
		String expectedRepositoryUrl = "http://crucible.atlassian.com/";
		String permId = "CR-5";
		String url = expectedRepositoryUrl + "cru/" + permId;

		String repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + permId;
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertNull(repositoryUrl);

		url = expectedRepositoryUrl + permId + "/test";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertNull(repositoryUrl);

		url = expectedRepositoryUrl + "cru/" + permId + "/test";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + "cru/";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);

		url = expectedRepositoryUrl + "cru//";
		repositoryUrl = CrucibleUtil.getRepositoryUrlFromUrl(url);
		assertEquals(expectedRepositoryUrl, repositoryUrl);
	}

	public void testIsFilterDefinition() {
		RepositoryQuery query = new RepositoryQuery(CrucibleCorePlugin.CONNECTOR_KIND, "crucible-test");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute("test", "tst");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, "");
		assertTrue(CrucibleUtil.isFilterDefinition(query));

		query.setAttribute(CrucibleConstants.KEY_FILTER_ID, "someFilter");
		assertFalse(CrucibleUtil.isFilterDefinition(query));
	}

	// TODO finish the util test
	// TODO make a test to ensure that changing the creds gives us a new httpclient so the state is changed
	// TODO test the cache
	// TODO test the activeReviewManager
}
