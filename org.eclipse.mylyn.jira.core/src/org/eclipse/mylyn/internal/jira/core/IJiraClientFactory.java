/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core;

import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Eugene Kuleshov
 */
public interface IJiraClientFactory {

	public JiraClient getJiraClient(TaskRepository repository);

}
