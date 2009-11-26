/*******************************************************************************
 * Copyright (c) 2004, 2008 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

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

	public void readRssFeed(InputStream feed, String baseUrl, IProgressMonitor monitor) throws JiraException,
			IOException {
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(new JiraRssHandler(client, collector, baseUrl));
			InputSource inputSource = new InputSource(feed);
			inputSource.setEncoding(client.getCharacterEncoding(monitor));
			reader.parse(inputSource);
			collector.done();
		} catch (SAXException e) {
			throw new JiraException("Error parsing server response: " + e.getMessage(), e); //$NON-NLS-1$
		}
	}
}
