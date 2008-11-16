/*******************************************************************************
 * Copyright (c) 2005-2006 Polarion Software.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alexander Gurov - Initial API and implementation
 *    Gabor Liptak - Speedup Pattern's usage
 *******************************************************************************/

package com.atlassian.theplugin.eclipse.ui.panel.reporting;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.atlassian.theplugin.eclipse.ui.panel.AbstractDialogPanel;
import com.atlassian.theplugin.eclipse.util.PatternProvider;

/**
 * Styled preview panel
 * 
 * @author Alexander Gurov
 */
public class PreviewPanel extends AbstractDialogPanel {
	protected String report;
	protected Font font;

	public PreviewPanel(String title, String description, String message,
			String report) {
		this(title, description, message, report, null);
	}

	public PreviewPanel(String title, String description, String message,
			String report, Font font) {
		super(new String[] { IDialogConstants.OK_LABEL });
		this.dialogTitle = title;
		this.dialogDescription = description;
		this.defaultMessage = message;
		this.report = report;
		this.font = font;
	}

	public void createControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		Point prefferedSize = this.getPrefferedSize();
		data.widthHint = prefferedSize.x;
		data.heightHint = prefferedSize.y;
		composite.setLayoutData(data);

		this.report = PatternProvider.replaceAll(this.report, "<br>", "\n");
		this.report = PatternProvider.replaceAll(this.report, "&lt;", "<");
		this.report = PatternProvider.replaceAll(this.report, "&gt;", ">");
		StyledText styledText = new StyledText(composite, SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.WRAP);

		if (this.font != null) {
			styledText.setFont(this.font);
		}

		List<StyleRange> styledRanges = new ArrayList<StyleRange>();
		styledRanges = this.getStyleRanges();
		styledText.setText(this.report);
		styledText.setStyleRanges((StyleRange[]) styledRanges
				.toArray(new StyleRange[styledRanges.size()]));
		styledText.setEditable(false);
		styledText.setLayoutData(data);
	}

	public void dispose() {
		super.dispose();
		if (this.font != null) {
			this.font.dispose();
		}
	}

	protected void saveChanges() {
	}

	protected void cancelChanges() {
	}

	protected List<StyleRange> getStyleRanges() {
		List<StyleRange> styledRanges = new ArrayList<StyleRange>();

		Stack<StyleRange> boldEntries = new Stack<StyleRange>();
		Stack<StyleRange> italicEntries = new Stack<StyleRange>();

		for (int i = 0; i < this.report.length(); i++) {
			if (this.report.charAt(i) == '<' && i < this.report.length() - 2) {
				if (this.report.charAt(i + 2) == '>') {
					StyleRange range = new StyleRange();
					range.start = i;
					if (this.report.charAt(i + 1) == 'b') {
						range.fontStyle = SWT.BOLD;
						boldEntries.push(range);
						this.report = this.report.substring(0, i)
								+ this.report.substring(i + 3);
					} else if (this.report.charAt(i + 1) == 'i') {
						range.fontStyle = SWT.ITALIC;
						italicEntries.push(range);
						this.report = this.report.substring(0, i)
								+ this.report.substring(i + 3);
					}
				} else if (this.report.charAt(i + 1) == '/') {
					if (i < this.report.length() - 3
							&& this.report.charAt(i + 3) == '>') {
						if (this.report.charAt(i + 2) == 'b') {
							if (boldEntries.size() > 0) {
								StyleRange range = (StyleRange) boldEntries
										.pop();
								range.length = i - range.start;
								styledRanges.add(range);
								this.report = this.report.substring(0, i)
										+ this.report.substring(i + 4);
							}
						} else if (this.report.charAt(i + 2) == 'i') {
							if (italicEntries.size() > 0) {
								StyleRange range = (StyleRange) italicEntries
										.pop();
								range.length = i - range.start;
								styledRanges.add(range);
								this.report = this.report.substring(0, i)
										+ this.report.substring(i + 4);
							}
						}
					}
				}
			}
		}

		return styledRanges;
	}

	public Point getPrefferedSize() {
		return new Point(640, 300);
	}

}
