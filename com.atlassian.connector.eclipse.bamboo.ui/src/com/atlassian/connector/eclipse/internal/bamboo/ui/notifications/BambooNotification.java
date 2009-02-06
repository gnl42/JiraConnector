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

package com.atlassian.connector.eclipse.internal.bamboo.ui.notifications;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooView;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.util.Date;

/**
 * A notification of a changed Bamboo Build
 * 
 * @author Thomas Ehrnhoefer
 */
public class BambooNotification extends AbstractNotification {

	private final BambooBuild build;

	private final CHANGE change;

	public enum CHANGE {
		ADDED("Build added"), REMOVED("Build removed"), CHANGED("Build changed");
		private final String txt;

		private CHANGE(String txt) {
			this.txt = txt;
		}

		public String getText() {
			return txt;
		}
	}

	public BambooNotification(BambooBuild build, CHANGE change) {
		super();
		this.build = build;
		this.change = change;
	}

	@Override
	public Date getDate() {
		return build.getBuildCompletedDate();
	}

	@Override
	public String getDescription() {
		return change.getText();
	}

	@Override
	public String getLabel() {
		return build.getBuildKey() + NLS.bind(" [{0}]", build.getServerUrl());
	}

	@Override
	public Image getNotificationImage() {
		return null;
	}

	@Override
	public Image getNotificationKindImage() {
		switch (build.getStatus()) {
		case BUILD_FAILED:
			return CommonImages.getImage(BambooImages.STATUS_FAILED);
		case BUILD_SUCCEED:
			return CommonImages.getImage(BambooImages.STATUS_PASSED);
		default:
			return CommonImages.getImage(BambooImages.STATUS_DISABLED);
		}
	}

	@Override
	public void open() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null && window.getActivePage() != null) {
					try {
						window.getActivePage().showView(BambooView.ID);
					} catch (PartInitException e) {
						StatusHandler.fail(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
								"Failed to open Bamboo View"));
					}
				}
			}
		});
	}

	@Override
	public void setDate(Date date) {
		// ignore
	}

	public int compareTo(AbstractNotification anotherNotification) {
		if (anotherNotification == null || !(anotherNotification instanceof BambooNotification)) {
			throw new ClassCastException("A BambooNotification object expected."); //$NON-NLS-1$
		}
		BambooBuild anotherBuild = ((BambooNotification) anotherNotification).getBuild();
		if (build.getBuildCompletedDate() != null && anotherBuild.getBuildCompletedDate() != null) {
			return build.getBuildCompletedDate().compareTo(anotherBuild.getBuildCompletedDate());
		} else if (build.getBuildCompletedDate() == null) {
			return -1;
		} else {
			return 1;
		}
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public BambooBuild getBuild() {
		return build;
	}

}
