package com.atlassian.connector.eclipse.internal.branding.ui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.mylyn.internal.provisional.commons.ui.WorkbenchUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.atlassian.connector.eclipse.internal.branding.ui.AtlassianBrandingPlugin;
import com.atlassian.connector.eclipse.internal.ui.AtlassianBundlesInfo;
import com.atlassian.connector.eclipse.internal.ui.AtlassianLogo;
import com.atlassian.connector.eclipse.internal.ui.IBrandingConstants;

public class AtlassianPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String TEXT_MAIN = "The Atlassian Eclipse Connector is an Eclipse plugin that lets you "
			+ "work with the Atlassian products within your IDE. Now you don't "
			+ "have to switch between websites, email messages and news feeds to "
			+ "see what's happening to your project and your code. Instead, you "
			+ "can see the relevant <a href=\"http://www.atlassian.com/software/jira\">JIRA</a> issues "
			+ "and <a href=\"http://www.atlassian.com/software/bamboo\">Bamboo</a> build "
			+ "information right there in your development environment. Viewing your "
			+ "code in <a href=\"http://www.atlassian.com/software/fisheye\">FishEye</a> is just a click away.";

	@Override
	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NULL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(composite);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(composite);

		final Composite white = new Composite(composite, SWT.NULL);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(white);
		GridLayoutFactory.fillDefaults().numColumns(1).spacing(0, 0).applyTo(white);
		white.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		final Composite center = new Composite(white, SWT.NULL);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(center);
		GridLayoutFactory.fillDefaults().numColumns(1).margins(5, 0).spacing(0, 0).applyTo(center);

		final Label label = new Label(center, SWT.CENTER);
		final Image image = AtlassianLogo.getImage(AtlassianLogo.ATLASSIAN_LOGO);
		label.setImage(image);

		final int logoWidth = image.getBounds().width + 100;
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).hint(logoWidth, image.getBounds().height).applyTo(
				label);

		label.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		center.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));

		// OK, when we cannot just embed the browser, then
		// we can always build something decent manually:

		final Composite nestedComposite = new Composite(center, SWT.NONE);
		nestedComposite.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridLayoutFactory.fillDefaults().numColumns(1).margins(4, 0).applyTo(nestedComposite);
		GridDataFactory.fillDefaults()
				.align(SWT.CENTER, SWT.FILL)
				.hint(logoWidth, SWT.DEFAULT)
				.applyTo(nestedComposite);

		Link link = new Link(nestedComposite, SWT.NONE);
		link.setText(TEXT_MAIN);

		link.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text);
			}
		});
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).hint(logoWidth, SWT.DEFAULT).applyTo(link);

		final FontRegistry fontRegistry = new FontRegistry();
		fontRegistry.put("big", new FontData[] { new FontData("Arial", 18, SWT.BOLD) });
		final Font bigFont = fontRegistry.get("big");

		// a spacer
		new Label(nestedComposite, SWT.NONE).setVisible(false);

		final Link link2 = createLink(nestedComposite, "Developed by Atlassian for you to lust after", -1);
		link2.setFont(bigFont);

		link2.setText("Developed by Atlassian for you to lust after");
		final Link link3 = createLink(nestedComposite,
				"<a href=\"http://www.atlassian.com/\">http://www.atlassian.com/</a>", -1);
		link3.setFont(bigFont);

		// a spacer
		new Label(nestedComposite, SWT.NONE).setVisible(false);

		createLink(nestedComposite, "Licensed under the Eclipse Public License Version 1.0 (\"EPL\").", -1);
		createLink(nestedComposite, "Copyright (c) Atlassian 2009", -1);

		// a spacer
		new Label(nestedComposite, SWT.NONE).setVisible(false);

		final Composite buttonBar = new Composite(composite, SWT.NONE);
		buttonBar.setLayout(new RowLayout());

		createButton(buttonBar, "On-line Help",
				"http://confluence.atlassian.com/display/IDEPLUGIN/Atlassian+Connector+for+Eclipse");
		createButton(buttonBar, "Visit Forum", "http://forums.atlassian.com/forum.jspa?forumID=124&start=0");
		createButton(buttonBar, "Request Support", "https://support.atlassian.com/browse/ECSP");
		createButton(buttonBar, "Report Bug", "https://studio.atlassian.com/browse/PLE");
		if (AtlassianBundlesInfo.isOnlyJiraInstalled()) {
			createButton(buttonBar, "Install Bamboo, Crucible && FishEye Integration",
					IBrandingConstants.INTEGRATIONS_GUIDE_URL);
		}

		return composite;
	}

	private Link createLink(Composite parent, String text, int width) {
		Link link = new Link(parent, SWT.NONE);
		link.setText(text);

		link.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).hint(width, SWT.DEFAULT).applyTo(link);
		link.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(e.text);
			}

		});
		return link;
	}

	private void createButton(final Composite parent, String text, final String url) {
		Button helpButton = new Button(parent, SWT.PUSH);
		helpButton.setText(text);
		helpButton.setToolTipText("Open " + url);
		helpButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				WorkbenchUtil.openUrl(url);
			}
		});
	}

	public AtlassianPreferencePage() {
		noDefaultAndApplyButton();
		setPreferenceStore(AtlassianBrandingPlugin.getDefault().getPreferenceStore());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}