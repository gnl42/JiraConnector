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

package com.atlassian.theplugin.eclipse.core.operation.bamboo;

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Save repository location changes operation
 * 
 * @author Alexander Gurov
 */
public class SaveBambooServersOperation extends AbstractNonLockingOperation {
	public SaveBambooServersOperation() {
		super("Operation.SaveBambooServers");
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		BambooConfigurationStorage.instance().saveConfiguration();
	}
	
}
