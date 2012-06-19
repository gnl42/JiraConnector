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

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

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
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.commons.workbench.forms.CommonFormUtil;
import org.eclipse.mylyn.internal.tasks.core.TaskAttachment;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttachmentTableLabelProvider;
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

import com.atlassian.connector.eclipse.internal.jira.ui.JiraImages;

/**
 * I hope to contribute it back one day to Mylyn framework
 */
public class JiraTaskEditorAttachmentsPart extends AbstractTaskEditorPart {

	private static final String ID_POPUP_MENU = "org.eclipse.mylyn.tasks.ui.editor.menu.attachments"; //$NON-NLS-1$

	private final String[] attachmentsColumns = { Messages.TaskEditorAttachmentPart_Name,
			Messages.TaskEditorAttachmentPart_Description, /*"Type", */Messages.TaskEditorAttachmentPart_Size,
			Messages.TaskEditorAttachmentPart_Creator, Messages.TaskEditorAttachmentPart_Created };

	private final int[] attachmentsColumnWidths = { 130, 150, /*100,*/70, 100, 100 };

	private final int[] attachmentsColumnWidthsNoDescription = { 150, 0, 100, 180, 100 };

	private List<TaskAttribute> attachments;

	private boolean hasIncoming;

	private MenuManager menuManager;

	private Composite attachmentsComposite;

	private boolean useDescriptionColumn;

	private Section section;

	public JiraTaskEditorAttachmentsPart() {
		setPartName(Messages.TaskEditorAttachmentPart_Attachments);
	}

	public void setUseDescriptionColumn(boolean useDescriptionColumn) {
		this.useDescriptionColumn = useDescriptionColumn;
	}

	private void createAttachmentTable(FormToolkit toolkit, final Composite attachmentsComposite) {
		Table attachmentsTable = toolkit.createTable(attachmentsComposite, SWT.MULTI | SWT.FULL_SELECTION);
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
			TableColumn column = new TableColumn(attachmentsTable, SWT.LEFT, columnIndex);
			column.setText(attachmentsColumns[i]);
			if (useDescriptionColumn) {
				column.setWidth(attachmentsColumnWidths[i]);
			} else {
				column.setWidth(attachmentsColumnWidthsNoDescription[i]);
			}
			columnIndex++;
		}
		int sizeColumn = useDescriptionColumn ? 2 : 1;
		attachmentsTable.getColumn(sizeColumn).setAlignment(SWT.RIGHT);

		TableViewer attachmentsViewer = new TableViewer(attachmentsTable);
		attachmentsViewer.setUseHashlookup(true);
		attachmentsViewer.setColumnProperties(attachmentsColumns);
		ColumnViewerToolTipSupport.enableFor(attachmentsViewer, ToolTip.NO_RECREATE);

		attachmentsViewer.setSorter(new ViewerSorter() {
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				ITaskAttachment attachment1 = (ITaskAttachment) e1;
				ITaskAttachment attachment2 = (ITaskAttachment) e2;
				Date created1 = attachment1.getCreationDate();
				Date created2 = attachment2.getCreationDate();
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
		});

