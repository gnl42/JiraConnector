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

package org.eclipse.mylyn.internal.monitor.core.collection;

import org.eclipse.mylyn.monitor.core.InteractionEvent;

/**
 * Test whether an InteractionEvent meets particular criteria
 * 
 * @author Gail Murphy
 * @author Mik Kersten
 */
public class InteractionEventClassifier {

	/**
	 * isEdit currently classifies selections in editor as edits. May need to split off a different version
	 */
	public static boolean isEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT)
				|| (event.getKind().equals(InteractionEvent.Kind.SELECTION) && isSelectionInEditor(event));
	}

	public static boolean isSelection(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.SELECTION) && !isSelectionInEditor(event);
	}

	public static boolean isCommand(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.COMMAND);
	}

	public static boolean isJavaEdit(InteractionEvent event) {
		return event.getKind().equals(InteractionEvent.Kind.EDIT)
				&& (event.getOriginId().contains("java") || event.getOriginId().contains("jdt.ui")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static boolean isJDTEvent(InteractionEvent event) {
		return (isEdit(event) || isSelection(event) || isCommand(event)) && getCleanOriginId(event).contains("jdt"); //$NON-NLS-1$
	}

	public static boolean isSelectionInEditor(InteractionEvent event) {
		return event.getOriginId().contains("Editor") || event.getOriginId().contains("editor") //$NON-NLS-1$ //$NON-NLS-2$
				|| event.getOriginId().contains("source"); //$NON-NLS-1$
	}

	public static String getCleanOriginId(InteractionEvent event) {
		String cleanOriginId = ""; //$NON-NLS-1$
		String originId = event.getOriginId();

		if (event.getKind().equals(InteractionEvent.Kind.COMMAND)) {
			for (int i = 0; i < originId.length(); i++) {
				char curChar = originId.charAt(i);
				if (!(curChar == '&')) {
					if (Character.getType(curChar) == Character.CONTROL) {
						cleanOriginId = cleanOriginId.concat(" "); //$NON-NLS-1$
					} else {
						cleanOriginId = cleanOriginId.concat(String.valueOf(curChar));
					}
				}
			}
			return cleanOriginId;
		} else {
			return originId;
		}
	}

	public static String formatDuration(long timeToFormatInms) {
		long timeInSeconds = timeToFormatInms / 1000;
		long hours, minutes;
		hours = timeInSeconds / 3600;
		timeInSeconds = timeInSeconds - (hours * 3600);
		minutes = timeInSeconds / 60;
		timeInSeconds = timeInSeconds - (minutes * 60);
		return hours + "." + minutes; //$NON-NLS-1$
	}

}
