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

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.internal.jira.core.model.JiraVersion;
import org.eclipse.mylyn.internal.jira.core.model.NamedFilter;
import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.FilterDefinitionConverter;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebSession;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebSessionCallback;

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
				String version = client.getCache().getServerInfo(monitor).getVersion();
				if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_7) >= 0) {
					rssUrlBuffer.append("/sr/jira.issueviews:searchrequest-xml/").append(filter.getId()).append( //$NON-NLS-1$
							"/SearchRequest-").append(filter.getId()).append(".xml"); //$NON-NLS-1$ //$NON-NLS-2$
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("?tempMax=").append(collector.getMaxHits()); //$NON-NLS-1$
					}
				} else {
					rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&"); //$NON-NLS-1$
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&'); //$NON-NLS-1$
					}
					rssUrlBuffer.append("requestId=").append(filter.getId()); //$NON-NLS-1$
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
				String version = client.getCache().getServerInfo(monitor).getVersion();
				if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_7) >= 0) {
					rssUrlBuffer.append("/sr/jira.issueviews:searchrequest-xml/temp/SearchRequest.xml?decorator=none&reset=true&"); //$NON-NLS-1$
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&'); //$NON-NLS-1$
					}
				} else {
					rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&reset=true&"); //$NON-NLS-1$
					if (collector.getMaxHits() != IssueCollector.NO_LIMIT) {
						rssUrlBuffer.append("tempMax=").append(collector.getMaxHits()).append('&'); //$NON-NLS-1$
					}
				}
				FilterDefinitionConverter filterConverter = new FilterDefinitionConverter(
						client.getCharacterEncoding(monitor), client.getConfiguration().getDateFormat());
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
				String version = client.getCache().getServerInfo(monitor).getVersion();
				if (new JiraVersion(version).compareTo(JiraVersion.JIRA_3_7) >= 0) {
					rssUrlBuffer.append("/si/jira.issueviews:issue-xml/"); //$NON-NLS-1$
					rssUrlBuffer.append(issueKey);
					rssUrlBuffer.append("/"); //$NON-NLS-1$
					rssUrlBuffer.append(issueKey);
					rssUrlBuffer.append(".xml"); //$NON-NLS-1$
				} else {
					rssUrlBuffer.append("/browse/"); //$NON-NLS-1$
					rssUrlBuffer.append(issueKey);
					rssUrlBuffer.append("?view=rss&decorator=none&reset=true&tempMax=1"); //$NON-NLS-1$
				}
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
