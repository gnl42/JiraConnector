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

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.mylyn.internal.jira.core.model.JiraWorkLog;
import org.eclipse.mylyn.internal.jira.core.service.JiraTimeFormat;

/**
 * @author Steffen Pingel
 */
public class WorkLogTableLabelProvider extends ColumnLabelProvider {

	private final JiraTimeFormat format;

	public WorkLogTableLabelProvider(JiraTimeFormat format) {
		this.format = format;
	}

	public String getColumnText(Object element, int columnIndex) {
		JiraWorkLog attachment = (JiraWorkLog) element;
		switch (columnIndex) {
		case 0:
			return attachment.getAuthor();
		case 1:
			// XXX use EditorUtil
			if (attachment.getStartDate() != null) {
				return attachment.getStartDate().toString();
			} else {
				return ""; //$NON-NLS-1$
			}
		case 2:
			return format.format(attachment.getTimeSpent());
		case 3:
			return attachment.getComment();
		}
		return "unrecognized column"; //$NON-NLS-1$
	}

	@Override
	public void update(ViewerCell cell) {
		super.update(cell);
		cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
	}

}
