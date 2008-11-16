/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.verifier;

import org.eclipse.swt.widgets.Control;

/**
 * Abstract verifier proxy implementation
 * 
 * @author Sergiy Logvin
 */
public abstract class AbstractVerifierProxy extends AbstractVerifier {
	protected AbstractVerifier verifier;

	public AbstractVerifierProxy(AbstractVerifier verifier) {
		super();
		this.verifier = verifier;
	}

	public void addVerifierListener(IVerifierListener listener) {
		this.verifier.addVerifierListener(listener);
		super.addVerifierListener(listener);
	}

	public void removeVerifierListener(IVerifierListener listener) {
		this.verifier.removeVerifierListener(listener);
		super.removeVerifierListener(listener);
	}

	public boolean verify(Control input) {
		if (this.isVerificationEnabled(input)) {
			return this.verifier.verify(input);
		}
		if (!(this.hasWarning = this.verifier.hasWarning())) {
			this.fireOk();
		}
		return true;
	}
	
	protected abstract boolean isVerificationEnabled(Control input);

	protected String getErrorMessage(Control input) {
		return null;
	}

	protected String getWarningMessage(Control input) {
		return null;
	}

}
