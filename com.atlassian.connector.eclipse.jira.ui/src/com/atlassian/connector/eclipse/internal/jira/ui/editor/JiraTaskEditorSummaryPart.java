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

import com.atlassian.connector.eclipse.internal.jira.core.JiraAttribute;
import com.atlassian.connector.eclipse.internal.jira.ui.JiraImages;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.tasks.ui.editors.EditorUtil;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorSummaryPart;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Jacek Jaroczynski
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public class JiraTaskEditorSummaryPart extends TaskEditorSummaryPart {

	@Override
	protected Composite createHeaderLayout(Composite parent, FormToolkit toolkit) {
		Composite composite = super.createHeaderLayout(parent, toolkit);

		TaskAttribute attribute = getTaskData().getRoot().getMappedAttribute(JiraAttribute.VOTES.id());
		if (attribute != null) {
			addAttribute(composite, toolkit, attribute, true);

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

		Layout layout = composite.getLayout();
		if (layout instanceof GridLayout) {
			GridLayout gLayout = (GridLayout) layout;
			gLayout.numColumns += 1;

			final Label dndLabel = new Label(composite, SWT.NONE);
			dndLabel.setImage(JiraImages.getImage(JiraImages.DROP_HERE));
			GridDataFactory.fillDefaults().span(1, 2).align(SWT.RIGHT, SWT.CENTER).grab(true, false).applyTo(dndLabel);
			dndLabel.setToolTipText(Messages.JiraTaskEditorSummaryPart_Attachements_Drop_Zone_Hover);

			Composite secondLineComposite = new Composite(composite, SWT.NONE);
			final RowLayout rowLayout = new RowLayout();
			rowLayout.center = true;
			rowLayout.marginLeft = 0;
			secondLineComposite.setLayout(rowLayout);
			rowLayout.spacing = 8;
			GridDataFactory.fillDefaults().span(gLayout.numColumns - 1, 1).applyTo(secondLineComposite);
			toolkit.adapt(secondLineComposite);

			final TaskAttribute reporterAttribute = getTaskData().getRoot().getMappedAttribute(
					JiraAttribute.USER_REPORTER.id());
			if (reporterAttribute != null) {
				addAttribute(secondLineComposite, toolkit, reporterAttribute, false);
			}

			final TaskAttribute assigneeAttribute = getTaskData().getRoot().getMappedAttribute(
					JiraAttribute.USER_ASSIGNED.id());
			if (assigneeAttribute != null) {
				addAttribute(secondLineComposite, toolkit, assigneeAttribute, false);
			}

		}

		return composite;
	}

	private void addAttribute(Composite composite, FormToolkit toolkit, TaskAttribute attribute,
			boolean shouldInitializeGridData) {
		AbstractAttributeEditor editor = createAttributeEditor(attribute);
		if (editor != null) {
			editor.setReadOnly(true);
			editor.setDecorationEnabled(false);

			editor.createLabelControl(composite, toolkit);
			if (shouldInitializeGridData) {
				GridDataFactory.defaultsFor(editor.getLabelControl())
						.indent(EditorUtil.HEADER_COLUMN_MARGIN, 0)
						.applyTo(editor.getLabelControl());
			}

			editor.createControl(composite, toolkit);
			getTaskEditorPage().getAttributeEditorToolkit().adapt(editor);
		}
	}

}
