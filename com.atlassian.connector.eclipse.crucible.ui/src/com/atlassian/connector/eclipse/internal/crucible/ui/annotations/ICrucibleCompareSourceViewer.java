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

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import com.atlassian.connector.commons.misc.IntRanges;
import org.eclipse.jface.text.source.LineRange;

public interface ICrucibleCompareSourceViewer {
	LineRange getSelection();

	void focusOnLines(IntRanges ranges);

	void registerContextMenu();
}