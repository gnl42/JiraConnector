package com.atlassian.theplugin.jira;

import com.atlassian.theplugin.commons.cfg.JiraServerCfg;
import com.atlassian.theplugin.commons.cfg.ServerIdImpl;
import com.atlassian.theplugin.commons.cfg.UserCfg;
import com.atlassian.theplugin.commons.jira.JiraServerData;
import junit.framework.TestCase;

public class JiraServerDataTest extends TestCase {
	public void testBasicAuth() {
		JiraServerCfg serverCfg = new JiraServerCfg("name", new ServerIdImpl(), false);
		serverCfg.setBasicHttpUser(new UserCfg("piotr", "singleton"));
		JiraServerData.Builder builder = new JiraServerData.Builder(serverCfg);
		JiraServerData serverData = builder.build();

		assertTrue(serverData.getBasicUser().equals(serverCfg.getBasicHttpUser()));
		assertTrue(serverData.isUseBasicUser());
	}

	public void testDefaultUser() {
		JiraServerCfg serverCfg = new JiraServerCfg("name", new ServerIdImpl(), false);
		serverCfg.setUsername("piotr");
		serverCfg.setPassword("singleton");
		serverCfg.setUseDefaultCredentials(true);
		JiraServerData.Builder builder = new JiraServerData.Builder(serverCfg);
		builder.defaultUser(new UserCfg("defaultUser", "defaultPass"));
		JiraServerData serverData = builder.build();

		assertEquals(serverData.getUsername(), "defaultUser");
		assertEquals(serverData.getPassword(), "defaultPass");

		serverCfg.setUseDefaultCredentials(false);
		builder = new JiraServerData.Builder(serverCfg);
		serverData = builder.build();

		assertEquals(serverData.getUsername(), "piotr");
		assertEquals(serverData.getPassword(), "singleton");
	}
}
