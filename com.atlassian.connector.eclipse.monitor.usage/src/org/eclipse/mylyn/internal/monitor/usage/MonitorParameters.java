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

import java.util.ArrayList;
import java.util.Collection;

public class MonitorParameters {

	private final Collection<String> urls = new ArrayList<String>();

	public void setAcceptedUrlList(Collection<String> urls) {
		this.urls.clear();
		this.urls.addAll(urls);
	}

}
