/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop - initial API and implementation
 *     Atlassian - repackaging and extending for JIRA specific needs
 ******************************************************************************/

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.Messages;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiMenus;
import org.eclipse.mylyn.internal.tasks.ui.wizards.TaskAttachmentWizard.Mode;
import org.eclipse.mylyn.tasks.core.ITaskAttachment;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import me.glindholm.connector.eclipse.internal.jira.ui.JiraImages;

/**
 * I hope to contribute it back one day to Mylyn framework
 */
public class JiraTaskEditorAttachmentsPart extends AbstractTaskEditorPart {

    private static final String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.attachments"; //$NON-NLS-1$

    private final String[] attachmentsColumns = { Messages.TaskEditorAttachmentPart_Name,
            Messages.TaskEditorAttachmentPart_Description, /*"Type", */Messages.TaskEditorAttachmentPart_Size,
            Messages.TaskEditorAttachmentPart_Creator, Messages.TaskEditorAttachmentPart_Created };

    private final int[] attachmentsColumnWidths = { 130, 150, /* 100, */70, 100, 250 }; // Not used

    private final int[] attachmentsColumnWidthsNoDescription = { 270, 0, 100, 180, 250 };

    private List<TaskAttribute> attachments;

    private boolean hasIncoming;

    private MenuManager menuManager;

    private Composite attachmentsComposite;

    private boolean useDescriptionColumn;

    private Section section;

    public JiraTaskEditorAttachmentsPart() {
        setPartName(Messages.TaskEditorAttachmentPart_Attachments);
    }

    public void setUseDescriptionColumn(final boolean useDescriptionColumn) {
        this.useDescriptionColumn = useDescriptionColumn;
    }

    private void createAttachmentTable(final FormToolkit toolkit, final Composite attachmentsComposite) {
        final Table attachmentsTable = toolkit.createTable(attachmentsComposite, SWT.MULTI | SWT.FULL_SELECTION);
        attachmentsTable.setLinesVisible(true);
        attachmentsTable.setHeaderVisible(true);
        attachmentsTable.setLayout(new GridLayout());
        GridDataFactory.fillDefaults()
        .align(SWT.FILL, SWT.FILL)
        .grab(true, false)
        .hint(500, SWT.DEFAULT)
        .applyTo(attachmentsTable);
        attachmentsTable.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TREE_BORDER);

        for (int i = 0, columnIndex = 0; i < attachmentsColumns.length; i++) {
            if (!useDescriptionColumn && attachmentsColumns[i].equals(Messages.TaskEditorAttachmentPart_Description)) {
                continue;
            }
            final TableColumn column = new TableColumn(attachmentsTable, SWT.LEFT, columnIndex);
            column.setText(attachmentsColumns[i]);
            if (useDescriptionColumn) {
                column.setWidth(attachmentsColumnWidths[i]);
            } else {
                column.setWidth(attachmentsColumnWidthsNoDescription[i]);
            }
            columnIndex++;
        }
        final int sizeColumn = useDescriptionColumn ? 2 : 1;
        attachmentsTable.getColumn(sizeColumn).setAlignment(SWT.RIGHT);

        final TableViewer attachmentsViewer = new TableViewer(attachmentsTable);
        attachmentsViewer.setUseHashlookup(true);
        attachmentsViewer.setColumnProperties(attachmentsColumns);
        ColumnViewerToolTipSupport.enableFor(attachmentsViewer, ToolTip.NO_RECREATE);

