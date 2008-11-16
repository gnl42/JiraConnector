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

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BambooStatusDisplay;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.view.popup.NotificationPopup;

public class BambooStatusTooltip implements BambooStatusDisplay {
	
	NotificationPopup popup;

	public void updateBambooStatus(BuildStatus status, BambooPopupInfo popupInfo) {
		
		if (popup != null) {
			popup.close();
		}

		Shell shell = new Shell(PlatformUI.getWorkbench().getDisplay());
		
		popup = new NotificationPopup(shell);
		popup.setContent(status, popupInfo);
		popup.open();
	}
}
