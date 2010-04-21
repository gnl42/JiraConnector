/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.FormToolkit;

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class JiraTaskEditorSummaryPart extends TaskEditorSummaryPart {

	@Override
	protected Composite createHeaderLayout(Composite parent, FormToolkit toolkit) {
		Composite composite = super.createHeaderLayout(parent, toolkit);

		TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(JiraAttribute.VOTES.id());
		if (attribute != null) {
			addAttribute(composite, toolkit, attribute);

			Layout layout = composite.getLayout();
			if (layout instanceof GridLayout) {
				GridLayout gl = (GridLayout) layout;
				gl.numColumns = composite.getChildren().length;

				if (gl.numColumns == 0) {
					gl.numColumns = 1;
					toolkit.createLabel(composite, " "); //$NON-NLS-1$
				}
			}
		}

		return composite;
	}

	private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute) {
		AbstractAttributeEditor editor = createAttributeEditor(attribute);
		if (editor != null) {
			editor.setReadOnly(true);
			editor.setDecorationEnabled(false);

			editor.createLabelControl(composite, toolkit);
			GridDataFactory.defaultsFor(editor.getLabelControl()).indent(EditorUtil.HEADER_COLUMN_MARGIN, 0).applyTo(
					editor.getLabelControl());

			editor.createControl(composite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
		}
	}
}
