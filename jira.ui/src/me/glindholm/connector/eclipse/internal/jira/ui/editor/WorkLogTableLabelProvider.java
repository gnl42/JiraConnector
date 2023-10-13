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

package me.glindholm.connector.eclipse.internal.jira.ui.editor;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import me.glindholm.connector.eclipse.internal.jira.core.model.JiraWorkLog;
import me.glindholm.connector.eclipse.internal.jira.core.service.JiraTimeFormat;

/**
 * @author Steffen Pingel
 */
public class WorkLogTableLabelProvider extends ColumnLabelProvider {

    private final JiraTimeFormat formatTimeSpent;
    private static final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ssZ").withZone(ZoneId.systemDefault());

    public WorkLogTableLabelProvider(final JiraTimeFormat format) {
        formatTimeSpent = format;

    }

    public String getColumnText(final Object element, final int columnIndex) {
        final JiraWorkLog attachment = (JiraWorkLog) element;
        switch (columnIndex) {
        case 0:
            return attachment.getAuthor().getExternalId();
        case 1:
            // XXX use EditorUtil
            if (attachment.getStartDate() != null) {
                return dateFormat.format(attachment.getStartDate().atOffset(ZoneOffset.UTC));
            } else {
                return ""; //$NON-NLS-1$
            }
        case 2:
            return formatTimeSpent.format(attachment.getTimeSpent());
        case 3:
            return attachment.getComment();
        }
        return "unrecognized column"; //$NON-NLS-1$
    }

    @Override
    public void update(final ViewerCell cell) {
        super.update(cell);
        cell.setText(getColumnText(cell.getElement(), cell.getColumnIndex()));
    }

}
