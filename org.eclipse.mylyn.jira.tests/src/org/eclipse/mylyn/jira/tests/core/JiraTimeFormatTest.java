/*******************************************************************************
 * Copyright (c) 2004, 2009 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.core;

import java.text.ParseException;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.service.JiraTimeFormat;

/**
 * @author Eugene Kuleshov
 */
public class JiraTimeFormatTest extends TestCase {

	private final static long M = 60;

	private final static long H = 60 * M;

	private final static long D = 24 * H;

	private final static long W = 7 * D;

	public void testFormat() {
		JiraTimeFormat f = new JiraTimeFormat();

		assertEquals("", f.format(""));
		assertEquals("", f.format("abc"));
		assertEquals("0m", f.format(1));
		assertEquals("1m", f.format(60));
		assertEquals("30m", f.format(60 * 30));
		assertEquals("1h", f.format(60 * 60));
		assertEquals("1h 30m", f.format(60 * 90));
		assertEquals("1w 2d 3h 4m", f.format(W + 2 * D + 3 * H + 4 * M));
	}

	public void testParse() throws Exception {
		JiraTimeFormat f = new JiraTimeFormat();

		try {
			assertEquals(0L, f.parse(""));
			fail("Parsing should have failed");
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
		}
		try {
			assertEquals(0L, f.parse("1"));
			fail("Parsing should have failed");
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
		}
		try {
			assertEquals(0L, f.parse("0g"));
			fail("Parsing should have failed");
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
		}
		assertEquals(0L, f.parse("0m"));
		assertEquals(0L, f.parse("0"));
		assertEquals(60L, f.parse("1m"));
		assertEquals(60L * 30, f.parse("30m"));
	}

	public void testParseObject() throws Exception {
		JiraTimeFormat f = new JiraTimeFormat();

		//valid formats
		assertEquals(0L, f.parseObject("0"));
		assertEquals(0L, f.parseObject("0m"));
		assertEquals(60L, f.parseObject("1m"));
		assertEquals(60L * 30, f.parseObject("30m"));
		assertEquals(60L * 60, f.parseObject("60m"));
		assertEquals(60L * 60, f.parseObject("1h"));
		assertEquals(60L * 90, f.parseObject("1h 30m"));
		assertEquals(60L * 90, f.parseObject("1h   30m"));
		assertEquals(120L, f.parseObject("1m1m"));
		assertEquals(120L, f.parseObject("1m 1m"));
		assertEquals(60L * 60 * 25, f.parseObject("1d 1h"));
		assertEquals(60L * 60 * 25 + 60, f.parseObject("1d 1h 1m"));
		assertEquals(60L * 60 * 25 + 60, f.parseObject("1d   1h      1m"));
		assertEquals(60L * 60 * 25 + 60, f.parseObject("1d1h1m"));
		assertEquals(60L * 60 * 24 * 7, f.parseObject("1w"));
		assertEquals(60L * (60 * 24 * 7 + 60 * 24 + 61), f.parseObject("1w 1d 1h 1m"));
		assertEquals(60L * 60 * 25 + 60, f.parseObject("1m 1d 1h")); //only allow w d h m ordering
		assertEquals(60L * (60 * 24 * 7 + 60 * 24 + 61), f.parseObject("1h 1w 1m 1d")); //only allow w d h m ordering

		//invalid formats
		try {
			assertEquals(null, f.parseObject(""));
			fail("Parsing should have failed");
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(0, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("0g"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(1, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1mm"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(2, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("0 1m"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(1, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1 1m"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(1, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("0h 1"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(0, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1h 1"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(0, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1mq"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(2, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1q"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(1, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("1w foo 1h"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(3, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("foo1m"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(0, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("foo 1m"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(0, ((ParseException) e).getErrorOffset());
		}
		try {
			assertEquals(null, f.parseObject("12d foo bar 5m"));
		} catch (Exception e) {
			assertTrue(e instanceof ParseException);
			assertEquals(4, ((ParseException) e).getErrorOffset());
		}
	}
}
