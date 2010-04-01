/*******************************************************************************
 * Copyright (c) 2010 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.jira.tests.ui;

import com.atlassian.connector.eclipse.internal.jira.ui.JiraSearchHandler;

import junit.framework.TestCase;

public class JiraSearchHandlerTest extends TestCase {

	public void testPrepareSearchString() {
		String stackTrace = "java.lang.NullPointerException\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run(ProgressDialog.java:219)\n"
				+ "	at org.apache.coyote.http11.Http11BaseProtocol$Http11ConnectionHandler.processConnection(Http11BaseProtocol.java:665)";
		String expectedStackTrace = "java.lang.NullPointerException AND "
				+ "at AND com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run AND ProgressDialog.java AND 219 AND "
				+ "at AND org.apache.coyote.http11.Http11BaseProtocol AND Http11ConnectionHandler.processConnection AND Http11BaseProtocol.java AND 665";
		assertEquals(expectedStackTrace, JiraSearchHandler.prepareSearchString(stackTrace));
	}

	public void testPrepareSearchStringWithDollarSign() {
		String stackTrace = "java.lang.NullPointerException\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.aboutToStart(ProgressDialog.java:123)\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run(ProgressDialog.java:219)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.updateServerData(AddOrEditFishEyeMappingDialog.java:442)";
		String expectedStackTrace = "java.lang.NullPointerException AND "
				+ "at AND com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.aboutToStart AND ProgressDialog.java AND 123 AND "
				+ "at AND com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run AND ProgressDialog.java AND 219 AND "
				+ "at AND com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.updateServerData AND AddOrEditFishEyeMappingDialog.java AND 442";
		assertEquals(expectedStackTrace, JiraSearchHandler.prepareSearchString(stackTrace));
	}

	public void testPrepareSearchStringForLongerStackTrace() {
		String stackTrace = "java.lang.NullPointerException\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.aboutToStart(ProgressDialog.java:123)\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run(ProgressDialog.java:219)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.updateServerData(AddOrEditFishEyeMappingDialog.java:442)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.access$12(AddOrEditFishEyeMappingDialog.java:433)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog$3.selectionChanged(AddOrEditFishEyeMappingDialog.java:359)\n"
				+ "	at org.eclipse.jface.viewers.Viewer$2.run(Viewer.java:162)\n"
				+ "	at org.eclipse.core.runtime.SafeRunner.run(SafeRunner.java:42)\n"
				+ "	at org.Platform.run(SomeQuiteLongFileWithPlatformClassAndMaybeSomethingElse.java:888)\n"
				+ "	at org.eclipse.ui.internal.JFaceUtil$1.run(JFaceUtil.java:48)\n"
				+ "	at org.eclipse.jface.util.SafeRunnable.run(SafeRunnable.java:175)\n"
				+ "	at org.eclipse.jface.viewers.Viewer.fireSelectionChanged(Viewer.java:160)\n"
				+ "	at org.eclipse.jface.viewers.StructuredViewer.updateSelection(StructuredViewer.java:2132)\n"
				+ "	at org.eclipse.jface.viewers.StructuredViewer.setSelection(StructuredViewer.java:1669)\n"
				+ "	at org.eclipse.jface.viewers.Viewer.setSelection(Viewer.java:392)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.createPageControls(AddOrEditFishEyeMappingDialog.java:401)\n"
				+ "	at com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.createDialogArea(ProgressDialog.java:75)\n"
				+ "	at org.eclipse.jface.dialogs.TitleAreaDialog.createContents(TitleAreaDialog.java:147)\n"
				+ "	at org.eclipse.jface.window.Window.create(Window.java:431)\n"
				+ "	at org.eclipse.jface.dialogs.Dialog.create(Dialog.java:1089)\n"
				+ "	at org.eclipse.jface.window.Window.open(Window.java:790)\n"
				+ "	at com.atlassian.connector.eclipse.fisheye.ui.preferences.SourceRepositoryMappingPreferencePage$1.run(SourceRepositoryMappingPreferencePage.java:73)\n"
				+ "	at org.eclipse.swt.widgets.RunnableLock.run(RunnableLock.java:35)\n"
				+ "	at org.eclipse.swt.widgets.Synchronizer.runAsyncMessages(Synchronizer.java:134)\n"
				+ "	at org.eclipse.swt.widgets.Display.runAsyncMessages(Display.java:3468)\n"
				+ "	at org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3115)\n"
				+ "	at org.eclipse.jface.window.Window.runEventLoop(Window.java:825)\n"
				+ "	at org.eclipse.jface.window.Window.open(Window.java:801)\n"
				+ "	at org.eclipse.ui.internal.dialogs.WorkbenchPreferenceDialog.open(WorkbenchPreferenceDialog.java:211)\n"
				+ "	at com.atlassian.connector.eclipse.internal.crucible.ui.wizards.DefineRepositoryMappingButton$1.widgetSelected(DefineRepositoryMappingButton.java:71)\n"
				+ "	at org.eclipse.swt.widgets.TypedListener.handleEvent(TypedListener.java:228)\n"
				+ "	at org.eclipse.swt.widgets.EventTable.sendEvent(EventTable.java:84)\n"
				+ "	at org.eclipse.swt.widgets.Widget.sendEvent(Widget.java:1176)\n"
				+ "	at org.eclipse.swt.widgets.Display.runDeferredEvents(Display.java:3493)\n"
				+ "	at org.eclipse.swt.widgets.Display.readAndDispatch(Display.java:3112)\n"
				+ "	at org.eclipse.jface.window.Window.runEventLoop(Window.java:825)\n"
				+ "	at org.eclipse.jface.window.Window.open(Window.java:801)";

		final String expectedQuery = "java.lang.NullPointerException AND at AND com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.aboutToStart AND ProgressDialog.java AND 123"
				+ " AND at AND com.atlassian.connector.eclipse.ui.dialogs.ProgressDialog.run AND ProgressDialog.java AND 219"
				+ " AND at AND com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.updateServerData AND AddOrEditFishEyeMappingDialog.java AND 442"
				+ " AND at AND com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog.access AND 12 AND AddOrEditFishEyeMappingDialog.java AND 433"
				+ " AND at AND com.atlassian.connector.eclipse.fisheye.ui.preferences.AddOrEditFishEyeMappingDialog AND 3.selectionChanged AND AddOrEditFishEyeMappingDialog.java AND 359"
				+ " AND at AND org.eclipse.jface.viewers.Viewer AND 2.run AND Viewer.java AND 162 AND at AND org.eclipse.core.runtime.SafeRunner.run AND SafeRunner.java AND 42"
				+ " AND at AND org.Platform.run AND SomeQuiteLongFileWithPlatform*";
		assertEquals(expectedQuery, JiraSearchHandler.prepareSearchString(stackTrace));
	}

}
