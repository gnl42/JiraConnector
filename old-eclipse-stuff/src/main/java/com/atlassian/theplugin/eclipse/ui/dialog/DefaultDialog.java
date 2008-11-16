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

package com.atlassian.theplugin.eclipse.ui.dialog;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.eclipse.ui.panel.IDialogManager;
import com.atlassian.theplugin.eclipse.ui.panel.IDialogPanel;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.util.PluginIcons;

/**
 * Default dialog implementation
 * 
 * @author Alexander Gurov
 */
public class DefaultDialog extends MessageDialog implements IDialogManager {
	public static final int DIALOG_FAILED = -1;
	public static final int BUTTON_WIDTH = 76;

	protected Listener keyListener;
	protected IDialogPanel panel;
	protected Control infoPanel;
	protected Font mainLabelFont;
	protected Label message;
	protected Label icon;

	protected Image infoImage;
	protected Image levelOkImage;
	protected Image levelWarningImage;
	protected Image levelErrorImage;

	protected Composite mainComposite;

	public DefaultDialog(Shell parentShell, IDialogPanel panel) {
		super(parentShell, panel.getDialogTitle(), null, null,
				MessageDialog.NONE, panel.getButtonNames(), 0);
		this.setShellStyle(this.getShellStyle() | SWT.RESIZE);
		this.panel = panel;
		this.levelOkImage = this.findImage("icons/common/level_ok.gif");
		this.levelWarningImage = this
				.findImage("icons/common/level_warning.gif");
		this.levelErrorImage = this.findImage("icons/common/level_error.gif");
	}

	public static int convertHeightInCharsToPixels(Control control, int chars) {
		GC gc = new GC(control);
		gc.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics = gc.getFontMetrics();
		gc.dispose();
		return fontMetrics.getHeight() * chars;
	}

	public static int computeButtonWidth(Button button) {
		int width = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 6;
		return Math.max(width, DefaultDialog.BUTTON_WIDTH);
	}

	public void setButtonEnabled(int idx, boolean enabled) {
		this.getButton(idx).setEnabled(enabled);
	}

	public boolean isButtonEnabled(int idx) {
		return this.getButton(idx).getEnabled();
	}

	public void setMessage(int level, String message) {
		Image img = this.levelOkImage;
		switch (level) {
		case IDialogManager.LEVEL_ERROR: {
			img = this.levelErrorImage;
			break;
		}
		case IDialogManager.LEVEL_WARNING: {
			img = this.levelWarningImage;
			break;
		}
		}
		if (message == null) {
			message = this.panel.getDefaultMessage();
		}
		this.message.setText(message == null ? "" : message);
		this.icon.setImage(img);
	}

	public int open() {
		try {
			this.setReturnCode(DefaultDialog.DIALOG_FAILED);
			return super.open();
		} finally {
			this.dispose();
		}
	}

	public void forceClose(int buttonID) {
		if (this.isButtonEnabled(buttonID)) {
			this.buttonPressed(buttonID);
		}
	}

