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
    public static String RELATIONSHIP_PROJECTSPROJECTUSES = "projectsProjectUses";
    
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
     * Gets the project pools
     * @return The list of project pools
     */
    public List<RemotePool> getProjectPools() {
        List<Pool> projectPools = aem.getRootPools(Constants.CLASS_GENERICPROJECT, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT, true);
        List<RemotePool> remoteProjPools = new ArrayList();
        
        for (Pool projectPool : projectPools)
            remoteProjPools.add(new RemotePool(projectPool));
        
        return remoteProjPools;
    }
    
    /** Gets the project in a Project pool 
     * @param poolId Project pool id
     * @param limit Max result number, -1 without limit
     * @return The list of projects
     * @throws ApplicationObjectNotFoundException If the Project pool is not found
     */
    public List<RemoteBusinessObjectLight> getProjectsInProjectPool(long poolId, int limit) throws ApplicationObjectNotFoundException {
        return aem.getPoolItems(poolId, limit);
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
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    public long addProject(long parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, ArraySizeMismatchException, MetadataObjectNotFoundException  {

        aem.getPool(parentId);
        return bem.createPoolItem(parentId, className, attributeNames, attributeValues, 0);
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
    public long addActivity(long parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues) throws 
        MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, 
        OperationNotPermittedException, ApplicationObjectNotFoundException, ArraySizeMismatchException {
        
        
        HashMap<String, String> attributes = new HashMap<>();
        
        if (attributeNames != null && attributeValues != null) {
            
            if (attributeNames.length != attributeValues.length)
                throw new ArraySizeMismatchException("attributeNames", "attributeValues");
            
            for (int i = 0; i < attributeNames.length; i += 1)
                attributes.put(attributeNames[i], attributeValues[i]);
        }
        return bem.createSpecialObject(className, parentClassName, parentId, attributes, -1);
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
     * @throws InvalidArgumentException  If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public List<RemoteBusinessObjectLight> getProjectResurces(String projectClass, long projectId) throws 
        InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        return bem.getSpecialAttribute(projectClass, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
        
    /**
     * Gets the activities associates to an Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @return The list of Activities
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws MetadataObjectNotFoundException If the project class is not found
     * @throws ObjectNotFoundException If the project is not found
     */
    public List<RemoteBusinessObjectLight> getProjectActivities(String projectClass, long projectId) 
        throws InvalidArgumentException, MetadataObjectNotFoundException, ObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        List<RemoteBusinessObjectLight> children = bem.getObjectSpecialChildren(projectClass, projectId);
        List<RemoteBusinessObjectLight> activities = new ArrayList();
        
        for (RemoteBusinessObjectLight child : children) {
            if (mem.isSubClass(Constants.CLASS_GENERICACTIVITY, child.getClassName()))
                activities.add(child);
        }
        return activities;
    }
            
    /**
     * Associates objects to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object Id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws ArraySizeMismatchException if array sizes of objectClass and objectId are not the same
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectsToProject(String projectClass, long projectId, String[] objectClass, long[] objectId) throws 
        InvalidArgumentException, ArraySizeMismatchException, ObjectNotFoundException, 
        OperationNotPermittedException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        if (objectClass.length != objectId.length)
            throw new ArraySizeMismatchException("objectClass", "objectId");
        
        for (int i = 0; i < objectId.length; i += 1) {
            bem.createSpecialRelationship(projectClass, projectId, objectClass[i], objectId[i], RELATIONSHIP_PROJECTSPROJECTUSES, true);
        }
    }
        
    /**
     * Associates an object to a Project
     * @param projectClass Project class
     * @param projectId Project Id
     * @param objectClass Object class
     * @param objectId Object id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws OperationNotPermittedException
     * @throws MetadataObjectNotFoundException
     */
    public void associateObjectToProject(String projectClass, long projectId, String objectClass, long objectId) throws 
        InvalidArgumentException, ObjectNotFoundException, OperationNotPermittedException, 
        MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        bem.createSpecialRelationship(projectClass, projectId, objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES, true);
    }
        
    /**
     * Releases an object from Project
     * @param objectClass Object class
     * @param objectId Object id
     * @param projectClass Project class
     * @param projectId Project id
     * @throws InvalidArgumentException If the project is not subclass of GenericProject
     * @throws ObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     */
    public void releaseObjectFromProject(String objectClass, long objectId, String projectClass, long projectId) throws 
        InvalidArgumentException, ObjectNotFoundException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPROJECT, projectClass))
            throw new InvalidArgumentException(String.format("Class %s is not a project", projectClass));
        
        bem.releaseSpecialRelationship(objectClass, objectId, projectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
    
    /**
     * Gets the project associate to an object
     * @param objectClass Object class
     * @param objectId Object Id
     * @return The list of projects
     * @throws ObjectNotFoundException If the project is no found
     * @throws MetadataObjectNotFoundException If the project class is no found
     */
    public List<RemoteBusinessObjectLight> getProjectsAssociateToObject(String objectClass, long objectId) throws ObjectNotFoundException, MetadataObjectNotFoundException {
        return bem.getSpecialAttribute(objectClass, objectId, RELATIONSHIP_PROJECTSPROJECTUSES);
    }
    
    /**
     * Creates a Project Pool
     * @param name Project Pool name
     * @param description Project Pool description
     * @param instanceOfClass Project Pool class
     * @return The new Project Pool id
     * @throws MetadataObjectNotFoundException If he project pool class is no found
     */
    public long createProjectPool(String name, String description, String instanceOfClass) throws MetadataObjectNotFoundException {
        return aem.createRootPool(name, description, instanceOfClass, ApplicationEntityManager.POOL_TYPE_MODULE_COMPONENT);
    }
                    
    @Override
    public void configureModule(ApplicationEntityManager aem, MetadataEntityManager mem, BusinessEntityManager bem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        
        this.mem.setSpecialRelationshipDisplayName(RELATIONSHIP_PROJECTSPROJECTUSES, "Associated to this project");
    }
    
}
