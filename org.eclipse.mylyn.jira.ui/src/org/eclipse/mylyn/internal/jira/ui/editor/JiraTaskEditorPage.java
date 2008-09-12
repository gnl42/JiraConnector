/*******************************************************************************
* Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.jira.core.IJiraConstants;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.JiraFieldType;
import org.eclipse.mylyn.internal.jira.core.JiraTaskDataHandler;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.mylyn.tasks.ui.editors.AbstractTaskEditorPage;
import org.eclipse.mylyn.tasks.ui.editors.AttributeEditorFactory;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;

/**
 * @author Steffen Pingel
 */
public class JiraTaskEditorPage extends AbstractTaskEditorPage {

	public JiraTaskEditorPage(TaskEditor editor) {
		super(editor, JiraCorePlugin.CONNECTOR_KIND);
	}

	@Override
	protected AttributeEditorFactory createAttributeEditorFactory() {
		AttributeEditorFactory factory = new AttributeEditorFactory(getModel(), getTaskRepository()) {
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
				return super.createEditor(type, taskAttribute);
			}
		};
		return factory;
	}

}
