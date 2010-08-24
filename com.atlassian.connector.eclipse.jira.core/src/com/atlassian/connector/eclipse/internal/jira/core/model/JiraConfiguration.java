/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model;

/**
 * @author Jacek Jaroczynski
 */
public class JiraConfiguration {

	private int timeTrackingHoursPerDay;

	private int timeTrackingDaysPerWeek;

	private boolean allowAttachments;

	private boolean allowExternalUserManagment;

	private boolean allowIssueLinking;

	private boolean allowSubTasks;

	private boolean allowTimeTracking;

	private boolean allowUnassignedIssues;

	private boolean allowVoting;

	private boolean allowWatching;

	public void setTimeTrackingHoursPerDay(int timeTrackingHoursPerDay) {
		this.timeTrackingHoursPerDay = timeTrackingHoursPerDay;
	}

	public void setTimeTrackingDaysPerWeek(int timeTrackingDaysPerWeek) {
		this.timeTrackingDaysPerWeek = timeTrackingDaysPerWeek;
	}

	public void setAllowAttachments(boolean allowAttachments) {
		this.allowAttachments = allowAttachments;
	}

	public void setAllowExternalUserManagment(boolean allowExternalUserManagment) {
		this.allowExternalUserManagment = allowExternalUserManagment;
	}

	public void setAllowIssueLinking(boolean allowIssueLinking) {
		this.allowIssueLinking = allowIssueLinking;
	}

	public void setAllowSubTasks(boolean allowSubTasks) {
		this.allowSubTasks = allowSubTasks;
	}

	public void setAllowTimeTracking(boolean allowTimeTracking) {
		this.allowTimeTracking = allowTimeTracking;
	}

	public void setAllowUnassignedIssues(boolean allowUnassignedIssues) {
		this.allowUnassignedIssues = allowUnassignedIssues;
	}

	public void setAllowVoting(boolean allowVoting) {
		this.allowVoting = allowVoting;
	}

	public void setAllowWatching(boolean allowWatching) {
		this.allowWatching = allowWatching;
	}

	public int getTimeTrackingHoursPerDay() {
		return timeTrackingHoursPerDay;
	}

	public int getTimeTrackingDaysPerWeek() {
		return timeTrackingDaysPerWeek;
	}

	public boolean isAllowAttachments() {
		return allowAttachments;
	}

	public boolean isAllowExternalUserManagment() {
		return allowExternalUserManagment;
	}

	public boolean isAllowIssueLinking() {
		return allowIssueLinking;
	}

	public boolean isAllowSubTasks() {
		return allowSubTasks;
	}

	public boolean isAllowTimeTracking() {
		return allowTimeTracking;
	}

	public boolean isAllowUnassignedIssues() {
		return allowUnassignedIssues;
	}

	public boolean isAllowVoting() {
		return allowVoting;
	}

	public boolean isAllowWatching() {
		return allowWatching;
	}
}
