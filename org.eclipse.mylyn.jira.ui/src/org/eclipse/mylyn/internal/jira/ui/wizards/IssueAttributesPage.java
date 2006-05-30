/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.tigris.jira.core.model.IssueType;
import org.tigris.jira.core.model.Priority;
import org.tigris.jira.core.model.Resolution;
import org.tigris.jira.core.model.Status;
import org.tigris.jira.core.model.filter.CurrentUserFilter;
import org.tigris.jira.core.model.filter.FilterDefinition;
import org.tigris.jira.core.model.filter.IssueTypeFilter;
import org.tigris.jira.core.model.filter.NobodyFilter;
import org.tigris.jira.core.model.filter.PriorityFilter;
import org.tigris.jira.core.model.filter.ResolutionFilter;
import org.tigris.jira.core.model.filter.SpecificUserFilter;
import org.tigris.jira.core.model.filter.StatusFilter;
import org.tigris.jira.core.model.filter.UserFilter;
import org.tigris.jira.core.model.filter.UserInGroupFilter;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Brock Janiczak
 * @author Eugene Kuleshov (layout and other improvements)
 */
public class IssueAttributesPage extends WizardPage {
	final Placeholder ANY_ISSUE_TYPE = new Placeholder("Any");

	final Placeholder ANY_RESOLUTION = new Placeholder("Any");

	final Placeholder UNRESOLVED = new Placeholder("Unresolved");

	final Placeholder UNASSIGNED = new Placeholder("Unassigned");

	final Placeholder ANY_REPORTER = new Placeholder("Any");

	final Placeholder NO_REPORTER = new Placeholder("No Reporter");

	final Placeholder CURRENT_USER_REPORTER = new Placeholder("Current User");

	final Placeholder SPECIFIC_USER_REPORTER = new Placeholder("Specified User");

	final Placeholder SPECIFIC_GROUP_REPORTER = new Placeholder("Specified Group");

	final Placeholder ANY_ASSIGNEE = new Placeholder("Any");

	final Placeholder CURRENT_USER_ASSIGNEE = new Placeholder("Current User");

	final Placeholder SPECIFIC_USER_ASSIGNEE = new Placeholder("Specified User");

	final Placeholder SPECIFIC_GROUP_ASSIGNEE = new Placeholder("Specified Group");

	final Placeholder ANY_STATUS = new Placeholder("Any");

	final Placeholder ANY_PRIORITY = new Placeholder("Any");

//	private final TaskRepository repository;

	private final JiraServer jiraServer;

	private final FilterDefinition workingCopy;

	private final boolean isNew;

	private ListViewer issueType;

	private ComboViewer reporterType;

	private ComboViewer assigneeType;

	private ListViewer status;

	private ListViewer resolution;

	private ListViewer priority;

	Text assignee;

	Text reporter;