		List<ITaskAttachment> attachmentList = new ArrayList<ITaskAttachment>(attachments.size());
		for (TaskAttribute attribute : attachments) {
			TaskAttachment taskAttachment = new TaskAttachment(getModel().getTaskRepository(), getModel().getTask(),
					attribute);
			getTaskData().getAttributeMapper().updateTaskAttachment(taskAttachment, attribute);
			attachmentList.add(taskAttachment);
		}
		attachmentsViewer.setContentProvider(new ArrayContentProvider());
		attachmentsViewer.setLabelProvider(new AttachmentTableLabelProvider(getModel(),
				getTaskEditorPage().getAttributeEditorToolkit()) {
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (!useDescriptionColumn && columnIndex >= 1) {
					columnIndex++;
				}
				return super.getColumnText(element, columnIndex);
			}
		});
		attachmentsViewer.addOpenListener(new IOpenListener() {
			public void open(OpenEvent event) {
				if (!event.getSelection().isEmpty()) {
					StructuredSelection selection = (StructuredSelection) event.getSelection();
					ITaskAttachment attachment = (ITaskAttachment) selection.getFirstElement();
					TasksUiUtil.openUrl(attachment.getUrl());
				}
			}
		});
		attachmentsViewer.addSelectionChangedListener(getTaskEditorPage());
		attachmentsViewer.setInput(attachmentList.toArray());

		menuManager = new MenuManager();
		menuManager.setRemoveAllWhenShown(true);
		menuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TasksUiMenus.fillTaskAttachmentMenu(manager);
			}
		});
		getTaskEditorPage().getEditorSite().registerContextMenu(ID_POPUP_MENU, menuManager, attachmentsViewer, true);
		Menu menu = menuManager.createContextMenu(attachmentsTable);
		attachmentsTable.setMenu(menu);
	}

	private void createButtons(Composite attachmentsComposite, FormToolkit toolkit) {
		final Composite attachmentControlsComposite = toolkit.createComposite(attachmentsComposite);
		attachmentControlsComposite.setLayout(new GridLayout(3, false));
		attachmentControlsComposite.setLayoutData(new GridData(GridData.BEGINNING));

		Button attachFileButton = toolkit.createButton(attachmentControlsComposite,
				Messages.TaskEditorAttachmentPart_Attach_, SWT.PUSH);
		attachFileButton.setImage(CommonImages.getImage(CommonImages.FILE_PLAIN));
		attachFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.DEFAULT, null);
			}
		});
		getTaskEditorPage().registerDefaultDropListener(attachFileButton);

		Button attachScreenshotButton = toolkit.createButton(attachmentControlsComposite,
				Messages.TaskEditorAttachmentPart_Attach__Screenshot, SWT.PUSH);
		attachScreenshotButton.setImage(CommonImages.getImage(CommonImages.IMAGE_CAPTURE));
		attachScreenshotButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.SCREENSHOT, null);
			}
		});
		getTaskEditorPage().registerDefaultDropListener(attachScreenshotButton);

		final CLabel dndHintLabel = new CLabel(attachmentControlsComposite, SWT.LEFT);
		dndHintLabel.setImage(JiraImages.getImage(JiraImages.LIGHTBULB));
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).applyTo(dndHintLabel);
		dndHintLabel.setText(com.atlassian.connector.eclipse.internal.jira.ui.editor.Messages.JiraTaskEditorSummaryPart_Attachements_Drag_and_Drop_Hint);
		getTaskEditorPage().registerDefaultDropListener(dndHintLabel);

	}

	@Override
	public void createControl(Composite parent, final FormToolkit toolkit) {
		initialize();

		section = createSection(parent, toolkit, hasIncoming);
		section.setText(getPartName() + " (" + attachments.size() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		if (hasIncoming) {
			expandSection(toolkit, section);
		} else {
			section.addExpansionListener(new ExpansionAdapter() {
				@Override
				public void expansionStateChanged(ExpansionEvent event) {
					if (attachmentsComposite == null) {
						expandSection(toolkit, section);
						getTaskEditorPage().reflow();
					}
				}
			});
		}
		setSection(toolkit, section);
	}

	private void expandSection(FormToolkit toolkit, Section section) {
		attachmentsComposite = toolkit.createComposite(section);
		attachmentsComposite.setLayout(EditorUtil.createSectionClientLayout());
		attachmentsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		getTaskEditorPage().registerDefaultDropListener(section);

		if (attachments.size() > 0) {
			createAttachmentTable(toolkit, attachmentsComposite);
		} else {
			Label label = toolkit.createLabel(attachmentsComposite, Messages.TaskEditorAttachmentPart_No_attachments);
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
		for (TaskAttribute attachmentAttribute : attachments) {
			if (getModel().hasIncomingChanges(attachmentAttribute)) {
				hasIncoming = true;
				break;
			}
		}
	}

	@Override
	protected void fillToolBar(ToolBarManager toolBarManager) {
		Action attachFileAction = new Action() {
			@Override
			public void run() {
				EditorUtil.openNewAttachmentWizard(getTaskEditorPage(), Mode.DEFAULT, null);
			}
		};
		attachFileAction.setToolTipText(Messages.TaskEditorAttachmentPart_Attach_);
		attachFileAction.setImageDescriptor(CommonImages.FILE_PLAIN_SMALL);
		toolBarManager.add(attachFileAction);

		Action attachScreenshotAction = new Action() {
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
	public boolean setFormInput(Object input) {
		if (input instanceof String) {
			String text = (String) input;
			if (text.startsWith(TaskAttribute.PREFIX_ATTACHMENT)) {
				if (attachments != null) {
					for (TaskAttribute attachmentAttribute : attachments) {
						if (text.equals(attachmentAttribute.getId())) {
							selectReveal(attachmentAttribute);
						}
					}
				}
			}
		}
		return super.setFormInput(input);
	}

	public TaskAttribute selectReveal(TaskAttribute attachmentAttribute) {
		if (attachmentAttribute == null) {
			return null;
		}
		expand();
		for (TaskAttribute attachment : attachments) {
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
