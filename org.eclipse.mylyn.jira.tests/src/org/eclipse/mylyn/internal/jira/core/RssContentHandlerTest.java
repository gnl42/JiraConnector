/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.internal.jira.core.service.web.rss.RssContentHandler;

import junit.framework.TestCase;

public class RssContentHandlerTest extends TestCase {

	public void testUnescape() {
		assertEquals("\n", RssContentHandler.stripTags("\n<br/>\n"));
		assertEquals("\n\n", RssContentHandler.stripTags("\n<br/>\n<br/>\n"));
	}

	public void testHasMarkup() {
		assertFalse(RssContentHandler.hasMarkup(""));
		assertFalse(RssContentHandler.hasMarkup("abc"));
		assertFalse(RssContentHandler.hasMarkup("  "));
		assertFalse(RssContentHandler.hasMarkup("&nbsp;"));
		assertFalse(RssContentHandler.hasMarkup("<br/>"));
		assertFalse(RssContentHandler.hasMarkup("abc <br/>def"));
		assertFalse(RssContentHandler.hasMarkup("abc <a href=\"ghi\">def</a>"));
		assertFalse(RssContentHandler.hasMarkup("abc <br/> def <a href=\"ghi\">def</a>"));
		assertFalse(RssContentHandler.hasMarkup("\n<br/>\r\n"));
		
		assertTrue(RssContentHandler.hasMarkup("<br>"));
		assertTrue(RssContentHandler.hasMarkup("<li>"));
		assertTrue(RssContentHandler.hasMarkup("<b>"));
		assertTrue(RssContentHandler.hasMarkup("<li><br>"));
		assertTrue(RssContentHandler.hasMarkup("abc <br/> def <a href=\"ghi\">def</a> <br>"));
		assertTrue(RssContentHandler.hasMarkup("\n<br/>\r\n<li>"));
	}

}
