/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui;

import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TaskRepositoryLocationUiFactory;

public class JiraTaskRepositoryLocationUiFactory extends TaskRepositoryLocationUiFactory {

	@Override
	public AbstractWebLocation createWebLocation(TaskRepository taskRepository) {
		return new JiraTaskRepositoryLocationUi(taskRepository);
	}
}
