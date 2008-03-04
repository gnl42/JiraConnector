/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.jira.core.service.web.rss;

import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.internal.jira.core.JiraCorePlugin;
import org.eclipse.mylyn.internal.jira.core.html.HTML2TextReader;
import org.eclipse.mylyn.internal.jira.core.model.Attachment;
import org.eclipse.mylyn.internal.jira.core.model.Comment;
import org.eclipse.mylyn.internal.jira.core.model.Component;
import org.eclipse.mylyn.internal.jira.core.model.CustomField;
import org.eclipse.mylyn.internal.jira.core.model.Issue;
import org.eclipse.mylyn.internal.jira.core.model.IssueLink;
import org.eclipse.mylyn.internal.jira.core.model.Project;
import org.eclipse.mylyn.internal.jira.core.model.SecurityLevel;
import org.eclipse.mylyn.internal.jira.core.model.Subtask;
import org.eclipse.mylyn.internal.jira.core.model.Version;
import org.eclipse.mylyn.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.monitor.core.StatusHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * From the wiki:
 * 
 * <ul>
 * <li>Cascading Select - Multiple select lists where the options for the second select list dynamically updates based
 * on the value of the first</li>
 * <li>Date Picker - Input field allowing input with a date picker and enforcing valid dates</li>
 * <li>Free Text Field (unlimited text) - Multiple line text-area enabling entry of longer text strings</li>
 * <li>Multi Checkboxes Checkboxes allowing multiple values to be selected</li>
 * <li>Multi Select - Select list permitting multiple values to be selected</li>
 * <li>Number Field Input field storing and validating numeric (floating point) values</li>
 * <li>Project Picker - Select list displaying the projects viewable by the user in the system</li>
 * <li>Radio Buttons - Radio buttons ensuring only one value can be selected</li>
 * <li>Select List - Single select list with a configurable list of options</li>
 * <li>Text Field - Basic single line input field to allow simple text input of less than 255 characters</li>
 * <li>URL Field - Input field that validates a valid URL</li>
 * <li>User Picker - Choose a user from the user base via a popup picker window.</li>
 * <li>Version Picker - Select list with the all versions related to the current project of the issue</li>
 * </ul>
 * 
 * The processing of custom fields might need to be done using extension points to handle custom UI
 * 
 * <p>
 * TODO need a way to convert from custom values into typed values
 * 
 * <p>
 * com.atlassian.jira.plugin.system.customfieldtypes:textfield
 * com.atlassian.jira.plugin.system.customfieldtypes:textarea
 * 
 * com.atlassian.jira.plugin.system.customfieldtypes:select
 * com.atlassian.jira.plugin.system.customfieldtypes:multiselect
 * com.atlassian.jira.plugin.system.customfieldtypes:cascadingselect
 * com.atlassian.jira.plugin.system.customfieldtypes:multicheckboxes
 * com.atlassian.jira.plugin.system.customfieldtypes:datepicker
 * com.atlassian.jira.plugin.system.customfieldtypes:datetime com.atlassian.jira.plugin.system.customfieldtypes:version
 * com.atlassian.jira.plugin.system.customfieldtypes:multiversion
 * com.atlassian.jira.plugin.system.customfieldtypes:userpicker
 * com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker
 * com.atlassian.jira.plugin.system.customfieldtypes:grouppicker
 * com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker
 * com.atlassian.jira.plugin.system.customfieldtypes:float com.atlassian.jira.plugin.system.customfieldtypes:project
 * com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons com.atlassian.jira.plugin.system.customfieldtypes:url
 * com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield
 * 
 * <p>
 * TODO probably need to filter out the following field types (and maybe others from JIRA Toolkit)
 * 
 * <p>
 * com.atlassian.jira.toolkit:assigneedomain com.atlassian.jira.toolkit:attachments com.atlassian.jira.toolkit:comments
 * com.atlassian.jira.toolkit:dayslastcommented com.atlassian.jira.toolkit:lastusercommented
 * com.atlassian.jira.toolkit:message com.atlassian.jira.toolkit:multikeyfield com.atlassian.jira.toolkit:multiproject
 * com.atlassian.jira.toolkit:originalestimate com.atlassian.jira.toolkit:participants
 * com.atlassian.jira.toolkit:reporterdomain com.atlassian.jira.toolkit:resolveddate
 * com.atlassian.jira.toolkit:supporttools com.atlassian.jira.toolkit:userproperty
 * com.atlassian.jira.toolkit:velocitymessage com.atlassian.jira.toolkit:velocityviewmessage
 * com.atlassian.jira.toolkit:viewmessage
 * 
 * com.atlassian.jira.ext.charting:firstresponsedate
 * 
 * @see http://www.atlassian.com/software/jira/docs/latest/customfields/overview.html
 * @see http://confluence.atlassian.com/display/JIRAEXT/JIRA+Toolkit
 * 
 * @author Brock Janiczak
 * @author Steffen Pingel
 * @author Eugene Kuleshov
 */
