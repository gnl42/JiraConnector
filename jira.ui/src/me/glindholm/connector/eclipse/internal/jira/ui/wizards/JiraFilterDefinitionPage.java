/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Eugene Kuleshov - improvements
 *     Tasktop Technologies - improvements
 *     Atlassian - improvements
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.wizards;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
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
import org.eclipse.mylyn.commons.core.ICoreRunnable;
import org.eclipse.mylyn.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.commons.workbench.forms.DatePicker;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.wizards.AbstractRepositoryQueryPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import me.glindholm.connector.eclipse.internal.jira.core.JiraClientFactory;
import me.glindholm.connector.eclipse.internal.jira.core.JiraCorePlugin;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraComponent;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraIssueType;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraPriority;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraProject;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraResolution;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraStatus;
import me.glindholm.connector.eclipse.internal.jira.core.model.JiraVersion;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ComponentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ContentFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.CurrentUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.DateRangeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.FilterDefinition;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.IssueTypeFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.NobodyFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.PriorityFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ProjectFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.ResolutionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.SpecificUserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.StatusFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.UserInGroupFilter;
import me.glindholm.connector.eclipse.internal.jira.core.model.filter.VersionFilter;
import me.glindholm.connector.eclipse.internal.jira.core.service.FilterDefinitionConverter;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraClient;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraException;
import me.glindholm.connector.eclipse.internal.jira.core.util.JiraUtil;
import me.glindholm.connector.eclipse.internal.jira.ui.JiraUiPlugin;
import me.glindholm.connector.eclipse.internal.jira.ui.WdhmUtil;

/**
 * @author Brock Janiczak
 * @author Eugene Kuleshov
 * @author Mik Kersten
 * @author Steffen Pingel
 * @author Thomas Ehrnhoefer
 * @author Pawel Niewiadomski
 * @author Jacek Jaroczynski
 * @author Wojciech Seliga
 */
public class JiraFilterDefinitionPage extends AbstractRepositoryQueryPage {

    private static final String JIRA_STATUS_CLOSED = "6"; //$NON-NLS-1$

    private static final String JIRA_STATUS_RESOLVED = "5"; //$NON-NLS-1$

    private static final int DATE_CONTROL_WIDTH_HINT = 350;

    final static class ComponentLabelProvider implements ILabelProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public Image getImage(final Object element) {
            return null;
        }

        @Override
        public String getText(final Object element) {
            if (element instanceof Placeholder) {
                return ((Placeholder) element).getText();
            }
            return ((JiraComponent) element).getName();
        }

        @Override
        public boolean isLabelProperty(final Object element, final String property) {
            return false;
        }

