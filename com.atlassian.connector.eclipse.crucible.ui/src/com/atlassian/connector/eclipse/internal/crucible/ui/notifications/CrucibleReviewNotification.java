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

package com.atlassian.connector.eclipse.internal.crucible.ui.notifications;

import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.theplugin.commons.crucible.api.model.notification.CrucibleNotification;

import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotification;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonImages;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A custom crucible change notification
 * 
 * @author Shawn Minto
 */
public class CrucibleReviewNotification extends AbstractNotification {

//	private final DecoratingLabelProvider labelProvider = new DecoratingLabelProvider(
//			new TaskElementLabelProvider(true), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator());

	private Date date;

	private String description;

	private final String label;

	private boolean isNew;

	private final String repositoryUrl;

	private final String taskId;

	public CrucibleReviewNotification(String repositoryUrl, String taskId, String label,
			List<CrucibleNotification> notifications) {
		if (notifications == null) {
			description = "New review";
			isNew = true;
		} else {
			Set<String> descriptions = new HashSet<String>();
			isNew = false;
			description = "";
			for (CrucibleNotification notification : notifications) {
				if (!descriptions.contains(notification.getPresentationMessage())) {
					descriptions.add(notification.getPresentationMessage());
					description += notification.getPresentationMessage() + "\n";
				}
			}
		}
		this.repositoryUrl = repositoryUrl;
		this.taskId = taskId;
		this.label = label;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void open() {
		PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {

			public void run() {
				TaskRepository taskRepository = CrucibleUiUtil.getCrucibleTaskRepository(repositoryUrl);
				if (taskRepository != null) {
					ITask task = CrucibleUiUtil.getCrucibleTask(taskRepository, taskId);
					if (task != null) {
						TasksUiInternal.refreshAndOpenTaskListElement(task);
					}
				}
			}
		});
	}

	@Override
	public Image getNotificationImage() {
		return CrucibleImages.getImage(CrucibleImages.CRUCIBLE);
	}

	@Override
	public Image getNotificationKindImage() {
		if (isNew) {
			return CommonImages.getImage(CommonImages.OVERLAY_SYNC_INCOMMING_NEW);
		} else {
			return CommonImages.getImage(CommonImages.OVERLAY_SYNC_INCOMMING);
		}
	}

	@Override
	public void setDate(Date date) {
		this.date = date;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((date == null) ? 0 : date.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof CrucibleReviewNotification)) {
			return false;
		}
		final CrucibleReviewNotification other = (CrucibleReviewNotification) obj;
		if (date == null) {
			if (other.date != null) {
				return false;
			}
		} else if (!date.equals(other.date)) {
			return false;
		}
		if (description == null) {
			if (other.description != null) {
				return false;
			}
		} else if (!description.equals(other.description)) {
			return false;
		}
		if (label == null) {
			if (other.label != null) {
				return false;
			}
		} else if (!label.equals(other.label)) {
			return false;
		}
		return true;
	}

	public int compareTo(AbstractNotification anotherNotification) {
		if (!(anotherNotification != null)) {
			throw new ClassCastException("A ITaskListNotification object expected."); //$NON-NLS-1$
		}
		Date anotherDate = (anotherNotification).getDate();
		if (date != null && anotherDate != null) {
			return date.compareTo(anotherDate);
		} else if (date == null) {
			return -1;
		} else {
			return 1;
		}
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

}