public class RssContentHandler extends DefaultHandler {

	private static final String CUSTOM_FIELD_TYPE_TEXTAREA = "com.atlassian.jira.plugin.system.customfieldtypes:textarea";

	private static final SimpleDateFormat XML_DATE_FORMAT = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss Z (zz)", Locale.US); //$NON-NLS-1$

	private static final SimpleDateFormat XML_DUE_DATE_FORMAT = new SimpleDateFormat(
			"E, dd MMM yyyy HH:mm:ss", Locale.US); //$NON-NLS-1$

	private static final String CREATED_ATTR = "created"; //$NON-NLS-1$

	private static final String LEVEL_ATTR = "level"; //$NON-NLS-1$

	private static final String AUTHOR_ATTR = "author"; //$NON-NLS-1$

	private static final String ID_ATTR = "id"; //$NON-NLS-1$

	private static final String KEY_ATTR = "key"; //$NON-NLS-1$

	private static final String USERNAME_ATTR = "username"; //$NON-NLS-1$

	private static final String SECONDS_ATTR = "seconds"; //$NON-NLS-1$

	private static final String NAME_ATTR = "name"; //$NON-NLS-1$

	private static final String SIZE_ATTR = "size"; //$NON-NLS-1$

	private static final String RSS = "rss"; //$NON-NLS-1$

	private static final String CHANNEL = "channel"; //$NON-NLS-1$

	private static final String ITEM = "item"; //$NON-NLS-1$

	private static final String COMMENTS = "comments"; //$NON-NLS-1$

	private static final String COMMENT = "comment"; //$NON-NLS-1$

	private static final String VOTES = "votes"; //$NON-NLS-1$

	private static final String ORIGINAL_ESTIMATE = "timeoriginalestimate"; //$NON-NLS-1$

	private static final String CURRENT_ESTIMATE = "timeestimate"; //$NON-NLS-1$

	private static final String ACTUAL = "timespent"; //$NON-NLS-1$

	private static final String SUBTASKS = "subtasks"; //$NON-NLS-1$

	private static final String SUBTASK = "subtask"; //$NON-NLS-1$

	private static final String ATTACHMENTS = "attachments"; //$NON-NLS-1$

	private static final String ATTACHMENT = "attachment"; //$NON-NLS-1$

	private static final String DUE = "due"; //$NON-NLS-1$

	private static final String COMPONENT = "component"; //$NON-NLS-1$

	private static final String FIX_VERSION = "fixVersion"; //$NON-NLS-1$

	private static final String VERSION = "version"; //$NON-NLS-1$

	private static final String UPDATED = "updated"; //$NON-NLS-1$

	private static final String CREATED = "created"; //$NON-NLS-1$

	private static final String REPORTER = "reporter"; //$NON-NLS-1$

	private static final String ASSIGNEE = "assignee"; //$NON-NLS-1$

	private static final String RESOLUTION = "resolution"; //$NON-NLS-1$

	private static final String STATUS = "status"; //$NON-NLS-1$

	private static final String PRIORITY = "priority"; //$NON-NLS-1$

	private static final String TYPE = "type"; //$NON-NLS-1$

