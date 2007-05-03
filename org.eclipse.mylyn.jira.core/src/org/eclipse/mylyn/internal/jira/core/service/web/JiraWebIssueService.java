/*******************************************************************************
 * Copyright (c) 2007 Mylar committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brock Janiczak - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.internal.jira.core.service.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.text.html.HTML.Tag;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartBase;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.eclipse.mylar.core.net.HtmlStreamTokenizer;
import org.eclipse.mylar.core.net.HtmlTag;
import org.eclipse.mylar.core.net.HtmlStreamTokenizer.Token;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.Resolution;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.SingleIssueCollector;
import org.eclipse.mylar.internal.jira.core.service.JiraException;
import org.eclipse.mylar.internal.jira.core.service.JiraRemoteMessageException;
import org.eclipse.mylar.internal.jira.core.service.JiraServer;
import org.eclipse.mylar.internal.jira.core.service.web.rss.RssFeedProcessorCallback;

/**
 * TODO look at creation Operation classes to perform each of these actions
 * 
 * @author Brock Janiczak
 */
public class JiraWebIssueService {

	private static final String DATE_FORMAT = "dd-MMM-yyyy"; //$NON-NLS-1$

	private final JiraServer server;

	public JiraWebIssueService(JiraServer server) {
		this.server = server;
	}

