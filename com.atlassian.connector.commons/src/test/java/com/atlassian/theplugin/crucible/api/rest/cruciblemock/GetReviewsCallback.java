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
import com.atlassian.theplugin.commons.crucible.api.model.State;
import static junit.framework.Assert.assertTrue;
import org.ddsteps.mock.httpserver.JettyMockServer;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

public class GetReviewsCallback implements JettyMockServer.Callback {
	List<State> states;

	public GetReviewsCallback(List<State> states) {
		this.states = states;
	}

	public void onExpectedRequest(String target,
								  HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		assertTrue(request.getPathInfo().endsWith("/rest-service/reviews-v1/details"));

		Document doc;
		final String[] statesParam = request.getParameterValues("state");
		if (statesParam != null) {
			assertTrue(1 == statesParam.length);
			String[] stateStrings = statesParam[0].split(",");
			List<State> returnStates = new ArrayList<State>();
			for (String stateString : stateStrings) {
				State s = State.fromValue(stateString);
				if (states.contains(s)) {
					returnStates.add(s);
				}
			}
			doc = getReviews(returnStates);
		} else {
			doc = getReviews(states);
		}
		XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
		outputter.output(doc, response.getOutputStream());
	}

	private Document getReviews(List<State> states) {
		Element root = new Element("detailedReviews");
		Document doc = new Document(root);
		for (State state : states) {
			getContent(root).add(getReviewInState(state));
		}
		return doc;
	}

	private Element getReviewInState(State state) {
		Element reviewData = new Element("detailedReviewData");

		addTag(reviewData, "author", "author");
		addTag(reviewData, "creator", "creator");
		addTag(reviewData, "description", "description");
		addTag(reviewData, "moderator", "moderator");
		addTag(reviewData, "name", "name");
		addTag(reviewData, "projectKey", "CR0");
		addTag(reviewData, "repoName", "RepoName");
		addTag(reviewData, "state", state.value());
		addTag(reviewData, "metricsVersion", String.valueOf(1));

		Element newPermaId = new Element("permaId");
		Element newId = new Element("id");
		newId.addContent("CR0-1");
		getContent(newPermaId).add(newId);
		getContent(reviewData).add(newPermaId);

		return reviewData;
	}

	void addTag(Element root, String tagName, String tagValue) {
		Element newElement = new Element(tagName);
		newElement.addContent(tagValue);
        getContent(root).add(newElement);
	}
}