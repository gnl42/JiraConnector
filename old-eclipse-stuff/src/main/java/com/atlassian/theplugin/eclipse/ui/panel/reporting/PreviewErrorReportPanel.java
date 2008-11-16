/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.panel.reporting;

import org.eclipse.swt.graphics.Point;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Preview error report panel
 *
 * @author Sergiy Logvin
 */
public class PreviewErrorReportPanel extends PreviewReportPanel {
	public PreviewErrorReportPanel(String report) {
		super(Activator.getDefault().getResource("PreviewErrorReportPanel.Description"), report);
	}
	
	public Point getPrefferedSize() {
		return new Point(750, 700);
	}
	
}
