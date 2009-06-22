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

package com.atlassian.connector.eclipse.internal.crucible.core.configuration;

import com.atlassian.theplugin.commons.cfg.CrucibleServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerId;

/**
 * Server Config for Eclipse connector so that we can key on just the url
 * 
 * @author Shawn Minto
 */
public class EclipseCrucibleServerCfg extends CrucibleServerCfg {

	private static final int HASCODE_MAGIC_NOT_TEMPORARY = 1237;

	private static final int HASCODE_MAGIC_TEMPORARY = 1231;

	private static final int HASHCODE_MAGIC = 31;

	private final boolean isTemporary;

	public EclipseCrucibleServerCfg(String name, String url, boolean isTemporary) {
		super(name, new ServerId());
		setUrl(url);
		this.isTemporary = isTemporary;
	}

	@Override
	public int hashCode() {
		int result;
		result = (isEnabled() ? 1 : 0);
		result = HASHCODE_MAGIC * result + (getUrl() != null ? getUrl().hashCode() : 0);
		result = HASHCODE_MAGIC * result + (getUserName() != null ? getUserName().hashCode() : 0);
//		result = HASHCODE_MAGIC * result + (getPassword() != null ? getPassword().hashCode() : 0);
		result = HASHCODE_MAGIC * result + (isTemporary ? HASCODE_MAGIC_TEMPORARY : HASCODE_MAGIC_NOT_TEMPORARY);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EclipseCrucibleServerCfg)) {
			return false;
		}

		final EclipseCrucibleServerCfg serverCfg = (EclipseCrucibleServerCfg) obj;

//		if (getPassword() != null ? !getPassword().equals(serverCfg.getPassword()) : serverCfg.getPassword() != null) {
//			return false;
//		}
		if (getUrl() != null ? !getUrl().equals(serverCfg.getUrl()) : serverCfg.getUrl() != null) {
			return false;
		}
		if (getUserName() != null ? !getUserName().equals(serverCfg.getUserName()) : serverCfg.getUserName() != null) {
			return false;
		}

		if (isTemporary != serverCfg.isTemporary) {
			return false;
		}
		return true;
	}

}
