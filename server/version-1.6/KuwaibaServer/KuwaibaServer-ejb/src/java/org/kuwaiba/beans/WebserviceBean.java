/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.beans;

import com.neotropic.kuwaiba.correlation.SimpleCorrelation;
import static com.neotropic.kuwaiba.correlation.SimpleCorrelation.servicesInPorts;
import com.neotropic.kuwaiba.modules.ipam.IPAMModule;
import com.neotropic.kuwaiba.modules.mpls.MPLSModule;
import com.neotropic.kuwaiba.modules.projects.ProjectsModule;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHModule;
import com.neotropic.kuwaiba.modules.sdh.SDHPosition;
import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.scheduling.JobManager;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import javax.ejb.Singleton;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.BusinessRule;
import org.kuwaiba.apis.persistence.application.BusinessRuleConstraint;
import org.kuwaiba.apis.persistence.application.FavoritesFolder;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.GroupProfileLight;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.application.Privilege;
import org.kuwaiba.apis.persistence.application.ResultMessage;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.Task;
import org.kuwaiba.apis.persistence.application.TaskResult;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.UserProfileLight;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLightList;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.sync.SyncManager;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.util.bre.TempBusinessRulesEngine;
import org.kuwaiba.utils.i18n.I18N;
import org.kuwaiba.ws.todeserialize.StringPair;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.ws.toserialize.application.GroupInfo;
import org.kuwaiba.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.ws.toserialize.application.PrivilegeInfo;
import org.kuwaiba.ws.toserialize.application.RemoteBackgroundJob;
import org.kuwaiba.ws.toserialize.application.RemoteBusinessRule;
import org.kuwaiba.ws.toserialize.application.RemoteBusinessRuleConstraint;
import org.kuwaiba.ws.toserialize.application.RemoteFavoritesFolder;
import org.kuwaiba.ws.toserialize.application.RemotePool;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteResultMessage;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.ws.toserialize.application.RemoteTask;
import org.kuwaiba.ws.toserialize.application.RemoteTaskResult;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.ws.toserialize.application.Validator;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.ws.toserialize.business.ServiceLevelCorrelatedInformation;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Session bean to give primary support to the web service calls
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Singleton
public class WebserviceBean implements WebserviceBeanRemote {

    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Business rules engine reference
     */
    private TempBusinessRulesEngine bre;
    /**
     * Sync/load data reference
     */
    private SyncManager sync;

    
    public WebserviceBean() {
        super();
        bre = new TempBusinessRulesEngine();
        sync = new SyncManager();
        connect();
    }

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public long createClass(ClassInfo classDefinition, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createClass", ipAddress, sessionId);
            ClassMetadata cm = new ClassMetadata();

            cm.setName(classDefinition.getClassName());
            cm.setDisplayName(classDefinition.getDisplayName());
            cm.setDescription(classDefinition.getDescription());
            cm.setParentClassName(classDefinition.getParentClassName());
            cm.setAbstract(classDefinition.isAbstract());
            cm.setColor(classDefinition.getColor());
            cm.setCountable(classDefinition.isCountable());
            cm.setCreationDate(Calendar.getInstance().getTimeInMillis());
            cm.setIcon(classDefinition.getIcon());
            cm.setSmallIcon(classDefinition.getSmallIcon());
            cm.setCustom(classDefinition.isCustom());
            cm.setViewable(classDefinition.isViewable());
            cm.setInDesign(classDefinition.isInDesign());
            //TODO decode flags, set category
            //cm.setCategory(classDefinition.getCategory());
            long newClassId = mem.createClass(cm);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_METADATA_OBJECT, 
                String.format("Created %s class", classDefinition.getClassName())
            );
            return newClassId;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteClass(String className, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteClass", ipAddress, sessionId);
            mem.deleteClass(className);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted %s class", className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteClass", ipAddress, sessionId);
            ClassMetadata classMetadata = mem.getClass(classId);
            mem.deleteClass(classId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted %s class", classMetadata.getName()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(className);
            List<Validator> validators = new ArrayList<>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, className))
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(classId);
            List<Validator> validators = new ArrayList<>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, myClass.getName())){
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                }
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getAllClassesLight(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAllClassesLight", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getAllClassesLight(includeListTypes, false);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName()))
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSubclassesLight", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLight(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName())){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSubClassesLightNoRecursive", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName())){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public boolean isSubclassOf(String className, String subclassOf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("isSubclassOf", ipAddress, sessionId);
            return mem.isSubClass(subclassOf, className);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfo> getAllClasses(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAllClasses", ipAddress, sessionId);
            List<ClassInfo> cml = new ArrayList<>();
            List<ClassMetadata> classMetadataList = mem.getAllClasses(includeListTypes, false);

            for (ClassMetadata classMetadata : classMetadataList){
                List<Validator> validators = new ArrayList<>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadata.getName())){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfo(classMetadata, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void createAttribute(String className, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createAttribute", ipAddress, sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(attributeDefinition.getName());
            attributeMetadata.setDisplayName(attributeDefinition.getDisplayName());
            attributeMetadata.setDescription(attributeDefinition.getDescription());
            attributeMetadata.setReadOnly(attributeDefinition.isReadOnly());
            attributeMetadata.setType(attributeDefinition.getType());
            attributeMetadata.setUnique(attributeDefinition.isUnique());
            attributeMetadata.setMandatory(attributeDefinition.isMandatory());
            attributeMetadata.setVisible(attributeDefinition.isVisible());
            attributeMetadata.setNoCopy(attributeDefinition.isNoCopy());

            mem.createAttribute(className, attributeMetadata, true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Created attribute in %s class", className));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttribute(long classId, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createAttribute", ipAddress, sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(attributeDefinition.getName());
            attributeMetadata.setDisplayName(attributeDefinition.getDisplayName());
            attributeMetadata.setDescription(attributeDefinition.getDescription());
            attributeMetadata.setReadOnly(attributeDefinition.isReadOnly());
            attributeMetadata.setType(attributeDefinition.getType());
            attributeMetadata.setUnique(attributeDefinition.isUnique());
            attributeMetadata.setMandatory(attributeDefinition.isMandatory());
            attributeMetadata.setVisible(attributeDefinition.isVisible());
            attributeMetadata.setNoCopy(attributeDefinition.isNoCopy());
            ClassMetadata classMetadata = mem.getClass(classId);
            mem.createAttribute(classId, attributeMetadata);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Created attribute in %s class", classMetadata.getName()));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setClassProperties(ClassInfo newClassDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setClassProperties", ipAddress, sessionId);
            ClassMetadata cm = new ClassMetadata();

            cm.setId(newClassDefinition.getId());
            cm.setName(newClassDefinition.getClassName());
            cm.setDisplayName(newClassDefinition.getDisplayName());
            cm.setDescription(newClassDefinition.getDescription());
            cm.setParentClassName(newClassDefinition.getParentClassName());
            cm.setAbstract(newClassDefinition.isAbstract());
            cm.setCountable(newClassDefinition.isCountable());
            cm.setInDesign(newClassDefinition.isInDesign());
            cm.setIcon(newClassDefinition.getIcon());
            cm.setSmallIcon(newClassDefinition.getSmallIcon());
            cm.setColor(newClassDefinition.getColor());
            //cm.setCategory(classDefinition.getCategory());
            
            ChangeDescriptor changeDescriptor = mem.setClassProperties(cm);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                changeDescriptor);

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public boolean hasAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("hasAttribute", ipAddress, sessionId);
            return mem.hasAttribute(className, attributeName);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAttribute", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.getDescription());
            return atrbInfo;
         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("setClassProperties", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeId);

            AttributeInfo attrInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.getDescription());
            return attrInfo;

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(long classId, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setAttributeProperties", ipAddress, sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(newAttributeDefinition.getId());
            attrMtdt.setName(newAttributeDefinition.getName());
            attrMtdt.setDisplayName(newAttributeDefinition.getDisplayName());
            attrMtdt.setDescription(newAttributeDefinition.getDescription());
            attrMtdt.setType(newAttributeDefinition.getType());
            attrMtdt.setAdministrative(newAttributeDefinition.isAdministrative());
            attrMtdt.setUnique(newAttributeDefinition.isUnique());
            attrMtdt.setMandatory(newAttributeDefinition.isMandatory());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());

            ChangeDescriptor changeDescriptor = mem.setAttributeProperties(classId, attrMtdt);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                changeDescriptor);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setAttributeProperties", ipAddress, sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(newAttributeDefinition.getId());
            attrMtdt.setName(newAttributeDefinition.getName());
            attrMtdt.setDisplayName(newAttributeDefinition.getDisplayName());
            attrMtdt.setDescription(newAttributeDefinition.getDescription());
            attrMtdt.setType(newAttributeDefinition.getType());
            attrMtdt.setAdministrative(newAttributeDefinition.isAdministrative());
            attrMtdt.setUnique(newAttributeDefinition.isUnique());
            attrMtdt.setMandatory(newAttributeDefinition.isMandatory());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());

            mem.setAttributeProperties(className, attrMtdt);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Updated property in %s class", className));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteAttribute", ipAddress, sessionId);
            mem.deleteAttribute(className, attributeName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted attribute in %s class", className));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(long classId, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteAttribute", ipAddress, sessionId);
            ClassMetadata classMetadata = mem.getClass(classId);
            mem.deleteAttribute(classId, attributeName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_METADATA_OBJECT, 
                String.format("Deleted attribute in %s class", classMetadata.getName()));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleChildren", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }

    }
    
    @Override
    public List<ClassInfoLight> getPossibleSpecialChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleSpecialChildren", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildren(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleChildrenNoRecursive", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getPossibleSpecialChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleSpecialChildrenNoRecursive", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        
    @Override
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUpstreamContainmentHierarchy", ipAddress, sessionId);
            List<ClassInfoLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive)){
                res.add(new ClassInfoLight(cil, new Validator[]{}));
            }
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUpstreamSpecialContainmentHierarchy", ipAddress, sessionId);
            List<ClassInfoLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamSpecialContainmentHierarchy(className, recursive)){
                res.add(new ClassInfoLight(cil, new Validator[]{}));
            }
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPossibleChildren", ipAddress, sessionId);
            ClassMetadata classMetadata = parentClassId == -1 ? null : mem.getClass(parentClassId);
            mem.addPossibleChildren(parentClassId, possibleChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible children to %s", classMetadata == null ? "Navigation Tree Root" : classMetadata.getName()));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(long parentClassId, long[] possibleSpecialChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPossibleSpecialChildren", ipAddress, sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            mem.addPossibleSpecialChildren(parentClassId, possibleSpecialChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible special children to %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPossibleChildren", ipAddress, sessionId);
            mem.addPossibleChildren(parentClassName, possibleChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible children to %s", parentClassName));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void addPossibleSpecialChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPossibleChildren", ipAddress, sessionId);
            mem.addPossibleSpecialChildren(parentClassName, possibleChildren);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Added possible special children to %s", parentClassName));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("removePossibleChildren", ipAddress, sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Removed possible children from %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void removePossibleSpecialChildren(long parentClassId, long[] specialChildrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("removePossibleSpecialChildren", ipAddress, sessionId);
            ClassMetadata classMetadata = null;
            if (parentClassId != -1)
                classMetadata = mem.getClass(parentClassId);
            
            mem.removePossibleSpecialChildren(parentClassId, specialChildrenToBeRemoved);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Removed possible special children from %s class", classMetadata != null ? classMetadata.getName() : "Navigation Tree Root"));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createListTypeItem", ipAddress, sessionId);
            
            long lstTypeItemId = aem.createListTypeItem(className, name, displayName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created list Type Item %s (%s)", name, className));
            
            return lstTypeItemId;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deletelistTypeItem", ipAddress, sessionId);
            aem.deleteListTypeItem(className, oid, realeaseRelationships);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted list Type Item with id %s", oid));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getListTypeItems(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItems", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
            List<RemoteObjectLight> res = new ArrayList<>();
            for (RemoteBusinessObjectLight listTypeItem : listTypeItems)
                res.add(new RemoteObjectLight(listTypeItem));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public byte[] getClassHierarchy(boolean showAll, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClassHierarchy", ipAddress, sessionId);
            return aem.getClassHierachy(showAll);
        }catch (InventoryException ex){
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteSession createSession(String user, String password, String IPAddress)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            Session newSession = aem.createSession(user, password, IPAddress);
            aem.createGeneralActivityLogEntry(user, ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, String.format("Connected from %s", IPAddress));
            return new RemoteSession(newSession.getToken(), newSession.getUser());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage()); 
        }
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws ServerSideException, NotAuthorizedException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            String user = getUserNameFromSession(sessionId);
            aem.closeSession(sessionId, remoteAddress);
            aem.createGeneralActivityLogEntry(user, ActivityLogEntry.ACTIVITY_TYPE_CLOSE_SESSION, String.format("Connected from %s", remoteAddress));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage()); 
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public List<RemoteObjectLight> getObjectChildren(long oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(objectClassId, oid, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSiblings", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSiblings(className, oid, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getChildrenOfClass", ipAddress, sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getChildrenOfClassLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(long parentOid, String parentClass, 
            String classToFilter, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSpecialChildrenOfClassLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getChildrenOfClassLightRecursive", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLightRecursive(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getObject(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObject", ipAddress, sessionId);
            return new RemoteObject(bem.getObject(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectLight", ipAddress, sessionId);
            return new RemoteObjectLight(bem.getObjectLight(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight getCommonParent(String aObjectClass, long aOid, String bObjectClass, long bOid, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getCommonParent", ipAddress, sessionId);
            RemoteBusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aOid, bObjectClass, bOid);
            if (commonParent.getId() != -1) // is not DummyRoot
                return new RemoteObjectLight(commonParent.getId(), commonParent.getName(), commonParent.getClassName());
            else
                return new RemoteObjectLight(-1L, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getParent(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParent", ipAddress, sessionId);
            RemoteBusinessObjectLight parent = bem.getParent(objectClass, oid);
            return new RemoteObjectLight(parent.getId(), parent.getName(), parent.getClassName());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getParents(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParents", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getParents(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, long objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSpecialAttribute", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(objectClass, objectId, attributeName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, long oid, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSpecialAttributes", ipAddress, sessionId);
            HashMap<String, List<RemoteBusinessObjectLight>> relationships = bem.getSpecialAttributes(objectClass, oid);
            RemoteObjectSpecialRelationships res = new RemoteObjectSpecialRelationships(relationships);

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClassName, 
            long oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParentsUntilFirstOfClass", ipAddress, sessionId);
            List<RemoteObjectLight> remoteObjects = new ArrayList<>();
            for (RemoteBusinessObjectLight remoteObject : bem.getParentsUntilFirstOfClass(objectClassName, oid, objectToMatchClassName))
                remoteObjects.add(new RemoteObjectLight(remoteObject));
            return remoteObjects;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight getFirstParentOfClass(String objectClassName, 
        long oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException {
                
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getLastParentUntilFirstOfClass", ipAddress, sessionId);
            RemoteBusinessObjectLight firstParentOfClass = bem.getFirstParentOfClass(objectClassName, oid, objectToMatchClassName);
            return firstParentOfClass != null ? new RemoteObjectLight(firstParentOfClass) : null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParentOfClass", ipAddress, sessionId);
            return new RemoteObject(bem.getParentOfClass(objectClass, oid, parentClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }  

    @Override
    public List<RemoteObjectLight> getObjectSpecialChildren (String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectSpecialChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(objectClass, objectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectsOfClassLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsOfClassLight(className, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public ClassInfoLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));

        try {
            aem.validateWebServiceCall("getInstanceableListTypes", ipAddress, sessionId);
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
            ClassInfoLight[] res = new ClassInfoLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new ClassInfoLight(instanceableListTypes.get(i), new Validator[0]);
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateWebServiceCall("createObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
            
            long newObjectId = bem.createObject(className, parentClassName, parentOid, attributes, template);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newObjectId));
            return newObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateWebServiceCall("createSpecialObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);

            long newSpecialObjectId = bem.createSpecialObject(className, parentClassName, parentOid, attributes, template);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newSpecialObjectId));
            
            return newSpecialObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteObject(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            bem.deleteObject(className, oid, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("Object with id %s of class %s deleted", className, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteObjects(String[] classNames, long[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (classNames.length != oids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("deleteObjects", ipAddress, sessionId);
            HashMap<String,List<Long>> objects = new HashMap<>();
            for (int i = 0; i< classNames.length;i++) {
                List<Long> existingObjects = objects.get(classNames[i]);
                if (existingObjects == null){
                    List<Long> newIdList = new ArrayList<>();
                    newIdList.add(oids[i]);
                    objects.put(classNames[i], newIdList);
                }
                else
                    existingObjects.add(oids[i]);
            }

            bem.deleteObjects(objects, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("%s objects deleted", oids.length ));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveObjectsToPool(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjectsToPool", ipAddress, sessionId);
            HashMap<String,List<Long>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<Long> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }

            HashMap<String,long[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<Long> ids = temObjects.get(className);
                long[] ids_ = new long[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjectsToPool(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to pool with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjects", ipAddress, sessionId);
            HashMap<String,List<Long>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<Long> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,long[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<Long> ids = temObjects.get(className);
                long[] ids_ = new long[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjects(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void moveSpecialObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveSpecialObjects", ipAddress, sessionId);
            HashMap<String,List<Long>> temObjects = new HashMap<>();
            
            for (int i = 0; i< objectClasses.length; i++){
                List<Long> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,long[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<Long> ids = temObjects.get(className);
                long[] ids_ = new long[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            
            bem.moveSpecialObjects(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void movePoolItem(long poolId, String poolItemClassName, long poolItemId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("movePoolItem", ipAddress, sessionId);
            bem.movePoolItem(poolId, poolItemClassName, poolItemId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                String.format("%s moved to pool with id %s", poolItemId, poolId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long[] copyObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("copyObjects", ipAddress, sessionId);
            HashMap<String,long[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
            }
            long[] newObjects = bem.copyObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special) object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long[] copySpecialObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("copySpecialObjects", ipAddress, sessionId);
            HashMap<String,long[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
            }
            long[] newObjects = bem.copySpecialObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special)object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long copyPoolItem(long poolId, String poolItemClassName, long poolItemId, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("movePoolItem", ipAddress, sessionId);
            long id = bem.copyPoolItem(poolId, poolItemClassName, poolItemId, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                String.format("%s moved to pool with id %s", poolItemId, poolId));
            return id;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateObject(String className, long oid, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateWebServiceCall("updateObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);

            ChangeDescriptor theChange = bem.updateObject(className, oid, attributes);
            
            if (mem.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className))
                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, theChange);
            else
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), className,
                        oid, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, theChange);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<AttributeInfo> getMandatoryAttributesInClass(String className, String ipAddress, String sessionId)  throws ServerSideException{
         if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
                aem.validateWebServiceCall("getMandatoryAttributesInClass", ipAddress, sessionId);
                List<AttributeMetadata> mandatoryObjectAttributes = bem.getMandatoryAttributesInClass(className);
                return AttributeMetadata.toAttributeInfo(mandatoryObjectAttributes);

            } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long [] createBulkObjects(String className, String parentClassName, long parentOid, int numberOfObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkObjects", ipAddress, sessionId);
            
            long[] newObjects = bem.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new objects of class %s", numberOfObjects, className));
            
            return newObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long[] createBulkSpecialObjects(String className, String parentClassName, long parentId, int numberOfSpecialObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkSpecialObjects", ipAddress, sessionId);
            
            long[] newSpecialObjects = bem.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new special objects  of class %s", numberOfSpecialObjects, className));
            
            return newSpecialObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Physical connections
    @Override
    public void connectMirrorPort(String[] aObjectClass, long[] aObjectId, String[] bObjectClass, long[] bObjectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        if (aObjectId == bObjectId)
            throw new ServerSideException("A port can not be mirror to itself");
        
        try {
            aem.validateWebServiceCall("connectMirrorPort", ipAddress, sessionId);
            
            if(aObjectClass.length != bObjectClass.length || bObjectId.length != aObjectId.length){
                throw new ServerSideException(String.format("Not the same number of front = %s and back ports = %s", aObjectId.length, bObjectId.length));
            }
            
            for (int i=0; i < aObjectClass.length; i++) {
                if (!mem.isSubClass("GenericPort", aObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));
            
                if (!mem.isSubClass("GenericPort", bObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));

                if (bem.hasSpecialRelationship(aObjectClass[i], aObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));

                if (bem.hasSpecialRelationship(bObjectClass[i], bObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));
                
                bem.createSpecialRelationship(aObjectClass[i], aObjectId[i], bObjectClass[i], bObjectId[i], "mirror", true); //NOI18N
            
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), aObjectClass[i], aObjectId[i], 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    "mirror", "", Long.toString(aObjectId[i]) + ", " + Long.toString(bObjectId[i]), ""); //NOI18N          
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseMirrorPort(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseMirrorPort", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPort", objectClass)) //NOI18N
                throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(objectClass, objectId)));
                        
            RemoteBusinessObjectLight theOtherPort = null;
            if (bem.hasSpecialRelationship(objectClass, objectId, "mirror", 1)) //NOI18N
                theOtherPort = bem.getSpecialAttribute(objectClass, objectId, "mirror").get(0); //NOI18N
            
            if (theOtherPort == null)
                throw new ServerSideException(String.format("Object %s no has a mirror port", bem.getObjectLight(objectClass, objectId)));
                
            bem.releaseSpecialRelationship(objectClass, objectId, -1, "mirror"); //NOI18N   
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), objectClass, objectId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "mirror", Long.toString(theOtherPort.getId()), "", ""); //NOI18N            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPhysicalConnection(String aObjectClass, long aObjectId,
            String bObjectClass, long bObjectId, String parentClass, long parentId,
            String name, String connectionClass, long templateId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        long newConnectionId = -1;
        
        try {
            aem.validateWebServiceCall("createPhysicalConnection", ipAddress, sessionId);
            
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass)) //NOI18N
                throw new ServerSideException("Class %s is not subclass of GenericPhysicalConnection"); //NOI18N

            boolean isLink = false;
            
            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubClass("GenericPhysicalLink", connectionClass)) { //NOI18N
                
                if (!mem.isSubClass("GenericPort", aObjectClass) || !mem.isSubClass("GenericPort", bObjectClass)) //NOI18N
                    throw new ServerSideException("One of the endpoints provided is not a port");
                
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, "endpointA").isEmpty()) //NOI18N
                    
                    throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(aObjectClass, aObjectId)));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, "endpointB").isEmpty()) //NOI18N
                    throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(bObjectClass, bObjectId)));
                
                isLink = true;
            }

            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new ServerSideException("The name of the connection can not be empty");
            
            attributes.put(Constants.PROPERTY_NAME, name);
            
            newConnectionId = bem.createSpecialObject(connectionClass, parentClass, parentId, attributes, templateId);
            
            if (isLink) { //Check connector mappings only if it's a link
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClass, aObjectId);
                aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClass, bObjectId);
            }
            
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, "endpointA", true);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, "endpointB", true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            
            return newConnectionId;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1)
                deleteObjects(new String[]{ connectionClass }, new long[]{ newConnectionId }, true, ipAddress, sessionId);

            throw new ServerSideException(e.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, long connectionId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getConnectionEndpoints", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));

            List<RemoteBusinessObjectLight> endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA"); //NOI18N
            List<RemoteBusinessObjectLight> endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB"); //NOI18N
            return new RemoteObjectLight[] {endpointA.isEmpty() ? null : new RemoteObjectLight(endpointA.get(0)), 
                                            endpointB.isEmpty() ? null : new RemoteObjectLight(endpointB.get(0))};

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, Long[] sideAIds, 
                String[] linksClassNames, Long[] linksIds, String[] sideBClassNames, 
                Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("connectPhysicalLinks", ipAddress, sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (linksClassNames[i] != null && !mem.isSubClass("GenericPhysicalLink", linksClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical link", linksClassNames[i]));
                if (sideAClassNames[i] != null && !mem.isSubClass("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubClass("GenericPort", sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideBClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect a port to itself");
                
                List<RemoteBusinessObjectLight> aEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointA"); //NOI18N
                List<RemoteBusinessObjectLight> bEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointB"); //NOI18N
                
                if (!aEndpointList.isEmpty()){
                    if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (!bEndpointList.isEmpty()){
                    if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointA").isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointB").isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideAClassNames[i], sideAIds[i])));
                    
                    if (aEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i], "endpointA", true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint A", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointB").isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointA").isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideBClassNames[i], sideBIds[i])));
                    
                    if (bEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i], "endpointB", true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint B", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
            }
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Connected physical links %s-%s-%s", Arrays.toString(sideAIds), Arrays.toString(linksIds), Arrays.toString(sideBIds)));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectPhysicalContainers(String[] sideAClassNames, Long[] sideAIds, 
                String[] containersClassNames, Long[] containersIds, String[] sideBClassNames, 
                Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("connectPhysicalContainers", ipAddress, sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (containersClassNames[i] != null && !mem.isSubClass("GenericPhysicalContainer", containersClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical container", containersClassNames[i]));
                if (sideAClassNames[i] != null && mem.isSubClass("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                if (sideBClassNames[i] != null && mem.isSubClass("GenericPort", sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect an object to itself");
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    List<RemoteBusinessObjectLight> aEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointA"); //NOI18N
                    if (aEndpointList.isEmpty())
                        bem.createSpecialRelationship(containersClassNames[i], containersIds[i], sideAClassNames[i], sideAIds[i], "endpointA", true); //NOI18N
                    else
                        throw new ServerSideException(String.format("Container %s already has an endpoint A", bem.getObjectLight(containersClassNames[i], containersIds[i])));
                }
                
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    List<RemoteBusinessObjectLight> bEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointB"); //NOI18N
                    if (bEndpointList.isEmpty())
                        bem.createSpecialRelationship(containersClassNames[i], containersIds[i], sideBClassNames[i], sideBIds[i], "endpointB", true); //NOI18N
                    else
                        throw new ServerSideException(String.format("Container %s already has a endpoint B", bem.getObjectLight(containersClassNames[i], containersIds[i])));
                }
            }
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Connected physical containers %s-%s-%s", Arrays.toString(sideAIds), Arrays.toString(containersIds), Arrays.toString(sideBIds)));            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void disconnectPhysicalConnection(String connectionClass, long connectionId, 
            int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("disconnectPhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            String  affectedProperties = "", oldValues = "";
            
            switch (sideToDisconnect) {
                case 1: //A side
                    RemoteBusinessObjectLight endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += Long.toString(endpointA.getId()) + " ";
                    break;
                case 2: //B side
                    RemoteBusinessObjectLight endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointB")); //NOI18N
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += Long.toString(endpointB.getId()) + " ";
                    break;
                case 3: //Both sides
                    endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N
                    endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA", "endpointB")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += Long.toString(endpointA.getId()) + " ";
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += Long.toString(endpointB.getId()) + " ";
                    break;
                default:
                    throw new ServerSideException(String.format("Wrong side to disconnect option"));
            }
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), connectionClass, connectionId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, oldValues, "", ""); //NOI18N
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getPhysicalPath(String objectClassName, long oid, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPhysicalPath", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPort", objectClassName))
                throw new ServerSideException(String.format("Class %s is not a port", objectClassName));
            
            List<RemoteBusinessObjectLight> thePath = bem.getPhysicalPath(objectClassName, oid); 
            return RemoteObjectLight.toRemoteObjectLightArray(thePath);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, 
            long linkId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getLogicalLinkDetails", ipAddress, sessionId); //NOI18N
            
            RemoteBusinessObject linkObject = bem.getObject(linkId);
            
            RemoteBusinessObjectLight endpointA = null, endpointB = null;
            List<RemoteBusinessObjectLight> physicalPathA = null, physicalPathB = null;
            String endpointARelationshipName, endpointBRelationshipName;
            
            if (mem.isSubClass("GenericSDHTributaryLink", linkClass)) { //NOI18N
                endpointARelationshipName = "sdhTTLEndpointA"; //NOI18N
                endpointBRelationshipName = "sdhTTLEndpointB"; //NOI18N
                
            } else {
                if ("MPLSLink".equals(linkClass)) { //NOI18N
                    endpointARelationshipName = "mplsEndpointA"; //NOI18N
                    endpointBRelationshipName = "mplsEndpointB"; //NOI18N
                }
                else
                    throw new ServerSideException(String.format("Class %s is not a supported logical link", linkClass)); //NOI18N
            }
            
            List<RemoteBusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName); //NOI18N
            if (!endpointARelationship.isEmpty()) {
                endpointA = endpointARelationship.get(0);
                physicalPathA = bem.getPhysicalPath(endpointA.getClassName(), endpointA.getId());
            }

            List<RemoteBusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName); //NOI18N
            if (!endpointBRelationship.isEmpty()) {
                endpointB = endpointBRelationship.get(0);
                physicalPathB = bem.getPhysicalPath(endpointB.getClassName(), endpointB.getId());
            }

            return new RemoteLogicalConnectionDetails(linkObject, endpointA, endpointB, physicalPathA, physicalPathB);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClassName, long objectAId,
            String objectBClassName, long objectBId, String containerClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getContainersBetweenObjects", ipAddress, sessionId); //NOI18N
            List<RemoteObjectLight> res = new ArrayList<>();

            HashMap<String, List<RemoteBusinessObjectLight>> specialAttributesA = bem.getSpecialAttributes(objectAClassName, objectAId);
            HashMap<String, List<RemoteBusinessObjectLight>> specialAttributesB = bem.getSpecialAttributes(objectBClassName, objectBId);
            
            if(!specialAttributesA.isEmpty() && !specialAttributesB.isEmpty()){
                List<RemoteBusinessObjectLight> wireContainersListA = new  ArrayList<>();

                if(specialAttributesA.get("endpointA") != null){
                    for(RemoteBusinessObjectLight container : specialAttributesA.get("endpointA")){
                        if(container.getClassName().equals(containerClassName))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesA.get("endpointB") != null){
                    for(RemoteBusinessObjectLight container : specialAttributesA.get("endpointB")){
                        if(container.getClassName().equals(containerClassName))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesB.get("endpointA") != null){
                    for(RemoteBusinessObjectLight container : specialAttributesB.get("endpointA")){
                        if(container.getClassName().equals(containerClassName)){
                            if(wireContainersListA.contains(container))
                                res.add(new RemoteObjectLight(container));
                        }
                    }
                }

                if(specialAttributesB.get("endpointB") != null){
                    for(RemoteBusinessObjectLight container : specialAttributesB.get("endpointB")){
                        if(container.getClassName().equals(containerClassName)){
                            if(wireContainersListA.contains(container))
                                res.add(new RemoteObjectLight(container));
                        }
                    }
                }
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, long objectId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getPhysicalConnectionsInObject", ipAddress, sessionId); //NOI18N
            List<RemoteObjectLightList> res = new ArrayList<>();
            
            List<RemoteBusinessObjectLight> allCommunicationsPorts = bem.getChildrenOfClassLightRecursive(objectId, objectClass, "GenericCommunicationsPort", -1);
            
            for (RemoteBusinessObjectLight aCommunicationsPort : allCommunicationsPorts) {
                List<RemoteBusinessObjectLight> physicalPath = bem.getPhysicalPath(aCommunicationsPort.getClassName(), aCommunicationsPort.getId());
                if (physicalPath.size() > 1)
                    res.add(new RemoteObjectLightList(RemoteObjectLight.toRemoteObjectLightArray(physicalPath)));
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deletePhysicalConnection(String objectClassName, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deletePhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", objectClassName))
                throw new ServerSideException(String.format("Class %s is not a physical connection", objectClassName));
            
            bem.deleteObject(objectClassName, objectId, true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                    String.format("Deleted %s instance with id %s", objectClassName, objectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Service Manager
    @Override
    public void associateObjectToService(String objectClass, long objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("associateObjectToService", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, "uses", true); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", "", Long.toString(objectId), ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    @Override
    public void associateObjectsToService(String[] objectClass, long[] objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            String affectedProperties = "", newValues = "";
            aem.validateWebServiceCall("associateObjectsToService", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            for (int i = 0; i < objectId.length; i++) {
                bem.createSpecialRelationship(serviceClass, serviceId, objectClass[i], objectId[i], "uses", true); //NOI18N
                affectedProperties += "uses" + " "; //NOI18N
                newValues += objectId[i] + " ";
            }            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, "", newValues, "Associate objects to service"); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromService(String serviceClass, long serviceId, long otherObjectId, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseObjectFromService", ipAddress, sessionId);
            bem.releaseSpecialRelationship(serviceClass, serviceId, otherObjectId, "uses"); //NOI18N
                       
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", Long.toString(otherObjectId), "", "Release object from service"); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getServiceResources(String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getServiceResources", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(serviceClass, serviceId, "uses")); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    @Override
    public List<UserInfo> getUsers(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUsers", ipAddress, sessionId);
            List<UserProfile> users = aem.getUsers();

            List<UserInfo> usersInfo = new ArrayList<>();
            for (UserProfile userProfile : users)
                usersInfo.add(new UserInfo(userProfile));
            return usersInfo;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<UserInfo> getUsersInGroup(long groupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUsersInGroup", ipAddress, sessionId);
            List<UserProfile> users = aem.getUsersInGroup(groupId);

            List<UserInfo> usersInfo = new ArrayList<>();
            for (UserProfile userProfile : users)
                usersInfo.add(new UserInfo(userProfile));
            
            return usersInfo;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<GroupInfoLight> getGroupsForUser(long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGroupsForUser", ipAddress, sessionId);
            List<GroupProfileLight> groups = aem.getGroupsForUser(userId);

            List<GroupInfoLight> groupsInfo = new ArrayList<>();
            for (GroupProfileLight groupProfile : groups)
                groupsInfo.add(new GroupInfoLight(groupProfile));
            
            return groupsInfo;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<GroupInfo> getGroups(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGroups", ipAddress, sessionId);
            List<GroupProfile> groups = aem.getGroups();

            List<GroupInfo> userGroupInfo = new ArrayList<>();
            for (GroupProfile group : groups)
                userGroupInfo.add(new GroupInfo(group));
                        
            return userGroupInfo;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, 
    String firstName, String lastName, int enabled, int type, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setUserProperties", ipAddress, sessionId);
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set user %s properties", userName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addUserToGroup(long userId, long groupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addUserToGroup", ipAddress, sessionId);
            aem.addUserToGroup(userId, groupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Added user to group", groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removeUserFromGroup(long userId, long groupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("removeUserFromGroup", ipAddress, sessionId);
            aem.removeUserFromGroup(userId, groupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed user from group", groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setPrivilegeToUser(long userId, String featureToken, int accessLevel, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPrivilegeToUser", ipAddress, sessionId);
            aem.setPrivilegeToUser(userId, featureToken, accessLevel);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set privilege %s to user %s", featureToken, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setPrivilegeToGroup(long groupId, String featureToken, int accessLevel, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("addPrivilegeToGroup", ipAddress, sessionId);
            aem.setPrivilegeToGroup(groupId, featureToken, accessLevel);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set privilege %s to group %s", featureToken, groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePrivilegeFromUser(long userId, String featureToken, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("removePrivilegeFromUser", ipAddress, sessionId);
            aem.removePrivilegeFromUser(userId, featureToken);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed privilege %s to user %s", featureToken, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePrivilegeFromGroup(long groupId, String featureToken, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("removePrivilegeFromGroup", ipAddress, sessionId);            
            aem.removePrivilegeFromGroup(groupId, featureToken);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Removed privilege %s to group %s", featureToken, groupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createGroup(String groupName, String description, List<Long> users, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createGroup", ipAddress, sessionId);
            
            long groupId = aem.createGroup(groupName, description, users);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("New group %s", groupName));
            return groupId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createUser(String userName, String password, String firstName, 
        String lastName, boolean enabled, int type, List<PrivilegeInfo> privileges, 
        long defaultGroupId, String ipAddress, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createUser", ipAddress, sessionId);
            List<Privilege> remotePrivileges = new ArrayList<>();
            
            if (privileges != null) {
                for (PrivilegeInfo privilege : privileges) {
                    Privilege remotePrivilege = new Privilege(privilege.getFeatureToken(), privilege.getAccessLevel());
                    if (!remotePrivileges.contains(remotePrivilege)) //Ignore duplicated privileges. This should not happen, but, you now...
                        remotePrivileges.add(remotePrivilege);
                }
            }
            
            long newUserId = aem.createUser(userName, password, firstName, lastName, enabled, type, remotePrivileges, defaultGroupId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("New User %s", userName));
            
            return newUserId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setGroupProperties(long oid, String groupName, String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setGroupProperties", ipAddress, sessionId);
            aem.setGroupProperties(oid, groupName, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("Set group %s properties", groupName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(long[] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteUsers", ipAddress, sessionId);
            aem.deleteUsers(oids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("%s users deleted", oids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteGroups(long[] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteGroups", ipAddress, sessionId);
            aem.deleteGroups(oids);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s groups deleted", oids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createListTypeItemRelatedView(long listTypeItemId, String listTypeItemClassName, String viewClassName, 
        String name, String description, byte [] structure, byte [] background, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createListTypeItemRelateView", ipAddress, sessionId);            
            
            long viewId = aem.createListTypeItemRelatedView(listTypeItemId, listTypeItemClassName, viewClassName, name, description, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s [%s] list type item related view %s [%s] with id %s", listTypeItemId, listTypeItemClassName, name, viewClassName, viewId));
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, 
        String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateListTypeItemRelatedView", ipAddress, sessionId);
            
            ChangeDescriptor theChange = aem.updateListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId, name, description, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        }catch(InventoryException ie){
            throw new ServerSideException(ie.getMessage());
        }
    }
    
    @Override
    public ViewInfo getListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItemRelatedView", ipAddress, sessionId);
            ViewObject myView = aem.getListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId);
            if (myView == null)
                return null;
            ViewInfo res = new ViewInfo(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public ViewInfoLight[] getListTypeItemRelatedViews(long listTypeItemId, String listTypeItemClass, int limit, 
        String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItemRelatedViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(listTypeItemId, listTypeItemClass, limit);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            int i = 0;
            for (ViewObjectLight view : views){
                res[i] = new ViewInfoLight(view);
                i++;
            }
            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }   
    }
    
    @Override
    public void deleteListTypeItemRelatedView(long listTypeItemId, String listTypeItemClass, long viewId, 
        String ipAddress, String sessionId) 
        throws ServerSideException {        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteListTypeItemRelatedView", ipAddress, sessionId);
            aem.deleteListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());            
        }
    }

    @Override   
    public long createObjectRelatedView(long objectId, String objectClass, String name, 
        String description, String viewClassName, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createObjectRelatedView", ipAddress, sessionId);            
            
            long viewId = aem.createObjectRelatedView(objectId, objectClass, name, description, viewClassName, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s [%s] object related view %s [%s] with id %s", objectId, objectClass, name, viewClassName, viewId));
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(String viewClass, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createGeneralView", ipAddress, sessionId);
                        
            long viewId = aem.createGeneralView(viewClass, name, description, structure, background);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created general view %s [%s] with id %s", name, viewClass, viewId));
            
            return viewId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ViewInfo getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectRelatedView", ipAddress, sessionId);
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId);
            if (myView == null)
                return null;
            ViewInfo res = new ViewInfo(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getObjectRelatedViews(long oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectRelatedViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getObjectRelatedViews(oid, objectClass, limit);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            int i = 0;
            for (ViewObjectLight view : views){
                res[i] = new ViewInfoLight(view);
                i++;
            }
            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }

    @Override
    public ViewInfo getGeneralView(long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGeneralView", ipAddress, sessionId);
            ViewObject viewObject = aem.getGeneralView(viewId);
            if(viewObject == null) {
                return null;
            }
            ViewInfo viewInfo = new ViewInfo(viewObject);
            viewInfo.setBackground(viewObject.getBackground());
            return viewInfo;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }
   
    @Override
    public ViewInfoLight[] getGeneralViews(String viewClassName, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGeneralViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getGeneralViews(viewClassName, limit);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            for (int i = 0; i < views.size(); i++)
                res[i] = new ViewInfoLight(views.get(i));
           return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }
    
    @Override
    public void updateObjectRelatedView(long objectOid, String objectClass, 
        long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateObjectRelatedView", ipAddress, sessionId);
            
            ChangeDescriptor theChange = aem.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        }catch(InventoryException ie){
            throw new ServerSideException(ie.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, 
        byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateGeneralView", ipAddress, sessionId);
            
            ChangeDescriptor theChange = aem.updateGeneralView(viewId, viewName, viewDescription, structure, background);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        } catch(InventoryException ie) {
            throw new ServerSideException(ie.getMessage());
        }
    }

    @Override
    public void deleteGeneralView(long [] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteGeneralView", ipAddress, sessionId);
            aem.deleteGeneralViews(oids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s general views deleted", oids.length));            
        } catch(InventoryException ie) {
            throw new ServerSideException(ie.getMessage());
        }
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createQuery", ipAddress, sessionId);
            
            long queryId = aem.createQuery(queryName, ownerOid, queryStructure, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create query %s with id %s", queryName, queryId));             
            return queryId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, 
        String description, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("saveQuery", ipAddress, sessionId);
            
            ChangeDescriptor changeDescriptor = aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteQuery", ipAddress, sessionId);
            aem.deleteQuery(queryOid);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted query with id %s", queryOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getQueries", ipAddress, sessionId);
            List<CompactQuery> queries = aem.getQueries(showPublic);
            RemoteQueryLight[] rql =  new RemoteQueryLight[queries.size()];
            Integer i = 0;
            for (CompactQuery compactQuery : queries) {
                rql[i] = new RemoteQueryLight(compactQuery.getId(),
                        compactQuery.getName(),
                        compactQuery.getDescription(),
                        compactQuery.getIsPublic());
                i++;
            }
            return rql;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteQuery getQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getQuery", ipAddress, sessionId);
            return new RemoteQuery(aem.getQuery(queryOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ResultRecord[] executeQuery(TransientQuery query, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeQuery", ipAddress, sessionId);
            List<org.kuwaiba.apis.persistence.application.ResultRecord> resultRecordList = aem.executeQuery(transientQuerytoExtendedQuery(query));

            ResultRecord[] resultArray = new ResultRecord[resultRecordList.size()];
            
            for (int i=0;resultRecordList.size() >i; i++)
            {
                RemoteObjectLight rol = new RemoteObjectLight(resultRecordList.get(i).getId(), resultRecordList.get(i).getName(), resultRecordList.get(i).getClassName());
                resultArray[i] = new ResultRecord(rol, (ArrayList<String>) resultRecordList.get(i).getExtraColumns());
            }

            return resultArray;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Pools
    @Override
    public long createRootPool(String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createRootPool", ipAddress, sessionId);
            
            long poolId = aem.createRootPool(name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Root Pool", name));
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPoolInObject(String parentClassname, long parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolInObject", ipAddress, sessionId);
            long poolId = aem.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s pool in %s object with id %s", name, parentClassname, parentId));            
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPoolInPool(long parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolInPool", ipAddress, sessionId);
            long poolId = aem.createPoolInPool(parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Pool In Pool with id %s ", name, parentId));  
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String attributeNames[], String attributeValues[], long templateId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolItem", ipAddress, sessionId);
            long objectId = bem.createPoolItem(poolId, className, attributeNames, attributeValues, templateId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created pool item with id", objectId));
            
            return objectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
   
    @Override
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deletePools", ipAddress, sessionId);
            aem.deletePools(ids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("%s pools deleted", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void setPoolProperties(long poolId, String name, String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setPoolProperties", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.setPoolProperties(poolId, name, description);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemotePool getPool(long poolId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPool", ipAddress, sessionId);
            return new RemotePool(aem.getPool(poolId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getRootPools", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = aem.getRootPools(className, type, includeSubclasses);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getPoolsInObject(String objectClassName, long objectId, String poolClass, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolsInObject", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = aem.getPoolsInObject(objectClassName, objectId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getPoolsInPool(long parentPoolId, String poolClass, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolsInPool", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = aem.getPoolsInPool(parentPoolId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getPoolItems(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolItems", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPoolItems(poolId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getBusinessObjectAuditTrail", ipAddress, sessionId);
            List<ActivityLogEntry> entries = aem.getBusinessObjectAuditTrail(objectClass, objectId, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGeneralActivityAuditTrail", ipAddress, sessionId);
            List<ActivityLogEntry> entries = aem.getGeneralActivityAuditTrail(page, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createTask(String name, String description, boolean enabled, boolean commitOnExecute, String script, 
            List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTask", ipAddress, sessionId);
            long res = aem.createTask(name, description, enabled, commitOnExecute, script, parameters, schedule, notificationType);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created task %s with id %s", name, res));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTaskProperties(long taskId, String propertyName, String propertyValue, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateTaskProperties", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskProperties(taskId, propertyName, propertyValue);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTaskParameters(long taskId, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateTaskParameters", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskParameters(taskId, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);  
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTaskSchedule(long taskId, TaskScheduleDescriptor schedule, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateTaskSchedule", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskSchedule(taskId, schedule);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateTaskNotificationType(long taskId, TaskNotificationDescriptor notificationType, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateTaskNotificationType", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTaskNotificationType(taskId, notificationType);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteTask getTask(long taskId, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTask", ipAddress, sessionId);
            Task theTask = aem.getTask(taskId);
            List<UserInfoLight> users = new ArrayList<>();
            
            for (UserProfileLight aUser : theTask.getUsers())
                users.add(new UserInfoLight(aUser));
            
            return new RemoteTask(theTask.getId(), theTask.getName(), theTask.getDescription(), theTask.isEnabled(), 
                                    theTask.commitOnExecute(), theTask.getScript(), theTask.getParameters(), theTask.getSchedule(), 
                                    theTask.getNotificationType(), users);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<UserInfoLight> getSubscribersForTask(long taskId, String ipAddress, String sessionId) throws ServerSideException {
         if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSubscribersForTask", ipAddress, sessionId);
            return aem.getSubscribersForTask(taskId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteTask> getTasks(String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTasks", ipAddress, sessionId);
            List<Task> tasks = aem.getTasks();
            
            List<RemoteTask> remoteTasks = new ArrayList<>();
            
            for (Task task : tasks) {
                List<UserInfoLight> users = new ArrayList<>();
                for (UserProfileLight aUser : task.getUsers())
                    users.add(new UserInfoLight(aUser));
                remoteTasks.add(new RemoteTask(task.getId(), task.getName(), task.getDescription(), task.isEnabled(), task.commitOnExecute(), task.getScript(),
                                    task.getParameters(), task.getSchedule(), task.getNotificationType(), users));
            }
            
            return remoteTasks;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteTask> getTasksForUser(long userId, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTasks", ipAddress, sessionId);
            List<Task> tasks = aem.getTasksForUser(userId);
            
            List<RemoteTask> remoteTasks = new ArrayList<>();
            
            for (Task task : tasks) {
                List<UserInfoLight> users = new ArrayList<>();
                for (UserProfileLight aUser : task.getUsers())
                    users.add(new UserInfoLight(aUser));
                remoteTasks.add(new RemoteTask(task.getId(), task.getName(), task.getDescription(), task.isEnabled(), task.commitOnExecute(), task.getScript(),
                                    task.getParameters(), task.getSchedule(), task.getNotificationType(), users));
            }
            return remoteTasks;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteTask(long taskId, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteTask", ipAddress, sessionId);
            aem.deleteTask(taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                    String.format("Deleted task with id %s", taskId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void subscribeUserToTask(long userId, long taskId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("subscribeUserToTask", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.subscribeUserToTask(userId, taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void unsubscribeUserFromTask(long userId, long taskId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("unsubscribeUserFromTask", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.unsubscribeUserFromTask(userId, taskId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteTaskResult executeTask(long taskId, String ipAddress, String sessionId) throws ServerSideException  {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeTask", ipAddress, sessionId);
            TaskResult theTaskResult = aem.executeTask(taskId);
            RemoteTaskResult remoteTaskResult = new RemoteTaskResult();
            
            for(ResultMessage resultMessage : theTaskResult.getMessages())
                remoteTaskResult.getMessages().add(new RemoteResultMessage(resultMessage.getMessageType(), resultMessage.getMessage()));
            
            return remoteTaskResult;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
       
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/Bulk load data methods">
    @Override
    public String bulkUpload(byte[] file, int commitSize, int dataType, String ipAddress, String sessionId) throws ServerSideException{
        if (sync == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        return sync.bulkUploadFromFile(file, commitSize, dataType, ipAddress, sessionId);
    }
    
    @Override
    public byte[] downloadBulkLoadLog(String fileName, String ipAddress, String sessionId) throws ServerSideException{
        if (sync == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            return sync.downloadBulkLoadLog(fileName, ipAddress, sessionId);
        } catch (IOException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>
    
    //<editor-fold desc="Templates" defaultstate="collapsed">
    @Override
    public long createTemplate(String templateClass, String templateName, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplate", ipAddress, sessionId);
            long templateId = aem.createTemplate(templateClass, templateName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created template %s", templateName));
            return templateId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createTemplateElement(String templateElementClass, String templateElementParentClassName, 
            long templateElementParentId, String templateElementName, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplateElement", ipAddress, sessionId);
            long templateElementId = aem.createTemplateElement(templateElementClass, templateElementParentClassName, 
                    templateElementParentId, templateElementName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create template element %s [%s] with id %s", templateElementName, templateElementClass, templateElementId));            
            return templateElementId;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, 
        long tsElementParentId, String tsElementName, String ipAddress, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplateElement", ipAddress, sessionId);
            long templateSpecialElementId = aem.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
                    tsElementParentId, tsElementName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create special template element %s [%s] with id %s", tsElementName, tsElementClass, templateSpecialElementId));
            return templateSpecialElementId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, long templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkTemplateElement", ipAddress, sessionId);
            long[] ids = aem.createBulkTemplateElement(templateElementClassName, templateElementParentClassName, 
                    templateElementParentId, numberOfTemplateElements, templateElementNamePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("%s new templates elements of class %s", numberOfTemplateElements, templateElementClassName));            
            return ids;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, long stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkSpecialTemplateElement", ipAddress, sessionId);
            long[] ids = aem.createBulkSpecialTemplateElement(stElementClass, stElementParentClassName, 
                    stElementParentId, numberOfTemplateElements, stElementNamePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("%s new special templates elements of class %s", numberOfTemplateElements, stElementClass));  
            return ids;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateTemplateElement(String templateElementClass, long templateElementId, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateTemplateElement", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.updateTemplateElement(templateElementClass, templateElementId, attributeNames, attributeValues);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT,
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteTemplateElement(String templateElementClass, long templateElementId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteTemplateElement", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = aem.deleteTemplateElement(templateElementClass, templateElementId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT,
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getTemplatesForClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplatesForClass", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> templates = aem.getTemplatesForClass(className);
            List<RemoteObjectLight> remoteTemplates = new ArrayList<>();
            
            for (RemoteBusinessObjectLight template : templates)
                remoteTemplates.add(new RemoteObjectLight(template));
            
            return remoteTemplates;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, 
            long templateElementId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplateElementChildren", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> templateElementChildren = aem.getTemplateElementChildren(templateElementClass, templateElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (RemoteBusinessObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild));
            
            return remoteTemplateElementChildren;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, 
            long tsElementId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplateSpecialElementChildren", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> templateElementChildren = aem.getTemplateSpecialElementChildren(tsElementClass, tsElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (RemoteBusinessObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild));
            
            return remoteTemplateElementChildren;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getTemplateElement(String templateElementClass, long templateElementId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplateElement", ipAddress, sessionId);
            return new RemoteObject(aem.getTemplateElement(templateElementClass, templateElementId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long[] copyTemplateElements(String[] sourceObjectsClassNames, long[] sourceObjectsIds, 
            String newParentClassName,long newParentId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copyTemplateElements", ipAddress, sessionId);
            long [] templateElementsIds = aem.copyTemplateElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                String.format("Copied %s template elements", templateElementsIds.length));
            return templateElementsIds;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, long[] sourceObjectsIds, 
        String newParentClassName, long newParentId, String ipAddress, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copyTemplateSpecialElements", ipAddress, sessionId);
            long [] templateSpecialElements = aem.copyTemplateSpecialElement(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                String.format("Copied %s template special elements", templateSpecialElements.length));
            return templateSpecialElements;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getDeviceLayouts(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getDeviceLayouts", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getDeviceLayouts());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public byte[] getDeviceLayoutStructure(long oid, String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getDeviceLayoutStructure", ipAddress, sessionId);
            return aem.getDeviceLayoutStructure(oid, className);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Reporting methods">
    @Override    
    public long createClassLevelReport(String className, String reportName, String reportDescription, 
            String script, int outputType, boolean enabled, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createClassLevelReport", ipAddress, sessionId);
            long reportId = bem.createClassLevelReport(className, reportName, reportDescription, script, outputType, enabled);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created class level report %s", reportName));
            return reportId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, 
            String script, int outputType, boolean enabled, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createInventoryLevelReport", ipAddress, sessionId);
            long reportId = bem.createInventoryLevelReport(reportName, reportDescription, script, outputType, enabled, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created inventory level report %s", reportName));
            return reportId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteReport(long reportId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteReport", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = bem.deleteReport(reportId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateReport", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = bem.updateReport(reportId, reportName, reportDescription, enabled, type, script);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateReportParameters(long reportId, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateReportParameters", ipAddress, sessionId);
            ChangeDescriptor changeDescriptor = bem.updateReportParameters(reportId, parameters);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                changeDescriptor);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteReportLight> getClassLevelReports(String className, boolean recursive, 
            boolean includeDisabled, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClassLevelReports", ipAddress, sessionId);
            return bem.getClassLevelReports(className, recursive, includeDisabled);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteReportLight> getInventoryLevelReports(boolean includeDisabled, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getInventoryLevelReports", ipAddress, sessionId);
            return bem.getInventoryLevelReports(includeDisabled);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteReport getReport(long reportId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getReport", ipAddress, sessionId);
            return bem.getReport(reportId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeClassLevelReport", ipAddress, sessionId);
            return bem.executeClassLevelReport(objectClassName, objectId, reportId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeInventoryLevelReport", ipAddress, sessionId);
            return bem.executeInventoryLevelReport(reportId, parameters);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    //</editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Commercial modules data methods">
        // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    @Override
    public long createSDHTransportLink(String classNameEndpointA, long idEndpointA, String classNameEndpointB, long idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHTransportLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            long SDHTransportLinkId = sdhModule.createSDHTransportLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Transport Link %s [%s]", defaultName, linkType));
            return SDHTransportLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createSDHContainerLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<SDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHContainerLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            long SDHContainerLinkId = sdhModule.createSDHContainerLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Container Link %s [%s]", defaultName, linkType));
            return SDHContainerLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSDHTributaryLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<SDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            long SDHTributaryLinkId = sdhModule.createSDHTributaryLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Tributary Link %s [%s]", defaultName, linkType));
            return SDHTributaryLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHTransportLink(String transportLinkClass, long transportLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("deleteSDHTransportLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            String transportLinkName = bem.getObject(transportLinkClass, transportLinkId).getName();
            sdhModule.deleteSDHTransportLink(transportLinkClass, transportLinkId, forceDelete);
                                    
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Transport Link %s [%s]", transportLinkName, transportLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHContainerLink(String containerLinkClass, long containerLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("deleteSDHContainerLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            String containerLinkName = bem.getObject(containerLinkClass, containerLinkId).getName();
            sdhModule.deleteSDHContainerLink(containerLinkClass, containerLinkId, forceDelete);
                        
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Container Link %s [%s]", containerLinkName, containerLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHTributaryLink(String tributaryLinkClass, long tributaryLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("deleteSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            String tributaryLinkName = bem.getObject(tributaryLinkClass, tributaryLinkId).getName();
            sdhModule.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, forceDelete);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Tributary Link %s [%s]", tributaryLinkName, tributaryLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("findSDHRoutesUsingTransportLinks", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.findSDHRoutesUsingTransportLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("findSDHRoutesUsingContainerLinks", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.findSDHRoutesUsingContainerLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<SDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, long transportLinkId, String ipAddress, String sessionId) 
            throws ServerSideException {
        try {
            aem.validateWebServiceCall("getSDHTransportLinkStructure", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.getSDHTransportLinkStructure(transportLinkClass, transportLinkId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<SDHContainerLinkDefinition> getSDHContainerLinkStructure(String transportLinkClass, long transportLinkId, String ipAddress, String sessionId) 
            throws ServerSideException {
        try {
            aem.validateWebServiceCall("getSDHContainerLinkStructure", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.getSDHContainerLinkStructure(transportLinkClass, transportLinkId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>    
        // <editor-fold defaultstate="collapsed" desc="IP Administration Manager Module">
    @Override
    public RemoteObject getSubnet(long id, String className, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnet", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return new RemoteObject(ipamModule.getSubnet(className, id));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemotePool getSubnetPool(long id, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnetPool", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return ipamModule.getSubnetPool(id);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemotePool[] getSubnetPools(int limit, long parentId, String className, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnetPools", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemotePool.toRemotePoolArray(ipamModule.getSubnetPools(limit, parentId, className));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnets(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnets", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnets(limit, poolId));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createSubnetPool(long parentId, String subnetPoolName, 
            String subnetPoolDescription, String className, String ipAddress, 
            String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createSubnetPool", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            long subnetPoolId = ipamModule.createSubnetsPool(parentId, subnetPoolName, subnetPoolDescription, className);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created Subnet Pool %s [%s]", subnetPoolName, className));
            
            return subnetPoolId;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createSubnet(long id, String className, String attributeNames[], 
            String attributeValues[], String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createSubnet", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            long subnetId = ipamModule.createSubnet(id, className, attributeNames, attributeValues);
            
            String subnameName = bem.getObjectLight(className, subnetId).getName();
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created subnet %s", subnameName));
            return subnetId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSubnets(String className, List<Long> ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("deleteSubnets", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.deleteSubnets(className, ids, releaseRelationships);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted %s subnets", ids.size()));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSubnetPools(long[] ids, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("deleteSubnetPools", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.deleteSubnetPools(ids);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted %s subnet pools", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long addIP(long id, String parentClassName, String attributeNames[], String attributeValues[], 
            String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("addIP", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
            
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            long ipAddressId = ipamModule.addIP(id, parentClassName, attributes);
                        
            String ipAddressName = bem.getObjectLight(Constants.CLASS_IP_ADDRESS, ipAddressId).getName();
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created IP Address %s", ipAddressName));
            return ipAddressId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void removeIP(long[] ids, boolean releaseRelationships, String ipAddress, 
            String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("removeIP", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N            
            ipamModule.removeIP(ids, releaseRelationships);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Removed %s IP Addresses", ids.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relateIPtoPort(long ipId, String portClassName, long portId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateIPtoDevice", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateIPtoPort(ipId, portClassName, portId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), portClassName, portId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, "", Long.toString(ipId), "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relateSubnetToVlan(long id, String className, long vlanId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateSubnetToVLAN", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateSubnetToVLAN(id, className, vlanId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, "", Long.toString(id), "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relateSubnetToVrf(long id, String className, long vrfId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateSubnetToVRF", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateSubnetToVRF(id, className, vrfId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, "", Long.toString(id), "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releasePortFromIP(String deviceClassName, long deviceId, long id, String ipAddress, String sessionId) throws ServerSideException{
    try{
            aem.validateWebServiceCall("releasePortFromIP", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releasePortFromIP(deviceClassName, deviceId, id);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), deviceClassName, deviceId,
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, Long.toString(id), "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSubnetFromVlan(long vlanId, long id, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("releaseSubnetFromVLAN", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releaseSubnetFromVLAN(vlanId, id);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, Long.toString(id), "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSubnetFromVRF(long subnetId, long vrfId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("releaseSubnetFromVRF", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releaseSubnetFromVRF(subnetId, vrfId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, Long.toString(subnetId), "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnetUsedIps(long id, String className, int limit, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("getSubnetUsedIps", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetUsedIps(id, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnetsInSubent(long id, String className, int limit, String ipAddress, String sessionId)  throws ServerSideException{
        try{
            aem.validateWebServiceCall("getSubnetsInSubent", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetsInSubent(id, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relatePortToInterface(long portId, String portClassName, String interfaceClassName, long interfaceId, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("relatePortToInterface", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relatePortToInterface(portId, portClassName, interfaceClassName, interfaceId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, "", Long.toString(portId), "");            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releasePortFromInterface(String interfaceClassName, long interfaceId ,long portId, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("releasePortFromInterface", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releasePortFromInterface(interfaceClassName, interfaceId, portId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, Long.toString(portId), "", ""); 
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }    
    @Override
    public boolean itOverlaps(String networkIp, String broadcastIp, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("itOverlaps", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.itOverlaps(networkIp, broadcastIp);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
        return true;
    }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Contract Manager">
    @Override
    public void associateObjectsToContract(String[] objectClass, long[] objectId, String contractClass, long contractId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateWebServiceCall("associateObjectsToContract", ipAddress, sessionId);
            if (!mem.isSubClass(Constants.CLASS_GENERICCONTRACT, contractClass))
                throw new ServerSideException(String.format("Class %s is not a contract", contractClass));
            
            boolean allEquipmentANetworkElement = true;
            
            for (int i = 0; i < objectId.length; i++) {
                if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, objectClass[i]))
                    allEquipmentANetworkElement = false;
                else
                    bem.createSpecialRelationship(objectClass[i], objectId[i], contractClass, contractId, "contractHas", true); //NOI18N
            }
            String contractName = bem.getObjectLight(contractClass, contractId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Associated %s objects to contract %s [%s]", objectId.length, contractName, contractClass));
            
            if (!allEquipmentANetworkElement)
                throw new InvalidArgumentException("All non-inventory elements were ignored");
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromContract(String objectClass, long objectId, long contractId,
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseObjectFromContract", ipAddress, sessionId);
            bem.releaseSpecialRelationship(objectClass, objectId, contractId, "contractHas"); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "contractHas", Long.toString(objectId), "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    // </editor-fold>    
        // <editor-fold defaultstate="collapsed" desc="MPLS Module">
    @Override
    public long createMPLSLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createMPLSLink", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            long MPLSLinkId = mplsModule.createMPLSLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created MPLS Link %s [%s]", defaultName, linkType));
            return MPLSLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteMPLSLink(String linkClass, long linkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("deleteMPLSLink", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            String linkName = bem.getObjectLight(linkClass, linkId).getName();
            mplsModule.deleteMPLSLink(linkClass, linkId, forceDelete);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Deleted MPLS Link %s [%s]", linkName, linkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>    
        // <editor-fold defaultstate="collapsed" desc="Project Manager">
    @Override
    public List<RemotePool> getProjectPools(String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getProjectPools", ipAddress, sessionId);
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            return projectsModule.getProjectPools();
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long addProject(long parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("addProject", ipAddress, sessionId);
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            
            long projectId = projectsModule.addProject(parentId, parentClassName, className, attributeNames, attributeValues);
            String projectName = bem.getObjectLight(className, projectId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created Project %s [%s]", projectName, className));
            
            return projectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteProject(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            String projectName = bem.getObjectLight(className, oid).getName();
            aem.validateWebServiceCall("deleteProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            projectsModule.deleteProject(className, oid, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted Project %s [%s]", projectName, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long addActivity(long parentId, String parentClassName, String className, String attributeNames[], String attributeValues[], String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("addActivity", ipAddress, sessionId);
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            
            long activityId = projectsModule.addActivity(parentId, parentClassName, className, attributeNames, attributeValues);
            String activityName = bem.getObjectLight(className, activityId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created Activity %s [%s]", activityName, className));
            
            return activityId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteActivity(String className, long oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteActivity", ipAddress, sessionId);
            String activityName = bem.getObjectLight(className, oid).getName();
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            projectsModule.deleteActivity(className, oid, releaseRelationships);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted Activity %s [%s]", activityName, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectsInProjectPool(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProjectsInProjectPool", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectsInProjectPool(poolId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectResurces(String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProjectResurces", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectResurces(projectClass, projectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectActivities(String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProjectActivities", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectActivities(projectClass, projectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void associateObjectsToProject(String projectClass, long projectId, String[] objectClass, long[] objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("associateObjectsToProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            projectsModule.associateObjectsToProject(projectClass, projectId, objectClass, objectId);               
            
            String projectName = bem.getObjectLight(projectClass, projectId).getClassName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Associated %s objects to project %s [%s]", objectId.length, projectName, projectClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void associateObjectToProject(String projectClass, long projectId, String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("associateObjectToProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            projectsModule.associateObjectToProject(projectClass, projectId, objectClass, objectId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                ProjectsModule.RELATIONSHIP_PROJECTSPROJECTUSES, "", Long.toString(projectId), "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromProject(String objectClass, long objectId, String projectClass, long projectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseObjectFromProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            projectsModule.releaseObjectFromProject(objectClass, objectId, projectClass, projectId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId,
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                ProjectsModule.RELATIONSHIP_PROJECTSPROJECTUSES, Long.toString(projectId), "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProjectsAssociateToObject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getProjectsAssociateToObject(objectClass, objectId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createProjectPool(String name, String description, String instanceOfClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createProjectPool", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            long projectId = projectsModule.createProjectPool(name, description, instanceOfClass);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Create Project Pool with id %s", projectId));
            return projectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>    
        //<editor-fold desc="Synchronization API" defaultstate="collapsed">
    @Override
    public long createSynchronizationDataSourceConfig(long syngGroupId, String name, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createSynchronizationDataSourceConfig", ipAddress, sessionId);
            //TODO: audit entry
            return aem.createSyncDataSourceConfig(syngGroupId, name, parameters);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSynchronizationGroup(String name,String syncProviderId, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createSynchronizationGroup", ipAddress, sessionId);
            //TODO: audit entry
            return aem.createSyncGroup(name, syncProviderId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateSynchronizationGroup(long syncGroupId, List<StringPair> syncGroupProperties, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateSyncDataSourceConfiguration", ipAddress, sessionId);
            aem.updateSyncGroup(syncGroupId, syncGroupProperties);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateSyncDataSourceConfiguration(long syncDataSourceConfigId, List<StringPair> parameters, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateSyncDataSourceConfiguration", ipAddress, sessionId);
            aem.updateSyncDataSourceConfig(syncDataSourceConfigId, parameters);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSynchronizationGroup> getSynchronizationGroups(String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSynchronizationGroups", ipAddress, sessionId);

            List<SynchronizationGroup> syncGroups = aem.getSyncGroups();
            return RemoteSynchronizationGroup.toArray(syncGroups);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteSynchronizationGroup getSynchronizationGroup(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSynchronizationGroup", ipAddress, sessionId);
            return new RemoteSynchronizationGroup(aem.getSyncGroup(syncGroupId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteSynchronizationConfiguration> getSyncDataSourceConfigurations(long syncGroupId, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSyncDataSourceConfigurations", ipAddress, sessionId);

            List<RemoteSynchronizationConfiguration> RemoteSynchronizationConfigurations = new ArrayList<>();

            List<SyncDataSourceConfiguration> syncDataSourceConfigurations = aem.getSyncDataSourceConfigurations(syncGroupId);

            for (SyncDataSourceConfiguration syncDataSourceConfiguration : syncDataSourceConfigurations) {
                List<StringPair> params = new ArrayList<>();
                for(String key : syncDataSourceConfiguration.getParameters().keySet())
                    params.add(new StringPair(key, syncDataSourceConfiguration.getParameters().get(key)));

                RemoteSynchronizationConfigurations.add(new RemoteSynchronizationConfiguration(
                        syncDataSourceConfiguration.getId(), syncDataSourceConfiguration.getName(), params));
            }

            return RemoteSynchronizationConfigurations;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSynchronizationGroup(long syncGroupId, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteSynchronizationGroup", ipAddress, sessionId);
            aem.deleteSynchronizationGroup(syncGroupId);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteSynchronizationDataSourceConfig", ipAddress, sessionId);
            aem.deleteSynchronizationDataSourceConfig(syncDataSourceConfigId);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public BackgroundJob launchAutomatedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("launchAutomatedSynchronizationTask", ipAddress, sessionId);
            return null; //To be implemented

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public BackgroundJob launchSupervisedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("launchSupervisedSynchronizationTask", ipAddress, sessionId);
            Properties parameters = new Properties();
            parameters.put("syncGroupId", Long.toString(syncGroupId)); //NOI18N                   
            
            BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            JobManager.getInstance().launch(backgroundJob);
            return backgroundJob;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }        
    
    @Override
    public List<SyncResult> executeSyncActions(List<SyncFinding> findings, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeSyncActions", ipAddress, sessionId);
            SyncAction syncActions = new SyncAction(findings);
            return  syncActions.execute();
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteSynchronizationGroup> copySyncGroup(long[] syncGroupIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copySyncGroup", ipAddress, sessionId);
            
            List<SynchronizationGroup> syncGroups = aem.copySyncGroup(syncGroupIds);
            return RemoteSynchronizationGroup.toArray(syncGroups);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteSynchronizationConfiguration> copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copySyncDataSourceConfiguration", ipAddress, sessionId);
            List<RemoteSynchronizationConfiguration> RemoteSynchronizationConfigurations = new ArrayList<>();

            List<SyncDataSourceConfiguration> syncDataSourceConfigurations = aem.copySyncDataSourceConfiguration(syncGroupId, syncDataSourceConfigurationIds);

            for (SyncDataSourceConfiguration syncDataSourceConfiguration : syncDataSourceConfigurations) {
                List<StringPair> params = new ArrayList<>();
                for(String key : syncDataSourceConfiguration.getParameters().keySet())
                    params.add(new StringPair(key, syncDataSourceConfiguration.getParameters().get(key)));

                RemoteSynchronizationConfigurations.add(new RemoteSynchronizationConfiguration(
                        syncDataSourceConfiguration.getId(), syncDataSourceConfiguration.getName(), params));
            }
            return RemoteSynchronizationConfigurations;
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void moveSyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("moveSyncDataSourceConfiguration", ipAddress, sessionId);
            aem.moveSyncDataSourceConfiguration(syncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBackgroundJob> getCurrentJobs(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getCurrentJobs", ipAddress, sessionId);
        
            List<RemoteBackgroundJob> result = new ArrayList();
            for (BackgroundJob job : JobManager.getInstance().getCurrentJobs()) {
                
                if (job.getStatus().equals(BackgroundJob.JOB_STATUS.RUNNNING)) {
                    result.add(new RemoteBackgroundJob(
                        job.getId(), job.getJobTag(), job.getProgress(), 
                        job.allowConcurrence(), job.getStatus().toString(), 
                        job.getStartTime(), job.getEndTime()));
                }
            }
            return result;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void killJob(long jobId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getCurrentJobs", ipAddress, sessionId);
        
            JobManager.getInstance().kill(jobId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        //</editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Fault Management Integration">

    @Override
    public AssetLevelCorrelatedInformation getAffectedServices(int resourceType, String resourceDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAffectedServices", ipAddress, sessionId);
            String[] resourceDefinitionTokens = resourceDefinition.split(";");
            
            if (resourceType == 1) { //Hardware
            
                switch (resourceDefinitionTokens.length) {
                    case 1: //A whole network element
                        return SimpleCorrelation.servicesInDevice(resourceDefinitionTokens[0], bem);
                    case 2:
                        return SimpleCorrelation.servicesInSlotOrBoard(resourceDefinitionTokens[0], resourceDefinitionTokens[1], bem, mem);
                    case 3:
                        
                        List<RemoteBusinessObjectLight> matchedCommunicationsElements = bem.getObjectsWithFilterLight("GenericCommunicationsElement", "name", resourceDefinitionTokens[0]);
                        
                        if (matchedCommunicationsElements.isEmpty())
                            throw new ServerSideException(String.format("No resource with name %s could be found", resourceDefinitionTokens[0]));
                        
                        if (matchedCommunicationsElements.size() > 1)
                            throw new ServerSideException(String.format("More than one communications equipment with name %s was found", resourceDefinitionTokens[0]));
                        
                        List<RemoteBusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.get(0).getClassName(), 
                                                                            matchedCommunicationsElements.get(0).getId(), -1);

                        for (RemoteBusinessObjectLight deviceChild : deviceChildren) {
                            if (resourceDefinitionTokens[1].equals(deviceChild.getName())) {
                                List<RemoteBusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
                                for (RemoteBusinessObjectLight portInSlot : portsInSlot) {
                                    if (resourceDefinitionTokens[2].equals(portInSlot.getName())) 
                                        return servicesInPorts(Arrays.asList(bem.getObject(portInSlot.getId())), bem);
                                }
                                throw new ServerSideException(String.format("No port %s was found on device %s", 
                                        resourceDefinitionTokens[2], resourceDefinitionTokens[0]));
                            }
                        }  
                        
                        throw new ServerSideException(String.format("No slot in communications equipment %s with name %s was found", 
                                resourceDefinitionTokens[0], resourceDefinitionTokens[1]));
                    default:
                        throw new ServerSideException("Invalid resource definition");
                }
            }
            
            if (resourceType == 2) { //Logical connection
                List<RemoteBusinessObject> matchedConnections = bem.getObjectsWithFilter("GenericLogicalConnection", "name", resourceDefinitionTokens[0]);
                if (matchedConnections.isEmpty())
                    throw new ServerSideException(String.format("No logical connection with name %s could be found", resourceDefinitionTokens[0]));
                
                List<RemoteObjectLight> rawServices = new ArrayList<>();
                for (RemoteBusinessObjectLight matchedConnection : matchedConnections) {
                    List<RemoteBusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
                            matchedConnection.getId(), "uses");
                    for (RemoteBusinessObjectLight serviceInConnection : servicesInConnection)
                        rawServices.add(new RemoteObjectLight(serviceInConnection));
                }
                
                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
                HashMap<RemoteBusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

                //Now we organize the rawServices by customers
                for (RemoteObjectLight rawService : rawServices) {
                    RemoteBusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getOid(), Constants.CLASS_GENERICCUSTOMER);
                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                        if (!rawCorrelatedInformation.containsKey(customer))
                            rawCorrelatedInformation.put(customer, new ArrayList<RemoteObjectLight>());
                        
                        rawCorrelatedInformation.get(customer).add(rawService);
                    }
                }

                for (RemoteBusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
            }
            
            if (resourceType == 3) { //Same as 2, but use a GenericPhysicalConnection
                List<RemoteBusinessObject> matchedConnections = bem.getObjectsWithFilter("GenericPhysicalConnection", "name", resourceDefinitionTokens[0]);
                if (matchedConnections.isEmpty())
                    throw new ServerSideException(String.format("No physical connection with name %s could be found", resourceDefinitionTokens[0]));
                
                List<RemoteObjectLight> rawServices = new ArrayList<>();
                for (RemoteBusinessObjectLight matchedConnection : matchedConnections) {
                    List<RemoteBusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
                            matchedConnection.getId(), "uses");
                    for (RemoteBusinessObjectLight serviceInConnection : servicesInConnection)
                        rawServices.add(new RemoteObjectLight(serviceInConnection));
                }
                
                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
                HashMap<RemoteBusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

                //Now we organize the rawServices by customers
                for (RemoteObjectLight rawService : rawServices) {
                    RemoteBusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getOid(), Constants.CLASS_GENERICCUSTOMER);
                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                        if (!rawCorrelatedInformation.containsKey(customer))
                            rawCorrelatedInformation.put(customer, new ArrayList<RemoteObjectLight>());
                        
                        rawCorrelatedInformation.get(customer).add(rawService);
                    }
                }

                for (RemoteBusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
            }
            
            throw new ServerSideException("Invalid resource type");
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Helper methods. Click on the + sign on the left to edit the code.">
    protected final void connect() {
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            mem = persistenceService.getMetadataEntityManager();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
        } catch(Exception ex){
            mem = null;
            bem = null;
            aem = null;
        }
    }

    /**
     * Helper class to parse from a transientQuery into a ExtendedQuery
     * @param query
     * @return
     */
    private ExtendedQuery transientQuerytoExtendedQuery(TransientQuery query){
        ExtendedQuery eq;
        List<ExtendedQuery> listeq = new ArrayList<>();

        if(query == null)
            return null;
        else
            eq = new ExtendedQuery(query.getClassName(),
                                query.getLogicalConnector(),
                                query.getAttributeNames(),
                                query.getVisibleAttributeNames(),
                                query.getAttributeValues(),
                                query.getConditions(), listeq, query.getPage(), query.getLimit());


        if(query.getJoins() != null){
            for(TransientQuery join : query.getJoins()){
                    listeq.add(transientQuerytoExtendedQuery(join));
            }
        }
        
        return eq;
    }
    
    /**
     * Finds the user name using the session Id
     * @param sessionId The sessionId
     * @return The username or null of the session could not be found
     */
    public String getUserNameFromSession (String sessionId) {
        Session aSession = aem.getSessions().get(sessionId);
        if (aSession == null)
            return null;
        return aSession.getUser().getUserName();
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Favorites">    
    @Override
    public long createFavoritesFolderForUser(String favoritesFolderName, long userId, String ipAddress, String sessionId) throws ServerSideException {
        
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createFavoritesFolderForUser", ipAddress, sessionId);
            long favoritesFolderId = aem.createFavoritesFolderForUser(favoritesFolderName, userId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Create Favorites Folder %s For User %s", favoritesFolderName, getUserNameFromSession(sessionId)));
            return favoritesFolderId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteFavoritesFolders (long[] favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteFavoritesFolders ", ipAddress, sessionId);
            
            aem.deleteFavoritesFolders (favoritesFolderId, userId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted %s Favorites Folders", favoritesFolderId.length));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForUser(long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getFavoritesFoldersForUser", ipAddress, sessionId);
            
            List<RemoteFavoritesFolder> remoteBookmarks = new ArrayList();
            List<FavoritesFolder> favoritesFolders = aem.getFavoritesFoldersForUser(userId);
            
            for (FavoritesFolder favoritesFolder : favoritesFolders)
                remoteBookmarks.add(new RemoteFavoritesFolder(favoritesFolder));
            
            return remoteBookmarks;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void addObjectsToFavoritesFolder(String[] objectClass, long[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateWebServiceCall("addObjectsToFavoritesFolder", ipAddress, sessionId);
            
            for (int i = 0; i < objectId.length; i += 1)
                aem.addObjectTofavoritesFolder(objectClass[i], objectId[i], favoritesFolderId, userId);
            
            String favoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Added %s objects to favorites folder %s", objectId.length, favoritesFolderName));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void removeObjectsFromFavoritesFolder(String[] objectClass, long[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateWebServiceCall("removeObjectsFromFavoritesFolder", ipAddress, sessionId);
            
            for (int i = 0; i < objectId.length; i += 1)
                aem.removeObjectFromfavoritesFolder(objectClass[i], objectId[i], favoritesFolderId, userId);
            
            String favoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Remove %s objects from favorites folder %s", objectId.length, favoritesFolderName));                        
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getObjectsInFavoritesFolder(long favoritesFolderId, long userId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectsInFavoritesFolder", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getObjectsInFavoritesFolder(favoritesFolderId, userId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId ,String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getFavoritesFoldersForObject", ipAddress, sessionId);
            
            List<RemoteFavoritesFolder> remoteBookmarks = new ArrayList();
            List<FavoritesFolder> favoritesFolders = aem.getFavoritesFoldersForObject(userId, objectClass, objectId);
            
            for (FavoritesFolder favoritesFolder : favoritesFolders)
                remoteBookmarks.add(new RemoteFavoritesFolder(favoritesFolder));
            
            return remoteBookmarks;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());            
        }
    }
    
    @Override
    public RemoteFavoritesFolder getFavoritesFolder(long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getFavoritesFolder", ipAddress, sessionId);
            
            return new RemoteFavoritesFolder(aem.getFavoritesFolder(favoritesFolderId, userId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateFavoritesFolder(long favoritesFolderId, long userId, String favoritesFolderName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateFavoritesFolder", ipAddress, sessionId);
            
            String oldFavoritesFolderName = aem.getFavoritesFolder(favoritesFolderId, userId).getName();
            aem.updateFavoritesFolder(favoritesFolderId, userId, favoritesFolderName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT,
                new ChangeDescriptor(Constants.PROPERTY_NAME, oldFavoritesFolderName, favoritesFolderName, "Updated favorites folder"));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="Business Rules. Click on the + sign on the left to edit the code.">    
    @Override
    public long createBusinessRule(String ruleName, String ruleDescription, int ruleType, int ruleScope, 
            String appliesTo, String ruleVersion, List<String> constraints, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBusinessRule", ipAddress, sessionId);
            long businessRuleId = aem.createBusinessRule(ruleName, ruleDescription, ruleType, ruleScope, appliesTo, ruleVersion, constraints);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created business rule %s", ruleName));
            return businessRuleId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteBusinessRule(long businessRuleId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteBusinessRule", ipAddress, sessionId);
            aem.deleteBusinessRule(businessRuleId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("Deleted business rule %s", businessRuleId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteBusinessRule> getBusinessRules(int type, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getBusinessRules", ipAddress, sessionId);
            List<BusinessRule> businessRules = aem.getBusinessRules(type);
            
            List<RemoteBusinessRule> res = new ArrayList<>();
            
            for (BusinessRule businessRule : businessRules) {
                RemoteBusinessRule remoteBusinessRule = new RemoteBusinessRule(businessRule.getRuleId(), businessRule.getName(), businessRule.getDescription(), 
                        businessRule.getAppliesTo(), businessRule.getType(), businessRule.getScope(), businessRule.getVersion());
                
                for (BusinessRuleConstraint constraint : businessRule.getConstraints())
                    remoteBusinessRule.getConstraints().add(new RemoteBusinessRuleConstraint(constraint.getName(), constraint.getDefinition()));
                
                res.add(remoteBusinessRule);
            }
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>
}
