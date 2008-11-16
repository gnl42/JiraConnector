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

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooConfigurationStorage;

/**
 * Add repository location implementation
 * 
 * @author Alexander Gurov
 */
public class AddBambooServerOperation extends AbstractNonLockingOperation {
	protected IBambooServer server;

	public AddBambooServerOperation(IBambooServer server) {
		super("Operation.AddBambooServer");
		this.server = server;
	}

	public ISchedulingRule getSchedulingRule() {
		return null;
	}
	
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		BambooConfigurationStorage.instance().addBambooServer(this.server);
	}
	
	protected String getShortErrorMessage(Throwable t) {
		return MessageFormat.format(super.getShortErrorMessage(t), new Object[] {this.server.getUrl()});
	}

}
