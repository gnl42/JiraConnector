/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.soap;

import org.apache.commons.httpclient.HttpMethodBase;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;

/**
 * @author Steffen Pingel
 */
public class JiraRequest {

	private static final int METHOD_POLL_INTERVAL = 200;

	private static ThreadLocal<JiraRequest> currentRequest = new ThreadLocal<JiraRequest>();

	private static ThreadLocal<IProgressMonitor> currentMonitor = new ThreadLocal<IProgressMonitor>();

	public static JiraRequest getCurrentRequest() {
		return currentRequest.get();
	}

	public static void setCurrentRequest(JiraRequest request) {
		currentRequest.set(request);
	}

	public static IProgressMonitor getCurrentMonitor() {
		return currentMonitor.get();
	}

	public static void setCurrentMonitor(IProgressMonitor monitor) {
		currentMonitor.set(monitor);
	}

	private volatile HttpMethodBase method;

	private final IProgressMonitor monitor;

	private volatile boolean done;

	public JiraRequest(IProgressMonitor monitor) {
		this.monitor = monitor;

	}

	public void cancel() {
		while (method == null) {
			if (done) {
				throw new OperationCanceledException();
			}

			try {
				Thread.sleep(METHOD_POLL_INTERVAL);
			} catch (InterruptedException e) {
				throw new OperationCanceledException();
			}
		}

		method.abort();
	}

	public HttpMethodBase getMethod() {
		return method;
	}

	public IProgressMonitor getMonitor() {
		return monitor;
	}

	public void setMethod(HttpMethodBase method) {
		this.method = method;
	}

	public void done() {
		done = true;
	}

}
