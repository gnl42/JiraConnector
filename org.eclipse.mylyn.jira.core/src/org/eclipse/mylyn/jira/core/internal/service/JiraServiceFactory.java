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
package org.eclipse.mylar.jira.core.internal.service;

/**
 * Factory class for creating ta Jira Service imlementation. TODO finish
 * documenting this. Explain the extension point.
 */
public interface JiraServiceFactory {
	/**
	 * Create a new service that will communicate to the supplied
	 * <code>server</code>. How the service talks to the server is up to the
	 * implementation.
	 * 
	 * @param server
	 *            Jira Server instance to communicate with
	 * @return Configured Jira Service
	 */
	public abstract JiraService createService(JiraServer server);
}