        final ViewerComparator attachmentSorter = new ViewerComparator() {
            @Override
            public int compare(final Viewer viewer, final Object e1, final Object e2) {
                final ITaskAttachment attachment1 = (ITaskAttachment) e1;
                final ITaskAttachment attachment2 = (ITaskAttachment) e2;
                final Date created1 = attachment1.getCreationDate();
                final Date created2 = attachment2.getCreationDate();
                if (created1 != null && created2 != null) {
                    return created1.compareTo(created2);
                } else if (created1 == null && created2 != null) {
                    return -1;
                } else if (created1 != null && created2 == null) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };
        attachmentsViewer.setComparator(attachmentSorter);

        final List<ITaskAttachment> attachmentList = new ArrayList<>(attachments.size());
        for (final TaskAttribute attribute : attachments) {
            final TaskAttachment taskAttachment = new TaskAttachment(getModel().getTaskRepository(), getModel().getTask(),
                    attribute);
            getTaskData().getAttributeMapper().updateTaskAttachment(taskAttachment, attribute);
            attachmentList.add(taskAttachment);
        }
        attachmentsViewer.setContentProvider(new ArrayContentProvider());

        attachmentsViewer.setLabelProvider(new CellLabelProvider() {
            @Override
            public void update(final ViewerCell cell) {
                final TaskAttachment element = (TaskAttachment) cell.getElement();
                final String text;
                final int columnIndex = cell.getColumnIndex();
                switch (columnIndex) {
                case 0:
                    text = element.getFileName();
                    break;
                case 1:
                    text = humanReadableByteCountSI(element.getLength());
                    break;
                case 2:
                    text = element.getAuthor().getPersonId();
                    break;
                case 3:
                    text = element.getCreationDate().toLocaleString();
                    break;
                default:
                    text = "Unexpected column: " + columnIndex;
                }
                cell.setText(text);
            }
        });

        attachmentsViewer.addOpenListener(new IOpenListener() {
            @Override
            public void open(final OpenEvent event) {
                if (!event.getSelection().isEmpty()) {
                    final StructuredSelection selection = (StructuredSelection) event.getSelection();
                    final ITaskAttachment attachment = (ITaskAttachment) selection.getFirstElement();
                    TasksUiUtil.openUrl(attachment.getUrl());
                }
            }
        });
        attachmentsViewer.addSelectionChangedListener(getTaskEditorPage());
        attachmentsViewer.setInput(attachmentList.toArray());

        menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        menuManager.addMenuListener(new IMenuListener() {
            @Override
            public void menuAboutToShow(final IMenuManager manager) {
                TasksUiMenus.fillTaskAttachmentMenu(manager);
            }
        });
        getTaskEditorPage().getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, attachmentsViewer, true);
        final Menu menu = menuManager.createContextMenu(attachmentsTable);
        attachmentsTable.setMenu(menu);
    }

    /**
     * @author Andreas Lundblad
     * @see <a href=
     *      "https://programming.guide/java/formatting-byte-size-to-human-readable-format.html">Formatting
     *      byte size to human readable format</a>
     * @param bytes
     * @return
     */
    private static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }
        final CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    private void createButtons(final Composite attachmentsComposite, final FormToolkit toolkit) {
        final Composite attachmentControlsComposite = toolkit.createComposite(attachmentsComposite);
        attachmentControlsComposite.setLayout(new GridLayout(3, false));
        attachmentControlsComposite.setLayoutData(new GridData(GridData.BEGINNING));

        final Button attachFileButton = toolkit.createButton(attachmentControlsComposite,
                Messages.TaskEditorAttachmentPart_Attach_, SWT.PUSH);
        attachFileButton.setImage(CommonImages.getImage(CommonImages.FILE_PLAIN));
        attachFileButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.DEFAULT, null);
            }
        });
        getTaskEditorPage().registerDefaultDropListener(attachFileButton);

        final Button attachScreenshotButton = toolkit.createButton(attachmentControlsComposite,
                Messages.TaskEditorAttachmentPart_Attach__Screenshot, SWT.PUSH);
        attachScreenshotButton.setImage(CommonImages.getImage(CommonImages.IMAGE_CAPTURE));
        attachScreenshotButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent e) {
                EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.SCREENSHOT, null);
            }
        });
        getTaskEditorPage().registerDefaultDropListener(attachScreenshotButton);

        final CLabel dndHintLabel = new CLabel(attachmentControlsComposite, SWT.LEFT);
        dndHintLabel.setImage(JiraImages.getImage(JiraImages.LIGHTBULB));
        GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(dndHintLabel);
        dndHintLabel.setText(me.glindholm.connector.eclipse.internal.jira.ui.editor.Messages.JiraTaskEditorSummaryPart_Attachements_Drag_and_Drop_Hint);
        getTaskEditorPage().registerDefaultDropListener(dndHintLabel);

    }

    @Override
    public void createControl(final Composite parent, final FormToolkit toolkit) {
        initialize();

        section = createSection(parent, toolkit, hasIncoming);
        section.setText(getPartName() + " (" + attachments.size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
        if (hasIncoming) {
            expandSection(toolkit, section);
        } else {
            section.addExpansionListener(new ExpansionAdapter() {
                @Override
                public void expansionStateChanged(final ExpansionEvent event) {
                    if (attachmentsComposite == null) {
                        expandSection(toolkit, section);
                        getTaskEditorPage().reflow();
                    }
                }
            });
        }
        setSection(toolkit, section);
    }

    private void expandSection(final FormToolkit toolkit, final Section section) {
        attachmentsComposite = toolkit.createComposite(section);
        attachmentsComposite.setLayout(EditorUtil.createSectionClientLayout());
        attachmentsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

        getTaskEditorPage().registerDefaultDropListener(section);

        if (attachments.size() > 0) {
            createAttachmentTable(toolkit, attachmentsComposite);
        } else {
            final Label label = toolkit.createLabel(attachmentsComposite, Messages.TaskEditorAttachmentPart_No_attachments);
            getTaskEditorPage().registerDefaultDropListener(label);
        }

        createButtons(attachmentsComposite, toolkit);

        toolkit.paintBordersFor(attachmentsComposite);
        section.setClient(attachmentsComposite);
    }

    @Override
    public void dispose() {
        if (menuManager != null) {
            menuManager.dispose();
        }
        super.dispose();
    }

    private void initialize() {
        attachments = getTaskData().getAttributeMapper().getAttributesByType(getTaskData(),
                TaskAttribute.TYPE_ATTACHMENT);
        for (final TaskAttribute attachmentAttribute : attachments) {
            if (getModel().hasIncomingChanges(attachmentAttribute)) {
                hasIncoming = true;
                break;
            }
        }
    }

    @Override
    protected void fillToolBar(final ToolBarManager toolBarManager) {
        final Action attachFileAction = new Action() {
            @Override
            public void run() {
                EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.DEFAULT, null);
            }
        };
        attachFileAction.setToolTipText(Messages.TaskEditorAttachmentPart_Attach_);
        attachFileAction.setImageDescriptor(CommonImages.FILE_PLAIN_SMALL);
        toolBarManager.add(attachFileAction);

        final Action attachScreenshotAction = new Action() {
            @Override
            public void run() {
                EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.SCREENSHOT, null);
            }
        };

        attachScreenshotAction.setImageDescriptor(CommonImages.IMAGE_CAPTURE);
        attachScreenshotAction.setToolTipText(Messages.TaskEditorAttachmentPart_Attach__Screenshot);
        toolBarManager.add(attachScreenshotAction);
    }

    @Override
    public boolean setFormInput(final Object input) {
        if (input instanceof String) {
            final String text = (String) input;
            if (text.startsWith(TaskAttribute.PREFIX_ATTACHMENT)) {
                if (attachments != null) {
                    for (final TaskAttribute attachmentAttribute : attachments) {
                        if (text.equals(attachmentAttribute.getId())) {
                            selectReveal(attachmentAttribute);
                        }
                    }
                }
            }
        }
        return super.setFormInput(input);
    }

    public TaskAttribute selectReveal(final TaskAttribute attachmentAttribute) {
        if (attachmentAttribute == null) {
            return null;
        }
        expand();
        for (final TaskAttribute attachment : attachments) {
            if (attachment.equals(attachmentAttribute)) {
                CommonFormUtil.ensureVisible(attachmentsComposite);
                EditorUtil.focusOn(getTaskEditorPage().getManagedForm().getForm(), section);

                return attachmentAttribute;
            }
        }
        return null;
    }

    private void expand() {
        try {
            getTaskEditorPage().setReflow(false);

            if (section != null) {
                CommonFormUtil.setExpanded(section, true);
            }
        } finally {
            getTaskEditorPage().setReflow(true);
        }
        getTaskEditorPage().reflow();
    }

}
