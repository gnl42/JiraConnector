/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.net.Proxy;
import java.net.URL;

import org.apache.axis.MessageContext;
import org.apache.axis.transport.http.CommonsHTTPSender;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylyn.core.net.WebClientUtil;

@SuppressWarnings("serial")
public class JiraHttpSender extends CommonsHTTPSender {
	
	public static final String PROXY = "org.eclipse.mylyn.jira.proxy";
	public static final String HTTP_USER = "org.eclipse.mylyn.jira.httpUser";
	public static final String HTTP_PASSWORD = "org.eclipse.mylyn.jira.httpPassword";
	
	@Override
	protected HostConfiguration getHostConfiguration(HttpClient client, MessageContext context, URL url) {
		Proxy proxy = (Proxy) context.getProperty(PROXY);
		String httpUser = (String) context.getProperty(HTTP_USER);
		String httpPassword = (String) context.getProperty(HTTP_PASSWORD);
		WebClientUtil.setupHttpClient(client, proxy, url.toString(), httpUser, httpPassword);
		return client.getHostConfiguration();
	}
}
