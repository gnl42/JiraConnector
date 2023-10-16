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

package me.glindholm.connector.eclipse.internal.commons.ui;

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryMigrator;
import org.eclipse.mylyn.tasks.core.TaskRepository;

@SuppressWarnings("restriction")
public class CategoryAddingRepositoryMigrator extends AbstractRepositoryMigrator {

    private final String connectorKind;

    private final String targetCategory;

    public CategoryAddingRepositoryMigrator(final String connectorKind, final String targetCategory) {
        this.connectorKind = connectorKind;
        this.targetCategory = targetCategory;
    }

    @Override
    public String getConnectorKind() {
        return connectorKind;
    }

    @Override
    public boolean migrateRepository(final TaskRepository repository) {
        if (repository.getProperty(IRepositoryConstants.PROPERTY_CATEGORY) == null) {
            repository.setProperty(IRepositoryConstants.PROPERTY_CATEGORY, targetCategory);
            return true;
        }
        return false;
    }

}
