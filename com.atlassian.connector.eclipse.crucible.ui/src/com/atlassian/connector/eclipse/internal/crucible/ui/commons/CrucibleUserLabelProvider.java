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

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * Label provider for cached users
 * 
 * @author Shawn Minto
 */
public class CrucibleUserLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
		if (element instanceof User) {
			return CrucibleUiUtil.getDisplayNameOrUsername((User) element);
		}
		return super.getText(element);
	}
}
