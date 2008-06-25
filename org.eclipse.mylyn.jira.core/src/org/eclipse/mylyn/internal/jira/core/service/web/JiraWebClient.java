/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.text.html.HTML.Tag;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer;
import org.eclipse.mylyn.commons.net.HtmlTag;
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer.Token;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.WebServerInfo;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.JiraException;
import org.eclipse.mylyn.internal.jira.core.service.JiraRemoteException;
import org.eclipse.mylyn.internal.jira.core.service.JiraRemoteMessageException;

/**
 * TODO look at creation Operation classes to perform each of these actions TODO extract field names into constants
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class JiraWebClient {

	public static final String DATE_FORMAT = "dd-MMM-yyyy"; //$NON-NLS-1$

	public static final String DUE_DATE_FORMAT = "dd/MMM/yy"; //$NON-NLS-1$

	private final JiraClient client;

	public JiraWebClient(JiraClient client) {
		this.client = client;
	}

	public void addCommentToIssue(final JiraIssue issue, final String comment, IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/AddComment.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType());
				post.addParameter("comment", comment);
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				try {
					execute(post);
					if (!expectRedirect(post, issue)) {
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	private String getContentType() throws JiraException {
		return "application/x-www-form-urlencoded; charset=" + client.getCharacterEncoding();
	}

	// TODO refactor common parameter configuration with advanceIssueWorkflow() method
	public void updateIssue(final JiraIssue issue, final String comment, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/EditIssue.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType());
				post.addParameter("summary", issue.getSummary());
				post.addParameter("issuetype", issue.getType().getId());
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId());
				}
				if (issue.getDue() != null) {
					post.addParameter("duedate",
							new SimpleDateFormat(DUE_DATE_FORMAT, Locale.US).format(issue.getDue()));
				} else {
					post.addParameter("duedate", "");
				}
				post.addParameter("timetracking", Long.toString(issue.getEstimate() / 60) + "m");

				Component[] components = issue.getComponents();
				if (components != null) {
					if (components.length == 0) {
						post.addParameter("components", "-1");
					} else {
						for (Component component : components) {
							post.addParameter("components", component.getId());
						}
					}
				}

				Version[] versions = issue.getReportedVersions();
				if (versions != null) {
					if (versions.length == 0) {
						post.addParameter("versions", "-1");
					} else {
						for (Version version : versions) {
							post.addParameter("versions", version.getId());
						}
					}
				}

				Version[] fixVersions = issue.getFixVersions();
				if (fixVersions != null) {
					if (fixVersions.length == 0) {
						post.addParameter("fixVersions", "-1");
					} else {
						for (Version fixVersion : fixVersions) {
							post.addParameter("fixVersions", fixVersion.getId());
						}
					}
				}

				// TODO need to be able to choose unassigned and automatic
				if (issue.getAssignee() != null) {
					post.addParameter("assignee", issue.getAssignee());
				} else {
					post.addParameter("assignee", "-1");
				}
				if (issue.getReporter() != null) {
					post.addParameter("reporter", issue.getReporter());
				}
				post.addParameter("environment", issue.getEnvironment());
				post.addParameter("description", issue.getDescription());

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				if (issue.getSecurityLevel() != null) {
					post.addParameter("security", issue.getSecurityLevel().getId());
				}

				addCustomFields(issue, post);

				try {
					execute(post);
					if (!expectRedirect(post, issue)) {
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	public void assignIssueTo(final JiraIssue issue, final int assigneeType, final String user, final String comment,
			IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/AssignIssue.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType());

				post.addParameter("assignee", getAssigneeParam(server, issue, assigneeType, user));

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				try {
					execute(post);
					if (!expectRedirect(post, issue)) {
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	public void advanceIssueWorkflow(final JiraIssue issue, final String actionKey, final String comment,
			final String[] fields, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				PostMethod post = new PostMethod(baseUrl + "/secure/CommentAssignIssue.jspa");
				post.setRequestHeader("Content-Type", getContentType());

				post.addParameter("id", issue.getId());
				post.addParameter("action", actionKey);
				// method.addParameter("assignee", issue.getAssignee());

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");

				for (String field : fields) {
					String[] values = issue.getFieldValues(field);
					if (values == null) {
						// method.addParameter(field, "");
					} else {
						for (String value : values) {
							post.addParameter(field, value);
						}
					}
				}

				try {
					execute(post);
					if (!expectRedirect(post, issue)) {
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}
		});
	}

	public void attachFile(final JiraIssue issue, final String comment, final PartSource partSource,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		attachFile(issue, comment, new FilePart("filename.1", partSource), contentType, monitor);
	}

	public void attachFile(final JiraIssue issue, final String comment, final String filename, final byte[] contents,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		attachFile(issue, comment, new FilePart("filename.1", new ByteArrayPartSource(filename, contents)),
				contentType, monitor);
	}

	public void attachFile(final JiraIssue issue, final String comment, final String filename, final File file,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		try {
			FilePartSource partSource = new FilePartSource(filename, file);
			attachFile(issue, comment, new FilePart("filename.1", partSource), contentType, monitor);
		} catch (FileNotFoundException e) {
			throw new JiraException(e);
		}
	}

	public void attachFile(final JiraIssue issue, final String comment, final FilePart filePart,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder attachFileURLBuffer = new StringBuilder(baseUrl);
				attachFileURLBuffer.append("/secure/AttachFile.jspa");

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());

				List<PartBase> parts = new ArrayList<PartBase>();

				StringPart idPart = new StringPart("id", issue.getId());
				StringPart commentLevelPart = new StringPart("commentLevel", "");

				// The transfer encodings have to be removed for some reason
				// There is no need to send the content types for the strings,
				// as they should be in the correct format
				idPart.setTransferEncoding(null);
				idPart.setContentType(null);

				if (comment != null) {
					StringPart commentPart = new StringPart("comment", comment);
					commentPart.setTransferEncoding(null);
					commentPart.setContentType(null);
					parts.add(commentPart);
				}

				commentLevelPart.setTransferEncoding(null);
				commentLevelPart.setContentType(null);

				filePart.setTransferEncoding(null);
				if (contentType != null) {
					filePart.setContentType(contentType);
				}
				parts.add(filePart);
				parts.add(idPart);
				parts.add(commentLevelPart);

				post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]),
						post.getParams()));

				try {
					execute(post);
					if (!expectRedirect(post, "/secure/ManageAttachments.jspa?id=" + issue.getId())) {
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}
		});
	}

	public void retrieveFile(final JiraIssue issue, final Attachment attachment, final byte[] attachmentData,
			IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/attachment/");
				rssUrlBuffer.append(attachment.getId());
				rssUrlBuffer.append("/");
				try {
					rssUrlBuffer.append(URLEncoder.encode(attachment.getName(), server.getCharacterEncoding()));
				} catch (UnsupportedEncodingException e) {
					throw new JiraException(e);
				}

				GetMethod get = new GetMethod(rssUrlBuffer.toString());
				try {
					int result = execute(get);
					if (result != HttpStatus.SC_OK) {
						handleErrorMessage(get);
					} else {
						byte[] data = get.getResponseBody();
						if (data.length != attachmentData.length) {
							throw new IOException("Unexpected attachment size (got " + data.length + ", expected "
									+ attachmentData.length + ")");
						}
						System.arraycopy(data, 0, attachmentData, 0, attachmentData.length);
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					get.releaseConnection();
				}
			}

		});
	}

	public void retrieveFile(final JiraIssue issue, final Attachment attachment, final OutputStream out,
			IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {

			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/attachment/");
				rssUrlBuffer.append(attachment.getId());
				rssUrlBuffer.append("/");
				try {
					rssUrlBuffer.append(URLEncoder.encode(attachment.getName(), server.getCharacterEncoding()));
				} catch (UnsupportedEncodingException e) {
					throw new JiraException(e);
				}

				GetMethod get = new GetMethod(rssUrlBuffer.toString());
				try {
					int result = execute(get);
					if (result != HttpStatus.SC_OK) {
						handleErrorMessage(get);
					} else {
						out.write(get.getResponseBody());
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					get.releaseConnection();
				}
			}

		});
	}

	public String createIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		return createIssue("/secure/CreateIssueDetails.jspa", issue, monitor);
	}

	public String createSubTask(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		return createIssue("/secure/CreateSubTaskIssueDetails.jspa", issue, monitor);
	}

	// TODO refactor common parameter configuration with advanceIssueWorkflow() method
	private String createIssue(final String url, final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		final String[] issueKey = new String[1];
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder attachFileURLBuffer = new StringBuilder(baseUrl);
				attachFileURLBuffer.append(url);

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType());

				post.addParameter("pid", issue.getProject().getId());
				post.addParameter("issuetype", issue.getType().getId());
				post.addParameter("summary", issue.getSummary());
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId());
				}
				if (issue.getDue() != null) {
					post.addParameter("duedate",
							new SimpleDateFormat(DUE_DATE_FORMAT, Locale.US).format(issue.getDue()));
				}
				post.addParameter("timetracking", Long.toString(issue.getEstimate() / 60) + "m");

				if (issue.getComponents() != null) {
					for (int i = 0; i < issue.getComponents().length; i++) {
						post.addParameter("components", issue.getComponents()[i].getId());
					}
				} else {
					post.addParameter("components", "-1");
				}

				if (issue.getReportedVersions() != null) {
					for (int i = 0; i < issue.getReportedVersions().length; i++) {
						post.addParameter("versions", issue.getReportedVersions()[i].getId());
					}
				} else {
					post.addParameter("versions", "-1");
				}

				if (issue.getFixVersions() != null) {
					for (int i = 0; i < issue.getFixVersions().length; i++) {
						post.addParameter("fixVersions", issue.getFixVersions()[i].getId());
					}
				} else {
					post.addParameter("fixVersions", "-1");
				}

				if (issue.getAssignee() == null) {
					post.addParameter("assignee", "-1"); // Default assignee
				} else if (issue.getAssignee().length() == 0) {
					post.addParameter("assignee", ""); // nobody
				} else {
					post.addParameter("assignee", issue.getAssignee());
				}

				post.addParameter("reporter", server.getUserName());

				post.addParameter("environment", issue.getEnvironment() != null ? issue.getEnvironment() : "");
				post.addParameter("description", issue.getDescription() != null ? issue.getDescription() : "");

				if (issue.getParentId() != null) {
					post.addParameter("parentIssueId", issue.getParentId());
				}

				addCustomFields(issue, post);

				try {
					execute(post);
					if (!expectRedirect(post, "/browse/")) {
						handleErrorMessage(post);
					} else {
						final Header locationHeader = post.getResponseHeader("location");
						// parse issue key from issue URL 
						String location = locationHeader.getValue();
						int i = location.lastIndexOf("/");
						if (i != -1) {
							issueKey[0] = location.substring(i + 1);
						} else {
							throw new JiraException(
									"The server redirected to an unexpected location while creating an issue: "
											+ location);
						}
					}
				} finally {
					post.releaseConnection();
				}
			}
		});
		assert issueKey[0] != null;
		return issueKey[0];
	}

	public void watchIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		watchUnwatchIssue(issue, true, monitor);
	}

	public void unwatchIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		watchUnwatchIssue(issue, false, monitor);
	}

	private void watchUnwatchIssue(final JiraIssue issue, final boolean watch, IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/browse/").append(issue.getKey());
				urlBuffer.append("?watch=").append(Boolean.toString(watch));

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Changing watch status failed. Return code: " + result);
					}
				} finally {
					head.releaseConnection();
				}
			}

		});
	}

	public void voteIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		voteUnvoteIssue(issue, true, monitor);
	}

	public void unvoteIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		voteUnvoteIssue(issue, false, monitor);
	}

	private void voteUnvoteIssue(final JiraIssue issue, final boolean vote, IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {

			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/browse/").append(issue.getKey());
				urlBuffer.append("?vote=").append(vote ? "vote" : "unvote");

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Changing vote failed. Return code: " + result);
					}
				} finally {
					head.releaseConnection();
				}
			}

		});
	}

	public void deleteIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {

			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/secure/DeleteIssue.jspa");
				urlBuffer.append("?id=").append(issue.getId());
				urlBuffer.append("&confirm=true");

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Deleting issue failed. Return code: " + result);
					}
				} finally {
					head.releaseConnection();
				}
			}

		});
	}

	public WebServerInfo getWebServerInfo(IProgressMonitor monitor) throws JiraException {
		final WebServerInfo webServerInfo = new WebServerInfo();
		final JiraWebSession s = new JiraWebSession(client);
		s.setLogEnabled(true);
		s.doInSession(new JiraWebSessionCallback() {
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				webServerInfo.setBaseUrl(s.getBaseURL());
				webServerInfo.setCharacterEncoding(s.getCharacterEncoding());
				webServerInfo.setInsecureRedirect(s.isInsecureRedirect());
			}
		}, monitor);
		return webServerInfo;
	}

	private String getAssigneeParam(JiraClient server, JiraIssue issue, int assigneeType, String user) {
		switch (assigneeType) {
		case JiraClient.ASSIGNEE_CURRENT:
			return issue.getAssignee();
		case JiraClient.ASSIGNEE_DEFAULT:
			return "-1"; //$NON-NLS-1$
		case JiraClient.ASSIGNEE_NONE:
			return ""; //$NON-NLS-1$
		case JiraClient.ASSIGNEE_SELF:
			return server.getUserName();
		case JiraClient.ASSIGNEE_USER:
			return user;
		default:
			return user;
		}
	}

	protected void handleErrorMessage(HttpMethodBase method) throws JiraException {
		try {
			String response = method.getResponseBodyAsString();
			// TODO consider logging the error

			if (method.getStatusCode() == HttpStatus.SC_SERVICE_UNAVAILABLE) {
				throw new JiraRemoteException("JIRA system error", null);
			}

			if (response == null) {
				throw new JiraRemoteMessageException("Error making JIRA request: " + method.getStatusCode(), "");
			}

			StringReader reader = new StringReader(response);
			try {
				StringBuilder msg = new StringBuilder();
				HtmlStreamTokenizer tokenizer = new HtmlStreamTokenizer(reader, null);
				for (Token token = tokenizer.nextToken(); token.getType() != Token.EOF; token = tokenizer.nextToken()) {
					if (token.getType() == Token.TAG) {
						HtmlTag tag = (HtmlTag) token.getValue();

						String classValue = tag.getAttribute("class");
						if (classValue != null) {
							if (tag.getTagType() == HtmlTag.Type.DIV) {
								if (classValue.startsWith("infoBox")) {
									throw new JiraRemoteMessageException(getContent(tokenizer, HtmlTag.Type.DIV));
								} else if (classValue.startsWith("errorArea")) {
									throw new JiraRemoteMessageException(getContent(tokenizer, HtmlTag.Type.DIV));
								}
							} else if (tag.getTagType() == HtmlTag.Type.SPAN) {
								if (classValue.startsWith("errMsg")) {
									msg.append(getContent(tokenizer, HtmlTag.Type.SPAN));
								}
							}
						}
					}
				}
				if (msg.length() == 0) {
					throw new JiraRemoteMessageException(response);
				} else {
					throw new JiraRemoteMessageException(msg.toString());
				}
			} catch (ParseException e) {
				throw new JiraRemoteMessageException("Error parsing JIRA response: " + method.getStatusCode(), "");
			} finally {
				reader.close();
			}
		} catch (IOException e) {
			throw new JiraException(e);
		}
	}

	private String getContent(HtmlStreamTokenizer tokenizer, Tag closingTag) throws IOException, ParseException {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (Token token = tokenizer.nextToken(); token.getType() != Token.EOF; token = tokenizer.nextToken()) {
			if (token.getType() == Token.TAG) {
				HtmlTag tag = (HtmlTag) token.getValue();
				if (tag.getTagType() == closingTag) {
					if (tag.isEndTag()) {
						if (count == 0) {
							break;
						} else {
							count--;
						}
					} else {
						count++;
					}
				}
			}

			sb.append(token.toString());
		}
		return sb.toString();
	}

	private void addCustomFields(final JiraIssue issue, PostMethod post) {
		for (CustomField customField : issue.getCustomFields()) {
			for (String value : customField.getValues()) {
				String key = customField.getKey();
				if (key == null || //
						(!key.startsWith("com.atlassian.jira.toolkit") && //
						!key.startsWith("com.atlassian.jira.ext.charting"))) {
					post.addParameter(customField.getId(), value == null ? "" : value);
				}
			}
		}
	}

	private void doInSession(IProgressMonitor monitor, JiraWebSessionCallback callback) throws JiraException {
		JiraWebSession session = new JiraWebSession(client);
		session.doInSession(callback, monitor);
	}

}
