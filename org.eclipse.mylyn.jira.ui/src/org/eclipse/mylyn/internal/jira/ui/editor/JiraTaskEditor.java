/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.mylyn.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraFieldType;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.internal.tasks.ui.editors.AbstractAttributeEditorManager;
import org.eclipse.mylyn.internal.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.internal.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.internal.tasks.ui.editors.LayoutHint.RowSpan;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.RepositoryTaskEditorInput;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 */
@SuppressWarnings("restriction")
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	private static final int MULTI_ROW_HEIGHT = 55;

	private static final int COLUMN_WIDTH = 140;

	private static final int MULTI_COLUMN_WIDTH = 380;

	private class JiraAttributeEditorManager extends AbstractAttributeEditorManager {

		public JiraAttributeEditorManager(RepositoryTaskEditorInput input) {
			super(input);
		}

		@Override
		public void addTextViewer(SourceViewer viewer) {
			JiraTaskEditor.this.textViewers.add(viewer);

		}

		@Override
		public boolean attributeChanged(RepositoryTaskAttribute attribute) {
			return JiraTaskEditor.this.attributeChanged(attribute);
		}

		@Override
		public void configureContextMenuManager(MenuManager menuManager) {
			// TODO EDITOR
		}

		@Override
		public Color getColorIncoming() {
			return JiraTaskEditor.this.getColorIncoming();
		}

		@Override
		public TaskRepository getTaskRepository() {
			return JiraTaskEditor.this.repository;
		}

	}

	private AttributeEditorFactory factory;

	private FormToolkit toolkit;

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	protected void addActionButtons(Composite buttonComposite) {
		super.addActionButtons(buttonComposite);
	}

	@Override
	protected void addCCList(Composite attributesComposite) {
		// disabled
	}

	@Override
	protected void addRadioButtons(Composite buttonComposite) {
		super.addRadioButtons(buttonComposite);
	}

	@Override
	protected void addSelfToCC(Composite composite) {
		// disabled
	}

	private AbstractAttributeEditor createAttributeEditor(RepositoryTaskAttribute attribute, JiraFieldType type) {
		switch (type) {
		case DATEPICKER:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_DATE, attribute);
		case ISSUELINK:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_TASK_DEPENDENCY, attribute);
		case ISSUELINKS:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_TASK_DEPENDENCY, attribute);
		case MULTISELECT:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_MULTI_SELECT, attribute);
		case SELECT:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_SINGLE_SELECT, attribute);
		case TEXTAREA:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_LONG_TEXT, attribute);
		default:
			return factory.createEditor(RepositoryTaskAttribute.TYPE_SHORT_TEXT, attribute);
		}
	}

	@Override
	protected void createAttributeLayout(Composite attributesComposite) {
		List<AbstractAttributeEditor> attributeEditors = createAttributeEditors();

		int columnCount = ((GridLayout) attributesComposite.getLayout()).numColumns;
		((GridLayout) attributesComposite.getLayout()).verticalSpacing = 6;
		
		int currentColumn = 1;
		int currentPriority = 0;
		for (AbstractAttributeEditor attributeEditor : attributeEditors) {
			int priority = (attributeEditor.getLayoutHint() != null) ? attributeEditor.getLayoutHint().getPriority() : LayoutHint.DEFAULT_PRIORITY;
			if (priority != currentPriority) {
				currentPriority = priority;
				if (currentColumn > 1) {
					while (currentColumn <= columnCount) {
						getManagedForm().getToolkit().createLabel(attributesComposite, "");
						currentColumn++;
					}
					currentColumn = 1;
				}
			}
			
			if (attributeEditor.hasLabel()) {
				attributeEditor.createLabelControl(attributesComposite, toolkit);
				Label label = attributeEditor.getLabelControl();
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(label);
				currentColumn++;
			}
			
			attributeEditor.createControl(attributesComposite, toolkit);
			LayoutHint layoutHint = attributeEditor.getLayoutHint();
			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
			if (layoutHint != null && !(layoutHint.rowSpan == RowSpan.SINGLE && layoutHint.columnSpan == ColumnSpan.SINGLE)) {
				if (layoutHint.rowSpan == RowSpan.MULTIPLE) {
					gd.heightHint = MULTI_ROW_HEIGHT;					
				}
				if (layoutHint.columnSpan == ColumnSpan.SINGLE) {
					gd.widthHint = COLUMN_WIDTH;
					gd.horizontalSpan = 1;
				} else {
					gd.widthHint = MULTI_COLUMN_WIDTH;
					gd.horizontalSpan = columnCount - currentColumn + 1;
				}
			} else {
				gd.widthHint = COLUMN_WIDTH;
				gd.horizontalSpan = 1;
			}
			attributeEditor.getControl().setLayoutData(gd);
			currentColumn += gd.horizontalSpan;

			currentColumn %= columnCount;
		}

		getManagedForm().getToolkit().paintBordersFor(attributesComposite);
	}

	private List<AbstractAttributeEditor> createAttributeEditors() {
		List<AbstractAttributeEditor> attributeEditors = new ArrayList<AbstractAttributeEditor>();
		
		for (final RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()
					|| (attribute.isReadOnly() && (attribute.getValue() == null || attribute.getValue().length() == 0))) {
				continue;
			}

			JiraFieldType type = JiraFieldType.valueByKey(attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY));
			Collection<String> options = attribute.getOptions();
			if (type.equals(JiraFieldType.SELECT) && (options == null || options.isEmpty() || attribute.isReadOnly())) {
				type = JiraFieldType.TEXTFIELD;
			} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
				type = JiraFieldType.TEXTFIELD;
			}

			AbstractAttributeEditor attributeEditor = createAttributeEditor(attribute, type);
			if (attributeEditor != null) {
				attributeEditors.add(attributeEditor);
			}
		}
		
		Collections.sort(attributeEditors, new Comparator<AbstractAttributeEditor>() {
			public int compare(AbstractAttributeEditor o1, AbstractAttributeEditor o2) {
				int p1 = (o1.getLayoutHint() != null) ? o1.getLayoutHint().getPriority() : LayoutHint.DEFAULT_PRIORITY;
				int p2 = (o2.getLayoutHint() != null) ? o2.getLayoutHint().getPriority() : LayoutHint.DEFAULT_PRIORITY;
				return p1 - p2;
			}			
		});
		
		return attributeEditors;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		JiraAttributeEditorManager manager = new JiraAttributeEditorManager(
				(RepositoryTaskEditorInput) getEditorInput());
		factory = new AttributeEditorFactory(manager);
		toolkit = getManagedForm().getToolkit();

		super.createFormContent(managedForm);
	}

	@Override
	protected String getHistoryUrl() {
		if (taskData != null) {
			String taskId = taskData.getTaskKey();
			String repositoryUrl = taskData.getRepositoryUrl();
			if (getConnector() != null && repositoryUrl != null && taskId != null) {
				String url = getConnector().getTaskUrl(repositoryUrl, taskId);
				//AbstractTask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
				if (url != null) {
					return url + "?page=history";
				}
			}
		}

		return super.getHistoryUrl();
	}

	@Override
	protected boolean hasContentAssist(RepositoryOperation repositoryOperation) {
		if ("assignee".equals(repositoryOperation.getInputName())) {
			return true;
		}
		return super.hasContentAssist(repositoryOperation);
	}

	@Override
	protected boolean hasContentAssist(RepositoryTaskAttribute attribute) {
		String key = attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY);
		// TODO need more robust detection
		if (JiraFieldType.USERPICKER.getKey().equals(key)) {
			return true;
		}

		return super.hasContentAssist(attribute);
	}

	@Override
	protected void validateInput() {
	}

}
