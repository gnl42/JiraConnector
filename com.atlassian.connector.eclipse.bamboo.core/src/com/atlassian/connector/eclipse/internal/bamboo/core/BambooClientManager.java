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

package com.atlassian.connector.eclipse.internal.bamboo.core;

import static com.atlassian.connector.eclipse.internal.core.ServerDataUtil.getServerData;

import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClient;
import com.atlassian.connector.eclipse.internal.bamboo.core.client.BambooClientData;
import com.atlassian.connector.eclipse.internal.core.client.HttpSessionCallbackImpl;
import com.atlassian.connector.eclipse.internal.core.client.RepositoryClientManager;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.remoteapi.ServerData;
import com.atlassian.theplugin.commons.remoteapi.rest.HttpSessionCallback;
import com.atlassian.theplugin.commons.util.LoggerImpl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.commons.net.AbstractWebLocation;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.TaskRepositoryLocationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to manage the clients and data on a per-repository basis
 * 
 * @author Shawn Minto
 * @author Wojciech Seliga
 */
public class BambooClientManager extends RepositoryClientManager<BambooClient, BambooClientData> {

	private BambooServerFacade crucibleServerFacade;

	private final HttpSessionCallbackImpl clientCallback;

	public BambooClientManager(File cacheFile) {
		super(cacheFile);
		clientCallback = new HttpSessionCallbackImpl();
	}

	@Override
	public synchronized BambooClient getClient(TaskRepository taskRepository) {
		BambooClient client = super.getClient(taskRepository);
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);
		ServerData serverCfg = getServerData(location, taskRepository, false);
		updateHttpSessionCallback(location, serverCfg);

		return client;
	}

	private void updateHttpSessionCallback(AbstractWebLocation location, ServerData serverCfg) {
		clientCallback.updateHostConfiguration(location, serverCfg);
	}

	@Override
	protected BambooClient createClient(TaskRepository taskRepository, BambooClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		ServerData serverCfg = getServerData(location, taskRepository, false);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		BambooServerFacade bambooFacade = getBambooFacade(callback);

		return new BambooClient(location, serverCfg, bambooFacade, data);
	}

	public BambooClient createTempClient(TaskRepository taskRepository, BambooClientData data) {
		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(taskRepository);

		ServerData serverCfg = getServerData(location, taskRepository, true);
		HttpSessionCallback callback = getHttpSessionCallback(location, serverCfg);
		BambooServerFacade crucibleServer = getBambooFacade(callback);

		BambooClient tempClient = new BambooClient(location, serverCfg, crucibleServer, data);
		return tempClient;
	}

	private synchronized BambooServerFacade getBambooFacade(HttpSessionCallback callback) {
		if (crucibleServerFacade == null) {
			crucibleServerFacade = BambooServerFacadeImpl.getInstance(LoggerImpl.getInstance());
			crucibleServerFacade.setCallback(callback);
		}
		return crucibleServerFacade;
	}

	public void deleteTempClient(ServerData serverData) {
		clientCallback.removeClient(serverData);
	}

	@Override
	public synchronized void repositoryRemoved(TaskRepository repository) {
		super.repositoryRemoved(repository);

		AbstractWebLocation location = getTaskRepositoryLocationFactory().createWebLocation(repository);
		ServerData serverCfg = getServerData(location, repository, false);
		clientCallback.removeClient(serverCfg);

		BambooCorePlugin.getBuildPlanManager().repositoryRemoved(repository);
	}

	private HttpSessionCallback getHttpSessionCallback(AbstractWebLocation location, ServerData serverCfg) {
		clientCallback.initializeHostConfiguration(location, serverCfg);
		return clientCallback;
	}

	@Override
	public TaskRepositoryLocationFactory getTaskRepositoryLocationFactory() {
		TaskRepositoryLocationFactory parentFactory = super.getTaskRepositoryLocationFactory();
		if (parentFactory == null) {
			return new TaskRepositoryLocationFactory();
		} else {
			return parentFactory;
		}
	}

	@Override
	protected BambooClientData createRepositoryConfiguration() {
		return new BambooClientData();
	}

	/**
	 * For testing purposes only
	 * 
	 * @return
	 */
	public HttpSessionCallbackImpl getClientCallback() {
		return clientCallback;
	}

	protected BambooClientData getConfiguration(BambooClient client) {
		return client.getClientData();
	}

	/*
	 * temporary fix for the broken/not-working serialization mechanism 
	 */
	@Override
	protected void updateClientDataMap(Map<String, BambooClient> clientByUrl,
			Map<String, BambooClientData> clientDataByUrl) {
		for (Entry<String, BambooClient> entry : clientByUrl.entrySet()) {
			String url = entry.getKey();
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, getConfiguration(entry.getValue()));
			}
		}
	}

	@Override
	protected void removeClient(TaskRepository repository, Map<String, BambooClient> clientByUrl,
			Map<String, BambooClientData> clientDataByUrl) {
		String url = repository.getRepositoryUrl();
		BambooClient client = clientByUrl.remove(url);
		if (client != null) {
			if (clientDataByUrl.containsKey(url)) {
				clientDataByUrl.put(url, getConfiguration(client));
			}
		}
	}

	@Override
	protected ObjectInput createObjectInput(File cacheFile) throws FileNotFoundException, IOException {
		return new ObjectInputStream(new FileInputStream(cacheFile));
	}

	@Override
	protected ObjectOutput createObjectOutput(File cacheFile) throws IOException {
		return new ObjectOutputStream(new FileOutputStream(cacheFile));
	}

	@Override
	protected void readCache() {
		if (getCacheFile() == null || !getCacheFile().exists()) {
			return;
		}

		ObjectInput in = null;
		try {
			in = createObjectInput(getCacheFile());
			int size = in.readInt();
			for (int i = 0; i < size; i++) {
				String url = (String) in.readObject();
				BambooClientData data = (BambooClientData) in.readObject();
				if (url != null && data != null) {
					getClientDataByUrl().put(url, data);
				}
			}
		} catch (Throwable e) {
			StatusHandler.log(new Status(IStatus.WARNING, BambooCorePlugin.PLUGIN_ID,
					"The repository configuration cache could not be read", e));
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
