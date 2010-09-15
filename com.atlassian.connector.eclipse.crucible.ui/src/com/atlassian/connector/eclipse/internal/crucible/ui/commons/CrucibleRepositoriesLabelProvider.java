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

package com.atlassian.connector.eclipse.internal.crucible.ui.commons;

import com.atlassian.theplugin.commons.crucible.api.model.Repository;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * label provider for crucible repositories
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleRepositoriesLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		if (element instanceof Repository) {
			return ((Repository) element).getName();
		}
		return super.getText(element);
	}
}
