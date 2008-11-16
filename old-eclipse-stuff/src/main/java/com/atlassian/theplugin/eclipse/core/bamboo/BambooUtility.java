package com.atlassian.theplugin.eclipse.core.bamboo;

import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

public final class BambooUtility {

	private BambooUtility() {
		// utility class
	}
	
	public static Exception validateBambooLocation(IBambooServer location) {
		BambooServerFacade bambooFacade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		try {
			bambooFacade.testServerConnection(location.getUrl(), location.getUsername(), location.getPassword());
		} catch (final Exception ex) {
			return ex;
		}
		return null;
	}
	
}
