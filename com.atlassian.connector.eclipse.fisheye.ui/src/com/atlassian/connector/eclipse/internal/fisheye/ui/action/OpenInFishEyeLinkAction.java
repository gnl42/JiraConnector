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

package com.atlassian.connector.eclipse.internal.fisheye.ui.action;

import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class OpenInFishEyeLinkAction extends AbstractFishEyeLinkAction implements IWorkbenchWindowActionDelegate {

	public OpenInFishEyeLinkAction() {
		super("Open in  FishEye");
	}

	protected void processUrl(String url) {
		TasksUiUtil.openUrl(url);
	}

}
