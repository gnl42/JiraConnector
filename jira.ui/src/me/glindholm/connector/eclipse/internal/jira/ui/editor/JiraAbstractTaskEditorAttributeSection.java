/*******************************************************************************
 * Copyright (c) 2004, 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Perforce - fixes for bug 318396
 *******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractTaskEditorSection;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.notifications.TaskDiffUtil;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.internal.tasks.ui.views.UpdateRepositoryConfigurationAction;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.sync.TaskJob;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.progress.IProgressConstants2;

/**
 * Editor part that shows {@link TaskAttribute}s in a two column layout.
 *
 * @author Steffen Pingel
 * @author Kevin Sawicki
 */
public abstract class JiraAbstractTaskEditorAttributeSection extends AbstractTaskEditorSection {

    private static final int LABEL_WIDTH = 170;

    private static final int COLUMN_WIDTH = 250;

    private static final int COLUMN_GAP = 20;

    private static final int MULTI_COLUMN_WIDTH = COLUMN_WIDTH + 5 + COLUMN_GAP + LABEL_WIDTH + 5 + COLUMN_WIDTH;

    private static final int MULTI_ROW_HEIGHT = 55;

    private List<AbstractAttributeEditor> attributeEditors;

    private boolean hasIncoming;

    private Composite attributesComposite;

    private boolean needsRefresh;

    public JiraAbstractTaskEditorAttributeSection() {
    }

    @Override
    public void createControl(Composite parent, final FormToolkit toolkit) {
        initialize();
        super.createControl(parent, toolkit);
    }

    public boolean hasIncoming() {
        return hasIncoming;
    }

    public void selectReveal(TaskAttribute attribute) {
        if (attribute == null) {
            return;
        }
        if (!getSection().isExpanded()) {
            CommonFormUtil.setExpanded(getSection(), true);
        }
        EditorUtil.reveal(getTaskEditorPage().getManagedForm().getForm(), attribute.getId());
    }

    @Override
    public boolean setFormInput(Object input) {
        if (input instanceof String) {
            String text = (String) input;
            Collection<TaskAttribute> attributes = getAttributes();
            for (TaskAttribute attribute : attributes) {
                if (text.equals(attribute.getId())) {
                    selectReveal(attribute);
                }
            }
        }
        return super.setFormInput(input);
    }

    private void createAttributeControls(Composite attributesComposite, FormToolkit toolkit, int columnCount) {
        int currentColumn = 1;
        int currentPriority = 0;
        for (AbstractAttributeEditor attributeEditor : attributeEditors) {
            int priority = attributeEditor.getLayoutHint() != null
                    ? attributeEditor.getLayoutHint().getPriority()
                            : LayoutHint.DEFAULT_PRIORITY;
            if (priority != currentPriority) {
                currentPriority = priority;
                if (currentColumn > 1) {
                    while (currentColumn <= columnCount) {
                        getManagedForm().getToolkit().createLabel(attributesComposite, ""); //$NON-NLS-1$
                        currentColumn++;
                    }
                    currentColumn = 1;
                }
            }

            if (attributeEditor.hasLabel()) {
                attributeEditor.createLabelControl(attributesComposite, toolkit);
                Label label = attributeEditor.getLabelControl();
                String text = label.getText();
                String shortenText = TaskDiffUtil.shortenText(label, text, LABEL_WIDTH);
                label.setText(shortenText);
                if (!text.equals(shortenText)) {
                    label.setToolTipText(text);
                }
                GridData gd = GridDataFactory.fillDefaults()
                        .align(SWT.RIGHT, SWT.CENTER)
                        .hint(LABEL_WIDTH, SWT.DEFAULT)
                        .create();
                if (currentColumn > 1) {
                    gd.horizontalIndent = COLUMN_GAP;
                    gd.widthHint = LABEL_WIDTH + COLUMN_GAP;
                }
                label.setLayoutData(gd);
                currentColumn++;
            }

            attributeEditor.createControl(attributesComposite, toolkit);
            LayoutHint layoutHint = attributeEditor.getLayoutHint();
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
            RowSpan rowSpan = layoutHint != null && layoutHint.rowSpan != null ? layoutHint.rowSpan : RowSpan.SINGLE;
            ColumnSpan columnSpan = layoutHint != null && layoutHint.columnSpan != null
                    ? layoutHint.columnSpan
                            : ColumnSpan.SINGLE;
            gd.horizontalIndent = 1;// prevent clipping of decorators on Windows
            if (rowSpan == RowSpan.SINGLE && columnSpan == ColumnSpan.SINGLE) {
                gd.widthHint = COLUMN_WIDTH;
                gd.horizontalSpan = 1;
            } else {
                if (rowSpan == RowSpan.MULTIPLE) {
                    gd.heightHint = MULTI_ROW_HEIGHT;
                }
                if (columnSpan == ColumnSpan.SINGLE) {
                    gd.widthHint = COLUMN_WIDTH;
                    gd.horizontalSpan = 1;
                } else {
                    gd.widthHint = MULTI_COLUMN_WIDTH;
                    gd.horizontalSpan = columnCount - currentColumn + 1;
                }
            }
            attributeEditor.getControl().setLayoutData(gd);

            getTaskEditorPage().getAttributeEditorToolkit().adapt(attributeEditor);

            currentColumn += gd.horizontalSpan;
            currentColumn %= columnCount;
        }
    }

