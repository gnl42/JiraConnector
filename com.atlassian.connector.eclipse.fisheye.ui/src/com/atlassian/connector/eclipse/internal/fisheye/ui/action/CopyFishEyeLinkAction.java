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

package com.atlassian.connector.eclipse.internal.fisheye.ui.action;


import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class CopyFishEyeLinkAction extends AbstractFishEyeLinkAction implements IWorkbenchWindowActionDelegate {

	public CopyFishEyeLinkAction() {
		super("Copy FishEye Link to Clipboard");
	}

	protected void processUrl(String url) {
		final Clipboard clipboard = new Clipboard(Display.getDefault());
		TextTransfer textTransfer = TextTransfer.getInstance();
		clipboard.setContents(new Object[] { url }, new Transfer[] { textTransfer });
	}

}
