package com.atlassian.connector.eclipse.jira.tests.model;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.model.filter.JiraFieldsNames;

public class JiraFieldNamesTest extends TestCase {

	public void testClassicVsJql() {
		JiraFieldsNames classicNames = JiraFieldsNames.createClassic();
		JiraFieldsNames jqlNames = JiraFieldsNames.createJql();

		// just few tests if enum wrappers works
		assertEquals("version", classicNames.AFFECTED_VERSION());
		assertEquals("affectedVersion", jqlNames.AFFECTED_VERSION());
		assertEquals(classicNames.REPORTER(), jqlNames.REPORTER());
		assertFalse(classicNames.PROJECT().equals(jqlNames.PROJECT()));
	}
}
