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

package com.atlassian.theplugin.eclipse.core.operation;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Unprotected action does not catch any exception that can be throwed by it 
 * 
 * @author Alexander Gurov
 */
public interface IUnprotectedOperation {
	public void run(IProgressMonitor monitor) throws Exception;
}
