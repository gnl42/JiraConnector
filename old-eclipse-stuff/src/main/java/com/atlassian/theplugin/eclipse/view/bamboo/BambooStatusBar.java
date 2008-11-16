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

package com.atlassian.theplugin.eclipse.view.bamboo;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.StatusLineContributionItem;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;


public class BambooStatusBar extends StatusLineContributionItem implements BambooStatusDisplay {
	
	public BambooStatusBar() {
		super(Activator.PLUGIN_ID + ".statusline");

		setImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_UNKNOWN));
		//setToolTipText("PAZU");
		
		setActionHandler(new Action() {
			public void run() {
				try {
					IWorkbenchPage page = 
						Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage();
					
					//if (page.isPartVisible(n))
					
					page.showView(Activator.PLUGIN_ID + ".view.bamboo.BambooToolWindow");
				} catch (PartInitException e) {
					e.printStackTrace();
				} 
			}
		});
	}

	public void updateBambooStatus(BuildStatus generalBuildStatus,
			BambooPopupInfo info) {
		switch(generalBuildStatus) {
		case BUILD_FAILED:
			setImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_FAILED));
			setToolTipText("Some builds failed. Click to see details.");
			break;
		case BUILD_SUCCEED:
			setImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_SUCCEEDED));
			setToolTipText("All builds currently passing.");
			break;
		case UNKNOWN:
		default:
			setImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_UNKNOWN));
			setToolTipText("");
			break;
		}
		
		
	}
}
