/*******************************************************************************
 * Copyright (c) 2004 - 2006 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira;

import org.eclipse.mylar.internal.tasklist.AbstractRepositoryQuery;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 */
public class JiraRepositoryQuery extends AbstractRepositoryQuery {

	@Override
	public String getRepositoryKind() {
		return MylarJiraPlugin.REPOSITORY_KIND;
	}

	public Image getStatusIcon() {
		return null;
	}

	public boolean isDragAndDropEnabled() {
		return false;
	}

	public boolean isActivatable() {
		return false;
	}

	public Font getFont() {
		return null;
	}

}
