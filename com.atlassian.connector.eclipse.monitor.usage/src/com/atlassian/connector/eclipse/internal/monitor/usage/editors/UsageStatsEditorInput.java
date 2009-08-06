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

package com.atlassian.connector.eclipse.internal.monitor.usage.editors;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.atlassian.connector.eclipse.internal.monitor.usage.ReportGenerator;

/**
 * @author Mik Kersten
 */
public class UsageStatsEditorInput implements IEditorInput {

	private final ReportGenerator reportGenerator;

	private final List<File> usageFiles;

	/**
	 * Supports either the single workspace file or multiple zip files.
	 */
	public UsageStatsEditorInput(List<File> files, ReportGenerator reportGenerator) {
		// try {
		this.reportGenerator = reportGenerator;
		usageFiles = files;
		reportGenerator.getStatisticsFromInteractionHistories(usageFiles, null);
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return "Usage Summary";
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return "Mylyn Usage Statistics";
	}

	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		return null;
	}

	public List<File> getInputFiles() {
		return usageFiles;
	}

	public ReportGenerator getReportGenerator() {
		return reportGenerator;
	}

	private final byte[] buffer = new byte[8192];

	public void transferData(InputStream sourceStream, OutputStream destination) throws IOException {
		int bytesRead = 0;
		while (bytesRead != -1) {
			bytesRead = sourceStream.read(buffer, 0, buffer.length);
			if (bytesRead != -1) {
				destination.write(buffer, 0, bytesRead);
			}
		}
	}
}
