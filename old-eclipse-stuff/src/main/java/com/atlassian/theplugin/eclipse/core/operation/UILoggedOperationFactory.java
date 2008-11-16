/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov (Polarion Software) - initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.core.operation;

import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.util.ILoggedOperationFactory;

/**
 * UI logged operation factory
 * 
 * @author Alexander Gurov
 */
public class UILoggedOperationFactory implements ILoggedOperationFactory {
	public IActionOperation getLogged(IActionOperation operation) {
		IActionOperation retVal = this.wrappedOperation(operation);
		
		retVal.setConsoleStream(Activator.getDefault().getConsoleStream());
		
		return retVal;
	}

	protected IActionOperation wrappedOperation(IActionOperation operation) {
		return new UILoggedOperation(operation);
	}
}