    private void initialize() {
        attributeEditors = new ArrayList<>();
        hasIncoming = false;

        Collection<TaskAttribute> attributes = getAttributes();
        for (TaskAttribute attribute : attributes) {
            AbstractAttributeEditor attributeEditor = createAttributeEditor(attribute);
            if (attributeEditor != null) {
                attributeEditors.add(attributeEditor);
                if (getModel().hasIncomingChanges(attribute)) {
                    hasIncoming = true;
                }
            }
        }

        Comparator<AbstractAttributeEditor> attributeSorter = createAttributeEditorSorter();
        if (attributeSorter != null) {
            Collections.sort(attributeEditors, attributeSorter);
        }
    }

    /**
     * Create a comparator by which attribute editors will be sorted. By default attribute editors are sorted by layout
     * hint priority. Subclasses may override this method to sort attribute editors in a custom way.
     *
     * @return comparator for {@link AbstractAttributeEditor} objects
     */
    protected Comparator<AbstractAttributeEditor> createAttributeEditorSorter() {
        return new Comparator<>() {
            @Override
            public int compare(AbstractAttributeEditor o1, AbstractAttributeEditor o2) {
                int p1 = o1.getLayoutHint() != null ? o1.getLayoutHint().getPriority() : LayoutHint.DEFAULT_PRIORITY;
                int p2 = o2.getLayoutHint() != null ? o2.getLayoutHint().getPriority() : LayoutHint.DEFAULT_PRIORITY;
                return p1 - p2;
            }
        };
    }

    @Override
    protected Control createContent(FormToolkit toolkit, Composite parent) {
        attributesComposite = toolkit.createComposite(parent);
        attributesComposite.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Control focus = event.display.getFocusControl();
                if (focus instanceof Text && ((Text) focus).getEditable() == false) {
                    getManagedForm().getForm().setFocus();
                }
            }
        });

        GridLayout attributesLayout = EditorUtil.createSectionClientLayout();
        attributesLayout.numColumns = 4;
        attributesLayout.horizontalSpacing = 9;
        attributesLayout.verticalSpacing = 6;
        attributesComposite.setLayout(attributesLayout);

        GridData attributesData = new GridData(GridData.FILL_BOTH);
        attributesData.horizontalSpan = 1;
        attributesData.grabExcessVerticalSpace = false;
        attributesComposite.setLayoutData(attributesData);

        createAttributeControls(attributesComposite, toolkit, attributesLayout.numColumns);
        toolkit.paintBordersFor(attributesComposite);

        return attributesComposite;
    }

    protected IAction doCreateRefreshAction() {
        UpdateRepositoryConfigurationAction repositoryConfigRefresh = new UpdateRepositoryConfigurationAction() {
            @Override
            public void run() {
                getTaskEditorPage().showEditorBusy(true);
                final TaskJob job = TasksUiInternal.getJobFactory().createUpdateRepositoryConfigurationJob(
                        getTaskEditorPage().getConnector(), getTaskEditorPage().getTaskRepository(),
                        getTaskEditorPage().getTask());
                job.addJobChangeListener(new JobChangeAdapter() {
                    @Override
                    public void done(IJobChangeEvent event) {
                        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                getTaskEditorPage().showEditorBusy(false);
                                if (job.getStatus() != null) {
                                    getTaskEditorPage().getTaskEditor().setStatus(
                                            org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorAttributePart_Updating_of_repository_configuration_failed,
                                            org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorAttributePart_Update_Failed, job.getStatus());
                                } else {
                                    getTaskEditorPage().refresh();
                                }
                            }
                        });
                    }
                });
                job.setUser(true);
                // show the progress in the system task bar if this is a user job (i.e. forced)
                job.setProperty(IProgressConstants2.SHOW_IN_TASKBAR_ICON_PROPERTY, Boolean.TRUE);
                job.setPriority(Job.INTERACTIVE);
                job.schedule();
            };
        };
        repositoryConfigRefresh.setImageDescriptor(TasksUiImages.REPOSITORY_SYNCHRONIZE_SMALL);
        repositoryConfigRefresh.selectionChanged(new StructuredSelection(getTaskEditorPage().getTaskRepository()));
        repositoryConfigRefresh.setToolTipText(org.eclipse.mylyn.internal.tasks.ui.editors.Messages.TaskEditorAttributePart_Refresh_Attributes);
        return repositoryConfigRefresh;
    }

    @Override
    protected void fillToolBar(ToolBarManager toolBar) {
        if (needsRefresh()) {
            IAction repositoryConfigRefresh = doCreateRefreshAction();
            toolBar.add(repositoryConfigRefresh);
        }
    }

    /**
     * Returns the list of attributes that are show in the section.
     */
    protected abstract Collection<TaskAttribute> getAttributes();

    @Override
    protected String getInfoOverlayText() {
        StringBuilder sb = new StringBuilder();
        List<TaskAttribute> overlayAttributes = getOverlayAttributes();
        for (TaskAttribute attribute : overlayAttributes) {
            String label = getModel().getTaskData().getAttributeMapper().getValueLabel(attribute);
            if (label != null) {
                if (sb.length() > 0) {
                    sb.append(" / "); //$NON-NLS-1$
                }
            }
            sb.append(label);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    /**
     * Returns the attributes that are shown in the overlay text.
     *
     * @see #getInfoOverlayText()
     */
    protected abstract List<TaskAttribute> getOverlayAttributes();

    protected boolean needsRefresh() {
        return needsRefresh;
    }

    protected void setNeedsRefresh(boolean needsRefresh) {
        this.needsRefresh = needsRefresh;
    }

    /**
     * Integrator requested the ability to control whether the attributes section is expanded on creation.
     */
    @Override
    protected boolean shouldExpandOnCreate() {
        return getTaskData().isNew() || hasIncoming;
    }

}
