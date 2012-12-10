package com.steelejr.refactor.handler;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.steelejr.refactor.util.JdtUtil;

public class SearchUnusedHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			if (ss.getFirstElement() != null && !ss.isEmpty()) {
				Object obj = ss.getFirstElement();
				if (obj instanceof IJavaProject) {
					IJavaProject project = (IJavaProject) obj;
					try {
						searchProject (project);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
	
	private void searchProject (IJavaProject javaProject) throws CoreException {
		IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
		for (IPackageFragmentRoot root : roots) {
			if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
				for (IJavaElement entry : root.getChildren()) {
					if (entry instanceof IPackageFragment) {
						IPackageFragment frag = (IPackageFragment) entry;
						for (ICompilationUnit unit : frag.getCompilationUnits()) {
							IType [] types = unit.getAllTypes();
							for (IType type : types) {
								List<IJavaElement> used = JdtUtil.searchReferences(type);
								if (used.size() == 0) {
									displayUnused (type);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void displayUnused (IType type) {
		System.out.println("Unused Type: " + type.getElementName());
	}
}
