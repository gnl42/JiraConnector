package org.eclipse.mylyn.jira.tests.ui;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.ui.WdhmUtil;

public class WdhmUtilTest extends TestCase {

	public void testIsValid() {
		assertTrue(isValid("1w"));
		assertTrue(isValid("99w"));
		assertTrue(isValid(" 99W "));
		assertTrue(isValid(" - 99W "));

		assertTrue(isValid("1d"));
		assertTrue(isValid("1D"));
		assertTrue(isValid("1w1d"));
		assertTrue(isValid("1w 1d "));
		assertTrue(isValid("1w 1d "));

		assertTrue(isValid("1h"));
		assertTrue(isValid("1H"));
		assertTrue(isValid("1w1h"));
		assertTrue(isValid(" 1w 1d 1h "));
		assertTrue(isValid(" 1d 1h"));
		assertTrue(isValid(" 1w 1h "));

		assertTrue(isValid("1m"));
		assertTrue(isValid("1M"));
		assertTrue(isValid("1w1d1h999m"));
		assertTrue(isValid("1w1d 1h 999m "));
		assertTrue(isValid("1w 1h 99m "));

		assertFalse(isValid("1h1w"));

	}

	private boolean isValid(String text) {
		return WdhmUtil.isValid(text);
	}

}
