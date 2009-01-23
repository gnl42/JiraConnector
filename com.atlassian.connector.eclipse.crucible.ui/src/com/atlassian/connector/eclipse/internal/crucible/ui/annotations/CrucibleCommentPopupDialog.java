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

import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.VersionedCommentPart;

import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Popup to show the information about the annotation in
 * 
 * @author Shawn Minto
 */
public class CrucibleCommentPopupDialog extends PopupDialog {

	private static final int MAX_WIDTH = 500;

	private int maxWidth;

	private CrucibleAnnotationHoverInput annotationInput;

	private FormToolkit toolkit;

	private Composite composite;

	public CrucibleCommentPopupDialog(Shell parent, int shellStyle) {
		super(parent, shellStyle, false, false, false, false, null, null);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		if (toolkit == null) {
			toolkit = new FormToolkit(getShell().getDisplay());
		}

		composite = toolkit.createComposite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());

		parent.setBackground(toolkit.getColors().getBackground());
		getShell().setBackground(toolkit.getColors().getBackground());

		return composite;
	}

	public void dispose() {
		close();
		toolkit.dispose();
	}

	public void setFocus() {
		getShell().forceFocus();
		if (composite.getChildren().length > 0) {
			composite.getChildren()[0].setFocus();
		}
	}

	public Point computeSizeHint() {
		int widthHint = MAX_WIDTH;
		if (maxWidth < widthHint) {
			widthHint = maxWidth;
		}

		return getShell().computeSize(widthHint, SWT.DEFAULT, true);
	}

	public void removeFocusListener(FocusListener listener) {
		composite.removeFocusListener(listener);
	}

	public void addFocusListener(FocusListener listener) {
		composite.addFocusListener(listener);

	}

	public boolean isFocusControl() {
		return composite.isFocusControl();
	}

	public void removeDisposeListener(DisposeListener listener) {
		getShell().removeDisposeListener(listener);

	}

	public void addDisposeListener(DisposeListener listener) {
		getShell().addDisposeListener(listener);
	}

	public Rectangle getBounds() {
		return getShell().getBounds();
	}

	public Rectangle computeTrim() {
		return getShell().computeTrim(0, 0, 0, 0);
	}

	public void setSizeConstraints(int newMaxWidth, int newMaxHeight) {
		this.maxWidth = newMaxWidth;
	}

	public void setLocation(Point location) {
		getShell().setLocation(location);
	}

	public void setSize(int width, int height) {
		getShell().setSize(width, height);
	}

	public void setInput(Object input) {
		if (input instanceof CrucibleAnnotationHoverInput) {
			this.annotationInput = (CrucibleAnnotationHoverInput) input;

			// clear the composite in case we are re-using it
			for (Control control : composite.getChildren()) {
				control.dispose();
			}

			for (CrucibleCommentAnnotation annotation : annotationInput.getCrucibleAnnotations()) {
				VersionedCommentPart part = new VersionedCommentPart(annotation.getVersionedComment(),
						annotation.getCrucibleFileInfo(), null);
				toolkit.adapt(part.createControl(composite, toolkit), true, true);
				toolkit.adapt(composite);
			}
		} else {
			input = null;
		}

	}
}
