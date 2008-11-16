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

package com.atlassian.theplugin.eclipse.ui.verifier;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.widgets.Control;

/**
 * Group verifier allows us to verify dedicated group of controls placed on the
 * panel
 * 
 * @author Alexander Gurov
 */
public class GroupVerifier extends CompositeVerifier {
	protected Map<AbstractVerifier, Control> componentsMap;

	public GroupVerifier() {
		super();
		this.componentsMap = new HashMap<AbstractVerifier, Control>();
	}

	public boolean verify() {
		this.hasWarning = false;
		for (Iterator<AbstractVerifier> it = this.verifiers.iterator(); it
				.hasNext();) {
			AbstractVerifier iVer = (AbstractVerifier) it.next();
			if (!iVer.verify((Control) this.componentsMap.get(iVer))) {
				return false;
			}
		}
		if (!this.hasWarning) {
			this.fireOk();
		}
		return true;
	}

	public void add(Control cmp, AbstractVerifier verifier) {
		super.add(verifier);
		this.componentsMap.put(verifier, cmp);
	}

	public void remove(Control cmp) {
		for (Iterator<Map.Entry<AbstractVerifier, Control>> it = this.componentsMap
				.entrySet().iterator(); it.hasNext();) {
			Map.Entry<AbstractVerifier, Control> entry = (Map.Entry<AbstractVerifier, Control>) it
					.next();
			if (cmp == entry.getValue()) {
				AbstractVerifier verifier = (AbstractVerifier) entry.getKey();
				super.remove(verifier);
				it.remove();
				break;
			}
		}
	}

	public void removeAll() {
		super.removeAll();
		this.componentsMap.clear();
	}

	public Iterator<Control> getComponents() {
		return this.componentsMap.values().iterator();
	}

}
