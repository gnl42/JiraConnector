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

package org.eclipse.mylyn.monitor.ui;

/**
 * Extend to monitor periods of user activity and inactivity.
 * 
 * @author Mik Kersten
 * @author Rob Elves
 * @since 2.0
 */
public abstract class AbstractUserActivityMonitor {

	private long lastEventTimeStamp = -1;

	/**
	 * @since 2.0
	 */
	public long getLastInteractionTime() {
		synchronized (this) {
			return lastEventTimeStamp;
		}
	}

	/**
	 * @since 2.0
	 */
	public void setLastEventTime(long lastEventTime) {
		synchronized (this) {
			lastEventTimeStamp = lastEventTime;
		}
	}

	/**
	 * @since 2.0
	 */
	public abstract void start();

	/**
	 * @since 2.0
	 */
	public abstract void stop();

	/**
	 * @return false if monitor unable to run (i.e. startup failures of any kind)
	 * @since 2.0
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * @since 3.1
	 */
	public String getOriginId() {
		return null;
	}

}
