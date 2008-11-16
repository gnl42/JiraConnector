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

/*
 * Created on 14.11.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package com.atlassian.theplugin.eclipse.view.popup;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.atlassian.theplugin.commons.bamboo.BambooBuild;
import com.atlassian.theplugin.commons.bamboo.BambooPopupInfo;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.PluginIcons;

/**
 * @author Benjamin Pasero
 * @author Mik Kersten
 */
public class NotificationPopup extends AbstractNotificationPopup {

	private static final RGB COLOR_BUILD_FAILED = new RGB(255, 220, 220);
	private static final RGB COLOR_BUILD_SUCCEEDED = new RGB(220, 255, 220);
	private BambooPopupInfo content = new BambooPopupInfo();
	private BuildStatus status = BuildStatus.UNKNOWN;

	public NotificationPopup(Display display) {
		super(display);
	}
	
	public NotificationPopup(Shell shell) {
		super(shell.getDisplay());
	}

	protected void createTitleArea(Composite parent) {
		
		((GridData) parent.getLayoutData()).heightHint = 24;

		Label titleLabel = new Label(parent, SWT.NONE);
		titleLabel.setText("Bamboo notification");
		titleLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		titleLabel.setBackground(parent.getBackground());
		

		Label closeButton = new Label(parent, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		closeButton.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		closeButton.setImage(PluginIcons.getImageRegistry().get(PluginIcons.ICON_CLOSE));
		closeButton.addMouseListener(new MouseAdapter() {
			public void mouseUp(MouseEvent e) {
				close();
			}
		});
	}

	protected void createContentArea(Composite parent) {
		
		Color backgroundColor;
		
		switch (status) {
		case BUILD_FAILED:
			backgroundColor = new Color(Activator.getDefault().getDisplay(), COLOR_BUILD_FAILED);
			break;
		case BUILD_SUCCEED:
			backgroundColor = new Color(Activator.getDefault().getDisplay(), COLOR_BUILD_SUCCEEDED);
			break;
		default:
			backgroundColor = parent.getBackground();
		}
		
		parent.setBackground(backgroundColor);
		
		for (BambooBuild build : content.getBambooBuilds()) {
			
			String icon;
			String st;
			Color fontColor;
						
			
			switch (build.getStatus()) {
				case BUILD_SUCCEED:
					icon = PluginIcons.ICON_BAMBOO_SUCCEEDED;
					st = "succeeded";
					fontColor = Activator.getDefault().getDisplay().getSystemColor(SWT.COLOR_BLACK);
					break;
				case BUILD_FAILED:
					icon = PluginIcons.ICON_BAMBOO_FAILED;
					st = "failed";
					fontColor = Activator.getDefault().getDisplay().getSystemColor(SWT.COLOR_DARK_RED);
					break;
				default:
					icon = PluginIcons.ICON_BAMBOO_UNKNOWN;
					st = "unknown";
					fontColor = Activator.getDefault().getDisplay().getSystemColor(SWT.COLOR_BLACK);
					break;
			}
			
			Composite notificationComposite = new Composite(parent, SWT.NO_FOCUS);
			notificationComposite.setLayout(new GridLayout(2, false));
			notificationComposite.setBackground(parent.getBackground());

			Label image = new Label(notificationComposite, SWT.NO_FOCUS);
			image.setText("example build");
			image.setImage(PluginIcons.getImageRegistry().get(icon));
			image.setBackground(parent.getBackground());

			Label l2 = new Label(notificationComposite, SWT.NO_FOCUS);
			l2.setText(build.getBuildKey() + " " + build.getBuildNumber() + " " + st);
			l2.setBackground(parent.getBackground());
			//l2.setForeground(color);
			
		}

	}

	@Override
	protected String getPopupShellTitle() {
		return "Sample Notification";
	}

	public void resetState() {
	}
	
	public void setContent(BuildStatus status, BambooPopupInfo popupInfo) {
		this.status  = status;
		this.content = popupInfo;
	}
}