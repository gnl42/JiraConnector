/**
 * Copyright (C) 2008 Atlassian
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.glindholm.theplugin.commons.cfg.xstream;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import me.glindholm.theplugin.commons.cfg.ServerCfg;
import me.glindholm.theplugin.commons.cfg.ServerCfgFactoryException;
import me.glindholm.theplugin.commons.cfg.SharedServerList;
import me.glindholm.theplugin.commons.util.LoggerImpl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

/**
 * @autrhor pmaruszak
 * @date Jun 10, 2010
 */


public class HomeDirSharedConfigurationImpl
		extends BasePrivateConfigurationDao<SharedServerList>
		implements UserSharedConfigurationDao {
	private static final String GLOBAL_SERVERS_FILE_NAME = "shared-servers";

	/**
	 * This method is blocking for a very short period of time in some cases
	 */
	public synchronized void save(SharedServerList serversInfo, Collection<ServerCfg> allServers) throws ServerCfgFactoryException {
		Document document;
		File outputFile = null;

		try {
			outputFile = new File(getPrivateCfgDirectorySavePath(), GLOBAL_SERVERS_FILE_NAME);
			if (!outputFile.exists()) {
                outputFile.createNewFile();
			}
			SharedServerList storedList = load();
			if (outputFile.exists() && outputFile.canWrite()) {
				if (storedList != null) {
					//merge existing cfg
					document = createJDom(SharedServerList.merge(serversInfo, storedList, allServers));
//                    document = createJDom(serversInfo);
				} else {
					document = createJDom(serversInfo);
				}

				writeXmlFile(document.getRootElement(), outputFile);
			} else {
                LoggerImpl.getInstance().error("Shared configuration hasn't been saved - cannot write file " + outputFile.getAbsolutePath());
				throw new ServerCfgFactoryException("Shared configuration hasn't been saved");
			}

		} catch (Exception e) {
            LoggerImpl.getInstance().error("Shared configuration hasn't been saved", e);
            final ServerCfgFactoryException ex = new ServerCfgFactoryException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

	public SharedServerList load() throws ServerCfgFactoryException {
		final File atlassianDir = getPrivateCfgDirectorySavePath();

		if (isDirReady()) {
			final File sharedConfigFile = new File(atlassianDir.getAbsolutePath(), GLOBAL_SERVERS_FILE_NAME);
			if (sharedConfigFile.isFile() && sharedConfigFile.canRead() && sharedConfigFile.length() > 0) {
				Document doc;

				final SAXBuilder builder = new SAXBuilder(false);
				try {
					doc = builder.build(sharedConfigFile.toURI().toString());
				} catch (JDOMException e) {
					throw new ServerCfgFactoryException("Cannot parse shared cfg file " + e.getMessage());
				} catch (IOException e) {
					throw new ServerCfgFactoryException("Cannot read shared cfg file " + sharedConfigFile.getAbsolutePath() + " : " + e.getMessage());
				}

				SharedServerList globalServerInfos = null;
				if (doc != null) {
					globalServerInfos = loadJDom(doc.getRootElement(), SharedServerList.class, true);
				}
				return globalServerInfos;
			} else {
				return null;
			}

		} else {
			throw new ServerCfgFactoryException("Cannot read shared configuration stored in directory ["
					+ atlassianDir.getAbsolutePath() + "]. Directory does not exist or is not accessible");
		}
	}


	@Override
	String getRootElementName() {
		return GLOBAL_SERVERS_FILE_NAME;
	}
}
