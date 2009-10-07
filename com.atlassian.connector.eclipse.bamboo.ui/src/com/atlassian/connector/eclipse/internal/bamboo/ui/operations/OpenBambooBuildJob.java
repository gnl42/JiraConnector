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

package com.atlassian.connector.eclipse.internal.bamboo.ui.operations;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooCorePlugin;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditor;
import com.atlassian.connector.eclipse.internal.bamboo.ui.editor.BambooEditorInput;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenBambooBuildJob extends Job {

	private final String buildKey;

	private final int buildNumber;

	private final TaskRepository repository;

	public OpenBambooBuildJob(String buildKey, int buildNumber, TaskRepository repository) {
		super("Open Bamboo Build");

		this.buildKey = buildKey;
		this.buildNumber = buildNumber;
		this.repository = repository;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		BambooClient client = BambooCorePlugin.getRepositoryConnector().getClientManager().getClient(repository);

		final BambooBuild[] build = new BambooBuild[1];
		try {
			build[0] = client.getBuildForPlanAndNumber(monitor, repository, buildKey, buildNumber, 0);
		} catch (CoreException e) {
			return new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Failed to retrieve build details for "
					+ buildKey + " " + buildNumber, e);
		}

		if (build[0] == null) {
			return new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Failed to retrieve build details for "
					+ buildKey + " " + buildNumber);
		}

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				BambooEditorInput input = new BambooEditorInput(new EclipseBambooBuild(build[0], repository));
				try {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window == null) {
						StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
								"Failed to open Bamboo Rich Editor: no available workbench window. Please try again."));
					} else {
						window.getActivePage().openEditor(input, BambooEditor.ID);
					}
				} catch (PartInitException e) {
					StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
							"Failed to open Bamboo Rich Editor: " + e.getMessage(), e));
				}
			}
		});

		return Status.OK_STATUS;
	}
}
