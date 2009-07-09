/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Apr 10, 2007
 * Time: 1:21:16 PM
 */
package org.veryquick.embweb;

import java.util.Map;

/**
 * Request handler for the ultra light-weight server - implmement to handle requests
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$
 *         Last Modified: $Date:Apr 10, 2007$
 *         $Id: blah$
 */
public interface HttpRequestHandler {

	/**
	 * Type "post"
	 */
	Type POST = new Type();

	/**
	 * Type "get"
	 */
	Type GET = new Type();

	/**
	 * Handle a request
	 *
	 * @param type	   the type of request
	 * @param url		the url the user requested (relative to the host base, e.g. "/index.html")
	 * @param parameters a map of URL parameters where the key is the name and the value is the value
	 * @return a response response
	 */
	Response handleRequest(HttpRequestHandler.Type type, String url, Map<String, String> parameters);

	public static class Type {

	}
}
