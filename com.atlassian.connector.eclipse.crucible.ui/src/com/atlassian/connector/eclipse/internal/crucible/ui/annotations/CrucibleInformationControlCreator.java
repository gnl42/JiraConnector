/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.crucible.ui.annotations;

import com.atlassian.connector.eclipse.internal.crucible.ui.FocusTooltipHandler;

import org.eclipse.core.commands.Command;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;

/**
 * The class that will created the information control for the annotation
 * 
 * @author Shawn Minto
 */
public class CrucibleInformationControlCreator implements IInformationControlCreator {

	public IInformationControl createInformationControl(Shell parent) {
		//make sure edit text context is active (for F2 to work)
		IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
		contextService.activateContext("org.eclipse.ui.textEditorScope");
		//add our own handler as F2 focus handler
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		Command showInfoCommand = commandService.getCommand(ITextEditorActionDefinitionIds.SHOW_INFORMATION);
		showInfoCommand.setHandler(new FocusTooltipHandler());
		return new CrucibleInformationControl(parent, this);
	}
}