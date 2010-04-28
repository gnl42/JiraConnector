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

import org.eclipse.jdt.internal.debug.ui.console.JavaNativeStackTraceHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

@SuppressWarnings("restriction")
public class JavaNativeTracker extends JavaStackTraceTracker {

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			int offset = event.getOffset();
			int length = event.getLength();
			TextConsole console = getConsole();
			IHyperlink link = new JavaNativeStackTraceHyperlink(console);
			console.addHyperlink(link, offset + 1, length - 2);
		} catch (BadLocationException e) {
		}
	}

	@Override
	public String getPattern() {
		return "\\(Native Method\\)";
	}

	@Override
	public String getLineQualifier() {
		return "Native Method";
	}
}
