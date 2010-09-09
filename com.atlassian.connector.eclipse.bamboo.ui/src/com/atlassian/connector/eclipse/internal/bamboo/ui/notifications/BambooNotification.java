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

import com.atlassian.connector.eclipse.internal.bamboo.core.BambooUtil;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
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

	private final TaskRepository repository;

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

	public BambooNotification(BambooBuild build, TaskRepository repository, CHANGE change) {
		super();
		this.build = build;
		this.change = change;
		this.repository = repository;
	}

	@Override
	public Date getDate() {
		return build.getCompletionDate();
	}

	@Override
	public String getDescription() {
		return change.getText();
	}

	@Override
	public String getLabel() {
		return build.getPlanKey() + NLS.bind(" [{0}]", repository.getRepositoryLabel());
	}

	@Override
	public Image getNotificationImage() {
		return null;
	}

	@Override
	public Image getNotificationKindImage() {
		switch (build.getStatus()) {
		case FAILURE:
			return CommonImages.getImage(BambooImages.STATUS_FAILED);
		case SUCCESS:
			return CommonImages.getImage(BambooImages.STATUS_PASSED);
		default:
			return CommonImages.getImage(BambooImages.STATUS_DISABLED);
		}
	}

	@Override
	public void open() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
			public void run() {
				String url = BambooUtil.getUrlFromBuild(build);
				TasksUiUtil.openUrl(url);
			}
		});
	}

	@Override
	public void setDate(Date date) {
		// ignore
	}

	public int compareTo(AbstractNotification anotherNotification) {
		if (anotherNotification == null) {
			throw new ClassCastException("A BambooNotification object expected."); //$NON-NLS-1$
		}
		Date date;
		if (anotherNotification instanceof BambooNotification) {
			date = ((BambooNotification) anotherNotification).getBuild().getCompletionDate();
		} else {
			date = anotherNotification.getDate();
		}
		if (build.getCompletionDate() != null && date != null) {
			return build.getCompletionDate().compareTo(date);
		} else if (build.getCompletionDate() == null) {
			return -1;
		} else {
			return 1;
		}
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public BambooBuild getBuild() {
		return build;
	}

}
