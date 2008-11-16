package com.atlassian.theplugin.eclipse.view.bamboo;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.internal.utils.UniversalUniqueIdentifier;
import org.eclipse.core.runtime.IPath;

import com.atlassian.theplugin.eclipse.core.bamboo.BambooServer;
import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.LoggedOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;

public class BambooConfigurationStorage {

	public static final String STATE_INFO_FILE_NAME = ".bambooServers";

	private static BambooConfigurationStorage instance = new BambooConfigurationStorage();

	private File stateInfoFile;

	private IBambooServer[] bambooServers;

	protected BambooConfigurationStorage() {
		this.bambooServers = new IBambooServer[0];
	}

	@SuppressWarnings("restriction")
	public IBambooServer newBambooServer() {
		return new BambooServer(new UniversalUniqueIdentifier().toString());
	}

	public void copyBambooServer(IBambooServer to, IBambooServer from) {
		to.setLabel(from.getLabel());
		to.setUsername(from.getUsername());
		to.setPassword(from.getPassword());
		to.setPasswordSaved(from.isPasswordSaved());
		to.setUrl(from.getUrl());
	}

	public static BambooConfigurationStorage instance() {
		return BambooConfigurationStorage.instance;
	}

	public void initialize(IPath stateInfoLocation) throws Exception {
		this.initializeImpl(stateInfoLocation, STATE_INFO_FILE_NAME);
	}

	protected void initializeImpl(IPath stateInfoLocation, String fileName)
			throws Exception {
		this.stateInfoFile = stateInfoLocation.append(fileName).toFile();
		if (this.stateInfoFile.createNewFile()) {
			this.saveBambooServers();
		}

		try {
			this.loadBambooServers();
		} catch (Exception e) {
			LoggedOperation.reportError(Activator.getDefault().getResource(
					"Error.LoadBambooServers"), e);
			this.saveBambooServers();
		}
	}

	protected void saveBambooServers() throws Exception {
		ObjectOutputStream stream = null;
		try {
			stream = new ObjectOutputStream(new FileOutputStream(
					this.stateInfoFile));
			for (int i = 0; i < this.bambooServers.length; i++) {
				stream.writeObject(this.bambooServers[i]);
			}
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {
				}
			}
		}
	}

	protected void loadBambooServers() throws Exception {
		List<IBambooServer> tmp = new ArrayList<IBambooServer>(Arrays
				.asList(this.bambooServers));
		ObjectInputStream stream = null;
		try {
			stream = new ObjectInputStream(new FileInputStream(
					this.stateInfoFile));

			// why stream.available() does not work ???
			while (true) {
				IBambooServer obj = (IBambooServer) stream.readObject();
				if (!tmp.contains(obj)) {
					tmp.add(obj);
				}
			}
		} catch (EOFException ex) {
			// EOF, do nothing
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex) {
				}
			}
		}
		this.bambooServers = (IBambooServer[]) tmp
				.toArray(new IBambooServer[tmp.size()]);
	}

	public IBambooServer[] getBambooServers() {
		return this.bambooServers;
	}

	public IBambooServer getBambooServer(String id) {
		for (int i = 0; i < this.bambooServers.length; i++) {
			if (this.bambooServers[i].getId().equals(id)) {
				return this.bambooServers[i];
			}
		}
		return null;
	}

	public synchronized void addBambooServer(IBambooServer server) {
		List<IBambooServer> tmp = new ArrayList<IBambooServer>(Arrays
				.asList(this.bambooServers));
		if (!tmp.contains(server)) {
			tmp.add(server);
			this.bambooServers = (IBambooServer[]) tmp
					.toArray(new IBambooServer[tmp.size()]);
		}
	}

	public void saveConfiguration() throws Exception {
		this.saveBambooServers();
	}

	public synchronized void removeBambooServer(IBambooServer server) {
		List<IBambooServer> tmp = new ArrayList<IBambooServer>(Arrays
				.asList(this.bambooServers));
		if (tmp.remove(server)) {
			this.bambooServers = (IBambooServer[]) tmp
					.toArray(new IBambooServer[tmp.size()]);
		}
	}

}
