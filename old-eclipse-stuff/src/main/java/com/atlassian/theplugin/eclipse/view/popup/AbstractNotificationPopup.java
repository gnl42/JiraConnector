/**
 * Copyright (C) 2008 Atlassian
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Created on 13.11.2007
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

package com.atlassian.theplugin.eclipse.view.popup;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.eclipse.view.popup.SwtUtil.FadeJob;
import com.atlassian.theplugin.eclipse.view.popup.SwtUtil.IFadeListener;

/**
 * @author Benjamin Pasero
 * @author Mik Kersten
 * @author Steffen Pingel
 */
public abstract class AbstractNotificationPopup extends Window {

	private static final int TITLE_HEIGHT = 24;

	private static final String LABEL_NOTIFICATION = "Notification";

	private static final String LABEL_JOB_CLOSE = "Close Notification Job";

	private static final int DEFAULT_WIDTH = 200;

	private static final int DEFAULT_HEIGHT = 100;

	private static final long DEFAULT_DELAY_CLOSE = 8 * 1000;

	private static final int PADDING_EDGE = 5;

	private long delayClose = DEFAULT_DELAY_CLOSE;

	protected LocalResourceManager resources;

	private NotificationPopupColors color;

	private final Display display;

	private Shell shell;

	private Region lastUsedRegion;

	private Image lastUsedBgImage;

