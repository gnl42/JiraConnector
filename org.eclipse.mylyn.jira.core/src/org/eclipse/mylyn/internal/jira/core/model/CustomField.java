/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom field container
 * 
 * @author Eugene Kuleshov
 */
public class CustomField implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String id;

	private final String key;

	private final String name;

	private final List<String> values;

	private boolean readOnly; 

	private boolean markupDetected;
	
	public CustomField(String id, String key, String name, List<String> values) {
		this.id = id;
		this.key = key;
		this.name = name;
		this.values = new ArrayList<String>(values);
	}

	public String getId() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
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
