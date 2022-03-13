/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atlassian.theplugin.commons.util;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Jacek Jaroczynski
 */
public final class XmlUtil {
	private XmlUtil() {
	}

	public static void printXml(Document request) {
		XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
		try {
			o.output(request, System.out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String toPrettyFormatedString(Element element) {
		XMLOutputter o = new XMLOutputter(Format.getPrettyFormat());
		StringWriter writer = new StringWriter();
		try {
			o.output(element, writer);
		} catch (IOException e) {
			return "[" + element.getName() + "] - string representation failed: " + e.getMessage();
		}
		return writer.toString();
	}

	@SuppressWarnings("unchecked")
	public static List<Element> getChildElements(Element node, String childName) {
		return node.getChildren(childName);
	}

}
