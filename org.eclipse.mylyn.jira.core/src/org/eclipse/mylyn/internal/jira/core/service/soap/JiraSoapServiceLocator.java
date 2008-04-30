/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * JiraSoapServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

import org.apache.axis.transport.http.HTTPConstants;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.JiraSoapServiceServiceLocator;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 */
@SuppressWarnings("serial")
public class JiraSoapServiceLocator extends JiraSoapServiceServiceLocator {

	private AbstractWebLocation location;

	private boolean compression;

	public JiraSoapServiceLocator() {
	}

	public JiraSoapServiceLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public JiraSoapServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	@Override
	public Call createCall() throws ServiceException {
		Call call = super.createCall();
		// JIRA does not accept compressed SOAP messages: see bug 175915
		//call.setProperty(HTTPConstants.MC_GZIP_REQUEST, Boolean.TRUE);
		if (compression) {
			call.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
		}

//		WebCredentials credentials = location.getCredentials(WebCredentials.Type.HTTP);
//		if (credentials != null) {
//			call.setProperty(JiraHttpSender.HTTP_USER, credentials.getUserName());
//			call.setProperty(JiraHttpSender.HTTP_PASSWORD, credentials.getPassword());
//		}
//
//		Proxy proxy = location.getProxyForHost(WebClientUtil.getDomain(location.getUrl()), IProxyData.HTTP_PROXY_TYPE);
//		if (proxy != null) {
//			call.setProperty(JiraHttpSender.PROXY, proxy);
//		}
		call.setProperty(JiraHttpSender.LOCATION, location);

		// some servers break with a 411 Length Required when chunked encoding
		// is used
		Map<String, Boolean> headers = new Hashtable<String, Boolean>();
		headers.put(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED, Boolean.FALSE);
		call.setProperty(HTTPConstants.REQUEST_HEADERS, headers);
		return call;
	}

	public boolean isCompression() {
		return compression;
	}

	public void setCompression(boolean compression) {
		this.compression = compression;
	}

	public AbstractWebLocation getLocation() {
		return location;
	}

	public void setLocation(AbstractWebLocation location) {
		this.location = location;
	}

}
