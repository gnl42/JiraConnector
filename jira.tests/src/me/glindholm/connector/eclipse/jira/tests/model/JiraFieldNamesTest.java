package me.glindholm.connector.eclipse.jira.tests.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import me.glindholm.connector.eclipse.internal.jira.core.model.filter.JiraFieldsNames;

public class JiraFieldNamesTest  {

	@Test
	public void testClassicVsJql() {
		final var classicNames = JiraFieldsNames.createClassic();
		final var jqlNames = JiraFieldsNames.createJql();

		// just few tests if enum wrappers works
		assertEquals("version", classicNames.AFFECTED_VERSION());
		assertEquals("affectedVersion", jqlNames.AFFECTED_VERSION());
		assertEquals(classicNames.REPORTER(), jqlNames.REPORTER());
		assertFalse(classicNames.PROJECT().equals(jqlNames.PROJECT()));
	}
}
