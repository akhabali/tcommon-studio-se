// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.utils;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.BinaryResourceImpl;
import org.junit.Test;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.utils.workbench.resources.ResourceUtils;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.general.TalendNature;
import org.talend.core.model.properties.ProcessItem;
import org.talend.core.model.properties.PropertiesFactory;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.repository.constants.FileConstants;
import org.talend.core.repository.model.ResourceModelUtils;
import org.talend.core.repository.utils.URIHelper;
import org.talend.core.repository.utils.XmiResourceManager;
import org.talend.designer.core.model.utils.emf.talendfile.NodeType;
import org.talend.designer.core.model.utils.emf.talendfile.TalendFileFactory;
import org.talend.repository.ProjectManager;

/**
 * DOC nrousseau class global comment. Detailled comment
 */
public class XmiResourceManagerTest {

    /**
     * create project function took from LocalRepositoryFactory from code version 4.1.1.
     * 
     * @param projectInfor
     * @return
     * @throws PersistenceException
     */
    public Project createProject(Project projectInfor) throws PersistenceException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        String technicalLabel = Project.createTechnicalName(projectInfor.getLabel());
        IProject prj = root.getProject(technicalLabel);

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();

        try {
            IProjectDescription desc = null;
            if (prj.exists()) {
                desc = prj.getDescription();
            } else {
                desc = workspace.newProjectDescription(projectInfor.getLabel());
            }
            desc.setNatureIds(new String[] { TalendNature.ID });
            desc.setComment(projectInfor.getDescription());

            if (!prj.exists()) {
                prj.create(desc, null);
            }
            prj.open(IResource.BACKGROUND_REFRESH, null);
            prj.setDefaultCharset("UTF-8", null);
        } catch (CoreException e) {
            throw new PersistenceException(e);
        }

        Project project = new Project();
        // Fill project object
        project.setLabel(projectInfor.getLabel());
        project.setDescription(projectInfor.getDescription());
        project.setLanguage(projectInfor.getLanguage());
        project.setAuthor(projectInfor.getAuthor());
        project.setLocal(true);
        project.setTechnicalLabel(technicalLabel);

