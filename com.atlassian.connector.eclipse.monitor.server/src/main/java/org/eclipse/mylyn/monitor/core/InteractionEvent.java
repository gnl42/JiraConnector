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

package org.eclipse.mylyn.monitor.core;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Immutable. Encapsulates interaction made by the user or on behalf of the user.
 * 
 * Also see: http://wiki.eclipse.org/index.php/Mylyn_Integrator_Reference#Monitor_API
 * 
 * @author Mik Kersten
 * @since 2.0
 */
@SuppressWarnings("serial")
public class InteractionEvent implements Serializable {

	/**
	 * Determines the type of interaction that took place, either initiated by the user or done on behalf of the user.
	 */
	public enum Kind {
		/**
		 * User selection of elements, issued by the Eclipse post-selection mechanism.
		 */
		SELECTION,

		/**
		 * Edit events that are created by text selections in an editor.
		 */
		EDIT,

		/**
		 * Commands and actions invoked via buttons, menus, and keyboard shortcuts.
		 */
		COMMAND,

		/**
		 * Workbench preference changes, sometimes made by the user, sometimes automatically on behalf of the user.
		 */
		PREFERENCE,

		/**
		 * Candidates for future interaction.
		 */
		PREDICTION,

		/**
		 * Indirect user interaction with elements (e.g. parent gets implicitly selected when element is selected).
		 */
		PROPAGATION,

		/**
		 * Direct manipulation of interest via actions such as "Mark as Landmark" and "Mark Less Interesting".
		 */
		MANIPULATION,

		/**
		 * Capture interaction with tasks, the workbench, and lifecycle events that define where the user's attention is
		 * directed.
		 */
		ATTENTION;

		/**
		 * TODO: add PREFERENCE?
		 */
		public boolean isUserEvent() {
			return this == SELECTION || this == EDIT || this == COMMAND || this == PREFERENCE;
		}

		/**
		 * @return Simple string representation of the event kind or "null" if no such kind.
		 */
		@Override
		public String toString() {
			switch (this) {
			case SELECTION:
				return "selection"; //$NON-NLS-1$
			case EDIT:
				return "edit"; //$NON-NLS-1$
			case COMMAND:
				return "command"; //$NON-NLS-1$
			case PREFERENCE:
				return "preference"; //$NON-NLS-1$
			case PREDICTION:
				return "prediction"; //$NON-NLS-1$
			case PROPAGATION:
				return "propagation"; //$NON-NLS-1$
			case MANIPULATION:
				return "manipulation"; //$NON-NLS-1$
			case ATTENTION:
				return "attention"; //$NON-NLS-1$
			default:
				return "null"; //$NON-NLS-1$
			}
		}

		/**
		 * @return The corresponding event based on the string provided, or null if no such STring.
		 */
		public static Kind fromString(String string) {
			if (string == null) {
				return null;
			}
			if (string.equals("selection")) { //$NON-NLS-1$
				return SELECTION;
			}
			if (string.equals("edit")) { //$NON-NLS-1$
				return EDIT;
			}
			if (string.equals("command")) { //$NON-NLS-1$
				return COMMAND;
			}
			if (string.equals("preference")) { //$NON-NLS-1$
				return PREFERENCE;
			}
			if (string.equals("prediction")) { //$NON-NLS-1$
				return PREDICTION;
			}
			if (string.equals("propagation")) { //$NON-NLS-1$
				return PROPAGATION;
			}
			if (string.equals("manipulation")) { //$NON-NLS-1$
				return MANIPULATION;
			}
			if (string.equals("attention")) { //$NON-NLS-1$
				return ATTENTION;
			}
			return null;
		}
	}

	private final Kind kind;

	private final Date date;

	private final Date endDate;

	private final String originId;

	private final String structureKind;

	private final String structureHandle;

	private final String navigation;

	private final String delta;
	
	private final float interestContribution;

