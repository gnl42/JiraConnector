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

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that is scheduled periodically to check for user activity.
 * 
 * @author Robert Elves
 * @author Steffen Pingel
 */
public class CheckActivityJob extends Job {

	/**
	 * If true, the user is assumed to be active.
	 */
	private boolean active;

	/**
	 * The rescheduling interval in ms when active. Should be reasonably short to provide accurate tracking.
	 */
	private final int ACTIVE_TICK = 30 * 1000;

	private final IActivityManagerCallback callback;

	/**
	 * The timeout when to assume a user is inactive. If set to 0 it is assumed that a user is always active.
	 */
	private int inactivityTimeout;

	protected long previousEventTime;

	/**
	 * Protected for testing.
	 */
	protected long tick = ACTIVE_TICK;

	public CheckActivityJob(IActivityManagerCallback callback) {
		super(Messages.CheckActivityJob_Activity_Monitor_Job);
		this.callback = callback;
	}

	public int getInactivityTimeout() {
		return this.inactivityTimeout;
	}

	public boolean isActive() {
		return active;
	}

	protected boolean isEnabled() {
		return Platform.isRunning() && !MonitorUiPlugin.getDefault().getWorkbench().isClosing();
	}

	/**
	 * Uses a short interval when inactive. This makes event notifications more accurate when switching from an inactive
	 * to an active state, e.g. to ensure lively updates of the UI.
	 */
	public void reschedule() {
		schedule(active ? tick : tick / 6);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (isEnabled()) {
			try {
				long lastEventTime = callback.getLastEventTime();
				long currentTime = System.currentTimeMillis();
				// check if the last activity exceeds timeout
				if ((currentTime - lastEventTime) >= inactivityTimeout && inactivityTimeout != 0) {
					if (active) {
						// time out
						active = false;
						callback.inactive();
					}
				} else {
					if (!active) {
						active = true;
						// back, start recording activity
						if (inactivityTimeout != 0) {
							previousEventTime = lastEventTime;
						} else {
							// if timeouts are disabled only the currentTime is relevant for tracking activity 
							previousEventTime = currentTime;
						}
						callback.active();
					} else {
						// check if the activity internal is unreasonably long, it is likely that 
						// the computer came back from sleep at worst difference should be tick * 2
						if (currentTime - previousEventTime > tick * 3) {
							if (inactivityTimeout != 0) {
								// check for recent event
								if (currentTime - lastEventTime <= tick) {
									// event since resume
									previousEventTime = lastEventTime;
								} else {
									// time out
									active = false;
									callback.inactive();
								}
							} else {
								// if timeouts are disabled only the currentTime is relevant for tracking activity 
								previousEventTime = currentTime;
							}
						} else {
							callback.addMonitoredActivityTime(previousEventTime, currentTime);
							previousEventTime = currentTime;
						}
					}
				}
			} finally {
				reschedule();
			}
		}
		return Status.OK_STATUS;
	}

	public void setInactivityTimeout(int inactivityTimeout) {
		this.inactivityTimeout = inactivityTimeout;
	}

}
