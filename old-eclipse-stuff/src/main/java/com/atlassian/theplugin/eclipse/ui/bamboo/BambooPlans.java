package com.atlassian.theplugin.eclipse.ui.bamboo;

import java.util.ArrayList;

import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.eclipse.core.operation.CompositeOperation;
import com.atlassian.theplugin.eclipse.core.operation.IActionOperation;
import com.atlassian.theplugin.eclipse.core.operation.LoggedOperation;
import com.atlassian.theplugin.eclipse.preferences.Activator;
import com.atlassian.theplugin.eclipse.ui.RefreshPending;
import com.atlassian.theplugin.eclipse.ui.operation.bamboo.GetBambooPlansChildrenOperation;
import com.atlassian.theplugin.eclipse.ui.utility.DefaultOperationWrapperFactory;
import com.atlassian.theplugin.eclipse.ui.utility.UIMonitorUtil;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.IDataTreeNode;
import com.atlassian.theplugin.eclipse.view.bamboo.IParentTreeNode;

public class BambooPlans extends BambooFictiveNode implements IBambooTreeNode,
		IDataTreeNode, IParentTreeNode {
	private BambooServerNode server;
	private GetBambooPlansChildrenOperation childrenOp;
	private BambooTreeViewer bambooTree;

	public BambooPlans(BambooServerNode server) {
		this.server = server;
	}

	public Object[] getChildren(Object o) {
		// final IRepositoryContainer container =
		// (IRepositoryContainer)this.resource;

		if (this.childrenOp != null) {
			Object[] retVal = wrapChildren(this.childrenOp.getChildren());
			return retVal == null ? new Object[] { this.childrenOp
					.getExecutionState() != IActionOperation.ERROR ? (Object) new RefreshPending(
					this)
					: new BambooErrorNode(this.childrenOp.getStatus()) }
					: retVal;
		} else {
			this.childrenOp = new GetBambooPlansChildrenOperation(server
					.getBambooServer());
			/* if (!((IRepositoryContainer)this.resource).isChildrenCached()) { */
			CompositeOperation op = new CompositeOperation(this.childrenOp
					.getId());
			op.add(this.childrenOp);
			op.add(server.getRefreshOperation(this.bambooTree));

			UIMonitorUtil.doTaskScheduled(op,
					new DefaultOperationWrapperFactory() {
						public IActionOperation getLogged(
								IActionOperation operation) {
							return new LoggedOperation(operation);
						}
					});
			return new Object[] { new RefreshPending(this) };
			/*
			 * }
			 * 
			 * UIMonitorUtil.doTaskBusyDefault(this.childrenOp);
			 * 
			 * return RepositoryFolder.wrapChildren(this,
			 * this.childrenOp.getChildren(), this.childrenOp);
			 */
		}
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry()
				.get(PluginIcons.ICON_BAMBOO));
	}

	public String getLabel(Object o) {
		return Activator.getDefault().getResource(
				"BambooServerView.Model.Plans");
	}

	public void refresh() {
		this.childrenOp = null;
	}

	public boolean hasChildren() {
		return true;
	}

	public Object getData() {
		// TODO Auto-generated method stub
		return null;
	}

	protected BambooPlanNode[] wrapChildren(BambooPlan[] plans) {
		if (plans == null) {
			return null;
		}
		ArrayList<BambooPlanNode> children = new ArrayList<BambooPlanNode>(
				plans.length);
		if (plans != null) {
			for (BambooPlan plan : plans) {
				children.add(new BambooPlanNode(plan));
			}
		}
		return children.toArray(new BambooPlanNode[children.size()]);
	}

	public void setViewer(BambooTreeViewer viewer) {
		this.bambooTree = viewer;
	}
}
