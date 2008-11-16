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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.atlassian.theplugin.eclipse.preferences.Activator;

/**
 * Abstract field verifier implementation
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractVerifier {
	protected List<IVerifierListener> listeners;
	protected boolean filledRight;
	protected boolean hasWarning;

	public AbstractVerifier() {
		this.listeners = new ArrayList<IVerifierListener>();
		this.filledRight = false;
		this.hasWarning = false;
	}

	public synchronized void addVerifierListener(IVerifierListener listener) {
		synchronized (this.listeners) {
			this.listeners.add(listener);
		}
	}

	public void removeVerifierListener(IVerifierListener listener) {
		synchronized (this.listeners) {
			this.listeners.remove(listener);
		}
	}

	public boolean isFilledRight() {
		return this.filledRight;
	}

	public boolean hasWarning() {
		return this.hasWarning;
	}

	public boolean verify(Control input) {
		String msg = this.getErrorMessage(input);
		if (msg != null) {
			this.fireError(msg);
			return false;
		}
		msg = this.getWarningMessage(input);
		if (msg != null) {
			this.fireWarning(msg);
		} else {
			this.fireOk();
		}
		return true;
	}

	protected abstract String getErrorMessage(Control input);

	protected abstract String getWarningMessage(Control input);

	protected String getText(Control input) {
		if (input instanceof Text) {
			return ((Text) input).getText();
		} else if (input instanceof Combo) {
			return ((Combo) input).getText();
		}
		String message = Activator.getDefault()
				.getResource("Verifier.Abstract");
		throw new RuntimeException(MessageFormat.format(message,
				new Object[] { this.getClass().getName() }));
	}

	protected void fireError(String errorReason) {
		this.filledRight = false;
		this.hasWarning = false;

		Object[] listeners = null;
		synchronized (this.listeners) {
			listeners = this.listeners.toArray();
		}
		for (int i = listeners.length - 1; i >= 0; i--) {
			((IVerifierListener) listeners[i]).hasError(errorReason);
		}
	}

	protected void fireWarning(String warningReason) {
		this.filledRight = true;
		this.hasWarning = true;
		Object[] listeners = null;
		synchronized (this.listeners) {
			listeners = this.listeners.toArray();
		}
		for (int i = listeners.length - 1; i >= 0; i--) {
			((IVerifierListener) listeners[i]).hasWarning(warningReason);
		}
	}

	protected void fireOk() {
		this.filledRight = true;
		this.hasWarning = false;
		Object[] listeners = null;
		synchronized (this.listeners) {
			listeners = this.listeners.toArray();
		}
		for (int i = listeners.length - 1; i >= 0; i--) {
			((IVerifierListener) listeners[i]).hasNoError();
		}
	}

}
