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

package me.glindholm.connector.eclipse.jira.tests.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JQLFilterDataExtractor;

@Disabled
public class JQLFilterDataExtractorTest extends FilterDataExtractorTest {

	private JQLFilterDataExtractor extractor;

	@Override
	@BeforeEach
	public void setUp() {
		extractor = new JQLFilterDataExtractor();
	}

	protected void assertCollectionsEqual(final Collection<String> expected, final Collection<String> actual) {
		assertNotNull(expected);
		assertNotNull(actual);
		final var message = "expected=" + expected.toString() + " actual=" + actual.toString() + "expected size="
				+ expected.size() + " actual size=" + actual.size();
		assertEquals(expected.size(), actual.size(), "SIZE IS DIFFERENT " + message);
		assertTrue(actual.containsAll(expected), "UNKNOWN ELEMENTS " + message);
		assertTrue(expected.containsAll(actual), "MISSING ELEMENTS " + message);
	}

	@Override
	@Test
	public void testExtractAssignedTo() {
		// specific user
		var actual = extractor.extractAssignedTo(filterDefinition.getAssignedToFilter());
		Collection<String> expected = List.of("assigneelogin");
		assertCollectionsEqual(expected, actual);

		// current user
		actual = extractor.extractAssignedTo(filterDefinition2.getAssignedToFilter());
		expected = List.of("currentUser()");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractComponents() {
		// few component ids
		var actual = extractor.extractComponents(filterDefinition.getComponentFilter());
		Collection<String> expected = List.of("comp0id", "comp1id");
		assertCollectionsEqual(expected, actual);

		// no component
		actual = extractor.extractComponents(filterDefinition2.getComponentFilter());
		expected = List.of("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractDates() {
		final var dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		// several date ranges
		var actual = extractor.extractDates(filterDefinition.getCreatedDateFilter(), dateFormat);
		Collection<String> expected = List.of(">= \"2012-01-01\"", "<= \"2012-12-31\"");
		assertCollectionsEqual(expected, actual);

		actual = extractor.extractDates(filterDefinition.getUpdatedDateFilter(), dateFormat);
		expected = List.of(">= \"1998-02-01\"", "<= \"1998-02-28\"");
		assertCollectionsEqual(expected, actual);

		actual = extractor.extractDates(filterDefinition.getDueDateFilter(), dateFormat);
		expected = List.of(">= \"1900-01-01\"", "<= \"2999-12-31\"");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractIssueTypes() {
		// few issue types
		var actual = extractor.extractIssueTypes(filterDefinition.getIssueTypeFilter());
		Collection<String> expected = List.of("\"issue0name\"", "\"issue1name\"");
		assertCollectionsEqual(expected, actual);

		// sub-task and top-level issue types
		actual = extractor.extractIssueTypes(filterDefinition2.getIssueTypeFilter());
		expected = List.of("subTaskIssueTypes()", "standardIssueTypes()");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractPriorities() {
		// few priorities
		final var actual = extractor.extractPriorities(filterDefinition.getPriorityFilter());
		final Collection<String> expected = List.of("1", "2", "3", "4", "5");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractProjects() {
		// few project KEYs
		final var actual = extractor.extractProjects(filterDefinition.getProjectFilter());
		final Collection<String> expected = List.of("prj0KEY", "prj1KEY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractReportedBy() {
		// single login (specific user)
		var actual = extractor.extractReportedBy(filterDefinition.getReportedByFilter());
		Collection<String> expected = List.of("reporterlogin");
		assertCollectionsEqual(expected, actual);

		// no reporter
		actual = extractor.extractReportedBy(filterDefinition2.getReportedByFilter());
		expected = List.of("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractResolutions() {
		// few resolutions
		var actual = extractor.extractResolutions(filterDefinition.getResolutionFilter());
		Collection<String> expected = List.of("\"res0 name\"", "\"res1 name\"");
		assertCollectionsEqual(expected, actual);

		// unresolved
		actual = extractor.extractResolutions(filterDefinition2.getResolutionFilter());
		expected = List.of("Unresolved");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractStatuses() {
		// few status ids
		final var actual = extractor.extractStatuses(filterDefinition.getStatusFilter());
		final Collection<String> expected = List.of("status0id", "status1id");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractVersions() {
		// few specific versions + all released versions
		var actual = extractor.extractVersions(filterDefinition.getFixForVersionFilter());
		Collection<String> expected = List.of("\"ver0name\"", "\"ver1name\"", "releasedVersions()");
		assertCollectionsEqual(expected, actual);

		// few specific versions + all unreleased versions
		actual = extractor.extractVersions(filterDefinition.getReportedInVersionFilter());
		expected = List.of("\"ver1name\"", "\"ver2name\"", "unreleasedVersions()");
		assertCollectionsEqual(expected, actual);

		// no version at all
		actual = extractor.extractVersions(filterDefinition2.getFixForVersionFilter());
		expected = List.of("EMPTY");
		assertCollectionsEqual(expected, actual);
	}

	@Override
	@Test
	public void testExtractWorkRatios() {
		// just min + max
		final var actual = extractor.extractWorkRatios(filterDefinition.getEstimateVsActualFilter());
		final Collection<String> expected = List.of(" >= 10", " <= 90");
		assertCollectionsEqual(expected, actual);
	}

}
