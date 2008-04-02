/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.net.URL;

import org.apache.axis.MessageContext;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.eclipse.mylyn.web.core.AbstractWebLocation;
import org.eclipse.mylyn.web.core.WebUtil;

/**
 * @author Steffen Pingel
 */
public class JiraHttpSender extends CommonsHttpSender {

	private static final long serialVersionUID = 1L;

	public static final String PROXY = "org.eclipse.mylyn.jira.proxy";

	public static final String HTTP_USER = "org.eclipse.mylyn.jira.httpUser";

	public static final String HTTP_PASSWORD = "org.eclipse.mylyn.jira.httpPassword";

	public static final String LOCATION = "org.eclipse.mylyn.jira.location";

	protected static final String USER_AGENT = "JiraConnector Apache Axis/1.3";

	@Override
	protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext context, URL url) {
		AbstractWebLocation location = (AbstractWebLocation) context.getProperty(LOCATION);
		JiraRequest request = JiraRequest.getCurrentRequest();
		WebUtil.configureHttpClient(client, USER_AGENT);
		return WebUtil.createHostConfiguration(client, location, request.getMonitor());
	}

	@Override
	protected void addContextInfo(HttpMethodBase method, HttpClient httpClient, MessageContext msgContext, URL tmpURL)
			throws Exception {
		super.addContextInfo(method, httpClient, msgContext, tmpURL);

		JiraRequest request = JiraRequest.getCurrentRequest();
		request.setMethod(method);
	}
}
