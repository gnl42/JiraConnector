package com.atlassian.theplugin.eclipse.util;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import com.atlassian.theplugin.commons.RequestDataInfo;
import com.atlassian.theplugin.commons.bamboo.BuildStatus;

public class PluginIcons {
	private static ImageRegistry imageRegistry;

	public static final String ICON_BAMBOO = "Bamboo";
	public static final String ICON_BAMBOO_LARGE = "Bamboo_large";
	public static final String ICON_BAMBOO_NEW = "Bamboo_new";

	public static final String ICON_BAMBOO_RUN = "Run_bamboo_build";
	public static final String ICON_BAMBOO_LABEL = "Label_bamboo_build";
	public static final String ICON_BAMBOO_COMMENT = "Comment_bamboo_build";
	
	public static final String ICON_BAMBOO_REFRESH = "Refresh_bamboo_builds";
	public static final String ICON_BAMBOO_GET_FULL_LOG = "Get_log_bamboo_build";
	
	public static final String ICON_BAMBOO_UNKNOWN = BuildStatus.UNKNOWN.toString();
	public static final String ICON_BAMBOO_FAILED = BuildStatus.BUILD_FAILED.toString();
	public static final String ICON_BAMBOO_SUCCEEDED = BuildStatus.BUILD_SUCCEED.toString();
	
	public static final String ICON_CLOSE = "Close";
	public static final String ICON_FAVOURITE_ON = "Favourite on";
	public static final String ICON_FAVOURITE_OFF = "Favourite off";
	
	public static final String ICON_COLLAPSE_ALL = "Collapse_all";

	public static final String ICON_PLUGIN = "Plugin";

	public static final String ICON_REFRESH_PENDING = ICON_PLUGIN;

	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			imageRegistry.put(ICON_BAMBOO, ImageDescriptor.createFromFile(RequestDataInfo.class, "/icons/bamboo-blue-16.png"));
			imageRegistry.put(ICON_BAMBOO_LARGE, ImageDescriptor.createFromFile(RequestDataInfo.class, "/icons/atlassian_icon-70.png"));
			imageRegistry.put(ICON_BAMBOO_NEW, imageRegistry.get(ICON_BAMBOO));
		
			imageRegistry.put(ICON_BAMBOO_SUCCEEDED, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_passed.gif"));
			imageRegistry.put(ICON_BAMBOO_FAILED, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_failed.gif"));
			imageRegistry.put(ICON_BAMBOO_UNKNOWN, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_plan_unknown.gif"));
	
			imageRegistry.put(ICON_BAMBOO_RUN, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/run.gif"));
			imageRegistry.put(ICON_BAMBOO_LABEL, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_label.gif"));
			imageRegistry.put(ICON_BAMBOO_COMMENT, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_comment.png"));
			imageRegistry.put(ICON_BAMBOO_REFRESH, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/refresh.gif"));
			
			// TODO change get_full_log icon
			imageRegistry.put(ICON_BAMBOO_GET_FULL_LOG, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/bamboo_log.gif"));
			
			imageRegistry.put(ICON_BAMBOO_REFRESH, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/refresh.gif"));
			
			imageRegistry.put(ICON_CLOSE, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/close.gif"));
			
			imageRegistry.put(ICON_FAVOURITE_ON, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/favourite_on.gif"));
			imageRegistry.put(ICON_FAVOURITE_OFF, ImageDescriptor.createFromFile(PluginUtil.class, "/icons/favourite_off.gif"));
			
			imageRegistry.put(ICON_COLLAPSE_ALL, ImageDescriptor.createFromFile(RequestDataInfo.class, "/icons/common/collapseall.gif"));

			imageRegistry.put(ICON_PLUGIN, ImageDescriptor.createFromFile(RequestDataInfo.class, "/icons/ico_plugin.png"));
		}
		
		return PluginIcons.imageRegistry;
	}

}
