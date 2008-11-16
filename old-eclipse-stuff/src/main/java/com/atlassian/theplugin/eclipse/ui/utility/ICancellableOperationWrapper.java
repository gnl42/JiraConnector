/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.utility;

import org.eclipse.jface.operation.IRunnableWithProgress;

import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;

/**
 * Interface that provide information about executed operation and cacellation flag
 * 
 * @author Alexander Gurov
 */
public interface ICancellableOperationWrapper extends IRunnableWithProgress {
	public void setCancelled(boolean cancelled);
	public boolean isCancelled();
	public IActionOperation getOperation();
	public String getOperationName();
}

