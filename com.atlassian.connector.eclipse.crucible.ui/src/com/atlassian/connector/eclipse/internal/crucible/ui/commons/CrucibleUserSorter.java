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

import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Sorter for cached users
 * 
 * @author Shawn Minto
 */
public class CrucibleUserSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		String displayName1 = null;
		String displayName2 = null;

		if (e1 instanceof User && e2 instanceof User) {
			displayName1 = ((User) e1).getDisplayName();
			displayName2 = ((User) e2).getDisplayName();
		}

		if (displayName1 != null && displayName2 != null) {
			return displayName1.compareTo(displayName2);
		}

		return super.compare(viewer, e1, e2);
	}
}
