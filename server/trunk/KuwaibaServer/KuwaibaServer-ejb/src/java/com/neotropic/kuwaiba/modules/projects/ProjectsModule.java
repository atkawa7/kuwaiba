/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.kuwaiba.modules.projects;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.ws.toserialize.application.RemotePool;

/**
 * Project management module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProjectsModule implements GenericCommercialModule {
    
    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The ApplicationEntityManager instance
     */
    private ApplicationEntityManager aem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    /**
     * Relationship project to object
     */
    public static String RELATIONSHIP_PROJECTSPROJECTHAS = "projectsProjectHas";
    
    @Override
    public String getName() {
        return "Projects Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "Projects Management Module";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }

    @Override
    public String getCategory() {
        return "planning/projects";
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public boolean isValid() {
        return true;
    }
    
    /**
     * Gets the Project root pool
     * @param className Project root pool class name
     * @return The Project root node
     * @throws InvalidArgumentException if the class provided is not subclass of Constants.CLASS_GENERICPROJECT
     * @throws MetadataObjectNotFoundException If <code>Constants.CLASS_GENERICPROJECT</code> is not a valid subclass of InventoryObject
     */
    public RemotePool getProjectsRootPool(String className) throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, className))
            throw new InvalidArgumentException(String.format("Class %s is not a project", className));
        
        List<Pool> projectRootPools = aem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        
        if (projectRootPools.isEmpty()) {
            
            aem.createRootPool(Constants.NODE_PROJECTROOT, Constants.NODE_PROJECTROOT, Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT);
            projectRootPools = aem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_ROOT, false);
        }
        if (projectRootPools.size() == 1)
            return new RemotePool(projectRootPools.get(0));
        
        return null;    
    }
    
    /**
     * Gets the projects associated to the projects root pool
     * @param projectsRootPoolId Projects root pool id
     * @param limit Max result number, -1 with out limit
     * @return The list of projects
     * @throws ApplicationObjectNotFoundException If the project root pool is not founded
     */
    public List<RemoteBusinessObjectLight> getProjectsFromProjectRootPool(long projectsRootPoolId, int limit) throws 
        ApplicationObjectNotFoundException {
        
        return aem.getPoolItems(projectsRootPoolId, limit);
    }
        
    /**
     * Adds a Project
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attribute names
     * @param attributeValues Attribute values
     * @return The Project id
     * @throws InvalidArgumentException If any of the attributes or its type is invalid
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     * @throws MetadataObjectNotFoundException If the class name could not be found
     * @throws ObjectNotFoundException If the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long addProject(long parentId, String parentClassName, String className, String[] attributeNames, String[][] attributeValues) throws 
        InvalidArgumentException, ArraySizeMismatchException, MetadataObjectNotFoundException, 
        ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        
        try {
            aem.getPool(parentId);
            // The new Project is child of the project root pool
            return bem.createPoolItem(parentId, className, attributeNames, attributeValues, 0);
            
        } catch (ApplicationObjectNotFoundException ex) {
            
            HashMap<String, List<String>> attributes = new HashMap<>();
            
            if (attributeNames != null && attributeValues != null) {
                
                if (attributeNames.length != attributeValues.length)
                    throw new ArraySizeMismatchException("attributeNames", "attributeValues");
                
                for (int i = 0; i < attributeNames.length; i += 1)
                    attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));
            }
            // The new Project is child of and Activity or Project
            return bem.createSpecialObject(className, parentClassName, parentId, attributes, 0);
        }
    }
        
    /**
     * Deletes a Project
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @throws ObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     */
    public void deleteProject(String className, long oid, boolean releaseRelationships) throws 
        ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {
        
        bem.deleteObject(className, oid, releaseRelationships);
    }
            
    /**
     * Adds an Activity.
     * @param parentId Parent Id
     * @param parentClassName Parent class name
     * @param className Class name
     * @param attributeNames Attribute names
     * @param attributeValues Attribute values
     * @return The Activity id
     * @throws MetadataObjectNotFoundException If the object's class can't be found
     * @throws ObjectNotFoundException If the parent id is not found
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws ApplicationObjectNotFoundException If the specified template could not be found
     * @throws ArraySizeMismatchException If attributeNames and attributeValues have different sizes.
     */
    public long addActivity(long parentId, String parentClassName, String className, String[] attributeNames, String[][] attributeValues) throws 
        MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, 
        OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException {
        
        
        HashMap<String, List<String>> attributes = new HashMap<>();
        
        if (attributeNames != null && attributeValues != null) {
            
            if (attributeNames.length != attributeValues.length)
                throw new ArraySizeMismatchException("attributeNames", "attributeValues");
            
            for (int i = 0; i < attributeNames.length; i += 1)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));
        }
        return bem.createSpecialObject(className, parentClassName, parentId, attributes, 0);
    }
    
    /**
     * Deletes an activity
     * @param className Class name
     * @param oid Object id
     * @param releaseRelationships Release relationships
     * @throws ObjectNotFoundException If the object couldn't be found
     * @throws MetadataObjectNotFoundException If the class could not be found
     * @throws OperationNotPermittedException If the object could not be deleted because there's some business rules that avoids it or it has incoming relationships
     */
    public void deleteActivity(String className, long oid, boolean releaseRelationships) throws 
        ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {
        
        bem.deleteObject(className, oid, releaseRelationships);
    }
    
    /**
     * Gets the resources associates with an Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @return The list of project resources
     * @throws ServerSideException  If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public List<RemoteBusinessObjectLight> getProjectResurces(String projectClass, long projectId) throws 
        ServerSideException, ObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        return bem.getSpecialAttribute(projectClass, projectId, RELATIONSHIP_PROJECTSPROJECTHAS);
    }
        
    /**
     * Gets the activities associates to an Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @return The list of Activities
     * @throws ServerSideException
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     */
    public List<RemoteBusinessObjectLight> getProjectActivities(String projectClass, long projectId) 
        throws ServerSideException, MetadataObjectNotFoundException, ObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        List<RemoteBusinessObjectLight> children = bem.getObjectSpecialChildren(projectClass, projectId);
        List<RemoteBusinessObjectLight> activities = new ArrayList();
        
        for (RemoteBusinessObjectLight child : children) {
            if (mem.isSubClass(Constants.CLASS_GENERICACTIVITY, child.getClassName()))
                activities.add(child);
        }
        return activities;
    }
    
    /**
     * Gets the project associate to a give project
     * @param projectClass Project class
     * @param projectId Project id
     * @return The list of projects 
     * @throws ServerSideException If the projectClass are no a subclass of projectClass
     * @throws MetadataObjectNotFoundException
     * @throws ObjectNotFoundException
     */
    public List<RemoteBusinessObjectLight> getProjectsFromProject(String projectClass, long projectId) throws 
        ServerSideException, MetadataObjectNotFoundException, ObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        List<RemoteBusinessObjectLight> children = bem.getObjectSpecialChildren(projectClass, projectId);
        List<RemoteBusinessObjectLight> projects = new ArrayList();
        
        for (RemoteBusinessObjectLight child : children) {
            if (mem.isSubClass(Constants.CLASS_GENERICPROJECT, child.getClassName()))
                projects.add(child);
        }
        return projects;
    }
        
    /**
     * Associates objects to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object Id
     * @throws ServerSideException If the project is not subclass of GenericProject
     * @throws ArraySizeMismatchException if array sizes of objectClass and objectId are not the same
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectsToProject(String projectClass, long projectId, String[] objectClass, long[] objectId) throws 
        ServerSideException, ArraySizeMismatchException, ObjectNotFoundException, 
        OperationNotPermittedException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        if (objectClass.length != objectId.length)
            throw new ArraySizeMismatchException("objectClass", "objectId");
        
        for (int i = 0; i < objectId.length; i += 1) {
            bem.createSpecialRelationship(projectClass, projectId, objectClass[i], objectId[i], RELATIONSHIP_PROJECTSPROJECTHAS, true);
        }
    }
        
    /**
     * Associates an object to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object id
     * @throws ServerSideException If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectToProject(String projectClass, long projectId, String objectClass, long objectId) throws 
        ServerSideException, ObjectNotFoundException, OperationNotPermittedException, 
        MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        bem.createSpecialRelationship(projectClass, projectId, objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTHAS, true);
    }
        
    /**
     * Releases an object from Project
     * @param objectClass Object class
     * @param objectId Object id
     * @param projectClass Project class
     * @param projectId Project id
     * @throws ServerSideException If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public void releaseObjectFromProject(String objectClass, long objectId, String projectClass, long projectId) throws 
        ServerSideException, ObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new ServerSideException(String.format("Class %s is not a project", projectClass));
        
        bem.releaseSpecialRelationship(objectClass, objectId, projectId, RELATIONSHIP_PROJECTSPROJECTHAS);
    }
    
    /**
     * Creates a Project Pool
     * @param name Project Pool name
     * @param description Project Pool description
     * @param instanceOfClass Project Pool class
     * @return The new Project Pool id
     * @throws MetadataObjectNotFoundException
     */
    public long createProjectPool(String name, String description, String instanceOfClass) throws MetadataObjectNotFoundException {
        return aem.createRootPool(name, description, instanceOfClass, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT);
    }
                    
    @Override
    public void configureModule(ApplicationEntityManager aem, MetadataEntityManager mem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_PROJECTSPROJECTHAS, "Associated to this project");
    }
    
}
