/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.ui;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.core.JiraTimeFormat;

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

		assertEquals(0L, f.parse(""));
		assertEquals(0L, f.parse("0m"));
		assertEquals(60L, f.parse("1m"));
		assertEquals(60L * 30, f.parse("30m"));
	}

	public void testParseObject() throws Exception {
		JiraTimeFormat f = new JiraTimeFormat();

		assertEquals(0L, f.parseObject(""));
		assertEquals(0L, f.parseObject("0m"));
		assertEquals(60L, f.parseObject("1m"));
		assertEquals(60L * 30, f.parseObject("30m"));
		assertEquals(60L * 60, f.parseObject("60m"));
		assertEquals(60L * 60, f.parseObject("1h"));
		assertEquals(60L * 90, f.parseObject("1h 30m"));
		assertEquals(60L * 60 * 25, f.parseObject("1d 1h"));
		assertEquals(60L * 60 * 25 + 60, f.parseObject("1d 1h 1m"));
		assertEquals(60L * 60 * 24 * 7, f.parseObject("1w"));
		assertEquals(60L * (60 * 24 * 7 + 60 * 24 + 61), f.parseObject("1w 1d 1h 1m"));
	}

}
