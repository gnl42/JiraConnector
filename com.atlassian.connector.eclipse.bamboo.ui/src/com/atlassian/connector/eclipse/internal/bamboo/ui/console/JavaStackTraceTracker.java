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

package com.atlassian.connector.eclipse.internal.bamboo.ui.console;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.debug.ui.console.JavaLikeExtensionsResolver;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class JavaStackTraceTracker implements IPatternMatchListener {
	private TextConsole fConsole;

	public void matchFound(PatternMatchEvent event) {
		try {
			int offset = event.getOffset();
			int length = event.getLength();
			IHyperlink link = new JavaStackTraceHyperlink(fConsole);
			fConsole.addHyperlink(link, offset + 1, length - 2);
		} catch (BadLocationException e) {
		}
	}

	public void disconnect() {
		fConsole = null;
	}

	public void connect(TextConsole console) {
		fConsole = console;
	}

	public String getPattern() {
		return String.format("\\(\\S*%s\\S*\\)", getLineQualifier());
	}

	public String getLineQualifier() {
		try {
			return new JavaLikeExtensionsResolver().resolveValue(null, null);
		} catch (CoreException e) {
			return null;
		}
	}

	public int getCompilerFlags() {
		return 0;
	}

	protected TextConsole getConsole() {
		return fConsole;
	}
}
