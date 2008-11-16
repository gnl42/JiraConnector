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

package com.atlassian.theplugin.eclipse.view.popup;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;

/**
 * Encapsulates tasks that reside on a repository or local computer and participate in synchronization with the source
 * that contains their data.
 * 
 * @author Mik Kersten
 * @author Rob Elves
 * @since 2.0
 */
public abstract class AbstractTask extends AbstractTaskContainer {

	public static final String DEFAULT_TASK_KIND = "task";

	private String repositoryUrl;

	private String taskKind = DEFAULT_TASK_KIND;

	private String taskId;

	private String owner;

	private boolean active = false;

	private String summary;

	private String priority = PriorityLevel.getDefault().toString();

	private boolean completed;

	private boolean isNotifiedIncoming = false;

	private boolean reminded = false;
	
	private boolean floatingScheduledDate = false;

	private String categoryHandle = "";

	private Set<AbstractTaskContainer> containers = new HashSet<AbstractTaskContainer>();

	// ************ Synch ****************

	/** The last time this task's bug report was in a synchronized (read?) state. */
	private String lastReadTimeStamp;

	private boolean synchronizing;

	private boolean submitting;

	private RepositoryTaskSyncState synchronizationState = RepositoryTaskSyncState.SYNCHRONIZED;

	// transient
	private IStatus synchronizationStatus = null;

	private boolean stale = false;

	public enum RepositoryTaskSyncState {
		OUTGOING, SYNCHRONIZED, INCOMING, CONFLICT
	}

	// ************ Planning ****************

	private Date completionDate = null;

	private Date creationDate = null;

	private Date scheduledForDate = null;

	private Date dueDate = null;

	private String notes = "";

	private int estimatedTimeHours = 1;

	public enum PriorityLevel {
		P1, P2, P3, P4, P5;

		@Override
		public String toString() {
			switch (this) {
			case P1:
				return "P1";
			case P2:
				return "P2";
			case P3:
				return "P3";
			case P4:
				return "P4";
			case P5:
				return "P5";
			default:
				return "P3";
			}
		}

		public String getDescription() {
			switch (this) {
			case P1:
				return "Very High";
			case P2:
				return "High";
			case P3:
				return "Normal";
			case P4:
				return "Low";
			case P5:
				return "Very Low";
			default:
				return "";
			}
		}

		/** 
		 * @since 2.3
		 */
		public static PriorityLevel fromLevel(int level) {
			if (level <= 1) {
				return P1;
			}
			if (level == 2) {
				return P2;
			}
			if (level == 3) {
				return P3;
			}
			if (level == 4) {
				return P4;
			}
			if (level >= 5) {
				return P5;
			}
			return getDefault();
		}
		
		public static PriorityLevel fromString(String string) {
			if (string.equals("P1")) {
				return P1;
			}
			if (string.equals("P2")) {
				return P2;
			}
			if (string.equals("P3")) {
				return P3;
			}
			if (string.equals("P4")) {
				return P4;
			}
			if (string.equals("P5")) {
				return P5;
			}
			return getDefault();
		}

		
		public static PriorityLevel fromDescription(String string) {
			if (string == null) {
				return null;
			}
			if (string.equals("Very High")) {
				return P1;
			}
			if (string.equals("High")) {
				return P2;
			}
			if (string.equals("Normal")) {
				return P3;
			}
			if (string.equals("Low")) {
				return P4;
			}
			if (string.equals("Very Low")) {
				return P5;
			}
			return getDefault();
		}

		public static PriorityLevel getDefault() {
			return P3;
		}
	}

	public AbstractTask(String repositoryUrl, String taskId, String summary) {
		super(RepositoryTaskHandleUtil.getHandle(repositoryUrl, taskId));
		this.repositoryUrl = repositoryUrl;
		this.taskId = taskId;
		this.summary = summary;
		this.url = "";
	}

	/**
	 * Final to preserve the handle identifier format required by the framework.
	 */
	@Override
	public final String getHandleIdentifier() {
		return super.getHandleIdentifier();
	}

	/**
	 * True for tasks that can be modified without a round-trip to a server. For example, such a task can be marked
	 * completed via the Task List.
	 */
	public abstract boolean isLocal();

	// API-3.0 rename to getRepositoryKind()
	public abstract String getConnectorKind();

	public String getLastReadTimeStamp() {
		return lastReadTimeStamp;
	}

	public void setLastReadTimeStamp(String lastReadTimeStamp) {
		this.lastReadTimeStamp = lastReadTimeStamp;
	}

	public void setSynchronizationState(RepositoryTaskSyncState syncState) {
		this.synchronizationState = syncState;
	}

	public RepositoryTaskSyncState getSynchronizationState() {
		return synchronizationState;
	}

	public boolean isSynchronizing() {
		return synchronizing;
	}

	public void setSynchronizing(boolean sychronizing) {
		this.synchronizing = sychronizing;
	}

	public boolean isNotified() {
		return isNotifiedIncoming;
	}

