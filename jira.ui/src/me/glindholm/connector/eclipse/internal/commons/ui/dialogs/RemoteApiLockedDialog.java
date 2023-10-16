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

package me.glindholm.connector.eclipse.internal.commons.ui.dialogs;

import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import me.glindholm.connector.eclipse.internal.commons.ui.Messages;
import me.glindholm.connector.eclipse.internal.core.CoreMessages;

public class RemoteApiLockedDialog extends ErrorDialogWithHyperlink {

    public RemoteApiLockedDialog(final Shell parentShell, final String repositoryUrl) {
        super(parentShell, Messages.RemoteApiLockedDialog_permission_denied, CoreMessages.Captcha_authentication_required, Messages.RemoteApiLockedDialog_5,
                () -> BrowserUtil.openUrl(repositoryUrl, IWorkbenchBrowserSupport.AS_EXTERNAL));
    }

}
