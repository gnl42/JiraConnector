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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.PatternMatchEvent;

@SuppressWarnings("restriction")
public class JavaCompilationErrorTracker extends JavaStackTraceTracker {

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			int offset = event.getOffset();
			int length = event.getLength();
			IHyperlink link = new JavaCompilationErrorHyperlink(getConsole());
			getConsole().addHyperlink(link, offset, length);
		} catch (BadLocationException e) {
		}
	}

	@Override
	public String getPattern() {
		try {
			return String.format("[^\\(\\s/]+%s\\[\\d*,\\d*\\]", new JavaLikeExtensionsResolver().resolveValue(null,
					null));
		} catch (CoreException e) {
			return null;
		}
	}

}
