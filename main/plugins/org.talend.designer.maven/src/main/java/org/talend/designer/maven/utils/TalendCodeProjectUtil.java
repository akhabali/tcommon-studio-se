// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.maven.utils;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2e.core.internal.IMavenConstants;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.designer.maven.model.TalendMavenConstants;
import org.talend.designer.maven.tools.creator.CreateMavenCodeProject;

/**
 * created by ggu on 23 Jan 2015 Detailled comment
 *
 */
public final class TalendCodeProjectUtil {

    public static IProject initCodeProject(IProgressMonitor monitor) throws Exception {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IProject codeProject = root.getProject(TalendMavenConstants.PROJECT_NAME);

        if (!codeProject.exists() || needRecreate(codeProject)) {
            CreateMavenCodeProject createProject = new CreateMavenCodeProject(codeProject);
            createProject.create(monitor);
            codeProject = createProject.getProject();
        }

        if (!codeProject.isOpen()) {
            codeProject.open(IProject.BACKGROUND_REFRESH, monitor);
        } else {
            codeProject.refreshLocal(IProject.DEPTH_INFINITE, monitor);
        }
        return codeProject;
    }

    @SuppressWarnings("restriction")
    private static boolean needRecreate(IProject codeProject) {
        if (codeProject.exists()) {
            try {
                codeProject.refreshLocal(IResource.DEPTH_ONE, null);
                // not java project
                if (!codeProject.hasNature(JavaCore.NATURE_ID)) {
                    return true;
                }

                // like TDI-33044, when creating, kill the studio. the classpath file won't be created.
                if (!codeProject.getFile(IJavaProject.CLASSPATH_FILE_NAME).exists()) {
                    return true;
                }

                // IJavaProject javaProject = JavaCore.create(codeProject);
                // javaProject.getRawClasspath(); //test the nature and classpath?

                // no maven nature.
                if (!codeProject.hasNature(IMavenConstants.NATURE_ID)) {
                    return true;
                }

                // no pom
                if (!codeProject.getFile(TalendMavenConstants.POM_FILE_NAME).exists()) {
                    return true;
                }

                // FIXME pom is not "pom" packaging?
                // will change to "pom" packaging when ProjectPomManager.updateAttributes. so no need check.

            } catch (CoreException e) {
                ExceptionHandler.process(e);
            }
        }
        return false;
    }

    // public static IMarker[] getMavenMarks(IFile file) throws CoreException {
    // IMarker[] findMarkers = file.findMarkers(IMavenConstants.MARKER_CONFIGURATION_ID, true, IResource.DEPTH_ONE);
    // return findMarkers;
    // }

}
