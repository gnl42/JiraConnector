/**
 * Copyright 2007, Gareth Cronin (NZ) Ltd
 * User: garethc
 * Date: Jun 19, 2007
 * Time: 1:31:50 PM
 */
package org.veryquick.embweb.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

/**
 * Parser for MIME-types from a standard MIME type file
 * <p/>
 * Copyright 2007, Gareth Cronin (NZ) Ltd.
 *
 * @author $Author:garethc$
 *         Last Modified: $Date:Jun 19, 2007$
 *         $Id: blah$
 */
public final class MimeTypeParser {

	/**
	 * Logger
	 */
	public static final Logger LOGGER = LoggerImpl.getInstance();

	/**
	 * Singleton instance
	 */
	private static MimeTypeParser instance;

	/**
	 * Map of file extension to type
	 */
	private Map<String, String> typeMap;

	/**
	 * Hidden constructor
	 */
	private MimeTypeParser() {
		this.typeMap = new HashMap<String, String>();
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream("/org/veryquick/embweb/handlers/mime.types");
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line = null;
			while ((line = reader.readLine()) != null) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				if (tokenizer.hasMoreTokens()) {
					String mimeType = tokenizer.nextToken();
					while (tokenizer.hasMoreTokens()) {
						typeMap.put(tokenizer.nextToken(), mimeType);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("failed to parse the file", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.warn("failed to close", e);
				}
			}
		}

	}

	/**
	 * Get singleton instance
	 *
	 * @return instance
	 */
	public static synchronized MimeTypeParser getInstance() {
		if (instance == null) {
			instance = new MimeTypeParser();
		}
		return instance;
	}

	/**
	 * Get the type for the given file extension
	 *
	 * @param fileExtension extension (e.g. "txt" or "html")
	 * @return type (e.g. "text/plain" or "text/html")
	 */
	public String getType(String fileExtension) {
		// trim leading dot if given
		if (fileExtension.startsWith(".")) {
			fileExtension = fileExtension.substring(1);
		}
		return this.typeMap.get(fileExtension);
	}
}
