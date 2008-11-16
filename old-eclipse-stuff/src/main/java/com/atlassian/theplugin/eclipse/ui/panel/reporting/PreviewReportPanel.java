
/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.panel.reporting;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Preview any product quality report panel
 * 
 * @author Alexander Gurov
 */
public class PreviewReportPanel extends PreviewPanel {
	public PreviewReportPanel(String description, String report) {
		super(Activator.getDefault().getResource("PreviewReportPanel.Preview"), description, 
				Activator.getDefault().getResource("PreviewReportPanel.Message"), report);
	}
	
	public String getHelpId() {
    	return "org.eclipse.team.svn.help.previewReportDialogContext";
	}	
}
