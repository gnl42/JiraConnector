/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import java.util.Set;

import org.eclipse.mylyn.internal.jira.core.IJiraConstants;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraFieldType;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.CheckboxMultiSelectAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPart;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorPartDescriptor;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.ColumnSpan;
import org.eclipse.mylyn.tasks.ui.editors.LayoutHint.RowSpan;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
		setNeedsPrivateSection(true);
		setNeedsSubmitButton(true);
	}

	@Override
	protected Set<TaskEditorPartDescriptor> createPartDescriptors() {
		Set<TaskEditorPartDescriptor> parts = super.createPartDescriptors();
		if (getModel().getTaskData().getRoot().getAttribute(IJiraConstants.ATTRIBUTE_WORKLOG_NOT_SUPPORTED) == null) {
			parts.add(new TaskEditorPartDescriptor("org.eclipse.mylyn.jira.worklog") { //$NON-NLS-1$
				@Override
				public AbstractTaskEditorPart createPart() {
					return new WorkLogPart();
				}
			}.setPath(PATH_ATTRIBUTES));
		}
		return parts;
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository(), getEditorSite()) {
			@Override
			public AbstractAttributeEditor createEditor(String type, TaskAttribute taskAttribute) {
				if (JiraTaskDataHandler.isTimeSpanAttribute(taskAttribute)) {
					return new TimeSpanAttributeEditor(getModel(), taskAttribute);
				}
				if (JiraUtil.isCustomDateTimeAttribute(taskAttribute)) {
					String metaType = taskAttribute.getMetaData().getValue(IJiraConstants.META_TYPE);
					if (JiraFieldType.DATETIME.getKey().equals(metaType)) {
						return new DateTimeAttributeEditor(getModel(), taskAttribute, true);
					} else if (JiraFieldType.DATE.getKey().equals(metaType)) {
						return new DateTimeAttributeEditor(getModel(), taskAttribute, false);
					}
				}
				if (TaskAttribute.TYPE_MULTI_SELECT.equals(type)) {
					CheckboxMultiSelectAttributeEditor attributeEditor = new CheckboxMultiSelectAttributeEditor(
							getModel(), taskAttribute);
					attributeEditor.setLayoutHint(new LayoutHint(RowSpan.SINGLE, ColumnSpan.SINGLE));
					return attributeEditor;
				}
				return super.createEditor(type, taskAttribute);
			}
		};
		return factory;
	}

}
