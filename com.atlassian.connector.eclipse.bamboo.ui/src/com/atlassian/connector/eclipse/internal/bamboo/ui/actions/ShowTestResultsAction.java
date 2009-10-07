/*******************************************************************************
 * Copyright (c) 2009 Atlassian and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Atlassian - initial API and implementation
 ******************************************************************************/

package com.atlassian.connector.eclipse.internal.bamboo.ui.actions;

import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooImages;
import com.atlassian.connector.eclipse.internal.bamboo.ui.BambooUiPlugin;
import com.atlassian.connector.eclipse.internal.bamboo.ui.EclipseBambooBuild;
import com.atlassian.connector.eclipse.internal.bamboo.ui.operations.RetrieveTestResultsJob;
import com.atlassian.theplugin.commons.bamboo.BambooBuild;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.junit.launcher.AssertionVMArg;
import org.eclipse.jdt.internal.junit.launcher.JUnitLaunchConfigurationConstants;
import org.eclipse.jdt.internal.junit.launcher.JUnitMigrationDelegate;
import org.eclipse.jdt.internal.junit.launcher.TestKindRegistry;
import org.eclipse.jdt.internal.junit.model.JUnitModel;
import org.eclipse.jdt.internal.junit.model.TestRunSession;
import org.eclipse.jdt.internal.junit.ui.JUnitPlugin;
import org.eclipse.jdt.internal.junit.ui.TestRunnerViewPart;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import java.io.File;

/**
 * Action to open the Test Results
 * 
 * @author Thomas Ehrnhoefer
 * @author Wojciech Seliga
 */
@SuppressWarnings("restriction")
public class ShowTestResultsAction extends EclipseBambooBuildSelectionListenerAction {

	private static boolean isJUnitAvailable = false;

	static {
		try {
			if (JUnitPlugin.getDefault() != null) {
				isJUnitAvailable = true;
			}

		} catch (Throwable e) {
			//ignore - swallow exception
		}
	}

	public ShowTestResultsAction() {
		super(null);
		initialize();
	}

	private void initialize() {
		setText("Show Test Results");
		setImageDescriptor(BambooImages.JUNIT);
	}

	@Override
	void onRun(EclipseBambooBuild eclipseBambooBuild) {
		downloadAndShowTestResults(eclipseBambooBuild);
	}

