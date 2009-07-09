/**
 * Copyright 2006-2007, subject to LGPL version 3
 * User: garethc
 * Date: Apr 10, 2007
 * Time: 4:15:37 PM
 */
package org.veryquick.embweb.handlers;

import org.veryquick.embweb.HttpRequestHandler;
import org.veryquick.embweb.Response;

import com.atlassian.theplugin.commons.util.Logger;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import java.io.*;
import java.util.Map;

/**
 * Handler that servers up files from a given filebase. Uses standard mime.types file from a Linux distribution
 * for non text files. Note that it buffers each request into memory before serving, so it would not be suitable
 * for high-performance, large-file serving
 * <p/>
 * Copyright 2006-2007, subject to LGPL version 3
 *
 * @author $Author:garethc$
 *         Last Modified: $Date:Apr 10, 2007$
 *         $Id: blah$
 */
public class FileBasedRequestHandler implements HttpRequestHandler {

	/**
	 * Logger
	 */
	public static final Logger LOGGER = LoggerImpl.getInstance();

	/**
	 * Base from which to server files
	 */
	private File base;

	/**
	 * Create a new handler that serves files from a base directory
	 *
	 * @param base directory
	 */
	public FileBasedRequestHandler(File base) {
		if (!base.isDirectory()) {
			throw new IllegalArgumentException("base must be a directory: " + base);
		}
		this.base = base;
	}

	/**
	 * Handle a request
	 *
	 * @param type
	 * @param url
	 * @param parameters
	 * @return a response
	 */
	public Response handleRequest(Type type, String url, Map<String, String> parameters) {
		Response response = new Response();
		File fileToRead = new File(base, url);

		if (!fileToRead.exists()) {
			response.setNotFound(url);
			return response;
		}

		// determine mime type
		String mimeType = null;
		int indexOfDot = fileToRead.getName().indexOf('.');
		if (indexOfDot >= 0) {
			String extension = fileToRead.getName().substring(indexOfDot);
			mimeType = MimeTypeParser.getInstance().getType(extension);
		}
		if (mimeType != null) {
			response.setContentType(mimeType);
		}


		FileInputStream in = null;
		StringWriter writer;
		try {
			in = new FileInputStream(fileToRead);
			int nextByte;
			if (mimeType == null || mimeType.startsWith("text")) {
				writer = new StringWriter();
				while ((nextByte = in.read()) >= 0) {
					writer.write(nextByte);
				}
				writer.close();
				response.addContent(writer.toString());
			} else {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				while ((nextByte = in.read()) >= 0) {
					out.write(nextByte);
				}
				out.close();
				response.setBinaryContent(out.toByteArray());
			}
		}
		catch (Exception e) {
			LOGGER.error("error reading file", e);
			response.setError(e);
			return response;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOGGER.warn("failed to close stream", e);
				}
			}
		}

		response.setOk();
		return response;
	}
}
