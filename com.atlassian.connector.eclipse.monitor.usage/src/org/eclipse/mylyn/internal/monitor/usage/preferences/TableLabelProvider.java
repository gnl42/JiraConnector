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

package org.eclipse.mylyn.internal.monitor.usage.preferences;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.mylyn.internal.monitor.usage.UsageCollector;
import org.eclipse.swt.graphics.Image;

public final class TableLabelProvider extends LabelProvider implements ITableLabelProvider {
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		UsageCollector row = (UsageCollector) element;
		switch (columnIndex) {
		case 0:
			return row.getUploadUrl();
		case 1:
			if (row.getEventFilters().size() == 0) {
				return "Send everything";
			}
			return row.getEventFilters().toString();
		case 2:
			return row.getDetailsUrl();
		default:
			return "???";
		}
	}
}