	public void addCommentToIssue(final Issue issue, final String comment) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/AddComment.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
				post.addParameter("comment", comment);
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				try {
					client.executeMethod(post);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					post.releaseConnection();
				}

			}

		});
	}

	public void updateIssue(final Issue issue, final String comment) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/EditIssue.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
				post.addParameter("summary", issue.getSummary());
				post.addParameter("issuetype", issue.getType().getId());
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId());
				}
				if (issue.getDue() != null) {
					post.addParameter("duedate", new SimpleDateFormat(DATE_FORMAT, Locale.US).format(issue.getDue()));
				} else {
					post.addParameter("duedate", "");
				}
				Component[] components = issue.getComponents();
				if (components.length == 0) {
					post.addParameter("components", "-1");
				} else {
					for (int i = 0; i < components.length; i++) {
						post.addParameter("components", components[i].getId());
					}
				}

				Version[] versions = issue.getReportedVersions();
				if (versions.length == 0) {
					post.addParameter("versions", "-1");
				} else {
					for (int i = 0; i < versions.length; i++) {
						post.addParameter("versions", versions[i].getId());
					}
				}

				Version[] fixVersions = issue.getFixVersions();
				if (fixVersions.length == 0) {
					post.addParameter("fixVersions", "-1");
				} else {
					for (int i = 0; i < fixVersions.length; i++) {
						post.addParameter("fixVersions", fixVersions[i].getId());
					}
				}

				// TODO need to be able to choose unassigned and automatic
				if (issue.getAssignee() != null) {
					post.addParameter("assignee", issue.getAssignee());
				} else {
					post.addParameter("assignee", "-1");
				}
				post.addParameter("reporter", issue.getReporter());
				post.addParameter("environment", issue.getEnvironment());
				post.addParameter("description", issue.getDescription());

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				try {
					int result = client.executeMethod(post);
					if (result != HttpStatus.SC_TEMPORARY_REDIRECT) {
						handleErrorMessage(post);
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	public void assignIssueTo(final Issue issue, final int assigneeType, final String user, final String comment) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/AssignIssue.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

				post.addParameter("assignee", getAssigneeParam(server, issue, assigneeType, user));

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");
				post.addParameter("id", issue.getId());

				try {
					int result = client.executeMethod(post);
					if (result != HttpStatus.SC_TEMPORARY_REDIRECT) {
						handleErrorMessage(post);
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					post.releaseConnection();
				}
			}

		});
	}

	public void advanceIssueWorkflow(final Issue issue, final String action, final Resolution resolution,
			final Version[] fixVersions, final String comment, final int assigneeType, final String user) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/CommentAssignIssue.jspa");

				PostMethod post = new PostMethod(rssUrlBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
				
				if (resolution != null) {
					post.addParameter("resolution", resolution.getId());
					if (fixVersions == null || fixVersions.length == 0) {
						post.addParameter("fixVersions", "-1");
					} else {
						for (int i = 0; i < fixVersions.length; i++) {
							post.addParameter("fixVersions", fixVersions[i].getId());
						}
					}
				}

				post.addParameter("assignee", getAssigneeParam(server, issue, assigneeType, user));

				if (comment != null) {
					post.addParameter("comment", comment);
				}
				post.addParameter("commentLevel", "");
				post.addParameter("action", action);
				post.addParameter("id", issue.getId());

				try {
					int result = client.executeMethod(post);
					if (result != HttpStatus.SC_TEMPORARY_REDIRECT) {
						handleErrorMessage(post);
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					post.releaseConnection();
				}
			}
		});
	}

	public void advanceIssueWorkflow(final Issue issue, final String action) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) throws JiraException {
				StringBuffer rssUrlBuffer = new StringBuffer(server.getBaseURL());
				rssUrlBuffer.append("/secure/WorkflowUIDispatcher.jspa?");
				rssUrlBuffer.append("id=").append(issue.getId());
				rssUrlBuffer.append("&action=").append(action);
				GetMethod method = new GetMethod(rssUrlBuffer.toString());
				method.setFollowRedirects(false);
				try {
					int result = client.executeMethod(method);
					if (result != HttpStatus.SC_TEMPORARY_REDIRECT) {
						handleErrorMessage(method);
					}
				} catch (IOException e) {
					throw new JiraException(e);
				} finally {
					method.releaseConnection();
				}
			}

		});
	}

	public void startIssue(Issue issue) throws JiraException {
		advanceIssueWorkflow(issue, "4");
	}

	public void stopIssue(Issue issue) throws JiraException {
		advanceIssueWorkflow(issue, "301");
	}

	public void resolveIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment,
			int assigneeType, String user) throws JiraException {
		advanceIssueWorkflow(issue, "5", resolution, fixVersions, comment, assigneeType, user);
	}

	public void reopenIssue(Issue issue, String comment, int assigneeType, String user) throws JiraException {
		if (issue.getStatus().isResolved() || issue.getStatus().isClosed()) {
			advanceIssueWorkflow(issue, "3", null, null, comment, assigneeType, user);
		} else {
			advanceIssueWorkflow(issue, "6", null, null, comment, assigneeType, user);
		}
	}

	public void closeIssue(Issue issue, Resolution resolution, Version[] fixVersions, String comment, int assigneeType,
			String user) throws JiraException {
		if (issue.getStatus().isResolved()) {
			advanceIssueWorkflow(issue, "701", resolution, fixVersions, comment, assigneeType, user);
		} else {
			advanceIssueWorkflow(issue, "2", resolution, fixVersions, comment, assigneeType, user);
		}
	}

	public void attachFile(final Issue issue, final String comment, final String filename, final byte[] contents,
			final String contentType) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.internal.jira.core.service.web.JiraWebSessionCallback#execute(org.apache.commons.httpclient.HttpClient,org.eclipse.mylar.internal.jira.core.service.JiraServer)
			 */
			public void execute(HttpClient client, JiraServer server) {
				StringBuffer attachFileURLBuffer = new StringBuffer(server.getBaseURL());
				attachFileURLBuffer.append("/secure/AttachFile.jspa");

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

				List<PartBase> parts = new ArrayList<PartBase>();

				FilePart filePart = new FilePart("filename.1", new ByteArrayPartSource(filename, contents));
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

				post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post
						.getParams()));

				try {
					client.executeMethod(post);
					// Expect a 302 response here as it can redirect to the
					// manage attachments screen
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					post.releaseConnection();
				}

			}
		});
	}

	public void attachFile(final Issue issue, final String comment, final File file, final String contentType) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.internal.jira.core.service.web.JiraWebSessionCallback#execute(org.apache.commons.httpclient.HttpClient,org.eclipse.mylar.internal.jira.core.service.JiraServer)
			 */
			public void execute(HttpClient client, JiraServer server) {
				StringBuffer attachFileURLBuffer = new StringBuffer(server.getBaseURL());
				attachFileURLBuffer.append("/secure/AttachFile.jspa");

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());
				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

				try {
					List<PartBase> parts = new ArrayList<PartBase>();

					FilePart filePart = new FilePart("filename.1", file);
					StringPart idPart = new StringPart("id", issue.getId());
					StringPart commentLevelPart = new StringPart("commentLevel", "");

					// The transfer encodings have to be removed for some reason
					// There is no need to send the content types for the
					// strings, as they should be in the correct format
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

					post.setRequestEntity(new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), post
							.getParams()));

					client.executeMethod(post);
					// Expect a 302 response here as it can redirect to the
					// manage attachments screen
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					post.releaseConnection();
				}

			}
		});
	}

	public Issue createIssue(final Issue issue) throws JiraException {
		final String[] location = new String[1];
		final Issue[] result = new Issue[1];

		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.mylar.internal.jira.core.service.web.JiraWebSessionCallback#execute(org.apache.commons.httpclient.HttpClient,org.eclipse.mylar.internal.jira.core.service.JiraServer)
			 */
			public void execute(HttpClient client, JiraServer server) throws JiraException {
				StringBuffer attachFileURLBuffer = new StringBuffer(server.getBaseURL());
				attachFileURLBuffer.append("/secure/CreateIssueDetails.jspa");

				PostMethod post = new PostMethod(attachFileURLBuffer.toString());

				post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");

				post.addParameter("pid", issue.getProject().getId());
				post.addParameter("issuetype", issue.getType().getId());
				post.addParameter("summary", issue.getSummary());
				if (issue.getPriority() != null) {
					post.addParameter("priority", issue.getPriority().getId());
				}
				if (issue.getDue() != null) {
					post.addParameter("duedate", new SimpleDateFormat(DATE_FORMAT, Locale.US).format(issue.getDue()));
				}

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

				try {
					int status = client.executeMethod(post);
					// Expect a 302 response here as it should redirect to the
					// issue detail screen
					if (status == HttpURLConnection.HTTP_MOVED_TEMP) {
						Header locationHeader = post.getResponseHeader("Location");
						location[0] = locationHeader.getValue();

						SingleIssueCollector collector = new SingleIssueCollector();
						new RssFeedProcessorCallback(true, collector) {
							protected String getRssUrl() {
								return location[0];
							}
						}.execute(client, server);
						result[0] = collector.getIssue();
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					post.releaseConnection();
				}

			}
		});

		return result[0];
	}

	private void watchUnwatchIssue(final Issue issue, final boolean watch) throws JiraException {
		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) {
				StringBuffer urlBuffer = new StringBuffer(server.getBaseURL());
				urlBuffer.append("/browse/").append(issue.getKey());
				urlBuffer.append("?watch=").append(Boolean.toString(watch));

				HeadMethod head = new HeadMethod(urlBuffer.toString());

				try {
					client.executeMethod(head);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					head.releaseConnection();
				}

			}

		});
	}

	public void watchIssue(final Issue issue) throws JiraException {
		watchUnwatchIssue(issue, true);
	}

	public void unwatchIssue(final Issue issue) throws JiraException {
		watchUnwatchIssue(issue, false);
	}

	private void voteUnvoteIssue(final Issue issue, final boolean vote) throws JiraException {
		if (!issue.canUserVote(this.server.getUserName())) {
			return;
		}

		JiraWebSession s = new JiraWebSession(server);
		s.doInSession(new JiraWebSessionCallback() {

			public void execute(HttpClient client, JiraServer server) {
				StringBuffer urlBuffer = new StringBuffer(server.getBaseURL());
				urlBuffer.append("/browse/").append(issue.getKey());
				urlBuffer.append("?vote=").append(vote ? "vote" : "unvote");

				HeadMethod head = new HeadMethod(urlBuffer.toString());

				try {
					client.executeMethod(head);
				} catch (HttpException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					head.releaseConnection();
				}

			}

		});
	}

	public void voteIssue(final Issue issue) throws JiraException {
		voteUnvoteIssue(issue, true);
	}

	public void unvoteIssue(final Issue issue) throws JiraException {
		voteUnvoteIssue(issue, false);
	}

	private String getAssigneeParam(JiraServer server, Issue issue, int assigneeType, String user) {
		switch (assigneeType) {
		case JiraServer.ASSIGNEE_CURRENT:
			return issue.getAssignee();
		case JiraServer.ASSIGNEE_DEFAULT:
			return "-1"; //$NON-NLS-1$
		case JiraServer.ASSIGNEE_NONE:
			return ""; //$NON-NLS-1$
		case JiraServer.ASSIGNEE_SELF:
			return server.getUserName();
		case JiraServer.ASSIGNEE_USER:
			return user;
		default:
			return user;
		}
	}

	private void handleErrorMessage(HttpMethodBase method) throws IOException, JiraException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(),
				JiraServer.CHARSET));
		try {
			StringBuilder sb = new StringBuilder();
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
								sb.append(getContent(tokenizer, HtmlTag.Type.SPAN));
							}
						}
					}

				}
			}
			if (sb.length() == 0) {
				sb.append("No details available");
			}
			throw new JiraRemoteMessageException("An error has occured: " + sb.toString(), "");
		} catch (ParseException e) {
			throw new JiraRemoteMessageException("An error has occured.", "");
		} finally {
			reader.close();
		}
	}

	private String getContent(HtmlStreamTokenizer tokenizer, Tag closingTag) throws IOException, ParseException {
		StringBuffer sb = new StringBuffer();
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

}
