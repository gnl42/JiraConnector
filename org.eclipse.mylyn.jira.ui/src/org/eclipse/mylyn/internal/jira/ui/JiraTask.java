/*******************************************************************************
 * Copyright (c) 2006 - 2006 Mylar eclipse.org project and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mylar project committers - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.ui;

import org.eclipse.mylar.internal.jira.core.model.Priority;
import org.eclipse.mylar.tasks.core.AbstractRepositoryTask;

/**
 * @author Mik Kersten
 * @author Wesley Coelho (initial integration patch)
 */
public class JiraTask extends AbstractRepositoryTask {

	private String key = null;

	// XXX: from Trac
	public enum Key {
		CC("cc"), CHANGE_TIME("changetime"), COMPONENT("component"), DESCRIPTION("description"), ID("id"), KEYWORDS(
				"keywords"), MILESTONE("milestone"), OWNER("owner"), PRIORITY("priority"), REPORTER("reporter"), RESOLUTION(
				"resolution"), STATUS("status"), SEVERITY("severity"), SUMMARY("summary"), TIME("time"), TYPE("type"), VERSION(
				"version");

		public static Key fromKey(String name) {
			for (Key key : Key.values()) {
				if (key.getKey().equals(name)) {
					return key;
				}
			}
			return null;
		}

		private String key;

		Key(String key) {
			this.key = key;
		}

		@Override
		public String toString() {
			return key;
		}

		public String getKey() {
			return key;
		}
	}
	
	public enum PriorityLevel {
		BLOCKER, CRITICAL, MAJOR, MINOR, TRIVIAL;

		@Override
		public String toString() {
			switch (this) {
			case BLOCKER:
				return "P1";
			case CRITICAL:
				return "P2";
			case MAJOR:
				return "P3";
			case MINOR:
				return "P4";
			case TRIVIAL:
				return "P5";
			default:
				return "P5";
			}
		}

		public static PriorityLevel fromPriority(Priority priority) {
			if (priority == null) {
				return null;
			}
			String priorityId = priority.getId();
			if (priorityId == null)
				return null;
			if (priorityId.equals("1"))
				return BLOCKER;
			if (priorityId.equals("2"))
				return CRITICAL;
			if (priorityId.equals("3"))
				return MAJOR;
			if (priorityId.equals("4"))
				return MINOR;
			if (priorityId.equals("5"))
				return TRIVIAL;
			return null;
		}
	}

	public enum Kind {
		BUG, FEATURE, TASK, IMPROVEMENT, CUSTOM_ISSUE;

		@Override
		public String toString() {
			switch (this) {
			case BUG:
				return "Bug";
			case FEATURE:
				return "New Feature";
			case TASK:
				return "Task";
			case IMPROVEMENT:
				return "Improvement";
			case CUSTOM_ISSUE:
				return "Custom Issue";
			default:
				return "";
			}
		}
	}

	/**
	 * The handle is also the task's Jira url
	 */
	public JiraTask(String repositoryUrl, String id, String label, boolean newTask) {
		super(repositoryUrl, id, label, newTask);
	}

	@Override
	public String getRepositoryKind() {
		return JiraUiPlugin.REPOSITORY_KIND;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getIdentifyingLabel() {
		return getKey();
	}	
}
