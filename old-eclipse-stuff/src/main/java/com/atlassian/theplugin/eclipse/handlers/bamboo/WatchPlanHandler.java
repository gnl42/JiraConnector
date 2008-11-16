package com.atlassian.theplugin.eclipse.handlers.bamboo;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import com.atlassian.theplugin.commons.bamboo.BambooPlan;
import com.atlassian.theplugin.eclipse.ui.bamboo.BambooPlanNode;


public class WatchPlanHandler extends AbstractHandler implements IElementUpdater  {
	
	private static boolean checked = false;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		checked = !checked;
		
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		if (selection instanceof TreeSelection) {
			
			Iterator iterator = ((TreeSelection) selection).iterator();
			
			while (iterator.hasNext()) {
				Object planNode = iterator.next();
				if (planNode instanceof BambooPlanNode) {
					BambooPlan bambooPlan = ((BambooPlanNode) planNode).getBambooPlan();
					
					// TODO add bambooPlan to watches
					System.out.println(bambooPlan.getPlanKey());
				}
			}
		}
		
		
		return null;
	}

	public void updateElement(UIElement element, Map parameters) {
//		String contextId = (String) parameters.get(TOGGLE_ID);
//		element.setChecked(ICommandService contextActivations.get(contextId) != null);
		
		//element.setChecked(checked);
	}
	
	

}
