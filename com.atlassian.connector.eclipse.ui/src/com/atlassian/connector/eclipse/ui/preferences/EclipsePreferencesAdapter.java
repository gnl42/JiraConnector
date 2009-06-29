/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Atlassian - getting rid of JDT dependency and incorporated into Eclipse Connector + added save support
 *     
 *******************************************************************************/
package com.atlassian.connector.eclipse.ui.preferences;

import com.atlassian.connector.eclipse.ui.AtlassianUiPlugin;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import java.io.IOException;

/**
 * Adapts {@link org.eclipse.core.runtime.Preferences} to {@link org.eclipse.jface.preference.IPreferenceStore}.
 * 
 * This is almost a clone of org.eclipse.jdt.internal.ui.text.PreferencesAdapter, which unfortunately depend on JDT
 */
public class EclipsePreferencesAdapter implements IPreferenceStore, IPersistentPreferenceStore {

	/**
	 * Property change listener. Listens for events of type
	 * {@link org.eclipse.core.runtime.Preferences.PropertyChangeEvent} and fires a
	 * {@link org.eclipse.jface.util.PropertyChangeEvent} on the adapter with arguments from the received event.
	 */
	private class PreferenceChangeListener implements IPreferenceChangeListener {
		public void preferenceChange(PreferenceChangeEvent event) {
			firePropertyChangeEvent(event.getKey(), event.getOldValue(), event.getNewValue());
		}
	}

	/** Listeners on the adapter */
	private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);

	/** Listener on the adapted Preferences */
	private final PreferenceChangeListener fListener = new PreferenceChangeListener();

	/** Adapted Preferences */
	private final IEclipsePreferences fPreferences;

	/** True if no events should be forwarded */
	private boolean fSilent;

	private final IScopeContext fContext;

	private final String defaultQualifier;

	private final IScopeContext defaultContext;

	/**
	 * Initialize with the given Preferences.
	 * 
	 * @param preferences
	 *            The preferences to wrap.
	 */
	public EclipsePreferencesAdapter(IScopeContext context, String qualifier) {
		fContext = context;
		fPreferences = fContext.getNode(qualifier);
		defaultQualifier = qualifier;
		defaultContext = new DefaultScope();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fListeners.size() == 0) {
			fPreferences.addPreferenceChangeListener(fListener);
		}
		fListeners.add(listener);
	}

	/**
	 * {@inheritDoc}
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		fListeners.remove(listener);
		if (fListeners.size() == 0) {
			fPreferences.removePreferenceChangeListener(fListener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(String name) {
		try {
			return ArrayUtils.contains(fPreferences.keys(), name);
		} catch (BackingStoreException e) {
			return false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void firePropertyChangeEvent(String name, Object oldValue, Object newValue) {
		if (!fSilent) {
			final PropertyChangeEvent event = new PropertyChangeEvent(this, name, oldValue, newValue);
			Object[] listeners = fListeners.getListeners();
			for (Object listener2 : listeners) {
				final IPropertyChangeListener listener = (IPropertyChangeListener) listener2;
				Runnable runnable = new Runnable() {
					public void run() {
						listener.propertyChange(event);
					}
				};

				if (Display.getCurrent() != null) {
					runnable.run();
				} else {
					// Post runnable into UI thread
					Shell shell = AtlassianUiPlugin.getActiveWorkbenchShell();
					Display display;
					if (shell != null) {
						display = shell.getDisplay();
					} else {
						display = Display.getDefault();
					}
					display.asyncExec(runnable);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBoolean(String name) {
		return fPreferences.getBoolean(name, getDefaultBoolean(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getDefaultBoolean(String name) {
		return getDefaultPreferences().getBoolean(name, BOOLEAN_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDefaultDouble(String name) {
		return getDefaultPreferences().getDouble(name, DOUBLE_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public float getDefaultFloat(String name) {
		return getDefaultPreferences().getFloat(name, FLOAT_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getDefaultInt(String name) {
		return getDefaultPreferences().getInt(name, INT_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public long getDefaultLong(String name) {
		return getDefaultPreferences().getLong(name, LONG_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultString(String name) {
		return getDefaultPreferences().get(name, STRING_DEFAULT_DEFAULT);
	}

	/**
	 * {@inheritDoc}
	 */
	public double getDouble(String name) {
		return fPreferences.getDouble(name, getDefaultDouble(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public float getFloat(String name) {
		return fPreferences.getFloat(name, getDefaultFloat(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public int getInt(String name) {
		return fPreferences.getInt(name, getDefaultInt(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public long getLong(String name) {
		return fPreferences.getLong(name, getDefaultLong(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public String getString(String name) {
		return fPreferences.get(name, getDefaultString(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDefault(String name) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean needsSaving() {
		try {
			return fPreferences.keys().length > 0;
		} catch (BackingStoreException e) {
			// ignore
		}
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void putValue(String name, String value) {
		try {
			fSilent = true;
			fPreferences.put(name, value);
		} finally {
			fSilent = false;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, double value) {
		getDefaultPreferences().putDouble(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, float value) {
		getDefaultPreferences().putFloat(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, int value) {
		getDefaultPreferences().putInt(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, long value) {
		getDefaultPreferences().putLong(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, String value) {
		getDefaultPreferences().put(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDefault(String name, boolean value) {
		getDefaultPreferences().putBoolean(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setToDefault(String name) {
		fPreferences.put(name, getDefaultString(name));
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, double value) {
		fPreferences.putDouble(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, float value) {
		fPreferences.putFloat(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, int value) {
		fPreferences.putInt(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, long value) {
		fPreferences.putLong(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, String value) {
		fPreferences.put(name, value);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setValue(String name, boolean value) {
		fPreferences.putBoolean(name, value);
	}

	public void save() throws IOException {
		try {
			fPreferences.flush();
		} catch (BackingStoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Return the default IEclipsePreferences for this store.
	 * 
	 * @return this store's default preference node
	 */
	private IEclipsePreferences getDefaultPreferences() {
		return defaultContext.getNode(defaultQualifier);
	}
}
