package me.glindholm.connector.eclipse.internal.branding.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import me.glindholm.connector.eclipse.internal.branding.ui.JiraConnectorBrandingPlugin;

public class JiraConnectorPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        return composite;
    }

    public JiraConnectorPreferencePage() {
        noDefaultAndApplyButton();
        setPreferenceStore(JiraConnectorBrandingPlugin.getDefault().getPreferenceStore());
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    @Override
    public void init(IWorkbench workbench) {
    }

}