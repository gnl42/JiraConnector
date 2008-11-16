package com.atlassian.theplugin.eclipse.ui.operation.bamboo;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacade;
import com.atlassian.theplugin.commons.bamboo.BambooServerFacadeImpl;
import com.atlassian.theplugin.commons.configuration.ServerBean;
import com.atlassian.theplugin.eclipse.core.bamboo.IBambooServer;
import com.atlassian.theplugin.eclipse.core.operation.AbstractNonLockingOperation;
import com.atlassian.theplugin.eclipse.util.PluginUtil;

public class GetBambooPlansChildrenOperation extends
		AbstractNonLockingOperation {
	
	private IBambooServer server;
	private BambooPlan[] children;
	
	public GetBambooPlansChildrenOperation(IBambooServer server) {
		super("Operation.GetBambooPlansChildren");
		this.server = server;
	}

	@Override
	protected void runImpl(IProgressMonitor monitor) throws Exception {
		BambooServerFacade facade = BambooServerFacadeImpl.getInstance(PluginUtil.getLogger());
		ServerBean serverConfiguration = new ServerBean();
		serverConfiguration.setName(server.getLabel());
		serverConfiguration.setUserName(server.getUsername());
		serverConfiguration.setUrlString(server.getUrl());
		serverConfiguration.transientSetPasswordString(server.getPassword(), false);
		Collection<BambooPlan> plans = facade.getPlanList(serverConfiguration);
		children = plans.toArray(new BambooPlan[plans.size()]);
	}

	public BambooPlan[] getChildren() {
		return children;
	}
}
