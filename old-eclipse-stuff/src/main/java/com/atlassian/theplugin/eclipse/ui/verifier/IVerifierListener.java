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

package com.atlassian.theplugin.eclipse.ui.verifier;

/**
 * Verifier events listener interface
 * 
 * @author Alexander Gurov
 */
public interface IVerifierListener {
    public void hasError(String errorReason);
    public void hasWarning(String warningReason);
    public void hasNoError();
}
