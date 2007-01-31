/**
 * JiraSoapServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.2RC3 Feb 28, 2005 (10:15:14 EST) WSDL2Java emitter.
 */

package org.eclipse.mylar.jira.core.internal.service.soap;

import java.util.Hashtable;
import java.util.Map;

import javax.xml.rpc.Call;
import javax.xml.rpc.ServiceException;

import org.apache.axis.transport.http.HTTPConstants;
import org.eclipse.mylar.jira.core.internal.soap.JiraSoapServiceServiceLocator;


// TODO Tere must be an easier way to set these properties. Shame the Axis doco
// is so bad.
public class GZipJiraSoapServiceServiceLocator extends JiraSoapServiceServiceLocator {

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
	public Call createCall() throws ServiceException {
		Call call = super.createCall();
		// TODO allow this to be turned on. It does not match up with the option
		// in Jira
		// call.setProperty(HTTPConstants.MC_GZIP_REQUEST, Boolean.TRUE);
		call.setProperty(HTTPConstants.MC_ACCEPT_GZIP, Boolean.TRUE);

		// Some servers break with a 411 Length Required when chunked encoding
		// is used
		Map headers = new Hashtable();
		headers.put(HTTPConstants.HEADER_TRANSFER_ENCODING_CHUNKED, Boolean.FALSE);
		call.setProperty(HTTPConstants.REQUEST_HEADERS, headers);
		return call;
	}
}
