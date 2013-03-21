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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Jacek Jaroczynski
 */
public enum PlanBranches {
	NO("No"), ALL("All Plan Branches"), MINE("My Plan Branches");

	private final String text;

	PlanBranches(String text) {
		this.text = text;
	}

	public static PlanBranches from(String text) {
		for (PlanBranches value : values()) {
			if (value.getText() != null && value.getText().equals(text)) {
				return value;
			}
		}
		return NO;
	}

	public static String[] stringValues() {
		List<String> ret = new ArrayList<String>(values().length);

		for (PlanBranches pb : values()) {
			ret.add(pb.getText());
		}

		return ret.toArray(new String[ret.size()]);
	}

	public String getText() {
		return text;
	}
}