	protected void dispose() {
		if (this.panel != null) {
			this.panel.dispose();
		}

		if (this.mainLabelFont != null) {
			this.mainLabelFont.dispose();
		}
		if (this.infoImage != null) {
			this.infoImage.dispose();
		}
		if (this.levelOkImage != null) {
			this.levelOkImage.dispose();
		}
		if (this.levelWarningImage != null) {
			this.levelWarningImage.dispose();
		}
		if (this.levelErrorImage != null) {
			this.levelErrorImage.dispose();
		}
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId != IDialogConstants.CANCEL_ID) {
			this.panel.buttonPressed(buttonId);
		}
		super.buttonPressed(buttonId);
	}

	protected Control createContents(Composite parent) {
		GridLayout layout = null;
		GridData data = null;

		this.mainComposite = new Composite(parent, SWT.NONE);

		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		this.mainComposite.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		this.mainComposite.setLayoutData(data);
		Dialog.applyDialogFont(this.mainComposite);
		this.initializeDialogUnits(this.mainComposite);

		this.infoPanel = this.createInfoPanel(this.mainComposite);
		this.dialogArea = this.createMainPanel(this.mainComposite);
		this.createBottomPanel(this.mainComposite);

		// computing the best size for the dialog
		Point defaultSize = this.dialogArea.getShell().computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true);
		data = (GridData) this.dialogArea.getLayoutData();
		int defaultHeightHint = data.heightHint;
		data.heightHint = this.panel.getPrefferedSize().y;
		this.dialogArea.setLayoutData(data);
		Point prefferedSize = this.dialogArea.getShell().computeSize(
				SWT.DEFAULT, SWT.DEFAULT, true);
		if (prefferedSize.y < defaultSize.y) {
			data.heightHint = defaultHeightHint;
			this.dialogArea.setLayoutData(data);
		}

		this.panel.initPanel(this);
		this.panel.addListeners();
		this.panel.postInit();

		String hId = this.panel.getHelpId();
		if (hId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(
					this.mainComposite, hId);
		}

		return this.mainComposite;
	}

	public void create() {
		super.create();
		this.getShell().getDisplay().addFilter(SWT.KeyDown,
				this.keyListener = new Listener() {
					public void handleEvent(Event event) {
						if (event.stateMask == SWT.CTRL
								&& event.keyCode == SWT.CR) {
							DefaultDialog.this
									.forceClose(IDialogConstants.OK_ID);
							event.doit = false;
						}
					}
				});
	}

	public boolean close() {
		if (this.keyListener != null) {
			this.getShell().getDisplay().removeFilter(SWT.KeyDown,
					this.keyListener);
		}
		// ESC pressed? (ESC handling is hardcoded in SWT and corresponding
		// event is not translated to the user nor as "KeyEvent" nor as
		// "button pressed")
		if (this.getReturnCode() == Window.CANCEL) {
			this.panel.buttonPressed(IDialogConstants.CANCEL_ID);
		}
		return this.panel.canClose() && super.close();
	}

	protected Control createInfoPanel(Composite parent) {
		Color bgColor = new Color(null, 255, 255, 255);

		GridLayout layout = null;
		GridData data = null;

		Composite infoPanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.numColumns = 2;
		infoPanel.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		Point pt = this.panel.getPrefferedSize();
		data.widthHint = pt.x;
		infoPanel.setLayoutData(data);
		infoPanel.setBackground(bgColor);

		Composite leftSide = new Composite(infoPanel, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 1;
		leftSide.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		leftSide.setLayoutData(data);
		leftSide.setBackground(bgColor);

		Label iconLabel = new Label(infoPanel, SWT.NONE);
		this.infoImage = this.findImage(this.panel.getImagePath());
		iconLabel.setImage(this.infoImage);

		Font defaultFont = JFaceResources.getBannerFont();
		FontData[] fData = defaultFont.getFontData();
		this.mainLabelFont = new Font(UIMonitorUtil.getDisplay(), fData);

		Label description = new Label(leftSide, SWT.WRAP);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		description.setLayoutData(data);
		String text = this.panel.getDialogDescription();
		description.setText(text != null ? text : "");
		description.setFont(this.mainLabelFont);
		description.setBackground(bgColor);

		this.icon = new Label(leftSide, SWT.NONE);
		data = new GridData();
		data.verticalAlignment = SWT.BEGINNING;
		this.icon.setLayoutData(data);
		this.icon.setBackground(bgColor);

		this.message = new Label(leftSide, SWT.WRAP);
		data = new GridData(GridData.FILL_BOTH);
		data.horizontalIndent = 3;
		this.message.setLayoutData(data);
		this.message.setBackground(bgColor);

		this.setMessage(IDialogManager.LEVEL_OK, null);

		return infoPanel;
	}

	protected Control createMainPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;

		Composite fullSizePanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		fullSizePanel.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		fullSizePanel.setLayoutData(data);

		Label separator = new Label(fullSizePanel, SWT.HORIZONTAL
				| SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite customPanel = new Composite(fullSizePanel, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.marginWidth = 7;
		customPanel.setLayout(layout);
		data = new GridData(GridData.FILL_BOTH);
		Point pt = this.panel.getPrefferedSize();
		data.widthHint = pt.x;
		customPanel.setLayoutData(data);

		separator = new Label(fullSizePanel, SWT.HORIZONTAL | SWT.SEPARATOR);
		separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		this.panel.createControls(customPanel);

		return customPanel;
	}

	protected Control createBottomPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;

		Composite composite = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 7;
		layout.marginWidth = 7;
		layout.numColumns = 2;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		composite.setLayoutData(data);

		this.createHelpImageButton(composite);

		this.buttonBar = this.createButtonPanel(composite);
		((GridData) this.buttonBar.getLayoutData()).horizontalIndent = 7;

		return composite;
	}

	protected ToolBar createHelpImageButton(Composite parent) {
		ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.NO_FOCUS);
		((GridLayout) parent.getLayout()).numColumns++;
		toolBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		final Cursor cursor = new Cursor(parent.getDisplay(), SWT.CURSOR_HAND);
		toolBar.setCursor(cursor);
		toolBar.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				cursor.dispose();
			}
		});
		ToolItem item = new ToolItem(toolBar, SWT.NONE);
		item.setImage(JFaceResources.getImage(DLG_IMG_HELP));
		item.setToolTipText(JFaceResources.getString("helpToolTip")); //$NON-NLS-1$
		item.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				DefaultDialog.this.mainComposite.notifyListeners(SWT.Help,
						new Event());
			}
		});
		return toolBar;
	}

	protected Control createButtonPanel(Composite parent) {
		GridLayout layout = null;
		GridData data = null;

		Composite buttonPanel = new Composite(parent, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonPanel.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalAlignment = SWT.RIGHT;
		buttonPanel.setLayoutData(data);

		return this.createButtonBar(buttonPanel);
	}

	protected Image findImage(String imagePath) {
		ImageDescriptor descriptor = ImageDescriptor
				.createFromImage(PluginIcons.getImageRegistry().get(
						PluginIcons.ICON_PLUGIN));
		if (descriptor == null) {
			descriptor = ImageDescriptor.getMissingImageDescriptor();
		}
		return descriptor.createImage();
	}

}
