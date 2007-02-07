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

package org.eclipse.mylar.internal.jira.core.service.soap;

import java.net.Proxy;

import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.eclipse.mylar.core.net.SslProtocolSocketFactory;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.JiraService;
import org.eclipse.mylar.internal.jira.core.service.JiraServiceFactory;

/**
 * @author Brock Janiczak
 */
@SuppressWarnings("restriction")
public class SoapJiraServiceFactory implements JiraServiceFactory {

	static {
		// XXX need org.eclipse.mylar.internal.tasks.core.SslProtocolSocketFactory(proxy)
		 Proxy proxy = null;
		 Protocol.registerProtocol("https", new Protocol("https",
				(ProtocolSocketFactory) new SslProtocolSocketFactory(proxy), 443));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.mylar.internal.jira.core.service.JiraServiceFactory#createService(org.eclipse.mylar.internal.jira.core.service.JiraServer)
	 */
	public JiraService createService(JiraServer server) {
		return new SoapJiraService(server);
	}

}
