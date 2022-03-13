package com.atlassian.theplugin.commons.crucible.api;

import org.apache.commons.httpclient.methods.multipart.FilePart;

// @todo ideally byte arrays should be replaced by input streams
public class UploadItem {
	public static final String DEFAULT_CONTENT_TYPE = FilePart.DEFAULT_CONTENT_TYPE;
	public static final String DEFAULT_CHARSET = FilePart.DEFAULT_CHARSET;

	private final String fileName;
	private final byte[] oldContent;
	private final byte[] newContent;
	private final String oldType;
	private final String newType;
	private final String oldCharset;
	private final String newCharset;

	public UploadItem(final String fileName, final byte[] oldContent, final byte[] newContent) {
		this(fileName, DEFAULT_CONTENT_TYPE, DEFAULT_CHARSET, oldContent,
				DEFAULT_CONTENT_TYPE, DEFAULT_CHARSET, newContent);
	}

	/**
	 *
	 * @param fileName
	 * @param oldContent
	 * @param newContent
	 */
	public UploadItem(final String fileName, final String oldType, final String oldCharset, final byte[] oldContent,
			final String newType, final String newCharset,
			final byte[] newContent) {
		this.fileName = fileName;
		this.oldType = oldType;
		this.oldContent = oldContent;
		this.oldCharset = oldCharset;
		this.newType = newType;
		this.newContent = newContent;
		this.newCharset = newCharset;
	}

	public String getFileName() {
		return fileName;
	}

	public byte[] getOldContent() {
		return oldContent;
	}

	public byte[] getNewContent() {
		return newContent;
	}

	public String getOldContentType() {
		return oldType;
	}

	public String getNewContentType() {
		return newType;
	}

	public String getOldCharset() {
		return oldCharset;
	}

	public String getNewCharset() {
		return newCharset;
	}
}
