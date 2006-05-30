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

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.mylar.internal.jira.JiraCustomQuery;
import org.eclipse.mylar.internal.jira.JiraServerFacade;
import org.eclipse.mylar.provisional.tasklist.AbstractRepositoryQuery;
import org.eclipse.mylar.provisional.tasklist.MylarTaskListPlugin;
import org.eclipse.mylar.provisional.tasklist.TaskRepository;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.tigris.jira.core.model.Component;
import org.tigris.jira.core.model.Project;
import org.tigris.jira.core.model.Version;
import org.tigris.jira.core.model.filter.ComponentFilter;
import org.tigris.jira.core.model.filter.ContentFilter;
import org.tigris.jira.core.model.filter.FilterDefinition;
import org.tigris.jira.core.model.filter.ProjectFilter;
import org.tigris.jira.core.model.filter.VersionFilter;
import org.tigris.jira.core.service.JiraServer;

/**
 * @author Brock Janiczak
 * @author Eugene Kuleshov (layout and other improvements)
 */
public class FilterSummaryPage extends WizardPage {
	
	final Placeholder ANY_FIX_VERSION = new Placeholder("Any");

	final Placeholder NO_FIX_VERSION = new Placeholder("No Fix Version");

	final Placeholder ANY_REPORTED_VERSION = new Placeholder("Any");

	final Placeholder NO_REPORTED_VERSION = new Placeholder("No Version");

	final Placeholder UNRELEASED_VERSION = new Placeholder("Unreleased Versions");

	final Placeholder RELEASED_VERSION = new Placeholder("Released Versions");

	final Placeholder ANY_COMPONENT = new Placeholder("Any");

	final Placeholder NO_COMPONENT = new Placeholder("No Component");

	private final JiraServer server;

	private ListViewer project;

	private ListViewer reportedIn;

	private ListViewer components;

	private ListViewer fixFor;

	private final FilterDefinition workingCopy;

	private Text name;

	private Text description;

	private Text queryString;

	private Button searchSummary;

	private Button searchDescription;

	private Button searchComments;

	private Button searchEnvironment;

	private final boolean isNew;

	private IssueAttributesPage issueAttributesPage;

	private final TaskRepository repository;

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	protected FilterSummaryPage(TaskRepository repository, FilterDefinition workingCopy, boolean isNew) {
		super("summaryPage", "Filter Summary", null);
		this.repository = repository;

		this.server = JiraServerFacade.getDefault().getJiraServer(repository);
		this.workingCopy = workingCopy;
		this.isNew = isNew;

		setPageComplete(false);
	}

	public void createControl(Composite parent) {
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(5, false));

		Label lblName = new Label(c, SWT.NONE);
		final GridData gridData = new GridData();
		lblName.setLayoutData(gridData);
		lblName.setText("Name:");

