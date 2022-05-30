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

import java.util.ArrayList;
import java.util.Collection;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.JDomDriver;

import me.glindholm.theplugin.commons.cfg.BambooServerCfg;
import me.glindholm.theplugin.commons.cfg.JiraServerCfg;
import me.glindholm.theplugin.commons.cfg.PrivateBambooServerCfgInfo;
import me.glindholm.theplugin.commons.cfg.PrivateProjectConfiguration;
import me.glindholm.theplugin.commons.cfg.PrivateServerCfgInfo;
import me.glindholm.theplugin.commons.cfg.ProjectConfiguration;
import me.glindholm.theplugin.commons.cfg.ServerCfg;
import me.glindholm.theplugin.commons.cfg.ServerIdImpl;
import me.glindholm.theplugin.commons.cfg.SharedServerList;
import me.glindholm.theplugin.commons.cfg.SubscribedPlan;

public final class JDomXStreamUtil {
    private static final String PLAN = "plan";
    private static final String PLAN_KEY = "key";
    private static final String GROUPED_KEY = "grouped";

    private JDomXStreamUtil() {
    }

    public static XStream getProjectJDomXStream() {
        return getProjectJDomXStream(false);
    }

    public static XStream getProjectJDomXStream(boolean saveAll) {


        XStream xStream = new XStream(new Sun14ReflectionProvider(), new JDomDriver());
        xStream.setMode(XStream.ID_REFERENCES);
        xStream.alias("bamboo", BambooServerCfg.class);
        xStream.alias("jira", JiraServerCfg.class);

        xStream.alias(PLAN, SubscribedPlan.class);

        xStream.omitField(ServerCfg.class, "deleted");

        if (!saveAll) {
            xStream.omitField(ServerCfg.class, "username");
            xStream.omitField(ServerCfg.class, "password");
            xStream.omitField(ServerCfg.class, "isPasswordStored");
            xStream.omitField(ServerCfg.class, "useDefaultCredentials");

            xStream.omitField(JiraServerCfg.class, "dontUseBasicAuth");
            xStream.omitField(JiraServerCfg.class, "basicHttpUser");

            xStream.omitField(BambooServerCfg.class, "timezoneOffset");
        }

        xStream.aliasField("server-id", ServerCfg.class, "serverId");

        if (!saveAll) {
            xStream.omitField(ServerCfg.class, "isEnabled");
        }
        xStream.aliasField("use-favourites", ServerCfg.class, "isUseFavourites");
        xStream.aliasField("bamboo2", ServerCfg.class, "isBamboo2");
        xStream.aliasField("show-branches", ServerCfg.class, "showBranches");
        xStream.aliasField("my-branches-only", ServerCfg.class, "myBranchesOnly");

        xStream.alias("project-configuration", ProjectConfiguration.class);
        xStream.aliasField("default-jira-server", ProjectConfiguration.class, "defaultJiraServerId");

        xStream.alias("private-server-cfg", PrivateServerCfgInfo.class);
        xStream.alias("private-bamboo-server-cfg", PrivateBambooServerCfgInfo.class);
        xStream.aliasField("server-id", PrivateServerCfgInfo.class, "serverId");
        xStream.aliasField("enabled", PrivateServerCfgInfo.class, "isEnabled");
        xStream.aliasField("timezone-offset", PrivateBambooServerCfgInfo.class, "timezoneOffset");
        xStream.aliasField("used-default-credentials", PrivateServerCfgInfo.class, "useDefaultCredentials");
        xStream.aliasField("basic-password", PrivateServerCfgInfo.class, "basicPassword");
        xStream.aliasField("basic-username", PrivateServerCfgInfo.class, "basicUsername");
        xStream.aliasField("use-http-basic", PrivateServerCfgInfo.class, "useHttpBasic");

        xStream.registerLocalConverter(PrivateServerCfgInfo.class, "password", new EncodedStringConverter());
        xStream.registerLocalConverter(PrivateServerCfgInfo.class, "basicPassword", new EncodedStringConverter());


        xStream.alias("private-project-cfg", PrivateProjectConfiguration.class);
        xStream.aliasField("private-server-cfgs", PrivateProjectConfiguration.class, "privateServerCfgInfos");
        xStream.aliasField("plans", PrivateProjectConfiguration.class, "plans");
        xStream.aliasField("useFavourites", PrivateProjectConfiguration.class, "use-favourites");
        xStream.aliasField("showBranches", PrivateProjectConfiguration.class, "show-branches");
        xStream.aliasField("myBranchesOnly", PrivateProjectConfiguration.class, "my-branches-only");


        xStream.aliasField("plans", BambooServerCfg.class, "plans");
        xStream.registerLocalConverter(ServerCfg.class, "password", new EncodedStringConverter());


        xStream.alias("shared-server-list", SharedServerList.class);

        xStream.addDefaultImplementation(ArrayList.class, Collection.class);

//		xStream.registerLocalConverter(BambooServerCfg.class, "plans", new Converter() {
//			public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
//				final Collection<?> plans = (Collection<?>) source;
//				for (Object o : plans) {
//					if (o instanceof SubscribedPlan) {
//						final SubscribedPlan plan = (SubscribedPlan) o;
//						writer.startNode(PLAN);
//						context.convertAnother(plan);
//						writer.endNode();
//					}
//				}
//			}
//
//			public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
//				Collection<SubscribedPlan> res = MiscUtil.buildArrayList();
//				while (reader.hasMoreChildren()) {
//					reader.moveDown();
//					if (PLAN.equals(reader.getNodeName())) {
//						SubscribedPlan plan = (SubscribedPlan) context.convertAnother(res, SubscribedPlan.class);
//						res.add(plan);
//					}
//					reader.moveUp();
//				}
//				return res;
//			}
//
//			public boolean canConvert(final Class aClass) {
//				return Collection.class.isAssignableFrom(aClass);
//			}
//		});
        xStream.registerConverter(new Converter() {

            @Override
            public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
                SubscribedPlan value = (SubscribedPlan) source;
                writer.addAttribute(PLAN_KEY, value.getKey());
                writer.addAttribute(GROUPED_KEY, value.isGrouped() ? "1" : "0");
                //writer.setValue(value.getPlanId());
            }

            @Override
            public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
                return new SubscribedPlan(reader.getAttribute(PLAN_KEY),
                        "1".equals(reader.getAttribute(GROUPED_KEY)));
            }

            @Override
            public boolean canConvert(final Class aClass) {
                return SubscribedPlan.class.isAssignableFrom(aClass);
            }
        });
        xStream.registerConverter(new Converter() {

            @Override
            public void marshal(final Object source, final HierarchicalStreamWriter writer, final MarshallingContext context) {
                ServerIdImpl value = (ServerIdImpl) source;
                writer.setValue(value.getId());
            }

            @Override
            public Object unmarshal(final HierarchicalStreamReader reader, final UnmarshallingContext context) {
                return new ServerIdImpl(reader.getValue());
            }

            @Override
            public boolean canConvert(final Class type) {
                return type.equals(ServerIdImpl.class);
            }
        });
        return xStream;

    }
}
