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

package org.eclipse.mylar.internal.jira.core.service.web.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.eclipse.mylar.core.MylarStatusHandler;
import org.eclipse.mylar.internal.jira.core.model.Attachment;
import org.eclipse.mylar.internal.jira.core.model.Comment;
import org.eclipse.mylar.internal.jira.core.model.Component;
import org.eclipse.mylar.internal.jira.core.model.CustomField;
import org.eclipse.mylar.internal.jira.core.model.Issue;
import org.eclipse.mylar.internal.jira.core.model.Project;
import org.eclipse.mylar.internal.jira.core.model.Subtask;
import org.eclipse.mylar.internal.jira.core.model.Version;
import org.eclipse.mylar.internal.jira.core.model.filter.IssueCollector;
import org.eclipse.mylar.internal.jira.core.service.JiraClient;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * From the wiki:
 * 
 * <ul>
 * <li>Cascading Select - Multiple select lists where the options for the
 * second select list dynamically updates based on the value of the first</li>
 * <li>Date Picker - Input field allowing input with a date picker and
 * enforcing valid dates</li>
 * <li>Free Text Field (unlimited text) - Multiple line text-area enabling
 * entry of longer text strings</li>
 * <li>Multi Checkboxes Checkboxes allowing multiple values to be selected</li>
 * <li>Multi Select - Select list permitting multiple values to be selected</li>
 * <li>Number Field Input field storing and validating numeric (floating point)
 * values</li>
 * <li>Project Picker - Select list displaying the projects viewable by the
 * user in the system</li>
 * <li>Radio Buttons - Radio buttons ensuring only one value can be selected</li>
 * <li>Select List - Single select list with a configurable list of options</li>
 * <li>Text Field - Basic single line input field to allow simple text input of
 * less than 255 characters</li>
 * <li>URL Field - Input field that validates a valid URL</li>
 * <li>User Picker - Choose a user from the user base via a popup picker
 * window.</li>
 * <li>Version Picker - Select list with the all versions related to the
 * current project of the issue</li>
 * </ul>
 * 
 * The processing of custom fields might need to be done using extension points
 * to handle custom UI
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
 * com.atlassian.jira.plugin.system.customfieldtypes:datetime
 * com.atlassian.jira.plugin.system.customfieldtypes:version
 * com.atlassian.jira.plugin.system.customfieldtypes:multiversion
 * com.atlassian.jira.plugin.system.customfieldtypes:userpicker
 * com.atlassian.jira.plugin.system.customfieldtypes:multiuserpicker
 * com.atlassian.jira.plugin.system.customfieldtypes:grouppicker
 * com.atlassian.jira.plugin.system.customfieldtypes:multigrouppicker
 * com.atlassian.jira.plugin.system.customfieldtypes:float
 * com.atlassian.jira.plugin.system.customfieldtypes:project
 * com.atlassian.jira.plugin.system.customfieldtypes:radiobuttons
 * com.atlassian.jira.plugin.system.customfieldtypes:url
 * com.atlassian.jira.plugin.system.customfieldtypes:readonlyfield
 * 
 * <p>
 * TODO probably need to filter out the following field types (and maybe others from JIRA Toolkit)
 * 
 * <p>
 * com.atlassian.jira.toolkit:assigneedomain
 * com.atlassian.jira.toolkit:attachments 
 * com.atlassian.jira.toolkit:comments
 * com.atlassian.jira.toolkit:dayslastcommented
 * com.atlassian.jira.toolkit:lastusercommented
 * com.atlassian.jira.toolkit:message
 * com.atlassian.jira.toolkit:multikeyfield
 * com.atlassian.jira.toolkit:multiproject
 * com.atlassian.jira.toolkit:originalestimate
 * com.atlassian.jira.toolkit:participants
 * com.atlassian.jira.toolkit:reporterdomain
 * com.atlassian.jira.toolkit:resolveddate
 * com.atlassian.jira.toolkit:supporttools
 * com.atlassian.jira.toolkit:userproperty
 * com.atlassian.jira.toolkit:velocitymessage
 * com.atlassian.jira.toolkit:velocityviewmessage
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

	private static final String XML_DATE_FORMAT = "E, dd MMM yyyy HH:mm:ss Z (zz)"; //$NON-NLS-1$
	
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

	private static final int IN_INWARDS_LINKS = 10;

	private static final int IN_OUTWARDS_LINKS = 11;

	private static final int IN_INWARDS_ISSUE_LINK = 12;

	private static final int IN_OUTWARDS_ISSUE_LINK = 13;
	
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

	private ArrayList<Comment> currentComments = new ArrayList<Comment>();

	private ArrayList<Version> currentFixVersions = null;

	private ArrayList<Version> currentReportedVersions = null;

	private ArrayList<Component> currentComponents = null;

	private ArrayList<Attachment> currentAttachments = new ArrayList<Attachment>();

	private ArrayList<CustomField> currentCustomFields = new ArrayList<CustomField>();

	private String currentSubtaskId;

	private ArrayList<Subtask> currentSubtasks = new ArrayList<Subtask>();
	
	private String customFieldId;
	
	private String customFieldKey;

	private String customFieldName;

	private ArrayList<String> customFieldValues = new ArrayList<String>();

	private String attachmentId;

	private String attachmentName;

	private long attachmentSize;

	private String attachmentAuthor;

	private Date attachmentCreated;
	
	/**
	 * Creates a new RSS reader that will create issues from the RSS information
	 * by querying the local Jira Server for any missing information. Issues
	 * will be published to <code>collector</code> as they are read from the
	 * stream.
	 * 
	 * @param client
	 *            Jira server we are listing the issues of. This can either be a
	 *            locally cached jira server or a connection to a live instance.
	 * @param collector
	 *            Collecter that will be processing the issues as they are read
	 *            from the RSS feed.
	 * @param baseUrl the base URL of the repository
	 */
	public RssContentHandler(JiraClient client, IssueCollector collector, String baseUrl) {
		this.client = client;
		this.collector = collector;
	}

	public void startDocument() throws SAXException {
		state = START;
		currentElementText = new StringBuffer(256);
		collector.start();
	}

	public void endDocument() throws SAXException {
		if (state != START) {
			// System.err.println("Document ended abnormally");
		}
		this.collector.done();
		// remove unused buffers
		currentElementText = null;
	}

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
				currentIssue.setType(client.getIssueTypeById(attributes.getValue(ID_ATTR)));
			} else if (PRIORITY.equals(localName)) {
				currentIssue.setPriority(client.getPriorityById(attributes.getValue(ID_ATTR)));
			} else if (STATUS.equals(localName)) {
				currentIssue.setStatus(client.getStatusById(attributes.getValue(ID_ATTR)));
			} else if (ASSIGNEE.equals(localName)) {
				String assigneeName = attributes.getValue(USERNAME_ATTR);
				currentIssue.setAssignee(assigneeName);
			} else if (REPORTER.equals(localName)) {
				String reporterName = attributes.getValue(USERNAME_ATTR);
				currentIssue.setReporter(reporterName);
			} else if (RESOLUTION.equals(localName)) {
				String resolutionId = attributes.getValue(ID_ATTR);
				currentIssue.setResolution(resolutionId != null ? client.getResolutionById(resolutionId) : null);
			} else if (ORIGINAL_ESTIMATE.equals(localName)) {
				currentIssue.setInitialEstimate(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
			} else if (CURRENT_ESTIMATE.equals(localName)) {
				currentIssue.setEstimate(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
			} else if (ACTUAL.equals(localName)) {
				currentIssue.setActual(Long.parseLong(attributes.getValue(SECONDS_ATTR)));
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
			}
			break;
		case IN_ISSUE_LINK_TYPE:
			if (ISSUE_LINK_NAME.equals(localName)) {
				// TODO what do we need this for?
			} else if (INWARD_LINKS.equals(localName)) {
				state = IN_INWARDS_LINKS;
			} else if (OUTWARD_LINKS.equals(localName)) {
				state = IN_OUTWARDS_LINKS;
			}

			break;
		case IN_INWARDS_LINKS:
			if (ISSUE_LINK.equals(localName)) {
				state = IN_INWARDS_ISSUE_LINK;
			}
			break;
		case IN_OUTWARDS_LINKS:
			if (ISSUE_LINK.equals(localName)) {
				state = IN_OUTWARDS_ISSUE_LINK;
			}
			break;
		case IN_INWARDS_ISSUE_LINK:
			if (ISSUE_KEY.equals(localName)) {
				// TODO create the issue link
			}
			break;
		case IN_OUTWARDS_ISSUE_LINK:
			if (ISSUE_KEY.equals(localName)) {
				// TODO create the issue link
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
			if(CUSTOM_FIELD_VALUE.equals(localName)) {
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
			if(SUBTASK.equals(localName)) {
				currentSubtaskId = attributes.getValue(ID_ATTR);
			}
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (state) {
		case IN_SUBTASKS:
			if(SUBTASK.equals(localName)) {
				currentSubtasks.add(new Subtask(currentSubtaskId, currentElementText.toString()));
			}else if(SUBTASKS.equals(localName)) {
				state = IN_ITEM;
			}
			break;
		case IN_ATTACHMENTS:
			if (ATTACHMENTS.equals(localName)) {
				state = IN_ITEM;
			} else if (ATTACHMENT.equals(localName)) {
				Attachment attachment = new Attachment(attachmentId, attachmentName, attachmentSize, attachmentAuthor, attachmentCreated);
				currentAttachments.add(attachment);
			}
			break;

		case IN_CUSTOM_FIELD_VALUE:
			if(CUSTOM_FIELD_VALUE.equals(localName)) {
				customFieldValues.add(currentElementText.toString());
				state = IN_CUSTOM_FIELD_VALUES;
			}
			break;
		case IN_CUSTOM_FIELD_VALUES:
			if (CUSTOM_FIELD_VALUES.equals(localName)) {
				if(customFieldValues.size()==0) {
					customFieldValues.add(currentElementText.toString());
				}
				state = IN_CUSTOM_FIELD;
			}
			break;
		case IN_CUSTOM_FIELD_NAME:
			if (CUSTOM_FIELD_NAME.equals(localName)) {
				customFieldName = currentElementText.toString();
				state = IN_CUSTOM_FIELD;
			}
			break;
		case IN_CUSTOM_FIELD:
			if (CUSTOM_FIELD.equals(localName)) {
				currentCustomFields.add(new CustomField(customFieldId, customFieldKey, customFieldName, customFieldValues));
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
		case IN_OUTWARDS_ISSUE_LINK:
			if (ISSUE_LINK.equals(localName)) {
				state = IN_OUTWARDS_LINKS;
			}
			break;

		case IN_INWARDS_ISSUE_LINK:
			if (ISSUE_LINK.equals(localName)) {
				state = IN_INWARDS_LINKS;
			}
			break;

		case IN_OUTWARDS_LINKS:
			if (OUTWARD_LINKS.equals(localName)) {
				state = IN_ISSUE_LINK_TYPE;
			}
			break;

		case IN_INWARDS_LINKS:
			if (INWARD_LINKS.equals(localName)) {
				state = IN_ISSUE_LINK_TYPE;
			}
			break;

		case IN_ISSUE_LINK_TYPE:
			if (ISSUE_LINK_TYPE.equals(localName)) {
				state = IN_ISSUE_LINKS;
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
				Comment comment = new Comment(currentElementText.toString(), commentAuthor, commentLevel, commentDate);
				currentComments.add(comment);
			}
			break;
		case IN_ITEM:
			if (CHANNEL.equals(localName)) {
				state = LOOKING_FOR_CHANNEL;
			} else if (ITEM.equals(localName)) {
				if (currentReportedVersions != null) {
					currentIssue.setReportedVersions(currentReportedVersions
							.toArray(new Version[currentReportedVersions.size()]));
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
				collector.collectIssue(currentIssue);
				currentIssue = null;
				currentSubtasks.clear();
				currentCustomFields.clear();
				currentAttachments.clear();
				currentComments.clear();
				currentFixVersions = null;
				currentReportedVersions = null;
				currentComponents = null;
				state = LOOKING_FOR_ITEM;
			} else if (TITLE.equals(localName)) {

			} else if (LINK.equals(localName)) {

			} else if (DESCRIPTION.equals(localName)) {
				currentIssue.setDescription(currentElementText.toString());
			} else if (ENVIRONMENT.equals(localName)) {
				currentIssue.setEnvironment(currentElementText.toString());
			} else if (KEY.equals(localName)) {
				String key = currentElementText.toString();
				currentIssue.setKey(key);
				currentIssue.setUrl(client.getBaseUrl() + "/browse/" + key);
				// TODO super dodgey to assume the project from the issue key
				String projectKey = key.substring(0, key.indexOf('-'));
				Project project = client.getProjectByKey(projectKey);
				if (project == null) {
					throw new SAXException("No project with key '" + projectKey + "' found");
				}
				currentIssue.setProject(project);
			} else if (PARENT.equals(localName)) {
				currentIssue.setParentKey(currentElementText.toString());
			} else if (SUMMARY.equals(localName)) {
				currentIssue.setSummary(currentElementText.toString());
			} else if (CREATED.equals(localName)) {
				currentIssue.setCreated(convertToDate(currentElementText.toString()));
			} else if (UPDATED.equals(localName)) {
				currentIssue.setUpdated(convertToDate(currentElementText.toString()));
			} else if (VERSION.equals(localName)) {
				if (currentIssue.getProject() == null) {
					throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
				}
				Version version = currentIssue.getProject().getVersion(currentElementText.toString());
				if (version == null) {
					throw new SAXException("No version with name '" + currentElementText.toString() + "' found");
				}
				if (currentReportedVersions == null) {
					currentReportedVersions = new ArrayList<Version>();
				}
				currentReportedVersions.add(version);
			} else if (FIX_VERSION.equals(localName)) {
				if (currentIssue.getProject() == null) {
					throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
				}
				Version version = currentIssue.getProject().getVersion(currentElementText.toString());
				if (version == null) {
					throw new SAXException("No version with name '" + currentElementText.toString() + "' found");
				}
				if (currentFixVersions == null) {
					currentFixVersions = new ArrayList<Version>();
				}
				currentFixVersions.add(version);
			} else if (COMPONENT.equals(localName)) {
				if (currentIssue.getProject() == null) {
					throw new SAXException("Issue " + currentIssue.getId() + " does not have a valid project");
				}
				Component component = currentIssue.getProject().getComponent(currentElementText.toString());
				if (component == null) {
					throw new SAXException("No component with name '" + currentElementText.toString() + "' found");
				}
				if (currentComponents == null) {
					currentComponents = new ArrayList<Component>();
				}
				currentComponents.add(component);
			} else if (DUE.equals(localName)) {
				currentIssue.setDue(convertToDate(currentElementText.toString()));
			} else if (VOTES.equals(localName)) {
				// TODO check for number format exception
				if (currentElementText.toString().length() > 0) {
					currentIssue.setVotes(Integer.parseInt(currentElementText.toString()));
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
				//System.err.println("Unknown Issue attribute: " + localName);
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

	public void characters(char[] ch, int start, int length) throws SAXException {
		currentElementText.append(ch, start, length);
	}

	private static Date convertToDate(String value) {
		if (value == null || value.length() == 0) { 
			return null;
		}

		try {
			return new SimpleDateFormat(XML_DATE_FORMAT, Locale.US).parse(value);
		} catch (ParseException e) {
			MylarStatusHandler.log(e, "Error while parsing date string " + value);
			return null;
		}
	}
}