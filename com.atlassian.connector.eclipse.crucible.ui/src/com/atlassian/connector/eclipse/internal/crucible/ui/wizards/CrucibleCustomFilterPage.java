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

package com.atlassian.connector.eclipse.internal.crucible.ui.wizards;

import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleRepositoryConnector;
import com.atlassian.connector.eclipse.internal.crucible.core.CrucibleUtil;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClient;
import com.atlassian.connector.eclipse.internal.crucible.core.client.CrucibleClientData;
import com.atlassian.connector.eclipse.internal.crucible.core.client.model.CrucibleCachedProject;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserContentProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserLabelProvider;
import com.atlassian.connector.eclipse.internal.crucible.ui.commons.CrucibleUserSorter;
import com.atlassian.connector.eclipse.ui.commons.TreeContentProvider;
import com.atlassian.theplugin.commons.crucible.api.model.CustomFilter;
import com.atlassian.theplugin.commons.crucible.api.model.State;
import com.atlassian.theplugin.commons.crucible.api.model.User;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.mylyn.internal.provisional.tasks.ui.wizards.AbstractRepositoryQueryPage2;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Query page for custom defined filters
 * 
 * @author Shawn Minto
 */
public class CrucibleCustomFilterPage extends AbstractRepositoryQueryPage2 {

	private static final String ANY = "<Any>";

	private static final String COMPLETE = "Complete";

	private static final int STATES_LIST_HEIGHT_HINT = 70;

	private static final String TITLE = "New Crucible Query";

	private static final User USER_ANY = new User("ANY_USER", ANY);

	private static final CrucibleCachedProject PROJECT_ANY = new CrucibleCachedProject(ANY, ANY, "ANY_PROJECT");

	private Button allRolesButton;

	private Button anyRolesButton;

	private ComboViewer reviewerStatusCombo;

	private ComboViewer authorCombo;

	private ComboViewer creatorCombo;

	private ComboViewer moderatorCombo;

	private ComboViewer projectCombo;

	private ComboViewer reviewerCombo;

	private ListViewer statesList;

	private String queryTitle;

	public CrucibleCustomFilterPage(TaskRepository repository, IRepositoryQuery query, String queryTitle) {
		this(repository, query);
		this.queryTitle = queryTitle;
	}

	public CrucibleCustomFilterPage(TaskRepository repository, IRepositoryQuery query) {
		super(TITLE, repository, query);
		setDescription("Select options to create a query");
	}

	public CrucibleCustomFilterPage(TaskRepository repository) {
		this(repository, null);
	}

