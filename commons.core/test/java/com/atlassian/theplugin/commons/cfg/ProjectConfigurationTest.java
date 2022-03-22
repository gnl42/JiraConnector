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
package me.glindholm.theplugin.commons.cfg;

import me.glindholmtheplugin.commons.util.MiscUtil;
import com.spartez.util.junit3.TestUtil;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Iterator;

/**
 * ProjectConfiguration Tester.
 *
 * @author wseliga
 */
public class ProjectConfigurationTest extends TestCase {

    private static final BambooServerCfg BAMBOO_1 = new BambooServerCfg("mybamboo", new ServerIdImpl());
    ProjectConfiguration projectCfg;
    ProjectConfiguration anotherCfg;

    protected void setUp() throws Exception {
        super.setUp();

        projectCfg = new ProjectConfiguration(MiscUtil.buildArrayList(BAMBOO_1));
        anotherCfg = new ProjectConfiguration(projectCfg);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHashCode() {
        assertEquals(projectCfg.hashCode(), anotherCfg.hashCode());
    }

    public void testGetClone() {
        final ProjectConfiguration clone = projectCfg.getClone();
        assertNotSame(clone, projectCfg);
        assertEquals(projectCfg, clone);
        assertEquals(projectCfg.getServers(), clone.getServers());

        final Iterator<ServerCfg> oIt = projectCfg.getServers().iterator();
        final Iterator<ServerCfg> cIt = clone.getServers().iterator();
        while (oIt.hasNext() && cIt.hasNext()) {
            final ServerCfg oServer = oIt.next();
            final ServerCfg cServer = cIt.next();
            assertNotSame(oServer, cServer);
            assertEquals(oServer, cServer);
        }
    }

    public void testGetJiraServers() {

        Collection<JiraServerCfg> jiraServers = MiscUtil.buildArrayList();
        jiraServers.add(new JiraServerCfg("server1", new ServerIdImpl(), true));
        jiraServers.add(new JiraServerCfg("server2", new ServerIdImpl(), true));

        projectCfg.getServers().addAll(jiraServers);

        assertEquals(jiraServers, projectCfg.getAllJIRAServers());
    }

    public void testGetBambooServers() {

        Collection<BambooServerCfg> bambooServers = MiscUtil.buildArrayList();
        bambooServers.add(new BambooServerCfg("server1", new ServerIdImpl()));
        bambooServers.add(new BambooServerCfg("server2", new ServerIdImpl()));

        projectCfg.getServers().remove(BAMBOO_1);
        projectCfg.getServers().addAll(bambooServers);

        assertEquals(bambooServers, projectCfg.getAllBambooServers());
    }

}
