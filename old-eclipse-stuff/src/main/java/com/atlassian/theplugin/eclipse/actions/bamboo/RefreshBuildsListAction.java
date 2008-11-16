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

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;

public class RefreshBuildsListAction extends Action {
	
	private static final String REFRESH_BUILD_LIST = "Refresh";

	@Override
	public void run() {
		super.run();
		
		Activator.getDefault().rescheduleStatusCheckers();
		
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO use eclipse resource handling
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_BAMBOO_REFRESH));
	}

	@Override
	public String getText() {
		return REFRESH_BUILD_LIST;
	}

	@Override
	public String getToolTipText() {
		return REFRESH_BUILD_LIST;
	}
	
	
	

}