	@Override
	protected void createPageContent(Composite parent) {

		if (queryTitle != null) {
			setQueryTitle(queryTitle);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(2).create());

		// TODO fix other 3 fields

		Label label = new Label(composite, SWT.NONE);
		label.setText("Project: ");
		label.setLayoutData(new GridData());

		projectCombo = new ComboViewer(composite, SWT.READ_ONLY);
		projectCombo.setContentProvider(new TreeContentProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection) {
					return ((Collection) inputElement).toArray();
				}
				return new Object[0];
			}
		});
		projectCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof CrucibleCachedProject) {
					return ((CrucibleCachedProject) element).getName();
				}
				return super.getText(element);
			}
		});
		projectCombo.setSorter(new ViewerSorter());
		projectCombo.getControl()
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		createRolesGroup(composite);

		label = new Label(composite, SWT.NONE);
		label.setText("State: ");
		label.setLayoutData(new GridData());

		statesList = new ListViewer(composite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		statesList.setContentProvider(new TreeContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Object[]) {
					return (Object[]) inputElement;
				}
				return new Object[0];
			}
		});
		statesList.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof State) {
					return ((State) element).getDisplayName();
				}
				return super.getText(element);
			}
		});
		statesList.setSorter(new ViewerSorter());
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		gd.heightHint = STATES_LIST_HEIGHT_HINT;
		statesList.getControl().setLayoutData(gd);

		doRefresh();
	}

	private void createRolesGroup(Composite composite) {
		Label label;
		Group rolesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
		rolesGroup.setText("Roles");
		rolesGroup.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 2;
		rolesGroup.setLayoutData(gd);

		label = new Label(rolesGroup, SWT.NONE);
		label.setText("Author: ");
		label.setLayoutData(new GridData());

		authorCombo = new ComboViewer(rolesGroup, SWT.READ_ONLY);
		authorCombo.setContentProvider(new CrucibleUserContentProvider());
		authorCombo.setLabelProvider(new CrucibleUserLabelProvider());
		authorCombo.setSorter(new CrucibleUserSorter());
		authorCombo.getControl().setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		label = new Label(rolesGroup, SWT.NONE);
		label.setText("Moderator: ");
		label.setLayoutData(new GridData());

		moderatorCombo = new ComboViewer(rolesGroup, SWT.READ_ONLY);
		moderatorCombo.setContentProvider(new CrucibleUserContentProvider());
		moderatorCombo.setLabelProvider(new CrucibleUserLabelProvider());
		moderatorCombo.setSorter(new CrucibleUserSorter());
		moderatorCombo.getControl().setLayoutData(
				new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		label = new Label(rolesGroup, SWT.NONE);
		label.setText("Creator: ");
		label.setLayoutData(new GridData());

		creatorCombo = new ComboViewer(rolesGroup, SWT.READ_ONLY);
		creatorCombo.setContentProvider(new CrucibleUserContentProvider());
		creatorCombo.setLabelProvider(new CrucibleUserLabelProvider());
		creatorCombo.setSorter(new CrucibleUserSorter());
		creatorCombo.getControl()
				.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		label = new Label(rolesGroup, SWT.NONE);
		label.setText("Reviewer: ");
		label.setLayoutData(new GridData());

		reviewerCombo = new ComboViewer(rolesGroup, SWT.READ_ONLY);
		reviewerCombo.setContentProvider(new CrucibleUserContentProvider());
		reviewerCombo.setLabelProvider(new CrucibleUserLabelProvider());
		reviewerCombo.setSorter(new CrucibleUserSorter());
		reviewerCombo.getControl().setLayoutData(
				new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		label = new Label(rolesGroup, SWT.NONE);
		label.setText("Reviewer Status: ");
		label.setLayoutData(new GridData());

		reviewerStatusCombo = new ComboViewer(rolesGroup, SWT.READ_ONLY);
		reviewerStatusCombo.setContentProvider(new TreeContentProvider() {
			@Override
			@SuppressWarnings("unchecked")
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof Collection) {
					return ((Collection) inputElement).toArray();
				}
				return new Object[0];
			}
		});
		reviewerStatusCombo.setLabelProvider(new LabelProvider());
		reviewerStatusCombo.setSorter(new ViewerSorter());
		reviewerStatusCombo.getControl().setLayoutData(
				new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));

		allRolesButton = new Button(rolesGroup, SWT.RADIO);
		allRolesButton.setText("Match All Roles");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		allRolesButton.setLayoutData(gd);

		anyRolesButton = new Button(rolesGroup, SWT.RADIO);
		anyRolesButton.setText("Match Any Roles");
		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		anyRolesButton.setLayoutData(gd);

	}

	@Override
	protected void doRefresh() {
		Set<User> users = new HashSet<User>(getClient().getClientData().getCachedUsers());
		users.add(USER_ANY);

		Set<CrucibleCachedProject> projects = new HashSet<CrucibleCachedProject>(getClient().getClientData()
				.getCachedProjects());
		projects.add(PROJECT_ANY);

		// TODO add the ANY values and set selections to be what they were before if they exist

		projectCombo.setInput(projects);
		projectCombo.setSelection(new StructuredSelection(PROJECT_ANY));

		authorCombo.setInput(users);
		authorCombo.setSelection(new StructuredSelection(USER_ANY));

		moderatorCombo.setInput(users);
		moderatorCombo.setSelection(new StructuredSelection(USER_ANY));

		creatorCombo.setInput(users);
		creatorCombo.setSelection(new StructuredSelection(USER_ANY));

		reviewerCombo.setInput(users);
		reviewerCombo.setSelection(new StructuredSelection(USER_ANY));

		statesList.setInput(State.values());

		allRolesButton.setSelection(true);
		anyRolesButton.setSelection(false);

		reviewerStatusCombo.setInput(Arrays.asList(ANY, COMPLETE));
		reviewerStatusCombo.setSelection(new StructuredSelection(ANY));

		restoreState(getQuery());
	}

	@Override
	protected boolean hasRepositoryConfiguration() {
		return getClient().hasRepositoryData();
	}

	private CrucibleClient getClient() {
		return ((CrucibleRepositoryConnector) getConnector()).getClientManager().getClient(getTaskRepository());
	}

	@Override
	protected boolean restoreState(IRepositoryQuery query) {

		if (query == null) {
			return false;
		}

		String project = query.getAttribute(CustomFilter.PROJECT);
		CrucibleCachedProject cachedProject = getCachedProject(project);
		if (cachedProject != null) {
			projectCombo.setSelection(new StructuredSelection(cachedProject));
		}

		String author = query.getAttribute(CustomFilter.AUTHOR);
		User cachedAuthor = getCachedUser(author);
		if (cachedAuthor != null) {
			authorCombo.setSelection(new StructuredSelection(cachedAuthor));
		}

		String creator = query.getAttribute(CustomFilter.CREATOR);
		User cachedCreator = getCachedUser(creator);
		if (cachedCreator != null) {
			creatorCombo.setSelection(new StructuredSelection(cachedCreator));
		}

		String moderator = query.getAttribute(CustomFilter.MODERATOR);
		User cachedModerator = getCachedUser(moderator);
		if (cachedModerator != null) {
			moderatorCombo.setSelection(new StructuredSelection(cachedModerator));
		}

		String reviewer = query.getAttribute(CustomFilter.REVIEWER);
		User cachedReviewer = getCachedUser(reviewer);
		if (cachedReviewer != null) {
			reviewerCombo.setSelection(new StructuredSelection(cachedReviewer));
		}

		String statesString = query.getAttribute(CustomFilter.STATES);
		State[] states = CrucibleUtil.getStatesFromString(statesString);
		if (states != null) {
			statesList.setSelection(new StructuredSelection(states));
		}

		String allCompleteString = query.getAttribute(CustomFilter.ALLCOMPLETE);
		String completeString = query.getAttribute(CustomFilter.COMPLETE);
		boolean allComplete = Boolean.parseBoolean(allCompleteString);
		boolean complete = Boolean.parseBoolean(completeString);

		if (!complete && !allComplete) {
			reviewerStatusCombo.setSelection(new StructuredSelection(ANY));
		} else {
			reviewerStatusCombo.setSelection(new StructuredSelection(COMPLETE));
		}

		String orRoles = query.getAttribute(CustomFilter.ORROLES);
		if (Boolean.parseBoolean(orRoles)) {
			allRolesButton.setSelection(false);
			anyRolesButton.setSelection(true);
		} else {
			allRolesButton.setSelection(true);
			anyRolesButton.setSelection(false);
		}
		return true;
	}

	private User getCachedUser(String username) {
		if (username == null || username.length() == 0) {
			return USER_ANY;
		}

		CrucibleClientData clientData = getClient().getClientData();
		if (clientData != null && clientData.getCachedUsers() != null) {
			for (User user : clientData.getCachedUsers()) {
				if (user.getUserName().equals(username)) {
					return user;
				}
			}
		}

		return null;
	}

	private CrucibleCachedProject getCachedProject(String projectKey) {
		if (projectKey == null || projectKey.length() == 0) {
			return PROJECT_ANY;
		}

		CrucibleClientData clientData = getClient().getClientData();
		if (clientData != null && clientData.getCachedProjects() != null) {
			for (CrucibleCachedProject project : clientData.getCachedProjects()) {
				if (project.getKey().equals(projectKey)) {
					return project;
				}
			}
		}

		return null;
	}

	@Override
	public void applyTo(IRepositoryQuery query) {
		query.setSummary(getQueryTitle());

		query.setAttribute(CustomFilter.AUTHOR, getAuthor());
		query.setAttribute(CustomFilter.CREATOR, getCreator());
		query.setAttribute(CustomFilter.MODERATOR, getModerator());
		query.setAttribute(CustomFilter.PROJECT, getProject());

		String reviewer = getReviewer();
		query.setAttribute(CustomFilter.REVIEWER, reviewer);
		query.setAttribute(CustomFilter.STATES, getStates());

		query.setAttribute(CustomFilter.ORROLES, getOrRoles());

		String reviewerStatus = getReviewerStatus();
		Boolean complete = ANY.equals(reviewerStatus) ? Boolean.FALSE : COMPLETE.equals(reviewerStatus);
		if (reviewer != null && reviewer.length() > 0) {
			query.setAttribute(CustomFilter.ALLCOMPLETE, "");
			query.setAttribute(CustomFilter.COMPLETE, Boolean.toString(complete));
		} else {
			query.setAttribute(CustomFilter.ALLCOMPLETE, Boolean.toString(complete));
			query.setAttribute(CustomFilter.COMPLETE, "");
		}

		String customFilterUrl = CrucibleUtil.createFilterWebUrl(getTaskRepository().getUrl(), query);
		query.setUrl(customFilterUrl);

	}

	private String getStates() {
		String statesString = "";
		ISelection selection = statesList.getSelection();
		if (selection instanceof StructuredSelection) {
			Object[] objs = ((StructuredSelection) selection).toArray();
			for (Object obj : objs) {
				if (obj instanceof State) {
					String state = ((State) obj).value();
					if (statesString.length() > 0) {
						statesString += ",";
					}
					statesString += state;
				}
			}
		}
		return statesString;
	}

	private String getReviewer() {
		ISelection selection = reviewerCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof User) {
				if (obj == USER_ANY) {
					return "";
				} else {
					return ((User) obj).getUserName();
				}
			}
		}
		return "";
	}

	private String getProject() {
		ISelection selection = projectCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof CrucibleCachedProject) {
				if (obj == PROJECT_ANY) {
					return "";
				} else {
					return ((CrucibleCachedProject) obj).getKey();
				}
			}
		}
		return "";
	}

	private String getOrRoles() {
		return Boolean.toString(anyRolesButton.getSelection());
	}

	private String getModerator() {
		ISelection selection = moderatorCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof User) {
				if (obj == USER_ANY) {
					return "";
				} else {
					return ((User) obj).getUserName();
				}
			}
		}
		return "";
	}

	private String getCreator() {
		ISelection selection = creatorCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof User) {
				if (obj == USER_ANY) {
					return "";
				} else {
					return ((User) obj).getUserName();
				}
			}
		}
		return "";
	}

	private String getAuthor() {
		ISelection selection = authorCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof User) {
				if (obj == USER_ANY) {
					return "";
				} else {
					return ((User) obj).getUserName();
				}
			}
		}
		return "";
	}

	private String getReviewerStatus() {
		ISelection selection = reviewerStatusCombo.getSelection();
		if (selection instanceof StructuredSelection) {
			Object obj = ((StructuredSelection) selection).getFirstElement();
			if (obj instanceof String) {
				return (String) obj;
			}
		}
		return null;
	}

	@Override
	public boolean isPageComplete() {
		return super.isPageComplete();
	}

}
