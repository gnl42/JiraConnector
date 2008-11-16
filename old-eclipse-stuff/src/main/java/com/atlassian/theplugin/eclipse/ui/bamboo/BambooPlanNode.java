package com.atlassian.theplugin.eclipse.ui.bamboo;

import org.eclipse.jface.resource.ImageDescriptor;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;

public class BambooPlanNode extends BambooFictiveNode {
	private BambooPlan bambooPlan;
	
	public BambooPlan getBambooPlan() {
		return bambooPlan;
	}

	public BambooPlanNode(BambooPlan plan) {
		this.bambooPlan = plan;
	}

	public Object[] getChildren(Object o) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImageDescriptor getImageDescriptor(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getLabel(Object o) {
		return bambooPlan.getPlanName() + " (" + bambooPlan.getPlanKey() + ")";
	}
}