	private void downloadAndShowTestResults(final EclipseBambooBuild eclipseBambooBuild) {
		final BambooBuild build = eclipseBambooBuild.getBuild();
		RetrieveTestResultsJob job = new RetrieveTestResultsJob(build, eclipseBambooBuild.getTaskRepository());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult() == Status.OK_STATUS) {
					File testResults = ((RetrieveTestResultsJob) event.getJob()).getTestResultsFile();
					if (testResults != null) {
						showJUnitView(testResults, build.getPlanKey() + "-" + build.getNumber());
					} else {
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								MessageDialog.openError(Display.getDefault().getActiveShell(), getText(),
										"Retrieving test result for " + build.getPlanKey() + "-" + build.getNumber()
												+ " failed. See error log for details.");
							}
						});
					}
				}
			}
		});
		job.schedule();
	}

	@Override
	boolean onUpdateSelection(EclipseBambooBuild eclipseBambooBuild) {
		if (!isJUnitAvailable) {
			return false;
		}
		final BambooBuild build = eclipseBambooBuild.getBuild();
		return (build.getTestsFailed() + build.getTestsPassed()) > 0;
	}

	private void showJUnitView(final File testResults, final String buildKey) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				new ShowTestResultsExecution().execute(testResults, buildKey);
			}
		});
	}

	/**
	 * Execution of the ShowTestResultsAction. Seperate class since there are optional dependencies, which should get
	 * loaded if and only if the dependencies are met.
	 * 
	 * @author Thomas Ehrnhoefer
	 */
	private class ShowTestResultsExecution {

		private static final String EMPTY_STRING = "";

		public void execute(final File testResults, final String buildKey) {
			IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (activeWorkbenchWindow == null) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
						"Error opening JUnit View. No active workbench window."));
				return;
			}
			try {
				activeWorkbenchWindow.getActivePage().showView(TestRunnerViewPart.NAME);
			} catch (PartInitException e) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Error opening JUnit View", e));
				return;
			}
			final TestRunSession trs = new TestRunSession("Bamboo build " + buildKey, null) {

				@Override
				public boolean rerunTest(String testId, String className, String testName, String launchMode)
						throws CoreException {
					String name = className;
					if (testName != null) {
						name += "." + testName;
					}
					final IType testElement = /*compositeProject.*/findType(className);
					if (testElement == null) {
						throw new CoreException(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID,
								"Cannot find Java project which contains class " + className + "."));
					}
					final ILaunchConfigurationWorkingCopy newCfg = createLaunchConfiguration(testElement);
					newCfg.launch(launchMode, null);
					return true;
				}

				private IType findType(String fullyQualifiedName) throws JavaModelException {
					final IJavaProject[] projects = JavaModelManager.getJavaModelManager()
							.getJavaModel()
							.getJavaProjects();
					for (IJavaProject project : projects) {
						final IType itype = project.findType(fullyQualifiedName);
						if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
							return itype;
						}
					}
					return null;
				}
			};
			try {
				JUnitModel.importIntoTestRunSession(testResults, trs);
			} catch (CoreException e) {
				StatusHandler.log(new Status(IStatus.ERROR, BambooUiPlugin.PLUGIN_ID, "Error opening JUnit View", e));
			}
			JUnitPlugin.getModel().addTestRunSession(trs);
		}

		/**
		 * this method is practically stolen "as is" from
		 * {@link org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut#createLaunchConfiguration(IJavaElement)}
		 * 
		 * The only meaningful difference is the following line:
		 * 
		 * <pre>
		 * ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(
		 * 		JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
		 * </pre>
		 */
		protected ILaunchConfigurationWorkingCopy createLaunchConfiguration(IJavaElement element) throws CoreException {
			final String testName;
			final String mainTypeQualifiedName;
			final String containerHandleId;

			switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT:
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT: {
				String name = JavaElementLabels.getTextLabel(element, JavaElementLabels.ALL_FULLY_QUALIFIED);
				containerHandleId = element.getHandleIdentifier();
				mainTypeQualifiedName = EMPTY_STRING;
				testName = name.substring(name.lastIndexOf(IPath.SEPARATOR) + 1);
			}
				break;
			case IJavaElement.TYPE: {
				containerHandleId = EMPTY_STRING;
				mainTypeQualifiedName = ((IType) element).getFullyQualifiedName('.'); // don't replace, fix for binary inner types
				testName = element.getElementName();
			}
				break;
			case IJavaElement.METHOD: {
				IMethod method = (IMethod) element;
				containerHandleId = EMPTY_STRING;
				mainTypeQualifiedName = method.getDeclaringType().getFullyQualifiedName('.');
				testName = method.getDeclaringType().getElementName() + '.' + method.getElementName();
			}
				break;
			default:
				throw new IllegalArgumentException("Invalid element type to create a launch configuration: "
						+ element.getClass().getName());
			}

			String testKindId = TestKindRegistry.getContainerTestKindId(element);

			ILaunchConfigurationType configType = getLaunchManager().getLaunchConfigurationType(
					JUnitLaunchConfigurationConstants.ID_JUNIT_APPLICATION);
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null,
					getLaunchManager().generateUniqueLaunchConfigurationNameFrom(testName));

			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainTypeQualifiedName);
			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, element.getJavaProject()
					.getElementName());
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_CONTAINER, containerHandleId);
			wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND, testKindId);
			JUnitMigrationDelegate.mapResources(wc);
			AssertionVMArg.setArgDefault(wc);
			if (element instanceof IMethod) {
				// only set for methods
				wc.setAttribute(JUnitLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, element.getElementName());
			}
			return wc;
		}

		private ILaunchManager getLaunchManager() {
			return DebugPlugin.getDefault().getLaunchManager();
		}
	}

}
