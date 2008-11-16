/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.dialogs.bamboo.CommentBuildDialog;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public class CommentBuildAction extends AbstractBambooAction {

	private static final String COMMENT_BUILD = "Comment Build";

	public CommentBuildAction(BambooToolWindow bambooToolWindow) {
		super(bambooToolWindow);

		setEnabled(false); // action is disabled by default
	}

	@Override
	public void run() {
		super.run();

		final BambooBuildAdapterEclipse build = getBuild();

		CommentBuildDialog dialog = new CommentBuildDialog(Activator
				.getDefault().getShell(), build);
		dialog.open();

		if (dialog.getReturnCode() == SWT.OK
				&& dialog.getComment().length() > 0) {

			final String comment = dialog.getComment();
			final String buildDesc = build.getBuildKey() + " "
					+ build.getBuildNumber();

			Job labelBuild = new Job("Commenting build " + buildDesc) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					try {
						setUIMessage("Commenting build " + buildDesc);
						bambooFacade
								.addCommentToBuild(build.getServer(), build
										.getBuildKey(), build.getBuildNumber(),
										comment);
						setUIMessage("Build " + buildDesc + " commented");
					} catch (ServerPasswordNotProvidedException e) {
						setUIMessage("Build "
								+ buildDesc
								+ "  not commented. Password not provided for server");
					} catch (RemoteApiException e) {
						setUIMessage("Build  " + buildDesc + " not commented. "
								+ e.getMessage());
					}
					return Status.OK_STATUS;
				}
			};

			labelBuild.setPriority(Job.SHORT);
			labelBuild.schedule();
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry()
				.get(PluginIcons.ICON_BAMBOO_COMMENT));
	}

	@Override
	public String getText() {
		return COMMENT_BUILD;
	}

	@Override
	public String getToolTipText() {
		return COMMENT_BUILD;
	}

}
