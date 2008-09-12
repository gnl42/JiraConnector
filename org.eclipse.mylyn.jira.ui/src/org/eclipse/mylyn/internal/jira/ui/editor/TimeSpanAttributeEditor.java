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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.mylyn.internal.jira.core.JiraTimeFormat;
import org.eclipse.mylyn.internal.jira.core.util.JiraUtil;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;
import org.eclipse.mylyn.tasks.ui.editors.AbstractAttributeEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Steffen Pingel
 */
public class TimeSpanAttributeEditor extends AbstractAttributeEditor {

	private Text text;

	private final JiraTimeFormat format;

	public TimeSpanAttributeEditor(TaskDataModel model, TaskAttribute taskAttribute) {
		super(model, taskAttribute);
		this.format = JiraUtil.getTimeFormat(model.getTaskRepository());
	}

	protected Text getText() {
		return text;
	}

	@Override
	public void createControl(Composite parent, FormToolkit toolkit) {
		if (isReadOnly()) {
			text = new Text(parent, SWT.FLAT | SWT.READ_ONLY);
			text.setFont(JFaceResources.getDefaultFont());
			text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
			text.setText(getValue());
		} else {
			text = toolkit.createText(parent, getValue(), SWT.FLAT);
			text.setFont(JFaceResources.getDefaultFont());
			text.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setValue(text.getText());
					//EditorUtil.ensureVisible(text);
				}
			});
		}
		toolkit.adapt(text, false, false);
		setControl(text);
	}

	public String getValue() {
		return format.format(getAttributeMapper().getLongValue(getTaskAttribute()));
	}

	public void setValue(String text) {
		getAttributeMapper().setLongValue(getTaskAttribute(), format.parse(text));
		attributeChanged();
	}

}
