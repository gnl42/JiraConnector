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

package com.atlassian.connector.eclipse.internal.crucible.ui.editor.parts;

import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleImages;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiPlugin;
import com.atlassian.connector.eclipse.internal.crucible.ui.CrucibleUiUtil;
import com.atlassian.connector.eclipse.internal.crucible.ui.AvatarImages.AvatarSize;
import com.atlassian.theplugin.commons.crucible.api.model.Reviewer;
import com.atlassian.theplugin.commons.crucible.api.model.User;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.jetbrains.annotations.Nullable;
import java.util.Iterator;
import java.util.Set;

/**
 * Form part that displays the details of a reviewer
 * 
 * @author Thomas Ehrnhoefer
 */
public class CrucibleReviewersListPart {

	public static Label createLabel(FormToolkit toolkit, Composite parent, String labelText) {
		Label reviewersLabel = createLabelControl(toolkit, parent, labelText);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.TOP).applyTo(reviewersLabel);
		return reviewersLabel;
	}

	public static Control createControl(FormToolkit toolkit, Composite parent, Set<Reviewer> reviewers,
			ImageRegistry imageRegistry, @Nullable Menu menu) {
		// CHECKSTYLE:MAGIC:OFF

		// final Composite reviewersPartComposite = createComposite(toolkit, parent);
		// reviewersPartComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(3).create());

		if (reviewers.isEmpty()) {
			// avoid blank gap on Linux
			return createLabelControl(toolkit, parent, " ");
		} else {
			Composite reviewersComposite = createComposite(toolkit, parent, menu);

			RowLayout layout = new RowLayout();
			layout.marginBottom = 0;
			layout.marginTop = 0;
			layout.marginRight = 0;
			layout.marginLeft = 0;
			layout.marginWidth = 0;
			layout.spacing = 0;
			layout.wrap = true;
			layout.fill = true;
			reviewersComposite.setLayout(layout);

			Iterator<Reviewer> iterator = reviewers.iterator();
			while (iterator.hasNext()) {
				final Reviewer reviewer = iterator.next();
				createParticipantComposite(toolkit, reviewersComposite, reviewer, reviewer.isCompleted(), iterator.hasNext(),
						imageRegistry);
				// final Composite singleReviewersComposite = createComposite(toolkit, reviewersComposite);
				// singleReviewersComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(
				// reviewer.isCompleted() ? 3 : 3).spacing(0, 0).margins(0, 0).create());
				//
				// final AvatarImages avatarsCache = CrucibleUiPlugin.getDefault().getAvatarsCache();
				// synchronized (avatarsCache) {
				// Image avatar = avatarsCache.getAvatar(reviewer, AvatarSize.LARGE);
				// if (avatar == null) {
				// avatar = CrucibleImages.getImage(CrucibleImages.DEFAULT_AVATAR_LARGE);
				// }
				//
				// if (reviewer.isCompleted()) {
				// Image overlayedAvatar = imageRegistry.get(reviewer.getUsername());
				// if (overlayedAvatar == null) {
				// overlayedAvatar = new DecorationOverlayIcon(avatar, CrucibleImages.REVIEWER_COMPLETE,
				// IDecoration.BOTTOM_RIGHT).createImage();
				// imageRegistry.put(reviewer.getUsername(), overlayedAvatar);
				// }
				// avatar = overlayedAvatar;
				// }
				//
				// final Label imageLabel = createLabelControl(toolkit, singleReviewersComposite, "doesnotmatter");
				// imageLabel.setImage(avatar);
				// GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(2, 0).applyTo(imageLabel);
				// }
				//
				// Text text = createReadOnlyText(toolkit, singleReviewersComposite,
				// CrucibleUiUtil.getDisplayNameOrUsername(reviewer), null, false);
				// GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(text);
				// text.setBackground(parent.getBackground());
				//
				// // if (reviewer.isCompleted()) {
				// // Label imageLabel = createLabelControl(toolkit, singleReviewersComposite, "doesnotmatter");
				// // imageLabel.setImage(CrucibleImages.getImage(CrucibleImages.REVIEWER_COMPLETE));
				// // GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(2, 0).applyTo(imageLabel);
				// // }
				//
				// if (iterator.hasNext()) {
				// Label label = createLabelControl(toolkit, singleReviewersComposite, ", ");
				// GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
				// label.setBackground(parent.getBackground());
				// }

			}
			GridDataFactory.fillDefaults().hint(250, SWT.DEFAULT).grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(
					reviewersComposite);
			return reviewersComposite;
		}
	}

	public static Composite createParticipantComposite(FormToolkit toolkit, Composite parent, User reviewer,
			boolean isCompleted, boolean hasComa, ImageRegistry imageRegistry) {

		final Composite singleReviewersComposite = createComposite(toolkit, parent, null);

		singleReviewersComposite.setLayout(GridLayoutFactory.fillDefaults().numColumns(
				hasComa ? 3 : 2).spacing(0, 0).margins(0, 0).create());

		final AvatarImages avatarsCache = CrucibleUiPlugin.getDefault().getAvatarsCache();
		synchronized (avatarsCache) {
			Image avatar = avatarsCache.getAvatar(reviewer, AvatarSize.LARGE);
			if (avatar == null) {
				avatar = CrucibleImages.getImage(CrucibleImages.DEFAULT_AVATAR_LARGE);
			}

			if (isCompleted) {
				Image overlayedAvatar = imageRegistry.get(reviewer.getUsername());
				if (overlayedAvatar == null) {
					overlayedAvatar = new DecorationOverlayIcon(avatar, CrucibleImages.REVIEWER_COMPLETE,
							IDecoration.BOTTOM_RIGHT).createImage();
					imageRegistry.put(reviewer.getUsername(), overlayedAvatar);
				}
				avatar = overlayedAvatar;
			}

			final Label imageLabel = createLabelControl(toolkit, singleReviewersComposite, "doesnotmatter");
			imageLabel.setImage(avatar);
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).indent(2, 0).applyTo(imageLabel);
		}

		Text text = createReadOnlyText(toolkit, singleReviewersComposite,
				CrucibleUiUtil.getDisplayNameOrUsername(reviewer), null, false);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(text);
		text.setBackground(parent.getBackground());

		if (hasComa) {
			Label label = createLabelControl(toolkit, singleReviewersComposite, ", ");
			GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
			label.setBackground(parent.getBackground());
		}
		return singleReviewersComposite;
	}

	private static Composite createComposite(FormToolkit toolkit, Composite parent, Menu menu) {
		final Composite composite = new Composite(parent, SWT.NONE);
		if (toolkit != null) {
			toolkit.adapt(composite);
		}
		if (menu != null) {
			CommonUiUtil.setMenu(composite, menu);
		}
		return composite;
	}

	private static Text createReadOnlyText(FormToolkit toolkit, Composite composite, String value, String labelString,
			boolean isMultiline) {

		if (labelString != null) {
			createLabelControl(toolkit, composite, labelString);
		}
		int style = SWT.FLAT | SWT.READ_ONLY;
		if (isMultiline) {
			style |= SWT.MULTI | SWT.WRAP;
		} else {
			style |= SWT.SINGLE;
		}
		Text text = new Text(composite, style | SWT.MULTI);
		text.setFont(JFaceResources.getDefaultFont());
		text.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
		text.setText(value);

		if (toolkit != null) {
			toolkit.adapt(text, true, true);
		}

		return text;
	}

	private static Label createLabelControl(FormToolkit toolkit, Composite composite, String labelString) {

		Label labelControl = null;
		if (toolkit != null) {
			labelControl = toolkit.createLabel(composite, labelString);
			labelControl.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		} else {
			labelControl = new Label(composite, SWT.NONE);
			labelControl.setText(labelString);
		}

		return labelControl;
	}

}
