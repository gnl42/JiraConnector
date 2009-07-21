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

package org.eclipse.mylyn.internal.monitor.ui;

import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 * @author Leah Findlater
 * @author Mik Kersten
 */
public class PerspectiveChangeMonitor extends PerspectiveAdapter {

	public static final String PERSPECTIVE_SAVED = "perspective saved"; //$NON-NLS-1$

	public static final String PERSPECTIVE_OPENED = "perspective opened"; //$NON-NLS-1$

	public static final String PERSPECTIVE_CLOSED = "perspective closed"; //$NON-NLS-1$

	public static final String PERSPECTIVE_CHANGED = "perspective changed"; //$NON-NLS-1$

	public static final String PERSPECTIVE_ACTIVATED = "perspective activated"; //$NON-NLS-1$

	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);

		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_ACTIVATED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		if (partRef != null) {
			String source = partRef.getId();
			InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CHANGED + ": " //$NON-NLS-1$
					+ changeId);
			MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
		}
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CHANGED + ": " //$NON-NLS-1$
				+ changeId);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_CLOSED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		String source = this.getPerspectiveId(perspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_OPENED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	@Override
	public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		String source = this.getPerspectiveId(newPerspective);
		InteractionEvent interactionEvent = InteractionEvent.makePreference(source, PERSPECTIVE_SAVED);
		MonitorUiPlugin.getDefault().notifyInteractionObserved(interactionEvent);
	}

	private String getPerspectiveId(IPerspectiveDescriptor perspective) {
		String id;

		if (perspective instanceof PerspectiveDescriptor) {
			String originalId = ((PerspectiveDescriptor) perspective).getOriginalId();
			if (!originalId.equals(perspective.getId())) {
				id = originalId + "[customized]"; //$NON-NLS-1$
			} else {
				id = perspective.getId();
			}
		} else {
			id = perspective.getId();
		}
		return id;
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