	/**
	 * Use to specify an uknown identifier, e.g. for an originId.
	 */
	public static final String ID_UNKNOWN = "?"; //$NON-NLS-1$

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId) {
		this(kind, structureKind, handle, originId, 1f);
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId, String navigatedRelation) {
		this(kind, structureKind, handle, originId, navigatedRelation, "null", 1f); //$NON-NLS-1$
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId, String navigatedRelation,
			float interestContribution) {
		this(kind, structureKind, handle, originId, navigatedRelation, "null", interestContribution); //$NON-NLS-1$
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public static InteractionEvent makeCommand(String originId, String delta) {
		return new InteractionEvent(InteractionEvent.Kind.COMMAND, "null", "null", originId, "null", delta, 1); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public static InteractionEvent makeCopy(InteractionEvent originalEvent, float newInterestContribution) {
		return new InteractionEvent(originalEvent.getKind(), originalEvent.getStructureKind(),
				originalEvent.getStructureHandle(), originalEvent.getOriginId(), originalEvent.getNavigation(),
				originalEvent.getDelta(), newInterestContribution, originalEvent.getDate(), originalEvent.getEndDate());
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public static InteractionEvent makePreference(String originId, String delta) {
		return new InteractionEvent(InteractionEvent.Kind.PREFERENCE, "null", "null", originId, "null", delta, 1); // default //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// contribution
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId, float interestContribution) {
		this(kind, structureKind, handle, originId, "null", "null", interestContribution); // default //$NON-NLS-1$ //$NON-NLS-2$
		// contribution
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId, String navigatedRelation,
			String delta, float interestContribution) {
		
		assert kind != null;
		assert originId != null;

		this.kind = kind;
		this.structureKind = structureKind;
		this.structureHandle = handle;
		this.originId = originId;
		this.navigation = navigatedRelation;
		this.delta = delta;
		this.interestContribution = interestContribution;
		this.date = Calendar.getInstance().getTime();
		this.endDate = this.date;
	}

	/**
	 * For parameter description see this class's getters.
	 */
	public InteractionEvent(Kind kind, String structureKind, String handle, String originId, String navigatedRelation,
			String delta, float interestContribution, Date startDate, Date endDate) {
		
		assert kind != null;
		assert originId != null;
		assert startDate != null;
		assert endDate != null;
		
		this.kind = kind;
		this.structureKind = structureKind;
		this.structureHandle = handle;
		this.originId = originId;
		this.navigation = navigatedRelation;
		this.delta = delta;
		this.interestContribution = interestContribution;
		this.date = startDate;
		this.endDate = endDate;
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || !(object instanceof InteractionEvent)) {
			return false;
		}
		InteractionEvent event = (InteractionEvent) object;
		return (date == null ? event.date == null : date.equals(event.date))
				&& (endDate == null ? event.endDate == null : endDate.equals(event.endDate))
				&& (kind == null ? event.kind == null : kind.equals(event.kind))
				&& (structureKind == null ? event.structureKind == null : structureKind.equals(event.structureKind))
				&& (structureHandle == null ? event.structureHandle == null
						: structureHandle.equals(event.structureHandle))
				&& (originId == null ? event.originId == null : originId.equals(event.originId))
				&& (navigation == null ? event.navigation == null : navigation.equals(event.navigation))
				&& (delta == null ? event.delta == null : delta.equals(event.delta))
				&& interestContribution == event.interestContribution;
	}

	@Override
	public int hashCode() {
		int hashCode = 0;
		if (date != null) {
			hashCode += date.hashCode();
		}
		if (endDate != null) {
			hashCode += endDate.hashCode();
		}
		if (kind != null) {
			hashCode += kind.hashCode();
		}
		if (structureKind != null) {
			hashCode += structureKind.hashCode();
		}
		if (structureHandle != null) {
			hashCode += structureHandle.hashCode();
		}
		if (originId != null) {
			hashCode += originId.hashCode();
		}
		if (navigation != null) {
			hashCode += navigation.hashCode();
		}
		if (delta != null) {
			hashCode += delta.hashCode();
		}
		// TODO: could this lose precision?
		hashCode += new Float(interestContribution).hashCode();
		return hashCode;
	}

	@Override
	public String toString() {
		return "(date: " + date + ", kind: " + kind + ", sourceHandle: " + structureHandle + ", origin: " + originId //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
				+ ", delta: " + delta + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public boolean isValidStructureHandle() {
		return structureHandle != null && !structureHandle.equals("null") && !structureHandle.trim().equals(ID_UNKNOWN); //$NON-NLS-1$
	}

	// TODO 4.0 change to getHandleIdentifier()
	public String getStructureHandle() {
		return structureHandle;
	}

	/**
	 * @return The content type of the element being interacted with.
	 */
	public String getStructureKind() {
		return structureKind;
	}

	/**
	 * @return Time stamp for the occurrence of the event.
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Can be used for extensibility, e.g. by adding an XML-encoded String.
	 * 
	 * @return Additional information relevant to interaction monitoring.
	 */
	public String getDelta() {
		return delta;
	}

	/**
	 * @return Defines the kind of interaction that took place.
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * @return The UI affordance that the event was issued from.
	 */
	public String getOriginId() {
		return originId;
	}

	/**
	 * @return If an aggregate event, amount of interest of all contained events.
	 */
	// TODO: consider refactoring in order to de-couple events from interest.
	public float getInterestContribution() {
		return interestContribution;
	}

	/**
	 * @return If an aggregate event, time stamp of the last occurrence.
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @return An identifier for the kind of relation that corresponds to the navigation to this element.
	 */
	public String getNavigation() {
		return navigation;
	}
}
