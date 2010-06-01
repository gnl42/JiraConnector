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

import com.atlassian.connector.eclipse.internal.commons.ui.CategoryAddingRepositoryMigrator;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;

public class JiraRepositoryMigrator extends CategoryAddingRepositoryMigrator {
	public JiraRepositoryMigrator() {
		super(JiraCorePlugin.CONNECTOR_KIND, IRepositoryConstants.CATEGORY_BUGS);
	}
}
