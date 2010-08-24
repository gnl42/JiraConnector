/*******************************************************************************
 * Copyright (c) 2004, 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.monitor.usage;

import java.util.Date;

import org.eclipse.core.runtime.Assert;

/**
 * @author Mik Kersten
 * @author Pawel Niewiadomski
 */
public class InteractionEvent {

	/**
	 * Determines the type of interaction that took place, either initiated by the user or done on behalf of the user.
	 */
	public enum Kind {
		ACTION, COMMAND, JOB, PREFERENCE, VIEW, PERSPECTIVE;

		/**
		 * @return Simple string representation of the event kind or "null" if no such kind.
		 */
		@Override
		public String toString() {
			switch (this) {
			case COMMAND:
				return "command";
			case PREFERENCE:
				return "preference";
			case ACTION:
				return "action";
			case VIEW:
				return "view";
			case JOB:
				return "job";
			case PERSPECTIVE:
				return "perspective";
			default:
				return "null";
			}
		}

		/**
		 * @return The corresponding event based on the string provided, or null if no such STring.
		 */
		public static Kind fromString(String string) {
			if (string == null) {
				return null;
			}
			if (string.equals("command")) {
				return COMMAND;
			}
			if (string.equals("preference")) {
				return PREFERENCE;
			}
			if (string.equals("action")) {
				return ACTION;
			}
			if (string.equals("view")) {
				return VIEW;
			}
			if (string.equals("perspective")) {
				return PERSPECTIVE;
			}
			return null;
		}
	}

	private final Kind kind;

	private final Date date;

	private final String pluginId;

	private final String eventId;

	private final String details;

	/**
	 * Use to specify an uknown identifier, e.g. for an originId.
	 */
	public static final String ID_UNKNOWN = "?"; //$NON-NLS-1$

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String pluginId, String eventId, String details, Date startDate) {
		Assert.isNotNull(kind);
		Assert.isNotNull(pluginId);
		Assert.isNotNull(eventId);
		this.kind = kind;
		this.pluginId = (pluginId != null) ? pluginId.intern() : null;
		this.eventId = (eventId != null) ? eventId.intern() : null;
		this.details = (details != null) ? details.intern() : null;
		this.date = startDate;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof InteractionEvent)) {
			return false;
		}
		InteractionEvent event = (InteractionEvent) object;
		return (date == null ? event.date == null : date.equals(event.date))
				&& (kind == null ? event.kind == null : kind.equals(event.kind))
				&& (pluginId == null ? event.pluginId == null : pluginId.equals(event.pluginId))
				&& (eventId == null ? event.eventId == null : eventId.equals(event.eventId))
				&& (details == null ? event.details == null : details.equals(event.details));
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (date != null) {
			hashCode += date.hashCode();
		}
		if (kind != null) {
			hashCode += kind.hashCode();
		}
		if (pluginId != null) {
			hashCode += pluginId.hashCode();
		}
		if (eventId != null) {
			hashCode += eventId.hashCode();
		}
		if (details != null) {
			hashCode += details.hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString() {
		return "(date: " + date + ", kind: " + kind + ", pluginId: " + pluginId + ", eventId: " + eventId //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ ", details: " + details + ")";
	}

	/**
	 * @return Time stamp for the occurrence of the event.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * @return Defines the kind of interaction that took place.
	 */
	public Kind getKind() {
		return kind;
	}

	public String getPluginId() {
		return pluginId;
	}

	public String getEventId() {
		return eventId;
	}

	public String getDetails() {
		return details;
	}

	public static InteractionEvent makePreference(String idPlugin, String string, String value) {
		return new InteractionEvent(Kind.PREFERENCE, idPlugin, string, value, new Date());
	}

}
