/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Lean Findlater - initial API and implementation
 *******************************************************************************/

package com.atlassian.connector.eclipse.ui.internal.monitor;

import org.eclipse.mylyn.internal.monitor.ui.MonitorUiPlugin;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PerspectiveAdapter;

/**
 * @author Leah Findlater
 * @author Mik Kersten
 */
public class TaskEditorMonitor extends PerspectiveAdapter {

	public static final String PERSPECTIVE_CHANGED = "perspective changed"; //$NON-NLS-1$

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {

		if (partRef == null || !(partRef instanceof IEditorReference)) {
			return;
		}

		IEditorReference er = (IEditorReference) partRef;
		IEditorInput ei = null;
		try {
			ei = er.getEditorInput();
		} catch (PartInitException e) {
			return;
		}

		if (!(ei instanceof TaskEditorInput)) {
			return;
		}

		TaskEditorInput tei = (TaskEditorInput) ei;

		String source = partRef.getId();
		InteractionEvent interactionEvent = new InteractionEvent(InteractionEvent.Kind.PREFERENCE,
				TaskEditorInput.class.getName(), tei.getTask().getHandleIdentifier(), source, "null", changeId + ": "
						+ tei.getTask().getConnectorKind(), 1);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}
}

/* Perspective listener methods */

// TODO Should we comment out the more detailed perspective listener methods and
// just use this one instead? This one logs the open set of views and editors
// whenever that changes.
/*
 * @Override public void perspectiveChanged(IWorkbenchPage page,
 * IPerspectiveDescriptor perspective, String changeId) {
 * super.perspectiveChanged(page, perspective, changeId);
 * 
 * if(changeId.startsWith("view") || changeId.startsWith("editor")) {
 * IWorkbenchPage workbenchPage =
 * PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
 * IViewReference[] viewList = workbenchPage.getViewReferences();
 * IEditorReference[] editorList = workbenchPage.getEditorReferences();
 * 
 * String delta = ""; for(int i = 0; i < viewList.length; i++) { delta = delta +
 * viewList[i].getTitle() + ","; } delta = delta + "Editor (" +
 * editorList.length + " open)";
 * 
 * String source = "perspective." + perspective.getLabel(); InteractionEvent
 * interactionEvent = new InteractionEvent( source, delta );
 * logger.interactionObserved(interactionEvent); } }
 */
