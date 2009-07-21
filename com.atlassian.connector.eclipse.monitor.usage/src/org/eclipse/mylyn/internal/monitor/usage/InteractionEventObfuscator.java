/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.monitor.usage;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.eclipse.core.internal.preferences.Base64;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;

/**
 * Provides one way hashing of events for the purpose of ensuring privacy of element handles.
 * 
 * @author Mik Kersten
 */
public class InteractionEventObfuscator {

	private static final char DELIM_PATH = '/';

	public static final String LABEL_FAILED_TO_OBFUSCATE = "<failed to obfuscate>";

	public static final String ENCRYPTION_ALGORITHM = "SHA";

	public String obfuscateHandle(String structureKind, String structureHandle) {
		if (structureHandle == null || structureHandle.equals("")) {
			return structureHandle;
		}
		StringBuilder obfuscated = new StringBuilder();
		AbstractContextStructureBridge bridge = ContextCore.getStructureBridge(structureKind);
		Object object = bridge.getObjectForHandle(structureHandle);
		if (object instanceof IAdaptable) {
			Object adapter = ((IAdaptable) object).getAdapter(IResource.class);
			if (adapter instanceof IResource) {
				obfuscated.append(obfuscateResourcePath(((IResource) adapter).getFullPath()));
				obfuscated.append(DELIM_PATH);
			}
		}
		obfuscated.append(obfuscateString(structureHandle));
		return obfuscated.toString();
	}

	/**
	 * Encrypts the string using SHA, then makes it reasonable to print.
	 */
	public String obfuscateString(String string) {
		String obfuscatedString = null;
		try {
			MessageDigest md = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
			md.update(string.getBytes());
			byte[] digest = md.digest();
			obfuscatedString = new String(Base64.encode(digest)).replace(DELIM_PATH, '=');
			// obfuscatedString = "" + new String(digest).hashCode();
		} catch (NoSuchAlgorithmException e) {
			StatusHandler.log(new Status(IStatus.WARNING, UiUsageMonitorPlugin.ID_PLUGIN, "SHA not available", e));
			obfuscatedString = LABEL_FAILED_TO_OBFUSCATE;
		}
		return obfuscatedString;
	}

	public String obfuscateResourcePath(IPath path) {
		if (path == null) {
			return "";
		} else {
			StringBuffer obfuscatedPath = new StringBuffer();
			for (int i = 0; i < path.segmentCount(); i++) {
				obfuscatedPath.append(obfuscateString(path.segments()[i]));
				if (i < path.segmentCount() - 1) {
					obfuscatedPath.append(DELIM_PATH);
				}
			}
			return obfuscatedPath.toString();
		}
	}

}
