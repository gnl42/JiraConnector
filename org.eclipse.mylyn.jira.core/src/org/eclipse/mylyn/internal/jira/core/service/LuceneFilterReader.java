/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service;

import java.io.StringReader;

import org.eclipse.mylyn.internal.jira.core.model.filter.FilterDefinition;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author	Brock Janiczak
 */
public class LuceneFilterReader {

	public FilterDefinition convert(String filter) {
		try {
			XMLReader reader = XMLReaderFactory.createXMLReader();
			LuceneFilterContentHandler handler = new LuceneFilterContentHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(new StringReader(filter)));

			return handler.getFilterDefinition();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
