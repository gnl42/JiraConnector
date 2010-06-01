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

package com.atlassian.connector.eclipse.internal.commons.ui.dialogs;

import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class RemoteApiLockedDialog extends ErrorDialogWithHyperlink {

	public RemoteApiLockedDialog(Shell parentShell, final String repositoryUrl) {
		super(parentShell, "Permission Denied",
				"Due to multiple failed login attemps you have been temporarily banned from using remote API.\n\n"
						+ "You need to log into your server using Web UI to clear failed login attemps counter.\n\n"
						+ "If you're browser has a current session with the server you need to change your password"
						+ " (to make sure you know it), then log out and log in again.", "<a>Open repository</a>",
				new Runnable() {
					public void run() {
						WorkbenchUtil.openUrl(repositoryUrl, IWorkbenchBrowserSupport.AS_EXTERNAL);
					}
				});
	}

}
