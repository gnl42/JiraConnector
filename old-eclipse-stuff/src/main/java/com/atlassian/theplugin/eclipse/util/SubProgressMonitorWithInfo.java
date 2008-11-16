/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.util;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * This subprogress monitor allow us to get current progress state
 * 
 * @author Alexander Gurov
 */
public class SubProgressMonitorWithInfo extends SubProgressMonitor {
	protected int currentProgress;
	protected double unknownProgress;
	protected int total;
	protected long lastTime;
	
	public SubProgressMonitorWithInfo(IProgressMonitor monitor, int ticks) {
		super(monitor, ticks);
		this.total = ticks;
		this.lastTime = 0;
	}

	public SubProgressMonitorWithInfo(IProgressMonitor monitor, int ticks, int style) {
		super(monitor, ticks, style);
		this.total = ticks;
		this.lastTime = 0;
	}
	
	public void beginTask(String name, int totalWork) {
		this.currentProgress = 0;
		this.unknownProgress = 0;
		super.beginTask(name, totalWork);
	}

	public void setTaskName(String name) {
		long time = System.currentTimeMillis();
		// redraw four times per second
		if (this.lastTime == 0 || (time - this.lastTime) >= 250) {
			this.lastTime = time;
			super.setTaskName(name);
		}
	}
	
	public void subTask(String name) {
		long time = System.currentTimeMillis();
		// redraw four times per second
		if (this.lastTime == 0 || (time - this.lastTime) >= 250) {
			this.lastTime = time;
			super.subTask(name);
		}
	}
	
	public void worked(int work) {
		this.currentProgress += work;
		super.worked(work);
	}
	
	public void unknownProgress(int current) {
		this.unknownProgress += 1f / ((this.unknownProgress + 1) * (this.unknownProgress > 75 ? this.unknownProgress : 1));
		int offset = (int)(this.unknownProgress - this.currentProgress);
		this.worked(offset);
	}
	
	public int getCurrentProgress() {
		return this.currentProgress;
	}
	
}
