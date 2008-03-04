/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
class JiraRssReader {

	private final JiraClient client;

	private final IssueCollector collector;

	public JiraRssReader(JiraClient client, IssueCollector collector) {
		this.client = client;
		this.collector = collector;
	}

	public void readRssFeed(InputStream feed, String baseUrl) throws JiraException, IOException {
		try {
			// TODO this only seems to work in J2SE 5.0
			// XMLReader reader = XMLReaderFactory.createXMLReader();

			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			XMLReader reader = factory.newSAXParser().getXMLReader();
			reader.setContentHandler(new JiraRssHandler(client, collector, baseUrl));
			InputSource inputSource = new InputSource(feed);
			inputSource.setEncoding(client.getCharacterEncoding());
			reader.parse(inputSource);
			collector.done();
		} catch (SAXException e) {
			throw new JiraException("Error parsing server response: " + e.getMessage(), e);
		} catch (ParserConfigurationException e) {
			throw new JiraException("Internal error parsing server response", e);
		}
	}
}
