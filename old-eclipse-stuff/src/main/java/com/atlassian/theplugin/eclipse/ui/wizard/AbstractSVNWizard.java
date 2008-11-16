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

package com.atlassian.theplugin.eclipse.ui.wizard;

import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Wizard implementation that allows us to hide progress monitor part if it is not needed by wizard it self.
 * 
 * @author Alexander Gurov
 */
public abstract class AbstractSVNWizard extends Wizard {

	public AbstractSVNWizard() {
		super();
	}

	public void createPageControls(Composite pageContainer) {
		if (!this.needsProgressMonitor()) {
			ProgressMonitorPart part = this.findProgressMonitorPart(pageContainer);
			if (part != null) {
				GridData data = new GridData();
				data.heightHint = 0;
				part.setLayoutData(data);
			}
		}
		super.createPageControls(pageContainer);
	}
	
	protected ProgressMonitorPart findProgressMonitorPart(Composite container) {
		if (container == null) {
			return null;
		}
		Control []children = container.getChildren();
		for (int i = 0; i < children.length; i++) {
			if (children[i] instanceof ProgressMonitorPart) {
				return (ProgressMonitorPart)children[i];
			}
		}
		return this.findProgressMonitorPart(container.getParent());
	}
	
}
