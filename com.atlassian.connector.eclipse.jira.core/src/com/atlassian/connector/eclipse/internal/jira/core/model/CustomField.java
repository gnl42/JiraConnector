/*******************************************************************************
 * Copyright (c) 2004, 2008 Eugene Kuleshov and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eugene Kuleshov - initial API and implementation
 *     Tasktop Technologies - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom field container.
 * 
 * @author Eugene Kuleshov
 * @author Steffen Pingel
 */
public class CustomField extends IssueField {

	private static final long serialVersionUID = 1L;

	private final String key;

	private final List<String> values;

	private boolean readOnly;

	private boolean markupDetected;

	public CustomField(String id, String key, String name, List<String> values) {
		super(id, name);
		this.key = key;
		this.values = new ArrayList<String>(values);
	}

	public String getKey() {
		return key;
	}

	public List<String> getValues() {
		return values;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public boolean isMarkupDetected() {
		return markupDetected;
	}

	public void setMarkupDetected(boolean markupDetected) {
		this.markupDetected = markupDetected;
	}

}