	private static final String SUMMARY = "summary"; //$NON-NLS-1$

	private static final String KEY = "key"; //$NON-NLS-1$

	private static final String PARENT = "parent"; //$NON-NLS-1$

	private static final String ENVIRONMENT = "environment"; //$NON-NLS-1$

	private static final String DESCRIPTION = "description"; //$NON-NLS-1$

	private static final String LINK = "link"; //$NON-NLS-1$

	private static final String TITLE = "title"; //$NON-NLS-1$

	private static final String CUSTOM_FIELDS = "customfields"; //$NON-NLS-1$

	private static final String CUSTOM_FIELD = "customfield"; //$NON-NLS-1$

	private static final String CUSTOM_FIELD_NAME = "customfieldname"; //$NON-NLS-1$

	private static final String CUSTOM_FIELD_VALUES = "customfieldvalues"; //$NON-NLS-1$

	private static final String CUSTOM_FIELD_VALUE = "customfieldvalue"; //$NON-NLS-1$

	private static final String ISSUE_LINKS = "issuelinks"; //$NON-NLS-1$

	private static final String ISSUE_LINK_TYPE = "issuelinktype"; //$NON-NLS-1$

	private static final String ISSUE_LINK_NAME = "name"; //$NON-NLS-1$

	private static final String INWARD_LINKS = "inwardlinks"; //$NON-NLS-1$

	private static final String OUTWARD_LINKS = "outwardlinks"; //$NON-NLS-1$

	private static final String ISSUE_LINK = "issuelink"; //$NON-NLS-1$

	private static final String ISSUE_KEY = "issuekey"; //$NON-NLS-1$

	private static final String SECURITY = "security";

	private static final int START = 0;

	private static final int LOOKING_FOR_CHANNEL = 1;

	private static final int LOOKING_FOR_ITEM = 2;

	private static final int IN_ITEM = 3;

	private static final int IN_COMMENTS = 4;

	private static final int IN_CUSTOM_FIELDS = 5;

	private static final int IN_CUSTOM_FIELD = 6;

	private static final int IN_CUSTOM_FIELD_VALUES = 7;

	private static final int IN_ISSUE_LINKS = 8;

	private static final int IN_ISSUE_LINK_TYPE = 9;

	private static final int IN_XWARDS_LINKS = 10;

	private static final int IN_XWARDS_ISSUE_LINK = 12;

	private static final int IN_ATTACHMENTS = 14;

	private static final int IN_CUSTOM_FIELD_NAME = 15;

	private static final int IN_CUSTOM_FIELD_VALUE = 16;

	private static final int IN_SUBTASKS = 17;

	int state = START;

	private StringBuffer currentElementText;

	private final JiraClient client;

	private final IssueCollector collector;

	private Issue currentIssue;

	private String commentAuthor;

	private String commentLevel;

	private Date commentDate;

	private final ArrayList<Comment> currentComments = new ArrayList<Comment>();

	private ArrayList<Version> currentFixVersions = null;

	private ArrayList<Version> currentReportedVersions = null;

	private ArrayList<Component> currentComponents = null;

	private final ArrayList<Attachment> currentAttachments = new ArrayList<Attachment>();

	private final ArrayList<CustomField> currentCustomFields = new ArrayList<CustomField>();

	private String currentSubtaskId;

	private final ArrayList<Subtask> currentSubtasks = new ArrayList<Subtask>();

	private String currentIssueLinkTypeId;

	private String currentIssueLinkTypeName;

	private String currentIssueLinkInwardDescription;

	private String currentIssueLinkOutwardDescription;

	private String currentIssueLinkIssueId;

	private final ArrayList<IssueLink> currentIssueLinks = new ArrayList<IssueLink>();

	private String customFieldId;

	private String customFieldKey;

	private String customFieldName;

	private final ArrayList<String> customFieldValues = new ArrayList<String>();

	private String attachmentId;

	private String attachmentName;

	private long attachmentSize;

	private String attachmentAuthor;

	private Date attachmentCreated;

	private boolean markupDetected;

