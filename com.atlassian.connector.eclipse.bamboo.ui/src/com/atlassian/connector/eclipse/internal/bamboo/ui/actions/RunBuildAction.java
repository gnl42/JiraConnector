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

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooConstants;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RunBuildJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.jetbrains.annotations.Nullable;

/**
 * Action for invoking a build run on the server
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
public class RunBuildAction extends EclipseBambooBuildSelectionListenerAction {

	private final Action action;

	/**
	 * 
	 * @param action
	 *            optional action to run (may be null)
	 */
	public RunBuildAction(@Nullable Action action) {
		super(null);
		this.action = action;
		initialize();
	}

	private void initialize() {
		setText(BambooConstants.RUN_BUILD_ACTION_LABEL);
		setToolTipText(BambooConstants.RUN_BUILD_ACTION_TOOLTIP);
		setImageDescriptor(BambooImages.RUN_BUILD);
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		final BambooBuild build = eclipseBambooBuild.getBuild();
		final RunBuildJob job = new RunBuildJob(build, eclipseBambooBuild.getTaskRepository());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (action != null) {
					action.run();
				}
				if (event.getResult().getCode() == IStatus.ERROR) {
					Display.getDefault().syncExec(new Runnable() {
						public void run() {
							MessageDialog.openError(null, getText(), "Running build " + build.getPlanKey()
									+ " failed. See Error Log for details.");
						}
					});
				}
			}
		});
		job.schedule();
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		return eclipseBambooBuild.getBuild().getEnabled();
	}
}