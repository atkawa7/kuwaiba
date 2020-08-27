/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import com.neotropic.kuwaiba.modules.ipam.IPAMModule;
import com.neotropic.kuwaiba.modules.mpls.MPLSConnectionDefinition;
import com.neotropic.kuwaiba.modules.mpls.MPLSModule;
import com.neotropic.kuwaiba.modules.projects.ProjectsModule;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHModule;
import com.neotropic.kuwaiba.modules.sdh.SDHPosition;
import com.neotropic.kuwaiba.modules.views.ViewModule;
import com.neotropic.kuwaiba.modules.warehouse.WarehouseModule;
import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.scheduling.JobManager;
import static com.neotropic.kuwaiba.sync.connectors.snmp.vlan.CiscoVlansSynchronizer.RELATIONSHIP_PORT_BELONGS_TO_VLAN;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
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
import org.kuwaiba.apis.persistence.application.ConfigurationVariable;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.FileObject;
import org.kuwaiba.apis.persistence.application.FileObjectLight;
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
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObjectLightList;
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
import org.kuwaiba.util.i18n.I18N;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.todeserialize.TransientQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.GroupInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.PrivilegeInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBackgroundJob;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBusinessRule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteBusinessRuleConstraint;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteContact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteFavoritesFolder;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemotePool;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteResultMessage;
import org.kuwaiba.apis.persistence.application.TemplateObjectLight;
import org.kuwaiba.apis.persistence.application.Validator;
import org.kuwaiba.apis.persistence.application.ValidatorDefinition;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.Artifact;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.apis.persistence.application.process.KpiResult;
import org.kuwaiba.apis.persistence.application.process.ProcessDefinition;
import org.kuwaiba.apis.persistence.application.process.ProcessInstance;
import org.kuwaiba.apis.persistence.business.Contact;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConfigurationVariable;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteInventoryProxy;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpi;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpiAction;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpiResult;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationConfiguration;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationGroup;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSynchronizationProvider;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTask;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteTaskResult;
import org.kuwaiba.interfaces.ws.toserialize.application.ResultRecord;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskNotificationDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.TaskScheduleDescriptor;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.UserInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidator;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteValidatorDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.AssetLevelCorrelatedInformation;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteMPLSConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLinkObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectRelatedObjects;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.business.ServiceLevelCorrelatedInformation;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHContainerLinkDefinition;
import org.kuwaiba.interfaces.ws.toserialize.business.modules.sdh.RemoteSDHPosition;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteAttributeMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.openide.util.Exceptions;

