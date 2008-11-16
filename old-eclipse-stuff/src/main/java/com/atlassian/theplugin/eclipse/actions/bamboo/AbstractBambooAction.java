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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.util.PluginUtil;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public abstract class AbstractBambooAction extends Action {
	protected BambooServerFacade bambooFacade;
	protected BambooToolWindow bambooToolWindow;
	
	public AbstractBambooAction(BambooToolWindow bambooToolWindowTable) {
		
		this.bambooToolWindow = bambooToolWindowTable;
		
		bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		
		setEnabled(false);	// action is disabled by default
	}
	
	/**
	 * Sets message in UI status bar. Should be called from within non-UI thread.
	 * @param message text to show in status bar
	 */
	protected void setUIMessage(final String message) {
		EclipseActionScheduler.getInstance().invokeLater(new Runnable() {

			public void run() {
				bambooToolWindow.setStatusBarText(message);
			}
		});
	}

	/**
	 * 
	 * @return build selected in bamboo table associated with the current action
	 */
	protected BambooBuildAdapterEclipse getBuild() {
		IStructuredSelection selection = 
			(IStructuredSelection) bambooToolWindow.getBambooToolWindowContent().getTableViewer().getSelection();
		final BambooBuildAdapterEclipse build = (BambooBuildAdapterEclipse) selection.getFirstElement();
		return build;
	}
	
	public abstract String getToolTipText();
	
	public abstract ImageDescriptor getImageDescriptor();

}
