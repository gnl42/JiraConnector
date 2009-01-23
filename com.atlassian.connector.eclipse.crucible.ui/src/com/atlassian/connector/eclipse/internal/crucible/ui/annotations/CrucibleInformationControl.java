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

import org.eclipse.jface.internal.text.html.HTMLTextPresenter;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * A custom control to display on hover, or delegates to the default control to display if we aren't dealing with a
 * CrucibleCommentAnnotation.
 * 
 * @author sminto
 */
public class CrucibleInformationControl extends DefaultInformationControl implements IInformationControlExtension2 {

	private Object input;

	private final Shell shell;

	@SuppressWarnings("restriction")
	public CrucibleInformationControl(Shell parent) {
		super(parent, SWT.NONE, new HTMLTextPresenter(true));
		this.shell = parent;
	}

	@Override
	public void setInformation(String content) {
		this.input = content;
		super.setInformation(content);
	}

	public void setInput(Object input) {
		this.input = input;
	}

	@Override
	public boolean hasContents() {
		return input != null || super.hasContents();
	}

	@Override
	public void setVisible(boolean visible) {
		if (input instanceof String) {
			setInformation((String) input);
			super.setVisible(visible);
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			// TODO display stuff properly
			super.setVisible(visible);
		} else {
			super.setVisible(visible);
		}
	}

	@Override
	public Point computeSizeHint() {
		if (input instanceof String) {
			setInformation((String) input);
			return super.computeSizeHint();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			// TODO compute the size properly
			return super.computeSizeHint();
		} else {
			return super.computeSizeHint();
		}

	}
}
