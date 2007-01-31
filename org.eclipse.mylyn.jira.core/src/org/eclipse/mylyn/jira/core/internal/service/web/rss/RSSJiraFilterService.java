/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.service.web.rss;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.eclipse.mylar.jira.core.internal.model.NamedFilter;
import org.eclipse.mylar.jira.core.internal.model.filter.FilterDefinition;
import org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector;
import org.eclipse.mylar.jira.core.internal.service.JiraServer;
import org.eclipse.mylar.jira.core.internal.service.web.JiraWebSession;

// TODO there is a mutual dependency on between this and the jira server
// I don't think there is any way to avoid it though
public class RSSJiraFilterService {

	private final JiraServer server;

	private final boolean useGZipCompression;

	public RSSJiraFilterService(JiraServer server) {
		this.server = server;
		this.useGZipCompression = server.hasSlowConnection();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraFilterService#findIssues(com.gbst.jira.core.filter.FilterDefinition,
	 *      com.gbst.jira.core.filter.IssueCollector)
	 */
	public void findIssues(final FilterDefinition filterDefinition, final IssueCollector collector) {
		// TODO make the callback a full class and pass in the filter and
		// collector
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RSSFeedProcessorCallback(useGZipCompression, collector) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.jira.core.internal.service.web.rss.RSSFeedProcessorCallback#getRssUrl()
			 */
			protected String getRssUrl() {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&reset=true&");

				if (server.getMaximumNumberOfMatches() != JiraServer.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(server.getMaximumNumberOfMatches()).append('&');
				}

				rssUrlBuffer.append(RSSJiraFilterConverterFactory.getConverter(server).convert(filterDefinition));

				return rssUrlBuffer.toString();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gbst.jira.core.service.JiraFilterService#executeNamedFilter(com.gbst.jira.core.model.NamedFilter,
	 *      com.gbst.jira.core.model.filter.IssueCollector)
	 */
	public void executeNamedFilter(final NamedFilter filter, final IssueCollector collector) {
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RSSFeedProcessorCallback(useGZipCompression, collector) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.jira.core.internal.service.web.rss.RSSFeedProcessorCallback#getRssUrl()
			 */
			protected String getRssUrl() {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/IssueNavigator.jspa?view=rss&decorator=none&");

				if (server.getMaximumNumberOfMatches() != JiraServer.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(server.getMaximumNumberOfMatches()).append('&');
				}
				rssUrlBuffer.append("requestId=").append(filter.getId());

				return rssUrlBuffer.toString();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.jira.core.internal.service.JiraFilterService#quickSearch(java.lang.String,
	 *      org.eclipse.mylar.jira.core.internal.model.filter.IssueCollector)
	 */
	public void quickSearch(final String searchString, IssueCollector collector) {
		JiraWebSession session = new JiraWebSession(server);

		session.doInSession(new RSSFeedProcessorCallback(useGZipCompression, collector) {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.jira.core.internal.service.web.rss.RSSFeedProcessorCallback#getRssUrl()
			 */
			protected String getRssUrl() {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/QuickSearch.jspa?view=rss&decorator=none&reset=true&");

				if (server.getMaximumNumberOfMatches() != JiraServer.NO_LIMIT) {
					rssUrlBuffer.append("tempMax=").append(server.getMaximumNumberOfMatches()).append('&');
				}

				try {
					rssUrlBuffer.append("searchString=").append(URLEncoder.encode(searchString, "UTF-8")); //$NON-NLS-1$
				} catch (UnsupportedEncodingException e) {
					// System must support UTF-8
				}

				return rssUrlBuffer.toString();
			}
		});

	}
}
