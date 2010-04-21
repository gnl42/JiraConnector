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

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import java.util.Iterator;
import java.util.Set;

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

import com.atlassian.connector.eclipse.internal.jira.core.IJiraConstants;
import com.atlassian.connector.eclipse.internal.jira.core.JiraCorePlugin;
import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
import com.atlassian.connector.eclipse.internal.jira.core.JiraTaskDataHandler;
import com.atlassian.connector.eclipse.internal.jira.core.util.JiraUtil;

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
			}.setPath(ID_PART_ATTRIBUTES + "/" + PATH_PLANNING)); //$NON-NLS-1$
		}

		// replace summary part
		Iterator<TaskEditorPartDescriptor> iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_SUMMARY)) {
				parts.remove(part);

				// add JIRA specific summary part (with votes number)
				parts.add(new TaskEditorPartDescriptor(ID_PART_SUMMARY) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraTaskEditorSummaryPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove comments part
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_COMMENTS)) {
				parts.remove(part);

				// add JIRA specific comments part (with visibility restriction info for each comment)
				parts.add(new TaskEditorPartDescriptor(ID_PART_COMMENTS) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraCommentPartCopy();
//						return new JiraCommentPart();
					}
				}.setPath(part.getPath()));

				break;
			}
		}

		// remove standard new comment part 
		iter = parts.iterator();
		while (iter.hasNext()) {
			TaskEditorPartDescriptor part = iter.next();
			if (part.getId().equals(ID_PART_NEW_COMMENT)) {
				parts.remove(part);

				// add JIRA specific comment part (with visibility restriction combo)
				parts.add(new TaskEditorPartDescriptor(ID_PART_NEW_COMMENT) {
					@Override
					public AbstractTaskEditorPart createPart() {
						return new JiraNewCommentPart(getModel());
					}
				}.setPath(part.getPath()));

				break;
			}
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
				if (IJiraConstants.TYPE_NUMBER.equals(type)) {
					return new NumberAttributeEditor(getModel(), taskAttribute);
				}
				return super.createEditor(type, taskAttribute);
			}
		};
		return factory;
	}

}
