/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.model;

import java.text.SimpleDateFormat;
import java.util.Collection;

import com.atlassian.connector.eclipse.internal.jira.core.model.filter.JQLFilterDataExtractor;
import com.google.common.collect.Lists;

public class JQLFilterDataExtractorTest extends FilterDataExtractorTest {

	private JQLFilterDataExtractor extractor;

	@Override
	public void setUp() {
		super.setUp();
		extractor = new JQLFilterDataExtractor();
	}

	protected void assertCollectionsEqual(Collection<String> expected, Collection<String> actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		String message = "expected=" + expected.toString() + " actual=" + actual.toString() + "expected size="
				+ expected.size() + " actual size=" + actual.size();
		assertEquals("SIZE IS DIFFERENT " + message, expected.size(), actual.size());
		assertTrue("UNKNOWN ELEMENTS " + message, actual.containsAll(expected));
		assertTrue("MISSING ELEMENTS " + message, expected.containsAll(actual));
	}

	@Override
	public void testExtractAssignedTo() {
		// specific user
		Collection<String> actual = extractor.extractAssignedTo(filterDefinition.getAssignedToFilter());
		Collection<String> expected = Lists.newArrayList("assigneelogin");
		assertCollectionsEqual(expected, actual);

		// current user
		actual = extractor.extractAssignedTo(filterDefinition2.getAssignedToFilter());
		expected = Lists.newArrayList("currentUser()");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractComponents() {
		// few component ids
		Collection<String> actual = extractor.extractComponents(filterDefinition.getComponentFilter());
		Collection<String> expected = Lists.newArrayList("comp0id", "comp1id");
		assertCollectionsEqual(expected, actual);

		// no component
		actual = extractor.extractComponents(filterDefinition2.getComponentFilter());
		expected = Lists.newArrayList("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractDates() {
		final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// several date ranges		
		Collection<String> actual = extractor.extractDates(filterDefinition.getCreatedDateFilter(), dateFormat);
		Collection<String> expected = Lists.newArrayList(">= \"2012-01-01\"", "<= \"2012-12-31\"");
		assertCollectionsEqual(expected, actual);

		actual = extractor.extractDates(filterDefinition.getUpdatedDateFilter(), dateFormat);
		expected = Lists.newArrayList(">= \"1998-02-01\"", "<= \"1998-02-28\"");
		assertCollectionsEqual(expected, actual);

		actual = extractor.extractDates(filterDefinition.getDueDateFilter(), dateFormat);
		expected = Lists.newArrayList(">= \"1900-01-01\"", "<= \"2999-12-31\"");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractIssueTypes() {
		// few issue types
		Collection<String> actual = extractor.extractIssueTypes(filterDefinition.getIssueTypeFilter());
		Collection<String> expected = Lists.newArrayList("\"issue0name\"", "\"issue1name\"");
		assertCollectionsEqual(expected, actual);

		// sub-task and top-level issue types
		actual = extractor.extractIssueTypes(filterDefinition2.getIssueTypeFilter());
		expected = Lists.newArrayList("subTaskIssueTypes()", "standardIssueTypes()");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractPriorities() {
		// few priorities
		Collection<String> actual = extractor.extractPriorities(filterDefinition.getPriorityFilter());
		Collection<String> expected = Lists.newArrayList("1", "2", "3", "4", "5");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractProjects() {
		// few project KEYs
		Collection<String> actual = extractor.extractProjects(filterDefinition.getProjectFilter());
		Collection<String> expected = Lists.newArrayList("prj0KEY", "prj1KEY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractReportedBy() {
		// single login (specific user)
		Collection<String> actual = extractor.extractReportedBy(filterDefinition.getReportedByFilter());
		Collection<String> expected = Lists.newArrayList("reporterlogin");
		assertCollectionsEqual(expected, actual);

		// no reporter
		actual = extractor.extractReportedBy(filterDefinition2.getReportedByFilter());
		expected = Lists.newArrayList("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractResolutions() {
		// few resolutions
		Collection<String> actual = extractor.extractResolutions(filterDefinition.getResolutionFilter());
		Collection<String> expected = Lists.newArrayList("\"res0 name\"", "\"res1 name\"");
		assertCollectionsEqual(expected, actual);

		// unresolved
		actual = extractor.extractResolutions(filterDefinition2.getResolutionFilter());
		expected = Lists.newArrayList("Unresolved");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractStatuses() {
		// few status ids
		Collection<String> actual = extractor.extractStatuses(filterDefinition.getStatusFilter());
		Collection<String> expected = Lists.newArrayList("status0id", "status1id");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	public void testExtractVersions() {
		// few specific versions + all released versions
		Collection<String> actual = extractor.extractVersions(filterDefinition.getFixForVersionFilter());
		Collection<String> expected = Lists.newArrayList("\"ver0name\"", "\"ver1name\"", "releasedVersions()");
		assertCollectionsEqual(expected, actual);

		// few specific versions + all unreleased versions
		actual = extractor.extractVersions(filterDefinition.getReportedInVersionFilter());
		expected = Lists.newArrayList("\"ver1name\"", "\"ver2name\"", "unreleasedVersions()");
		assertCollectionsEqual(expected, actual);

		// no version at all
		actual = extractor.extractVersions(filterDefinition2.getFixForVersionFilter());
		expected = Lists.newArrayList("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

}
