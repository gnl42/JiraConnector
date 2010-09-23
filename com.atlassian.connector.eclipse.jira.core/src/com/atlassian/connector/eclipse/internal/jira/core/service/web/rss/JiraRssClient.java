/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.web.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.connector.eclipse.internal.jira.core.model.NamedFilter;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import com.atlassian.connector.eclipse.internal.jira.core.model.filter.IssueCollector;
import com.atlassian.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebSession;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.JiraWebSessionCallback;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public class JiraRssClient {

	private final JiraClient client;

	private final JiraWebSession session;

	public JiraRssClient(JiraClient client, JiraWebSession session) {
		this.client = client;
		this.session = session;
	}

	private void doInSession(IProgressMonitor monitor, JiraWebSessionCallback callback) throws JiraException {
		session.doInSession(callback, monitor);
	}

	public void executeNamedFilter(final NamedFilter filter, final IssueCollector collector,
			final IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraRssSessionCallback(client.isCompressionEnabled(), collector) {
			@Override
			protected String getRssUrl(String baseUrl) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/sr/jira.issueviews:searchrequest-xml/").append(filter.getId()).append( //$NON-NLS-1$
						"/SearchRequest-").append(filter.getId()).append(".xml"); //$NON-NLS-1$ //$NON-NLS-2$
				if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
					rssUrlBuffer.append("?tempMax=").append(collector.getMaxHits()); //$NON-NLS-1$
				}
				return rssUrlBuffer.toString();
			}
		});
	}

	public void findIssues(final FilterDefinition filterDefinition, final IssueCollector collector,
			final IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraRssSessionCallback(client.isCompressionEnabled(), collector) {
			@Override
			protected String getRssUrl(String baseUrl) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?decorator=none&reset=true&"); //$NON-NLS-1$
				if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&'); //$NON-NLS-1$
				}
				FilterDefinitionConverter filterConverter = new FilterDefinitionConverter(
						client.getCharacterEncoding(monitor), client.getLocalConfiguration().getDateFormat());
				rssUrlBuffer.append(filterConverter.getQueryParams(filterDefinition));

				return rssUrlBuffer.toString();
			}
		});
	}

	public void getIssueByKey(final String issueKey, final IssueCollector collector, final IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraRssSessionCallback(client.isCompressionEnabled(), collector) {
			@Override
			protected String getRssUrl(String baseUrl) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/si/jira.issueviews:issue-xml/"); //$NON-NLS-1$
				rssUrlBuffer.append(issueKey);
				rssUrlBuffer.append("/"); //$NON-NLS-1$
				rssUrlBuffer.append(issueKey);
				rssUrlBuffer.append(".xml"); //$NON-NLS-1$
				return rssUrlBuffer.toString();
			}
		});
	}

	public void quickSearch(final String searchString, final IssueCollector collector, IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraRssSessionCallback(client.isCompressionEnabled(), collector) {
			@Override
			protected String getRssUrl(String baseUrl) {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/QuickSearch.jspa?view=rss&decorator=none&reset=true&"); //$NON-NLS-1$

				if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&'); //$NON-NLS-1$
				}

				try {
					rssUrlBuffer.append("searchString=").append(URLEncoder.encode(searchString, "UTF-8")); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (UnsupportedEncodingException e) {
					throw new RuntimeException("Unexpected error encoding search query", e); //$NON-NLS-1$
				}

				return rssUrlBuffer.toString();
			}
		});
	}

}
