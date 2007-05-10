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

package org.eclipse.mylar.internal.jira.core.service.web.rss;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.eclipse.mylar.internal.jira.core.service.web.JiraWebSessionCallback;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public abstract class RssFeedProcessorCallback implements JiraWebSessionCallback {

	private final boolean useGZipCompression;

	private final IssueCollector collector;

	public RssFeedProcessorCallback(boolean useGZipCompression, IssueCollector collector) {
		this.useGZipCompression = useGZipCompression;
		this.collector = collector;
	}

	public final void execute(HttpClient client, JiraClient server, String baseUrl) throws JiraException, IOException {
		String rssUrl = getRssUrl(baseUrl);
		GetMethod rssRequest = new GetMethod(rssUrl);
		// If there is only a single match JIRA will redirect to the issue
		// browser
		rssRequest.setFollowRedirects(true);

		// Tell the server we would like the response GZipped. This does not
		// guarantee it will be done
		if (useGZipCompression) {
			rssRequest.setRequestHeader("Accept-Encoding", "gzip"); //$NON-NLS-1$
		}

		try {
			if (collector.isCancelled()) {
				return;
			}
			client.executeMethod(rssRequest);

			// JIRA 3.4 can redirect straight to the issue browser, but not with
			// the RSS view type
			if (!isXMLOrRSS(rssRequest)) {
				rssRequest = new GetMethod(rssRequest.getURI().getURI());
				rssRequest.setQueryString("decorator=none&view=rss"); //$NON-NLS-1$
				client.executeMethod(rssRequest);

				// If it still isn't an XML response, an invalid issue was
				// entered
				if (!isXMLOrRSS(rssRequest)) {
					return;
				}
			}

			boolean isResponseGZipped = isResponseGZipped(rssRequest);

			InputStream rssFeed = null;
			try {
				rssFeed = isResponseGZipped ? new GZIPInputStream(rssRequest.getResponseBodyAsStream()) : rssRequest
						.getResponseBodyAsStream();
				new RssReader(server, collector).readRssFeed(rssFeed, baseUrl);
			} finally {
				try {
					if (rssFeed != null) {
						rssFeed.close();
					}
				} catch (IOException e) {
					// Do nothing
				}
			}
		} finally {
			rssRequest.releaseConnection();
		}
	}

	/**
	 * Determines the URL of the RSS being processed. This URL will typically be
	 * generated from a filter definition
	 *
	 * @param baseUrl the base URL of the repository
	 * @return The URL of the RSS feed to be processed
	 */
	protected abstract String getRssUrl(String baseUrl) throws JiraException;

	/**
	 * Determines if the response of <code>method</code> was GZip encoded
	 * 
	 * @param method
	 *            Method to determine encoding of
	 * @return <code>true</code> if the resposne was GZip encoded,
	 *         <code>false</code> otherwise.
	 */
	private boolean isResponseGZipped(HttpMethod method) {
		Header contentEncoding = method.getResponseHeader("Content-Encoding"); //$NON-NLS-1$
		return contentEncoding != null && "gzip".equals(contentEncoding.getValue()); //$NON-NLS-1$
	}

	private boolean isXMLOrRSS(HttpMethod method) throws HttpException {
		Header contentType = method.getResponseHeader("Content-Type"); //$NON-NLS-1$
		if (contentType == null) {
			return false;
		}

		HeaderElement[] values = contentType.getElements();
		for (int i = 0; i < values.length; i++) {
			HeaderElement element = values[i];
			if (element.getName().startsWith("text/xml")) { //$NON-NLS-1$
				return true;
			}
		}

		return false;
	}
}
