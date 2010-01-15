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

package com.atlassian.connector.eclipse.ui.commons;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.text.source.LineRange;

public final class ResourceEditorBean {
	private final IResource resource;

	private final LineRange lineRange;

	public ResourceEditorBean(IResource resource, LineRange lineRange) {
		this.resource = resource;
		this.lineRange = lineRange;
	}

	public IResource getResource() {
		return resource;
	}

	public LineRange getLineRange() {
		return lineRange;
	}
}