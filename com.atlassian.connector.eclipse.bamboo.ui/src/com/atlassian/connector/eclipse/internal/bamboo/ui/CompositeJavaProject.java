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

package com.atlassian.connector.eclipse.internal.bamboo.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.eval.IEvaluationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * This very simple wrapper/delegator which solely purpose is to cheat poorly extensible JUNit view (TestViewer) which
 * uses {@link #findType(String)} to decide whether to show Run/Debug actions or not.
 * 
 * @author wseliga
 */
public class CompositeJavaProject implements IJavaProject {

	private final IJavaProject mainProject;

	private final Collection<IJavaProject> projects;

	/**
	 * @param projects
	 *            non-empty list of projects to wrap. The first project is somewhat special, as this simple class
	 *            delegates there everything but methods used by TestViewer.
	 */
	public CompositeJavaProject(Collection<IJavaProject> projects) {
		if (projects == null || projects.size() == 0) {
			throw new IllegalArgumentException("At least one project expected");
		}
		Iterator<IJavaProject> it = projects.iterator();
		this.mainProject = it.next();
		this.projects = new ArrayList<IJavaProject>(projects);
	}

	/**
	 * Returns the first IJavaProject which contains given class (by its fully qualified name). Containment is defined
	 * as not merely having this class on the classpath but really containing the resource (file) which represents this
	 * class.
	 * 
	 * @param fullyQualifiedName
	 *            class name
	 * @return project which contains IResource which defines this class or <code>null</code> when such project is not
	 *         found
	 * @throws JavaModelException
	 */
	public IJavaProject findParentProject(String fullyQualifiedName) throws JavaModelException {
		for (IJavaProject project : projects) {
			IType itype = project.findType(fullyQualifiedName);
			if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
				return project;
			}
		}
		return null;
	}

	public void close() throws JavaModelException {
		mainProject.close();
	}

	public IClasspathEntry decodeClasspathEntry(String encodedEntry) {
		return mainProject.decodeClasspathEntry(encodedEntry);
	}

	public String encodeClasspathEntry(IClasspathEntry classpathEntry) {
		return mainProject.encodeClasspathEntry(classpathEntry);
	}

	public boolean exists() {
		return mainProject.exists();
	}

	public IJavaElement findElement(IPath path, WorkingCopyOwner owner) throws JavaModelException {
		return mainProject.findElement(path, owner);
	}

	public IJavaElement findElement(IPath path) throws JavaModelException {
		return mainProject.findElement(path);
	}

	/**
	 * This method is not used really and moreover it's not available in Eclipse 3.3 so let us just return null here
	 */
	public IJavaElement findElement(String bindingKey, WorkingCopyOwner owner) throws JavaModelException {
		return null;//mainProject.findElement(bindingKey, owner);
	}

	public IPackageFragment findPackageFragment(IPath path) throws JavaModelException {
		return mainProject.findPackageFragment(path);
	}

	public IPackageFragmentRoot findPackageFragmentRoot(IPath path) throws JavaModelException {
		return mainProject.findPackageFragmentRoot(path);
	}

	public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
		return mainProject.findPackageFragmentRoots(entry);
	}

	public String findRecommendedLineSeparator() throws JavaModelException {
		return mainProject.findRecommendedLineSeparator();
	}

	/**
	 * Returns the first IType which matches given class (by its fully qualified name). Only those {@link IType}s, which
	 * are real IResources(s) (Java files belonging to the project) are returned. Being on the classpath is not enough
	 * to be considered valid result here.
	 */
	public IType findType(String fullyQualifiedName, IProgressMonitor progressMonitor) throws JavaModelException {
		for (IJavaProject project : projects) {
			IType itype = project.findType(fullyQualifiedName, progressMonitor);
			if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
				return itype;
			}
		}
		return null;
	}

	public IType findType(String packageName, String typeQualifiedName, IProgressMonitor progressMonitor)
			throws JavaModelException {
		return mainProject.findType(packageName, typeQualifiedName, progressMonitor);
	}

	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner,
			IProgressMonitor progressMonitor) throws JavaModelException {
		return mainProject.findType(packageName, typeQualifiedName, owner, progressMonitor);
	}

	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner)
			throws JavaModelException {
		return mainProject.findType(packageName, typeQualifiedName, owner);
	}

	/**
	 * Returns the first IType which matches given class (by its fully qualified name). Only those {@link IType}s, which
	 * are real IResources(s) (Java files belonging to the project) are returned. Being on the classpath is not enough
	 * to be considered valid result here.
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaModelException {
		for (IJavaProject project : projects) {
			IType itype = project.findType(packageName, typeQualifiedName);
			if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
				return itype;
			}
		}
		return null;
	}

	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner, IProgressMonitor progressMonitor)
			throws JavaModelException {
		return mainProject.findType(fullyQualifiedName, owner, progressMonitor);
	}

	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaModelException {
		return mainProject.findType(fullyQualifiedName, owner);
	}

	/**
	 * Delegates finding to one of the wrapped projects. Returns the first IType which matches given class (by its fully
	 * qualified name). Only those {@link IType}s, which are real IResources(s) (Java files belonging to the project)
	 * are returned. Being on the classpath is not enough to be considered valid result here.
	 * 
	 * @param fullyQualifiedName
	 *            name of the class
	 * @return IType corresponding to given class name or <code>null</code>when none of wrapped projects contains (as
	 *         regular IResource) type represented by <code>fullyQualifiedName</code>
	 */
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		for (IJavaProject project : projects) {
			final IType itype = project.findType(fullyQualifiedName);
			if (itype != null && itype.getResource().getProject().equals(project.getProject())) {
				return itype;
			}
		}
		return null;
	}

	public Object getAdapter(Class adapter) {
		return mainProject.getAdapter(adapter);
	}

	public IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException {
		return mainProject.getAllPackageFragmentRoots();
	}

	public IJavaElement getAncestor(int ancestorType) {
		return mainProject.getAncestor(ancestorType);
	}

	public String getAttachedJavadoc(IProgressMonitor monitor) throws JavaModelException {
		return mainProject.getAttachedJavadoc(monitor);
	}

	public IBuffer getBuffer() throws JavaModelException {
		return mainProject.getBuffer();
	}

	public IJavaElement[] getChildren() throws JavaModelException {
		return mainProject.getChildren();
	}

	public IResource getCorrespondingResource() throws JavaModelException {
		return mainProject.getCorrespondingResource();
	}

	public String getElementName() {
		return mainProject.getElementName();
	}

	public int getElementType() {
		return mainProject.getElementType();
	}

	public String getHandleIdentifier() {
		return mainProject.getHandleIdentifier();
	}

	public IJavaModel getJavaModel() {
		return mainProject.getJavaModel();
	}

	public IJavaProject getJavaProject() {
		return mainProject.getJavaProject();
	}

	public Object[] getNonJavaResources() throws JavaModelException {
		return mainProject.getNonJavaResources();
	}

	public IOpenable getOpenable() {
		return mainProject.getOpenable();
	}

	public String getOption(String optionName, boolean inheritJavaCoreOptions) {
		return mainProject.getOption(optionName, inheritJavaCoreOptions);
	}

	public Map getOptions(boolean inheritJavaCoreOptions) {
		return mainProject.getOptions(inheritJavaCoreOptions);
	}

	public IPath getOutputLocation() throws JavaModelException {
		return mainProject.getOutputLocation();
	}

	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {
		return mainProject.getPackageFragmentRoot(resource);
	}

	public IPackageFragmentRoot getPackageFragmentRoot(String externalLibraryPath) {
		return mainProject.getPackageFragmentRoot(externalLibraryPath);
	}

	public IPackageFragmentRoot[] getPackageFragmentRoots() throws JavaModelException {
		return mainProject.getPackageFragmentRoots();
	}

	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
		return mainProject.getPackageFragmentRoots(entry);
	}

	public IPackageFragment[] getPackageFragments() throws JavaModelException {
		return mainProject.getPackageFragments();
	}

	public IJavaElement getParent() {
		return mainProject.getParent();
	}

	public IPath getPath() {
		return mainProject.getPath();
	}

	public IJavaElement getPrimaryElement() {
		return mainProject.getPrimaryElement();
	}

	public IProject getProject() {
		return mainProject.getProject();
	}

	public IClasspathEntry[] getRawClasspath() throws JavaModelException {
		return mainProject.getRawClasspath();
	}

	public String[] getRequiredProjectNames() throws JavaModelException {
		return mainProject.getRequiredProjectNames();
	}

	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry) throws JavaModelException {
		return mainProject.getResolvedClasspath(ignoreUnresolvedEntry);
	}

	public IResource getResource() {
		return mainProject.getResource();
	}

	public ISchedulingRule getSchedulingRule() {
		return mainProject.getSchedulingRule();
	}

	public IResource getUnderlyingResource() throws JavaModelException {
		return mainProject.getUnderlyingResource();
	}

	public boolean hasBuildState() {
		return mainProject.hasBuildState();
	}

	public boolean hasChildren() throws JavaModelException {
		return mainProject.hasChildren();
	}

	public boolean hasClasspathCycle(IClasspathEntry[] entries) {
		return mainProject.hasClasspathCycle(entries);
	}

	public boolean hasUnsavedChanges() throws JavaModelException {
		return mainProject.hasUnsavedChanges();
	}

	public boolean isConsistent() throws JavaModelException {
		return mainProject.isConsistent();
	}

	public boolean isOnClasspath(IJavaElement element) {
		return mainProject.isOnClasspath(element);
	}

	public boolean isOnClasspath(IResource resource) {
		return mainProject.isOnClasspath(resource);
	}

	public boolean isOpen() {
		return mainProject.isOpen();
	}

	public boolean isReadOnly() {
		return mainProject.isReadOnly();
	}

	public boolean isStructureKnown() throws JavaModelException {
		return mainProject.isStructureKnown();
	}

	public void makeConsistent(IProgressMonitor progress) throws JavaModelException {
		mainProject.makeConsistent(progress);
	}

	public IEvaluationContext newEvaluationContext() {
		return mainProject.newEvaluationContext();
	}

	public ITypeHierarchy newTypeHierarchy(IRegion region, IProgressMonitor monitor) throws JavaModelException {
		return mainProject.newTypeHierarchy(region, monitor);
	}

	public ITypeHierarchy newTypeHierarchy(IRegion region, WorkingCopyOwner owner, IProgressMonitor monitor)
			throws JavaModelException {
		return mainProject.newTypeHierarchy(region, owner, monitor);
	}

	public ITypeHierarchy newTypeHierarchy(IType type, IRegion region, IProgressMonitor monitor)
			throws JavaModelException {
		return mainProject.newTypeHierarchy(type, region, monitor);
	}

	public ITypeHierarchy newTypeHierarchy(IType type, IRegion region, WorkingCopyOwner owner, IProgressMonitor monitor)
			throws JavaModelException {
		return mainProject.newTypeHierarchy(type, region, owner, monitor);
	}

	public void open(IProgressMonitor progress) throws JavaModelException {
		mainProject.open(progress);
	}

	public IPath readOutputLocation() {
		return mainProject.readOutputLocation();
	}

	public IClasspathEntry[] readRawClasspath() {
		return mainProject.readRawClasspath();
	}

	public void save(IProgressMonitor progress, boolean force) throws JavaModelException {
		mainProject.save(progress, force);
	}

	public void setOption(String optionName, String optionValue) {
		mainProject.setOption(optionName, optionValue);
	}

	public void setOptions(Map newOptions) {
		mainProject.setOptions(newOptions);
	}

	public void setOutputLocation(IPath path, IProgressMonitor monitor) throws JavaModelException {
		mainProject.setOutputLocation(path, monitor);
	}

	public void setRawClasspath(IClasspathEntry[] entries, boolean canModifyResources, IProgressMonitor monitor)
			throws JavaModelException {
		mainProject.setRawClasspath(entries, canModifyResources, monitor);
	}

	public void setRawClasspath(IClasspathEntry[] entries, IPath outputLocation, boolean canModifyResources,
			IProgressMonitor monitor) throws JavaModelException {
		mainProject.setRawClasspath(entries, outputLocation, canModifyResources, monitor);
	}

	public void setRawClasspath(IClasspathEntry[] entries, IPath outputLocation, IProgressMonitor monitor)
			throws JavaModelException {
		mainProject.setRawClasspath(entries, outputLocation, monitor);
	}

	public void setRawClasspath(IClasspathEntry[] entries, IProgressMonitor monitor) throws JavaModelException {
		mainProject.setRawClasspath(entries, monitor);
	}
}
