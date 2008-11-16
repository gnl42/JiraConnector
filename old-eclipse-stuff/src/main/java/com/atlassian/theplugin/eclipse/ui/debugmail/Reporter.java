/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.debugmail;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import org.eclipse.core.runtime.IStatus;

/**
 * Mail reporter. Should implement only mail sending (non-UI part)
 * 
 * @author Alexander Gurov
 */
public final class Reporter {
	
	private Reporter() {
		
	}
	
	public static void sendReport(IMailSettingsProvider provider,
			IStatus status, String plugin, String operationName,
			String userComment, String email, String name, String sequenceNumber)
			throws Exception {
		String messageBody = Reporter.formReport(provider, status, plugin,
				userComment, email, name, sequenceNumber);
		MailDelivery.sendMail(messageBody, operationName, sequenceNumber,
				provider);
	}

	public static void sendReport(IMailSettingsProvider provider,
			String subject, String userComment, String email, String name,
			String sequenceNumber, boolean isProblem) throws Exception {
		String messageBody = Reporter.formReport(provider, userComment, email,
				name, sequenceNumber, isProblem);
		MailDelivery.sendMailReport(messageBody, subject, sequenceNumber,
				provider);
	}

	public static boolean checkStatus(IStatus status, IStatusVisitor visitor) {
		if (!status.isMultiStatus()) {
			return visitor.visit(status);
		} else {
			IStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (Reporter.checkStatus(children[i], visitor)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getStackTrace(IStatus operationStatus) {
		final String[] stackTrace = new String[] { "" };
		Reporter.checkStatus(operationStatus, new IStatusVisitor() {

			public boolean visit(IStatus status) {
				String trace = Reporter.getOutput(status);
				stackTrace[0] += trace + "\n";
				return false;
			}

		});
		return stackTrace[0];
	}

	public static interface IStatusVisitor {
		boolean visit(IStatus status);
	}

	public static String getOutput(IStatus status) {
		Throwable t = status.getException();
		String message = "";
		if (t != null) {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			PrintWriter writer = new PrintWriter(output);
			try {
				t.printStackTrace(writer);
			} finally {
				writer.close();
			}
			message = output.toString();
		}
		return message;
	}

	public static String formReport(IMailSettingsProvider provider,
			IStatus status, String plugin, String userComment, String email,
			String name, String sequenceNumber) {
		String messageBody = Reporter.formReportHeader(provider, userComment,
				email, name, sequenceNumber)
				+ "<br><br><b>Plug-in ID:</b> " + plugin;

		String msgPlusTrace = "";
		if (status != null) {
			String[] stackTraces = Reporter.getStackTrace(status).split("\n\n");
			for (int i = 0; i < stackTraces.length; i++) {
				int idx = stackTraces[i].indexOf('\n');
				if (idx == -1) {
					msgPlusTrace += stackTraces[i] + "<br><br>";
				} else {
					msgPlusTrace += "<b>" + stackTraces[i].substring(0, idx)
							+ "</b><br>" + stackTraces[i] + "<br><br>";
				}
			}
		}
		msgPlusTrace += Reporter.getJVMProperties();

		messageBody += "<br><br>" + msgPlusTrace;

		return messageBody;
	}

	public static String formReport(IMailSettingsProvider provider,
			String userComment, String email, String name,
			String sequenceNumber, boolean isProblem) {
		String report = Reporter.formReportHeader(provider, userComment, email,
				name, sequenceNumber);
		return isProblem ? (report + "<br><br>" + Reporter.getJVMProperties())
				: report;
	}

	protected static String formReportHeader(IMailSettingsProvider provider,
			String userComment, String email, String name, String sequenceNumber) {
		String author = (name != null ? name : "")
				+ (email != null && email.trim().length() > 0 ? " &lt;" + email
						+ "&gt;" : "");
		author = author.trim().length() > 0 ? author : "<i>[not specified]</i>";
		userComment = (userComment != null && userComment.trim().length() > 0) ? userComment
				: "<i>[empty]</i>";
		return "<b>" + sequenceNumber + "</b><br><br><b>Product:</b> "
				+ provider.getPluginName() + "<br><br><b>Version:</b> "
				+ provider.getProductVersion() + "<br><br><b>From:</b> "
				+ author + "<br><br><b>User comment:</b><br>" + userComment;
	}

	protected static String getJVMProperties() {
		return "<b>JVM Properties:</b><br>"
				+ System.getProperties().toString().replace('\n', ' ')
				+ "<br><br>";
	}

}