        @Override
        public void removeListener(final ILabelProviderListener listener) {
        }

    }

    private static final class Placeholder {
        private final String text;

        public Placeholder(final String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    final static class VersionLabelProvider implements ILabelProvider, IColorProvider {

        @Override
        public void addListener(final ILabelProviderListener listener) {

        }

        @Override
        public void dispose() {
        }

        @Override
        public Color getBackground(final Object element) {
            if (element instanceof Placeholder) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            }
            return null;
        }

        @Override
        public Color getForeground(final Object element) {
            return null;
        }

        @Override
        public Image getImage(final Object element) {
            return null;
        }

        @Override
        public String getText(final Object element) {
            if (element instanceof Placeholder) {
                return ((Placeholder) element).getText();
            }
            return ((JiraVersion) element).getName();
        }

        @Override
        public boolean isLabelProperty(final Object element, final String property) {
            return false;
        }

        @Override
        public void removeListener(final ILabelProviderListener listener) {
        }

    }

    private static final String PAGE_NAME = "Jira" + "SearchPage"; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String SEARCH_URL_ID = PAGE_NAME + ".SEARCHURL"; //$NON-NLS-1$

    private static final JiraProject[] NO_PROJECTS = {};

    private static final int HEIGHT_HINT = 50;

    private static final int WIDTH_HINT = 150;

    final Placeholder ALL_PROJECTS = new Placeholder(Messages.JiraFilterDefinitionPage_All_Projects);

    final Placeholder ANY_ASSIGNEE = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_COMPONENT = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_FIX_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_ISSUE_TYPE = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_PRIORITY = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    // attributes

    final Placeholder ANY_REPORTED_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_REPORTER = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_RESOLUTION = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    final Placeholder ANY_STATUS = new Placeholder(Messages.JiraFilterDefinitionPage_Any);

    private Text assignee;

    private ComboViewer assigneeType;

    private final JiraClient client;

    private ListViewer components;

    private DatePicker createdEndDatePicker;

    private DatePicker createdStartDatePicker;

    final Placeholder CURRENT_USER_ASSIGNEE = new Placeholder(Messages.JiraFilterDefinitionPage_Current_User);

    final Placeholder CURRENT_USER_REPORTER = new Placeholder(Messages.JiraFilterDefinitionPage_Current_User);

    private DatePicker dueEndDatePicker;

    private DatePicker dueStartDatePicker;

    private boolean firstTime = true;

    private ListViewer fixFor;

    private ListViewer issueType;

    final Placeholder NO_COMPONENT = new Placeholder(Messages.JiraFilterDefinitionPage_No_Component);

    final Placeholder NO_FIX_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_No_Fix_Version);

    final Placeholder NO_PROJECT = new Placeholder(Messages.JiraFilterDefinitionPage_No_Project);

    final Placeholder NO_REPORTED_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_No_Version);

    final Placeholder NO_REPORTER = new Placeholder(Messages.JiraFilterDefinitionPage_No_Reporter);

    private ListViewer priority;

    private ListViewer project;

    private Text queryString;

    final Placeholder RELEASED_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_Released_Versions);

    private ListViewer reportedIn;

    private Text reporter;

    private ComboViewer reporterType;

    private ListViewer resolution;

    private Button searchComments;

    private Button searchDescription;

    private Button searchEnvironment;

    private Button searchSummary;

    final Placeholder SPECIFIC_GROUP_ASSIGNEE = new Placeholder(Messages.JiraFilterDefinitionPage_Specified_Group);

    final Placeholder SPECIFIC_GROUP_REPORTER = new Placeholder(Messages.JiraFilterDefinitionPage_Specified_Group);

    final Placeholder SPECIFIC_USER_ASSIGNEE = new Placeholder(Messages.JiraFilterDefinitionPage_Specified_User);

    final Placeholder SPECIFIC_USER_REPORTER = new Placeholder(Messages.JiraFilterDefinitionPage_Specified_User);

    private ListViewer status;

    private Text titleText;

    final Placeholder UNASSIGNED = new Placeholder(Messages.JiraFilterDefinitionPage_Unassigned);

    final Placeholder UNRELEASED_VERSION = new Placeholder(Messages.JiraFilterDefinitionPage_Unreleased_Versions);

    final Placeholder UNRESOLVED = new Placeholder(Messages.JiraFilterDefinitionPage_Unresolved);

    private DatePicker updatedEndDatePicker;

    private DatePicker updatedStartDatePicker;

    private FilterDefinition workingCopy;

    private Text createdFrom;

    private Text createdTo;

    private String title;

    private Text updatedFrom;

    private Text updatedTo;

    private Text dueDateFrom;

    private Text dueDateTo;

    private boolean isPageComplete = true;

    public JiraFilterDefinitionPage(final TaskRepository repository) {
        this(repository, null);
    }

    public JiraFilterDefinitionPage(final TaskRepository repository, final IRepositoryQuery query) {
        super(Messages.JiraFilterDefinitionPage_JIRA_Query, repository, query);
        client = JiraClientFactory.getDefault().getJiraClient(repository);
        if (query != null) {
            workingCopy = (FilterDefinition) JiraUtil.getQuery(repository, client, query, false);
        } else {
            workingCopy = new FilterDefinition();
        }
        setDescription(Messages.JiraFilterDefinitionPage_Add_search_filters_to_define_query);
        setPageComplete(false);
    }

    void applyChanges() {
        if (queryString.getText().length() > 0 || searchSummary.getSelection()
                || searchDescription.getSelection() || searchEnvironment.getSelection()
                || searchComments.getSelection()) {
            workingCopy.setContentFilter(new ContentFilter(queryString.getText(),
                    searchSummary.getSelection(), searchDescription.getSelection(),
                    searchEnvironment.getSelection(), searchComments.getSelection()));
        } else {
            workingCopy.setContentFilter(null);
        }

        final IStructuredSelection projectSelection = (IStructuredSelection) project.getSelection();
        if (projectSelection.isEmpty()) {
            workingCopy.setProjectFilter(null);
        } else {
            boolean selectionContainsAll = false;
            boolean selectionContainsNone = false;
            final List<JiraProject> selectedProjects = new ArrayList<>();
            for (final Object selection : projectSelection) {
                if (ALL_PROJECTS.equals(selection)) {
                    selectionContainsAll = true;
                } else if (NO_PROJECT.equals(selection)) {
                    selectionContainsNone = true;
                } else if (selection instanceof JiraProject) {
                    selectedProjects.add((JiraProject) selection);
                }
            }
            if (selectionContainsAll) {
                workingCopy.setProjectFilter(null);
            } else if (selectedProjects.size() > 0) {
                workingCopy.setProjectFilter(new ProjectFilter(
                        selectedProjects.toArray(new JiraProject[selectedProjects.size()])));
            } else if (selectionContainsNone) {
                workingCopy.setProjectFilter(new ProjectFilter(new JiraProject[0]));
            } else {
                workingCopy.setProjectFilter(null);
            }
        }

        final IStructuredSelection reportedInSelection = (IStructuredSelection) reportedIn.getSelection();
        if (reportedInSelection.isEmpty()) {
            workingCopy.setReportedInVersionFilter(null);
        } else {
            boolean selectionContainsReleased = false;
            boolean selectionContainsUnreleased = false;
            boolean selectionContainsAll = false;
            boolean selectionContainsNone = false;

            final List<JiraVersion> selectedVersions = new ArrayList<>();

            for (final Object selection : reportedInSelection) {
                if (ANY_REPORTED_VERSION.equals(selection)) {
                    selectionContainsAll = true;
                } else if (NO_REPORTED_VERSION.equals(selection)) {
                    selectionContainsNone = true;
                } else if (RELEASED_VERSION.equals(selection)) {
                    selectionContainsReleased = true;
                } else if (UNRELEASED_VERSION.equals(selection)) {
                    selectionContainsUnreleased = true;
                } else if (selection instanceof JiraVersion) {
                    selectedVersions.add((JiraVersion) selection);
                }
            }

            if (selectionContainsAll) {
                workingCopy.setReportedInVersionFilter(null);
            } else {
                workingCopy.setReportedInVersionFilter(new VersionFilter(
                        selectedVersions.toArray(new JiraVersion[selectedVersions.size()]), selectionContainsNone,
                        selectionContainsReleased, selectionContainsUnreleased));
            }
        }

        final IStructuredSelection fixForSelection = (IStructuredSelection) fixFor.getSelection();
        if (fixForSelection.isEmpty()) {
            workingCopy.setFixForVersionFilter(null);
        } else {
            boolean selectionContainsReleased = false;
            boolean selectionContainsUnreleased = false;
            boolean selectionContainsAll = false;
            boolean selectionContainsNone = false;

            final List<JiraVersion> selectedVersions = new ArrayList<>();

            for (final Object selection : fixForSelection) {
                if (ANY_FIX_VERSION.equals(selection)) {
                    selectionContainsAll = true;
                } else if (NO_FIX_VERSION.equals(selection)) {
                    selectionContainsNone = true;
                } else if (RELEASED_VERSION.equals(selection)) {
                    selectionContainsReleased = true;
                } else if (UNRELEASED_VERSION.equals(selection)) {
                    selectionContainsUnreleased = true;
                } else if (selection instanceof JiraVersion) {
                    selectedVersions.add((JiraVersion) selection);
                }
            }

            if (selectionContainsAll) {
                workingCopy.setFixForVersionFilter(null);
            } else {
                workingCopy.setFixForVersionFilter(new VersionFilter(
                        selectedVersions.toArray(new JiraVersion[selectedVersions.size()]), selectionContainsNone,
                        selectionContainsReleased, selectionContainsUnreleased));
            }
        }

        final IStructuredSelection componentsSelection = (IStructuredSelection) components.getSelection();
        if (componentsSelection.isEmpty()) {
            workingCopy.setComponentFilter(null);
        } else {

            boolean selectionContainsAll = false;
            boolean selectionContainsNone = false;
            final List<JiraComponent> selectedComponents = new ArrayList<>();

            for (final Object selection : componentsSelection) {
                if (ANY_COMPONENT.equals(selection)) {
                    selectionContainsAll = true;
                } else if (NO_COMPONENT.equals(selection)) {
                    selectionContainsNone = true;
                } else if (selection instanceof JiraComponent) {
                    selectedComponents.add((JiraComponent) selection);
                }
            }

            if (selectionContainsAll) {
                workingCopy.setComponentFilter(null);
            } else {
                workingCopy.setComponentFilter(new ComponentFilter(
                        selectedComponents.toArray(new JiraComponent[selectedComponents.size()]), selectionContainsNone));
            }
        }

        // attributes

        // TODO support standard and subtask issue types
        final IStructuredSelection issueTypeSelection = (IStructuredSelection) issueType.getSelection();
        if (issueTypeSelection.isEmpty()) {
            workingCopy.setIssueTypeFilter(null);
        } else {
            boolean isAnyIssueTypeSelected = false;

            final List<JiraIssueType> selectedIssueTypes = new ArrayList<>();

            for (final Object selection : issueTypeSelection) {
                if (ANY_ISSUE_TYPE.equals(selection)) {
                    isAnyIssueTypeSelected = true;
                } else if (selection instanceof JiraIssueType) {
                    selectedIssueTypes.add((JiraIssueType) selection);
                }
            }

            if (isAnyIssueTypeSelected) {
                workingCopy.setIssueTypeFilter(null);
            } else {
                workingCopy.setIssueTypeFilter(new IssueTypeFilter(
                        selectedIssueTypes.toArray(new JiraIssueType[selectedIssueTypes.size()])));
            }
        }

        final IStructuredSelection reporterSelection = (IStructuredSelection) reporterType.getSelection();
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

        final IStructuredSelection assigneeSelection = (IStructuredSelection) assigneeType.getSelection();
        if (assigneeSelection.isEmpty()) {
            workingCopy.setAssignedToFilter(null);
        } else {
            if (ANY_ASSIGNEE.equals(assigneeSelection.getFirstElement())) {
                workingCopy.setAssignedToFilter(null);
            } else if (UNASSIGNED.equals(assigneeSelection.getFirstElement())) {
                workingCopy.setAssignedToFilter(new NobodyFilter());
            } else if (CURRENT_USER_ASSIGNEE.equals(assigneeSelection.getFirstElement())) {
                workingCopy.setAssignedToFilter(new CurrentUserFilter());
            } else if (SPECIFIC_GROUP_ASSIGNEE.equals(assigneeSelection.getFirstElement())) {
                workingCopy.setAssignedToFilter(new UserInGroupFilter(assignee.getText()));
            } else if (SPECIFIC_USER_ASSIGNEE.equals(assigneeSelection.getFirstElement())) {
                workingCopy.setAssignedToFilter(new SpecificUserFilter(assignee.getText()));
            } else {
                workingCopy.setAssignedToFilter(null);
            }
        }

        final IStructuredSelection statusSelection = (IStructuredSelection) status.getSelection();
        if (statusSelection.isEmpty()) {
            workingCopy.setStatusFilter(null);
        } else {
            boolean isAnyStatusSelected = false;

            final List<JiraStatus> selectedStatuses = new ArrayList<>();

            for (final Object selection : statusSelection) {
                if (ANY_STATUS.equals(selection)) {
                    isAnyStatusSelected = true;
                } else if (selection instanceof JiraStatus) {
                    selectedStatuses.add((JiraStatus) selection);
                }
            }

            if (isAnyStatusSelected) {
                workingCopy.setStatusFilter(null);
            } else {
                workingCopy.setStatusFilter(new StatusFilter(
                        selectedStatuses.toArray(new JiraStatus[selectedStatuses.size()])));
            }
        }

        final IStructuredSelection resolutionSelection = (IStructuredSelection) resolution.getSelection();
        if (resolutionSelection.isEmpty()) {
            workingCopy.setResolutionFilter(null);
        } else {
            boolean isAnyResolutionSelected = false;

            final List<JiraResolution> selectedResolutions = new ArrayList<>();

            for (final Object selection : resolutionSelection) {
                if (ANY_RESOLUTION.equals(selection)) {
                    isAnyResolutionSelected = true;
                } else if (selection instanceof JiraResolution) {
                    selectedResolutions.add((JiraResolution) selection);
                }
            }

            if (isAnyResolutionSelected) {
                workingCopy.setResolutionFilter(null);
            } else {
                workingCopy.setResolutionFilter(new ResolutionFilter(
                        selectedResolutions.toArray(new JiraResolution[selectedResolutions.size()])));
            }
        }

        final IStructuredSelection prioritySelection = (IStructuredSelection) priority.getSelection();
        if (prioritySelection.isEmpty()) {
            workingCopy.setPriorityFilter(null);
        } else {
            boolean isAnyPrioritiesSelected = false;

            final List<JiraPriority> selectedPriorities = new ArrayList<>();

            for (final Object selection : prioritySelection) {
                if (ANY_PRIORITY.equals(selection)) {
                    isAnyPrioritiesSelected = true;
                } else if (selection instanceof JiraPriority) {
                    selectedPriorities.add((JiraPriority) selection);
                }
            }

            if (isAnyPrioritiesSelected) {
                workingCopy.setPriorityFilter(null);
            } else {
                workingCopy.setPriorityFilter(new PriorityFilter(
                        selectedPriorities.toArray(new JiraPriority[selectedPriorities.size()])));
            }
        }

        workingCopy.setDueDateFilter(getRangeFilter(dueStartDatePicker, dueEndDatePicker, dueDateFrom, dueDateTo));
        workingCopy.setCreatedDateFilter(getRangeFilter(createdStartDatePicker, createdEndDatePicker, createdFrom,
                createdTo));
        workingCopy.setUpdatedDateFilter(getRangeFilter(updatedStartDatePicker, updatedEndDatePicker, updatedFrom,
                updatedTo));
    }

    @Override
    public void applyTo(final IRepositoryQuery query) {
        applyChanges();
        if (titleText != null) {
            query.setSummary(titleText.getText());
        }
        JiraUtil.setQuery(getTaskRepository(), query, workingCopy);
    }

    private void createComponentsViewer(final Composite c) {
        components = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = HEIGHT_HINT;
        gridData.widthHint = WIDTH_HINT;
        components.getControl().setLayoutData(gridData);

        components.setContentProvider(new IStructuredContentProvider() {
            private Object[] currentElements;

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return currentElements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                final JiraProject[] projects = (JiraProject[]) newInput;
                if (projects == null || projects.length == 0 || projects.length > 1) {
                    currentElements = new Object[] { ANY_COMPONENT };
                } else {
                    final Set<Object> elements = new LinkedHashSet<>();
                    elements.add(ANY_COMPONENT);
                    elements.add(NO_COMPONENT);
                    for (final JiraProject project : projects) {
                        if (project != null && project.hasDetails()) {
                            elements.addAll(Arrays.asList(project.getComponents()));
                        }
                    }
                    currentElements = elements.toArray(new Object[elements.size()]);
                }
            }

        });
        components.setLabelProvider(new ComponentLabelProvider());
        components.setInput(NO_PROJECTS);
    }

    @Override
    public void createControl(final Composite parent) {
        final Composite c = new Composite(parent, SWT.NONE);
        setControl(c);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(c);

        if (!inSearchContainer()) {
            GridDataFactory.fillDefaults().hint(800, SWT.DEFAULT).minSize(800, SWT.DEFAULT).grab(true, true).applyTo(c);

            final Label lblName = new Label(c, SWT.NONE);
            final GridData gridData = new GridData();
            lblName.setLayoutData(gridData);
            lblName.setText(Messages.JiraFilterDefinitionPage_Query_Title);

            titleText = new Text(c, SWT.BORDER);
            titleText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
            titleText.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent e) {
                    //					validatePage();
                    setPageComplete(isPageComplete());
                }
            });
        } else {
            GridDataFactory.fillDefaults().grab(true, true).applyTo(c);
        }

        {
            final Composite cc = new Composite(c, SWT.NONE);
            GridDataFactory.fillDefaults()
            .align(SWT.FILL, SWT.FILL)
            .grab(true, true)
            .hint(650, 130)
            .span(3, 1)
            .applyTo(cc);
            GridLayoutFactory.fillDefaults().numColumns(2).applyTo(cc);

            {
                final Label label = new Label(cc, SWT.NONE);
                label.setText(Messages.JiraFilterDefinitionPage_Project);
            }

            {
                final Label typeLabel = new Label(cc, SWT.NONE);
                typeLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
                typeLabel.setText(Messages.JiraFilterDefinitionPage_Type);
            }

            createProjectsViewer(cc);
            createIssueTypesViewer(cc);
        }

        createUpdateButton(c);

        final ExpandableComposite textSection = createExpandableComposite(c, "Text Search", false);
        final Composite textComposite = new Composite(textSection, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 5).applyTo(textComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textComposite);
        textSection.setClient(textComposite);
        createTextSearchContents(textComposite);
        textSection.setExpanded(inSearchContainer());

        final ExpandableComposite issueDetailsSection = createExpandableComposite(c, "Issue Details", true);

        final Composite detailsComposite = new Composite(issueDetailsSection, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 5).applyTo(detailsComposite);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(detailsComposite);
        issueDetailsSection.setClient(detailsComposite);
        createIssueDetailsContents(detailsComposite);
        issueDetailsSection.setExpanded(!inSearchContainer());

        final ExpandableComposite projectDetailsSection = createExpandableComposite(c, "Components / Versions", true);
        createProjectDetailsContents(projectDetailsSection);
        projectDetailsSection.setExpanded(!inSearchContainer());

        final ExpandableComposite datesSection = createExpandableComposite(c, "Dates and Times", false);
        final Composite datesComposite = new Composite(datesSection, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).margins(0, 5).applyTo(datesComposite);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(datesComposite);
        datesSection.setClient(datesComposite);
        createDatesContent(datesComposite);

        loadDefaults();
        Dialog.applyDialogFont(parent);
    }

    private void createProjectDetailsContents(final ExpandableComposite projectDetailsComposite) {
        final SashForm cc = new SashForm(projectDetailsComposite, SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(cc);
        {
            final Composite comp = new Composite(cc, SWT.NONE);
            final GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            comp.setLayout(gridLayout);

            new Label(comp, SWT.NONE).setText(Messages.JiraFilterDefinitionPage_Fix_For);
            createFixForViewer(comp);
        }

        {
            final Composite comp = new Composite(cc, SWT.NONE);
            final GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            comp.setLayout(gridLayout);

            new Label(comp, SWT.NONE).setText(Messages.JiraFilterDefinitionPage_In_Components);
            createComponentsViewer(comp);
        }

        {
            final Composite comp = new Composite(cc, SWT.NONE);
            final GridLayout gridLayout = new GridLayout(1, false);
            gridLayout.marginWidth = 0;
            comp.setLayout(gridLayout);

            final Label label = new Label(comp, SWT.NONE);
            label.setText(Messages.JiraFilterDefinitionPage_Reported_In);
            createReportedInViewer(comp);
        }

        cc.setWeights(1, 1, 1);
        projectDetailsComposite.setClient(cc);
    }

    private void createTextSearchContents(final Composite c) {
        final Label lblQuery = new Label(c, SWT.NONE);
        final GridData gd_lblQuery = new GridData();
        gd_lblQuery.verticalIndent = 7;
        lblQuery.setLayoutData(gd_lblQuery);
        lblQuery.setText(Messages.JiraFilterDefinitionPage_Query);
        queryString = new Text(c, SWT.BORDER);
        final GridData gd_queryString = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
        gd_queryString.verticalIndent = 7;
        queryString.setLayoutData(gd_queryString);
        // TODO put content assist here and a label describing what is
        // available

        queryString.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                // validatePage();
            }
        });

        final Label lblFields = new Label(c, SWT.NONE);
        lblFields.setText(Messages.JiraFilterDefinitionPage_FILEDS);
        lblFields.setLayoutData(new GridData());

        {
            final SelectionAdapter selectionAdapter = new SelectionAdapter() {
                @Override
                public void widgetSelected(final SelectionEvent e) {
                    // validatePage();
                }
            };

            final Composite comp = new Composite(c, SWT.NONE);
            comp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
            comp.setLayout(new FillLayout());

            searchSummary = new Button(comp, SWT.CHECK);
            searchSummary.setText(Messages.JiraFilterDefinitionPage_Summary);
            searchSummary.addSelectionListener(selectionAdapter);

            searchDescription = new Button(comp, SWT.CHECK);
            searchDescription.setText(Messages.JiraFilterDefinitionPage_Description);
            searchDescription.addSelectionListener(selectionAdapter);

            searchComments = new Button(comp, SWT.CHECK);
            searchComments.setText(Messages.JiraFilterDefinitionPage_Comments);
            searchComments.addSelectionListener(selectionAdapter);

            searchEnvironment = new Button(comp, SWT.CHECK);
            searchEnvironment.setText(Messages.JiraFilterDefinitionPage_Environment);
            searchEnvironment.addSelectionListener(selectionAdapter);
        }
    }

    private void createIssueDetailsContents(final Composite c) {
        {
            final Label reportedByLabel = new Label(c, SWT.NONE);
            reportedByLabel.setText(Messages.JiraFilterDefinitionPage_Reported_By);

            reporterType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
            final GridData gridData_1 = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gridData_1.widthHint = 133;
            reporterType.getControl().setLayoutData(gridData_1);

            reporterType.setContentProvider(new IStructuredContentProvider() {

                @Override
                public void dispose() {
                }

                @Override
                public Object[] getElements(final Object inputElement) {
                    return new Object[] { ANY_REPORTER, NO_REPORTER, CURRENT_USER_REPORTER, SPECIFIC_USER_REPORTER,
                            SPECIFIC_GROUP_REPORTER };
                }

                @Override
                public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                }

            });

            reporterType.setLabelProvider(new LabelProvider() {
                @Override
                public String getText(final Object element) {
                    return ((Placeholder) element).getText();
                }
            });

            reporterType.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    final Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (SPECIFIC_USER_REPORTER.equals(selection) || SPECIFIC_GROUP_REPORTER.equals(selection)) {
                        reporter.setEnabled(true);
                    } else {
                        reporter.setEnabled(false);
                        reporter.setText(""); //$NON-NLS-1$
                    }
                    // validatePage();
                }
            });

            reporterType.setInput(client);

            reporter = new Text(c, SWT.BORDER);
            reporter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            reporter.setEnabled(false);

            reporter.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent e) {
                    // validatePage();
                }
            });
        }

        {
            final Label assignedToLabel = new Label(c, SWT.NONE);
            assignedToLabel.setText(Messages.JiraFilterDefinitionPage_Assigned_To);

            assigneeType = new ComboViewer(c, SWT.BORDER | SWT.READ_ONLY);
            final GridData gridData_2 = new GridData(SWT.FILL, SWT.CENTER, false, false);
            gridData_2.widthHint = 133;
            assigneeType.getCombo().setLayoutData(gridData_2);

            assigneeType.setContentProvider(new IStructuredContentProvider() {

                @Override
                public void dispose() {
                }

                @Override
                public Object[] getElements(final Object inputElement) {
                    return new Object[] { ANY_ASSIGNEE, UNASSIGNED, CURRENT_USER_ASSIGNEE, SPECIFIC_USER_ASSIGNEE,
                            SPECIFIC_GROUP_ASSIGNEE };
                }

                @Override
                public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                }

            });

            assigneeType.setLabelProvider(new LabelProvider() {

                @Override
                public String getText(final Object element) {
                    return ((Placeholder) element).getText();
                }

            });

            assigneeType.addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(final SelectionChangedEvent event) {
                    final Object selection = ((IStructuredSelection) event.getSelection()).getFirstElement();
                    if (SPECIFIC_USER_ASSIGNEE.equals(selection) || SPECIFIC_GROUP_ASSIGNEE.equals(selection)) {
                        assignee.setEnabled(true);
                    } else {
                        assignee.setEnabled(false);
                        assignee.setText(""); //$NON-NLS-1$
                    }
                    // validatePage();
                }
            });

            assigneeType.setInput(client);

            assignee = new Text(c, SWT.BORDER);
            assignee.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            assignee.setEnabled(false);
            assignee.addModifyListener(new ModifyListener() {
                @Override
                public void modifyText(final ModifyEvent e) {
                    // validatePage();
                }
            });
        }

        {
            final SashForm cc = new SashForm(c, SWT.NONE);
            GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(cc);

            {
                final Composite comp = new Composite(cc, SWT.NONE);
                final GridLayout gridLayout = new GridLayout();
                gridLayout.marginHeight = 0;
                gridLayout.marginWidth = 0;
                comp.setLayout(gridLayout);

                final Label statusLabel = new Label(comp, SWT.NONE);
                statusLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
                statusLabel.setText(Messages.JiraFilterDefinitionPage_Status);

                status = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
                final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
                gridData.heightHint = HEIGHT_HINT;
                gridData.widthHint = WIDTH_HINT;
                status.getList().setLayoutData(gridData);

                status.setLabelProvider(new LabelProvider() {

                    @Override
                    public String getText(final Object element) {
                        if (element instanceof Placeholder) {
                            return ((Placeholder) element).getText();
                        }

                        return ((JiraStatus) element).getName();
                    }

                });
            }

            {
                final Composite comp = new Composite(cc, SWT.NONE);
                final GridLayout gridLayout = new GridLayout();
                gridLayout.marginHeight = 0;
                gridLayout.marginWidth = 0;
                comp.setLayout(gridLayout);

                final Label resolutionLabel = new Label(comp, SWT.NONE);
                resolutionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
                resolutionLabel.setText(Messages.JiraFilterDefinitionPage_Resolution);

                resolution = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
                final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
                gridData.heightHint = HEIGHT_HINT;
                gridData.widthHint = WIDTH_HINT;
                resolution.getList().setLayoutData(gridData);

                resolution.setLabelProvider(new LabelProvider() {

                    @Override
                    public String getText(final Object element) {
                        if (element instanceof Placeholder) {
                            return ((Placeholder) element).getText();
                        }

                        return ((JiraResolution) element).getName();
                    }

                });
            }

            {
                final Composite comp = new Composite(cc, SWT.NONE);
                final GridLayout gridLayout = new GridLayout();
                gridLayout.marginHeight = 0;
                gridLayout.marginWidth = 0;
                comp.setLayout(gridLayout);

                final Label priorityLabel = new Label(comp, SWT.NONE);
                priorityLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
                priorityLabel.setText(Messages.JiraFilterDefinitionPage_Priority);

                priority = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
                final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
                gridData.heightHint = HEIGHT_HINT;
                gridData.widthHint = WIDTH_HINT;
                priority.getList().setLayoutData(gridData);

                priority.setLabelProvider(new LabelProvider() {

                    @Override
                    public String getText(final Object element) {
                        if (element instanceof Placeholder) {
                            return ((Placeholder) element).getText();
                        }

                        return ((JiraPriority) element).getName();
                    }

                });
            }

            cc.setWeights(1, 1, 1);
        }
    }

    private void createDatesContent(final Composite c) {
        final ModifyListener wdhmLocalListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                validatePage();
            }
        };

        final DateFormat currentJiraDateTimeFormat = client.getLocalConfiguration().getDateTimeFormat();

        {
            final Label createdLabel = new Label(c, SWT.NONE);
            createdLabel.setText(Messages.JiraFilterDefinitionPage_Created + ":"); //$NON-NLS-1$

            final Composite composite = new Composite(c, SWT.NONE);
            final GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            composite.setLayout(layout);
            final GridData layoutData = new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 2);
            layoutData.widthHint = DATE_CONTROL_WIDTH_HINT;
            composite.setLayoutData(layoutData);

            createdStartDatePicker = new DatePicker(composite, SWT.BORDER,
                    Messages.JiraFilterDefinitionPage__start_date_, true, 0);
            createdStartDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults()
            .align(SWT.FILL, SWT.CENTER)
            .grab(true, false)
            .applyTo(createdStartDatePicker);
            createdEndDatePicker = new DatePicker(composite, SWT.BORDER, Messages.JiraFilterDefinitionPage__end_date_,
                    true, 0);
            createdEndDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(createdEndDatePicker);

            new Label(c, SWT.NONE);

            final Composite c1 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c1);
            final GridLayout gl = new GridLayout(2, false);
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            c1.setLayout(gl);

            final Label from = new Label(c1, SWT.NONE);
            from.setText(Messages.JiraFilterDefinitionPage_From + ":"); //$NON-NLS-1$
            createdFrom = new Text(c1, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(createdFrom);
            createdFrom.addModifyListener(wdhmLocalListener);

            final Composite c2 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c2);
            final GridLayout g2 = new GridLayout(2, false);
            g2.marginHeight = 0;
            g2.marginWidth = 0;
            c2.setLayout(g2);

            final Label to = new Label(c2, SWT.NONE);
            to.setText(Messages.JiraFilterDefinitionPage_To + ":"); //$NON-NLS-1$
            createdTo = new Text(c2, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(createdTo);
            createdTo.addModifyListener(wdhmLocalListener);
        }

        {
            final Label updatedLabel = new Label(c, SWT.NONE);
            updatedLabel.setText(Messages.JiraFilterDefinitionPage_Updated + ":"); //$NON-NLS-1$

            final Composite composite = new Composite(c, SWT.NONE);
            final GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            composite.setLayout(layout);
            final GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 2);
            layoutData.widthHint = DATE_CONTROL_WIDTH_HINT;
            composite.setLayoutData(layoutData);

            updatedStartDatePicker = new DatePicker(composite, SWT.BORDER,
                    Messages.JiraFilterDefinitionPage__start_date_, true, 0);
            updatedStartDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults()
            .align(SWT.FILL, SWT.CENTER)
            .grab(true, false)
            .applyTo(updatedStartDatePicker);
            updatedEndDatePicker = new DatePicker(composite, SWT.BORDER, Messages.JiraFilterDefinitionPage__end_date_,
                    true, 0);
            updatedEndDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(updatedEndDatePicker);

            new Label(c, SWT.NONE);

            final Composite c1 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c1);
            final GridLayout gl = new GridLayout(2, false);
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            c1.setLayout(gl);

            final Label from = new Label(c1, SWT.NONE);
            from.setText(Messages.JiraFilterDefinitionPage_From + ":"); //$NON-NLS-1$
            updatedFrom = new Text(c1, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(updatedFrom);
            updatedFrom.addModifyListener(wdhmLocalListener);

            final Composite c2 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c2);
            final GridLayout g2 = new GridLayout(2, false);
            g2.marginHeight = 0;
            g2.marginWidth = 0;
            c2.setLayout(g2);

            final Label to = new Label(c2, SWT.NONE);
            to.setText(Messages.JiraFilterDefinitionPage_To + ":"); //$NON-NLS-1$
            updatedTo = new Text(c2, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(updatedTo);
            updatedTo.addModifyListener(wdhmLocalListener);
        }

        {
            final Label dueDateLabel = new Label(c, SWT.NONE);
            dueDateLabel.setText(Messages.JiraFilterDefinitionPage_Due_Date + ":"); //$NON-NLS-1$

            final Composite composite = new Composite(c, SWT.NONE);
            final GridLayout layout = new GridLayout(2, true);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            composite.setLayout(layout);
            final GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 2);
            layoutData.widthHint = DATE_CONTROL_WIDTH_HINT;
            composite.setLayoutData(layoutData);

            dueStartDatePicker = new DatePicker(composite, SWT.BORDER, Messages.JiraFilterDefinitionPage__start_date_,
                    false, 0);
            dueStartDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(dueStartDatePicker);
            dueEndDatePicker = new DatePicker(composite, SWT.BORDER, Messages.JiraFilterDefinitionPage__end_date_,
                    false, 0);
            dueEndDatePicker.setDateFormat(currentJiraDateTimeFormat);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(dueEndDatePicker);

            new Label(c, SWT.NONE);

            final Composite c1 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c1);
            final GridLayout gl = new GridLayout(2, false);
            gl.marginHeight = 0;
            gl.marginWidth = 0;
            c1.setLayout(gl);

            final Label from = new Label(c1, SWT.NONE);
            from.setText(Messages.JiraFilterDefinitionPage_From + ":"); //$NON-NLS-1$
            dueDateFrom = new Text(c1, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(dueDateFrom);
            dueDateFrom.addModifyListener(wdhmLocalListener);

            final Composite c2 = new Composite(composite, SWT.NONE);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(c2);
            final GridLayout g2 = new GridLayout(2, false);
            g2.marginHeight = 0;
            g2.marginWidth = 0;
            c2.setLayout(g2);

            final Label to = new Label(c2, SWT.NONE);
            to.setText(Messages.JiraFilterDefinitionPage_To + ":"); //$NON-NLS-1$
            dueDateTo = new Text(c2, SWT.BORDER);
            GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(dueDateTo);
            dueDateTo.addModifyListener(wdhmLocalListener);
        }

        {
            new Label(c, SWT.NONE);

            final Composite cc = new Composite(c, SWT.NONE);
            final GridLayout gl = new GridLayout();
            gl.marginWidth = 0;
            gl.marginHeight = 20;
            cc.setLayout(gl);
            GridDataFactory.fillDefaults().span(2, 1).applyTo(cc);

            final Label explanation = new Label(cc, SWT.NONE);
            explanation.setText(NLS.bind(Messages.JiraWdhmExplanation, "'" + Messages.JiraFilterDefinitionPage_From //$NON-NLS-1$
                    + "' " + Messages.JiraFilterDefinitionPage_And + " '" + Messages.JiraFilterDefinitionPage_To + "'")); //$NON-NLS-1$

        }
    }

    private void adjustLayoutData(final Control control, final boolean shouldGrabVertical) {
        GridDataFactory.fillDefaults()
        .indent(0, 5)
        .grab(true, shouldGrabVertical)
        .span(3, SWT.DEFAULT)
        .applyTo(control);
    }

    private ExpandableComposite createExpandableComposite(final Composite parentControl, final String title,
            final boolean shouldGrabVertical) {
        final ExpandableComposite section = new ExpandableComposite(parentControl, ExpandableComposite.TWISTIE
                | ExpandableComposite.CLIENT_INDENT | ExpandableComposite.COMPACT) {

            @Override
            public void setExpanded(final boolean expanded) {
                adjustLayoutData(this, expanded && shouldGrabVertical);
                super.setExpanded(expanded);
            }

        };
        section.clientVerticalSpacing = 0;
        section.setBackground(parentControl.getBackground());
        section.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
        section.addExpansionListener(new ExpansionAdapter() {
            @Override
            public void expansionStateChanged(final ExpansionEvent e) {
                if (shouldGrabVertical) {
                    adjustLayoutData(section, e.getState());
                }
                parentControl.layout(true);
                getControl().getShell().pack();
            }
        });
        section.setText(title);
        adjustLayoutData(section, false);
        return section;
    }

    private void createIssueTypesViewer(final Composite comp) {
        issueType = new ListViewer(comp, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = HEIGHT_HINT;
        gridData.widthHint = WIDTH_HINT;
        issueType.getList().setLayoutData(gridData);

        issueType.setContentProvider(new IStructuredContentProvider() {
            private Object[] currentElements;

            @Override
            public Object[] getElements(final Object inputElement) {
                return currentElements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                final JiraProject[] projects = (JiraProject[]) newInput;
                JiraIssueType[] types = null;

                if (projects == null || projects.length == 0 || projects.length > 1) {
                    types = client.getCache().getIssueTypes();
                } else if (projects[0].hasDetails()) {
                    types = projects[0].getIssueTypes();
                }

                if (types != null) {
                    final Object[] elements = new Object[types.length + 1];
                    System.arraycopy(types, 0, elements, 1, types.length);
                    elements[0] = ANY_ISSUE_TYPE;
                    currentElements = elements;
                }
            }

            @Override
            public void dispose() {
            }
        });

        issueType.setLabelProvider(new LabelProvider() {

            @Override
            public String getText(final Object element) {
                if (element instanceof Placeholder) {
                    return ((Placeholder) element).getText();
                }

                return ((JiraIssueType) element).getName();
            }

        });

        issueType.setInput(NO_PROJECTS);
    }

    //	@Override
    //	public boolean isPageComplete() {
    //		// XXX workaround for bad implementation of AbstractRepositoryQueryPage.isPageComplete()
    //		String errorMessage = getErrorMessage();
    //		boolean isPageComplete = super.isPageComplete();
    //		if (errorMessage != null) {
    //			setErrorMessage(errorMessage);
    //		}
    //		return isPageComplete;
    //	}
    //
    //	void validatePage() {
    //		if (title != null && !title.getText().equals("")) {
    //			setErrorMessage(null);
    //			setPageComplete(true);
    //		} else {
    //			setErrorMessage("Query title is mandatory");
    //			setPageComplete(false);
    //		}
    //	}

    private void createFixForViewer(final Composite c) {
        fixFor = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = HEIGHT_HINT;
        gridData.widthHint = WIDTH_HINT;
        fixFor.getControl().setLayoutData(gridData);

        fixFor.setContentProvider(new IStructuredContentProvider() {
            private Object[] currentElements;

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return currentElements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                final JiraProject[] projects = (JiraProject[]) newInput;
                if (projects == null || projects.length == 0 || projects.length > 1) {
                    currentElements = new Object[] { ANY_FIX_VERSION };
                } else {
                    final List<Object> elements = new ArrayList<>();
                    elements.add(ANY_FIX_VERSION);
                    elements.add(NO_FIX_VERSION);

                    final Set<JiraVersion> releasedVersions = new LinkedHashSet<>();
                    final Set<JiraVersion> unreleasedVersions = new LinkedHashSet<>();
                    for (final JiraProject project : projects) {
                        if (project != null && project.hasDetails()) {
                            releasedVersions.addAll(Arrays.asList(project.getReleasedVersions(false)));
                            unreleasedVersions.addAll(Arrays.asList(project.getUnreleasedVersions(false)));
                        }
                    }

                    elements.add(RELEASED_VERSION);
                    elements.addAll(releasedVersions);
                    elements.add(UNRELEASED_VERSION);
                    elements.addAll(unreleasedVersions);

                    currentElements = elements.toArray(new Object[elements.size()]);
                }
            }
        });
        fixFor.setLabelProvider(new VersionLabelProvider());
        fixFor.setInput(NO_PROJECTS);
    }

    private void createProjectsViewer(final Composite c) {
        project = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = HEIGHT_HINT;
        gridData.widthHint = WIDTH_HINT;
        project.getControl().setLayoutData(gridData);

        project.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(final Object element) {
                if (element instanceof Placeholder) {
                    return ((Placeholder) element).getText();
                }
                return ((JiraProject) element).getName();
            }
        });

        project.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(final SelectionChangedEvent event) {
                final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                final List<JiraProject> selectedProjects = new ArrayList<>();
                if (!selection.isEmpty()) {
                    for (final Object sel : selection) {
                        if (!(sel instanceof Placeholder)) {
                            selectedProjects.add((JiraProject) sel);
                        }
                    }
                }
                updateCurrentProjects(selectedProjects.toArray(new JiraProject[selectedProjects.size()]));
                // validatePage();
            }
        });
    }

    private void createReportedInViewer(final Composite c) {
        reportedIn = new ListViewer(c, SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.H_SCROLL);
        final GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = HEIGHT_HINT;
        gridData.widthHint = WIDTH_HINT;
        reportedIn.getControl().setLayoutData(gridData);

        reportedIn.setContentProvider(new IStructuredContentProvider() {
            private Object[] currentElements;

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                return currentElements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
                final JiraProject[] projects = (JiraProject[]) newInput;
                if (projects == null || projects.length == 0 || projects.length > 1) {
                    currentElements = new Object[] { ANY_REPORTED_VERSION };
                } else {
                    final List<Object> elements = new ArrayList<>();
                    elements.add(ANY_REPORTED_VERSION);
                    elements.add(NO_REPORTED_VERSION);

                    final Set<Object> releasedVersions = new LinkedHashSet<>();
                    final Set<Object> unreleasedVersions = new LinkedHashSet<>();

                    for (final JiraProject project : projects) {
                        if (project != null && project.hasDetails()) {
                            releasedVersions.addAll(Arrays.asList(project.getReleasedVersions(false)));
                            unreleasedVersions.addAll(Arrays.asList(project.getUnreleasedVersions(false)));
                        }
                    }

                    elements.add(RELEASED_VERSION);
                    elements.addAll(releasedVersions);
                    elements.add(UNRELEASED_VERSION);
                    elements.addAll(unreleasedVersions);

                    currentElements = elements.toArray(new Object[elements.size()]);
                }
            }

        });
        reportedIn.setLabelProvider(new VersionLabelProvider());
        reportedIn.setInput(NO_PROJECTS);
    }

    protected void createUpdateButton(final Composite control) {
        final Button updateButton = new Button(control, SWT.PUSH);
        updateButton.setText(Messages.JiraFilterDefinitionPage_Update_Attributes_from_Repository);
        updateButton.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 3, 1));
        updateButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                applyChanges();
                updateAttributesFromRepository(true);
                loadFromWorkingCopy();
            }
        });
    }

    @Override
    public IDialogSettings getDialogSettings() {
        final IDialogSettings settings = JiraUiPlugin.getDefault().getDialogSettings();
        IDialogSettings dialogSettings = settings.getSection(PAGE_NAME);
        if (dialogSettings == null) {
            dialogSettings = settings.addNewSection(PAGE_NAME);
        }
        return dialogSettings;
    }

    @Override
    public String getQueryTitle() {
        return titleText != null ? titleText.getText() : ""; //$NON-NLS-1$
    }

    private DateRangeFilter getRangeFilter(final DatePicker startDatePicker, final DatePicker endDatePicker, final Text fromField,
            final Text toField) {
        final Calendar startDate = startDatePicker.getDate();
        final Calendar endDate = endDatePicker.getDate();
        return new DateRangeFilter(startDate == null || startDate.getTime() == null ? null : startDate.getTime().toInstant(),
                endDate == null || endDate.getTime() == null ? null : endDate.getTime().toInstant(),
                        fromField.getText(), toField.getText());
    }

    private void initializeContentProviders() {
        project.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                final JiraClient server = (JiraClient) inputElement;
                final Object[] elements = new Object[server.getCache().getProjects().length + 1];
                elements[0] = ALL_PROJECTS;
                System.arraycopy(server.getCache().getProjects(), 0, elements, 1,
                        server.getCache().getProjects().length);
                return elements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }

        });
        project.setInput(client);

        status.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                final JiraClient server = (JiraClient) inputElement;
                final Object[] elements = new Object[server.getCache().getStatuses().length + 1];
                elements[0] = ANY_STATUS;
                System.arraycopy(server.getCache().getStatuses(), 0, elements, 1,
                        server.getCache().getStatuses().length);

                return elements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }
        });
        status.setInput(client);

        resolution.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                final JiraClient server = (JiraClient) inputElement;
                final Object[] elements = new Object[server.getCache().getResolutions().length + 2];
                elements[0] = ANY_RESOLUTION;
                elements[1] = UNRESOLVED;
                System.arraycopy(server.getCache().getResolutions(), 0, elements, 2,
                        server.getCache().getResolutions().length);

                return elements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }
        });
        resolution.setInput(client);

        priority.setContentProvider(new IStructuredContentProvider() {

            @Override
            public void dispose() {
            }

            @Override
            public Object[] getElements(final Object inputElement) {
                final JiraClient client = (JiraClient) inputElement;
                final Object[] elements = new Object[client.getCache().getPriorities().length + 1];
                elements[0] = ANY_PRIORITY;
                System.arraycopy(client.getCache().getPriorities(), 0, elements, 1,
                        client.getCache().getPriorities().length);

                return elements;
            }

            @Override
            public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
            }
        });
        priority.setInput(client);
    }

    private void initializePage() {
        updateAttributesFromRepository(false);
        if (inSearchContainer()) {
            restoreWidgetValues();
        }

        if (titleText != null) {
            if (getQuery() != null) {
                titleText.setText(getQuery().getSummary());
            } else if (title != null) {
                titleText.setText(title);
            }
        }

        loadFromWorkingCopy();
    }

    private void loadDefaults() {
        // project.setSelection(new StructuredSelection(new Placeholder("All
        // Projects")));
        searchSummary.setSelection(true);
        searchDescription.setSelection(true);

        // issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE));
        // reporterType.setSelection(new StructuredSelection(ANY_REPORTER));
        // assigneeType.setSelection(new StructuredSelection(ANY_ASSIGNEE));
        // status.setSelection(new StructuredSelection(ANY_STATUS));
        // resolution.setSelection(new StructuredSelection(ANY_RESOLUTION));
        // priority.setSelection(new StructuredSelection(ANY_PRIORITY));
    }

    private void loadFromWorkingCopy() {
        if (workingCopy.getProjectFilter() != null) {
            project.setSelection(new StructuredSelection(workingCopy.getProjectFilter().getProjects()), true);
        } else {
            project.setSelection(new StructuredSelection(ALL_PROJECTS), true);
        }

        if (workingCopy.getFixForVersionFilter() != null) {

            final List<Object> versions = new ArrayList<>();

            if (workingCopy.getFixForVersionFilter().hasNoVersion()) {
                versions.add(NO_FIX_VERSION);
            }
            if (workingCopy.getFixForVersionFilter().isReleasedVersions()) {
                versions.add(RELEASED_VERSION);
            }
            if (workingCopy.getFixForVersionFilter().isUnreleasedVersions()) {
                versions.add(UNRELEASED_VERSION);
            }
            if (workingCopy.getFixForVersionFilter().getVersions() != null) {
                versions.addAll(Arrays.asList(workingCopy.getFixForVersionFilter().getVersions()));
            }

            fixFor.setSelection(new StructuredSelection(versions), true);
        } else {
            fixFor.setSelection(new StructuredSelection(ANY_FIX_VERSION), true);
        }

        if (workingCopy.getReportedInVersionFilter() != null) {

            final List<Object> versions = new ArrayList<>();

            if (workingCopy.getReportedInVersionFilter().hasNoVersion()) {
                versions.add(NO_REPORTED_VERSION);
            }
            if (workingCopy.getReportedInVersionFilter().isReleasedVersions()) {
                versions.add(RELEASED_VERSION);
            }
            if (workingCopy.getReportedInVersionFilter().isUnreleasedVersions()) {
                versions.add(UNRELEASED_VERSION);
            }

            if (workingCopy.getReportedInVersionFilter().getVersions() != null) {
                versions.addAll(Arrays.asList(workingCopy.getReportedInVersionFilter().getVersions()));
            }

            reportedIn.setSelection(new StructuredSelection(versions), true);

        } else {
            reportedIn.setSelection(new StructuredSelection(ANY_REPORTED_VERSION), true);
        }

        if (workingCopy.getContentFilter() != null) {
            queryString.setText(workingCopy.getContentFilter().getQueryString());
            searchComments.setSelection(workingCopy.getContentFilter().isSearchingComments());
            searchDescription.setSelection(workingCopy.getContentFilter().isSearchingDescription());
            searchEnvironment.setSelection(workingCopy.getContentFilter().isSearchingEnvironment());
            searchSummary.setSelection(workingCopy.getContentFilter().isSearchingSummary());
        }

        if (workingCopy.getComponentFilter() != null) {
            final List<Object> components = new ArrayList<>();
            if (workingCopy.getComponentFilter().hasNoComponent()) {
                components.add(NO_COMPONENT);
            }

            if (workingCopy.getComponentFilter().getComponents() != null) {
                components.addAll(Arrays.asList(workingCopy.getComponentFilter().getComponents()));
            }

            this.components.setSelection(new StructuredSelection(components), true);

        } else {
            components.setSelection(new StructuredSelection(ANY_COMPONENT), true);
        }

        // attributes

        if (workingCopy.getIssueTypeFilter() != null) {
            issueType.setSelection(new StructuredSelection(workingCopy.getIssueTypeFilter().getIsueTypes()), true);
        } else {
            issueType.setSelection(new StructuredSelection(ANY_ISSUE_TYPE), true);
        }

        if (workingCopy.getReportedByFilter() != null) {
            final UserFilter reportedByFilter = workingCopy.getReportedByFilter();
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
            final UserFilter assignedToFilter = workingCopy.getAssignedToFilter();
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
            status.setSelection(new StructuredSelection(workingCopy.getStatusFilter().getStatuses()), true);
        } else {
            status.setSelection(new StructuredSelection(ANY_STATUS), true);
        }

        if (workingCopy.getResolutionFilter() != null) {
            final JiraResolution[] resolutions = workingCopy.getResolutionFilter().getResolutions();
            if (resolutions.length == 0) {
                resolution.setSelection(new StructuredSelection(UNRESOLVED), true);
            } else {
                resolution.setSelection(new StructuredSelection(resolutions), true);
            }
        } else {
            resolution.setSelection(new StructuredSelection(ANY_RESOLUTION), true);
        }

        if (workingCopy.getPriorityFilter() != null) {
            priority.setSelection(new StructuredSelection(workingCopy.getPriorityFilter().getPriorities()), true);
        } else {
            priority.setSelection(new StructuredSelection(ANY_PRIORITY), true);
        }

        setDateRange(workingCopy.getCreatedDateFilter(), createdStartDatePicker, createdEndDatePicker);

        if (workingCopy.getCreatedDateFilter() != null && workingCopy.getCreatedDateFilter() instanceof DateRangeFilter) {
            final DateRangeFilter range = (DateRangeFilter) workingCopy.getCreatedDateFilter();
            createdFrom.setText(range.getFrom() == null ? "" : range.getFrom()); //$NON-NLS-1$
            createdTo.setText(range.getTo() == null ? "" : range.getTo()); //$NON-NLS-1$
        } else {
            createdFrom.setText(""); //$NON-NLS-1$
            createdTo.setText(""); //$NON-NLS-1$
        }

        setDateRange(workingCopy.getUpdatedDateFilter(), updatedStartDatePicker, updatedEndDatePicker);

        if (workingCopy.getUpdatedDateFilter() != null && workingCopy.getUpdatedDateFilter() instanceof DateRangeFilter) {
            final DateRangeFilter range = (DateRangeFilter) workingCopy.getUpdatedDateFilter();
            updatedFrom.setText(range.getFrom() == null ? "" : range.getFrom()); //$NON-NLS-1$
            updatedTo.setText(range.getTo() == null ? "" : range.getTo()); //$NON-NLS-1$
        } else {
            updatedFrom.setText(""); //$NON-NLS-1$
            updatedTo.setText(""); //$NON-NLS-1$
        }

        setDateRange(workingCopy.getDueDateFilter(), dueStartDatePicker, dueEndDatePicker);

        if (workingCopy.getDueDateFilter() != null && workingCopy.getDueDateFilter() instanceof DateRangeFilter) {
            final DateRangeFilter range = (DateRangeFilter) workingCopy.getDueDateFilter();
            dueDateFrom.setText(range.getFrom() == null ? "" : range.getFrom()); //$NON-NLS-1$
            dueDateTo.setText(range.getTo() == null ? "" : range.getTo()); //$NON-NLS-1$
        } else {
            dueDateFrom.setText(""); //$NON-NLS-1$
            dueDateTo.setText(""); //$NON-NLS-1$
        }
    }

    @Override
    public boolean performSearch() {
        if (inSearchContainer()) {
            saveState();
        }
        return super.performSearch();
    }

    private boolean restoreWidgetValues() {
        final IDialogSettings settings = getDialogSettings();
        final String searchUrl = settings.get(SEARCH_URL_ID + "." + getTaskRepository().getRepositoryUrl()); //$NON-NLS-1$
        if (searchUrl == null) {
            return false;
        }
        final FilterDefinitionConverter converter = new FilterDefinitionConverter(getTaskRepository().getCharacterEncoding(),
                client.getLocalConfiguration().getDateTimeFormat());
        workingCopy = converter.toFilter(client, searchUrl, false);
        return true;
    }

    @Override
    public void saveState() {
        final String repoId = "." + getTaskRepository().getRepositoryUrl(); //$NON-NLS-1$
        final IDialogSettings settings = getDialogSettings();
        settings.put(SEARCH_URL_ID + repoId, createQuery().getUrl());
    }

    private void setDateRange(final DateFilter dateFilter, final DatePicker startDatePicker, final DatePicker endDatePicker) {
        if (dateFilter instanceof DateRangeFilter) {
            final DateRangeFilter rangeFilter = (DateRangeFilter) dateFilter;

            if (rangeFilter.getFromDate() != null) {
                final Calendar c1 = Calendar.getInstance();
                c1.setTime(Date.from(rangeFilter.getFromDate()));
                startDatePicker.setDate(c1);
            } else {
                startDatePicker.setDate(null);
            }

            if (rangeFilter.getToDate() != null) {
                final Calendar c2 = Calendar.getInstance();
                c2.setTime(Date.from(rangeFilter.getToDate()));
                endDatePicker.setDate(c2);
            } else {
                endDatePicker.setDate(null);
            }
        }
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);

        if (getSearchContainer() != null) {
            getSearchContainer().setPerformActionEnabled(true);
        }

        if (visible && firstTime) {
            firstTime = false;
            if (!client.getCache().hasDetails()) {
                // delay the execution so the dialog's progress bar is visible
                // when the attributes are updated
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (getControl() != null && !getControl().isDisposed()) {
                            initializePage();
                        }
                    }
                });
            } else {
                // no remote connection is needed to get attributes therefore do
                // not use delayed execution to avoid flickering
                initializePage();
            }
        }
    }

    private void updateAttributesFromRepository(final boolean force) {
        if (!client.getCache().hasDetails() || force) {
            JiraProject[] projects = {};
            final IStructuredSelection selection = (IStructuredSelection) project.getSelection();
            if (selection.getFirstElement() instanceof JiraProject) {
                projects = new JiraProject[] { (JiraProject) selection.getFirstElement() };
            }
            internalUpdate(true, projects);
        }

        initializeContentProviders();
    }

    private void internalUpdate(final boolean updateProjectList, final JiraProject[] projects) {
        final ICoreRunnable runnable = new ICoreRunnable() {
            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                int size = projects.length;
                if (updateProjectList) {
                    size++;
                }
                final SubMonitor submonitor = SubMonitor.convert(monitor,
                        Messages.JiraFilterDefinitionPage_Update_Attributes_from_Repository, size);
                try {
                    final JiraClient client = JiraClientFactory.getDefault().getJiraClient(getTaskRepository());
                    if (updateProjectList) {
                        client.getCache().refreshDetails(submonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
                    }
                    for (final JiraProject project : projects) {
                        client.getCache().refreshProjectDetails(project.getId(),
                                submonitor.newChild(1, SubMonitor.SUPPRESS_NONE));
                    }
                } catch (final JiraException e) {
                    throw new CoreException(JiraCorePlugin.toStatus(getTaskRepository(), e));
                }
            }
        };

        IRunnableContext context = getContainer();
        if (context == null) {
            context = getSearchContainer().getRunnableContext();
        }
        if (context == null) {
            context = PlatformUI.getWorkbench().getProgressService();
        }
        try {
            CommonUiUtil.run(context, runnable);
        } catch (final CoreException e) {
            setErrorMessage(NLS.bind( //
                    Messages.JiraFilterDefinitionPage_Error_updating_attributes_X, e.getMessage()));
        } catch (final OperationCanceledException e) {
            // ignore
        }
    }

    void updateCurrentProjects(final JiraProject[] projects) {
        List<JiraProject> staleProjects = null;
        for (final JiraProject project : projects) {
            if (!project.hasDetails()) {
                if (staleProjects == null) {
                    staleProjects = new ArrayList<>();
                }
                staleProjects.add(project);
            }
        }
        if (staleProjects != null) {
            internalUpdate(false, staleProjects.toArray(new JiraProject[0]));
        }

        fixFor.setInput(projects);
        components.setInput(projects);
        reportedIn.setInput(projects);
        issueType.setInput(projects);
    }

    private void setQueryName(final String queryName) {
        if (titleText == null) {
            title = queryName;
        } else {
            titleText.setText(queryName);
        }
    }

    public void setCreatedRecently() {
        workingCopy = new FilterDefinition();

        setQueryName(Messages.JiraNamedFilterPage_Predefined_filter_added_recently);
        workingCopy.setCreatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$

        if (!firstTime) {
            loadFromWorkingCopy();
        }

    }

    public void setUpdatedRecently() {
        workingCopy = new FilterDefinition();

        setQueryName(Messages.JiraNamedFilterPage_Predefined_filter_updated_recently);
        workingCopy.setUpdatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$

        if (!firstTime) {
            loadFromWorkingCopy();
        }
    }

    public void setResolvedRecently() {
        workingCopy = new FilterDefinition();

        setQueryName(Messages.JiraNamedFilterPage_Predefined_filter_resolved_recently);
        workingCopy.setUpdatedDateFilter(new DateRangeFilter(null, null, "-1w", "")); //$NON-NLS-1$//$NON-NLS-2$

        final List<JiraStatus> statuses = new ArrayList<>();

        for (final JiraStatus status : client.getCache().getStatuses()) {
            if (JIRA_STATUS_RESOLVED.equals(status.getId()) || JIRA_STATUS_CLOSED.equals(status.getId())) {
                statuses.add(status);
            }
        }
        workingCopy.setStatusFilter(new StatusFilter(statuses.toArray(new JiraStatus[statuses.size()])));

        if (!firstTime) {
            loadFromWorkingCopy();
        }
    }

    public void setAssignedToMe() {
        workingCopy = new FilterDefinition();

        setQueryName(Messages.JiraNamedFilterPage_Predefined_filter_assigned_to_me);

        // empty (but not null) resolution filter means UNRESOLVED
        workingCopy.setResolutionFilter(new ResolutionFilter(new JiraResolution[0]));

        workingCopy.setAssignedToFilter(new CurrentUserFilter());

        if (!firstTime) {
            loadFromWorkingCopy();
        }
    }

    public void setReportedByMe() {
        workingCopy = new FilterDefinition();

        setQueryName(Messages.JiraNamedFilterPage_Predefined_filter_reported_by_me);

        workingCopy.setReportedByFilter(new CurrentUserFilter());

        if (!firstTime) {
            loadFromWorkingCopy();
        }
    }

    @Override
    public boolean isPageComplete() {
        return isPageComplete && super.isPageComplete();
    }

    private void validatePage() {
        if (!super.isPageComplete()) {
            return;
        }

        setErrorMessage(null);

        isPageComplete = true;

        if (!WdhmUtil.isValid(createdFrom.getText()) || !WdhmUtil.isValid(createdTo.getText())) {
            isPageComplete = false;
            setErrorMessage(Messages.JiraIncorrectWdhmValue + Messages.JiraFilterDefinitionPage_Created);
        } else if (!WdhmUtil.isValid(updatedFrom.getText()) || !WdhmUtil.isValid(updatedTo.getText())) {
            isPageComplete = false;
            setErrorMessage(Messages.JiraIncorrectWdhmValue + Messages.JiraFilterDefinitionPage_Updated);
        } else if (!WdhmUtil.isValid(dueDateFrom.getText()) || !WdhmUtil.isValid(dueDateTo.getText())) {
            isPageComplete = false;
            setErrorMessage(Messages.JiraIncorrectWdhmValue + Messages.JiraFilterDefinitionPage_Due_Date);
        }

        setPageComplete(isPageComplete);

    }

}
