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

package com.atlassian.connector.eclipse.internal.monitor.usage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;

/**
 * Used for logging interaction events.
 * 
 * @author Mik Kersten
 * @since 2.0
 */
public abstract class AbstractMonitorLog {

	protected File outputFile;

	protected FileOutputStream outputStream;

	protected boolean started = false;

	public AbstractMonitorLog() {
		super();
	}

	public void startMonitoring() {
		synchronized (this) {
			if (started) {
				return;
			} else {
				started = true;
			}
		}
		try {
			if (!outputFile.exists()) {
				outputFile.createNewFile();
			}
			outputStream = new FileOutputStream(outputFile, true);
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN, "Could not log to file: " //$NON-NLS-1$
					+ outputFile.getAbsolutePath(), e));
		}
	}

	public void stopMonitoring() {
		try {
			if (outputStream != null) {
				outputStream.flush();
				outputStream.close();
			}
			started = false;
		} catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					"Could not close interaction event stream", e)); //$NON-NLS-1$
		}
	}

	public File moveOutputFile(String newPath) {
		stopMonitoring();
		File newFile = new File(newPath);
		try {
			if (outputFile.exists() && !newFile.exists()) {
				outputFile.renameTo(newFile);
			} else if (!newFile.exists()) {
				newFile.createNewFile();
				outputFile.delete();
			} else {
				outputFile.delete();
			}
			this.outputFile = newFile;
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.ERROR, UiUsageMonitorPlugin.ID_PLUGIN,
					"Could not set logger output file", e)); //$NON-NLS-1$
		}
		startMonitoring();
		return newFile;
	}

	public File getOutputFile() {
		return outputFile;
	}

}
