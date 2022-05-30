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

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import me.glindholm.theplugin.commons.cfg.PrivateConfigurationDao;
import me.glindholm.theplugin.commons.cfg.PrivateServerCfgInfo;
import me.glindholm.theplugin.commons.cfg.ServerCfg;
import me.glindholm.theplugin.commons.cfg.ServerCfgFactoryException;
import me.glindholm.theplugin.commons.cfg.ServerId;

/**
 * User: pmaruszak
 */

public class HomeDirPrivateConfigurationDao
extends BasePrivateConfigurationDao<PrivateServerCfgInfo>
implements PrivateConfigurationDao {

    private static final String ROOT_ELEMENT_NAME = "single-server-private-cfg";

    @Override
    public PrivateServerCfgInfo load(final ServerId id) throws ServerCfgFactoryException {
        final File atlassianDir = getPrivateCfgDirectorySavePath();

        if (isDirReady()) {
            final File serverCfgFile = new File(atlassianDir.getAbsolutePath(), id.toString());
            if (serverCfgFile.isFile() && serverCfgFile.canRead()) {
                Document doc;

                final SAXBuilder builder = new SAXBuilder();
                try {

                    doc = builder.build(serverCfgFile.toURI().toString());
                } catch (JDOMException e) {
                    throw new ServerCfgFactoryException("Cannot parse server cfg file " + e.getMessage());
                } catch (IOException e) {
                    throw new ServerCfgFactoryException("Cannot read sever cfg file " + e.getMessage());
                }

                PrivateServerCfgInfo privateServerCfgInfo = null;
                if (doc != null) {
                    privateServerCfgInfo = load(doc);
                }
                return privateServerCfgInfo;
            } else {
                return null;
            }

        } else {
            throw new ServerCfgFactoryException("Cannot read private configuration stored in directory ["
                    + atlassianDir.getAbsolutePath() + "]. Directory does not exist or is not accessible");
        }

    }

    static PrivateServerCfgInfo load(final Document doc) throws ServerCfgFactoryException {
        return loadJDom(doc.getRootElement(), PrivateServerCfgInfo.class, false);
    }

    @Override
    public void save(@Nonnull final PrivateServerCfgInfo info) throws ServerCfgFactoryException {
        Document document = createJDom(info);

        try {
            writeXmlFile(document.getRootElement(), new File(getPrivateCfgDirectorySavePath(),
                    info.getServerId().toString()));
        } catch (IOException e) {
            final ServerCfgFactoryException ex = new ServerCfgFactoryException(e.getMessage());
            ex.initCause(e);
            throw ex;
        }

    }

    /*Target filr in  $HOME/.atlassian/ide-connector/atlassina-ide-connector*/


    @Override
    String getRootElementName() {
        return ROOT_ELEMENT_NAME;
    }


    public void deleteFile(ServerCfg server) {
        final File toDelete = new File(getPrivateCfgDirectoryPath(), server.getServerId().toString());
        if (toDelete.exists() && toDelete.canWrite()) {
            toDelete.delete();
        }
    }
}
