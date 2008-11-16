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

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public class RunBuildAction extends AbstractBambooAction {
	
	private static final String RUN_BUILD = "Run Build";
	
	public RunBuildAction(BambooToolWindow bambooToolWindowTable) {
		super(bambooToolWindowTable);
	}
	
	
	@Override
	public void run() {
		super.run();
		
		final BambooBuildAdapterEclipse build = getBuild();
		
		Job runBuild = new Job("Starting build on plan " + build.getBuildKey()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					setUIMessage("Starting build on plan " + build.getBuildKey());
					bambooFacade.executeBuild(build.getServer(), build.getBuildKey());
					setUIMessage("Build started on plan " + build.getBuildKey());
				} catch (ServerPasswordNotProvidedException e) {
					setUIMessage("Build not started. Password not provided for server");
				} catch (RemoteApiException e) {
					setUIMessage("Build not started. " + e.getMessage());
				}
				return Status.OK_STATUS;
			}
			
		};
		
		runBuild.setPriority(Job.SHORT);
		runBuild.schedule();
	}


	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_RUN));
	}


	@Override
	public String getToolTipText() {
		return RUN_BUILD;
	}


	@Override
	public String getText() {
		return RUN_BUILD;
	}	
	
	
}
