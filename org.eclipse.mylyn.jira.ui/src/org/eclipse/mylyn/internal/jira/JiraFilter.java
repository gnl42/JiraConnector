/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.mylar.internal.core.util.MylarStatusHandler;
import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.internal.tasklist.ITask;
import org.eclipse.mylar.internal.tasklist.ITaskListElement;
import org.eclipse.mylar.internal.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.internal.tasklist.TaskRepository;
import org.eclipse.mylar.internal.tasklist.ui.TaskListImages;
import org.eclipse.mylar.internal.tasklist.ui.views.TaskListView;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.tigris.jira.core.model.Issue;
import org.tigris.jira.core.model.NamedFilter;
import org.tigris.jira.core.model.filter.IssueCollector;

/**
 * A JiraFilter represents a query for issues from a Jira repository.
 * 
 * @author Wesley Coelho (initial integration patch)
 * @author Mik Kersten
 */
public class JiraFilter extends AbstractRepositoryQuery {

	private static final String LABEL_REFRESH_JOB = "Refreshing Jira Filter(s)";

	private static final int MAX_HITS = 75;

	protected NamedFilter filter = null;

	private boolean urlsInitialized = false;

	private boolean isRefreshing = false;

	public JiraFilter(NamedFilter filter, boolean refresh) {
		setMaxHits(MAX_HITS);
		this.filter = filter;
		initUrls();
		if (urlsInitialized && refresh) {
			refreshHits();
		}
		super.setDescription(filter.getDescription());
	}

	/**
	 * Initializes the url fields for the filter. When initialized, the filter's
	 * url is the repositoryUrl and the handle.
	 */
	private void initUrls() {
		TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getDefaultRepository(
				MylarJiraPlugin.JIRA_REPOSITORY_KIND);
		if (repository == null) {
			MylarStatusHandler.log("No default repository found for filter", this);
			setQueryUrl("Missing Repository " + MylarJiraPlugin.FILTER_URL_PREFIX + filter.getId());
			urlsInitialized = false;
		} else {
			setQueryUrl(repository.getUrl() + MylarJiraPlugin.FILTER_URL_PREFIX + filter.getId());
			urlsInitialized = true;
		}
		setRepositoryUrl(getQueryUrl());
		setHandleIdentifier(getQueryUrl());
	}

	public NamedFilter getNamedFilter() {
		return filter;
	}

	/**
	 * Downloads Jira filter results from the server and loads them into the
	 * hits list.
	 */
	public void refreshHits() {
		isRefreshing = true;
		Job j = new Job(LABEL_REFRESH_JOB) {

			@Override
			protected IStatus run(final IProgressMonitor monitor) {

				clearHits();

				try {

					JiraServerFacade.getDefault().getJiraServer().executeNamedFilter(filter, new IssueCollector() {

						public void done() {
							isRefreshing = false;
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									if (TaskListView.getDefault() != null)
										TaskListView.getDefault().getViewer().refresh();
								}
							});
						}

						public boolean isCancelled() {
							return monitor.isCanceled();
						}

						public void collectIssue(Issue issue) {
							JiraFilterHit hit = new JiraFilterHit(issue);
							addHit(hit);

						}

						public void start() {

						}
					});

				} catch (Exception e) {
					isRefreshing = false;
					JiraServerFacade.handleConnectionException(e);
					return Status.CANCEL_STATUS;
				}

				return Status.OK_STATUS;
			}

		};

		j.schedule();

	}

	public String getQueryUrl() {
		if (!urlsInitialized) {
			initUrls();
		}
		return super.getQueryUrl();
	}

	public String getRepositoryUrl() {
		if (!urlsInitialized) {
			initUrls();
		}
		return super.getRepositoryUrl();
	}

	public Image getIcon() {
		return TaskListImages.getImage(TaskListImages.QUERY);
	}

	public Image getStatusIcon() {
		return null;
	}

	public boolean isDragAndDropEnabled() {
		return false;
	}

	/** Priorities are not yet implemented */
	public String getPriority() {
		return "";
	}

	public String getDescription() {
		if (filter.getDescription() != null) {
			return filter.getDescription();
		} else {
			MylarStatusHandler.log("Filter had no description", this);
			return "";
		}
	}

	public void setDescription(String description) {
		filter.setDescription(description);
	}

	public boolean isLocal() {
		return false;
	}

	public boolean isActivatable() {
		return false;
	}

	public Font getFont() {
		for (ITaskListElement currHit : getHits()) {
			if (currHit instanceof JiraFilterHit) {
				JiraFilterHit hit = (JiraFilterHit) currHit;
				ITask task = hit.getCorrespondingTask();
				if (task != null && task.isActive()) {
					return TaskListImages.BOLD;
				}
			}
		}
		return null;
	}

	public String getToolTipText() {
		return filter.getDescription();
	}

	public String getStringForSortingDescription() {
		return filter.getDescription();
	}

	/** True if the filter is currently downloading hits */
	public boolean isRefreshing() {
		return isRefreshing;
	}

	public String getRepositoryKind() {
		return MylarJiraPlugin.JIRA_REPOSITORY_KIND;
	}

}
