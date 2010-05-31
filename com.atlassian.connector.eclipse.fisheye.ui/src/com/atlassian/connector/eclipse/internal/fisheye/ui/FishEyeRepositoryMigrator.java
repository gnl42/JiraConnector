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

package com.atlassian.connector.eclipse.internal.fisheye.ui;

import com.atlassian.connector.eclipse.commons.internal.ui.CategoryAddingRepositoryMigrator;
import com.atlassian.connector.eclipse.internal.fisheye.core.FishEyeCorePlugin;

import org.eclipse.mylyn.internal.tasks.core.IRepositoryConstants;

public class FishEyeRepositoryMigrator extends CategoryAddingRepositoryMigrator {
	public FishEyeRepositoryMigrator() {
		super(FishEyeCorePlugin.CONNECTOR_KIND, IRepositoryConstants.CATEGORY_OTHER);
	}
}
