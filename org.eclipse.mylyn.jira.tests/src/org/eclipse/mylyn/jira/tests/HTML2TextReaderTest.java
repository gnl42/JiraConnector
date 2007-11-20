/*******************************************************************************
 * Copyright (c) 2005, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.mylyn.jira.tests;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.mylyn.internal.jira.ui.html.HTML2TextReader;

/**
 * @author Steffen Pingel
 * @author George Lindholm
 */
public class HTML2TextReaderTest extends TestCase {

	public void testReadOnlyWhitespacesWithSkip() throws Exception {
		String text = " ";
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null) {
			@Override
			public int read(char[] cbuf, int off, int len) throws IOException {
				setSkipWhitespace(true);
				return super.read(cbuf, off, len);
			}
		};
		char[] chars = new char[text.length()];
		int len = html2TextReader.read(chars, 0, text.length());
		assertEquals(-1, len);
	}

	public void testReadOnlyWhitespaces() throws Exception {
		String text = " ";
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null);
		char[] chars = new char[text.length()];
		int len = html2TextReader.read(chars, 0, text.length());
		assertEquals(1, len);
		assertEquals(" ", new String(chars, 0, len));

		text = "&nbsp;";
		stringReader = new StringReader(text);
		html2TextReader = new HTML2TextReader(stringReader, null);
		chars = new char[text.length()];
		len = html2TextReader.read(chars, 0, text.length());
		assertEquals(1, len);
		assertEquals(" ", new String(chars, 0, len));
	}

	public void testHeadCpuLoop() throws IOException {
		assertEquals("b ", read("b <head> a "));
		assertEquals("b ", read("b <head> a </head>"));
	}

	public void testPreCpuLoop() throws IOException {
		assertEquals("b  b ", read("b <pre> b "));
		assertEquals("b  b ", read("b <pre> b </pre>"));
	}

	private String read(final String text) throws IOException {
		Reader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader, null);
		char[] chars = new char[text.length()];
		int len = html2TextReader.read(chars, 0, chars.length);
		return new String(chars, 0, len);
	}

}