	private final Job closeJob = new Job(LABEL_JOB_CLOSE) {

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (!display.isDisposed()) {
				display.asyncExec(new Runnable() {
					public void run() {
						Shell shell = AbstractNotificationPopup.this.getShell();
						if (shell == null || shell.isDisposed()) {
							return;
						}

						if (isMouseOver(shell)) {
							scheduleAutoClose();
							return;
						}

						AbstractNotificationPopup.this.closeFade();
					}

				});
			}
			if (monitor.isCanceled())
				return Status.CANCEL_STATUS;

			return Status.OK_STATUS;
		}
	};

	private boolean respectDisplayBounds = true;

	private boolean respectMonitorBounds = true;

	private FadeJob fadeJob;

	private boolean supportsFading;
	
	private boolean fadingEnabled;

	public AbstractNotificationPopup(Display display) {
		this(display, SWT.NO_TRIM | SWT.ON_TOP | SWT.NO_FOCUS);
	}

	public AbstractNotificationPopup(Display display, int style) {
		super(new Shell(display));
		setShellStyle(style);

		this.display = display;
		resources = new LocalResourceManager(JFaceResources.getResources());
		initResources();

		closeJob.setSystem(true);
	}

	public boolean isFadingEnabled() {
		return fadingEnabled;
	}
	
	public void setFadingEnabled(boolean fadingEnabled) {
		this.fadingEnabled = fadingEnabled;
	}
	
	/**
	 * Override to return a customized name. Default is to return the name of the product, specified by the -name (e.g.
	 * "Eclipse SDK") command line parameter that's associated with the product ID (e.g. "org.eclipse.sdk.ide"). Strips
	 * the trailing "SDK" for any name, since this part of the label is considered visual noise.
	 * 
	 * @return the name to be used in the title of the popup.
	 */
	protected String getPopupShellTitle() {
		IProduct product = Platform.getProduct();
		if (product != null) {
			String productName = product.getName();
			String LABEL_SDK = "SDK";
			if (productName.endsWith(LABEL_SDK)) {
				productName = productName.substring(0, productName.length() - LABEL_SDK.length());
			}
			return productName + " " + LABEL_NOTIFICATION;
		} else {
			return LABEL_NOTIFICATION;
		}
	}

	protected Image getPopupShellImage(int maximumHeight) {
		// always use the launching workbench window
		IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
		if (windows != null && windows.length > 0) {
			IWorkbenchWindow workbenchWindow = windows[0];
			if (workbenchWindow != null && !workbenchWindow.getShell().isDisposed()) {
				Image image = getShell().getImage();
				int diff = Integer.MAX_VALUE;
				if (image != null && image.getBounds().height <= maximumHeight) {
					diff = maximumHeight - image.getBounds().height;
				} else {
					image = null;
				}
				
				Image[] images = getShell().getImages();
				if (images != null && images.length > 0) {
					// find the icon that is closest in size, but not larger than maximumHeight 
					for (int i = 0; i < images.length; i++) {
						int newDiff = maximumHeight - images[i].getBounds().height;
						if (newDiff >= 0 && newDiff <= diff) {
							diff = newDiff;
							image = images[i];
						}
					}
				}
				
				return image;
			}
		}
		return null;
	}

	/**
	 * Override to populate with notifications.
	 * 
	 * @param parent
	 */
	protected void createContentArea(Composite parent) {
		// empty by default
	}

	/**
	 * Override to customize the title bar
	 */
	protected void createTitleArea(Composite parent) {
		((GridData) parent.getLayoutData()).heightHint = TITLE_HEIGHT;

		Label titleImageLabel = new Label(parent, SWT.NONE);
		titleImageLabel.setImage(getPopupShellImage(TITLE_HEIGHT));

		Label titleTextLabel = new Label(parent, SWT.NONE);
		titleTextLabel.setText(getPopupShellTitle());
		titleTextLabel.setFont(TaskListColorsAndFonts.BOLD);
		titleTextLabel.setForeground(color.getTitleText());
		titleTextLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		titleTextLabel.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		Label button = new Label(parent, SWT.NONE);
		//button.setImage(TasksUiImages.getImage(TasksUiImages.NOTIFICATION_CLOSE));

		button.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent e) {
				// ignore
			}

			public void mouseDown(MouseEvent e) {
				// ignore
			}

			public void mouseUp(MouseEvent e) {
				close();
			}

		});
	}

	private void initResources() {
		color = new NotificationPopupColors(display, resources);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);

		shell = newShell;
		newShell.setBackground(color.getBorder());
	}

	public void create() {
		super.create();
		addRegion(shell);
	}

	private void addRegion(Shell shell) {
		Region region = new Region();
		Point s = shell.getSize();

		/* Add entire Shell */
		region.add(0, 0, s.x, s.y);

		/* Subtract Top-Left Corner */
		region.subtract(0, 0, 5, 1);
		region.subtract(0, 1, 3, 1);
		region.subtract(0, 2, 2, 1);
		region.subtract(0, 3, 1, 1);
		region.subtract(0, 4, 1, 1);

		/* Subtract Top-Right Corner */
		region.subtract(s.x - 5, 0, 5, 1);
		region.subtract(s.x - 3, 1, 3, 1);
		region.subtract(s.x - 2, 2, 2, 1);
		region.subtract(s.x - 1, 3, 1, 1);
		region.subtract(s.x - 1, 4, 1, 1);

		/* Dispose old first */
		if (shell.getRegion() != null)
			shell.getRegion().dispose();

		/* Apply Region */
		shell.setRegion(region);

		/* Remember to dispose later */
		lastUsedRegion = region;
	}

	private boolean isMouseOver(Shell shell) {
		if (display.isDisposed()) {
			return false;
		}
		return shell.getBounds().contains(display.getCursorLocation());
	}

	@Override
	public int open() {
		if (shell == null || shell.isDisposed()) {
			shell = null;
			create();
		}

		constrainShellSize();
		shell.setLocation(fixupDisplayBounds(shell.getSize(), shell.getLocation()));

		if (isFadingEnabled()) {
			supportsFading = SwtUtil.setAlpha(shell, 0);
		} else {
			supportsFading = false;
		}
		shell.setVisible(true);
		if (supportsFading) {
			fadeJob = SwtUtil.fadeIn(shell, new IFadeListener() {
				public void faded(Shell shell, int alpha) {
					if (shell.isDisposed()) {
						return;
					}
										
					if (alpha == 255) {
						scheduleAutoClose();					
					}
				}
			});
		} else {
			scheduleAutoClose();
		}

		return Window.OK;
	}

	protected void scheduleAutoClose() {
		if (delayClose > 0) {
			closeJob.schedule(delayClose);
		}
	}

	protected Control createContents(Composite parent) {
		((GridLayout) parent.getLayout()).marginWidth = 1;
		((GridLayout) parent.getLayout()).marginHeight = 1;

		/* Outer Composite holding the controls */
		final Composite outerCircle = new Composite(parent, SWT.NO_FOCUS);
		outerCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.verticalSpacing = 0;

		outerCircle.setLayout(layout);

		/* Title area containing label and close button */
		final Composite titleCircle = new Composite(outerCircle, SWT.NO_FOCUS);
		titleCircle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		titleCircle.setBackgroundMode(SWT.INHERIT_FORCE);

		layout = new GridLayout(4, false);
		layout.marginWidth = 3;
		layout.marginHeight = 0;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 3;

		titleCircle.setLayout(layout);
		titleCircle.addControlListener(new ControlAdapter() {

			public void controlResized(ControlEvent e) {
				Rectangle clArea = titleCircle.getClientArea();
				lastUsedBgImage = new Image(titleCircle.getDisplay(), clArea.width, clArea.height);
				GC gc = new GC(lastUsedBgImage);

				/* Gradient */
				drawGradient(gc, clArea);

				/* Fix Region Shape */
				fixRegion(gc, clArea);

				gc.dispose();

				Image oldBGImage = titleCircle.getBackgroundImage();
				titleCircle.setBackgroundImage(lastUsedBgImage);

				if (oldBGImage != null) {
					oldBGImage.dispose();
				}
			}

			private void drawGradient(GC gc, Rectangle clArea) {
				gc.setForeground(color.getGradientBegin());
				gc.setBackground(color.getGradientEnd());
				gc.fillGradientRectangle(clArea.x, clArea.y, clArea.width, clArea.height, true);
			}

			private void fixRegion(GC gc, Rectangle clArea) {
				gc.setForeground(color.getBorder());

				/* Fill Top Left */
				gc.drawPoint(2, 0);
				gc.drawPoint(3, 0);
				gc.drawPoint(1, 1);
				gc.drawPoint(0, 2);
				gc.drawPoint(0, 3);

				/* Fill Top Right */
				gc.drawPoint(clArea.width - 4, 0);
				gc.drawPoint(clArea.width - 3, 0);
				gc.drawPoint(clArea.width - 2, 1);
				gc.drawPoint(clArea.width - 1, 2);
				gc.drawPoint(clArea.width - 1, 3);
			}
		});

		/* Create Title Area */
		createTitleArea(titleCircle);

		/* Outer composite to hold content controlls */
		Composite outerContentCircle = new Composite(outerCircle, SWT.NONE);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;

		outerContentCircle.setLayout(layout);
		outerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		outerContentCircle.setBackground(outerCircle.getBackground());

		/* Middle composite to show a 1px black line around the content controls */
		Composite middleContentCircle = new Composite(outerContentCircle, SWT.NO_FOCUS);

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginTop = 1;

		middleContentCircle.setLayout(layout);
		middleContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		middleContentCircle.setBackground(color.getBorder());

		/* Inner composite containing the content controls */
		Composite innerContentCircle = new Composite(middleContentCircle, SWT.NO_FOCUS);
		innerContentCircle.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		layout.marginHeight = 5;

		innerContentCircle.setLayout(layout);

		((GridLayout) innerContentCircle.getLayout()).marginLeft = 5;
		((GridLayout) innerContentCircle.getLayout()).marginRight = 2;
		innerContentCircle.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));

		/* Content Area */
		createContentArea(innerContentCircle);

		return outerCircle;
	}

	protected void initializeBounds() {
		Rectangle clArea = getPrimaryClientArea();
		int initialHeight = shell.computeSize(DEFAULT_WIDTH, SWT.DEFAULT).y;
		int height = DEFAULT_HEIGHT;
		if (DEFAULT_HEIGHT < initialHeight) {
			height = initialHeight;
		}

		Point size = new Point(DEFAULT_WIDTH, height);
		shell.setLocation(clArea.width + clArea.x - size.x - PADDING_EDGE, clArea.height + clArea.y - size.y
				- PADDING_EDGE);
		shell.setSize(size);
	}

	private Rectangle getPrimaryClientArea() {
		Monitor primaryMonitor = shell.getDisplay().getPrimaryMonitor();
		return (primaryMonitor != null) ? primaryMonitor.getClientArea() : shell.getDisplay().getClientArea();
	}

	public void closeFade() {
		if (fadeJob != null) {
			fadeJob.cancelAndWait(false);
		}
		if (supportsFading) {
			fadeJob = SwtUtil.fadeOut(getShell(), new IFadeListener() {
				public void faded(Shell shell, int alpha) {
					if (!shell.isDisposed() && alpha == 0) {
						shell.close();
					}					
				}
			});
		} else {
			shell.close();
		}
	}

	public boolean close() {
		resources.dispose();
		if (lastUsedRegion != null) {
			lastUsedRegion.dispose();
		}
		if (lastUsedBgImage != null && !lastUsedBgImage.isDisposed()) {
			lastUsedBgImage.dispose();
		}
		return super.close();
	}

	public long getDelayClose() {
		return delayClose;
	}

	public void setDelayClose(long delayClose) {
		this.delayClose = delayClose;
	}

	private Point fixupDisplayBounds(Point tipSize, Point location) {
		if (respectDisplayBounds || respectMonitorBounds) {
			Rectangle bounds;
			Point rightBounds = new Point(tipSize.x + location.x, tipSize.y + location.y);

			if (respectMonitorBounds) {
				bounds = shell.getDisplay().getPrimaryMonitor().getBounds();
			} else {
				bounds = getPrimaryClientArea();
			}

			if (!(bounds.contains(location) && bounds.contains(rightBounds))) {
				if (rightBounds.x > bounds.x + bounds.width) {
					location.x -= rightBounds.x - (bounds.x + bounds.width);
				}

				if (rightBounds.y > bounds.y + bounds.height) {
					location.y -= rightBounds.y - (bounds.y + bounds.height);
				}

				if (location.x < bounds.x) {
					location.x = bounds.x;
				}

				if (location.y < bounds.y) {
					location.y = bounds.y;
				}
			}
		}

		return location;
	}

}