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

import com.atlassian.connector.eclipse.internal.crucible.ui.IReviewActionListener;
import com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts.VersionedCommentPart;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Popup to show the information about the annotation in
 * 
 * @author Shawn Minto
 */
public class CrucibleCommentPopupDialog extends PopupDialog implements IReviewActionListener {

	private static final int MAX_WIDTH = 500;

	private int maxWidth;

	private CrucibleAnnotationHoverInput annotationInput;

	private FormToolkit toolkit;

	private Composite composite;

	private Label focusLabel;

	private ScrolledComposite scrolledComposite;

	private CrucibleInformationControl informationControl;

	private static CrucibleCommentPopupDialog currentPopupDialog;

	@SuppressWarnings("deprecation")
	public CrucibleCommentPopupDialog(Shell parent, int shellStyle) {
		//TODO e3.4
		super(parent, shellStyle, false, false, false, false, null, null);
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		if (toolkit == null) {
			toolkit = new FormToolkit(getShell().getDisplay());
		}

		scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		toolkit.adapt(scrolledComposite);

		composite = toolkit.createComposite(scrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());
		scrolledComposite.setContent(composite);

		parent.setBackground(toolkit.getColors().getBackground());
		getShell().setBackground(toolkit.getColors().getBackground());

		return composite;
	}

	public void dispose() {
		currentPopupDialog = null;
		close();
		toolkit.dispose();
	}

	public void setFocus() {
		getShell().forceFocus();

		if (focusLabel != null) {
			focusLabel.dispose();
		}

		if (composite.getChildren().length > 0) {
			composite.getChildren()[0].setFocus();
		}

		Point computeSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (computeSize.y > scrolledComposite.getSize().y) {
			scrolledComposite.setExpandVertical(false);
			composite.setSize(computeSize);
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
		//return composite.isFocusControl();
		return getShell().getDisplay().getActiveShell() == getShell();
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
		Point computeSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		if (computeSize.x > width) {
			width = computeSize.x;
		}
		getShell().setSize(width, height);
		scrolledComposite.setSize(width, height);
	}

	public void setInput(Object input) {
		if (input instanceof CrucibleAnnotationHoverInput) {
			this.annotationInput = (CrucibleAnnotationHoverInput) input;

			// clear the composite in case we are re-using it
			for (Control control : composite.getChildren()) {
				control.dispose();
			}

			currentPopupDialog = this;
			for (CrucibleCommentAnnotation annotation : annotationInput.getCrucibleAnnotations()) {
				VersionedCommentPart part = new VersionedCommentPart(annotation.getVersionedComment(),
						annotation.getReview(), annotation.getCrucibleFileInfo());

				part.hookCustomActionRunListener(this);

				toolkit.adapt(part.createControl(composite, toolkit), true, true);
				toolkit.adapt(composite);
				toolkit.adapt(scrolledComposite);
				toolkit.adapt(scrolledComposite.getParent());

				getShell().setBackground(toolkit.getColors().getBackground());
			}
			focusLabel = toolkit.createLabel(composite, "Press 'F2' for focus.");
			focusLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
			GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).applyTo(focusLabel);
		} else {
			input = null;
		}

	}

	public void actionAboutToRun(Action action) {
		close();
	}

	public void actionRan(Action action) {
		close();
	}

	public static CrucibleCommentPopupDialog getCurrentPopupDialog() {
		return currentPopupDialog;
	}

	public void setInformationControl(CrucibleInformationControl crucibleInformationControl) {
		this.informationControl = crucibleInformationControl;
	}

	public CrucibleInformationControl getInformationControl() {
		return informationControl;
	}
}
