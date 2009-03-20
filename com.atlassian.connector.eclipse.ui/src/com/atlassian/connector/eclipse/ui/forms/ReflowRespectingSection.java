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

package com.atlassian.connector.eclipse.ui.forms;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * A section that respects when the reflow is set to false on a part. This should be used for performance reasons when
 * there are many sections used
 * 
 * @author Shawn Minto
 */
public class ReflowRespectingSection extends Section {

	private final IReflowRespectingPart reflowPart;

	private final Font boldFont;

	public ReflowRespectingSection(FormToolkit toolkit, Composite parent, int style, IReflowRespectingPart reflowPart) {
		super(parent, style);
		this.reflowPart = reflowPart;

		setMenu(parent.getMenu());
		toolkit.adapt(this, true, true);
		FormColors colors = toolkit.getColors();
		if (toggle != null) {
			toggle.setHoverDecorationColor(colors.getColor(IFormColors.TB_TOGGLE_HOVER));
			toggle.setDecorationColor(colors.getColor(IFormColors.TB_TOGGLE));
		}

		boldFont = createBoldFont(colors.getDisplay(), parent.getFont());

		setFont(boldFont);
		if ((style & ExpandableComposite.TITLE_BAR) != 0 || (style & ExpandableComposite.SHORT_TITLE_BAR) != 0) {
			colors.initializeSectionToolBarColors();
			setTitleBarBackground(colors.getColor(IFormColors.TB_BG));
			setTitleBarBorderColor(colors.getColor(IFormColors.TB_BORDER));
			setTitleBarForeground(colors.getColor(IFormColors.TB_TOGGLE));
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (boldFont != null) {
			boldFont.dispose();
		}
	}

	@Override
	protected void reflow() {
		if (reflowPart != null && reflowPart.canReflow()) {
			super.reflow();
		}
	}

	/**
	 * From FormUtil on 3.3
	 */
	public static Font createBoldFont(Display display, Font regularFont) {
		FontData[] fontDatas = regularFont.getFontData();
		for (FontData element : fontDatas) {
			element.setStyle(element.getStyle() | SWT.BOLD);
		}
		return new Font(display, fontDatas);
	}

}
