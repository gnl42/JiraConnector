/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
class RssReader {
	
	private final JiraClient server;

	private final IssueCollector collector;

	public RssReader(JiraClient server, IssueCollector collector) {
		this.server = server;
		this.collector = collector;
	}

	public void readRssFeed(InputStream feed, String baseUrl) throws JiraException, IOException {
		try {
			// TODO this only seems to work in J2SE 5.0
			// XMLReader reader = XMLReaderFactory.createXMLReader();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XMLReader reader = factory.newSAXParser().getXMLReader();
			reader.setContentHandler(new RssContentHandler(server, collector, baseUrl));
			reader.parse(new InputSource(feed));
			collector.done();
		} catch (ParseCancelledException e) {
			// User requested this action, so don't log anything
		} catch (SAXException e) {
			throw new JiraException("Error parsing server response: " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new JiraException("Internal error parsing server response", e);
		}
	}
}
