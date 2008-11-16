/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sergiy Logvin - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.debugmail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.atlassian.theplugin.eclipse.util.PatternProvider;

/**
 * E-mail sender
 *
 * @author Sergiy Logvin
 */
public class MailDelivery {
	
	public static boolean sendMail(String to, String from, String subject,
            String body, String host, String port) throws Exception {
		
		if (host.length() == 0) {
			return true;
		}
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.smtp.auth", "false");
		
		Session session = Session.getDefaultInstance(props, null);
		session.setDebug(false);
		
		MimeMessage msg = new MimeMessage(session);
		msg.setFrom(new InternetAddress(from));
		msg.setRecipients(Message.RecipientType.TO, new InternetAddress[] {new InternetAddress(to)});
		msg.setSubject(subject);
		msg.setSentDate(new java.util.Date());
		msg.setText(body);
		String encoding = System.getProperty("file.encoding");
		encoding = (encoding == null ? "" : ("; charset=" + encoding));
		msg.setHeader("content-type", "text/html" + encoding);
		Transport.send(msg);
		
		return true;
	}
	
	public static boolean sendMail(String messageBody, String operationName, String reportId, IMailSettingsProvider settingsProvider) throws Exception {
		String to = settingsProvider.getEmailTo();;
		String from = settingsProvider.getEmailFrom();
		String subject = "[" + settingsProvider.getPluginName() + "] " + reportId + " - " + operationName + " Operation Failure Report";
		String host = settingsProvider.getHost();
		String port = settingsProvider.getPort();
		return MailDelivery.sendMail(to, from, subject, PatternProvider.replaceAll(messageBody, "\n", "<br>"), host, port);
	}

	public static boolean sendMailReport(String messageBody, String subject, String reportId, IMailSettingsProvider settingsProvider) throws Exception {
		String to = settingsProvider.getEmailTo();;
		String from = settingsProvider.getEmailFrom();
		subject = "[" + settingsProvider.getPluginName() + "] " + reportId + " - " + subject;
		String host = settingsProvider.getHost();
		String port = settingsProvider.getPort();
		return MailDelivery.sendMail(to, from, subject, PatternProvider.replaceAll(messageBody, "\n", "<br>"), host, port);
	}

}
