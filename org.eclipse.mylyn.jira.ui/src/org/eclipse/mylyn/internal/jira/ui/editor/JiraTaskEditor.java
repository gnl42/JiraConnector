/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.util.Collection;
import java.util.Date;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.mylyn.internal.jira.ui.JiraAttributeFactory;
import org.eclipse.mylyn.internal.jira.ui.JiraFieldType;
import org.eclipse.mylyn.internal.jira.ui.JiraUtils;
import org.eclipse.mylyn.tasks.core.RepositoryOperation;
import org.eclipse.mylyn.tasks.core.RepositoryTaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractRepositoryTaskEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * @author Mik Kersten
 * @author Rob Elves
 * @author Eugene Kuleshov
 */
public class JiraTaskEditor extends AbstractRepositoryTaskEditor {

	private TaskUiFactory factory;

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
		factory = new TaskUiFactory(getManagedForm().getToolkit(), this);

		super.createFormContent(managedForm);
	}

	@Override
	protected void createCustomAttributeLayout(Composite attributesComposite) {
		int numColumns = ((GridLayout) attributesComposite.getLayout()).numColumns;
		int currentCol = 1;

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

			switch (type) {
			case ISSUELINKS:
			case TEXTAREA:
				// all text areas go to the bottom
				break;
			case SELECT: {
				Label label = factory.createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
				factory.createCCombo(attributesComposite, attribute);
				currentCol += 2;
				break;
			}
			case MULTISELECT: {
				Label label = factory.createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
				factory.createList(attributesComposite, attribute);
				currentCol += 2;
				break;
			}
			case ISSUELINK: {
				Label label = factory.createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
				factory.createLink(attributesComposite, repository, attribute);
				currentCol += 2;
				break;
			}
			default: {
				// TEXTFIELD and everything else
				Label label = factory.createLabel(attributesComposite, attribute);
				GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
				factory.createText(attributesComposite, attribute);
				currentCol += 2;
			}
			}

			if (currentCol > numColumns) {
				currentCol -= numColumns;
			}
		}

		// due date
		{
			RepositoryTaskAttribute attribute = taskData.getAttribute(JiraAttributeFactory.ATTRIBUTE_DUE_DATE);
			if (attribute != null) {
				factory.createLabel(attributesComposite, attribute);
				factory.createDatePicker(attributesComposite, attribute, new TaskUiFactory.DateExternalizer() {
					public Date toDate(String value) {
						return JiraUtils.stringToDate(value);
					}

					public String toString(Date value) {
						return JiraUtils.dateToString(value);
					}
				});
				currentCol += 2;
			}
		}

		if (currentCol > 1) {
			while (currentCol <= numColumns) {
				getManagedForm().getToolkit().createLabel(attributesComposite, "");
				currentCol++;
			}
		}

		// subtasks and links
		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()
					|| !JiraFieldType.ISSUELINKS.getKey().equals(
							attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY))) {
				continue;
			}

			Label label = factory.createLabel(attributesComposite, attribute);
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.TOP).applyTo(label);
			factory.createLinkList(attributesComposite, repository, attribute);
		}

		// text areas
		for (RepositoryTaskAttribute attribute : taskData.getAttributes()) {
			if (attribute.isHidden()
					|| !JiraFieldType.TEXTAREA.getKey().equals(
							attribute.getMetaDataValue(JiraAttributeFactory.TYPE_KEY))
					// no need to show non-editable empty values
					|| (attribute.isReadOnly() && (attribute.getValue() == null || attribute.getValue().length() == 0))) {
				continue;
			}

			factory.createTextArea(attributesComposite, attribute);
		}

		getManagedForm().getToolkit().paintBordersFor(attributesComposite);
	}

	@Override
	protected void validateInput() {
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

	@Override
	public boolean attributeChanged(RepositoryTaskAttribute attribute) {
		// TODO remove
		return super.attributeChanged(attribute);
	}

	public void addTextViewer(TextViewer viewer) {
		// TODO remove
		textViewers.add(viewer);
	}

	public IContentProposalProvider createContentProposalProvider(RepositoryTaskAttribute attribute) {
		return super.createContentProposalProvider(attribute);
	}

	public ContentAssistCommandAdapter applyContentAssist(Text text, IContentProposalProvider proposalProvider) {
		return super.applyContentAssist(text, proposalProvider);
	}

	public ILabelProvider createProposalLabelProvider(RepositoryTaskAttribute attribute) {
		return super.createProposalLabelProvider(attribute);
	}

}
