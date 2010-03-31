/*******************************************************************************
 * Copyright (c) 2004, 2009 Brock Janiczak and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brock Janiczak - initial API and implementation
 *     Tasktop Technologies - improvements
 *     Eugene Kuleshov - improvements
 *******************************************************************************/

package com.atlassian.connector.eclipse.internal.jira.core.service.web;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import org.eclipse.mylyn.commons.net.HtmlStreamTokenizer.Token;
import org.eclipse.mylyn.commons.net.HtmlTag;

import com.atlassian.connector.eclipse.internal.jira.core.JiraFieldType;
import com.atlassian.connector.eclipse.internal.jira.core.model.Attachment;
import com.atlassian.connector.eclipse.internal.jira.core.model.Component;
import com.atlassian.connector.eclipse.internal.jira.core.model.CustomField;
import com.atlassian.connector.eclipse.internal.jira.core.model.JiraIssue;
import com.atlassian.connector.eclipse.internal.jira.core.model.Version;
import com.atlassian.connector.eclipse.internal.jira.core.model.WebServerInfo;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraClient;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteException;
import com.atlassian.connector.eclipse.internal.jira.core.service.JiraRemoteMessageException;
import com.atlassian.connector.eclipse.internal.jira.core.service.web.rss.JiraRssHandler;