	/**
	 * Creates a new RSS reader that will create issues from the RSS information by querying the local Jira Server for
	 * any missing information. Issues will be published to <code>collector</code> as they are read from the stream.
	 * 
	 * @param client
	 *            Jira server we are listing the issues of. This can either be a locally cached jira server or a
	 *            connection to a live instance.
	 * @param collector
	 *            Collecter that will be processing the issues as they are read from the RSS feed.
	 * @param baseUrl
	 *            the base URL of the repository
	 */
	public RssContentHandler(JiraClient client, IssueCollector collector, String baseUrl) {
		this.client = client;
		this.collector = collector;
	}

	@Override
	public void startDocument() throws SAXException {
		state = START;
		currentElementText = new StringBuffer(256);
		collector.start();
	}

	@Override
	public void endDocument() throws SAXException {
		if (state != START) {
			// ignore
		}
		this.collector.done();
		// remove unused buffers
		currentElementText = null;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentElementText.setLength(0);
		if (collector.isCancelled()) {
			throw new ParseCancelledException("User canceled operation");
		}

		switch (state) {
		case START:
			if (RSS.equals(localName)) {
				state = LOOKING_FOR_CHANNEL;
			}
			break;
		case LOOKING_FOR_CHANNEL:
			if (CHANNEL.equals(localName)) {
				state = LOOKING_FOR_ITEM;
			}
			break;
		case LOOKING_FOR_ITEM:
			if (ITEM.equals(localName)) {
				state = IN_ITEM;
				currentIssue = new Issue();
			}
			break;
		case IN_ITEM:
			if (KEY.equals(localName)) {
				currentIssue.setId(attributes.getValue(ID_ATTR));
			} else if (PARENT.equals(localName)) {
				currentIssue.setParentId(attributes.getValue(ID_ATTR));
			} else if (TYPE.equals(localName)) {
				currentIssue.setType(client.getCache().getIssueTypeById(attributes.getValue(ID_ATTR)));
			} else if (PRIORITY.equals(localName)) {
				currentIssue.setPriority(client.getCache().getPriorityById(attributes.getValue(ID_ATTR)));
			} else if (STATUS.equals(localName)) {
				currentIssue.setStatus(client.getCache().getStatusById(attributes.getValue(ID_ATTR)));
			} else if (ASSIGNEE.equals(localName)) {
				String assigneeName = attributes.getValue(USERNAME_ATTR);
				currentIssue.setAssignee(assigneeName);
			} else if (REPORTER.equals(localName)) {
				String reporterName = attributes.getValue(USERNAME_ATTR);
				currentIssue.setReporter(reporterName);
			} else if (RESOLUTION.equals(localName)) {
				String resolutionId = attributes.getValue(ID_ATTR);
				currentIssue.setResolution(resolutionId != null ? client.getCache().getResolutionById(resolutionId)
						: null);
			} else if (ORIGINAL_ESTIMATE.equals(localName)) {
				currentIssue.setInitialEstimate(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
			} else if (CURRENT_ESTIMATE.equals(localName)) {
				currentIssue.setEstimate(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
			} else if (ACTUAL.equals(localName)) {
				currentIssue.setActual(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
			} else if (SECURITY.equals(localName)) {
				SecurityLevel securityLevel = new SecurityLevel();
				securityLevel.setId(attributes.getValue(ID_ATTR));
				currentIssue.setSecurityLevel(securityLevel);
			}

			if (COMMENTS.equals(localName)) {
				state = IN_COMMENTS;
			} else if (ISSUE_LINKS.equals(localName)) {
				state = IN_ISSUE_LINKS;
			} else if (SUBTASKS.equals(localName)) {
				state = IN_SUBTASKS;
			} else if (CUSTOM_FIELDS.equals(localName)) {
				state = IN_CUSTOM_FIELDS;
			} else if (ATTACHMENTS.equals(localName)) {
				state = IN_ATTACHMENTS;
			}

			break;
		case IN_COMMENTS:
			if (COMMENT.equals(localName)) {
				commentAuthor = attributes.getValue(AUTHOR_ATTR);
				commentLevel = attributes.getValue(LEVEL_ATTR);
				commentDate = convertToDate(attributes.getValue(CREATED_ATTR));
			}
			break;
		case IN_ISSUE_LINKS:
			if (ISSUE_LINK_TYPE.equals(localName)) {
				state = IN_ISSUE_LINK_TYPE;
				currentIssueLinkTypeId = attributes.getValue(ID_ATTR);
			}
			break;
		case IN_ISSUE_LINK_TYPE:
			if (ISSUE_LINK_NAME.equals(localName)) {
				//
			} else if (INWARD_LINKS.equals(localName)) {
				currentIssueLinkInwardDescription = attributes.getValue(DESCRIPTION);
				state = IN_XWARDS_LINKS;
			} else if (OUTWARD_LINKS.equals(localName)) {
				currentIssueLinkOutwardDescription = attributes.getValue(DESCRIPTION);
				state = IN_XWARDS_LINKS;
			}
			break;
		case IN_XWARDS_LINKS:
			if (ISSUE_LINK.equals(localName)) {
				state = IN_XWARDS_ISSUE_LINK;
			}
			break;
		case IN_XWARDS_ISSUE_LINK:
			if (ISSUE_KEY.equals(localName)) {
				currentIssueLinkIssueId = attributes.getValue(ID_ATTR);
			}
			break;

		case IN_CUSTOM_FIELDS:
			if (CUSTOM_FIELD.equals(localName)) {
				customFieldId = attributes.getValue(ID_ATTR);
				customFieldKey = attributes.getValue(KEY_ATTR);
				state = IN_CUSTOM_FIELD;
			}
			break;
		case IN_CUSTOM_FIELD:
			if (CUSTOM_FIELD_NAME.equals(localName)) {
				state = IN_CUSTOM_FIELD_NAME;
			} else if (CUSTOM_FIELD_VALUES.equals(localName)) {
				state = IN_CUSTOM_FIELD_VALUES;
			}
			break;

		case IN_CUSTOM_FIELD_VALUES:
			if (CUSTOM_FIELD_VALUE.equals(localName)) {
				state = IN_CUSTOM_FIELD_VALUE;
			}
			break;

		case IN_ATTACHMENTS:
			if (ATTACHMENT.equals(localName)) {
				attachmentId = attributes.getValue(ID_ATTR);
				attachmentName = attributes.getValue(NAME_ATTR);
				attachmentSize = Long.parseLong(attributes.getValue(SIZE_ATTR));
				attachmentAuthor = attributes.getValue(AUTHOR_ATTR);
				attachmentCreated = convertToDate(attributes.getValue(CREATED_ATTR));
			}
			break;
		case IN_SUBTASKS:
			if (SUBTASK.equals(localName)) {
				currentSubtaskId = attributes.getValue(ID_ATTR);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (state) {
		case IN_SUBTASKS:
			if (SUBTASK.equals(localName)) {
				currentSubtasks.add(new Subtask(currentSubtaskId, getCurrentElementText()));
				currentSubtaskId = null;
			} else if (SUBTASKS.equals(localName)) {
				state = IN_ITEM;
			}
			break;
		case IN_ATTACHMENTS:
			if (ATTACHMENTS.equals(localName)) {
				state = IN_ITEM;
			} else if (ATTACHMENT.equals(localName)) {
				Attachment attachment = new Attachment(attachmentId, attachmentName, attachmentSize, attachmentAuthor,
						attachmentCreated);
				currentAttachments.add(attachment);
			}
			break;

		case IN_CUSTOM_FIELD_VALUE:
			if (CUSTOM_FIELD_VALUE.equals(localName)) {
				customFieldValues.add(getCurrentElementText());
				state = IN_CUSTOM_FIELD_VALUES;
			}
			break;
		case IN_CUSTOM_FIELD_VALUES:
			if (CUSTOM_FIELD_VALUES.equals(localName)) {
				if (customFieldValues.size() == 0) {
					// strip any line breaks and padding
					String text = getCurrentElementTextEscapeHtml();
					customFieldValues.add(text.trim());
				}
				state = IN_CUSTOM_FIELD;
			}
			break;
		case IN_CUSTOM_FIELD_NAME:
			if (CUSTOM_FIELD_NAME.equals(localName)) {
				customFieldName = getCurrentElementText();
				state = IN_CUSTOM_FIELD;
			}
			break;
		case IN_CUSTOM_FIELD:
			if (CUSTOM_FIELD.equals(localName)) {
				boolean customFieldMarkupDetected = false;
				if (CUSTOM_FIELD_TYPE_TEXTAREA.equals(customFieldKey)) {
					for (int i = customFieldValues.size() - 1; i >= 0; i--) {
						String value = customFieldValues.get(i);
						if (hasMarkup(value)) {
							customFieldMarkupDetected = true;
						} else {
							customFieldValues.set(i, stripTags(value));
						}
					}
				}
				markupDetected |= customFieldMarkupDetected;

				CustomField customField = new CustomField(customFieldId, customFieldKey, customFieldName,
						customFieldValues);
				customField.setMarkupDetected(customFieldMarkupDetected);
				currentCustomFields.add(customField);
				customFieldId = null;
				customFieldKey = null;
				customFieldName = null;
				customFieldValues.clear();
				state = IN_CUSTOM_FIELDS;
			}
			break;
		case IN_CUSTOM_FIELDS:
			if (CUSTOM_FIELDS.equals(localName)) {
				state = IN_ITEM;
			}
			break;

		case IN_XWARDS_ISSUE_LINK:
			if (ISSUE_LINK.equals(localName)) {
				String key = getCurrentElementText().trim();
				IssueLink link = new IssueLink(currentIssueLinkIssueId, key, currentIssueLinkTypeId,
						currentIssueLinkTypeName, currentIssueLinkInwardDescription, currentIssueLinkOutwardDescription);
				currentIssueLinks.add(link);
				currentIssueLinkIssueId = null;
				state = IN_XWARDS_LINKS;
			}
			break;

		case IN_XWARDS_LINKS:
			if (OUTWARD_LINKS.equals(localName) || INWARD_LINKS.equals(localName)) {
				state = IN_ISSUE_LINK_TYPE;
				currentIssueLinkOutwardDescription = null;
				currentIssueLinkInwardDescription = null;
			}
			break;

		case IN_ISSUE_LINK_TYPE:
			if (ISSUE_LINK_TYPE.equals(localName)) {
				currentIssueLinkTypeName = null;
				state = IN_ISSUE_LINKS;
			} else if (ISSUE_LINK_NAME.equals(localName)) {
				currentIssueLinkTypeName = getCurrentElementText().trim();
			}
			break;

		case IN_ISSUE_LINKS:
			if (ISSUE_LINKS.equals(localName)) {
				state = IN_ITEM;
			}
			break;

		case IN_COMMENTS:
			if (COMMENTS.equals(localName)) {
				state = IN_ITEM;
			} else if (COMMENT.equals(localName)) {
				boolean commentMarkupDetected = false;
				if (hasMarkup(currentElementText.toString())) {
					commentMarkupDetected = true;
				}
				Comment comment = new Comment(getCurrentElementTextEscapeHtml(), commentAuthor, commentLevel,
						commentDate);
				comment.setMarkupDetected(commentMarkupDetected);
				currentComments.add(comment);
			}
			break;
		case IN_ITEM:
			if (CHANNEL.equals(localName)) {
				state = LOOKING_FOR_CHANNEL;
			} else if (ITEM.equals(localName)) {
				if (currentReportedVersions != null) {
					currentIssue.setReportedVersions(currentReportedVersions.toArray(new Version[currentReportedVersions.size()]));
				}
				if (currentFixVersions != null) {
					currentIssue.setFixVersions(currentFixVersions.toArray(new Version[currentFixVersions.size()]));
				}
				if (currentComponents != null) {
					currentIssue.setComponents(currentComponents.toArray(new Component[currentComponents.size()]));
				}
				currentIssue.setComments(currentComments.toArray(new Comment[currentComments.size()]));
				currentIssue.setAttachments(currentAttachments.toArray(new Attachment[currentAttachments.size()]));
				currentIssue.setCustomFields(currentCustomFields.toArray(new CustomField[currentCustomFields.size()]));
				currentIssue.setSubtasks(currentSubtasks.toArray(new Subtask[currentSubtasks.size()]));
				currentIssue.setIssueLinks(currentIssueLinks.toArray(new IssueLink[currentIssueLinks.size()]));
				currentIssue.setMarkupDetected(markupDetected);
				collector.collectIssue(currentIssue);
				currentIssue = null;
				currentIssueLinks.clear();
				currentSubtasks.clear();
				currentCustomFields.clear();
				currentAttachments.clear();
				currentComments.clear();
				currentFixVersions = null;
				currentReportedVersions = null;
				currentComponents = null;
				markupDetected = false;
				state = LOOKING_FOR_ITEM;
			} else if (TITLE.equals(localName)) {

			} else if (LINK.equals(localName)) {

			} else if (DESCRIPTION.equals(localName)) {
				currentIssue.setDescription(getCurrentElementTextEscapeHtml());
			} else if (ENVIRONMENT.equals(localName)) {
				currentIssue.setEnvironment(getCurrentElementTextEscapeHtml());
			} else if (KEY.equals(localName)) {
				String key = getCurrentElementText();
				currentIssue.setKey(key);
				currentIssue.setUrl(client.getBaseUrl() + "/browse/" + key);
				// TODO super dodgey to assume the project from the issue key
				int i = key.lastIndexOf('-');
				if (i == -1) {
					//throw new SAXException("Invalid project key '" + projectKey + "'");
					break;
				}
				String projectKey = key.substring(0, i);
				Project project = client.getCache().getProjectByKey(projectKey);
				if (project == null) {
					//throw new SAXException("No project with key '" + projectKey + "' found");
					break;
				}
				currentIssue.setProject(project);
			} else if (PARENT.equals(localName)) {
				currentIssue.setParentKey(getCurrentElementText());
			} else if (SUMMARY.equals(localName)) {
				currentIssue.setSummary(getCurrentElementText());
			} else if (CREATED.equals(localName)) {
				currentIssue.setCreated(convertToDate(getCurrentElementText()));
			} else if (UPDATED.equals(localName)) {
				currentIssue.setUpdated(convertToDate(getCurrentElementText()));
			} else if (VERSION.equals(localName)) {
				if (currentIssue.getProject() == null) {
					//throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
					break;
				}
				Version version = currentIssue.getProject().getVersion(getCurrentElementText());
				// TODO add better handling of unknown versions
				if (version != null) {
					// throw new SAXException("No version with name '" + getCurrentElementText() + "' found");
					if (currentReportedVersions == null) {
						currentReportedVersions = new ArrayList<Version>();
					}
					currentReportedVersions.add(version);
				}
			} else if (FIX_VERSION.equals(localName)) {
				if (currentIssue.getProject() == null) {
					//throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
					break;
				}
				Version version = currentIssue.getProject().getVersion(getCurrentElementText());
				// TODO add better handling of unknown versions
				if (version != null) {
					// throw new SAXException("No version with name '" + getCurrentElementText() + "' found");
					if (currentFixVersions == null) {
						currentFixVersions = new ArrayList<Version>();
					}
					currentFixVersions.add(version);
				}
			} else if (COMPONENT.equals(localName)) {
				if (currentIssue.getProject() == null) {
					//throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
					break;
				}
				Component component = currentIssue.getProject().getComponent(getCurrentElementText());
				// TODO add better handling of unknown components
				if (component != null) {
					// throw new SAXException("No component with name '" + getCurrentElementText() + "' found");
					if (currentComponents == null) {
						currentComponents = new ArrayList<Component>();
					}
					currentComponents.add(component);
				}
			} else if (DUE.equals(localName)) {
				currentIssue.setDue(convertToDueDate(getCurrentElementText()));
			} else if (VOTES.equals(localName)) {
				if (getCurrentElementText().length() > 0) {
					try {
						currentIssue.setVotes(Integer.parseInt(getCurrentElementText()));
					} catch (NumberFormatException e) {
						StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN,
								"Error parsing number of votes", e));
					}
				}
			} else if (SECURITY.equals(localName)) {
				SecurityLevel securityLevel = currentIssue.getSecurityLevel();
				if (securityLevel != null) {
					securityLevel.setName(getCurrentElementText());
				}
			} else if (TYPE.equals(localName)) {

			} else if (PRIORITY.equals(localName)) {

			} else if (STATUS.equals(localName)) {

			} else if (ASSIGNEE.equals(localName)) {

			} else if (REPORTER.equals(localName)) {

			} else if (RESOLUTION.equals(localName)) {

			} else if (ORIGINAL_ESTIMATE.equals(localName)) {

			} else if (CURRENT_ESTIMATE.equals(localName)) {

			} else if (ACTUAL.equals(localName)) {

			} else {
				// ignore
			}

			break;
		case LOOKING_FOR_ITEM:
			if (CHANNEL.equals(localName)) {
				state = LOOKING_FOR_CHANNEL;
			}
			break;
		case LOOKING_FOR_CHANNEL:
			if (RSS.equals(localName)) {
				state = START;
			}
			break;

		}
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		currentElementText.append(ch, start, length);
	}

	private static Date convertToDate(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		try {
			return XML_DATE_FORMAT.parse(value);
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, "Error parsing date: \"" + value
					+ "\"", e));
			return null;
		}
	}

	private static Date convertToDueDate(String value) {
		if (value == null || value.length() == 0) {
			return null;
		}
		try {
			return XML_DUE_DATE_FORMAT.parse(value);
		} catch (ParseException e) {
			StatusHandler.log(new Status(IStatus.WARNING, JiraCorePlugin.ID_PLUGIN, "Error parsing due date: \""
					+ value + "\"", e));
			return null;
		}
	}

	private String getCurrentElementText() {
		String unescaped = currentElementText.toString();
		unescaped = StringEscapeUtils.unescapeXml(unescaped);
		return unescaped;
	}

	private String getCurrentElementTextEscapeHtml() {
		String unescaped = currentElementText.toString();
		if (hasMarkup(unescaped)) {
			markupDetected = true;
		} else {
			unescaped = stripTags(unescaped);
		}
		return unescaped;
	}

	/**
	 * Strips HTML tags from <code>text</code>. The RSS Feed enhances the output of certain fields such as
	 * description, environment and comments with HTML tags:
	 * 
	 * <ul>
	 * <li>for each line breaks a <code><br/>\n</code> or <code>\n<br/>\n</code> tag is added
	 * <li>links are wrapped in <code><a></code> tags
	 * </ul>
	 * 
	 * <p>
	 * This method strips all HTML tags and not just the tags mentioned above. The implementation should be refactored
	 * to ignore tags that were not added by the RSS feed.
	 */
	public static String stripTags(String text) {
		if (text == null || text.length() == 0) {
			return "";
		}
		StringReader stringReader = new StringReader(text);
		HTML2TextReader html2TextReader = new HTML2TextReader(stringReader);
		try {
			char[] chars = new char[text.length()];
			int len = html2TextReader.read(chars, 0, text.length());
			if (len == -1) {
				return "";
			}
			return new String(chars, 0, len);
		} catch (IOException e) {
			return text;
		}
	}

	/**
	 * Returns true if <code>unescaped</code> has HTML markup that can not be properly reverted to the original
	 * representation.
	 * 
	 * <p>
	 * Public for testing.
	 */
	public static boolean hasMarkup(String unescaped) {
		// look for any tag that is not <br/> and not <a... or </a>
		int i = unescaped.indexOf("<");
		while (i != -1) {
			if (!(unescaped.regionMatches(i + 1, "br/>", 0, 4) || unescaped.regionMatches(i + 1, "a ", 0, 1) || unescaped.regionMatches(
					i + 1, "/a>", 0, 3))) {
				return true;
			}
			i = unescaped.indexOf("<", i + 1);
		}
		return false;
	}

}