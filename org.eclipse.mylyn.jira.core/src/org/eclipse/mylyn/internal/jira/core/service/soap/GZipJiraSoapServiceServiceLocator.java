/**
 * JiraSoapServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package org.eclipse.mylyn.internal.jira.core.service.soap;

import java.net.Proxy;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

import org.apache.axis.transport.http.HTTPConstants;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.JiraSoapServiceServiceLocator;


// TODO Tere must be an easier way to set these properties. Shame the Axis doco
// is so bad.
/**
 * @author	Brock Janiczak
 */
@SuppressWarnings("serial")
public class GZipJiraSoapServiceServiceLocator extends JiraSoapServiceServiceLocator {

	private Proxy proxy;
	private String httpUser;
	private String httpPassword;
	private boolean compression;
	
	public GZipJiraSoapServiceServiceLocator() {
	}

	public GZipJiraSoapServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
		super(config);
	}

	public GZipJiraSoapServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName)
			throws javax.xml.rpc.ServiceException {
		super(wsdlLoc, sName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.axis.client.Service#createCall()
	 */
	@Override
	public Call createCall() throws ServiceException {
		Call call = super.createCall();
		// JIRA does not accept compressed SOAP messages: see bug 175915
		//call.setProperty(HTTPConstants.MC_GZIP_REQUEST, Boolean.TRUE);
		if (compression) {
			call.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);
		}
		if (httpUser != null && httpPassword != null) {
			call.setProperty(JiraHttpSender.HTTP_USER, httpUser);
			call.setProperty(JiraHttpSender.HTTP_PASSWORD, httpPassword);
		}
		if (proxy != null) {
			call.setProperty(JiraHttpSender.PROXY, proxy);
		}
		
		// Some servers break with a 411 Length Required when chunked encoding
		// is used
		Map<String, Boolean> headers = new Hashtable<String, Boolean>();
		headers.put(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED, Boolean.FALSE);
		call.setProperty(HTTPConstants.REQUEST_HEADERS, headers);
		return call;
	}

	public Proxy getProxy() {
		return proxy;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public String getHttpUser() {
		return httpUser;
	}

	public void setHttpUser(String httpUser) {
		this.httpUser = httpUser;
	}

	public String getHttpPassword() {
		return httpPassword;
	}

	public void setHttpPassword(String httpPassword) {
		this.httpPassword = httpPassword;
	}

	public boolean isCompression() {
		return compression;
	}
	
	public void setCompression(boolean compression) {
		this.compression = compression;
	}
	
}