		name = new Text(c, SWT.BORDER);
		name.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		name.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validatePage();
			}

		});

		if (!isNew) {
			name.setEnabled(false);
		}

		Label lblDescription = new Label(c, SWT.NONE);
		lblDescription.setText("Description:");
		lblDescription.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		description = new Text(c, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1);
		gd.heightHint = 40;
		description.setLayoutData(gd);
		description.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				validatePage();
			}

		});

		{
			SashForm cc = new SashForm(c, SWT.HORIZONTAL);
			cc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, 5, 1));

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				comp.setLayout(gridLayout);
				
				Label label = new Label(comp, SWT.NONE);
				label.setText("Project:");
				createProjectsViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				comp.setLayout(gridLayout);
				
				new Label(comp, SWT.NONE).setText("Fix For:");
				createFixForViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				comp.setLayout(gridLayout);
				
				new Label(comp, SWT.NONE).setText("In Components:");
				createComponentsViewer(comp);
			}

			{
				Composite comp = new Composite(cc, SWT.NONE);
				GridLayout gridLayout = new GridLayout(1, false);
				gridLayout.marginWidth = 0;
				gridLayout.marginHeight = 0;
				comp.setLayout(gridLayout);

				Label label = new Label(comp, SWT.NONE);
				label.setText("Reported In:");
				createReportedInViewer(comp);
			}
			// cc.setWeights(new int[] {1,1,1,1});
		}

		Label lblQuery = new Label(c, SWT.NONE);
		lblQuery.setLayoutData(new GridData());
		lblQuery.setText("Query:");
		queryString = new Text(c, SWT.BORDER);
		queryString.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		// TODO put content assist here and a label describing what is available

		queryString.addFocusListener(new FocusAdapter() {

			public void focusLost(FocusEvent e) {
				validatePage();
			}

		});

		Label lblFields = new Label(c, SWT.NONE);
		lblFields.setText("Fields:");
		lblFields.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));

		searchSummary = new Button(c, SWT.CHECK);
		searchSummary.setLayoutData(new GridData());
		searchSummary.setText("Summary");
		searchSummary.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

		});

		searchDescription = new Button(c, SWT.CHECK);
		searchDescription.setLayoutData(new GridData());
		searchDescription.setText("Description");
		searchDescription.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

		});

		searchComments = new Button(c, SWT.CHECK);
		searchComments.setLayoutData(new GridData());
		searchComments.setText("Comments");
		searchComments.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

		});

		searchEnvironment = new Button(c, SWT.CHECK);
		searchEnvironment.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		searchEnvironment.setText("Environment");
		searchEnvironment.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validatePage();
			}

		});
		
		// Need to turn off validation here
		if (isNew) {
			loadFromDefaults();
		} else {
			loadFromWorkingCopy();
		}
		
		setControl(c);
	}

	public IWizardPage getNextPage() {
		if (issueAttributesPage == null) {
			issueAttributesPage = new IssueAttributesPage(repository, workingCopy, isNew);
			issueAttributesPage.setWizard(getWizard());
		}

		return issueAttributesPage;
	}

	private void createReportedInViewer(Composite c) {
		reportedIn = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 200;
		gridData.widthHint = 80;
		reportedIn.getControl().setLayoutData(gridData);

		reportedIn.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Version[] versions = project.getVersions();
					Version[] releasedVersions = project.getReleasedVersions();
					Version[] unreleasedVersions = project.getUnreleasedVersions();

					Object[] elements = new Object[versions.length + 4];
					elements[0] = ANY_REPORTED_VERSION;
					elements[1] = NO_REPORTED_VERSION;
					elements[2] = RELEASED_VERSION;
					System.arraycopy(releasedVersions, 0, elements, 3, releasedVersions.length);

					elements[releasedVersions.length + 3] = UNRELEASED_VERSION;

					System.arraycopy(unreleasedVersions, 0, elements, releasedVersions.length + 4,
							unreleasedVersions.length);
					return elements;
				}
				return new Object[] { ANY_REPORTED_VERSION };
			}

		});
		reportedIn.setLabelProvider(new VersionLabelProvider());
		reportedIn.setInput(null);
	}

	private void createComponentsViewer(Composite c) {
		components = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 200;
		gridData.widthHint = 80;
		components.getControl().setLayoutData(gridData);

		components.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Component[] components = project.getComponents();

					Object[] elements = new Object[components.length + 2];
					elements[0] = ANY_COMPONENT;
					elements[1] = NO_COMPONENT;
					System.arraycopy(components, 0, elements, 2, components.length);
					return elements;
				}
				return new Object[] { ANY_COMPONENT };
			}

		});
		components.setLabelProvider(new ComponentLabelProvider());
		components.setInput(null);
	}

	private void createFixForViewer(Composite c) {
		fixFor = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 200;
		gridData.widthHint = 80;
		fixFor.getControl().setLayoutData(gridData);

		fixFor.setContentProvider(new IStructuredContentProvider() {
			private Project project;

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				project = (Project) newInput;
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				if (project != null) {
					Version[] versions = project.getVersions();
					Version[] releasedVersions = project.getReleasedVersions();
					Version[] unreleasedVersions = project.getUnreleasedVersions();

					Object[] elements = new Object[versions.length + 4];
					elements[0] = ANY_FIX_VERSION;
					elements[1] = NO_FIX_VERSION;
					elements[2] = RELEASED_VERSION;
					System.arraycopy(releasedVersions, 0, elements, 3, releasedVersions.length);

					elements[releasedVersions.length + 3] = UNRELEASED_VERSION;

					System.arraycopy(unreleasedVersions, 0, elements, releasedVersions.length + 4,
							unreleasedVersions.length);
					return elements;
				}
				return new Object[] { ANY_REPORTED_VERSION };
			}

		});
		fixFor.setLabelProvider(new VersionLabelProvider());
		fixFor.setInput(null);
	}

	private void createProjectsViewer(Composite c) {
		project = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.heightHint = 200;
		gridData.widthHint = 120;
		project.getControl().setLayoutData(gridData);
		
		project.setContentProvider(new IStructuredContentProvider() {

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				JiraServer server = (JiraServer) inputElement;
				Object[] elements = new Object[server.getProjects().length + 1];
				elements[0] = new Placeholder("All Projects");
				System.arraycopy(server.getProjects(), 0, elements, 1, server.getProjects().length);

				return elements;
			}

		});

		project.setLabelProvider(new LabelProvider() {

			public String getText(Object element) {
				if (element instanceof Placeholder) {
					return ((Placeholder) element).getText();
				}

				return ((Project) element).getName();
			}

		});

		project.setInput(server);
		project.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = ((IStructuredSelection) event.getSelection());
				Project selectedProject = null;
				if (!selection.isEmpty() && !(selection.getFirstElement() instanceof Placeholder)) {
					selectedProject = (Project) selection.getFirstElement();
				}

				updateCurrentProject(selectedProject);
				validatePage();
			}

		});
	}

	void updateCurrentProject(Project project) {
		this.fixFor.setInput(project);
		this.components.setInput(project);
		this.reportedIn.setInput(project);

	}

	void validatePage() {
		if (name.getText().length() == 0) {
			setErrorMessage("Name is mandatory");
			setPageComplete(false);
			return;
		}

		setErrorMessage(null);
		setPageComplete(true);
	}

	private void loadFromDefaults() {
		project.setSelection(new StructuredSelection(new Placeholder("All Projects")));
		searchSummary.setSelection(true);
		searchDescription.setSelection(true);
	}

	private void loadFromWorkingCopy() {
		if (workingCopy.getName() != null) {
			name.setText(workingCopy.getName());
		}

		if (workingCopy.getDescription() != null) {
			description.setText(workingCopy.getDescription());
		}

		if (workingCopy.getProjectFilter() != null) {
			project.setSelection(new StructuredSelection(workingCopy.getProjectFilter().getProject()));
		} else {
			project.setSelection(new StructuredSelection(new Placeholder("All Projects")));
		}

		if (workingCopy.getFixForVersionFilter() != null) {
			if (workingCopy.getFixForVersionFilter().hasNoVersion()) {
				fixFor.setSelection(new StructuredSelection(new Object[] { NO_FIX_VERSION }));
			} else if (workingCopy.getFixForVersionFilter().isReleasedVersions()
					&& workingCopy.getFixForVersionFilter().isUnreleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(new Object[] { RELEASED_VERSION, UNRELEASED_VERSION }));
			} else if (workingCopy.getFixForVersionFilter().isReleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(RELEASED_VERSION));
			} else if (workingCopy.getFixForVersionFilter().isUnreleasedVersions()) {
				fixFor.setSelection(new StructuredSelection(UNRELEASED_VERSION));
			} else {
				fixFor.setSelection(new StructuredSelection(workingCopy.getFixForVersionFilter().getVersions()));
			}
		} else {
			fixFor.setSelection(new StructuredSelection(ANY_FIX_VERSION));
		}

		if (workingCopy.getReportedInVersionFilter() != null) {
			if (workingCopy.getReportedInVersionFilter().hasNoVersion()) {
				reportedIn.setSelection(new StructuredSelection(new Object[] { NO_REPORTED_VERSION }));
			} else if (workingCopy.getReportedInVersionFilter().isReleasedVersions()
					&& workingCopy.getReportedInVersionFilter().isUnreleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(new Object[] { RELEASED_VERSION, UNRELEASED_VERSION }));
			} else if (workingCopy.getReportedInVersionFilter().isReleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(RELEASED_VERSION));
			} else if (workingCopy.getReportedInVersionFilter().isUnreleasedVersions()) {
				reportedIn.setSelection(new StructuredSelection(UNRELEASED_VERSION));
			} else {
				reportedIn
						.setSelection(new StructuredSelection(workingCopy.getReportedInVersionFilter().getVersions()));
			}
		} else {
			reportedIn.setSelection(new StructuredSelection(ANY_REPORTED_VERSION));
		}

		if (workingCopy.getContentFilter() != null) {
			this.queryString.setText(workingCopy.getContentFilter().getQueryString());
			this.searchComments.setSelection(workingCopy.getContentFilter().isSearchingComments());
			this.searchDescription.setSelection(workingCopy.getContentFilter().isSearchingDescription());
			this.searchEnvironment.setSelection(workingCopy.getContentFilter().isSearchingEnvironment());
			this.searchSummary.setSelection(workingCopy.getContentFilter().isSearchingSummary());
		}

		if (workingCopy.getComponentFilter() != null) {
			if (workingCopy.getComponentFilter().hasNoComponent()) {
				components.setSelection(new StructuredSelection(NO_COMPONENT));
			} else {
				components.setSelection(new StructuredSelection(workingCopy.getComponentFilter().getComponents()));
			}
		} else {
			components.setSelection(new StructuredSelection(ANY_COMPONENT));
		}
	}

	/* default */void applyChanges() {
		workingCopy.setName(this.name.getText());
		workingCopy.setDescription(this.description.getText());
		if (this.queryString.getText().length() > 0 || this.searchSummary.getSelection()
				|| this.searchDescription.getSelection() || this.searchEnvironment.getSelection()
				|| this.searchComments.getSelection()) {
			workingCopy.setContentFilter(new ContentFilter(this.queryString.getText(), this.searchSummary
					.getSelection(), this.searchDescription.getSelection(), this.searchEnvironment.getSelection(),
					this.searchComments.getSelection()));
		} else {
			workingCopy.setContentFilter(null);
		}

		IStructuredSelection projectSelection = (IStructuredSelection) this.project.getSelection();
		if (!projectSelection.isEmpty() && projectSelection.getFirstElement() instanceof Project) {
			workingCopy.setProjectFilter(new ProjectFilter((Project) projectSelection.getFirstElement()));
		} else {
			workingCopy.setProjectFilter(null);
		}

		IStructuredSelection reportedInSelection = (IStructuredSelection) reportedIn.getSelection();
		if (reportedInSelection.isEmpty()) {
			workingCopy.setReportedInVersionFilter(null);
		} else {
			boolean selectionContainsReleased = false;
			boolean selectionContainsUnreleased = false;
			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;

			List<Version> selectedVersions = new ArrayList<Version>();

			for (Iterator i = reportedInSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_REPORTED_VERSION.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_REPORTED_VERSION.equals(selection)) {
					selectionContainsNone = true;
				} else if (RELEASED_VERSION.equals(selection)) {
					selectionContainsReleased = true;
				} else if (UNRELEASED_VERSION.equals(selection)) {
					selectionContainsUnreleased = true;
				} else if (selection instanceof Version) {
					selectedVersions.add((Version) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setReportedInVersionFilter(null);
			} else if (selectionContainsNone) {
				workingCopy.setReportedInVersionFilter(new VersionFilter(new Version[0]));
			} else if (selectionContainsReleased || selectionContainsUnreleased) {
				workingCopy.setReportedInVersionFilter(new VersionFilter(selectionContainsReleased,
						selectionContainsUnreleased));
			} else if (selectedVersions.size() > 0) {
				workingCopy.setReportedInVersionFilter(
						new VersionFilter(selectedVersions.toArray(new Version[selectedVersions.size()])));
			} else {
				workingCopy.setReportedInVersionFilter(null);
			}
		}

		IStructuredSelection fixForSelection = (IStructuredSelection) fixFor.getSelection();
		if (fixForSelection.isEmpty()) {
			workingCopy.setFixForVersionFilter(null);
		} else {
			boolean selectionContainsReleased = false;
			boolean selectionContainsUnreleased = false;
			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;

			List<Version> selectedVersions = new ArrayList<Version>();

			for (Iterator i = fixForSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_FIX_VERSION.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_FIX_VERSION.equals(selection)) {
					selectionContainsNone = true;
				} else if (RELEASED_VERSION.equals(selection)) {
					selectionContainsReleased = true;
				} else if (UNRELEASED_VERSION.equals(selection)) {
					selectionContainsUnreleased = true;
				} else if (selection instanceof Version) {
					selectedVersions.add((Version) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setFixForVersionFilter(null);
			} else if (selectionContainsNone) {
				workingCopy.setFixForVersionFilter(new VersionFilter(new Version[0]));
			} else if (selectionContainsReleased || selectionContainsUnreleased) {
				workingCopy.setFixForVersionFilter(new VersionFilter(selectionContainsReleased,
						selectionContainsUnreleased));
			} else if (selectedVersions.size() > 0) {
				workingCopy.setFixForVersionFilter(
						new VersionFilter(selectedVersions.toArray(new Version[selectedVersions.size()])));
			} else {
				workingCopy.setFixForVersionFilter(null);
			}
		}

		IStructuredSelection componentsSelection = (IStructuredSelection) components.getSelection();
		if (componentsSelection.isEmpty()) {
			workingCopy.setComponentFilter(null);
		} else {

			boolean selectionContainsAll = false;
			boolean selectionContainsNone = false;
			List<Component> selectedComponents = new ArrayList<Component>();

			for (Iterator i = componentsSelection.iterator(); i.hasNext();) {
				Object selection = i.next();
				if (ANY_COMPONENT.equals(selection)) {
					selectionContainsAll = true;
				} else if (NO_COMPONENT.equals(selection)) {
					selectionContainsNone = true;
				} else if (selection instanceof Component) {
					selectedComponents.add((Component) selection);
				}
			}

			if (selectionContainsAll) {
				workingCopy.setComponentFilter(null);
			} else if (selectedComponents.size() > 0) {
				workingCopy.setComponentFilter(
						new ComponentFilter(selectedComponents.toArray(new Component[selectedComponents.size()])));
			} else if (selectionContainsNone) {
				workingCopy.setComponentFilter(new ComponentFilter(new Component[0]));
			} else {
				workingCopy.setComponentFilter(null);
			}
		}
	}

	final class ComponentLabelProvider implements ILabelProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof Placeholder) {
				return ((Placeholder) element).getText();
			}
			return ((Component) element).getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

	}

	final class VersionLabelProvider implements ILabelProvider, IColorProvider {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			if (element instanceof Placeholder) {
				return ((Placeholder) element).getText();
			}
			return ((Version) element).getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object,
		 *      java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element) {
			if (element instanceof Placeholder) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
			}
			return null;
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

	public AbstractRepositoryQuery getQuery() {
		this.applyChanges();
		issueAttributesPage.applyChanges();
		if (isNew) {
			server.addLocalFilter(workingCopy);
		}

		return new JiraCustomQuery(repository.getUrl(), workingCopy, MylarTaskListPlugin.getTaskListManager()
				.getTaskList());
	}
}
