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
package org.eclipse.mylar.jira.core.internal;

import org.eclipse.mylar.jira.core.internal.service.JiraServer;

public interface JiraServerListener {

	public abstract void serverAdded(JiraServer server);

	public abstract void serverRemoved(JiraServer server);
}
