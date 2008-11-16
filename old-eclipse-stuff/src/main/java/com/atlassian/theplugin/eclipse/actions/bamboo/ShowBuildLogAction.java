package com.atlassian.theplugin.eclipse.actions.bamboo;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.atlassian.theplugin.commons.exception.ServerPasswordNotProvidedException;
import com.atlassian.theplugin.commons.remoteapi.RemoteApiException;
import com.atlassian.theplugin.eclipse.EclipseActionScheduler;
import com.atlassian.theplugin.eclipse.editors.bamboo.BuildLogEditorInput;
import com.atlassian.theplugin.eclipse.util.PluginIcons;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooBuildAdapterEclipse;
import com.atlassian.theplugin.eclipse.view.bamboo.BambooToolWindow;

public class ShowBuildLogAction extends AbstractBambooAction {

	private static final String SHOW_BUILD_LOG = "Show Build Log";

	public ShowBuildLogAction(BambooToolWindow bambooToolWindowTable) {
		super(bambooToolWindowTable);
	}

	@Override
	public void run() {

		final BambooBuildAdapterEclipse build = getBuild();

		Job labelBuild = new Job("Retrieving build log") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					setUIMessage("Retrieving build log");
					final byte[] log = bambooFacade.getBuildLogs(build.getServer(), build.getBuildKey(), 
							build.getBuildNumber());

					EclipseActionScheduler.getInstance().invokeLater(
							new Runnable() {
								public void run() {
									IEditorInput editorInput = new BuildLogEditorInput(
											new String(log), 
											build.getBuildKey() + "-" + build.getBuildNumber());
									IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
									IWorkbenchPage page = window.getActivePage();
									try {
										page.openEditor(editorInput,
														"com.atlassian.theplugin.eclipse.editors.bamboo.LogEditor");
									} catch (PartInitException e) {
										e.printStackTrace();
									}
								}
							});
					setUIMessage("");
				} catch (ServerPasswordNotProvidedException e) {
					setUIMessage("Error getting build log. Password not provided for server");
				} catch (RemoteApiException e) {
					setUIMessage("Error getting build log. " + e.getMessage());
				}
				return Status.OK_STATUS;
			}
		};

		labelBuild.setPriority(Job.SHORT);
		labelBuild.schedule();

	};

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromImage(PluginIcons.getImageRegistry()
				.get(PluginIcons.ICON_BAMBOO_GET_FULL_LOG));
	}

	@Override
	public String getToolTipText() {
		return SHOW_BUILD_LOG;
	}

	@Override
	public String getText() {
		return SHOW_BUILD_LOG;
	}

	
}
