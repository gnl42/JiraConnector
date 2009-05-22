package com.atlassian.connector.eclipse.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class AtlassianPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private static final String DESCRIPTION = "Expand the tree to edit preferences for a specific feature";

	@Override
	protected Control createContents(Composite parent) {
		return null;
	}

	public AtlassianPreferencePage() {
		noDefaultAndApplyButton();
		setDescription(DESCRIPTION);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}