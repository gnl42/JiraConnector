/*******************************************************************************
 * Copyright (c) 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.ui.editor;

import org.eclipse.mylyn.internal.tasks.ui.editors.TextAttributeEditor;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskDataModel;

/**
 * @author Jacek Jaroczynski
 */
@SuppressWarnings("restriction")
public class NumberAttributeEditor extends TextAttributeEditor {

	public NumberAttributeEditor(TaskDataModel manager, TaskAttribute taskAttribute) {
		super(manager, taskAttribute);
	}

	@Override
	public String getValue() {
		String sNumber = super.getValue();

		try {
			Double dNumber = Double.valueOf(sNumber);

			// check if there is a fraction part
			if (dNumber != Math.floor(dNumber)) {
				return sNumber;
			}

			// cut fraction part if equals to 0
			return new Long(dNumber.longValue()).toString();

		} catch (NumberFormatException e) {
			return sNumber;
		}
	}
}
