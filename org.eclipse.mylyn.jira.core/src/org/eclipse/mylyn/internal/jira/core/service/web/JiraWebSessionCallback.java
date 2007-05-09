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

package org.eclipse.mylar.internal.jira.core.service.web;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;

/**
 * @author Brock Janiczak
 */
public interface JiraWebSessionCallback {

	public abstract void execute(HttpClient client, JiraClient server) throws JiraException, IOException;
	
}
