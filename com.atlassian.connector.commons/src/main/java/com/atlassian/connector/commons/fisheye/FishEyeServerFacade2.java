/*******************************************************************************
 * Copyright (c) 2008 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.commons.fisheye;

import com.atlassian.connector.commons.api.ConnectionCfg;
import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.fisheye.api.FishEyeSession;
import com.atlassian.theplugin.commons.fisheye.api.model.FisheyePathHistoryItem;
import com.atlassian.theplugin.commons.remoteapi.ProductServerFacade;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import java.util.Collection;

public interface FishEyeServerFacade2 extends ProductServerFacade {

	FishEyeSession getSession(ConnectionCfg server) throws RemoteApiException, ServerPasswordNotProvidedException;

	/**
	 * @deprecated We are going remove {@link FishEyeServerFacade2}, so getSession here is for new code that should use
	 *             {@link FishEyeSession} directly.
	 */
	@Deprecated

	Collection<String> getRepositories(final ConnectionCfg server) throws RemoteApiException;

	/**
	 * @deprecated We are going remove {@link FishEyeServerFacade2}, so getSession here is for new code that should use
	 *             {@link FishEyeSession} directly.
	 */
	@Deprecated
    Collection<FisheyePathHistoryItem> getPathHistory(final ConnectionCfg server, String repo, String path)
            throws RemoteApiException;
}
