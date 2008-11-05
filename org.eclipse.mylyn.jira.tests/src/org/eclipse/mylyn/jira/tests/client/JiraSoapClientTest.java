/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.jira.tests.client;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import junit.framework.TestCase;

import org.eclipse.mylyn.commons.net.WebLocation;
import org.eclipse.mylyn.context.tests.support.TestUtil;
import org.eclipse.mylyn.context.tests.support.TestUtil.Credentials;
import org.eclipse.mylyn.context.tests.support.TestUtil.PrivilegeLevel;
import org.eclipse.mylyn.internal.jira.core.model.JiraIssue;
import org.eclipse.mylyn.internal.jira.core.service.JiraClient;
import org.eclipse.mylyn.internal.jira.core.service.soap.JiraSoapClient;
import org.eclipse.mylyn.internal.jira.core.wsdl.beans.RemoteWorklog;
import org.eclipse.mylyn.internal.jira.core.wsdl.soap.JiraSoapService;
import org.eclipse.mylyn.jira.tests.util.JiraTestConstants;
import org.eclipse.mylyn.jira.tests.util.JiraTestUtil;

/**
 * @author Steffen Pingel
 */
public class JiraSoapClientTest extends TestCase {

	private JiraClient client;

	private JiraSoapClient soapClient;

	private JiraSoapService soapService;

	protected void init(String url, PrivilegeLevel level) throws Exception {
		Credentials credentials = TestUtil.readCredentials(level);
		client = new JiraClient(new WebLocation(url, credentials.username, credentials.password));
		soapClient = client.getSoapClient();
		soapService = soapClient.getSoapService();

		JiraTestUtil.refreshDetails(client);
	}

//	@Override
//	protected void tearDown() throws Exception {
//		JiraTestUtil.tearDown();
//	}

	public void testGetWorklogs() throws Exception {
		init(JiraTestConstants.JIRA_LATEST_URL, PrivilegeLevel.ADMIN);

		JiraIssue issue = JiraTestUtil.createIssue(client, "getWorklogs");
		System.err.println(issue.getUrl());

		String session = soapClient.login(null);
		RemoteWorklog log = new RemoteWorklog();
		log.setComment("abc");
		log.setStartDate(createDate("2008-01-01 10:00"));
		log.setCreated(createDate("2008-01-01 11:00"));
		log.setUpdated(createDate("2008-01-01 12:00"));
		//log.setTimeSpentInSeconds(5 * 60);
		log.setTimeSpent("5m");
		RemoteWorklog newLog = soapService.addWorklogAndAutoAdjustRemainingEstimate(session, issue.getKey(), log);
		dumpt(newLog);
		System.err.println(soapService.hasPermissionToCreateWorklog(session, issue.getKey()));
		System.err.println(soapService.hasPermissionToDeleteWorklog(session, newLog.getId()));
		soapService.deleteWorklogAndAutoAdjustRemainingEstimate(session, newLog.getId());

	}

	private void dumpt(RemoteWorklog newLog) {
		System.err.println(newLog.getAuthor());
		System.err.println(newLog.getGroupLevel());
		System.err.println(newLog.getComment());
		System.err.println(newLog.getRoleLevelId());
		System.err.println(newLog.getUpdateAuthor());
		System.err.println(newLog.getStartDate().getTime());
		System.err.println(newLog.getCreated().getTime());
		System.err.println(newLog.getUpdated().getTime());
		System.err.println(newLog.getTimeSpentInSeconds());
	}

	private Calendar createDate(String string) throws Exception {
		DateFormat format = new SimpleDateFormat("yyyy-mm-dd HH:mm");
		Calendar cal = Calendar.getInstance();
		cal.setTime(format.parse(string));
		return cal;
	}

}