	public void setNotified(boolean notified) {
		isNotifiedIncoming = notified;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public IStatus getSynchronizationStatus() {
		return synchronizationStatus;
	}

	public void setSynchronizationStatus(IStatus status) {
		this.synchronizationStatus = status;
	}

	public final String getTaskId() {
		return taskId;
	}

	public final String getRepositoryUrl() {
		return repositoryUrl;
	}

	@Override
	public final void setHandleIdentifier(String handleIdentifier) {
		throw new RuntimeException("Cannot set the handle identifier of a task, set repository URL instead.");
	}

	public final void setRepositoryUrl(String repositoryUrl) {
		this.repositoryUrl = repositoryUrl;
		super.setHandleIdentifier(RepositoryTaskHandleUtil.getHandle(repositoryUrl, taskId));
	}

	/**
	 * User identifiable key for the task to be used in UI facilities such as label displays and hyperlinked references.
	 * Can return the same as the ID (e.g. in the case of Bugzilla). Can return null if no such label exists.
	 */
	public String getTaskKey() {
		return taskId;
	}

	public boolean isSubmitting() {
		return submitting;
	}

	public void setSubmitting(boolean submitting) {
		this.submitting = submitting;
	}

	@Override
	public String toString() {
		return summary;
	}

	/**
	 * Package visible in order to prevent sets that don't update the index.
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractTask) {
			return this.getHandleIdentifier().equals(((AbstractTask) obj).getHandleIdentifier());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.getHandleIdentifier().hashCode();
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
		if (completed) {
			completionDate = new Date();
		} else {
			completionDate = null;
		}
	}

	@Override
	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getNotes() {
		// TODO: removed check for null once xml updated.
		if (notes == null) {
			notes = "";
		}
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public int getEstimateTimeHours() {
		return estimatedTimeHours;
	}

	public void setEstimatedTimeHours(int estimated) {
		this.estimatedTimeHours = estimated;
	}

	/**
	 * @API 3.0: Rename to internalAddParentContainer
	 */
	public void addParentContainer(AbstractTaskContainer container) {
		containers.add(container);
	}

	/**
	 * @API 3.0: Rename to internalremoveParentContainer
	 */
	public void removeParentContainer(AbstractTaskContainer container) {
		containers.remove(container);
	}

	public Set<AbstractTaskContainer> getParentContainers() {
		return new HashSet<AbstractTaskContainer>(containers);
	}

	@Override
	public String getSummary() {
		return summary;
	}

	public Date getCompletionDate() {
		return completionDate;
	}

	public void setScheduledForDate(Date date) {
		scheduledForDate = date;
	}

	public Date getScheduledForDate() {
		return scheduledForDate;
	}

	public boolean isReminded() {
		return reminded;
	}

	public void setReminded(boolean reminded) {
		this.reminded = reminded;
	}

	public Date getCreationDate() {
		if (creationDate == null) {
			creationDate = new Date();
		}
		return creationDate;
	}

	public void setCreationDate(Date date) {
		this.creationDate = date;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	/**
	 * @API-3.0: Deprecate. Use TaskActivityManager.isPastReminder(Abstract task)
	 */
	public boolean isPastReminder() {
		if (isCompleted() || scheduledForDate == null) {
			return false;
		} else {
			Date now = new Date();
			if (/*!internalIsFloatingScheduledDate() && */scheduledForDate.compareTo(now) < 0) {
				return true;
			} else {
				return false;
			}
		}
	}

	public boolean hasValidUrl() {
		String taskUrl = getUrl();
		if (taskUrl != null && !taskUrl.equals("") && !taskUrl.equals("http://") && !taskUrl.equals("https://")) {
			try {
				new URL(taskUrl);
				return true;
			} catch (MalformedURLException e) {
				return false;
			}
		}
		return false;
	}

	public String getTaskKind() {
		return taskKind;
	}

	public void setTaskKind(String kind) {
		this.taskKind = kind;
	}

	@Override
	public int compareTo(AbstractTaskContainer taskListElement) {
		return summary.compareTo(((AbstractTask) taskListElement).summary);
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date date) {
		this.dueDate = date;
	}

	public boolean isStale() {
		return stale;
	}

	public void setStale(boolean stale) {
		this.stale = stale;
	}

	/**
	 * @API 3.0: deprecate?
	 */
	public String getCategoryHandle() {
		return categoryHandle;
	}

	/**
	 * @API 3.0: deprecate?
	 */
	public void setCategoryHandle(String categoryHandle) {
		this.categoryHandle = categoryHandle;
	}

	/**
	 * @since 2.3
	 * @API 3.0: rename/move
	 */
	public boolean internalIsFloatingScheduledDate() {
		return floatingScheduledDate;
	}

	/**
	 * @since 2.3
	 * @API 3.0: rename/move
	 */
	public void internalSetFloatingScheduledDate(boolean floatingScheduledDate) {
		this.floatingScheduledDate = floatingScheduledDate;
	}

}
