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

package com.atlassian.theplugin.crucible.api.rest.cruciblemock;

import static com.atlassian.theplugin.commons.crucible.api.JDomHelper.getContent;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetRepositoriesCallback implements JettyMockServer.Callback {
	private int size;

	public GetRepositoriesCallback(int size) {
		this.size = size;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/repositories-v1"));

		Document doc;
		doc = getRepositories();
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getRepositories() {
		Element root = new Element("repositories");
		Document doc = new Document(root);
		for (int i = 0; i < size; i++) {
            root.addContent(getRepositories(i));
		}
		return doc;
	}

	private Element getRepositories(int i) {
		Element projectData = new Element("repoData");
		projectData.addContent(new Element("name").setText("RepoName" + Integer.toString(i)));
		return projectData;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
		getContent(root).add(newElement);
	}
}