/**
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
// TODO look at creation Operation classes to perform each of these actions
// TODO extract field names into constants
public class JiraWebClient {

	private final JiraClient client;

	private final JiraWebSession session;

	public JiraWebClient(JiraClient client, JiraWebSession session) {
		this.client = client;
		this.session = session;
	}

	public void addCommentToIssue(final JiraIssue issue, final String comment, IProgressMonitor monitor)
			throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/AddComment.jspa"); //$NON-NLS-1$

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType(monitor)); //$NON-NLS-1$
				prepareSecurityToken(post);
				post.addParameter("comment", comment); //$NON-NLS-1$
				post.addParameter("commentLevel", ""); //$NON-NLS-1$ //$NON-NLS-2$
				post.addParameter("id", issue.getId()); //$NON-NLS-1$

				try {
					execute(post);
					if (!expectRedirect(post, "/browse/" + issue.getKey(), false)) { //$NON-NLS-1$
						handleErrorMessage(post);
					}
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	private String getContentType(IProgressMonitor monitor) throws JiraException {
		return "application/x-www-form-urlencoded; charset=" + client.getCharacterEncoding(monitor); //$NON-NLS-1$
	}

	// TODO refactor common parameter configuration with advanceIssueWorkflow() method
	public void updateIssue(final JiraIssue issue, final String comment, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder rssUrlBuffer = new StringBuilder(baseUrl);
				rssUrlBuffer.append("/secure/EditIssue.jspa"); //$NON-NLS-1$

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType(monitor)); //$NON-NLS-1$
				prepareSecurityToken(post);
				post.addParameter("summary", issue.getSummary()); //$NON-NLS-1$
				post.addParameter("issuetype", issue.getType().getId()); //$NON-NLS-1$
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId()); //$NON-NLS-1$
				}
				addFields(client, issue, post, new String[] { "duedate" }); //$NON-NLS-1$
				post.addParameter("timetracking", Long.toString(issue.getEstimate() / 60) + "m"); //$NON-NLS-1$ //$NON-NLS-2$

				Component[] components = issue.getComponents();
				if (components != null) {
					if (components.length == 0) {
						post.addParameter("components", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						for (Component component : components) {
							post.addParameter("components", component.getId()); //$NON-NLS-1$
						}
					}
				}

				Version[] versions = issue.getReportedVersions();
				if (versions != null) {
					if (versions.length == 0) {
						post.addParameter("versions", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						for (Version version : versions) {
							post.addParameter("versions", version.getId()); //$NON-NLS-1$
						}
					}
				}

				Version[] fixVersions = issue.getFixVersions();
				if (fixVersions != null) {
					if (fixVersions.length == 0) {
						post.addParameter("fixVersions", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
					} else {
						for (Version fixVersion : fixVersions) {
							post.addParameter("fixVersions", fixVersion.getId()); //$NON-NLS-1$
						}
					}
				}

				// TODO need to be able to choose unassigned and automatic
				if (issue.getAssignee() != null) {
					post.addParameter("assignee", issue.getAssignee()); //$NON-NLS-1$
				} else {
					post.addParameter("assignee", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				if (issue.getReporter() != null) {
					post.addParameter("reporter", issue.getReporter()); //$NON-NLS-1$
				}
				if (issue.getEnvironment() != null) {
					post.addParameter("environment", issue.getEnvironment()); //$NON-NLS-1$
				}
				post.addParameter("description", issue.getDescription()); //$NON-NLS-1$

				if (comment != null) {
					post.addParameter("comment", comment); //$NON-NLS-1$
				}
				post.addParameter("commentLevel", ""); //$NON-NLS-1$ //$NON-NLS-2$
				post.addParameter("id", issue.getId()); //$NON-NLS-1$

				if (issue.getSecurityLevel() != null) {
					post.addParameter("security", issue.getSecurityLevel().getId()); //$NON-NLS-1$
				}

				addCustomFields(client, issue, post);

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
				rssUrlBuffer.append("/secure/AssignIssue.jspa"); //$NON-NLS-1$

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType(monitor)); //$NON-NLS-1$
				prepareSecurityToken(post);

				post.addParameter("assignee", getAssigneeParam(server, issue, assigneeType, user)); //$NON-NLS-1$

				if (comment != null) {
					post.addParameter("comment", comment); //$NON-NLS-1$
				}
				post.addParameter("commentLevel", ""); //$NON-NLS-1$ //$NON-NLS-2$
				post.addParameter("id", issue.getId()); //$NON-NLS-1$

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
				PostMethod post = new PostMethod(baseUrl + "/secure/CommentAssignIssue.jspa"); //$NON-NLS-1$
				post.setRequestHeader("Content-Type", getContentType(monitor)); //$NON-NLS-1$
				prepareSecurityToken(post);

				post.addParameter("id", issue.getId()); //$NON-NLS-1$
				post.addParameter("action", actionKey); //$NON-NLS-1$
				// method.addParameter("assignee", issue.getAssignee());

				if (comment != null) {
					post.addParameter("comment", comment); //$NON-NLS-1$
				}
				post.addParameter("commentLevel", ""); //$NON-NLS-1$ //$NON-NLS-2$

				addFields(server, issue, post, fields);

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
		attachFile(issue, comment, new FilePart("filename.1", partSource), contentType, monitor); //$NON-NLS-1$
	}

	public void attachFile(final JiraIssue issue, final String comment, final String filename, final byte[] contents,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		attachFile(issue, comment, new FilePart("filename.1", new ByteArrayPartSource(filename, contents)), //$NON-NLS-1$
				contentType, monitor);
	}

	public void attachFile(final JiraIssue issue, final String comment, final String filename, final File file,
			final String contentType, IProgressMonitor monitor) throws JiraException {
		try {
			FilePartSource partSource = new FilePartSource(filename, file);
			attachFile(issue, comment, new FilePart("filename.1", partSource), contentType, monitor); //$NON-NLS-1$
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
				attachFileURLBuffer.append("/secure/AttachFile.jspa"); //$NON-NLS-1$

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());
				prepareSecurityToken(post);

				List<PartBase> parts = new ArrayList<PartBase>();

				StringPart idPart = new StringPart("id", issue.getId()); //$NON-NLS-1$
				StringPart commentLevelPart = new StringPart("commentLevel", ""); //$NON-NLS-1$ //$NON-NLS-2$

				// The transfer encodings have to be removed for some reason
				// There is no need to send the content types for the strings,
				// as they should be in the correct format
				idPart.setTransferEncoding(null);
				idPart.setContentType(null);

				if (comment != null) {
					StringPart commentPart = new StringPart("comment", comment); //$NON-NLS-1$
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
					if (!expectRedirect(post, "/secure/ManageAttachments.jspa?id=" + issue.getId())) { //$NON-NLS-1$
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
				rssUrlBuffer.append("/secure/attachment/"); //$NON-NLS-1$
				rssUrlBuffer.append(attachment.getId());
				rssUrlBuffer.append("/"); //$NON-NLS-1$
				try {
					rssUrlBuffer.append(URLEncoder.encode(attachment.getName(), server.getCharacterEncoding(monitor)));
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
							throw new IOException("Unexpected attachment size (got " + data.length + ", expected " //$NON-NLS-1$ //$NON-NLS-2$
									+ attachmentData.length + ")"); //$NON-NLS-1$
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
				rssUrlBuffer.append("/secure/attachment/"); //$NON-NLS-1$
				rssUrlBuffer.append(attachment.getId());
				rssUrlBuffer.append("/"); //$NON-NLS-1$
				try {
					rssUrlBuffer.append(URLEncoder.encode(attachment.getName(), server.getCharacterEncoding(monitor)));
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
		return createIssue("/secure/CreateIssueDetails.jspa", issue, monitor); //$NON-NLS-1$
	}

	public String createSubTask(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		return createIssue("/secure/CreateSubTaskIssueDetails.jspa", issue, monitor); //$NON-NLS-1$
	}

	private void prepareSecurityToken(PostMethod post) {
		// this one is required as of JIRA 4.1
		// see http://confluence.atlassian.com/display/JIRA/Form+Token+Handling#FormTokenHandling-Scripting
		post.setRequestHeader("X-Atlassian-Token", "no-check"); //$NON-NLS-1$//$NON-NLS-2$
	}

	// TODO refactor common parameter configuration with advanceIssueWorkflow() method
	private String createIssue(final String url, final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		final String[] issueKey = new String[1];
		doInSession(monitor, new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient client, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder attachFileURLBuffer = new StringBuilder(baseUrl);
				attachFileURLBuffer.append(url);

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());
				post.setRequestHeader("Content-Type", getContentType(monitor)); //$NON-NLS-1$
				prepareSecurityToken(post);

				post.addParameter("pid", issue.getProject().getId()); //$NON-NLS-1$
				post.addParameter("issuetype", issue.getType().getId()); //$NON-NLS-1$
				post.addParameter("summary", issue.getSummary()); //$NON-NLS-1$
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId()); //$NON-NLS-1$
				}
				addFields(client, issue, post, new String[] { "duedate" }); //$NON-NLS-1$
				post.addParameter("timetracking", Long.toString(issue.getEstimate() / 60) + "m"); //$NON-NLS-1$ //$NON-NLS-2$

				if (issue.getComponents() != null) {
					for (int i = 0; i < issue.getComponents().length; i++) {
						post.addParameter("components", issue.getComponents()[i].getId()); //$NON-NLS-1$
					}
				} else {
					post.addParameter("components", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				if (issue.getReportedVersions() != null) {
					for (int i = 0; i < issue.getReportedVersions().length; i++) {
						post.addParameter("versions", issue.getReportedVersions()[i].getId()); //$NON-NLS-1$
					}
				} else {
					post.addParameter("versions", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				if (issue.getFixVersions() != null) {
					for (int i = 0; i < issue.getFixVersions().length; i++) {
						post.addParameter("fixVersions", issue.getFixVersions()[i].getId()); //$NON-NLS-1$
					}
				} else {
					post.addParameter("fixVersions", "-1"); //$NON-NLS-1$ //$NON-NLS-2$
				}

				if (issue.getAssignee() == null) {
					post.addParameter("assignee", "-1"); // Default assignee //$NON-NLS-1$ //$NON-NLS-2$
				} else if (issue.getAssignee().length() == 0) {
					post.addParameter("assignee", ""); // nobody //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					post.addParameter("assignee", issue.getAssignee()); //$NON-NLS-1$
				}

				post.addParameter("reporter", client.getUserName()); //$NON-NLS-1$

				post.addParameter("environment", issue.getEnvironment() != null ? issue.getEnvironment() : ""); //$NON-NLS-1$ //$NON-NLS-2$
				post.addParameter("description", issue.getDescription() != null ? issue.getDescription() : ""); //$NON-NLS-1$ //$NON-NLS-2$

				if (issue.getParentId() != null) {
					post.addParameter("parentIssueId", issue.getParentId()); //$NON-NLS-1$
				}

				if (issue.getSecurityLevel() != null) {
					post.addParameter("security", issue.getSecurityLevel().getId()); //$NON-NLS-1$
				}

				addCustomFields(client, issue, post);

				try {
					execute(post);
					if (!expectRedirect(post, "/browse/", false)) { //$NON-NLS-1$
						handleErrorMessage(post);
					} else {
						final Header locationHeader = post.getResponseHeader("location"); //$NON-NLS-1$
						// parse issue key from issue URL
						String location = locationHeader.getValue();
						int i = location.lastIndexOf("/"); //$NON-NLS-1$
						if (i != -1) {
							issueKey[0] = location.substring(i + 1);
						} else {
							throw new JiraException(
									"The server redirected to an unexpected location while creating an issue: " //$NON-NLS-1$
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
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/browse/").append(issue.getKey()); //$NON-NLS-1$
				urlBuffer.append("?watch=").append(Boolean.toString(watch)); //$NON-NLS-1$

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Changing watch status failed. Return code: " + result); //$NON-NLS-1$
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

			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/browse/").append(issue.getKey()); //$NON-NLS-1$
				urlBuffer.append("?vote=").append(vote ? "vote" : "unvote"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Changing vote failed. Return code: " + result); //$NON-NLS-1$
					}
				} finally {
					head.releaseConnection();
				}
			}

		});
	}

	public void deleteIssue(final JiraIssue issue, IProgressMonitor monitor) throws JiraException {
		doInSession(monitor, new JiraWebSessionCallback() {

			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				StringBuilder urlBuffer = new StringBuilder(baseUrl);
				urlBuffer.append("/secure/DeleteIssue.jspa"); //$NON-NLS-1$
				urlBuffer.append("?id=").append(issue.getId()); //$NON-NLS-1$
				urlBuffer.append("&confirm=true"); //$NON-NLS-1$

				HeadMethod head = new HeadMethod(urlBuffer.toString());
				try {
					int result = execute(head);
					if (result != HttpStatus.SC_OK) {
						throw new JiraException("Deleting issue failed. Return code: " + result); //$NON-NLS-1$
					}
				} finally {
					head.releaseConnection();
				}
			}

		});
	}

	public WebServerInfo getWebServerInfo(IProgressMonitor monitor) throws JiraException {
		final WebServerInfo serverInfo = new WebServerInfo();
		serverInfo.getStatistics().mark();
		session.doInSession(new JiraWebSessionCallback() {
			@Override
			public void run(JiraClient server, String baseUrl, IProgressMonitor monitor) throws JiraException {
				serverInfo.setBaseUrl(session.getBaseURL());
				serverInfo.setCharacterEncoding(session.getCharacterEncoding());
				serverInfo.setInsecureRedirect(session.isInsecureRedirect());
			}
		}, monitor);
		serverInfo.getStatistics().record("Login via web took {0}"); //$NON-NLS-1$
		return serverInfo;
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
				throw new JiraRemoteException("JIRA system error", null); //$NON-NLS-1$
			}

			if (response == null) {
				throw new JiraRemoteMessageException("Error making JIRA request: " + method.getStatusCode(), ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			System.out.print(response);

			StringReader reader = new StringReader(response);
			try {
				StringBuilder msg = new StringBuilder();
				HtmlStreamTokenizer tokenizer = new HtmlStreamTokenizer(reader, null);
				for (Token token = tokenizer.nextToken(); token.getType() != Token.EOF; token = tokenizer.nextToken()) {
					if (token.getType() == Token.TAG) {
						HtmlTag tag = (HtmlTag) token.getValue();

						String classValue = tag.getAttribute("class"); //$NON-NLS-1$
						if (classValue != null) {
							if (tag.getTagType() == Tag.DIV) {
								if (classValue.startsWith("infoBox")) { //$NON-NLS-1$
									throw new JiraRemoteMessageException(getContent(tokenizer, Tag.DIV));
								} else if (classValue.startsWith("errorArea")) { //$NON-NLS-1$
									throw new JiraRemoteMessageException(getContent(tokenizer, Tag.DIV));
								}
							} else if (tag.getTagType() == Tag.SPAN) {
								if (classValue.startsWith("errMsg")) { //$NON-NLS-1$
									msg.append(getContent(tokenizer, Tag.SPAN));
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
				throw new JiraRemoteMessageException("Error parsing JIRA response: " + method.getStatusCode(), ""); //$NON-NLS-1$ //$NON-NLS-2$
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

	private void addCustomFields(JiraClient client, JiraIssue issue, PostMethod post) {
		for (CustomField customField : issue.getCustomFields()) {
			addCustomField(client, post, customField);
		}
	}

	private void addCustomField(JiraClient client, PostMethod post, CustomField customField) {
		for (String value : customField.getValues()) {
			String key = customField.getKey();
			if (includeCustomField(key, value)) {
				if (value != null
						&& (JiraFieldType.DATE.getKey().equals(key) || JiraFieldType.DATETIME.getKey().equals(key))) {
					try {
						Date date = JiraRssHandler.getDateTimeFormat().parse(value);
						DateFormat format;
						if (JiraFieldType.DATE.getKey().equals(key)) {
							format = client.getConfiguration().getDateFormat();
						} else {
							format = client.getConfiguration().getDateTimeFormat();
						}
						value = format.format(date);
					} catch (ParseException e) {
						// XXX ignore
					}
				}
				post.addParameter(customField.getId(), value == null ? "" : value); //$NON-NLS-1$
			}
		}
	}

	private void addFields(JiraClient client, JiraIssue issue, PostMethod post, String[] fields) {
		for (String field : fields) {
			if ("duedate".equals(field)) { //$NON-NLS-1$
				if (issue.getDue() != null) {
					DateFormat format = client.getConfiguration().getDateFormat();
					post.addParameter("duedate", format.format(issue.getDue())); //$NON-NLS-1$
				} else {
					post.addParameter("duedate", ""); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else if (field.startsWith("customfield_")) { //$NON-NLS-1$
				for (CustomField customField : issue.getCustomFields()) {
					addCustomField(client, post, customField);
				}
			} else {
				String[] values = issue.getFieldValues(field);
				if (values == null) {
					// method.addParameter(field, "");
				} else {
					for (String value : values) {
						post.addParameter(field, value);
					}
				}
			}
		}
	}

	private boolean includeCustomField(String key, String value) {
		if (key == null) {
			return true;
		}

		if (key.startsWith("com.pyxis.greenhopper.jira:greenhopper-ranking")) { //$NON-NLS-1$
			// if this field is a valid float sent it back if not ignore it (old greenhopper publishes invalid content in this field)
			try {
				Float.parseFloat(value);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		return !key.startsWith("com.atlassian.jira.toolkit") && //$NON-NLS-1$
				!key.startsWith("com.atlassian.jira.ext.charting"); //$NON-NLS-1$
	}

	private void doInSession(IProgressMonitor monitor, JiraWebSessionCallback callback) throws JiraException {
		session.doInSession(callback, monitor);
	}

}
