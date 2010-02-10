/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.client;

import junit.framework.TestCase;

import com.atlassian.connector.eclipse.internal.jira.core.service.web.rss.JiraRssHandler;

/**
 * @author Steffen Pingel
 */
public class JiraRssHandlerTest extends TestCase {

	public void testUnescape() {
		assertEquals("\n", JiraRssHandler.stripTags("\n<br/>\n"));
		assertEquals("\n\n", JiraRssHandler.stripTags("\n<br/>\n<br/>\n"));
	}

	public void testHasMarkup() {
		assertFalse(JiraRssHandler.hasMarkup(""));
		assertFalse(JiraRssHandler.hasMarkup("abc"));
		assertFalse(JiraRssHandler.hasMarkup("  "));
		assertFalse(JiraRssHandler.hasMarkup("&nbsp;"));
		assertFalse(JiraRssHandler.hasMarkup("<br/>"));
		assertFalse(JiraRssHandler.hasMarkup("abc <br/>def"));
		assertFalse(JiraRssHandler.hasMarkup("abc <a href=\"ghi\">def</a>"));
		assertFalse(JiraRssHandler.hasMarkup("abc <br/> def <a href=\"ghi\">def</a>"));
		assertFalse(JiraRssHandler.hasMarkup("\n<br/>\r\n"));

		assertTrue(JiraRssHandler.hasMarkup("<br>"));
		assertTrue(JiraRssHandler.hasMarkup("<li>"));
		assertTrue(JiraRssHandler.hasMarkup("<b>"));
		assertTrue(JiraRssHandler.hasMarkup("<li><br>"));
		assertTrue(JiraRssHandler.hasMarkup("abc <br/> def <a href=\"ghi\">def</a> <br>"));
		assertTrue(JiraRssHandler.hasMarkup("\n<br/>\r\n<li>"));
	}

}
