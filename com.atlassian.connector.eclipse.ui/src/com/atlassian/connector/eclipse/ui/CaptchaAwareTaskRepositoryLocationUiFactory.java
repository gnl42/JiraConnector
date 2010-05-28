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

package com.atlassian.connector.eclipse.ui;

import com.atlassian.connector.eclipse.internal.core.client.ICaptchaAwareLocation;
import com.atlassian.connector.eclipse.ui.dialogs.RemoteApiLockedDialog;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.commons.net.UnsupportedRequestException;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.ui.TaskRepositoryLocationUi;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class CaptchaAwareTaskRepositoryLocationUiFactory extends TaskRepositoryLocationFactory {

	class CaptchaAwareTaskRepositoryLocaltionUi extends TaskRepositoryLocationUi implements ICaptchaAwareLocation {
		CaptchaAwareTaskRepositoryLocaltionUi(TaskRepository repo) {
			super(repo);
		}

		public void requestCaptchaAuthentication(IProgressMonitor monitor) throws UnsupportedRequestException {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					new RemoteApiLockedDialog(WorkbenchUtil.getShell(), taskRepository.getRepositoryUrl()).open();
				}
			});
			throw new UnsupportedRequestException(
					"You have been locked out of remote API. To unlock yourself use Web UI.");
		}
	}

	/**
	 * @since 3.0
	 */
	@Override
	public AbstractWebLocation createWebLocation(TaskRepository taskRepository) {
		return new CaptchaAwareTaskRepositoryLocaltionUi(taskRepository);
	}
}
