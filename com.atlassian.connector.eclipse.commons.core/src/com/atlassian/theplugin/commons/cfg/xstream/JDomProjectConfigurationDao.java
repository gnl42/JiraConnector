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
package com.atlassian.theplugin.commons.cfg.xstream;

import com.atlassian.theplugin.commons.cfg.PrivateConfigurationDao;
import com.atlassian.theplugin.commons.cfg.PrivateProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.PrivateServerCfgInfo;
import com.atlassian.theplugin.commons.cfg.ProjectConfiguration;
import com.atlassian.theplugin.commons.cfg.ProjectConfigurationDao;
import com.atlassian.theplugin.commons.cfg.ServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerCfgFactoryException;
import com.atlassian.theplugin.commons.cfg.SharedServerList;
import com.atlassian.theplugin.commons.util.LoggerImpl;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.JDom2Reader;
import com.thoughtworks.xstream.io.xml.JDom2Writer;
import org.jdom2.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class JDomProjectConfigurationDao implements ProjectConfigurationDao {

    private final Element publicElement;
    private final PrivateConfigurationDao privateConfigurationDao;
    private final UserSharedConfigurationDao sharedCfg;


    public JDomProjectConfigurationDao(final Element element, @NotNull PrivateConfigurationDao privateConfigurationDao,
            @NotNull UserSharedConfigurationDao userSharedCfg) {
        if (element == null) {
            throw new IllegalArgumentException(Element.class.getSimpleName() + " cannot be null");
        }
        // we compile using Maven2. @NotNull has no meaning in product
        //noinspection ConstantConditions
        if (privateConfigurationDao == null) {
            throw new IllegalArgumentException(PrivateConfigurationDao.class.getSimpleName() + " cannot be null");
        }
        this.publicElement = element;
        this.privateConfigurationDao = privateConfigurationDao;
        this.sharedCfg = userSharedCfg;

    }

    public ProjectConfiguration load() throws ServerCfgFactoryException {
        PrivateProjectConfiguration ppc = new PrivateProjectConfiguration();
        ProjectConfiguration res = load(publicElement, ProjectConfiguration.class);


        @Nullable
        final SharedServerList sharedServers = sharedCfg.load();
        if (sharedServers != null) {
            Collection<ServerCfg> serversToAdd = new ArrayList<ServerCfg>();
            if (res.getServers().size() > 0) {
                for (ServerCfg sharedServer : sharedServers) {

                    if (res.getServerCfg(sharedServer.getServerId()) == null) {
                        serversToAdd.add(sharedServer);
                    }
                }
            } else {
                serversToAdd.addAll(sharedServers);
            }
//            res.getServers().addAll(sharedServers);
            res.getServers().addAll(serversToAdd);
        }

        for (ServerCfg serverCfg : res.getServers()) {
            try {
                if (!serverCfg.isShared()) {
                    @Nullable
                    final PrivateServerCfgInfo privateServerCfgInfo
                            = privateConfigurationDao.load(serverCfg.getServerId());
                    if (privateServerCfgInfo != null) {
                        ppc.add(privateServerCfgInfo);
                    }
                }
            } catch (ServerCfgFactoryException e) {
                // let us ignore problem for the moment - there will be no private settings for such servers
            }
        }

        return merge(res, ppc);
    }


    public PrivateProjectConfiguration loadOldPrivateConfiguration(@NotNull Element privateElement)
            throws ServerCfgFactoryException {
        return load(privateElement, PrivateProjectConfiguration.class);
    }


    private <T> T load(final Element rootElement, Class<T> clazz) throws ServerCfgFactoryException {
        final int childCount = rootElement.getChildren().size();
        if (childCount != 1) {
            throw new ServerCfgFactoryException("Cannot travers JDom tree. Exactly one child node expected, but found ["
                    + childCount + "]");
        }
		final JDom2Reader reader = new JDom2Reader(rootElement.getChildren().get(0));
        final XStream xStream = JDomXStreamUtil.getProjectJDomXStream(false);
        try {
            return clazz.cast(xStream.unmarshal(reader));
        } catch (ClassCastException e) {
            throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + " due to ClassCastException: "
                    + e.getMessage(), e);
        } catch (Exception e) {
            throw new ServerCfgFactoryException("Cannot load " + clazz.getSimpleName() + ": "
                    + e.getMessage(), e);
        }
    }


    private ProjectConfiguration merge(final ProjectConfiguration projectConfiguration,
            final PrivateProjectConfiguration privateProjectConfiguration) {
        for (ServerCfg serverCfg : projectConfiguration.getServers()) {
            if (!serverCfg.isShared()) {
                PrivateServerCfgInfo psci = privateProjectConfiguration
                        .getPrivateServerCfgInfo(serverCfg.getServerId());
                serverCfg.mergePrivateConfiguration(psci);
            }
        }
        return projectConfiguration;
    }

    public void save(final ProjectConfiguration projectConfiguration) {
        final Collection<ServerCfg> privateServers = new ArrayList<ServerCfg>();
        final SharedServerList gList = new SharedServerList();
        for (ServerCfg server : projectConfiguration.getServers()) {
            if (server.isShared()) {
                gList.add(server);
//                if (projectConfiguration.getServerCfg(server.getServerId()) == null) {
//                    gList.add(server);
//                } else {
//                    projectConfiguration.getServerCfg(server.getServerId()).setShared(true);
//                }

                if (privateConfigurationDao instanceof HomeDirPrivateConfigurationDao) {
                    ((HomeDirPrivateConfigurationDao) privateConfigurationDao).deleteFile(server);
                }
            } else {
                try {

                    privateConfigurationDao.save(server.createPrivateProjectConfiguration());
                    privateServers.add(server);
                } catch (ServerCfgFactoryException e) {
                    LoggerImpl.getInstance().error("Cannot write private cfg file for server Uuid = "
                            + server.getServerId().getId());
                }
            }
        }

        final ProjectConfiguration configurationToSave = new ProjectConfiguration(projectConfiguration);
        configurationToSave.getServers().clear();
        configurationToSave.getServers().addAll(privateServers);
        save(configurationToSave, publicElement);

        try {
            sharedCfg.save(gList, projectConfiguration.getServers());
        } catch (ServerCfgFactoryException e) {
            LoggerImpl.getInstance().error("Cannot write private shared config file ", e);
        }
    }

    void save(final Object object, final Element rootElement) {
        if (object == null) {
            throw new NullPointerException("Serialized object cannot be null");
        }
		final JDom2Writer writer = new JDom2Writer(rootElement);
        final XStream xStream = JDomXStreamUtil.getProjectJDomXStream(false);
        xStream.marshal(object, writer);

    }

    static PrivateServerCfgInfo createPrivateProjectConfiguration(final ServerCfg serverCfg) {
        return serverCfg.createPrivateProjectConfiguration();
    }
}