	/**
	 * @param repository
	 * @param pageName
	 * @param title
	 * @param titleImage
	 * @param server
	 */
	protected IssueAttributesPage(TaskRepository repository, FilterDefinition workingCopy, boolean isNew) {
		super("issueAttributes", "Issue Attributes", null);
		this.jiraServer = JiraServerFacade.getDefault().getJiraServer(repository);
		this.workingCopy = workingCopy;
		this.isNew = isNew;

		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		GridData gd;

		Composite cc = new Composite(parent, SWT.NONE);
		cc.setLayout(new GridLayout(1, false));

		{
			Composite c = new Composite(cc, SWT.NONE);
			final GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			gridData.widthHint = 515;
			c.setLayoutData(gridData);
			c.setLayout(new GridLayout(3, false));

			Label lblReportedBy = new Label(c, SWT.NONE);
			lblReportedBy.setText("Reported By:");
			lblReportedBy.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			reporterType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
			final GridData gridData_1 = new GridData(SWT.FILL, SWT.FILL, false, false);
			gridData_1.widthHint = 133;
			reporterType.getControl().setLayoutData(gridData_1);

			reporterType.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					return new Object[] { ANY_REPORTER, NO_REPORTER, CURRENT_USER_REPORTER, SPECIFIC_USER_REPORTER,
							SPECIFIC_GROUP_REPORTER };
				}

			});

			reporterType.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					return ((Placeholder) element).getText();
				}

			});

			reporterType.setInput(jiraServer);

			reporter = new Text(c, SWT.BORDER);
			reporter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			reporter.setEnabled(false);

			reporter.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validatePage();
				}

			});

			Label lblAssignedTo = new Label(c, SWT.NONE);
			lblAssignedTo.setText("Assigned To:");
			lblAssignedTo.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

			assigneeType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
			final GridData gridData_2 = new GridData(SWT.FILL, SWT.FILL, false, false);
			gridData_2.widthHint = 118;
			assigneeType.getControl().setLayoutData(gridData_2);

			assigneeType.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					return new Object[] { ANY_ASSIGNEE, UNASSIGNED, CURRENT_USER_ASSIGNEE, SPECIFIC_USER_ASSIGNEE,
							SPECIFIC_GROUP_ASSIGNEE };
				}

			});

			assigneeType.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					return ((Placeholder) element).getText();
				}

			});

			assigneeType.setInput(jiraServer);
			assigneeType.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (SPECIFIC_USER_ASSIGNEE.equals(selection) || SPECIFIC_GROUP_ASSIGNEE.equals(selection)) {
						assignee.setEnabled(true);
					} else {
						assignee.setEnabled(false);
						assignee.setText(""); //$NON-NLS-1$
					}
					validatePage();
				}

			});

			reporterType.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
					if (SPECIFIC_USER_REPORTER.equals(selection) || SPECIFIC_GROUP_REPORTER.equals(selection)) {
						reporter.setEnabled(true);
					} else {
						reporter.setEnabled(false);
					}
				}

			});

			assignee = new Text(c, SWT.BORDER);
			assignee.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
			assignee.setEnabled(false);

			assignee.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validatePage();
				}

			});

		}

		{
			Composite c = new Composite(cc, SWT.NONE);
			final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			gridData.heightHint = 149;
			c.setLayoutData(gridData);
			c.setLayout(new GridLayout(4, false));

			Label lblIssueType = new Label(c, SWT.NONE);
			lblIssueType.setText("Type:");

			Label lblStatus = new Label(c, SWT.NONE);
			lblStatus.setText("Status:");

			Label lblResolution = new Label(c, SWT.NONE);
			lblResolution.setText("Resolution:");

			Label lblPriority = new Label(c, SWT.NONE);
			lblPriority.setText("Priority:");
			
			issueType = new ListViewer(c, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 40;
			issueType.getControl().setLayoutData(gd);

			issueType.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					JiraServer server = (JiraServer) inputElement;
					Object[] elements = new Object[server.getIssueTypes().length + 1];
					elements[0] = ANY_ISSUE_TYPE;
					System.arraycopy(server.getIssueTypes(), 0, elements, 1, server.getIssueTypes().length);

					return elements;
				}
			});

			issueType.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					if (element instanceof Placeholder) {
						return ((Placeholder) element).getText();
					}

					return ((IssueType) element).getName();
				}

			});

			issueType.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}

			});
			issueType.setInput(jiraServer);

			status = new ListViewer(c, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 40;
			status.getControl().setLayoutData(gd);

			status.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					JiraServer server = (JiraServer) inputElement;
					Object[] elements = new Object[server.getStatuses().length + 1];
					elements[0] = ANY_STATUS;
					System.arraycopy(server.getStatuses(), 0, elements, 1, server.getStatuses().length);

					return elements;
				}
			});

			status.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					if (element instanceof Placeholder) {
						return ((Placeholder) element).getText();
					}

					return ((Status) element).getName();
				}

			});

			status.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}

			});
			status.setInput(jiraServer);

			resolution = new ListViewer(c, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 40;
			resolution.getControl().setLayoutData(gd);
			resolution.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					JiraServer server = (JiraServer) inputElement;
					Object[] elements = new Object[server.getResolutions().length + 2];
					elements[0] = ANY_RESOLUTION;
					elements[1] = UNRESOLVED;
					System.arraycopy(server.getResolutions(), 0, elements, 2, server.getResolutions().length);

					return elements;
				}
			});

			resolution.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					if (element instanceof Placeholder) {
						return ((Placeholder) element).getText();
					}

					return ((Resolution) element).getName();
				}

			});

			resolution.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}

			});
			resolution.setInput(jiraServer);

			priority = new ListViewer(c, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.heightHint = 40;
			priority.getControl().setLayoutData(gd);

			priority.setContentProvider(new IStructuredContentProvider() {

				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				public void dispose() {
				}

				public Object[] getElements(Object inputElement) {
					JiraServer server = (JiraServer) inputElement;
					Object[] elements = new Object[server.getPriorities().length + 1];
					elements[0] = ANY_PRIORITY;
					System.arraycopy(server.getPriorities(), 0, elements, 1, server.getPriorities().length);

					return elements;
				}
			});

			priority.setLabelProvider(new LabelProvider() {

				public String getText(Object element) {
					if (element instanceof Placeholder) {
						return ((Placeholder) element).getText();
					}

					return ((Priority) element).getName();
				}

			});
			priority.addSelectionChangedListener(new ISelectionChangedListener() {

				public void selectionChanged(SelectionChangedEvent event) {
					validatePage();
				}

			});
			priority.setInput(jiraServer);
		}

		if (isNew) {
			loadFromDefaults();
		} else {
			loadFromWorkingCopy();
		}

		setControl(cc);
	}

	void validatePage() {

	}

	/* default */void applyChanges() {
		if(issueType==null) {
			return;
		}
		
		// TODO support standard and subtask issue types
		IStructuredSelection issueTypeSelection = (IStructuredSelection) issueType.getSelection();
		if (issueTypeSelection.isEmpty()) {
			workingCopy.setIssueTypeFilter(null);
		} else {
			boolean isAnyIssueTypeSelected = false;

			List<IssueType> selectedIssueTypes = new ArrayList<IssueType>();

			for (Iterator i = issueTypeSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_ISSUE_TYPE.equals(selection)) {
					isAnyIssueTypeSelected = true;
				} else if (selection instanceof IssueType) {
					selectedIssueTypes.add((IssueType) selection);
				}
			}

			if (isAnyIssueTypeSelected) {
				workingCopy.setIssueTypeFilter(null);
			} else {
				workingCopy.setIssueTypeFilter(
					new IssueTypeFilter(selectedIssueTypes.toArray(new IssueType[selectedIssueTypes.size()])));
			}
		}

		IStructuredSelection reporterSelection = (IStructuredSelection) reporterType.getSelection();
		if (reporterSelection.isEmpty()) {
			workingCopy.setReportedByFilter(null);
		} else {
			if (ANY_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(null);
			} else if (NO_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new NobodyFilter());
			} else if (CURRENT_USER_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new CurrentUserFilter());
			} else if (SPECIFIC_GROUP_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new UserInGroupFilter(reporter.getText()));
			} else if (SPECIFIC_USER_REPORTER.equals(reporterSelection.getFirstElement())) {
				workingCopy.setReportedByFilter(new SpecificUserFilter(reporter.getText()));
			} else {
				workingCopy.setReportedByFilter(null);
			}
		}

		IStructuredSelection assigneeSelection = (IStructuredSelection) assigneeType.getSelection();
		if (assigneeSelection.isEmpty()) {
			workingCopy.setAssignedToFilter(null);
		} else {
			if (ANY_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(null);
			} else if (UNASSIGNED.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new NobodyFilter());
			} else if (CURRENT_USER_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new CurrentUserFilter());
			} else if (SPECIFIC_GROUP_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new UserInGroupFilter(assignee.getText()));
			} else if (SPECIFIC_USER_REPORTER.equals(assigneeSelection.getFirstElement())) {
				workingCopy.setAssignedToFilter(new SpecificUserFilter(assignee.getText()));
			} else {
				workingCopy.setAssignedToFilter(null);
			}
		}

		IStructuredSelection statusSelection = (IStructuredSelection) status.getSelection();
		if (statusSelection.isEmpty()) {
			workingCopy.setStatusFilter(null);
		} else {
			boolean isAnyStatusSelected = false;

			List<Status> selectedStatuses = new ArrayList<Status>();

			for (Iterator i = statusSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_STATUS.equals(selection)) {
					isAnyStatusSelected = true;
				} else if (selection instanceof Status) {
					selectedStatuses.add((Status) selection);
				}
			}

			if (isAnyStatusSelected) {
				workingCopy.setStatusFilter(null);
			} else {
				workingCopy.setStatusFilter(
					new StatusFilter(selectedStatuses.toArray(new Status[selectedStatuses.size()])));
			}
		}

		IStructuredSelection resolutionSelection = (IStructuredSelection) resolution.getSelection();
		if (resolutionSelection.isEmpty()) {
			workingCopy.setResolutionFilter(null);
		} else {
			boolean isAnyResolutionSelected = false;

			List<Resolution> selectedResolutions = new ArrayList<Resolution>();

			for (Iterator i = resolutionSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_RESOLUTION.equals(selection)) {
					isAnyResolutionSelected = true;
				} else if (selection instanceof Resolution) {
					selectedResolutions.add((Resolution) selection);
				}
			}

			if (isAnyResolutionSelected) {
				workingCopy.setResolutionFilter(null);
			} else {
				workingCopy.setResolutionFilter(
					new ResolutionFilter(selectedResolutions.toArray(new Resolution[selectedResolutions.size()])));
			}
		}

		IStructuredSelection prioritySelection = (IStructuredSelection) priority.getSelection();
		if (prioritySelection.isEmpty()) {
			workingCopy.setPriorityFilter(null);
		} else {
			boolean isAnyPrioritiesSelected = false;

			List<Priority> selectedPriorities = new ArrayList<Priority>();

			for (Iterator i = prioritySelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_PRIORITY.equals(selection)) {
					isAnyPrioritiesSelected = true;
				} else if (selection instanceof Priority) {
					selectedPriorities.add((Priority) selection);
				}
			}

			if (isAnyPrioritiesSelected) {
				workingCopy.setPriorityFilter(null);
			} else {
				workingCopy.setPriorityFilter(
					new PriorityFilter(selectedPriorities.toArray(new Priority[selectedPriorities.size()])));
			}
		}
	}

	private void loadFromDefaults() {
		issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE));
		reporterType.setSelection(new StructuredSelection(ANY_REPORTER));
		assigneeType.setSelection(new StructuredSelection(ANY_ASSIGNEE));
		status.setSelection(new StructuredSelection(ANY_STATUS));
		resolution.setSelection(new StructuredSelection(ANY_RESOLUTION));
		priority.setSelection(new StructuredSelection(ANY_PRIORITY));
	}

	private void loadFromWorkingCopy() {
		if (workingCopy.getIssueTypeFilter() != null) {
			issueType.setSelection(new StructuredSelection(workingCopy.getIssueTypeFilter().getIsueTypes()));
		} else {
			issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE));
		}

		if (workingCopy.getReportedByFilter() != null) {
			UserFilter reportedByFilter = workingCopy.getReportedByFilter();
			if (reportedByFilter instanceof NobodyFilter) {
				reporterType.setSelection(new StructuredSelection(NO_REPORTER));
			} else if (reportedByFilter instanceof CurrentUserFilter) {
				reporterType.setSelection(new StructuredSelection(CURRENT_USER_REPORTER));
			} else if (reportedByFilter instanceof SpecificUserFilter) {
				reporterType.setSelection(new StructuredSelection(SPECIFIC_USER_REPORTER));
				reporter.setText(((SpecificUserFilter) reportedByFilter).getUser());
			} else if (reportedByFilter instanceof UserInGroupFilter) {
				reporterType.setSelection(new StructuredSelection(SPECIFIC_GROUP_REPORTER));
				reporter.setText(((UserInGroupFilter) reportedByFilter).getGroup());
			}
		} else {
			reporterType.setSelection(new StructuredSelection(ANY_REPORTER));
		}

		if (workingCopy.getAssignedToFilter() != null) {
			UserFilter assignedToFilter = workingCopy.getAssignedToFilter();
			if (assignedToFilter instanceof NobodyFilter) {
				assigneeType.setSelection(new StructuredSelection(UNASSIGNED));
			} else if (assignedToFilter instanceof CurrentUserFilter) {
				assigneeType.setSelection(new StructuredSelection(CURRENT_USER_ASSIGNEE));
			} else if (assignedToFilter instanceof SpecificUserFilter) {
				assigneeType.setSelection(new StructuredSelection(SPECIFIC_USER_ASSIGNEE));
				assignee.setText(((SpecificUserFilter) assignedToFilter).getUser());
			} else if (assignedToFilter instanceof UserInGroupFilter) {
				assigneeType.setSelection(new StructuredSelection(SPECIFIC_GROUP_ASSIGNEE));
				assignee.setText(((UserInGroupFilter) assignedToFilter).getGroup());
			}
		} else {
			assigneeType.setSelection(new StructuredSelection(ANY_ASSIGNEE));
		}

		if (workingCopy.getStatusFilter() != null) {
			status.setSelection(new StructuredSelection(workingCopy.getStatusFilter().getStatuses()));
		} else {
			status.setSelection(new StructuredSelection(ANY_STATUS));
		}

		if (workingCopy.getResolutionFilter() != null) {
			resolution.setSelection(new StructuredSelection(workingCopy.getResolutionFilter().getResolutions()));
		} else {
			resolution.setSelection(new StructuredSelection(ANY_RESOLUTION));
		}

		if (workingCopy.getPriorityFilter() != null) {
			priority.setSelection(new StructuredSelection(workingCopy.getPriorityFilter().getPriorities()));
		} else {
			priority.setSelection(new StructuredSelection(ANY_PRIORITY));
		}
	}

	private final class Placeholder {
		private final String text;

		public Placeholder(String text) {
			this.text = text;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof Placeholder))
				return false;

			Placeholder that = (Placeholder) obj;
			return this.text.equals(that.text);
		}

		public String getText() {
			return this.text;
		}
	}
}
