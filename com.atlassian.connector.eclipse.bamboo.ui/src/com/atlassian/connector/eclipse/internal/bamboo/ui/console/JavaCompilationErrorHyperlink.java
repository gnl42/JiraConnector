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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.console.ConsoleMessages;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.console.TextConsole;

@SuppressWarnings("restriction")
public class JavaCompilationErrorHyperlink extends JavaStackTraceHyperlink {

	public JavaCompilationErrorHyperlink(TextConsole console) {
		super(console);
	}

	/**
	 * Returns the fully qualified name of the type to open
	 * 
	 * @return fully qualified type name
	 * @exception CoreException
	 *                if unable to parse the type name
	 */
	@Override
	protected String getTypeName(String linkText) throws CoreException {
		int start = 0;
		int end = linkText.indexOf(':');
		if (start >= 0 && end > start) {
			//linkText could be something like packageA.TypeB(TypeA.java:45)
			//need to look in packageA.TypeA for line 45 since TypeB is defined
			//in TypeA.java 
			//Inner classes can be ignored because we're using file and line number

			// get File name (w/o .java)
			String typeName = linkText.substring(start, end);
			typeName = JavaCore.removeJavaLikeExtension(typeName);

			String qualifier = linkText.substring(0, start);
			// remove the method name
			start = qualifier.lastIndexOf('.');

			if (start >= 0) {
				// remove the class name
				start = new String((String) qualifier.subSequence(0, start)).lastIndexOf('.');
				if (start == -1) {
					start = 0; // default package
				}
			}

			if (start >= 0) {
				qualifier = qualifier.substring(0, start);
			}

			if (qualifier.length() > 0) {
				typeName = qualifier + "." + typeName; //$NON-NLS-1$
			}
			return typeName;
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0,
				ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_type_name_from_hyperlink__5, null);
		throw new CoreException(status);
	}

	/**
	 * Returns the line number associated with the stack trace or -1 if none.
	 * 
	 * @exception CoreException
	 *                if unable to parse the number
	 */
	@Override
	protected int getLineNumber(String linkText) throws CoreException {
		int index = linkText.lastIndexOf('[');
		if (index >= 0) {
			String numText = linkText.substring(index + 1);
			index = numText.indexOf(',');
			if (index >= 0) {
				numText = numText.substring(0, index);
			}
			try {
				return Integer.parseInt(numText);
			} catch (NumberFormatException e) {
				IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0,
						ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__6, e);
				throw new CoreException(status);
			}
		}
		IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0,
				ConsoleMessages.JavaStackTraceHyperlink_Unable_to_parse_line_number_from_hyperlink__6, null);
		throw new CoreException(status);
	}

	/**
	 * Returns this link's text
	 * 
	 * @exception CoreException
	 *                if unable to retrieve the text
	 */
	@Override
	protected String getLinkText() throws CoreException {
		try {
			IDocument document = getConsole().getDocument();
			IRegion region = getConsole().getRegion(this);
			int regionOffset = region.getOffset();

			int lineNumber = document.getLineOfOffset(regionOffset);
			IRegion lineInformation = document.getLineInformation(lineNumber);
			int lineOffset = lineInformation.getOffset();
			String line = document.get(lineOffset, lineInformation.getLength());

			int regionOffsetInLine = regionOffset - lineOffset;

			int linkEnd = line.indexOf(']', regionOffsetInLine);
			int linkStart = line.lastIndexOf(' ', regionOffsetInLine) > line.lastIndexOf('/', regionOffsetInLine) ? line.lastIndexOf(
					' ', regionOffsetInLine)
					: line.lastIndexOf('/', regionOffsetInLine);

			return line.substring(linkStart == -1 ? 0 : linkStart + 1, linkEnd + 1);
		} catch (BadLocationException e) {
			IStatus status = new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), 0,
					ConsoleMessages.JavaStackTraceHyperlink_Unable_to_retrieve_hyperlink_text__8, e);
			throw new CoreException(status);
		}
	}
}
