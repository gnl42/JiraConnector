package me.glindholm.connector.eclipse.internal.branding.ui.preferences;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.mylyn.commons.workbench.browser.BrowserUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import me.glindholm.connector.eclipse.internal.branding.ui.AtlassianBrandingPlugin;

public class AtlassianPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    @Override
    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NULL);
        return composite;
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