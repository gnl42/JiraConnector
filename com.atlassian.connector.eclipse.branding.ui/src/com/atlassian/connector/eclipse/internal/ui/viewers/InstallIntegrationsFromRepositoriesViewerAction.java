/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.ui.viewers;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.atlassian.connector.eclipse.internal.ui.AtlassianBundlesInfo;
import com.atlassian.connector.eclipse.internal.ui.IBrandingConstants;

public class InstallIntegrationsFromRepositoriesViewerAction implements IViewActionDelegate {

	public void init(IViewPart view) {

	}

	public void run(IAction action) {
		WorkbenchUtil.openUrl(IBrandingConstants.INSTALLATION_GUIDE_URL);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(AtlassianBundlesInfo.isOnlyJiraInstalled());
	}

}
