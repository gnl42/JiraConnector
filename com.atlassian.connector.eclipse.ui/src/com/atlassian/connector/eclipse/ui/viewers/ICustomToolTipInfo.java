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

package com.atlassian.connector.eclipse.ui.viewers;

import com.atlassian.connector.eclipse.ui.commons.CustomToolTip;

import org.eclipse.swt.widgets.Composite;

public interface ICustomToolTipInfo {

	void createToolTipArea(CustomToolTip tooltip, Composite composite);

	boolean isContainer();

}
