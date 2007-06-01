/*******************************************************************************
 * Copyright (c) 2004 - 2006 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui.editor;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylar.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylar.internal.jira.ui.JiraFieldType;
import org.eclipse.mylar.tasks.core.ITask;
import org.eclipse.mylar.tasks.core.RepositoryOperation;
import org.eclipse.mylar.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylar.tasks.ui.TasksUiPlugin;
import org.eclipse.mylar.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	public JiraTaskEditor(FormEditor editor) {
		super(editor);
	}

	@Override
	protected void addSelfToCC(Composite composite) {
		// disabled
	}

	@Override
	protected void addCCList(Composite attributesComposite) {
		// disabled
	}

	@Override
	protected void createAttributeLayout(Composite attributesComposite) {
		// removing common attributes section
	}

	@Override
	protected void addRadioButtons(Composite buttonComposite) {
		super.addRadioButtons(buttonComposite);
	}

	@Override
	protected void addActionButtons(Composite buttonComposite) {
			super.addActionButtons(buttonComposite);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
	}

	@Override
	protected void createCustomAttributeLayout(Composite attributesComposite) {
		int numColumns = ((GridLayout) attributesComposite.getLayout()).numColumns;
		int currentCol = 1;

		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()) {
				continue;
			}

			JiraFieldType type = JiraFieldType.valueByKey(attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY));
			Collection<String> options = attribute.getOptions();
			if (type.equals(JiraFieldType.SELECT) && (options == null || options.isEmpty() || attribute.isReadOnly())) {
				type = JiraFieldType.TEXTFIELD;
			} else if (type.equals(JiraFieldType.MULTISELECT) && (options == null || options.isEmpty())) {
				type = JiraFieldType.TEXTFIELD;
			}

			switch (type) {
			case TEXTAREA:
				// all text areas go to the bottom
				break;
			case SELECT: {
				Label label = createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

				CCombo combo = new CCombo(attributesComposite, SWT.FLAT | SWT.READ_ONLY);
				toolkit.adapt(combo, true, true);
				combo.setFont(TEXT_FONT);
				combo.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

				GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
				data.horizontalSpan = 1;
				data.widthHint = 140;
				data.horizontalIndent = HORZ_INDENT;
				combo.setLayoutData(data);

				if (attribute.getOptions() != null) {
					for (String val : attribute.getOptions()) {
						combo.add(val);
					}
				}

				String value = checkText(attribute.getValue());
				if (combo.indexOf(value) != -1) {
					combo.select(combo.indexOf(value));
				}
				combo.addSelectionListener(new ComboSelectionListener(combo));
				comboListenerMap.put(combo, attribute);

				if (hasChanged(attribute)) {
					combo.setBackground(backgroundIncoming);
				}

				currentCol += 2;
				break;
			}
			case MULTISELECT: {
				Label label = createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

				final List list = new List(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.V_SCROLL);
				toolkit.adapt(list, true, true);
				list.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
				list.setFont(TEXT_FONT);

				GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
				data.horizontalSpan = 1;
				data.widthHint = 125;
				data.heightHint = 45;
				data.horizontalIndent = HORZ_INDENT;
				list.setLayoutData(data);

				if (!attribute.getOptions().isEmpty()) {
					list.setItems(attribute.getOptions().toArray(new String[1]));
					for (String value : attribute.getValues()) {
						list.select(list.indexOf(value));
					}
					final RepositoryTaskAttribute attr = attribute;
					list.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							attr.clearValues();
							attr.setValues(Arrays.asList(list.getSelection()));
							attributeChanged(attr);
						}
					});
					list.showSelection();
				}

				if (hasChanged(attribute)) {
					list.setBackground(backgroundIncoming);
				}

				currentCol += 2;
				break;
			}

				// TEXTFIELD and everything else
			default: {
				Label label = createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

				int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;
				Text text = createTextField(attributesComposite, attribute, SWT.FLAT | style);

				GridData data = new GridData(SWT.LEFT, SWT.TOP, false, false);
				data.horizontalSpan = 1;
				data.widthHint = 135;
				data.horizontalIndent = HORZ_INDENT;
				text.setLayoutData(data);

				if (hasContentAssist(attribute)) {
					ContentAssistCommandAdapter adapter = applyContentAssist(text,
							createContentProposalProvider(attribute));

					ILabelProvider propsalLabelProvider = createProposalLabelProvider(attribute);
					if (propsalLabelProvider != null) {
						adapter.setLabelProvider(propsalLabelProvider);
					}
					adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
				}

				if (hasChanged(attribute)) {
					text.setBackground(backgroundIncoming);
				}

				currentCol += 2;
			}
			}

			if (currentCol > numColumns) {
				currentCol -= numColumns;
			}
		}

		if (currentCol > 1) {
			while (currentCol <= numColumns) {
				toolkit.createLabel(attributesComposite, "");
				currentCol++;
			}
		}

		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()
					|| !JiraFieldType.TEXTAREA.getKey().equals(
							attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY))) {
				continue;
			}

			Label label = createLabel(attributesComposite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);

			int style = attribute.isReadOnly() ? SWT.READ_ONLY : 0;

			// TextViewer viewer = addTextEditor(repository,
			// attributesComposite, attribute.getValue(), true, SWT.FLAT |
			// SWT.BORDER | SWT.MULTI | SWT.WRAP | style);
			TextViewer viewer = new TextViewer(attributesComposite, SWT.FLAT | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL
					| style);
			viewer.setDocument(new Document(attribute.getValue()));

			final StyledText text = viewer.getTextWidget();
			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

			// GridDataFactory.fillDefaults().span(3, 1).hint(300,
			// 40).applyTo(text);
			GridData data = new GridData(SWT.LEFT, SWT.TOP, true, false);
			data.horizontalSpan = 3;
			data.widthHint = 380;
			data.heightHint = 55;
			data.horizontalIndent = HORZ_INDENT;
			text.setLayoutData(data);

			toolkit.adapt(text, true, true);

			if (attribute.isReadOnly()) {
				viewer.setEditable(false);
			} else {
				viewer.setEditable(true);
				text.setData(attribute);
				text.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						String newValue = text.getText();
						RepositoryTaskAttribute attribute = (RepositoryTaskAttribute) text.getData();
						attribute.setValue(newValue);
						attributeChanged(attribute);
					}
				});
			}

			if (hasChanged(attribute)) {
				text.setBackground(backgroundIncoming);
			}
		}

		toolkit.paintBordersFor(attributesComposite);
	}

	@Override
	protected void validateInput() {
	}

	protected String getActivityUrl() {
		if (taskData != null) {
			String taskId = taskData.getId();
			String repositoryUrl = taskData.getRepositoryUrl();
			if (repositoryUrl != null && taskId != null) {
				ITask task = TasksUiPlugin.getTaskListManager().getTaskList().getTask(repositoryUrl, taskId);
				if (task != null) {
					return task.getTaskUrl() + "?page=history";
				}
			}
		}

		return super.getActivityUrl();
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
	protected boolean hasContentAssist(RepositoryOperation repositoryOperation) {
		if ("assignee".equals(repositoryOperation.getInputName())) {
			return true;
		}
		return super.hasContentAssist(repositoryOperation);
	}

}
