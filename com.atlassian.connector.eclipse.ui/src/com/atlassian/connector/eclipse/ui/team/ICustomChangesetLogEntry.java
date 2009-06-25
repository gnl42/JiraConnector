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

package com.atlassian.connector.eclipse.ui.team;

import java.util.Date;

public interface ICustomChangesetLogEntry extends Comparable<ICustomChangesetLogEntry> {

	String getComment();

	String getAuthor();

	String getRevision();

	String[] getChangedFiles();

	RepositoryInfo getRepository();

	Date getDate();

	int compareTo(ICustomChangesetLogEntry other);
}