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
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HeaderElement;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylyn.internal.jira.core.service.JiraServiceUnavailableException;
import org.eclipse.mylyn.internal.jira.core.service.web.JiraWebSessionCallback;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
public abstract class JiraRssSessionCallback extends JiraWebSessionCallback {

	private static final int MAX_REDIRECTS = 1;

	private final boolean useCompression;

	private final IssueCollector collector;

	public JiraRssSessionCallback(boolean useCompression, IssueCollector collector) {
		this.useCompression = useCompression;
		this.collector = collector;
	}

	@Override
	public final void run(JiraClient client, String baseUrl) throws JiraException, IOException {
		String rssUrl = getRssUrl(baseUrl);
		for (int i = 0; i <= MAX_REDIRECTS; i++) {
			GetMethod rssRequest = new GetMethod(rssUrl);
			rssRequest.setFollowRedirects(false);
			if (useCompression) {
				// request compressed response, this does not guarantee it will be done
				rssRequest.setRequestHeader("Accept-Encoding", "gzip"); //$NON-NLS-1$
			}

			try {
				int code = execute(rssRequest);

				// TODO refactor, code was copied from JiraWebSession.expectRedirect()
				if (code == HttpStatus.SC_MOVED_TEMPORARILY) {
					// check if redirect was to issue page, this means only a single result was received
					Header locationHeader = rssRequest.getResponseHeader("location");
					if (locationHeader == null) {
						throw new JiraServiceUnavailableException("Invalid server response, missing redirect location");
					}
					String url = locationHeader.getValue();
					if (!url.startsWith(baseUrl + "/browse/")) {
						throw new JiraException("Server redirected to unexpected location: " + url);
					}

					rssRequest.releaseConnection();

					// request XML for single result
					rssUrl = url + "?decorator=none&view=rss"; //$NON-NLS-1$
				} else if (code != HttpStatus.SC_OK) {
					StringBuilder sb = new StringBuilder("Unexpected result code ");
					sb.append(code);
					sb.append(" while running query: ");
					sb.append(rssUrl);
					throw new JiraRemoteMessageException(sb.toString(), rssRequest.getResponseBodyAsString());
				}

				// if it still isn't an XML response, an invalid issue was entered
				if (!isXMLOrRSS(rssRequest)) {
					return;
				}

				parseResult(client, baseUrl, rssRequest);

				// success
				return;
			} finally {
				rssRequest.releaseConnection();
			}
		}

		throw new JiraException("Maximum number of query redirects exceeded: " + rssUrl);
	}

	private void parseResult(JiraClient client, String baseUrl, GetMethod rssRequest) throws IOException, JiraException {
		InputStream in = rssRequest.getResponseBodyAsStream();
		try {
			if (isResponseGZipped(rssRequest)) {
				in = new GZIPInputStream(rssRequest.getResponseBodyAsStream());
			}
			new JiraRssReader(client, collector).readRssFeed(in, baseUrl);
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO log
			}
		}
	}

	/**
	 * Determines the URL of the RSS being processed. This URL will typically be generated from a filter definition
	 * 
	 * @param baseUrl
	 *            the base URL of the repository
	 * @return The URL of the RSS feed to be processed
	 */
	protected abstract String getRssUrl(String baseUrl) throws JiraException;

	/**
	 * Determines if the response of <code>method</code> was GZip encoded
	 * 
	 * @param method
	 *            Method to determine encoding of
	 * @return <code>true</code> if the resposne was GZip encoded, <code>false</code> otherwise.
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
		for (HeaderElement element : values) {
			if (element.getName().startsWith("text/xml")) { //$NON-NLS-1$
				return true;
			}
		}

		return false;
	}
}
