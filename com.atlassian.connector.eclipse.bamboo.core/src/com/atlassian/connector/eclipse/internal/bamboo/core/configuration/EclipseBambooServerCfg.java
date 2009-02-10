/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.core.configuration;

import com.atlassian.theplugin.commons.cfg.BambooServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;

/**
 * Server Config for Eclipse connector so that we can key on just the url
 * 
 * @author Shawn Minto
 */
public class EclipseBambooServerCfg extends BambooServerCfg {

	private static final int HASCODE_MAGIC_NOT_TEMPORARY = 1237;

	private static final int HASCODE_MAGIC_TEMPORARY = 1231;

	private static final int HASHCODE_MAGIC = 31;

	private final boolean isTemporary;

	public EclipseBambooServerCfg(String name, String url, boolean isTemporary) {
		super(name, new ServerId());
		setUrl(url);
		this.isTemporary = isTemporary;
	}

	@Override
	public int hashCode() {
		int result;
		result = (isEnabled() ? 1 : 0);
		result = HASHCODE_MAGIC * result + (getUrl() != null ? getUrl().hashCode() : 0);
		result = HASHCODE_MAGIC * result + (getUsername() != null ? getUsername().hashCode() : 0);
		result = HASHCODE_MAGIC * result + (isTemporary ? HASCODE_MAGIC_TEMPORARY : HASCODE_MAGIC_NOT_TEMPORARY);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EclipseBambooServerCfg)) {
			return false;
		}

		final EclipseBambooServerCfg serverCfg = (EclipseBambooServerCfg) obj;

		if (getUrl() != null ? !getUrl().equals(serverCfg.getUrl()) : serverCfg.getUrl() != null) {
			return false;
		}
		if (getUsername() != null ? !getUsername().equals(serverCfg.getUsername()) : serverCfg.getUsername() != null) {
			return false;
		}

		if (isTemporary != serverCfg.isTemporary) {
			return false;
		}
		return true;
	}

}
