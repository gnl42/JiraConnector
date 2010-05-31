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

package com.atlassian.connector.eclipse.commons.internal.ui;

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;
import org.eclipse.mylyn.tasks.core.TaskRepository;

@SuppressWarnings("restriction")
public class CategoryAddingRepositoryMigrator extends AbstractRepositoryMigrator {

	private final String connectorKind;

	private final String targetCategory;

	public CategoryAddingRepositoryMigrator(String connectorKind, String targetCategory) {
		this.connectorKind = connectorKind;
		this.targetCategory = targetCategory;
	}

	@Override
	public String getConnectorKind() {
		return connectorKind;
	}

	@SuppressWarnings("restriction")
	@Override
	public boolean migrateRepository(TaskRepository repository) {
		if (repository.getProperty(IRepositoryConstants.PROPERTY_CATEGORY) == null) {
			repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, targetCategory);
			return true;
		}
		return false;
	}

}
