package org.eclipse.mylar.jira.core.internal.model.filter;

import org.eclipse.mylar.jira.core.internal.model.Query;

/**
 * Query object that holds the query value that will be passed to the server.
 * TODO Possibly allow the user to construct this object without having to know
 * the syntax
 * 
 * @author Brock Janiczak
 */
public class SmartQuery implements Query {

	private String keywords;

	public SmartQuery(String keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the keywords
	 */
	public String getKeywords() {
		return this.keywords;
	}
}
