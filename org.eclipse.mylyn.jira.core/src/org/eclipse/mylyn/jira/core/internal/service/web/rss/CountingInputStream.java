/*******************************************************************************
 * Copyright (c) 2005 Jira Dashboard project.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylar.jira.core.internal.service.web.rss;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapping input stream that records how much data has been read from the
 * wrapped stream.
 */
public class CountingInputStream extends FilterInputStream {

	private long totalBytesRead = 0;

	private long markedBytesRead = 0;

	/**
	 * Creates a new input stream that will count the number of bytes read from
	 * <code>stream</code>
	 * 
	 * @param stream
	 */
	public CountingInputStream(InputStream stream) {
		super(stream);
	}

	/**
	 * Returns the total number of bytes read from this stream so far
	 * 
	 * @return Number of bytes read from this stream
	 */
	public long getTotalByesRead() {
		return totalBytesRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#read()
	 */
	public int read() throws IOException {
		int value = super.read();
		if (value != -1) {
			totalBytesRead++;
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {
		int bytesRead = super.read(b);
		if (bytesRead != -1) {
			totalBytesRead += bytesRead;
		}
		return bytesRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		int bytesRead = super.read(b, off, len);
		if (bytesRead != -1) {
			totalBytesRead += bytesRead;
		}
		return bytesRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#skip(long)
	 */
	public long skip(long n) throws IOException {
		long bytesRead = super.skip(n);
		totalBytesRead += bytesRead;
		return bytesRead;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#mark(int)
	 */
	public synchronized void mark(int readlimit) {
		markedBytesRead = totalBytesRead;
		super.mark(readlimit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.io.FilterInputStream#reset()
	 */
	public synchronized void reset() throws IOException {
		totalBytesRead = markedBytesRead;
		super.reset();
	}
}
