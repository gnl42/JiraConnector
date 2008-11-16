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

package com.atlassian.theplugin.eclipse.ui.bamboo;

import java.util.Date;

import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;

/**
 * Abstract repository resource
 * 
 * @author Alexander Gurov
 */
public interface IBambooResource {
	
	public static class Info {
		public final long fileSize;
		public final String lastAuthor;
		public final Date lastChangedDate;
		public final boolean hasProperties;
		
		public Info(long fileSize, String lastAuthor, Date lastChangedDate, boolean hasProperties) {
			this.fileSize = fileSize;
			this.lastAuthor = lastAuthor;
			this.hasProperties = hasProperties;
			this.lastChangedDate = lastChangedDate;
		}
	}

	public boolean isInfoCached();
	
	public void refresh();

	public String getName();
	
	public String getUrl();
	
	public IBambooResource getParent();
	
	public IBambooResource getRoot();
	
	public IBambooServer getBambooServer();
	
	public Info getInfo();
	
}
