/*******************************************************************************
 * Copyright (c) 2004, 2009 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *     Kevin Barnes, IBM Corporation - fix for bug 277974
 *******************************************************************************/

package com.atlassian.connector.eclipse.ui.commons;

import com.atlassian.connector.eclipse.ui.viewers.ICustomToolTipInfo;
import com.atlassian.connector.eclipse.ui.viewers.ICustomToolTipInfoProvider;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonFonts;
import org.eclipse.mylyn.internal.provisional.commons.ui.CommonUiUtil;
import org.eclipse.mylyn.internal.provisional.commons.ui.GradientToolTip;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.PlatformUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IFormColors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mik Kersten
 * @author Eric Booth
 * @author Leo Dos Santos
 * @author Steffen Pingel
 * @author Jacek Jaroczynski
 * 
 *         It works with controls containing {@link DecoratedResource} elements.
 */
@SuppressWarnings("restriction")
public class CustomToolTip extends GradientToolTip {

	private final static int MAX_WIDTH = 600;

	private final static int X_SHIFT = PlatformUtil.getToolTipXShift();

	private final static int Y_SHIFT = 1;

	private ICustomToolTipInfo currentTipElement;

	private final List<CustomToolTipListener> listeners = new ArrayList<CustomToolTipListener>();

	private boolean visible;

	private boolean triggeredByMouse = true;

	private final Control control;

	private final Color titleColor;

	private final boolean showForDirectories;

	private ICustomToolTipInfoProvider infoProvider;

	public CustomToolTip(Control control) {
		this(control, false);
	}

	public CustomToolTip(Control control, boolean showForDirectories) {
		super(control);
		this.control = control;
		this.showForDirectories = showForDirectories;
		setShift(new Point(1, 1));
		titleColor = TasksUiPlugin.getDefault().getFormColors(control.getDisplay()).getColor(IFormColors.TITLE);
	}

	public void setInfoProvider(ICustomToolTipInfoProvider provider) {
		infoProvider = provider;
	}

	public void dispose() {
		hide();
	}

	@Override
	protected void afterHideToolTip(Event event) {
		triggeredByMouse = true;
		visible = false;
		for (CustomToolTipListener listener : listeners.toArray(new CustomToolTipListener[0])) {
			listener.toolTipHidden(event);
		}
	}

	public void addTaskListToolTipListener(CustomToolTipListener listener) {
		listeners.add(listener);
	}

	public void removeTaskListToolTipListener(CustomToolTipListener listener) {
		listeners.remove(listener);
	}

	@Override
	public Point getLocation(Point tipSize, Event event) {
		Widget widget = getTipWidget(event);
		if (widget != null) {
			Rectangle bounds = getBounds(widget);
			if (bounds != null) {
				return control.toDisplay(bounds.x + X_SHIFT, bounds.y + bounds.height + Y_SHIFT);
			}
		}
		return super.getLocation(tipSize, event);//control.toDisplay(event.x + xShift, event.y + yShift);
	}

	protected Widget getTipWidget(Event event) {
		Point widgetPosition = new Point(event.x, event.y);
		Widget widget = event.widget;
		if (widget instanceof ToolBar) {
			ToolBar w = (ToolBar) widget;
			return w.getItem(widgetPosition);
		}
		if (widget instanceof Table) {
			Table w = (Table) widget;
			return w.getItem(widgetPosition);
		}
		if (widget instanceof Tree) {
			Tree w = (Tree) widget;
			return w.getItem(widgetPosition);
		}

		return widget;
	}

	private Rectangle getBounds(Widget widget) {
		if (widget instanceof ToolItem) {
			ToolItem w = (ToolItem) widget;
			return w.getBounds();
		}
		if (widget instanceof TableItem) {
			TableItem w = (TableItem) widget;
			return w.getBounds();
		}
		if (widget instanceof TreeItem) {
			TreeItem w = (TreeItem) widget;
			return w.getBounds();
		}
		return null;
	}

	@Override
	protected boolean shouldCreateToolTip(Event event) {
		assert infoProvider != null;
		currentTipElement = null;

		if (super.shouldCreateToolTip(event)) {
			Widget tipWidget = getTipWidget(event);
			if (tipWidget != null) {
				Rectangle bounds = getBounds(tipWidget);
				if (bounds != null && contains(bounds.x, bounds.y)) {
					ICustomToolTipInfo decoratedResource = infoProvider.getToolTipInfo(tipWidget);
					if (decoratedResource != null) {
						if (showForDirectories || !decoratedResource.isContainer()) {
							currentTipElement = decoratedResource;
						}
					}
				}
			}
		}

		if (currentTipElement == null) {
			hide();
			return false;
		} else {
			return true;
		}
	}

	private boolean contains(int x, int y) {
		if (control instanceof Scrollable) {
			return ((Scrollable) control).getClientArea().contains(x, y);
		} else {
			return control.getBounds().contains(x, y);
		}
	}

	@Override
	protected Composite createToolTipArea(Event event, Composite parent) {
		assert currentTipElement != null;

		Composite composite = createToolTipContentAreaComposite(parent);

		currentTipElement.createToolTipArea(this, composite);

		visible = true;

		return composite;
	}

	protected Composite createToolTipContentAreaComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 5;
		gridLayout.marginHeight = 2;
		composite.setLayout(gridLayout);
		composite.setBackground(composite.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		return composite;
	}

	private String removeTrailingNewline(String text) {
		if (text.endsWith("\n")) { //$NON-NLS-1$
			return text.substring(0, text.length() - 1);
		}
		return text;
	}

	public void addIconAndLabel(Composite parent, Image image, String text) {
		addIconAndLabel(parent, image, text, false);
	}

	public void addIconAndLabel(Composite parent, Image image, String text, boolean bold) {
		Label imageLabel = new Label(parent, SWT.NONE);
		imageLabel.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
		imageLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		imageLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
		imageLabel.setImage(image);

		Label textLabel = new Label(parent, SWT.WRAP);
		if (bold) {
			textLabel.setFont(CommonFonts.BOLD);
		}
		textLabel.setForeground(titleColor);
		textLabel.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		textLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_CENTER));
		text = removeTrailingNewline(text);
		textLabel.setText(CommonUiUtil.toLabel(text));
		int width = Math.min(textLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x, MAX_WIDTH);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).hint(width, SWT.DEFAULT).applyTo(textLabel);
	}

	public static interface CustomToolTipListener {
		void toolTipHidden(Event event);
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isTriggeredByMouse() {
		return triggeredByMouse;
	}

	@Override
	public void show(Point location) {
		triggeredByMouse = false;
		super.show(location);
	}

}