/**
 * Session bean to give primary support to the web service calls
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Singleton
public class WebserviceBeanImpl implements WebserviceBean {

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
     * Sync/load data reference
     */
    private final SyncManager sync;
    
    public WebserviceBeanImpl() {
        super();
        sync = new SyncManager();
        connect();
    }

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public long createClass(RemoteClassMetadata classDefinition, String ipAddress, String sessionId) throws ServerSideException{
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
    public RemoteClassMetadata getClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(className);
            return new RemoteClassMetadata(myClass);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteClassMetadata getClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(classId);
         
            return new RemoteClassMetadata(myClass);

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getAllClassesLight(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAllClassesLight", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getAllClassesLight(includeListTypes, false);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));
            
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSubclassesLight", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLight(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));
            
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteClassMetadataLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSubClassesLightNoRecursive", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata)
                cml.add(new RemoteClassMetadataLight(classMetadataLight));

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
            return mem.isSubclassOf(subclassOf, className);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadata> getAllClasses(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAllClasses", ipAddress, sessionId);
            List<RemoteClassMetadata> cml = new ArrayList<>();
            List<ClassMetadata> classMetadataList = mem.getAllClasses(includeListTypes, false);

            for (ClassMetadata classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadata(classMetadata));
            
            return cml;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void createAttribute(String className, RemoteAttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
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
            attributeMetadata.setOrder(attributeDefinition.getOrder());

            mem.createAttribute(className, attributeMetadata, true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                String.format("Created attribute in %s class", className));

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttribute(long classId, RemoteAttributeMetadata attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
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
            attributeMetadata.setOrder(attributeDefinition.getOrder());
            
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
    public void setClassProperties(RemoteClassMetadata newClassDefinition, String ipAddress, String sessionId) throws ServerSideException {
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
    public RemoteAttributeMetadata getAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAttribute", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            RemoteAttributeMetadata atrbInfo = new RemoteAttributeMetadata(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isReadOnly(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.isMultiple(),
                                                       atrbMtdt.getDescription(), 
                                                       atrbMtdt.isNoCopy(), 
                                                       atrbMtdt.getOrder());
            return atrbInfo;
         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteAttributeMetadata getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("setClassProperties", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeId);

            RemoteAttributeMetadata attrInfo = new RemoteAttributeMetadata(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.isReadOnly(),
                                                       atrbMtdt.isUnique(),
                                                       atrbMtdt.isMandatory(),
                                                       atrbMtdt.isMultiple(),
                                                       atrbMtdt.getDescription(), 
                                                       atrbMtdt.isNoCopy(), 
                                                       atrbMtdt.getOrder());
            return attrInfo;

         } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(long classId, RemoteAttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
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
            attrMtdt.setMultiple(newAttributeDefinition.isMultiple());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());
            attrMtdt.setOrder(newAttributeDefinition.getOrder());

            ChangeDescriptor changeDescriptor = mem.setAttributeProperties(classId, attrMtdt);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_METADATA_OBJECT, 
                changeDescriptor);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, RemoteAttributeMetadata newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
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
            attrMtdt.setMultiple(newAttributeDefinition.isMultiple());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());
            attrMtdt.setOrder(newAttributeDefinition.getOrder());

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
    public List<RemoteClassMetadataLight> getPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleChildren", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }

    }
    
    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleSpecialChildren", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildren(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleChildrenNoRecursive", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList)
                cml.add(new RemoteClassMetadataLight(classMetadata));
            
            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteClassMetadataLight> getPossibleSpecialChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPossibleSpecialChildrenNoRecursive", ipAddress, sessionId);
            List<RemoteClassMetadataLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleSpecialChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight classMetadata : classMetadataList) 
                cml.add(new RemoteClassMetadataLight(classMetadata));

            return cml;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        
    @Override
    public List<RemoteClassMetadataLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUpstreamContainmentHierarchy", ipAddress, sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive))
                res.add(new RemoteClassMetadataLight(cil));
            
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteClassMetadataLight> getUpstreamSpecialContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUpstreamSpecialContainmentHierarchy", ipAddress, sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamSpecialContainmentHierarchy(className, recursive))
                res.add(new RemoteClassMetadataLight(cil));
            
            return res;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteClassMetadataLight> getUpstreamClassHierarchy(String className, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getUpstreamClassHierarchy", ipAddress, sessionId);
            List<RemoteClassMetadataLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamClassHierarchy(className, includeSelf))
                res.add(new RemoteClassMetadataLight(cil));
            
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
    public String getAttributeValueAsString(String objectClass, String objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAttributeValueAsString", ipAddress, sessionId);
            return bem.getAttributeValueAsString(objectClass, objectId, attributeName);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public HashMap<String, String> getAttributeValuesAsString(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getAttributeValuesAsString", ipAddress, sessionId);
            return bem.getAttributeValuesAsString(objectClass, objectId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createListTypeItem", ipAddress, sessionId);
            
            String lstTypeItemId = aem.createListTypeItem(className, name, displayName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created list Type Item %s (%s)", name, className));
            
            return lstTypeItemId;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, String oid, boolean realeaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
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
            List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
            List<RemoteObjectLight> res = new ArrayList<>();
            for (BusinessObjectLight listTypeItem : listTypeItems)
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
    
    @Override
    public String createContact(String contactClass, List<StringPair> properties, String customerClassName, String customerId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createContact", ipAddress, sessionId);
            return bem.createContact(contactClass, properties, customerClassName, customerId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateContact(String contactClass, String contactId, List<StringPair> properties, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateContact", ipAddress, sessionId);
            bem.updateContact(contactClass, contactId, properties);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteContact(String contactClass, String contactId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteContact", ipAddress, sessionId);
            bem.deleteContact(contactClass, contactId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteContact getContact(String contactClass, String contactId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getContact", ipAddress, sessionId);
            return new RemoteContact(bem.getContact(contactClass, contactId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteContact> searchForContacts(String searchString, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            List<RemoteContact> res = new ArrayList();
            aem.validateWebServiceCall("searchForContacts", ipAddress, sessionId);
            for (Contact contact : bem.searchForContacts(searchString, maxResults)) 
                res.add(new RemoteContact(contact));

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteContact> getContactsForCustomer(String customerClass, String customerId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            List<RemoteContact> res = new ArrayList();
            aem.validateWebServiceCall("getContactsForCustomer", ipAddress, sessionId);
            for (Contact contact : bem.getContactsForCustomer(customerClass, customerId)) 
                res.add(new RemoteContact(contact));

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteSession createSession(String user, String password, int sessionType, String IPAddress)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            Session newSession = aem.createSession(user, password, sessionType, IPAddress);
            aem.createGeneralActivityLogEntry(user, ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, String.format("Connected from %s", IPAddress));
            return new RemoteSession(newSession.getToken(), newSession.getUser(), sessionType, newSession.getIpAddress());
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
    public List<RemoteObjectLight> getObjectChildren(String oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteObjectLight> getObjectChildren(String className, String oid, int maxResults, String ipAddress, String sessionId)
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
    public List<RemoteObjectLight> getSiblings(String className, String oid, int maxResults, String ipAddress, String sessionId)
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
    public List<RemoteObject> getChildrenOfClass(String parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
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
    public List<RemoteObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
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
    public List<RemoteObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, 
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
    public List<RemoteObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId) 
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
    public RemoteObject getObject(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException{
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
    public RemoteObjectLight getObjectLight(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException{
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
    public List<RemoteObjectLight> getObjectsWithFilterLight (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectsWithFilterLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsWithFilterLight(className, filterName, filterValue));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
        
    }
    
    @Override
    public List<RemoteObject> getObjectsWithFilter (String className, String filterName, String filterValue, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectsWithFilter", ipAddress, sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getObjectsWithFilter(className, filterName, filterValue));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
        
    }

    @Override
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSuggestedObjectsWithFilter", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSuggestedObjectsWithFilter(filter, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSuggestedObjectsWithFilter", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSuggestedObjectsWithFilter(filter, superClass, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getCommonParent", ipAddress, sessionId);
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aOid, bObjectClass, bOid);
            if (commonParent != null && commonParent.getId() != null && !commonParent.getId().equals("-1")) // is not DummyRoot
                return new RemoteObjectLight(commonParent.getClassName(), commonParent.getId(), commonParent.getName());
            else
                return new RemoteObjectLight("", "-1" , "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getParent(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParent", ipAddress, sessionId);
            BusinessObjectLight parent = bem.getParent(objectClass, oid);
            return new RemoteObjectLight(parent.getClassName(), parent.getId(), parent.getName());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getParents(String objectClass, String oid, String ipAddress, String sessionId) throws ServerSideException{
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
    public List<RemoteObjectLight> getSpecialAttribute(String objectClass, String objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException{
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
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, String oid, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSpecialAttributes", ipAddress, sessionId);
            HashMap<String, List<BusinessObjectLight>> relationships = bem.getSpecialAttributes(objectClass, oid);
            RemoteObjectSpecialRelationships res = new RemoteObjectSpecialRelationships(relationships);

            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getParentsUntilFirstOfClass(String objectClassName, 
            String oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getParentsUntilFirstOfClass", ipAddress, sessionId);
            List<RemoteObjectLight> remoteObjects = new ArrayList<>();
            for (BusinessObjectLight remoteObject : bem.getParentsUntilFirstOfClass(objectClassName, oid, objectToMatchClassName))
                remoteObjects.add(new RemoteObjectLight(remoteObject));
            return remoteObjects;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight getFirstParentOfClass(String objectClassName, 
        String oid, String objectToMatchClassName, String ipAddress, String sessionId) throws ServerSideException {
                
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getLastParentUntilFirstOfClass", ipAddress, sessionId);
            BusinessObjectLight firstParentOfClass = bem.getFirstParentOfClass(objectClassName, oid, objectToMatchClassName);
            return firstParentOfClass != null ? new RemoteObjectLight(firstParentOfClass) : null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getParentOfClass(String objectClass, String oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteObjectLight> getObjectSpecialChildren (String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException{
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
    public List<RemoteObject> getObjectsOfClass(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectsOfClass", ipAddress, sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getObjectsOfClass(className, maxResults));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteClassMetadataLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));

        try {
            aem.validateWebServiceCall("getInstanceableListTypes", ipAddress, sessionId);
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
            RemoteClassMetadataLight[] res = new RemoteClassMetadataLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new RemoteClassMetadataLight(instanceableListTypes.get(i));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createObject(String className, String parentClassName, String parentOid, String[] attributeNames,
            String[] attributeValues, String template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateWebServiceCall("createObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);
            
            String newObjectId = bem.createObject(className, parentClassName, parentOid, attributes, template);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newObjectId));
            return newObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSpecialObject(String className, String parentClassName, String parentOid, String[] attributeNames,
            String[] attributeValues, String template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateWebServiceCall("createSpecialObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);

            String newSpecialObjectId = bem.createSpecialObject(className, parentClassName, parentOid, attributes, template);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newSpecialObjectId));
            
            return newSpecialObjectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public boolean canDeleteObject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            return bem.canDeleteObject(className, oid);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteObject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
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
    public void deleteObjects(String[] classNames, String[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (classNames.length != oids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("deleteObjects", ipAddress, sessionId);
            HashMap<String, List<String>> objects = new HashMap<>();
            for (int i = 0; i< classNames.length;i++) {
                List<String> existingObjects = objects.get(classNames[i]);
                if (existingObjects == null){
                    List<String> newIdList = new ArrayList<>();
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
    public void moveObjectsToPool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjectsToPool", ipAddress, sessionId);
            HashMap<String, List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }

            HashMap<String, String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
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
    public void moveObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjects", ipAddress, sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
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
    public void moveSpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveSpecialObjects", ipAddress, sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
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
    public void movePoolItem(String poolId, String poolItemClassName, String poolItemId, String ipAddress, String sessionId) throws ServerSideException {
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
    public String[] copyObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("copyObjects", ipAddress, sessionId);
            HashMap<String,String[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new String[]{objectOids[i]});
            }
            String[] newObjects = bem.copyObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special) object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String[] copySpecialObjects(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("copySpecialObjects", ipAddress, sessionId);
            HashMap<String,String[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new String[]{objectOids[i]});
            }
            String[] newObjects = bem.copySpecialObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to (Special)object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("movePoolItem", ipAddress, sessionId);
            String id = bem.copyPoolItem(poolId, poolItemClassName, poolItemId, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                String.format("%s moved to pool with id %s", poolItemId, poolId));
            return id;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateObject(String className, String oid, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));

        try {
            aem.validateWebServiceCall("updateObject", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (StringPair attributeToBeUpdated : attributesToBeUpdated)
                attributes.put(attributeToBeUpdated.getKey(), attributeToBeUpdated.getValue());

            ChangeDescriptor theChange = bem.updateObject(className, oid, attributes);
            
            if (mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, className))
                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, theChange);
            else
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), className,
                        oid, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, theChange);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteAttributeMetadata> getMandatoryAttributesInClass(String className, String ipAddress, String sessionId)  throws ServerSideException{
         if (mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
                aem.validateWebServiceCall("getMandatoryAttributesInClass", ipAddress, sessionId);
                List<AttributeMetadata> mandatoryObjectAttributes = mem.getMandatoryAttributesInClass(className);
                return RemoteAttributeMetadata.toRemoteAttributeList(mandatoryObjectAttributes);

            } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, int numberOfObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkObjects", ipAddress, sessionId);
            
            String[] newObjects = bem.createBulkObjects(className, parentClassName, parentOid, numberOfObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new objects of class %s", numberOfObjects, className));
            
            return newObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkSpecialObjects", ipAddress, sessionId);
            
            String[] newSpecialObjects = bem.createBulkSpecialObjects(className, parentClassName, parentId, numberOfSpecialObjects, namePattern);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new special objects  of class %s", numberOfSpecialObjects, className));
            
            return newSpecialObjects;

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Physical connections
    @Override
    public void connectMirrorPort(String[] aObjectClass, String[] aObjectId, String[] bObjectClass, String[] bObjectId, String ipAddress, String sessionId) throws ServerSideException {
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
                if (!mem.isSubclassOf("GenericPort", aObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));
            
                if (!mem.isSubclassOf("GenericPort", bObjectClass[i])) //NOI18N
                    throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));

                if (bem.hasSpecialRelationship(aObjectClass[i], aObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(aObjectClass[i], aObjectId[i])));

                if (bem.hasSpecialRelationship(bObjectClass[i], bObjectId[i], "mirror", 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a mirror port", bem.getObjectLight(bObjectClass[i], bObjectId[i])));
                
                bem.createSpecialRelationship(aObjectClass[i], aObjectId[i], bObjectClass[i], bObjectId[i], "mirror", true); //NOI18N
            
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), aObjectClass[i], aObjectId[i], 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    "mirror", "", aObjectId[i] + ", " + bObjectId[i], ""); //NOI18N          
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectMirrorMultiplePort(String aObjectClass, String aObjectId, List<String> bObjectClasses, List<String>  bObjectIds, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        for (String bObjectId : bObjectIds) {
            if (aObjectId.equals(bObjectId))
                throw new ServerSideException("A port can not be mirror to itself");
        }
        try {
            aem.validateWebServiceCall("connectMirrorMultiplePort", ipAddress, sessionId);
            final String MIRROR_MULTIPLE = "mirrorMultiple";
            
            if (!mem.isSubclassOf("GenericPort", aObjectClass))
                throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(aObjectClass, aObjectId)));
                        
            for (int i = 0; i < bObjectClasses.size(); i++) {
                if (!mem.isSubclassOf("GenericPort", aObjectClass)) {
                    throw new ServerSideException(String.format(
                        "Object %s is not a port", 
                        bem.getObjectLight(bObjectClasses.get(i), bObjectIds.get(i))));
                }
                if (bem.hasSpecialRelationship(bObjectClasses.get(i), bObjectIds.get(i), MIRROR_MULTIPLE, 1)) //NOI18N
                    throw new ServerSideException(String.format("Object %s already has a %s port", bem.getObjectLight(bObjectClasses.get(i), bObjectIds.get(i)), MIRROR_MULTIPLE));
                
                bem.createSpecialRelationship(aObjectClass, aObjectId, bObjectClasses.get(i), bObjectIds.get(i), MIRROR_MULTIPLE, true); //NOI18N
            
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), aObjectClass, aObjectId, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    MIRROR_MULTIPLE, "", bObjectClasses.get(i) + ", " + bObjectIds.get(i), ""); //NOI18N      
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseMirrorPort(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseMirrorPort", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClass)) //NOI18N
                throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(objectClass, objectId)));
                        
            BusinessObjectLight theOtherPort = null;
            if (bem.hasSpecialRelationship(objectClass, objectId, "mirror", 1)) //NOI18N
                theOtherPort = bem.getSpecialAttribute(objectClass, objectId, "mirror").get(0); //NOI18N
            
            if (theOtherPort == null)
                throw new ServerSideException(String.format("Object %s no has a mirror port", bem.getObjectLight(objectClass, objectId)));
                
            bem.releaseSpecialRelationship(objectClass, objectId, "-1", "mirror"); //NOI18N   
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), objectClass, objectId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "mirror", theOtherPort.getId(), "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseMirrorMultiplePort(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseMirrorMultiplePort", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClass)) //NOI18N
                throw new ServerSideException(String.format("Object %s is not a port", bem.getObjectLight(objectClass, objectId)));
            final String MIRROR_MULTIPLE = "mirrorMultiple"; //NOI18N
                        
            BusinessObjectLight theOtherPort = null;
            if (bem.hasSpecialRelationship(objectClass, objectId, MIRROR_MULTIPLE, 1))
                theOtherPort = bem.getSpecialAttribute(objectClass, objectId, MIRROR_MULTIPLE).get(0);
            
            if (theOtherPort == null)
                throw new ServerSideException(String.format("Object %s no has a mirror multiple port", bem.getObjectLight(objectClass, objectId)));
                
            bem.releaseSpecialRelationship(objectClass, objectId, "-1", MIRROR_MULTIPLE);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), objectClass, objectId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                MIRROR_MULTIPLE, theOtherPort.getId(), "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createPhysicalConnection(String aObjectClass, String aObjectId,
            String bObjectClass, String bObjectId, String name, String connectionClass, 
            String templateId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        String newConnectionId = null;
        
        try {
            aem.validateWebServiceCall("createPhysicalConnection", ipAddress, sessionId);
            
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericPhysicalConnection", connectionClass)); //NOI18N
            
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            BusinessObjectLight commonParent = bem.getCommonParent(aObjectClass, aObjectId, bObjectClass, bObjectId);
            
            if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                throw new ServerSideException("The objects provided does not have a common parent, or it is the navigation root. The connection can not be created");
            
            boolean isLink = false;
            
            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N
                
                if (!mem.isSubclassOf("GenericPort", aObjectClass) || !mem.isSubclassOf("GenericPort", bObjectClass)) //NOI18N
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
            
            newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
            
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
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, ipAddress, sessionId);

            throw new ServerSideException(e.getMessage());
        }
    }

    @Override
    public String[] createPhysicalConnections(String[] aObjectClass, String[] aObjectId,
            String[] bObjectClass, String[] bObjectId, String name, String connectionClass, 
            String templateId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        String newConnectionId = null;
        
        try {
            aem.validateWebServiceCall("createPhysicalConnection", ipAddress, sessionId);
            
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not subclass of GenericPhysicalConnection", connectionClass)); //NOI18N
            
            if(aObjectClass.length != aObjectId.length && bObjectClass.length != bObjectId.length && 
                    aObjectClass.length != aObjectClass.length && bObjectId.length != bObjectId.length)
                throw new ServerSideException("The number of ports provided does not match. The connections can not be created");
            
            BusinessObjectLight commonParent = null;
            boolean isLink;
            
            HashMap<String, String> attributes = new HashMap<>();
            if (name == null || name.isEmpty())
                throw new ServerSideException("The name of the connection can not be empty");
            //The connection (either link or container, will be created in the closest common parent between the endpoints)
            
            String[] newConnectionIds = new String[aObjectId.length];
            
            for (int i = 0; i < aObjectClass.length; i++) {
                commonParent = bem.getCommonParent(aObjectClass[i], aObjectId[i], bObjectClass[i], bObjectId[i]);
                
                if (commonParent == null || commonParent.getName().equals(Constants.DUMMY_ROOT))
                    throw new ServerSideException("The objects provided does not have a common parent, or it is the navigation root. The connection can not be created");
                
                isLink = false;
            
                //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
                if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass)) { //NOI18N

                    if (!mem.isSubclassOf("GenericPort", aObjectClass[i]) || !mem.isSubclassOf("GenericPort", bObjectClass[i])) //NOI18N
                        throw new ServerSideException("One of the endpoints provided is not a port");

                    if (!bem.getSpecialAttribute(aObjectClass[i], aObjectId[i], "endpointA").isEmpty()) //NOI18N

                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(aObjectClass[i], aObjectId[i])));

                    if (!bem.getSpecialAttribute(bObjectClass[i], bObjectId[i], "endpointB").isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(bObjectClass[i], bObjectId[i])));

                    isLink = true;
                }
                
                attributes.put(Constants.PROPERTY_NAME, name + "_" + i);
          
                newConnectionId = bem.createSpecialObject(connectionClass, commonParent.getClassName(), commonParent.getId(), attributes, templateId);
                newConnectionIds[i] = newConnectionId;
                
                if (isLink) { //Check connector mappings only if it's a link
                   aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, aObjectClass[i], aObjectId[i]);
                   aem.checkRelationshipByAttributeValueBusinessRules(connectionClass, newConnectionId, bObjectClass[i], bObjectId[i]);
                }

                bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass[i], aObjectId[i], "endpointA", true);
                bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass[i], bObjectId[i], "endpointB", true);

                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                       ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("%s [%s] (%s)", name, connectionClass, newConnectionId));
            }//end for
            
            return newConnectionIds;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != null)
                deleteObjects(new String[]{ connectionClass }, new String[]{ newConnectionId }, true, ipAddress, sessionId);

            throw new ServerSideException(e.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getPhysicalConnectionEndpoints(String connectionClass, String connectionId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            String endpointAName = null, endpointBName = null;
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical or logical connection", connectionClass));
            else{
                endpointAName = "endpointA";
                endpointBName = "endpointB";
            }
            List<BusinessObjectLight> endpointA = bem.getSpecialAttribute(connectionClass, connectionId, endpointAName); //NOI18N
            List<BusinessObjectLight> endpointB = bem.getSpecialAttribute(connectionClass, connectionId, endpointBName); //NOI18N
            return new RemoteObjectLight[] {endpointA.isEmpty() ? null : new RemoteObjectLight(endpointA.get(0)), 
                                            endpointB.isEmpty() ? null : new RemoteObjectLight(endpointB.get(0))};

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
   
    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, String[] sideAIds, 
                String[] linksClassNames, String[] linksIds, String[] sideBClassNames, 
                String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("connectPhysicalLinks", ipAddress, sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                if (linksClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, linksClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical link", linksClassNames[i]));
                if (sideAClassNames[i] != null && !mem.isSubclassOf("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubclassOf("GenericPort", sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a port", sideBClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect a port to itself");
                
                String endpointAName = "endpointA", endpointBName = "endpointB";
 
                List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], endpointAName); //NOI18N
                List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], endpointBName); //NOI18N
                
                if (!aEndpointList.isEmpty()){
                    if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (!bEndpointList.isEmpty()){
                    if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at least one of the endpoints");
                }
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointAName).isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], endpointBName).isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideAClassNames[i], sideAIds[i])));
                    
                    if (aEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i], endpointAName, true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint A", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointBName).isEmpty() || //NOI18N
                        !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], endpointAName).isEmpty()) //NOI18N
                        throw new ServerSideException(String.format("The selected endpoint %s is already connected", bem.getObjectLight(sideBClassNames[i], sideBIds[i])));
                    
                    if (bEndpointList.isEmpty()) {
                        aem.checkRelationshipByAttributeValueBusinessRules(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i]);
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i], endpointBName, true); //NOI18N
                    }
                    else
                        throw new ServerSideException(String.format("Link %s already has an endpoint B", bem.getObjectLight(linksClassNames[i], linksIds[i])));
                }
                //Once the link has been connected, we have to check if the parent is consistent with the new endpoints
                //(that is, unless the link is inside a container. In that case, the container is respected as parent)
                //getParent(ipAddress, i, ipAddress, sessionId)
            }
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                String.format("Connected physical links %s-%s-%s", Arrays.toString(sideAIds), Arrays.toString(linksIds), Arrays.toString(sideBIds)));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectPhysicalContainers(String[] sideAClassNames, String[] sideAIds, 
                String[] containersClassNames, String[] containersIds, String[] sideBClassNames, 
                String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("connectPhysicalContainers", ipAddress, sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (containersClassNames[i] != null && !mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONTAINER, containersClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Class %s is not a physical container", containersClassNames[i]));
                if (sideAClassNames[i] != null && mem.isSubclassOf("GenericPort", sideAClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                if (sideBClassNames[i] != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, sideBClassNames[i])) //NOI18N
                    throw new ServerSideException(String.format("Can not connect an instance of %s to a port", containersClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect an object to itself");
                
                if (sideAIds[i] != null && sideAClassNames[i] != null) {
                    List<BusinessObjectLight> aEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointA"); //NOI18N
                    if (aEndpointList.isEmpty())
                        bem.createSpecialRelationship(containersClassNames[i], containersIds[i], sideAClassNames[i], sideAIds[i], "endpointA", true); //NOI18N
                    else
                        throw new ServerSideException(String.format("Container %s already has an endpoint A", bem.getObjectLight(containersClassNames[i], containersIds[i])));
                }
                
                if (sideBIds[i] != null && sideBClassNames[i] != null) {
                    List<BusinessObjectLight> bEndpointList = bem.getSpecialAttribute(containersClassNames[i], containersIds[i], "endpointB"); //NOI18N
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
    public void reconnectPhysicalConnection(String connectionClass, String connectionId, String newASideClass, String newASideId, String newBSideClass, String newBSideId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("reconnectPhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            boolean isLink = mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, connectionClass);
            
            if (newASideClass != null && newASideId != null && !newASideId.equals("-1")) { //Reconnect the A side
                disconnectPhysicalConnection(connectionClass, connectionId, 1 /*Disconnect A side*/, ipAddress, sessionId);
                if (isLink)
                    connectPhysicalLinks(new String[] { newASideClass }, new String[] { newASideId }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { null }, 
                            new String[] { null }, ipAddress, sessionId);
                else
                    connectPhysicalContainers(new String[] { newASideClass }, new String[] { newASideId }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { null }, 
                            new String[] { null }, ipAddress, sessionId);
            }
            
            if (newBSideClass != null && newBSideId != null && !newBSideId.equals("-1")) { //Reconnect the B side
                disconnectPhysicalConnection(connectionClass, connectionId, 2 /*Disconnect B side*/, ipAddress, sessionId);
                if (isLink)
                    connectPhysicalLinks(new String[] { null }, new String[] { null }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { newBSideClass }, 
                            new String[] { newBSideId }, ipAddress, sessionId);
                else
                    connectPhysicalContainers(new String[] { null }, new String[] { null }, new String[] { connectionClass }, 
                            new String[] { connectionId }, new String[] { newBSideClass }, 
                            new String[] { newBSideId }, ipAddress, sessionId);
            }
            
            //getCommonParent(connectionClass, newASideId, connectionClass, newBSideId, ipAddress, sessionId)
            //moveSpecialObjects(newASideClass, connectionId, objectClasses, objectOids, ipAddress, sessionId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void disconnectPhysicalConnection(String connectionClass, String connectionId, 
            int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("disconnectPhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, connectionClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            String  affectedProperties = "", oldValues = "";
            switch (sideToDisconnect) {
                case 1: //A side
                    BusinessObjectLight endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";
                    break;
                case 2: //B side
                    BusinessObjectLight endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N                    
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointB")); //NOI18N
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                case 3: //Both sides
                    endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA").get(0); //NOI18N
                    endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB").get(0); //NOI18N
                    bem.releaseRelationships(connectionClass, connectionId, Arrays.asList("endpointA", "endpointB")); //NOI18N
                    
                    affectedProperties += "endpointA" + " "; //NOI18N
                    oldValues += endpointA.getId() + " ";
                    
                    affectedProperties += "endpointB" + " "; //NOI18N
                    oldValues += endpointB.getId() + " ";
                    break;
                default:
                    throw new ServerSideException(String.format("Wrong side to disconnect option"));
            }
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), connectionClass, connectionId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                affectedProperties, oldValues, "", ""); //NOI18N
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getPhysicalPath(String objectClassName, String oid, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPhysicalPath", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClassName))
                throw new ServerSideException(String.format("Class %s is not a port", objectClassName));
            
            List<BusinessObjectLight> thePath = bem.getPhysicalPath(objectClassName, oid); 
            return RemoteObjectLight.toRemoteObjectLightArray(thePath);

        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectRelatedObjects getPhysicalTree(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPhysicalTree", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericPort", objectClass))
                throw new ServerSideException(String.format("Class %s is not a port", objectClass));
            return new RemoteObjectRelatedObjects(bem.getPhysicalTree(objectClass, objectId));            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Deprecated
    @Override
    public RemoteLogicalConnectionDetails getLogicalLinkDetails(String linkClass, 
            String linkId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getLogicalLinkDetails", ipAddress, sessionId); //NOI18N
            
            BusinessObject linkObject = bem.getObject(linkClass, linkId);
            
            BusinessObjectLight endpointA = null;
            BusinessObjectLight endpointB = null;
            List<BusinessObjectLight> physicalPathA = null, physicalPathB = null;
            String endpointARelationshipName, endpointBRelationshipName;
            
            if (mem.isSubclassOf("GenericSDHTributaryLink", linkClass)) { //NOI18N
                endpointARelationshipName = "sdhTTLEndpointA"; //NOI18N
                endpointBRelationshipName = "sdhTTLEndpointB"; //NOI18N
            } else {
                if ("MPLSLink".equals(linkClass)) { //NOI18N
                    endpointARelationshipName = "mplsEndpointA"; //NOI18N
                    endpointBRelationshipName = "mplsEndpointB"; //NOI18N
                }
                else
                    throw new ServerSideException(String.format("Class %s is not a supported logical link", linkClass)); 
            }
            
            List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName);
            if (!endpointARelationship.isEmpty()) {
                endpointA = endpointARelationship.get(0);
                physicalPathA = bem.getPhysicalPath(endpointA.getClassName(), endpointA.getId());
            }
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointA = new HashMap<>();    
            if(physicalPathA != null && !physicalPathA.isEmpty())
                physicalPathForVlansEndpointA = getPhysicalPathVlans(physicalPathA.get(physicalPathA.size() -1));
            
            else if(endpointA != null)
                physicalPathForVlansEndpointA = getPhysicalPathVlans(endpointA);
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForBDisEndpointA = new HashMap<>();    
//            if(physicalPathA != null && !physicalPathA.isEmpty())
//                physicalPathForBDisEndpointA = getEndPointContinuity("networkBridgeInterface", physicalPathA.get(physicalPathA.size() -1));
//            
//            else if(endpointA != null)
//                physicalPathForBDisEndpointA = getEndPointContinuity("networkBridgeInterface", endpointA);

            List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName);
            if (!endpointBRelationship.isEmpty()) {
                endpointB = endpointBRelationship.get(0);
                physicalPathB = bem.getPhysicalPath(endpointB.getClassName(), endpointB.getId());
            }
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointB = new HashMap<>();    
            if(physicalPathB != null && !physicalPathB.isEmpty())
                physicalPathForVlansEndpointB = getPhysicalPathVlans(physicalPathB.get(physicalPathB.size() -1));
            
            else if(endpointB != null)
                physicalPathForVlansEndpointB = getPhysicalPathVlans(endpointB);
            
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForBDisEndpointB = new HashMap<>();    
//            if(physicalPathB != null && !physicalPathB.isEmpty())
//                physicalPathForBDisEndpointB = getEndPointContinuity("networkBridgeInterface", physicalPathB.get(physicalPathB.size() -1));
//            
//            else if(endpointB != null)
//                physicalPathForBDisEndpointB =  getEndPointContinuity("networkBridgeInterface", endpointB);

            return new RemoteLogicalConnectionDetails(linkObject, endpointA, endpointB, 
                    physicalPathA == null ? new ArrayList<>() : physicalPathA, 
                    physicalPathB == null ? new ArrayList<>() : physicalPathB,
                    physicalPathForVlansEndpointA, physicalPathForBDisEndpointA, 
                    physicalPathForVlansEndpointB, physicalPathForBDisEndpointB);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Checks the continuity throw ports that belongs to the same VLAN
     * @param endpoint a given port to check if belong to a vlan
     * @return a map with key: port, value: physical path of that port
     * @throws ServerSideException 
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getPhysicalPathVlans(BusinessObjectLight endpoint) 
            throws ServerSideException{
        try {
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> vlansPhysicalPath = new HashMap<>();
            if(endpoint != null){
                //we get the the vlans to which the port belongs
                List<BusinessObjectLight> vlans = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), "portBelongsToVlan");
                for (BusinessObjectLight vlan : vlans) { //We get all the port of every vlan
                    List<BusinessObjectLight> vlanPorts = bem.getSpecialAttribute(vlan.getClassName(), vlan.getId(), "portBelongsToVlan");
                    for (BusinessObjectLight vlanPort : vlanPorts) {
                        if(vlanPort.getId() != null && endpoint.getId() != null && !vlanPort.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 
                            List<BusinessObjectLight> vlanPhysicalPath = bem.getPhysicalPath(vlanPort.getClassName(), vlanPort.getId());
                            if(!vlanPhysicalPath.isEmpty())
                                vlansPhysicalPath.put(vlanPort, vlanPhysicalPath);
                        }
                    }
                }
            }
            return vlansPhysicalPath;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    
    @Override
    public RemoteViewObject validateSavedE2EView(List<String> linkClasses, List<String> linkIds, RemoteViewObject savedView, String ipAddress, String sessionId) throws ServerSideException
    {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("validateSavedE2EView", ipAddress, sessionId); //NOI18N
            ViewModule viewModule = (ViewModule)aem.getCommercialModule("E2E Views Module"); //NOI18N
            ViewObject viewObject = new ViewObject(savedView.getId(), savedView.getName(), savedView.getDescription(), savedView.getViewClassName());
            viewObject.setStructure(savedView.getStructure());
            
            return new RemoteViewObject(viewModule.validateSavedE2EView(linkClasses, linkIds, viewObject));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteViewObject getE2EMap(List<String> linkClasses, 
            List<String> linkIds, boolean includePhyscialLinks,
            boolean includPhyscalPaths, boolean includeLogicalPaths,
            boolean includeVlans, boolean includeBDIs, String ipAddress, String sessionId) 
            throws ServerSideException
    {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getE2EMap", ipAddress, sessionId); //NOI18N
            ViewModule viewModule = (ViewModule)aem.getCommercialModule("E2E Views Module"); //NOI18N
            viewModule.createE2EView(linkClasses, linkIds, includPhyscalPaths, includeVlans, includePhyscialLinks, includeBDIs);
            return new RemoteViewObject(viewModule.getAsXML());
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getContainersBetweenObjects(String objectAClassName, String objectAId,
            String objectBClassName, String objectBId, String containerClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getContainersBetweenObjects", ipAddress, sessionId); //NOI18N
            List<RemoteObjectLight> res = new ArrayList<>();

            HashMap<String, List<BusinessObjectLight>> specialAttributesA = bem.getSpecialAttributes(objectAClassName, objectAId);
            HashMap<String, List<BusinessObjectLight>> specialAttributesB = bem.getSpecialAttributes(objectBClassName, objectBId);
            
            if(!specialAttributesA.isEmpty() && !specialAttributesB.isEmpty()){
                List<BusinessObjectLight> wireContainersListA = new  ArrayList<>();

                if(specialAttributesA.get("endpointA") != null){
                    for(BusinessObjectLight container : specialAttributesA.get("endpointA")){
                        if(container.getClassName().equals(containerClassName))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesA.get("endpointB") != null){
                    for(BusinessObjectLight container : specialAttributesA.get("endpointB")){
                        if(container.getClassName().equals(containerClassName))
                            wireContainersListA.add(container);
                    }
                }

                if(specialAttributesB.get("endpointA") != null){
                    for(BusinessObjectLight container : specialAttributesB.get("endpointA")){
                        if(container.getClassName().equals(containerClassName)){
                            if(wireContainersListA.contains(container))
                                res.add(new RemoteObjectLight(container));
                        }
                    }
                }

                if(specialAttributesB.get("endpointB") != null){
                    for(BusinessObjectLight container : specialAttributesB.get("endpointB")){
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
    public List<RemoteObjectLightList> getPhysicalConnectionsInObject(String objectClass, String objectId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getPhysicalConnectionsInObject", ipAddress, sessionId); //NOI18N
            List<RemoteObjectLightList> res = new ArrayList<>();
            
            List<BusinessObjectLight> allCommunicationsPorts = bem.getChildrenOfClassLightRecursive(objectId, objectClass, "GenericCommunicationsPort", -1);
            
            for (BusinessObjectLight aCommunicationsPort : allCommunicationsPorts) {
                List<BusinessObjectLight> physicalPath = bem.getPhysicalPath(aCommunicationsPort.getClassName(), aCommunicationsPort.getId());
                if (physicalPath.size() > 1)
                    res.add(new RemoteObjectLightList(physicalPath));
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getLogicalConnectionsInObject(String objectClass, String objectId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getLogicalConnectionsInObject", ipAddress, sessionId); //NOI18N
            List<RemoteObjectLight> res = new ArrayList<>();
                        
            List<BusinessObjectLight> allCommunicationsPorts = bem.getChildrenOfClassLightRecursive(objectId, objectClass, "GenericCommunicationsPort", -1);
            
            for (BusinessObjectLight aCommunicationsPort : allCommunicationsPorts) {
                HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(aCommunicationsPort.getClassName(), aCommunicationsPort.getId());
                
                if (specialAttributes.containsKey("sdhTLEndpointA"))
                    res.add(new RemoteObjectLight(specialAttributes.get("sdhTLEndpointA").get(0))); //NOI18N
                
                if (specialAttributes.containsKey("sdhTLEndpointB"))
                    res.add(new RemoteObjectLight(specialAttributes.get("sdhTLEndpointB").get(0))); //NOI18N
                
                 if (specialAttributes.containsKey("sdhTTLEnpointA"))
                    res.add(new RemoteObjectLight(specialAttributes.get("sdhTLEndpointA").get(0))); //NOI18N
                
                if (specialAttributes.containsKey("sdhTTLEnpointB"))
                    res.add(new RemoteObjectLight(specialAttributes.get("sdhTLEndpointB").get(0))); //NOI18N
                
                if (specialAttributes.containsKey("mplsLinkEndpointA"))
                    res.add(new RemoteObjectLight(specialAttributes.get("mplsLinkEndpointA").get(0))); //NOI18N
                
                if (specialAttributes.containsKey("mplsLinkEndpointB"))
                    res.add(new RemoteObjectLight(specialAttributes.get("mplsLinkEndpointB").get(0))); //NOI18N
            }
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    @Override
    public RemoteObject getLinkConnectedToPort(String portClassName, String portId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            aem.validateWebServiceCall("getLinkConnectedToPort", ipAddress, sessionId); //NOI18N
            
            BusinessObject connectedLink = bem.getLinkConnectedToPort(portClassName, portId);
            
            if (connectedLink == null)
                return null;
            else
                return new RemoteObject(connectedLink);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deletePhysicalConnection(String objectClassName, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deletePhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, objectClassName))
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
    public void associateObjectToService(String objectClass, String objectId, String serviceClass, String serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("associateObjectToService", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericService", serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, "uses", true); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", "", objectId, ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    @Override
    public void associateObjectsToService(String[] objectClass, String[] objectId, String serviceClass, String serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            String affectedProperties = "", newValues = "";
            aem.validateWebServiceCall("associateObjectsToService", ipAddress, sessionId);
            if (!mem.isSubclassOf("GenericService", serviceClass)) //NOI18N
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
    public void releaseObjectFromService(String serviceClass, String serviceId, String otherObjectId, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseObjectFromService", ipAddress, sessionId);
            bem.releaseSpecialRelationship(serviceClass, serviceId, otherObjectId, "uses"); //NOI18N
                       
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), serviceClass, serviceId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "uses", otherObjectId, "", "Release object from service"); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getServiceResources(String serviceClass, String serviceId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getServiceResources", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICSERVICE, serviceClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(serviceClass, serviceId, "uses")); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteObjectLight> getServicesForCustomer(String customerClass, String customerId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getServicesForCustomer", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICCUSTOMER, customerClass)) //NOI18N
                throw new ServerSideException(String.format("Class %s is not a customer", customerClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialChildrenOfClassLightRecursive(customerId, customerClass, Constants.CLASS_GENERICSERVICE, limit)); //NOI18N
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
    String firstName, String lastName, int enabled, int type, String email, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("setUserProperties", ipAddress, sessionId);
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, type, email);
            
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
        String lastName, boolean enabled, int type, String email, List<PrivilegeInfo> privileges, 
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
            
            long newUserId = aem.createUser(userName, password, firstName, lastName, enabled, type, email, remotePrivileges, defaultGroupId);
            
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
    public long createListTypeItemRelatedView(String listTypeItemId, String listTypeItemClassName, String viewClassName, 
        String name, String description, byte [] structure, byte [] background, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createListTypeItemRelatedView", ipAddress, sessionId);            
            
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
    public void updateListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, 
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
    public RemoteViewObject getListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItemRelatedView", ipAddress, sessionId);
            ViewObject myView = aem.getListTypeItemRelatedView(listTypeItemId, listTypeItemClass, viewId);
            if (myView == null)
                return null;
            RemoteViewObject res = new RemoteViewObject(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteViewObjectLight[] getListTypeItemRelatedViews(String listTypeItemId, String listTypeItemClass, int limit, 
        String ipAddress, String sessionId) 
        throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItemRelatedViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getListTypeItemRelatedViews(listTypeItemId, listTypeItemClass, limit);
            RemoteViewObjectLight[] res = new RemoteViewObjectLight[views.size()];
            int i = 0;
            for (ViewObjectLight view : views){
                res[i] = new RemoteViewObjectLight(view);
                i++;
            }
            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }   
    }
    
    @Override
    public void deleteListTypeItemRelatedView(String listTypeItemId, String listTypeItemClass, long viewId, 
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
    public List<RemoteObjectLight> getListTypeItemUses(String listTypeItemClass, String listTypeItemId, int limit, 
        String ipAddress, String sessionId) throws ServerSideException {        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getListTypeItemUses", ipAddress, sessionId);
            List<BusinessObjectLight> listTypeItemUses = aem.getListTypeItemUses(listTypeItemClass, listTypeItemId, limit);
            return RemoteObjectLight.toRemoteObjectLightArray(listTypeItemUses);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());            
        }
    }

    @Override   
    public long createObjectRelatedView(String objectId, String objectClass, String name, 
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
    public RemoteViewObject getObjectRelatedView(String oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectRelatedView", ipAddress, sessionId);
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId);
            if (myView == null)
                return null;
            RemoteViewObject res = new RemoteViewObject(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteViewObjectLight> getObjectRelatedViews(String oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectRelatedViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getObjectRelatedViews(oid, objectClass, limit);
            List<RemoteViewObjectLight> res = new ArrayList<>();
            
            for (ViewObjectLight view : views)
                res.add(new RemoteViewObjectLight(view));

            return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }

    @Override
    public RemoteViewObject getGeneralView(long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGeneralView", ipAddress, sessionId);
            ViewObject viewObject = aem.getGeneralView(viewId);
            if(viewObject == null) {
                return null;
            }
            RemoteViewObject viewInfo = new RemoteViewObject(viewObject);
            viewInfo.setBackground(viewObject.getBackground());
            return viewInfo;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }
   
    @Override
    public RemoteViewObjectLight[] getGeneralViews(String viewClassName, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getGeneralViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getGeneralViews(viewClassName, limit);
            RemoteViewObjectLight[] res = new RemoteViewObjectLight[views.size()];
            for (int i = 0; i < views.size(); i++)
                res[i] = new RemoteViewObjectLight(views.get(i));
           return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }
    
    @Override
    public void updateObjectRelatedView(String objectOid, String objectClass, 
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
            
            for (int i=0;resultRecordList.size() >i; i++) {
                RemoteObjectLight rol = new RemoteObjectLight(resultRecordList.get(i).getClassName(), resultRecordList.get(i).getId(), resultRecordList.get(i).getName());
                resultArray[i] = new ResultRecord(rol, (ArrayList<String>) resultRecordList.get(i).getExtraColumns());
            }

            return resultArray;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Pools
    @Override
    public String createRootPool(String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createRootPool", ipAddress, sessionId);
            
            String poolId = aem.createRootPool(name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Root Pool", name));
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createPoolInObject(String parentClassname, String parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolInObject", ipAddress, sessionId);
            String poolId = aem.createPoolInObject(parentClassname, parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s pool in %s object with id %s", name, parentClassname, parentId));            
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createPoolInPool(String parentId, String name, String description, String instancesOfClass, int type, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolInPool", ipAddress, sessionId);
            String poolId = aem.createPoolInPool(parentId, name, description, instancesOfClass, type);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created %s Pool In Pool with id %s ", name, parentId));  
            return poolId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createPoolItem(String poolId, String className, String attributeNames[], String attributeValues[], 
            String templateId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createPoolItem", ipAddress, sessionId);
            String objectId = bem.createPoolItem(poolId, className, attributeNames, attributeValues, templateId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created pool item with id", objectId));
            
            return objectId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
   
    @Override
    public void deletePools(String[] ids, String ipAddress, String sessionId) throws ServerSideException {
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
    public void setPoolProperties(String poolId, String name, String description, String ipAddress, String sessionId) throws ServerSideException {
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
    public RemotePool getPool(String poolId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPool", ipAddress, sessionId);
            return new RemotePool(bem.getPool(poolId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getRootPools(String className, int type, boolean includeSubclasses, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getRootPools", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getRootPools(className, type, includeSubclasses);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getPoolsInObject(String objectClassName, String objectId, String poolClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolsInObject", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getPoolsInObject(objectClassName, objectId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemotePool> getPoolsInPool(String parentPoolId, String poolClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolsInPool", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            List<Pool> rootPools = bem.getPoolsInPool(parentPoolId, poolClass);
            
            for (Pool aPool : rootPools)
                res.add(new RemotePool(aPool));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getPoolItems(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPoolItems", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getPoolItems(poolId, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, String objectId, int limit, String ipAddress, String sessionId) throws ServerSideException {
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
    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("attachFileToObject", ipAddress, sessionId);
            return bem.attachFileToObject(name, tags, file, className, objectId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("detachFileFromObject", ipAddress, sessionId);
            bem.detachFileFromObject(fileObjectId, className, objectId);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteFileObjectLight> getFilesForObject(String className, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            List<RemoteFileObjectLight> res = new ArrayList<>();
            aem.validateWebServiceCall("getFilesForObject", ipAddress, sessionId);
            List<FileObjectLight>filesForObject = bem.getFilesForObject(className, objectId);
            for (FileObjectLight objectFile :filesForObject)
                res.add(new RemoteFileObjectLight(objectFile.getFileOjectId(), objectFile.getName(), objectFile.getCreationDate(), objectFile.getTags()));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteFileObject getFile(long fileObjectId, String className, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getFile", ipAddress, sessionId);
            FileObject  fileForObject = bem.getFile(fileObjectId, className, objectId);
            return new RemoteFileObject(fileForObject.getFileOjectId(), fileForObject.getName(), fileForObject.getCreationDate(), fileForObject.getTags(), fileForObject.getFile());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, 
            String className, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateFileProperties", ipAddress, sessionId);
            bem.updateFileProperties(fileObjectId, properties, className, objectId);
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
        
    @Override
    public long createConfigurationVariable(String configVariablesPoolId, String name, 
            String description, int type, boolean masked, String valueDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createConfigurationVariable", ipAddress, sessionId);
            long res = aem.createConfigurationVariable(configVariablesPoolId, name, description, type, masked, valueDefinition);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Configuration variable %s was created", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateConfigurationVariable(String name, String propertyToUpdate, 
            String newValue, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("updateConfigurationVariable", ipAddress, sessionId);
            aem.updateConfigurationVariable(name, propertyToUpdate, newValue);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Configuration variable %s was was updated: %s -> %s", name, propertyToUpdate, newValue));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteConfigurationVariable", ipAddress, sessionId);
            aem.deleteConfigurationVariable(name);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Configuration variable %s was was deleted", name));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteConfigurationVariable getConfigurationVariable(String name, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));

        try {
            aem.validateWebServiceCall("getConfigurationVariable", ipAddress, sessionId);
            ConfigurationVariable configVariable = aem.getConfigurationVariable(name);
            return new RemoteConfigurationVariable(configVariable.getId(), configVariable.getName(), 
                    configVariable.getDescription(), configVariable.getValueDefinition(), configVariable.isMasked(), configVariable.getType());
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public Object getConfigurationVariableValue(String name, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));

        try {
            aem.validateWebServiceCall("getConfigurationVariable", ipAddress, sessionId);
            return aem.getConfigurationVariableValue(name);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteConfigurationVariable> getConfigurationVariablesInPool(String poolId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
            
        try {
            aem.validateWebServiceCall("getConfigurationVariablesInPool", ipAddress, sessionId);
            List<RemoteConfigurationVariable> res = new ArrayList<>();
            aem.getConfigurationVariablesInPool(poolId).forEach((configVariable) -> {
                res.add(new RemoteConfigurationVariable(configVariable.getId(), configVariable.getName(), 
                    configVariable.getDescription(), configVariable.getValueDefinition(), configVariable.isMasked(), configVariable.getType()));
            });
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getConfigurationVariablesPools(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getConfigurationVariablesPools", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            aem.getConfigurationVariablesPools().forEach((aConfigVariablesPool) -> {
                res.add(new RemotePool(aConfigVariablesPool.getId(), aConfigVariablesPool.getName(), aConfigVariablesPool.getDescription(), 
                        aConfigVariablesPool.getClassName(), aConfigVariablesPool.getType()));
            });
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createConfigurationVariablesPool(String name, String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createConfigurationVariablesPool", ipAddress, sessionId);
            String res = aem.createConfigurationVariablesPool(name, description);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Configuration variables pool %s was created", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateConfigurationVariablesPool(String poolId, String propertyToUpdate, String value, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("updateConfigurationVariablesPool", ipAddress, sessionId);
            aem.updateConfigurationVariablesPool(poolId, propertyToUpdate, value);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Configuration variables pool with id %s was was updated: %s -> %s", poolId, propertyToUpdate, value));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteConfigurationVariablesPool(String poolId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteConfigurationVariablesPool", ipAddress, sessionId);
            aem.deleteConfigurationVariablesPool(poolId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Configuration variables pool with id %s was was deleted", poolId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createProxy(String proxyPoolId, String proxyClass, List<StringPair> attributes, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createProxy", ipAddress, sessionId);
            
            HashMap<String, String> attributesAsHashMap = new HashMap<>();
            attributes.stream().forEach( anAttribute -> attributesAsHashMap.put(anAttribute.getKey(), anAttribute.getValue()) );
            
            String proxyId = aem.createProxy(proxyPoolId, proxyClass, attributesAsHashMap);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Created proxy with id %s", proxyId));
            
            return proxyId;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteProxy(String proxyClass, String proxyId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteProxy", ipAddress, sessionId);
            aem.deleteProxy(proxyClass, proxyId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Proxy with id %s", proxyId));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateProxy(String proxyClass, String proxyId, List<StringPair> attributes, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("updateProxy", ipAddress, sessionId);
            
            HashMap<String, String> attributesAsHashMap = new HashMap<>();
            attributes.stream().forEach( anAttribute -> attributesAsHashMap.put(anAttribute.getKey(), anAttribute.getValue()) );
            
            aem.updateProxy(proxyClass, proxyId, attributesAsHashMap);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Updated proxy with id %s", proxyId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createProxyPool(String name, String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createProxyPool", ipAddress, sessionId);
            String proxyPoolId = aem.createProxyPool(name, description);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Create proxy pool %s with id %s", name, proxyPoolId));
            return proxyPoolId;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateProxyPool(String proxyPoolId, String attributeName, String attributeValue, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("updateProxyPool", ipAddress, sessionId);
            aem.updateProxyPool(proxyPoolId, attributeName, attributeValue);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Updated proxy pool with id %s", proxyPoolId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteProxyPool(String proxyPoolId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteProxyPool", ipAddress, sessionId);
            aem.deleteProxyPool(proxyPoolId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Deleted proxy pool with id %s", proxyPoolId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemotePool> getProxyPools(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProxyPools", ipAddress, sessionId);
            List<RemotePool> res = new ArrayList<>();
            aem.getProxyPools().forEach( aPool -> res.add(new RemotePool(aPool) ));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteInventoryProxy> getProxiesInPool(String proxyPoolId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getProxiesInPool", ipAddress, sessionId);
            List<RemoteInventoryProxy> res = new ArrayList<>();
            aem.getProxiesInPool(proxyPoolId).forEach( aProxy -> res.add(new RemoteInventoryProxy(aProxy)) );
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteInventoryProxy> getAllProxies(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getAllProxies", ipAddress, sessionId);
            List<RemoteInventoryProxy> res = new ArrayList<>();
            aem.getAllProxies().forEach( aProxy -> res.add(new RemoteInventoryProxy(aProxy)) );
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void associateObjectToProxy(String objectClass, String objectId, String proxyClass, String proxyId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("associateObjectToProxy", ipAddress, sessionId);
            aem.associateObjectToProxy(objectClass, objectId, proxyClass, proxyId);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseObjectFromProxy(String objectClass, String objectId, String proxyClass, String proxyId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseObjectFromProxy", ipAddress, sessionId);
            aem.releaseObjectFromProxy(objectClass, objectId, proxyClass, proxyId);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    // </editor-fold>
    //<editor-fold desc="Validators" defaultstate="collapsed">

    @Override
    public long createValidatorDefinition(String name, String description, String classToBeApplied, String script, 
            boolean enabled, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createValidatorDefinition", ipAddress, sessionId);
            long res = aem.createValidatorDefinition(name, description, classToBeApplied, script, enabled);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("Validator definition %s ", name));
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateValidatorDefinition(long validatorDefinitionId, String name, String description, String classToBeApplied, 
            String script, Boolean enabled, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("updateValidatorDefinition", ipAddress, sessionId);
            aem.updateValidatorDefinition(validatorDefinitionId, name, description, classToBeApplied, script, enabled);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, String.format("Validator definition with id %s ", validatorDefinitionId));
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteValidatorDefinition> getValidatorDefinitionsForClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            List<RemoteValidatorDefinition> res = new ArrayList<>();
            aem.validateWebServiceCall("getValidatorDefinitionsForClass", ipAddress, sessionId);
            
            List<ValidatorDefinition> validatorDefinitions = aem.getValidatorDefinitionsForClass(className);
            
            for (ValidatorDefinition validatorDefinition : validatorDefinitions) {
                res.add(new RemoteValidatorDefinition(validatorDefinition.getId(), validatorDefinition.getName(), validatorDefinition.getDescription(), 
                        validatorDefinition.getClassToBeApplied(), validatorDefinition.getScript(), validatorDefinition.isEnabled()));
            }
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteValidator> runValidationsForObject(String objectClass, long objectId, String ipAddress, 
            String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            List<RemoteValidator> res = new ArrayList<>();
            aem.validateWebServiceCall("runValidationsForObject", ipAddress, sessionId);
            
            List<Validator> validators = aem.runValidationsForObject(objectClass, objectId);
            
            for (Validator validator : validators) {
                res.add(new RemoteValidator(validator.getName(), validator.getProperties()));
            }
            
            return res;
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteValidatorDefinition(long validatorDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("deleteValidatorDefinition", ipAddress, sessionId);
            aem.deleteValidatorDefinition(validatorDefinitionId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, String.format("Validator definition with id %s ", validatorDefinitionId));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //</editor-fold>
    
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
    public String createTemplate(String templateClass, String templateName, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplate", ipAddress, sessionId);
            String templateId = aem.createTemplate(templateClass, templateName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created template %s", templateName));
            return templateId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createTemplateElement(String templateElementClass, String templateElementParentClassName, 
            String templateElementParentId, String templateElementName, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplateElement", ipAddress, sessionId);
            String templateElementId = aem.createTemplateElement(templateElementClass, templateElementParentClassName, 
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
    public String createTemplateSpecialElement(String tsElementClass, String tsElementParentClassName, 
        String tsElementParentId, String tsElementName, String ipAddress, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createTemplateElement", ipAddress, sessionId);
            String templateSpecialElementId = aem.createTemplateSpecialElement(tsElementClass, tsElementParentClassName, 
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
    public String[] createBulkTemplateElement(String templateElementClassName, String templateElementParentClassName, 
            String templateElementParentId, int numberOfTemplateElements, String templateElementNamePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkTemplateElement", ipAddress, sessionId);
            String[] ids = aem.createBulkTemplateElement(templateElementClassName, templateElementParentClassName, 
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
    public String[] createBulkSpecialTemplateElement(String stElementClass, String stElementParentClassName, 
            String stElementParentId, int numberOfTemplateElements, String stElementNamePattern, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createBulkSpecialTemplateElement", ipAddress, sessionId);
            String[] ids = aem.createBulkSpecialTemplateElement(stElementClass, stElementParentClassName, 
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
    public void updateTemplateElement(String templateElementClass, String templateElementId, String[] attributeNames, 
            String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
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
    public void deleteTemplateElement(String templateElementClass, String templateElementId, String ipAddress, String sessionId) throws ServerSideException {
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
            List<TemplateObjectLight> templates = aem.getTemplatesForClass(className);
            List<RemoteObjectLight> remoteTemplates = new ArrayList<>();
            
            for (TemplateObjectLight template : templates)
                remoteTemplates.add(new RemoteObjectLight(template.getClassName(), template.getId(), template.getName()));
                        
            return remoteTemplates;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getTemplateElementChildren(String templateElementClass, 
            String templateElementId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplateElementChildren", ipAddress, sessionId);
            List<TemplateObjectLight> templateElementChildren = aem.getTemplateElementChildren(templateElementClass, templateElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (TemplateObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild.getClassName(), 
                    templateElementChild.getId(), templateElementChild.getName()));
            
            return remoteTemplateElementChildren;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getTemplateSpecialElementChildren(String tsElementClass, 
            String tsElementId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getTemplateSpecialElementChildren", ipAddress, sessionId);
            List<TemplateObjectLight> templateElementChildren = aem.getTemplateSpecialElementChildren(tsElementClass, tsElementId);
            List<RemoteObjectLight> remoteTemplateElementChildren = new ArrayList<>();
            
            for (TemplateObjectLight templateElementChild : templateElementChildren)
                remoteTemplateElementChildren.add(new RemoteObjectLight(templateElementChild.getClassName(), 
                    templateElementChild.getId(), templateElementChild.getName()));
            
            return remoteTemplateElementChildren;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getTemplateElement(String templateElementClass, String templateElementId, 
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
    public String[] copyTemplateElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, 
            String newParentClassName, String newParentId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copyTemplateElements", ipAddress, sessionId);
            String [] templateElementsIds = aem.copyTemplateElements(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT,
                String.format("%s template elements copied", templateElementsIds.length));
            return templateElementsIds;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String[] copyTemplateSpecialElements(String[] sourceObjectsClassNames, String[] sourceObjectsIds, 
        String newParentClassName, String newParentId, String ipAddress, String sessionId) throws ServerSideException {
        
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copyTemplateSpecialElements", ipAddress, sessionId);
            String[] templateSpecialElements = aem.copyTemplateSpecialElement(sourceObjectsClassNames, sourceObjectsIds, newParentClassName, newParentId);
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
    public byte[] getDeviceLayoutStructure(String oid, String className, String ipAddress, String sessionId) throws ServerSideException {
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
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId, String ipAddress, String sessionId) throws ServerSideException {
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
    public String createSDHTransportLink(String classNameEndpointA, String idEndpointA, String classNameEndpointB, 
            String idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHTransportLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            String SDHTransportLinkId = sdhModule.createSDHTransportLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Transport Link %s [%s]", defaultName, linkType));
            return SDHTransportLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createSDHContainerLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHContainerLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            List<SDHPosition> remotePositions = new ArrayList<>();
            
            for (RemoteSDHPosition position : positions)
                remotePositions.add(new SDHPosition(position.getLinkClass(), position.getLinkId(), position.getPosition()));
            
            String SDHContainerLinkId = sdhModule.createSDHContainerLink(classNameEndpointA, idEndpointA, 
                    classNameEndpointB, idEndpointB, linkType, remotePositions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Container Link %s [%s]", defaultName, linkType));
            return SDHContainerLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public String createSDHTributaryLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, String linkType, List<RemoteSDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            List<SDHPosition> remotePositions = new ArrayList<>();
            
            for (RemoteSDHPosition position : positions)
                remotePositions.add(new SDHPosition(position.getLinkClass(), position.getLinkId(), position.getPosition()));
            
            String SDHTributaryLinkId = sdhModule.createSDHTributaryLink(classNameEndpointA, idEndpointA, classNameEndpointB, 
                    idEndpointB, linkType, remotePositions, defaultName);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                String.format("Created SDH Tributary Link %s [%s]", defaultName, linkType));
            return SDHTributaryLinkId;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHTransportLink(String transportLinkClass, String transportLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
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
    public void deleteSDHContainerLink(String containerLinkClass, String containerLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
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
    public void deleteSDHTributaryLink(String tributaryLinkClass, String tributaryLinkId, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("deleteSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            
            String tributaryLinkName = bem.getObject(tributaryLinkClass, tributaryLinkId).getName();
            sdhModule.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                String.format("Deleted SDH Tributary Link %s [%s]", tributaryLinkName, tributaryLinkClass));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingTransportLinks(String communicationsEquipmentClassA, 
                                            String communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            String communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("findSDHRoutesUsingTransportLinks", ipAddress, sessionId);
            
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            List<RemoteObjectLightList> res  = new ArrayList<>();
            
            List<BusinessObjectLightList> routes = sdhModule.findSDHRoutesUsingTransportLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
            for (BusinessObjectLightList route : routes)
                res.add(new RemoteObjectLightList(route));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLightList> findSDHRoutesUsingContainerLinks(String communicationsEquipmentClassA, 
        String communicationsEquipmentIdA, String communicationsEquipmentClassB, 
        String communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("findSDHRoutesUsingContainerLinks", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            List<RemoteObjectLightList> res  = new ArrayList<>();
            List<BusinessObjectLightList> routes = sdhModule.findSDHRoutesUsingContainerLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
            for (BusinessObjectLightList route : routes)
                res.add(new RemoteObjectLightList(route));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, String transportLinkId, String ipAddress, String sessionId) 
            throws ServerSideException {
        try {
            aem.validateWebServiceCall("getSDHTransportLinkStructure", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            List<SDHContainerLinkDefinition> containerLinks = sdhModule.getSDHTransportLinkStructure(transportLinkClass, transportLinkId);
            
            List<RemoteSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SDHContainerLinkDefinition containerLink : containerLinks)
                res.add(new RemoteSDHContainerLinkDefinition(containerLink));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteSDHContainerLinkDefinition> getSDHContainerLinkStructure(String transportLinkClass, String transportLinkId, String ipAddress, String sessionId) 
            throws ServerSideException {
        try {
            aem.validateWebServiceCall("getSDHContainerLinkStructure", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            List<SDHContainerLinkDefinition> containerLinks = sdhModule.getSDHContainerLinkStructure(transportLinkClass, transportLinkId);
            List<RemoteSDHContainerLinkDefinition> res = new ArrayList<>();
            
            for (SDHContainerLinkDefinition containerLink : containerLinks)
                res.add(new RemoteSDHContainerLinkDefinition(containerLink));
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>    
        // <editor-fold defaultstate="collapsed" desc="IP Administration Manager Module">
    @Override
    public RemoteObject getSubnet(String id, String className, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnet", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return new RemoteObject(ipamModule.getSubnet(className, id));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemotePool getSubnetPool(String id, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnetPool", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return ipamModule.getSubnetPool(id);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemotePool[] getSubnetPools(String parentId, String className, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnetPools", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemotePool.toRemotePoolArray(ipamModule.getSubnetPools(parentId, className));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnets(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("getSubnets", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnets(limit, poolId));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createSubnetPool(String parentId, String subnetPoolName, 
            String subnetPoolDescription, String className, String ipAddress, 
            String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createSubnetPool", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            String subnetPoolId = ipamModule.createSubnetsPool(parentId, subnetPoolName, subnetPoolDescription, className);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created Subnet Pool %s [%s]", subnetPoolName, className));
            
            return subnetPoolId;            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public String createSubnet(String id, String className, List<StringPair> attributes, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createSubnet", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            
            String[] attributeNames = new String[attributes.size()];
            String[] attributeValues = new String[attributes.size()];
            
            for (int i = 0; i < attributes.size(); i++) {
                attributeNames[i] = attributes.get(i).getKey();
                attributeValues[i] = attributes.get(i).getValue();
            }
                
            String subnetId = ipamModule.createSubnet(id, className, attributeNames, attributeValues);
            
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
    public void deleteSubnets(String className, List<String> ids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
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
    public void deleteSubnetPools(String[] ids, String ipAddress, String sessionId) throws ServerSideException{
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
    public String addIPAddress(String id, String parentClassName, List<StringPair> attributesToBeUpdated, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("addIPAddress", ipAddress, sessionId);
            HashMap<String, String> attributes = new HashMap<>();
            
            for (StringPair attribute : attributesToBeUpdated)
                attributes.put(attribute.getKey(), attribute.getValue());
            
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            String ipAddressId = ipamModule.addIPAddress(id, parentClassName, attributes);
                        
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
    public void removeIP(String[] ids, boolean releaseRelationships, String ipAddress, 
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
    public void relateIPtoPort(String ipId, String portClassName, String portId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateIPtoDevice", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateIPtoPort(ipId, portClassName, portId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), portClassName, portId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, "", ipId, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Deprecated
    @Override
    public void relateSubnetToVlan(String id, String className, String vlanId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateSubnetToVLAN", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateSubnetToVLAN(id, className, vlanId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, "", id, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relateSubnetToVrf(String id, String className, String vrfId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("relateSubnetToVRF", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relateSubnetToVRF(id, className, vrfId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, "", id, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releasePortFromIP(String deviceClassName, String deviceId, String id, String ipAddress, String sessionId) throws ServerSideException{
    try{
            aem.validateWebServiceCall("releasePortFromIP", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releasePortFromIP(deviceClassName, deviceId, id);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), deviceClassName, deviceId,
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMHASADDRESS, id, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Deprecated
    @Override
    public void releaseSubnetFromVlan(String vlanId, String id, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("releaseSubnetFromVLAN", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releaseSubnetFromVLAN(vlanId, id);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN, id, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void releaseSubnetFromVRF(String subnetId, String vrfId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("releaseSubnetFromVRF", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releaseSubnetFromVRF(subnetId, vrfId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VRF_INSTANCE, vrfId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE, subnetId, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnetUsedIps(String id, String className, int limit, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("getSubnetUsedIps", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetUsedIps(id, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getSubnetsInSubnet(String id, String className, int limit, String ipAddress, String sessionId)  throws ServerSideException{
        try{
            aem.validateWebServiceCall("getSubnetsInSubnet", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            return RemoteObjectLight.toRemoteObjectLightArray(ipamModule.getSubnetsInSubnet(id, className));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void relatePortToInterface(String portId, String portClassName, String interfaceClassName, String interfaceId, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("relatePortToInterface", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.relatePortToInterface(portId, portClassName, interfaceClassName, interfaceId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, "", portId, "");            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releasePortFromInterface(String interfaceClassName, String interfaceId, String portId, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("releasePortFromInterface", ipAddress, sessionId);
            IPAMModule ipamModule = (IPAMModule)aem.getCommercialModule("IPAM Module"); //NOI18N
            ipamModule.releasePortFromInterface(interfaceClassName, interfaceId, portId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), interfaceClassName, interfaceId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                IPAMModule.RELATIONSHIP_IPAMPORTRELATEDTOINTERFACE, portId, "", ""); 
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
    public void associateObjectsToContract(String[] objectClass, String[] objectId, String contractClass, String contractId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        if (objectClass.length != objectId.length)
            throw new ServerSideException("The arrays provided have different lengths");
        
        try {
            aem.validateWebServiceCall("associateObjectsToContract", ipAddress, sessionId);
            if (!mem.isSubclassOf(Constants.CLASS_GENERICCONTRACT, contractClass))
                throw new ServerSideException(String.format("Class %s is not a contract", contractClass));
            
            boolean allEquipmentANetworkElement = true;
            
            for (int i = 0; i < objectId.length; i++) {
                if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, objectClass[i]))
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
    public void releaseObjectFromContract(String objectClass, String objectId, String contractId,
            String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseObjectFromContract", ipAddress, sessionId);
            bem.releaseSpecialRelationship(objectClass, objectId, contractId, "contractHas"); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "contractHas", objectId, "", ""); //NOI18N
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    // </editor-fold>    
        // <editor-fold defaultstate="collapsed" desc="MPLS Module">
    
    @Override
    public String createMPLSLink(String classNameEndpointA, String idEndpointA, 
            String classNameEndpointB, String idEndpointB, List<StringPair> attributesToBeSet, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("createMPLSLink", ipAddress, sessionId);
            
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N MPLS Networks Module
            HashMap<String, String> attributes = new HashMap<>();
            
            for (StringPair attribute : attributesToBeSet)
                attributes.put(attribute.getKey(), attribute.getValue());
            
            String MPLSLinkId = mplsModule.createMPLSLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, attributes, getUserNameFromSession(sessionId));
            return MPLSLinkId;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteMPLSConnectionDetails getMPLSLinkEndpoints(String connectionId, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("getMPLSLinkEndpoints", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            MPLSConnectionDefinition mplsLinkEndpoints = mplsModule.getMPLSLinkDetails(connectionId);
            
            RemoteMPLSConnectionDetails remoteMPLSConnectionDetails = new RemoteMPLSConnectionDetails(mplsLinkEndpoints);
            
            return remoteMPLSConnectionDetails;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectMplsLink(String[] sideAClassNames, String[] sideAIds, String[] linksIds, String[] sideBClassNames, String[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("connectMplsLink", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            mplsModule.connectMplsLink(sideAClassNames, sideAIds, linksIds, sideBClassNames, sideBIds, getUserNameFromSession(sessionId));
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void disconnectMPLSLink(String connectionId, int sideToDisconnect, String ipAddress, String sessionId) throws ServerSideException{
        try{
            aem.validateWebServiceCall("disconnectMPLSLink", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            mplsModule.disconnectMPLSLink(connectionId, sideToDisconnect, getUserNameFromSession(sessionId));
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
            
    @Override
    public void deleteMPLSLink(String linkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException{
        try {
            aem.validateWebServiceCall("deleteMPLSLink", ipAddress, sessionId);
            MPLSModule mplsModule = (MPLSModule)aem.getCommercialModule("MPLS Networks Module"); //NOI18N
            mplsModule.deleteMPLSLink(linkId, forceDelete, getUserNameFromSession(sessionId));
            
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
    public String addProject(String parentId, String parentClassName, String className, String[] attributeNames, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("addProject", ipAddress, sessionId);
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            
            String projectId = projectsModule.addProject(parentId, parentClassName, className, attributeNames, attributeValues);
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
    public void deleteProject(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
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
    public String addActivity(String parentId, String parentClassName, String className, String attributeNames[], String attributeValues[], String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("addActivity", ipAddress, sessionId);
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N
            
            String activityId = projectsModule.addActivity(parentId, parentClassName, className, attributeNames, attributeValues);
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
    public void deleteActivity(String className, String oid, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteObjectLight> getProjectsInProjectPool(String poolId, int limit, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteObjectLight> getAllProjects(String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("getAllProjects", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            return RemoteObjectLight.toRemoteObjectLightArray(projectsModule.getAllProjects());
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectResurces(String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteObjectLight> getProjectActivities(String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException {
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
    public void associateObjectsToProject(String projectClass, String projectId, String[] objectClass, String[] objectId, String ipAddress, String sessionId) throws ServerSideException {
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
    public void associateObjectToProject(String projectClass, String projectId, String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("associateObjectToProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            projectsModule.associateObjectToProject(projectClass, projectId, objectClass, objectId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId,
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                ProjectsModule.RELATIONSHIP_PROJECTSPROJECTUSES, "", projectId, "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromProject(String objectClass, String objectId, String projectClass, String projectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("releaseObjectFromProject", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            projectsModule.releaseObjectFromProject(objectClass, objectId, projectClass, projectId);
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                objectClass, objectId,
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                ProjectsModule.RELATIONSHIP_PROJECTSPROJECTUSES, projectId, "", "");
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteObjectLight> getProjectsAssociateToObject(String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
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
    public String createProjectPool(String name, String description, String instanceOfClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            aem.validateWebServiceCall("createProjectPool", ipAddress, sessionId);
            
            ProjectsModule projectsModule = (ProjectsModule) aem.getCommercialModule("Projects Module"); //NOI18N                        
            String projectId = projectsModule.createProjectPool(name, description, instanceOfClass);
            
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
    public List<RemoteSynchronizationProvider> getSynchronizationProviders(String ipAddress, String sessionId)throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSynchronizationProviders", ipAddress, sessionId); //NOI18N
            HashMap<String, RemoteSynchronizationProvider> map = new HashMap();
                        
            List<ConfigurationVariable> configVariables = aem.getConfigurationVariablesWithPrefix("sync.providers.enabled.provider");

            for (ConfigurationVariable configVariable : configVariables) {
                Object configVariableValue = aem.getConfigurationVariableValue(configVariable.getName());
                if (configVariableValue instanceof String) {
                    try {
                        Class providerClass = Class.forName((String) configVariableValue);
                        if (AbstractSyncProvider.class.isAssignableFrom(providerClass)) {
                            AbstractSyncProvider syncProvider = (AbstractSyncProvider)providerClass.newInstance();
                            map.put(configVariable.getName(), new RemoteSynchronizationProvider(syncProvider.getId(), syncProvider.getDisplayName(), syncProvider.isAutomated()));
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        throw new InvalidArgumentException(String.format("The configuration variable %s has an invalid value", configVariable.getName()));
                    }
                }
            }
            List<String> positions = new ArrayList();
            positions.addAll(map.keySet());
            Collections.sort(positions);
            
            List<RemoteSynchronizationProvider> syncProviders = new ArrayList();
            for (String position : positions)
                syncProviders.add(map.get(position));
            
            return syncProviders;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createSynchronizationDataSourceConfig(String objectId, long syngGroupId, String name, List<StringPair> parameters, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createSynchronizationDataSourceConfig", ipAddress, sessionId);
            //TODO: audit entry
            return aem.createSyncDataSourceConfig(objectId, syngGroupId, name, parameters);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSynchronizationGroup(String name, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createSynchronizationGroup", ipAddress, sessionId);
            //TODO: audit entry
            return aem.createSyncGroup(name);
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
    public RemoteSynchronizationConfiguration getSyncDataSourceConfiguration(String objectId, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getSyncDataSourceConfigurations", ipAddress, sessionId);

            SyncDataSourceConfiguration syncDataSourceConfiguration = aem.getSyncDataSourceConfiguration(objectId);
            
            List<StringPair> params = new ArrayList<>();
            for(String key : syncDataSourceConfiguration.getParameters().keySet())
                params.add(new StringPair(key, syncDataSourceConfiguration.getParameters().get(key)));

            return new RemoteSynchronizationConfiguration(
                    syncDataSourceConfiguration.getId(), syncDataSourceConfiguration.getName(), params);

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
    public BackgroundJob launchSupervisedSynchronizationTask(long syncGroupId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("launchSupervisedSynchronizationTask", ipAddress, sessionId);
            Properties parameters = new Properties();
            parameters.put("syncGroupId", Long.toString(syncGroupId)); //NOI18N  
            parameters.put("sessionId", sessionId); //NOI18N  
            
            BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            JobManager.getInstance().launch(backgroundJob);
            return backgroundJob;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }     
    
    @Override
    public BackgroundJob launchAdHocAutomatedSynchronizationTask(long[] syncDataSourceConfigIds, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("launchAdHocAutomatedSynchronizationTask", ipAddress, sessionId);
            Properties parameters = new Properties();
            
            parameters.put("sessionId", sessionId); //NOI18N
            parameters.put("provider", syncProvidersName); //NOI18N  
            String syncDSConfigIds= "";
            for (long syncDataSourceConfigId : syncDataSourceConfigIds) 
                syncDSConfigIds += Long.toString(syncDataSourceConfigId) +";";
            
            parameters.put("dataSourceConfigIds", syncDSConfigIds); //NOI18N  
            
            BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            JobManager.getInstance().launch(backgroundJob);
            return backgroundJob;
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public BackgroundJob launchAdHocAutomatedSynchronizationTask(long syncGroupId, String syncProvidersName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("launchAdHocAutomatedSynchronizationTask", ipAddress, sessionId);
                        
            Properties parameters = new Properties();
            parameters.put("syncGroupId", Long.toString(syncGroupId)); //NOI18N
            parameters.put("sessionId", sessionId); //NOI18N
            parameters.put("provider", syncProvidersName); //NOI18N  
            
            BackgroundJob backgroundJob = new BackgroundJob("DefaultSyncJob", false, parameters); //NOI18N
            JobManager.getInstance().launch(backgroundJob);
            return backgroundJob;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<SyncResult> executeSyncActions(long syncGroupId, List<SyncAction> actions, String ipAddress, String sessionId)throws ServerSideException{
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeSyncActions", ipAddress, sessionId);
            SynchronizationGroup syncGroup = PersistenceService.getInstance().getApplicationEntityManager().getSyncGroup(syncGroupId);
            return null; //syncGroup.getProvider().finalize(actions);
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
            
            //List<SynchronizationGroup> syncGroups = aem.copySyncGroup(syncGroupIds);
            return new ArrayList<>();//RemoteSynchronizationGroup.toArray(syncGroups);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    public void releaseSyncDataSourceConfigToSyncGroup(List<Long> syncDataSourceConfigIds, long syncGroupId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseSyncDataSourceConfigToSyncGroup", ipAddress, sessionId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
   
    @Override
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("copySyncDataSourceConfiguration", ipAddress, sessionId);
            
            aem.copySyncDataSourceConfiguration(syncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void moveSyncDataSourceConfiguration(long oldSyncGroupId, long newSyncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("moveSyncDataSourceConfiguration", ipAddress, sessionId);
            aem.moveSyncDataSourceConfiguration(oldSyncGroupId, newSyncGroupId, syncDataSourceConfigurationIds);
            //TODO: audit entry
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releaseSyncDataSourceConfigFromSyncGroup", ipAddress, sessionId);
            aem.releaseSyncDataSourceConfigFromSyncGroup(syncGroupId, syncDataSourceConfigurationIds);
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
        //<editor-fold desc="Process Engine" defaultstate="collapsed">

    @Override
    public RemoteArtifact getArtifactForActivity(long processInstanceId, long activityId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getArtifactForActivity", ipAddress, sessionId);
            Artifact artifact = aem.getArtifactForActivity(processInstanceId, activityId);
            
            return new RemoteArtifact(artifact.getId(), artifact.getName(), artifact.getContentType(), artifact.getContent(), artifact.getSharedInformation(), artifact.getCreationDate(), artifact.getCommitDate());
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteArtifactDefinition getArtifactDefinitionForActivity(long processDefinitionId, 
            long activityDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getArtifactDefinitionForActivity", ipAddress, sessionId);
            ArtifactDefinition artifactDefinition = aem.getArtifactDefinitionForActivity(processDefinitionId, activityDefinitionId);
            
            RemoteArtifactDefinition remoteArtifactDefinition = new RemoteArtifactDefinition(artifactDefinition.getId(), artifactDefinition.getName(), 
                artifactDefinition.getDescription(), artifactDefinition.getVersion(), artifactDefinition.getType(), 
                artifactDefinition.getDefinition(), artifactDefinition.getPreconditionsScript(), artifactDefinition.getPostconditionsScript(), 
                artifactDefinition.isPrintable(), artifactDefinition.getPrintableTemplate(), artifactDefinition.getExternalScripts()
            );
                        
            remoteArtifactDefinition.setSharedInformation(artifactDefinition.getSharedInformation());
                        
            return remoteArtifactDefinition;
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void commitActivity(long processInstanceId, long activityDefinitionId, 
            RemoteArtifact artifact, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("commitActivity", ipAddress, sessionId);
            aem.commitActivity(processInstanceId, activityDefinitionId, 
                new Artifact(artifact.getId(), artifact.getName(), artifact.getContentType(), artifact.getContent(), artifact.getSharedInformation(), artifact.getCreationDate(), artifact.getCommitDate()));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void updateActivity(long processInstanceId, long activityDefinitionId, RemoteArtifact artifact, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateActivity", ipAddress, sessionId);
            aem.updateActivity(processInstanceId, activityDefinitionId, 
                new Artifact(artifact.getId(), artifact.getName(), artifact.getContentType(), artifact.getContent(), artifact.getSharedInformation(), artifact.getCreationDate(), artifact.getCreationDate()));
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteActivityDefinition> getProcessInstanceActivitiesPath(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateActivity", ipAddress, sessionId);
            List<ActivityDefinition> activities = aem.getProcessInstanceActivitiesPath(processInstanceId);
            
            List<RemoteActivityDefinition> res = new ArrayList();
            for (ActivityDefinition activity : activities)
                res.add(RemoteActivityDefinition.asRemoteActivityDefinition(activity));
            
            return res;
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    @Override
    public RemoteActivityDefinition getNextActivityForProcessInstance(long processInstanceId, 
            String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getNextActivityForProcessInstance", ipAddress, sessionId);
            ActivityDefinition activityDefinition = aem.getNextActivityForProcessInstance(processInstanceId);
            return RemoteActivityDefinition.asRemoteActivityDefinition(activityDefinition);
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createProcessInstance(long processDefinitionId, String processInstanceName, 
            String processInstanceDescription, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateWebServiceCall("createProcessInstance", ipAddress, sessionId);
            return aem.createProcessInstance(processDefinitionId, processInstanceName, processInstanceDescription);
                        
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteProcessDefinition getProcessDefinition(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getProcessDefinition", ipAddress, sessionId);
            ProcessDefinition processDefinition = aem.getProcessDefinition(processDefinitionId);
            
            return new RemoteProcessDefinition(processDefinitionId, processDefinition.getName(), 
                    processDefinition.getDescription(), processDefinition.getCreationDate(), 
                    processDefinition.getVersion(), processDefinition.isEnabled(), 
                    RemoteActivityDefinition.asRemoteActivityDefinition(processDefinition.getStartActivity()),
                    RemoteKpi.asRemoteKpis(processDefinition.getKpis()),
                    RemoteKpiAction.asRemoteKpiActions(processDefinition.getKpiActions())
            );
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteProcessDefinition(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteProcessDefinition", ipAddress, sessionId);
            aem.deleteProcessDefinition(processDefinitionId);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateProcessDefinition(long processDefinitionId, List<StringPair> properties, 
            byte[] structure, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateProcessDefinition", ipAddress, sessionId);
            aem.updateProcessDefinition(processDefinitionId, properties, structure);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createProcessDefinition(String name, String description, String version, 
            boolean enabled, byte[] structure, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createProcessDefinition", ipAddress, sessionId);
            return aem.createProcessDefinition(name, description, version, enabled, structure);
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteProcessInstance> getProcessInstances(long processDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getProcessInstances", ipAddress, sessionId);
            List<ProcessInstance> processInstances = aem.getProcessInstances(processDefinitionId);
            
            List<RemoteProcessInstance> result = new ArrayList();
            
            for (ProcessInstance processInstance : processInstances) {
                result.add(new RemoteProcessInstance(
                    processInstance.getId(), 
                    processInstance.getName(), 
                    processInstance.getDescription(), 
                    processInstance.getCurrentActivity(), 
                    processInstance.getProcessDefinition()));
            }
            return result;
                        
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }        
    }
    
    @Override
    public List<RemoteProcessDefinition> getProcessDefinitions(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getProcessDefinitions", ipAddress, sessionId);
            List<ProcessDefinition> processDefinitions = aem.getProcessDefinitions();
            
            List<RemoteProcessDefinition> result = new ArrayList();
            
            for (ProcessDefinition processDefinition : processDefinitions) {
                                                
                result.add(new RemoteProcessDefinition(
                    processDefinition.getId(), 
                    processDefinition.getName(), 
                    processDefinition.getDescription(), 
                    processDefinition.getCreationDate(), 
                    processDefinition.getVersion(), 
                    processDefinition.isEnabled(), 
                    RemoteActivityDefinition.asRemoteActivityDefinition(processDefinition.getStartActivity()),
                    RemoteKpi.asRemoteKpis(processDefinition.getKpis()),
                    RemoteKpiAction.asRemoteKpiActions(processDefinition.getKpiActions())
                ));
            }
            return result;
                        
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteProcessInstance getProcessInstance(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getProcessInstance", ipAddress, sessionId);
            
            ProcessInstance processInstance = aem.getProcessInstance(processInstanceId);
            
            RemoteProcessInstance remoteProcessInstance = new RemoteProcessInstance(
                processInstance.getId(), 
                processInstance.getName(), 
                processInstance.getDescription(), 
                processInstance.getCurrentActivity(), 
                processInstance.getProcessDefinition());
            
            return remoteProcessInstance;
            
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteProcessInstance(long processInstanceId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteProcessInstance", ipAddress, sessionId);
            
            aem.deleteProcessInstance(processInstanceId);
                        
        } catch(InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void reloadProcessDefinitions(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("reloadProcessDefinitions", ipAddress, sessionId);
            
            aem.reloadProcessDefinitions();
            
        } catch(InventoryException ex) {
            Exceptions.printStackTrace(ex);
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteKpiResult executeActivityKpiAction(String kpiActionName, RemoteArtifact remoteArtifact, long processDefinitionId, long activityDefinitionId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("executeKpiAction", ipAddress, sessionId);
            
            Artifact artifact = null;
            
            if (remoteArtifact != null) {
                artifact = new Artifact(
                    remoteArtifact.getId(), 
                    remoteArtifact.getName(), 
                    remoteArtifact.getContentType(), 
                    remoteArtifact.getContent(), 
                    remoteArtifact.getSharedInformation(), 
                    remoteArtifact.getCreationDate(), 
                    remoteArtifact.getCommitDate());
            }
            ProcessDefinition processDefinition = aem.getProcessDefinition(processDefinitionId);
            ActivityDefinition activityDefinition = aem.getActivityDefinition(processDefinitionId, activityDefinitionId);
                        
            KpiResult kpiResult = KpiResult.runActivityKpiAction(kpiActionName, artifact, processDefinition, activityDefinition);
            RemoteKpiResult remoteKpiResult = null;
            
            if (kpiResult != null)
                remoteKpiResult = new RemoteKpiResult(kpiResult.getComplianceLevel(), kpiResult.getObservations(), kpiResult.getValues());
            
            return remoteKpiResult;
        } catch(InventoryException ex) {
            Exceptions.printStackTrace(ex);
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
                        
                        List<BusinessObjectLight> matchedCommunicationsElements = bem.getObjectsWithFilterLight("GenericCommunicationsElement", "name", resourceDefinitionTokens[0]);
                        
                        if (matchedCommunicationsElements.isEmpty())
                            throw new ServerSideException(String.format("No resource with name %s could be found", resourceDefinitionTokens[0]));
                        
                        if (matchedCommunicationsElements.size() > 1)
                            throw new ServerSideException(String.format("More than one communications equipment with name %s was found", resourceDefinitionTokens[0]));
                        
                        List<BusinessObjectLight> deviceChildren = bem.getObjectChildren(matchedCommunicationsElements.get(0).getClassName(), 
                                                                            matchedCommunicationsElements.get(0).getId(), -1);

                        for (BusinessObjectLight deviceChild : deviceChildren) {
                            if (resourceDefinitionTokens[1].equals(deviceChild.getName())) {
                                List<BusinessObjectLight> portsInSlot = bem.getObjectChildren(deviceChild.getClassName(), deviceChild.getId(), -1);
                                for (BusinessObjectLight portInSlot : portsInSlot) {
                                    if (resourceDefinitionTokens[2].equals(portInSlot.getName())) 
                                        return SimpleCorrelation.servicesInPorts(Arrays.asList(bem.getObject("GenericPort", portInSlot.getId())), bem);
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
                List<BusinessObject> matchedConnections = bem.getObjectsWithFilter("GenericLogicalConnection", "name", resourceDefinitionTokens[0]);
                if (matchedConnections.isEmpty())
                    throw new ServerSideException(String.format("No logical connection with name %s could be found", resourceDefinitionTokens[0]));
                
                List<RemoteObjectLight> rawServices = new ArrayList<>();
                for (BusinessObjectLight matchedConnection : matchedConnections) {
                    List<BusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
                            matchedConnection.getId(), "uses");
                    for (BusinessObjectLight serviceInConnection : servicesInConnection)
                        rawServices.add(new RemoteObjectLight(serviceInConnection));
                }
                
                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
                HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

                //Now we organize the rawServices by customers
                for (RemoteObjectLight rawService : rawServices) {
                    BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                        if (!rawCorrelatedInformation.containsKey(customer))
                            rawCorrelatedInformation.put(customer, new ArrayList<>());
                        
                        rawCorrelatedInformation.get(customer).add(rawService);
                    }
                }

                for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
            }
            
            if (resourceType == 3) { //Same as 2, but use a GenericPhysicalConnection
                List<BusinessObject> matchedConnections = bem.getObjectsWithFilter(Constants.CLASS_GENERICPHYSICALCONNECTION, "name", resourceDefinitionTokens[0]);
                if (matchedConnections.isEmpty())
                    throw new ServerSideException(String.format("No physical connection with name %s could be found", resourceDefinitionTokens[0]));
                
                List<RemoteObjectLight> rawServices = new ArrayList<>();
                for (BusinessObjectLight matchedConnection : matchedConnections) {
                    List<BusinessObjectLight> servicesInConnection = bem.getSpecialAttribute(matchedConnection.getClassName(), 
                            matchedConnection.getId(), "uses");
                    for (BusinessObjectLight serviceInConnection : servicesInConnection)
                        rawServices.add(new RemoteObjectLight(serviceInConnection));
                }
                
                List<ServiceLevelCorrelatedInformation> serviceLevelCorrelatedInformation = new ArrayList<>();
                HashMap<BusinessObjectLight, List<RemoteObjectLight>> rawCorrelatedInformation = new HashMap<>();

                //Now we organize the rawServices by customers
                for (RemoteObjectLight rawService : rawServices) {
                    BusinessObjectLight customer = bem.getFirstParentOfClass(rawService.getClassName(), rawService.getId(), Constants.CLASS_GENERICCUSTOMER);
                    if (customer != null) {//Services without customers will be ignored. This shouldn't happen, though
                        if (!rawCorrelatedInformation.containsKey(customer))
                            rawCorrelatedInformation.put(customer, new ArrayList<>());
                        
                        rawCorrelatedInformation.get(customer).add(rawService);
                    }
                }

                for (BusinessObjectLight customer : rawCorrelatedInformation.keySet()) 
                    serviceLevelCorrelatedInformation.add(new ServiceLevelCorrelatedInformation(new RemoteObjectLight(customer), rawCorrelatedInformation.get(customer)));

                return new AssetLevelCorrelatedInformation(RemoteObject.toRemoteObjectArray(matchedConnections), serviceLevelCorrelatedInformation);
            }
            
            throw new ServerSideException("Invalid resource type");
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="Warehouse Module">
    @Override
    public List<RemotePool> getWarehouseRootPool(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null || bem == null || mem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getWarehouseRootPool", ipAddress, sessionId);
            WarehouseModule warehouseModule = (WarehouseModule) aem.getCommercialModule(WarehouseModule.MODULE_NAME);
            
            List<RemotePool> remotePools = new ArrayList();
            
            List<Pool> pools = warehouseModule.getWarehouseRootPools();
            
            for (Pool pool : pools)
                remotePools.add(new RemotePool(pool));
            
            return remotePools;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void associatePhysicalNodeToWarehouse(String objectClass, String objectId, String warehouseClass, String warehouseId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("associatePhysicalNodeToWarehouse", ipAddress, sessionId);
            
            if (mem.isSubclassOf("Warehouse", warehouseClass) || mem.isSubclassOf("VirtualWarehouse", warehouseClass)) { //NOI18N
                
            
                bem.createSpecialRelationship(warehouseClass, warehouseId, objectClass, objectId, "warehouseHas", true); //NOI18N

                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    "warehouseHas", "", objectId, ""); //NOI18N
            }
            else
                throw new ServerSideException(String.format("Class %s is not a Warehouse or VirtualWarehouse", warehouseClass));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void associatesPhysicalNodeToWarehouse(String[] objectClass, String[] objectId, String warehouseClass, String warehouseId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        
        try {
            String affectedProperties = "", newValues = "";
            
            aem.validateWebServiceCall("associatesPhysicalNodeToWarehouse", ipAddress, sessionId);
            
            if (mem.isSubclassOf("Warehouse", warehouseClass) || mem.isSubclassOf("VirtualWarehouse", warehouseClass)) { //NOI18N
                
                for (int i = 0; i < objectId.length; i++) {
                    bem.createSpecialRelationship(warehouseClass, warehouseId, objectClass[i], objectId[i], "warehouseHas", true); //NOI18N
                    affectedProperties += "warehouseHas" + " "; //NOI18N
                    newValues += objectId[i] + " ";
                }
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                    affectedProperties, "", newValues, "Associate objects to " + warehouseClass); //NOI18N            
            } 
            else
                throw new ServerSideException(String.format("Class %s is not a Warehouse or VirtualWarehouse", warehouseClass));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    @Override
    public void releasePhysicalNodeFromWarehouse(String warehouseClass, String warehouseId, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releasePhysicalNodeFromWarehouse", ipAddress, sessionId);
            bem.releaseSpecialRelationship(warehouseClass, warehouseId, objectId, "warehouseHas"); //NOI18N
            
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), warehouseClass, warehouseId, 
                ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                "warehouseHas", objectId, "", "Release object from service"); //NOI18N
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    @Override
    public void moveObjectsToWarehousePool(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjectsToWarehousePool", ipAddress, sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }

            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
                for (int i=0; i<ids.size(); i++) 
                    ids_[i] = ids.get(i);
                
                objects.put(className, ids_);
            }
            bem.moveObjectsToPool(targetClass, targetOid, objects);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to warehouse pool with id %s", Arrays.toString(objectOids), targetOid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }        
    }
    @Override
    public void moveObjectsToWarehouse(String targetClass, String targetOid, String[] objectClasses, String[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateWebServiceCall("moveObjectsToWarehouse", ipAddress, sessionId);
            HashMap<String,List<String>> temObjects = new HashMap<>();
            for (int i = 0; i< objectClasses.length; i++){
                List<String> ids = temObjects.get(objectClasses[i]);
                if (ids == null)
                    ids = new ArrayList<>();
                
                ids.add(objectOids[i]);
                temObjects.put(objectClasses[i], ids);
            }
            
            HashMap<String,String[]> objects = new HashMap<>();
            for(String className : temObjects.keySet()){
                List<String> ids = temObjects.get(className);
                String[] ids_ = new String[ids.size()];
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
    public RemoteObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getPhysicalNodeToObjectInWarehouse", ipAddress, sessionId);
            BusinessObjectLight physicalNode = bem.getPhysicalNodeToObjectInWarehouse(objectClassName, objectId);
            return physicalNode != null ? new RemoteObjectLight(physicalNode) : null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }    
    @Override
    public RemoteObjectLight getWarehouseToObject(String objectClassName, String objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getWarehouseToObject", ipAddress, sessionId);
            BusinessObjectLight warehouse = bem.getWarehouseToObject(objectClassName, objectId);
            
            return warehouse != null ? new RemoteObjectLight(warehouse) : null;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>
        //  <editor-fold desc="Outside Plant" defaultstate="collapsed">

    @Override
    public long createOSPView(String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("createOSPView", ipAddress, sessionId);
            long res = aem.createOSPView(name, description, content);
                    
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, 
                String.format("Created OSP View with id %s ", res));
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteViewObject getOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getOSPView", ipAddress, sessionId);
            return new RemoteViewObject(aem.getOSPView(viewId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteViewObjectLight> getOSPViews(String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getOSPView", ipAddress, sessionId);
            List<RemoteViewObjectLight> res = new ArrayList<>();
            
            aem.getOSPViews().forEach((aView) -> res.add(new RemoteViewObjectLight(aView)));
            
            res.sort((view1, view2) -> {
                return view1.getName().compareTo(view2.getName()); //To change body of generated lambdas, choose Tools | Templates.
            });
            
            return res;
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateOSPView(long viewId, String name, String description, byte[] content, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("updateOSPView", ipAddress, sessionId);
            aem.updateOSPView(viewId, name, description, content);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, 
                String.format("OSP View with id %s ", viewId));
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteOSPView(long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("deleteOSPView", ipAddress, sessionId);
            aem.deleteOSPView(viewId);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                String.format("OSP View with id %s ", viewId));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        
    //  </editor-fold>
        // <editor-fold defaultstate="collapsed" desc="BGP Viewer Module">
        @Override
        public List<RemoteLogicalConnectionDetails> getBGPMap(List<String> mappedBGPLinksIds, String ipAddress, String sessionId) throws ServerSideException{
            if (aem == null)
                throw new ServerSideException(I18N.gm("cannot_reach_backend"));
            try {
                aem.validateWebServiceCall("createBGPView", ipAddress, sessionId);
                List<RemoteLogicalConnectionDetails> bgpMap = new ArrayList<>();
                List<BusinessObjectLight> bgpLinks = bem.getObjectsOfClassLight(Constants.CLASS_BGPLINK, -1);
                for (BusinessObjectLight bgpLink : bgpLinks) {
                    if(!mappedBGPLinksIds.contains(bgpLink.getId())){ //We only add the bgp links that are not yet rendered
                        List<BusinessObjectLight> physicalDeviceA = new ArrayList<>();
                        List<BusinessObjectLight> physicalDeviceB = new ArrayList<>();
                        List<BusinessObjectLight> bgpEndpointA = bem.getSpecialAttribute(bgpLink.getClassName(), bgpLink.getId(), "bgpLinkEndpointA");
                        if(!bgpEndpointA.isEmpty()){
                             BusinessObjectLight parent = bem.getParentOfClass(bgpEndpointA.get(0).getClassName(), bgpEndpointA.get(0).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                             if(parent == null)
                                parent = bem.getParent(bgpEndpointA.get(0).getClassName(), bgpEndpointA.get(0).getId());
                             if(parent != null)
                                physicalDeviceA.add(parent);
                        }
                        List<BusinessObjectLight> bgpEndpointB = bem.getSpecialAttribute(bgpLink.getClassName(), bgpLink.getId(), "bgpLinkEndpointB");
                        if(!bgpEndpointB.isEmpty()){
                             BusinessObjectLight parent = bem.getParentOfClass(bgpEndpointB.get(0).getClassName(), bgpEndpointB.get(0).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                             if(parent == null)
                                 parent = bem.getParent(bgpEndpointB.get(0).getClassName(), bgpEndpointB.get(0).getId());
                             if(parent != null)
                                 physicalDeviceB.add(parent);
                        }

                        bgpMap.add(new RemoteLogicalConnectionDetails(bem.getObject(bgpLink.getClassName(), bgpLink.getId()), 
                                   bgpEndpointA.isEmpty() ? null : bgpEndpointA.get(0), 
                                   bgpEndpointB.isEmpty() ? null : bgpEndpointB.get(0), 
                                   physicalDeviceA, physicalDeviceB));
                    }
                    
                }
 
                return bgpMap;
            
            } catch(InventoryException ex) {
                Exceptions.printStackTrace(ex);
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
    public void addObjectsToFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
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
    public void removeObjectsFromFavoritesFolder(String[] objectClass, String[] objectId, long favoritesFolderId, long userId, String ipAddress, String sessionId) throws ServerSideException {
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
    public List<RemoteFavoritesFolder> getFavoritesFoldersForObject(long userId ,String objectClass, String objectId, String ipAddress, String sessionId) throws ServerSideException {
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
    // <editor-fold defaultstate="collapsed" desc="Get Physical connections helper. Click on the + sign on the left to edit the code.">    
    /**
     * Provides a generic 
     * parser between physical path and a simplified list, of objects connections objects
     * [Node1 - Node2]
     * [Node1 - Node3]
     * [Node3 - Node4]
     * @param path a physical path with endpoint, connection, endpoint(one or several ports, mirror, virtual, service instances)
     * @return a list of ConfigurationItem-connection-ConfigurationItem
     * @throws MetadataObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private List<RemoteObjectLinkObject> getpPhysicalPathMap(List<RemoteObjectLight> path) 
            throws MetadataObjectNotFoundException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException
    {
        BusinessObject connection  = null;
        BusinessObject device = null;
        BusinessObject tempDevice = null;
        RemoteObjectLight tempEndPoint = null;
        List<RemoteObjectLinkObject> e2eMap = new ArrayList<>();
        
        for(int i = 0; i < path.size(); i++) {
            if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, path.get(i).getClassName()))
                connection = bem.getObject(path.get(i).getClassName(), path.get(i).getId());

            else if(!mem.isSubclassOf(Constants.CLASS_VIRTUALPORT, path.get(i).getClassName())) {
                //when two ports are followed the parent could be a GenericDistributionFrame(e.g. an ODF)
                if(i+1 < path.size() && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, path.get(i+1).getClassName())) {
                    device = bem.getParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
                    i++;
                }//if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                if(device == null)
                    device = bem.getParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);

                if(connection == null) {
                    tempEndPoint = path.get(i);
                    tempDevice = device;
                    device = null;
                } else { //if enters here it means that we have enough information to create the structure RemoteObjectLinkObject 
                    e2eMap.add(new RemoteObjectLinkObject(new RemoteObjectLight(tempDevice), tempEndPoint, new RemoteObject(connection), path.get(i), new RemoteObjectLight(device)));
                    connection = null;
                    tempEndPoint = path.get(i);
                    tempDevice = device;
                    device = null;
                }
            }
        }
        return e2eMap;
    }
    // </editor-fold>
    
    //<editor-fold desc="Kuwaiba 2.1" defaultstate="collapsed">
    @Override
    public long getObjectChildrenCount(String className, String oid, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectChildrenCount", ipAddress, sessionId);
            return bem.getObjectChildrenCount(className, oid);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    @Override
    public List<RemoteObjectLight> getObjectChildren(String className, String oid, long skip, long limit, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("getObjectChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, skip, limit));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }        
    }
    //</editor-fold>
    
    //<editor-fold desc="special explorer actions for VLANs" defaultstate="collapsed">
    @Override
    public void relatePortsToVlan(List<String> portsIds, List<String> portsClassNames, 
            String vlanId, String ipAddress, String sessionId) 
            throws ServerSideException
    {
         if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("relatePortsToVlan", ipAddress, sessionId);
            
            if(portsIds.size() == portsClassNames.size()){
                for (int i = 0; i < portsIds.size(); i++){
                    bem.createSpecialRelationship(Constants.CLASS_VLAN, vlanId, portsClassNames.get(i), portsIds.get(i), RELATIONSHIP_PORT_BELONGS_TO_VLAN, true);
                
                    aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), Constants.CLASS_VLAN, vlanId, 
                        ActivityLogEntry.ACTIVITY_TYPE_CREATE_RELATIONSHIP_INVENTORY_OBJECT, 
                        RELATIONSHIP_PORT_BELONGS_TO_VLAN, "", portsIds.get(i), "Relate port to VLAN"); //NOI18N
                }
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releasePortsFromVlan(List<String> portsIds, String vlanId,
            String ipAddress, String sessionId) throws ServerSideException
    {
        if (bem == null || aem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend"));
        try {
            aem.validateWebServiceCall("releasePortsFromVlan", ipAddress, sessionId);
            for (String portId : portsIds) {
                bem.releaseSpecialRelationship(Constants.CLASS_VLAN, vlanId, portId, RELATIONSHIP_PORT_BELONGS_TO_VLAN); //NOI18N
                       
            aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), 
                    Constants.CLASS_VLAN, vlanId, ActivityLogEntry.ACTIVITY_TYPE_RELEASE_RELATIONSHIP_INVENTORY_OBJECT, 
                RELATIONSHIP_PORT_BELONGS_TO_VLAN, portId, "", "Release object from VLAN"); //NOI18N  
            }
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    //</editor-fold>    
}
