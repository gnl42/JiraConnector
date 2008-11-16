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

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.core.operation.IUnprotectedOperation;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Discard location operation
 * 
 * @author Alexander Gurov
 */
public class DeleteBambooServerOperation extends AbstractNonLockingOperation {
	protected IBambooServer []servers;
	
	public DeleteBambooServerOperation(IBambooServer []servers) {
		super("Operation.DeleteBambooServer");
		this.servers = servers;
	}

	protected void runImpl(IProgressMonitor monitor) throws Exception {
		final BambooConfigurationStorage storage = BambooConfigurationStorage.instance();
		for (int i = 0; i < this.servers.length; i++) {
			final IBambooServer current = this.servers[i];
			this.protectStep(new IUnprotectedOperation() {
				public void run(IProgressMonitor monitor) throws Exception {
					storage.removeBambooServer(current);
				}
			}, monitor, this.servers.length);
		}
	}

}
