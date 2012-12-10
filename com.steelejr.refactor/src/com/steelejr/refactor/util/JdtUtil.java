package com.steelejr.refactor.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;

// see org.eclipse.jdt.internal.ui.util.coreutility
public class JdtUtil {

	/**
	 * Creates a folder and all parent folders if not existing.
	 * Project must exist.
	 */
	public static void createFolder (IFolder folder, boolean force, boolean local, IProgressMonitor monitor) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder ((IFolder)parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}
	
	/**
	 * @return {@link IJavaElement} references of the provided {@link IField}.
	 */
	public static List<IJavaElement> searchReferences (IField field) 
			throws CoreException {
		IJavaSearchScope searchScope = getSearchScope(field);
		return searchReferences (field, searchScope);
	}
	
	public static List<IJavaElement> searchReferences (IType type) 
		throws CoreException {
		IJavaSearchScope searchScope = getSearchScope(type);
		return searchReferences (type, searchScope);
	}
	
	public static List<IJavaElement> searchReferences (IType type, IJavaSearchScope scope) 
		throws CoreException {
		return searchReferences((IJavaElement)type, scope);
	}
	
	public static IJavaSearchScope getSearchScope (IJavaElement element) {
		IJavaProject project = element.getJavaProject();
		return SearchEngine.createJavaSearchScope(
				new IJavaElement[]{project}, 
				IJavaSearchScope.SOURCES);
	}
	
	public static List<IJavaElement> searchReferences (IJavaElement element, IJavaSearchScope scope) throws CoreException {
		final List<IJavaElement> references = new ArrayList<IJavaElement>();
		SearchRequestor requestor = new SearchRequestor() {
			@Override
			public void acceptSearchMatch(SearchMatch match) throws CoreException {
				if (match instanceof ReferenceMatch) {
					ReferenceMatch refMatch = (ReferenceMatch)match;
					IJavaElement element = (IJavaElement)refMatch.getElement();
					IJavaElement localElement = refMatch.getLocalElement();
					if (localElement != null) {
						element = localElement;
					} 
					references.add(element);
				}
			}
		};
		SearchPattern pattern = SearchPattern.createPattern(element, IJavaSearchConstants.REFERENCES);
		SearchEngine engine = new SearchEngine();
		engine.search(
				pattern, 
				new SearchParticipant[]{SearchEngine.getDefaultSearchParticipant()},
				scope, 
				requestor, 
				new NullProgressMonitor());
		return references;
	}
}
