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
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * A custom control to display on hover, or delegates to the default control to display if we aren't dealing with a
 * CrucibleCommentAnnotation.
 * 
 * @author sminto
 */
public class CrucibleInformationControl extends DefaultInformationControl implements IInformationControlExtension2 {

	private Object input;

	private CrucibleCommentPopupDialog commentPopupDialog;

	private final CrucibleInformationControlCreator informationControlCreator;

	@SuppressWarnings("restriction")
	public CrucibleInformationControl(Shell parent, CrucibleInformationControlCreator crucibleInformationControlCreator) {
		super(parent, new HTMLTextPresenter(true));
//		super(parent, true);
		this.informationControlCreator = crucibleInformationControlCreator;
		commentPopupDialog = new CrucibleCommentPopupDialog(parent, SWT.NO_FOCUS | SWT.ON_TOP);
		// Force create early so that listeners can be added at all times with API.
		commentPopupDialog.create();
		commentPopupDialog.setInformationControl(this);
	}

	public InformationPresenter getInformationPresenter() {
		return new InformationPresenter(informationControlCreator);
	}

	@Override
	public void setInformation(String content) {
		this.input = content;
		commentPopupDialog.setInput(input);
		super.setInformation(content);
	}

	public void setInput(Object input) {
		this.input = input;
		commentPopupDialog.setInput(input);
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
			if (visible) {
				commentPopupDialog.open();
			} else {
				commentPopupDialog.getShell().setVisible(false);
			}
		} else {
			super.setVisible(visible);
		}
	}

	@Override
	public void dispose() {
		commentPopupDialog.dispose();
		commentPopupDialog = null;
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
		commentPopupDialog.setSize(width, height);
	}

	@Override
	public void setLocation(Point location) {
		super.setLocation(location);
		commentPopupDialog.setLocation(location);
	}

	@Override
	public void setSizeConstraints(int maxWidth, int maxHeight) {
		super.setSizeConstraints(maxWidth, maxHeight);
		commentPopupDialog.setSizeConstraints(maxWidth, maxHeight);
	}

	@Override
	public Rectangle computeTrim() {
		if (input instanceof String) {
			return super.computeTrim();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			return commentPopupDialog.computeTrim();
		} else {
			return super.computeTrim();
		}

	}

	@Override
	public Rectangle getBounds() {
		if (input instanceof String) {
			return super.getBounds();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			return commentPopupDialog.getBounds();
		} else {
			return super.getBounds();
		}
	}

	@Override
	public void addDisposeListener(DisposeListener listener) {
		super.addDisposeListener(listener);
		commentPopupDialog.addDisposeListener(listener);
	}

	@Override
	public void removeDisposeListener(DisposeListener listener) {
		super.removeDisposeListener(listener);
		commentPopupDialog.removeDisposeListener(listener);
	}

	@Override
	public void setForegroundColor(Color foreground) {
		super.setForegroundColor(foreground);
	}

	@Override
	public void setBackgroundColor(Color background) {
		super.setBackgroundColor(background);
	}

	@Override
	public boolean isFocusControl() {
		if (input instanceof String) {
			return super.isFocusControl();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			return commentPopupDialog.isFocusControl();
		} else {
			return super.isFocusControl();
		}

	}

	@Override
	public void setFocus() {

		if (input instanceof String) {
			super.setFocus();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			commentPopupDialog.setFocus();
		} else {
			super.setFocus();
		}
	}

	@Override
	public void addFocusListener(FocusListener listener) {
		super.addFocusListener(listener);
		commentPopupDialog.addFocusListener(listener);
	}

	@Override
	public void removeFocusListener(FocusListener listener) {
		super.removeFocusListener(listener);
		commentPopupDialog.removeFocusListener(listener);
	}

	@Override
	public Point computeSizeHint() {
		if (input instanceof String) {
			setInformation((String) input);
			return super.computeSizeHint();
		} else if (input instanceof CrucibleAnnotationHoverInput) {
			return commentPopupDialog.computeSizeHint();
		} else {
			return super.computeSizeHint();
		}

	}

	public IInformationControlCreator getInformationPresenterControlCreator() {
		return new CrucibleInformationControlCreator();
	}

}