        return project;
    }

    public Project createTempProject() throws PersistenceException {
        Project project = new Project();
        project.setLabel("testauto");
        project.setDescription("no desc");
        project.setLanguage(ECodeLanguage.JAVA);
        User user = PropertiesFactory.eINSTANCE.createUser();
        user.setLogin("testauto@talend.com");
        project.setAuthor(user);
        return createProject(project);
    }

    /**
     * Test method for {@link org.talend.core.repository.utils.XmiResourceManager#resetResourceSet()}.
     */
    @Test
    public void testResetResourceSet() {
        XmiResourceManager xrm = new XmiResourceManager();
        xrm.resourceSet.getResources().add(new BinaryResourceImpl());
        assertTrue(!xrm.resourceSet.getResources().isEmpty());
        xrm.resetResourceSet();
        assertTrue(xrm.resourceSet.getResources().isEmpty());
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#loadProject(org.eclipse.core.resources.IProject)}.
     * 
     * @throws PersistenceException
     * @throws CoreException
     */
    @Test
    public void testLoadProject() throws PersistenceException, CoreException {
        Project project = createTempProject();
        XmiResourceManager xrm = new XmiResourceManager();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject("TESTAUTO");
        Resource projectResource = xrm.createProjectResource(prj);
        projectResource.getContents().add(project.getEmfProject());
        projectResource.getContents().add(project.getAuthor());
        xrm.saveResource(projectResource);
        IProject physProject = ResourceModelUtils.getProject(project);
        org.talend.core.model.properties.Project emfProject = xrm.loadProject(physProject);
        assertTrue(emfProject.getTechnicalLabel().equals("TESTAUTO"));
        physProject.delete(true, null);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#hasTalendProjectFile(org.eclipse.core.resources.IProject)}
     * .
     * 
     * @throws PersistenceException
     * @throws CoreException
     */
    @Test
    public void testHasTalendProjectFile() throws PersistenceException, CoreException {
        Project project = createTempProject();
        XmiResourceManager xrm = new XmiResourceManager();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject("TESTAUTO");
        Resource projectResource = xrm.createProjectResource(prj);
        projectResource.getContents().add(project.getEmfProject());
        projectResource.getContents().add(project.getAuthor());
        xrm.saveResource(projectResource);
        IProject physProject = ResourceModelUtils.getProject(project);
        assertTrue(xrm.hasTalendProjectFile(physProject));
        physProject.delete(true, null);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#createProjectResource(org.eclipse.core.resources.IProject)}
     * .
     * 
     * @throws CoreException
     */
    @Test
    public void testCreateProjectResource() throws CoreException {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject("TESTAUTO");
        prj.create(null);
        XmiResourceManager xrm = new XmiResourceManager();
        Resource resource = xrm.createProjectResource(prj);
        assertTrue(resource != null);
        prj.delete(true, null);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#createItemResource(org.eclipse.core.resources.IProject, org.talend.core.model.properties.Item, org.eclipse.core.runtime.IPath, org.talend.core.model.repository.ERepositoryObjectType, boolean)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testCreateItemResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();

        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        assertTrue(processItemResource != null);
        assertTrue(processItemResource.getURI().fileExtension().equals("item"));
        ResourceUtils.deleteFile(URIHelper.getFile(processItemResource.getURI()));
        xrm.resourceSet.getResources().remove(processItemResource);

        // test change of extension for the file.
        processItem = createTempProcessItem();
        processItem.setFileExtension("process");

        // after this there should be .properties / .process when create

        processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS, false);
        assertTrue(processItemResource != null);
        assertTrue(processItemResource.getURI().fileExtension().equals("process"));
        ResourceUtils.deleteFile(URIHelper.getFile(processItemResource.getURI()));
        xrm.resourceSet.getResources().remove(processItemResource);

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        assertTrue(routineItemResource != null);
        assertTrue(routineItemResource.getURI().fileExtension().equals("item"));
        ResourceUtils.deleteFile(URIHelper.getFile(routineItemResource.getURI()));
        xrm.resourceSet.getResources().remove(routineItemResource);
    }

    private ProcessItem createTempProcessItem() {
        ProcessItem processItem = PropertiesFactory.eINSTANCE.createProcessItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        processItem.setProperty(myProperty);
        myProperty.setLabel("myJob");
        myProperty.setVersion("0.1");

        processItem.setProcess(TalendFileFactory.eINSTANCE.createProcessType());
        processItem.getProcess().getNode().add(TalendFileFactory.eINSTANCE.createNodeType());
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentName("test");
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentVersion("0.1");

        return processItem;
    }

    private RoutineItem createTempRoutineItem() {
        RoutineItem routineItem = PropertiesFactory.eINSTANCE.createRoutineItem();
        Property myProperty = PropertiesFactory.eINSTANCE.createProperty();
        routineItem.setProperty(myProperty);
        myProperty.setLabel("myRoutine");
        myProperty.setVersion("0.1");
        routineItem.setContent(PropertiesFactory.eINSTANCE.createByteArray());
        routineItem.getContent().setInnerContent("myRoutineContent".getBytes());
        return routineItem;
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#deleteResource(org.eclipse.emf.ecore.resource.Resource)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testDeleteResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        assertTrue(processItemResource != null);
        IFile file = URIHelper.getFile(processItemResource.getURI());
        xrm.deleteResource(processItemResource);
        assertTrue(!file.exists());
        assertTrue(!xrm.resourceSet.getResources().contains(processItemResource));

        processItem = createTempProcessItem();
        processItem.setFileExtension("process"); // test with file extension change.
        processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS, false);
        assertTrue(processItemResource != null);
        file = URIHelper.getFile(processItemResource.getURI());
        xrm.deleteResource(processItemResource);
        assertTrue(!file.exists());
        assertTrue(!xrm.resourceSet.getResources().contains(processItemResource));

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        assertTrue(routineItemResource != null);
        file = URIHelper.getFile(routineItemResource.getURI());
        xrm.deleteResource(routineItemResource);
        assertTrue(!file.exists());
        assertTrue(!xrm.resourceSet.getResources().contains(routineItemResource));
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#createPropertyResource(org.eclipse.emf.ecore.resource.Resource)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testCreatePropertyResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        assertTrue(processItemResource != null);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        assertTrue(propertyResource != null);
        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);

        processItem = createTempProcessItem();
        processItem.setFileExtension("process"); // test with file extension change
        processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS, false);
        assertTrue(processItemResource != null);
        propertyResource = xrm.createPropertyResource(processItemResource);
        assertTrue(propertyResource != null);
        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        assertTrue(routineItemResource != null);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        assertTrue(propertyResource != null);
        xrm.deleteResource(propertyResource);
        xrm.deleteResource(routineItemResource);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#getItemResource(org.talend.core.model.properties.Item)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testGetItemResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        Resource itemResource = xrm.getItemResource(processItem);
        assertTrue(itemResource != null && itemResource.equals(processItemResource));
        xrm.deleteResource(propertyResource);
        xrm.deleteResource(itemResource);

        // process with file extension changed

        processItem = createTempProcessItem();
        processItem.setFileExtension("process");
        processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS, false);
        propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        itemResource = xrm.getItemResource(processItem);
        assertTrue(itemResource.getURI().fileExtension().equals("process"));
        assertTrue(itemResource != null && itemResource.equals(processItemResource));
        xrm.deleteResource(propertyResource);
        xrm.deleteResource(itemResource);

        // / routine

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        itemResource = xrm.getItemResource(routineItem);
        assertTrue(itemResource != null && itemResource.equals(routineItemResource));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(routineItemResource);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#getAffectedResources(org.talend.core.model.properties.Property)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testGetAffectedResources() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        Resource screenshotsResource = xrm.createScreenshotResource(project, processItem, new Path(""),
                ERepositoryObjectType.PROCESS, false);
        screenshotsResource.getContents().addAll(processItem.getProcess().getScreenshots());

        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);
        xrm.saveResource(screenshotsResource);

        List<Resource> resources = xrm.getAffectedResources(processItem.getProperty());
        assertTrue(resources.size() == 3);
        assertTrue(resources.contains(processItemResource));
        assertTrue(resources.contains(propertyResource));
        assertTrue(resources.contains(screenshotsResource));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);
        xrm.deleteResource(screenshotsResource);

        // process with file extension change
        processItem = createTempProcessItem();
        processItem.setFileExtension("process");
        processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS, false);
        propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        screenshotsResource = xrm.createScreenshotResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        screenshotsResource.getContents().addAll(processItem.getProcess().getScreenshots());

        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);
        xrm.saveResource(screenshotsResource);

        resources = xrm.getAffectedResources(processItem.getProperty());
        assertTrue(resources.size() == 3);
        assertTrue(resources.contains(processItemResource));
        assertTrue(resources.contains(propertyResource));
        assertTrue(resources.contains(screenshotsResource));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);
        xrm.deleteResource(screenshotsResource);

        // routine

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        resources = xrm.getAffectedResources(routineItem.getProperty());
        assertTrue(resources.size() == 2);
        assertTrue(resources.contains(routineItemResource));
        assertTrue(resources.contains(propertyResource));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(routineItemResource);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#loadProperty(org.eclipse.core.resources.IResource)}.
     * 
     * @throws PersistenceException
     */
    @Test
    public void testLoadProperty() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        IFile file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.1.properties"));

        Property property = xrm.loadProperty(file);
        // old resource should be unloaded, since we force to reload
        assertTrue(processItem.eResource() == null);
        assertTrue(property.eResource() != null);
        assertTrue(property.getLabel().equals("myJob"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine_0.1.properties"));

        property = xrm.loadProperty(file);
        // old resource should be unloaded, since we force to reload
        assertTrue(processItem.eResource() == null);
        assertTrue(property.eResource() != null);
        assertTrue(property.getLabel().equals("myRoutine"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#forceReloadProperty(org.talend.core.model.properties.Property)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testForceReloadProperty() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        Property property = xrm.forceReloadProperty(processItem.getProperty());
        // old resource should be unloaded, since we force to reload
        assertTrue(processItem.eResource() == null);
        assertTrue(property.eResource() != null);
        assertTrue(property.getLabel().equals("myJob"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        property = xrm.forceReloadProperty(routineItem.getProperty());
        // old resource should be unloaded, since we force to reload
        assertTrue(routineItem.eResource() == null);
        assertTrue(property.eResource() != null);
        assertTrue(property.getLabel().equals("myRoutine"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#moveResource(org.eclipse.emf.ecore.resource.Resource, org.eclipse.core.runtime.IPath)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testMoveResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        assertTrue(processItemResource != null);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        assertTrue(propertyResource != null);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        Resource screenshotsResource = xrm.createScreenshotResource(project, processItem, new Path(""),
                ERepositoryObjectType.PROCESS, false);
        screenshotsResource.getContents().addAll(processItem.getProcess().getScreenshots());

        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);
        xrm.saveResource(screenshotsResource);

        for (Resource resource : xrm.getAffectedResources(processItem.getProperty())) {
            xrm.moveResource(resource, project.getFolder("/temp").getFullPath().append(resource.getURI().lastSegment()));
        }

        assertTrue(processItem.eResource() != null);
        assertTrue(processItem.eResource().getURI().toString().endsWith("/temp/myJob_0.1.properties"));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);
        xrm.deleteResource(screenshotsResource);

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        for (Resource resource : xrm.getAffectedResources(routineItem.getProperty())) {
            xrm.moveResource(resource, project.getFolder("/temp").getFullPath().append(resource.getURI().lastSegment()));
        }

        assertTrue(routineItem.eResource() != null);
        assertTrue(routineItem.eResource().getURI().toString().endsWith("/temp/myRoutine_0.1.properties"));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(routineItemResource);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#saveResource(org.eclipse.emf.ecore.resource.Resource)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testSaveResource() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);

        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        Property property = xrm.forceReloadProperty(processItem.getProperty());

        assertTrue(((NodeType) ((ProcessItem) property.getItem()).getProcess().getNode().get(0)).getComponentName()
                .equals("test"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);

        routineItemResource.getContents().add(routineItem.getContent());
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        // check the routine still have the good value.
        property = xrm.forceReloadProperty(routineItem.getProperty());

        String content = new String(((RoutineItem) property.getItem()).getContent().getInnerContent());

        assertTrue(content.equals("myRoutineContent"));

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#getItemResourceURI(org.eclipse.emf.common.util.URI)}.
     */
    @Test
    public void testGetItemResourceURI() {
        XmiResourceManager xrm = new XmiResourceManager();
        URI uri = xrm.getItemResourceURI(URI.createFileURI("/myFolder/myProperty.properties"));
        assertTrue(uri.toString().endsWith("/myFolder/myProperty.item"));
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#isPropertyFile(org.eclipse.core.resources.IFile)}.
     * 
     * @throws PersistenceException
     */
    @Test
    public void testIsPropertyFileIFile() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);

        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);

        processItemResource.getContents().add(processItem.getProcess());
        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        IFile file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.1.properties"));
        assertTrue(xrm.isPropertyFile(file));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS) + "/myJob_0.1.item"));
        assertTrue(!xrm.isPropertyFile(file));

        xrm.deleteResource(propertyResource);
        xrm.deleteResource(processItemResource);
    }

    /**
     * Test method for {@link org.talend.core.repository.utils.XmiResourceManager#isPropertyFile(java.lang.String)}.
     */
    @Test
    public void testIsPropertyFileString() {
        XmiResourceManager xrm = new XmiResourceManager();
        assertTrue(xrm.isPropertyFile("/myFolder/myProperty.properties"));
        assertTrue(!xrm.isPropertyFile("/myFolder/myProperty.item"));
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#isProjectFile(org.eclipse.core.resources.IFile)}.
     * 
     * @throws PersistenceException
     * @throws CoreException
     */
    @Test
    public void testIsProjectFile() throws PersistenceException, CoreException {
        IProject physProject = ResourceModelUtils.getProject(createTempProject());
        IFile talendProjectFile = physProject.getFile("/" + FileConstants.LOCAL_PROJECT_FILENAME);
        IFile projectFile = physProject.getFile("/.project");
        XmiResourceManager xrm = new XmiResourceManager();
        assertTrue(xrm.isProjectFile(talendProjectFile));
        assertTrue(!xrm.isProjectFile(projectFile));
        physProject.delete(true, null);
    }

    private void checkFileExists(IProject project, ERepositoryObjectType type, String name, String version) {
        IFile fileProperty = project.getFile(new Path(ERepositoryObjectType.getFolderName(type) + "/" + name + "_" + version
                + ".properties"));
        IFile fileItem = project.getFile(new Path(ERepositoryObjectType.getFolderName(type) + "/" + name + "_" + version
                + ".item"));

        assertTrue(fileProperty.exists());
        assertTrue(fileItem.exists());
    }

    private void checkFileNotExists(IProject project, ERepositoryObjectType type, String name, String version) {
        IFile fileProperty = project.getFile(new Path(ERepositoryObjectType.getFolderName(type) + "/" + name + "_" + version
                + ".properties"));
        IFile fileItem = project.getFile(new Path(ERepositoryObjectType.getFolderName(type) + "/" + name + "_" + version
                + ".item"));

        assertTrue(!fileProperty.exists());
        assertTrue(!fileItem.exists());
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#propagateFileName(org.talend.core.model.properties.Property, org.talend.core.model.properties.Property)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testPropagateFileName() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        testProcesspropagateFileName(project, xrm);

        testRoutinepropagateFileName(project, xrm);
    }

    private void testProcesspropagateFileName(IProject project, XmiResourceManager xrm) throws PersistenceException {
        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());
        Resource screenshotsResource = xrm.createScreenshotResource(project, processItem, new Path(""),
                ERepositoryObjectType.PROCESS, false);
        screenshotsResource.getContents().addAll(processItem.getProcess().getScreenshots());

        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);
        xrm.saveResource(screenshotsResource);

        // test 1 item, 1 version, change label only
        processItem.getProperty().setLabel("myJob2");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob", "0.1");
        xrm.propagateFileName(processItem.getProperty(), processItem.getProperty());
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob2", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob", "0.1");

        // test 1 item, 1 version, change version only
        processItem.getProperty().setVersion("0.2");
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentVersion("0.2");
        xrm.propagateFileName(processItem.getProperty(), processItem.getProperty());
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob2", "0.1");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob2", "0.2");

        IFile file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob2_0.1.properties"));
        Property oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.1"));

        // test 1 item, 2 version, change name only
        processItem.getProperty().setLabel("myJob3");
        xrm.propagateFileName(processItem.getProperty(), processItem.getProperty());
        xrm.propagateFileName(processItem.getProperty(), oldProperty);

        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.1");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.2");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob2", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob2", "0.2");

        // test 1 item, 2 version, change version
        processItem.getProperty().setVersion("0.3");
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentVersion("0.3");
        xrm.propagateFileName(processItem.getProperty(), processItem.getProperty());
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob3_0.1.properties"));
        xrm.propagateFileName(processItem.getProperty(), xrm.loadProperty(file));

        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.1");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.2");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.3");

        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob3_0.1.properties"));
        oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.1"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob3_0.2.properties"));
        oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.2"));
        assertTrue(((NodeType) processItem.getProcess().getNode().get(0)).getComponentVersion().equals("0.3"));

        // test 1 item, 3 version, change version and name
        processItem.getProperty().setVersion("1.0");
        processItem.getProperty().setLabel("myJob");
        ((NodeType) processItem.getProcess().getNode().get(0)).setComponentVersion("1.0");
        xrm.propagateFileName(processItem.getProperty(), processItem.getProperty());
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob3_0.1.properties"));
        xrm.propagateFileName(processItem.getProperty(), xrm.loadProperty(file));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob3_0.2.properties"));
        xrm.propagateFileName(processItem.getProperty(), xrm.loadProperty(file));
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob", "0.1");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob", "0.2");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob", "0.3");
        checkFileExists(project, ERepositoryObjectType.PROCESS, "myJob", "1.0");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.2");
        checkFileNotExists(project, ERepositoryObjectType.PROCESS, "myJob3", "0.3");

        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.1.properties"));
        oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.1"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.2.properties"));
        oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.2"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.3.properties"));
        oldProperty = xrm.loadProperty(file);
        assertTrue(((NodeType) ((ProcessItem) oldProperty.getItem()).getProcess().getNode().get(0)).getComponentVersion().equals(
                "0.3"));
        assertTrue(((NodeType) processItem.getProcess().getNode().get(0)).getComponentVersion().equals("1.0"));

        Property property = xrm.forceReloadProperty(processItem.getProperty());

        List<Resource> resources = xrm.getAffectedResources(property);
        for (Resource resource : resources) {
            xrm.deleteResource(resource);
        }
    }

    private void testRoutinepropagateFileName(IProject project, XmiResourceManager xrm) throws PersistenceException {
        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        Resource propertyResource = xrm.createPropertyResource(routineItemResource);

        routineItemResource.getContents().add(routineItem.getContent());
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        // test 1 item, 1 version, change label only
        routineItem.getProperty().setLabel("myRoutine2");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "0.1");
        xrm.propagateFileName(routineItem.getProperty(), routineItem.getProperty());
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine2", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "0.1");

        // test 1 item, 1 version, change version only
        routineItem.getProperty().setVersion("0.2");
        routineItem.getContent().setInnerContent("myRoutineContent_0.2".getBytes());
        xrm.propagateFileName(routineItem.getProperty(), routineItem.getProperty());
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine2", "0.1");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine2", "0.2");

        IFile file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine2_0.1.properties"));
        Property oldProperty = xrm.loadProperty(file);
        String content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent"));

        // test 1 item, 2 version, change name only
        routineItem.getProperty().setLabel("myRoutine3");
        xrm.propagateFileName(routineItem.getProperty(), routineItem.getProperty());
        xrm.propagateFileName(routineItem.getProperty(), oldProperty);

        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.1");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.2");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine2", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine2", "0.2");

        // test 1 item, 2 version, change version
        routineItem.getProperty().setVersion("0.3");
        routineItem.getContent().setInnerContent("myRoutineContent_0.3".getBytes());
        xrm.propagateFileName(routineItem.getProperty(), routineItem.getProperty());
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine3_0.1.properties"));
        xrm.propagateFileName(routineItem.getProperty(), xrm.loadProperty(file));

        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.1");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.2");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.3");

        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine3_0.1.properties"));
        oldProperty = xrm.loadProperty(file);
        content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine3_0.2.properties"));
        oldProperty = xrm.loadProperty(file);
        content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent_0.2"));
        content = new String(routineItem.getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent_0.3"));

        // test 1 item, 3 version, change version and name
        routineItem.getProperty().setVersion("1.0");
        routineItem.getProperty().setLabel("myRoutine");
        routineItem.getContent().setInnerContent("myRoutineContent_1.0".getBytes());
        xrm.propagateFileName(routineItem.getProperty(), routineItem.getProperty());
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine3_0.1.properties"));
        xrm.propagateFileName(routineItem.getProperty(), xrm.loadProperty(file));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine3_0.2.properties"));
        xrm.propagateFileName(routineItem.getProperty(), xrm.loadProperty(file));
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "0.1");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "0.2");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "0.3");
        checkFileExists(project, ERepositoryObjectType.ROUTINES, "myRoutine", "1.0");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.1");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.2");
        checkFileNotExists(project, ERepositoryObjectType.ROUTINES, "myRoutine3", "0.3");

        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine_0.1.properties"));
        oldProperty = xrm.loadProperty(file);
        content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine_0.2.properties"));
        oldProperty = xrm.loadProperty(file);
        content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent_0.2"));
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine_0.3.properties"));
        oldProperty = xrm.loadProperty(file);
        content = new String(((RoutineItem) oldProperty.getItem()).getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent_0.3"));
        content = new String(routineItem.getContent().getInnerContent());
        assertTrue(content.equals("myRoutineContent_1.0"));

        Property property = xrm.forceReloadProperty(routineItem.getProperty());
        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());
    }

    /**
     * Test method for {@link org.talend.core.repository.utils.XmiResourceManager#unloadResources()}.
     * 
     * @throws IOException
     * @throws PersistenceException
     * @throws CoreException
     */
    @Test
    public void testUnloadResources() throws IOException, PersistenceException, CoreException {
        Project project = createTempProject();
        XmiResourceManager xrm = new XmiResourceManager();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject("TESTAUTO");
        Resource projectResource = xrm.createProjectResource(prj);
        projectResource.getContents().add(project.getEmfProject());
        projectResource.getContents().add(project.getAuthor());
        xrm.saveResource(projectResource);
        org.talend.core.model.properties.Project emfProject = xrm.loadProject(prj);
        assertTrue(emfProject.getTechnicalLabel().equals("TESTAUTO"));
        assertTrue(emfProject.eResource().isLoaded());
        xrm.unloadResources();
        assertTrue(emfProject.eResource() == null);
        prj.delete(true, null);
    }

    /**
     * Test method for
     * {@link org.talend.core.repository.utils.XmiResourceManager#unloadResources(org.talend.core.model.properties.Property)}
     * .
     * 
     * @throws PersistenceException
     */
    @Test
    public void testUnloadResourcesProperty() throws PersistenceException {
        IProject project = ResourceModelUtils.getProject(ProjectManager.getInstance().getCurrentProject());
        XmiResourceManager xrm = new XmiResourceManager();

        // 2 kinds of type mainly, fully emf (like Process) or with byteArrayResource (Routines)
        // so test only the 2 actually. (will need some refactor if want to test all easily..)

        ProcessItem processItem = createTempProcessItem();
        Resource processItemResource = xrm.createItemResource(project, processItem, new Path(""), ERepositoryObjectType.PROCESS,
                false);
        Resource propertyResource = xrm.createPropertyResource(processItemResource);
        propertyResource.getContents().add(processItem.getProperty());
        propertyResource.getContents().add(processItem);
        processItemResource.getContents().add(processItem.getProcess());

        xrm.saveResource(processItemResource);
        xrm.saveResource(propertyResource);

        xrm.unloadResources(processItem.getProperty());
        assertTrue(processItem.eResource() == null);
        assertTrue(processItem.getProperty().eResource() == null);

        IFile file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.PROCESS)
                + "/myJob_0.1.properties"));

        Property property = xrm.loadProperty(file);

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());

        RoutineItem routineItem = createTempRoutineItem();
        Resource routineItemResource = xrm.createItemResource(project, routineItem, new Path(""), ERepositoryObjectType.ROUTINES,
                true);
        propertyResource = xrm.createPropertyResource(routineItemResource);
        propertyResource.getContents().add(routineItem.getProperty());
        propertyResource.getContents().add(routineItem);
        routineItemResource.getContents().add(routineItem.getContent());
        xrm.saveResource(routineItemResource);
        xrm.saveResource(propertyResource);

        xrm.unloadResources(routineItem.getProperty());
        assertTrue(routineItem.eResource() == null);
        assertTrue(routineItem.getProperty().eResource() == null);
        file = project.getFile(new Path(ERepositoryObjectType.getFolderName(ERepositoryObjectType.ROUTINES)
                + "/myRoutine_0.1.properties"));

        property = xrm.loadProperty(file);

        xrm.deleteResource(property.eResource());
        xrm.deleteResource(property.getItem().eResource());
    }

    /**
     * Test method for {@link org.talend.core.repository.utils.XmiResourceManager#unloadResource(java.lang.String)}.
     * 
     * @throws IOException
     * @throws PersistenceException
     * @throws CoreException
     */
    @Test
    public void testUnloadResource() throws IOException, PersistenceException, CoreException {
        Project project = createTempProject();
        XmiResourceManager xrm = new XmiResourceManager();
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        IProject prj = root.getProject("TESTAUTO");
        Resource projectResource = xrm.createProjectResource(prj);
        projectResource.getContents().add(project.getEmfProject());
        projectResource.getContents().add(project.getAuthor());
        xrm.saveResource(projectResource);
        org.talend.core.model.properties.Project emfProject = xrm.loadProject(prj);
        assertTrue(emfProject.getTechnicalLabel().equals("TESTAUTO"));
        assertTrue(emfProject.eResource().isLoaded());
        xrm.unloadResource(emfProject.eResource().getURI().toString());
        assertTrue(emfProject.eResource() == null);
        prj.delete(true, null);
    }

}
