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

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.internal.jira.core.service.JiraClient;

/**
 * @author Brock Janiczak
 */
public interface JiraClientListener {

	public abstract void clientAdded(JiraClient server);

	public abstract void clientRemoved(JiraClient server);
}
