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

package com.atlassian.connector.eclipse.crucible.ui.preferences;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;

public enum ActivateReview {
	ALWAYS("Always", MessageDialogWithToggle.ALWAYS), PROMPT("Prompt", MessageDialogWithToggle.PROMPT), NEVER("Never",
			MessageDialogWithToggle.NEVER);

	private final String key;

	private final String label;

	ActivateReview(String label, String key) {
		this.label = label;
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getLabel() {
		return label;
	}

	public String toString() {
		return key;
	}

	public static ActivateReview getObjectFromKey(String key) {
		for (ActivateReview obj : values()) {
			if (obj.getKey().equals(key)) {
				return obj;
			}
		}

		return null;
	}

}
