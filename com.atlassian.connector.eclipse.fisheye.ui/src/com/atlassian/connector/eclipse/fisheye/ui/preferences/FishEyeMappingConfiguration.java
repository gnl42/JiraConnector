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

package com.atlassian.connector.eclipse.fisheye.ui.preferences;

public class FishEyeMappingConfiguration {
	private final String scmPath;

	private final String fishEyeServer;

	private final String fishEyeRepo;

	public FishEyeMappingConfiguration(String scmPath, String fishEyeServer, String fishEyeRepo) {
		this.scmPath = scmPath;
		this.fishEyeServer = fishEyeServer;
		this.fishEyeRepo = fishEyeRepo;
	}

	public String getScmPath() {
		return scmPath;
	}

	public String getFishEyeServer() {
		return fishEyeServer;
	}

	public String getFishEyeRepo() {
		return fishEyeRepo;
	}

	public FishEyeMappingConfiguration getClone() {
		return new FishEyeMappingConfiguration(scmPath, fishEyeServer, fishEyeRepo);
	}

}
