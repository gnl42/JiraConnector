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

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.mylar.internal.jira.core.model.NamedFilter;
import org.eclipse.mylar.tasks.core.AbstractRepositoryQuery;

/**
 * A JiraRepositoryQuery represents a server-side query for Jira repository.
 * 
 * @author Mik Kersten
 */
public class JiraRepositoryQuery extends AbstractRepositoryQuery {

	protected NamedFilter filter = null;

	public JiraRepositoryQuery(String repositoryUrl, NamedFilter filter) {
		super(filter.getName());
		this.filter = filter;
		super.repositoryUrl = repositoryUrl;
		setUrl(repositoryUrl + JiraRepositoryConnector.FILTER_URL_PREFIX + "&requestId=" + filter.getId());
		// super.setDescription(filter.getName());
	}

	@Override
	public String getRepositoryKind() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	public NamedFilter getNamedFilter() {
		return filter;
	}

	/**
	 * Name comes from server.
	 */
	@Override
	public boolean canRename() {
		return false;
	}
}

// public void refreshHits() {
// isRefreshing = true;
// Job j = new Job(LABEL_REFRESH_JOB) {
//
// @Override
// protected IStatus run(final IProgressMonitor monitor) {
// clearHits();
// try {
// TaskRepository repository =
// MylarTaskListPlugin.getRepositoryManager().getRepository(JiraUiPlugin.JIRA_REPOSITORY_KIND,
// repositoryUrl);
// JiraServerFacade.getDefault().getJiraServer(repository).executeNamedFilter(filter,
// new IssueCollector() {
//
// public void done() {
// isRefreshing = false;
// Display.getDefault().asyncExec(new Runnable() {
// public void run() {
// if (TaskListView.getDefault() != null)
// TaskListView.getDefault().refreshAndFocus();
// }
// });
// }
//
// public boolean isCancelled() {
// return monitor.isCanceled();
// }
//
// public void collectIssue(Issue issue) {
// int issueId = new Integer(issue.getId());
// JiraFilterHit hit = new JiraFilterHit(issue,
// JiraFilter.this.getRepositoryUrl(), issueId);
// addHit(hit);
// }
//
// public void start() {
//
// }
// });
//
// } catch (Exception e) {
// isRefreshing = false;
// JiraServerFacade.handleConnectionException(e);
// return Status.CANCEL_STATUS;
// }
//
// return Status.OK_STATUS;
// }
//
// };
//
// j.schedule();
//
// }

// public String getQueryUrl() {
// return super.getQueryUrl();
// }

// public String getPriority() {
// return "";
// }

// public String getDescription() {
// if (filter.getName() != null) {
// return filter.getName();
// } else {
// return super.getDescription();
// }
// }
//
// public void setDescription(String summary) {
// filter.setDescription(summary);
// }

// public boolean isLocal() {
// return false;
// }

// /** True if the filter is currently downloading hits */
// public boolean isRefreshing() {
// return isRefreshing;
// }
// public void setRefreshing(boolean refreshing) {
// this.isRefreshing = refreshing;
// }
