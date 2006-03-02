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

import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryQuery;
import org.tigris.jira.core.model.NamedFilter;

/**
 * A JiraFilter represents a query for issues from a Jira repository.
 * 
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraFilter extends AbstractRepositoryQuery {

//	private static final String LABEL_REFRESH_JOB = "Refreshing Jira Filter(s)";

	private static final int MAX_HITS = 75;

	protected NamedFilter filter = null;

//	private boolean isRefreshing = false;

	public JiraFilter(String repositoryUrl, NamedFilter filter) {
		setMaxHits(MAX_HITS);
		this.filter = filter;
		super.repositoryUrl = repositoryUrl;
		setQueryUrl(repositoryUrl + MylarJiraPlugin.FILTER_URL_PREFIX + filter.getId());

		super.setDescription(filter.getName());
	}

	public NamedFilter getNamedFilter() {
		return filter;
	}

//	/**
//	 * TODO: refactor into common refresh mechanism.
//	 */
//	public void refreshHits() {
//		isRefreshing = true;
//		Job j = new Job(LABEL_REFRESH_JOB) {
//
//			@Override
//			protected IStatus run(final IProgressMonitor monitor) {
//				clearHits();
//				try {
//					TaskRepository repository = MylarTaskListPlugin.getRepositoryManager().getRepository(MylarJiraPlugin.JIRA_REPOSITORY_KIND, repositoryUrl);
//					JiraServerFacade.getDefault().getJiraServer(repository).executeNamedFilter(filter, new IssueCollector() {
//
//						public void done() {
//							isRefreshing = false;
//							Display.getDefault().asyncExec(new Runnable() {
//								public void run() {
//									if (TaskListView.getDefault() != null)
//										TaskListView.getDefault().refreshAndFocus();
//								}
//							});
//						}
//
//						public boolean isCancelled() {
//							return monitor.isCanceled();
//						}
//
//						public void collectIssue(Issue issue) {
//							int issueId = new Integer(issue.getId());
//							JiraFilterHit hit = new JiraFilterHit(issue, JiraFilter.this.getRepositoryUrl(), issueId);
//							addHit(hit);
//						}
//
//						public void start() {
//
//						}
//					});
//
//				} catch (Exception e) {
//					isRefreshing = false;
//					JiraServerFacade.handleConnectionException(e);
//					return Status.CANCEL_STATUS;
//				}
//
//				return Status.OK_STATUS;
//			}
//
//		};
//
//		j.schedule();
//
//	}

	public String getQueryUrl() {
		return super.getQueryUrl();
	}

	public String getPriority() {
		return "";
	}

	public String getDescription() {
		if (filter.getName() != null) {
			return filter.getName();
		} else {
			return super.getDescription();
		}
	}

	public void setDescription(String description) {
		filter.setDescription(description);
	}

	public boolean isLocal() {
		return false;
	}

//	/** True if the filter is currently downloading hits */
//	public boolean isRefreshing() {
//		return isRefreshing;
//	}

	public String getRepositoryKind() {
		return MylarJiraPlugin.JIRA_REPOSITORY_KIND;
	}

	public NamedFilter getFilter() {
		return filter;
	}

//	public void setRefreshing(boolean refreshing) {
//		this.isRefreshing = refreshing;
//	}

}
