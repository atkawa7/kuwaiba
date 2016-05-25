/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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

import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHModule;
import com.neotropic.kuwaiba.modules.sdh.SDHPosition;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLightList;
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
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.ApplicationLogEntry;
import org.kuwaiba.ws.toserialize.application.GroupInfo;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.Validator;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.business.RemoteObjectSpecialRelationships;
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createClass", ipAddress, sessionId);
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

            return mem.createClass(cm);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteClass(String className, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteClass", ipAddress, sessionId);
            mem.deleteClass(className);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteClass", ipAddress, sessionId);
            mem.deleteClass(classId);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(className);
            List<Validator> validators = new ArrayList<>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, className))
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getClass", ipAddress, sessionId);
            ClassMetadata myClass = mem.getClass(classId);
            List<Validator> validators = new ArrayList<>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, myClass.getName())){
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                }
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));

         } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getAllClassesLight(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getAllClassesLight", ipAddress, sessionId);
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
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getSubclassesLight", ipAddress, sessionId);
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
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getSubClassesLightNoRecursive", ipAddress, sessionId);
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
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfo> getAllClasses(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getAllClasses", ipAddress, sessionId);
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
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveClass(String classToMoveName, String targetParentName, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("moveClass", ipAddress, sessionId);
            mem.moveClass(classToMoveName, targetParentName);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void moveClass(long classToMoveId, long targetParentId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("moveClass", ipAddress, sessionId);
            mem.moveClass(classToMoveId, targetParentId);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttribute(String className, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createAttribute", ipAddress, sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(attributeDefinition.getName());
            attributeMetadata.setDisplayName(attributeDefinition.getDisplayName());
            attributeMetadata.setDescription(attributeDefinition.getDescription());
            attributeMetadata.setReadOnly(attributeDefinition.isReadOnly());
            attributeMetadata.setType(attributeDefinition.getType());
            attributeMetadata.setUnique(attributeDefinition.isUnique());
            attributeMetadata.setVisible(attributeDefinition.isVisible());
            attributeMetadata.setNoCopy(attributeDefinition.isNoCopy());

            mem.createAttribute(className, attributeMetadata);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void createAttribute(long classId, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createAttribute", ipAddress, sessionId);
            AttributeMetadata attributeMetadata = new AttributeMetadata();

            attributeMetadata.setName(attributeDefinition.getName());
            attributeMetadata.setDisplayName(attributeDefinition.getDisplayName());
            attributeMetadata.setDescription(attributeDefinition.getDescription());
            attributeMetadata.setReadOnly(attributeDefinition.isReadOnly());
            attributeMetadata.setType(attributeDefinition.getType());
            attributeMetadata.setUnique(attributeDefinition.isUnique());
            attributeMetadata.setVisible(attributeDefinition.isVisible());
            attributeMetadata.setNoCopy(attributeDefinition.isNoCopy());
            
            mem.createAttribute(classId, attributeMetadata);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setClassProperties(ClassInfo newClassDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("setClassProperties", ipAddress, sessionId);
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
            
            mem.setClassProperties(cm);

         } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getAttribute", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription());
            return atrbInfo;
         } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        try {
            aem.validateCall("setClassProperties", ipAddress, sessionId);
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeId);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription());
            return atrbInfo;

         } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(long classId, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("setAttributeProperties", ipAddress, sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(newAttributeDefinition.getId());
            attrMtdt.setName(newAttributeDefinition.getName());
            attrMtdt.setDisplayName(newAttributeDefinition.getDisplayName());
            attrMtdt.setDescription(newAttributeDefinition.getDescription());
            attrMtdt.setType(newAttributeDefinition.getType());
            attrMtdt.setAdministrative(newAttributeDefinition.isAdministrative());
            attrMtdt.setUnique(newAttributeDefinition.isUnique());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());

            mem.setAttributeProperties(classId, attrMtdt);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("setAttributeProperties", ipAddress, sessionId);
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setId(newAttributeDefinition.getId());
            attrMtdt.setName(newAttributeDefinition.getName());
            attrMtdt.setDisplayName(newAttributeDefinition.getDisplayName());
            attrMtdt.setDescription(newAttributeDefinition.getDescription());
            attrMtdt.setType(newAttributeDefinition.getType());
            attrMtdt.setAdministrative(newAttributeDefinition.isAdministrative());
            attrMtdt.setUnique(newAttributeDefinition.isUnique());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());
            attrMtdt.setNoCopy(newAttributeDefinition.isNoCopy());

            mem.setAttributeProperties(className, attrMtdt);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteAttribute", ipAddress, sessionId);
            mem.deleteAttribute(className, attributeName);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(long classId, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteAttribute", ipAddress, sessionId);
            mem.deleteAttribute(classId, attributeName);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPossibleChildren", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }

    }

    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPossibleChildrenNoRecursive", ipAddress, sessionId);
            List<ClassInfoLight> cml = new ArrayList<>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getSpecialPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        List<ClassInfoLight> res = new ArrayList<>();
        try {
            aem.validateCall("getSpecialPossibleChildrenNoRecursive", ipAddress, sessionId);
            for (String aClass : bre.getPossibleChildrenAccordingToModels().keySet()){
                if (mem.isSubClass(aClass, parentClassName)){
                    for (String possibleChild : bre.getPossibleChildrenAccordingToModels().get(aClass)){
                        List<ClassMetadataLight> subClasses = mem.getSubClassesLight(possibleChild, false, true);
                        for (ClassMetadataLight subClass : subClasses)
                            res.add(new ClassInfoLight(subClass, new Validator[0]));
                    }
                    break;
                }
            }
        } catch (InventoryException ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
        return res;
    }
        
    @Override
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getUpstreamContainmentHierarchy", ipAddress, sessionId);
            List<ClassInfoLight> res = new ArrayList<>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive)){
                res.add(new ClassInfoLight(cil, new Validator[]{}));
            }
            return res;

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("addPossibleChildren", ipAddress, sessionId);
            mem.addPossibleChildren(parentClassId, possibleChildren);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("addPossibleChildren", ipAddress, sessionId);
            mem.addPossibleChildren(parentClassName, possibleChildren);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("removePossibleChildren", ipAddress, sessionId);
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createListTypeItem", ipAddress, sessionId);
            return aem.createListTypeItem(className, name, displayName);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deletelistTypeItem", ipAddress, sessionId);
            aem.deleteListTypeItem(className, oid, realeaseRelationships);

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getListTypeItems(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getListTypeItems", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
            RemoteObjectLight[] res = new RemoteObjectLight[listTypeItems.size()];
            for (int i = 0; i < res.length; i++)
                res[i] = new RemoteObjectLight(listTypeItems.get(i));
            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public byte[] getClassHierarchy(boolean showAll, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getClassHierarchy", ipAddress, sessionId);
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            Session newSession = aem.createSession(user, password, IPAddress);
            aem.createGeneralActivityLogEntry(user, ActivityLogEntry.ACTIVITY_TYPE_OPEN_SESSION, String.format("Connected from %s", IPAddress));
            return new RemoteSession(newSession.getToken(), newSession.getUser());
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage()); 
        }
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws ServerSideException, NotAuthorizedException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            String user = getUserNameFromSession(sessionId);
            aem.closeSession(sessionId, remoteAddress);
            aem.createGeneralActivityLogEntry(user, ActivityLogEntry.ACTIVITY_TYPE_CLOSE_SESSION, String.format("Connected from %s", remoteAddress));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteObjectLight[] getObjectChildren(long oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(objectClassId, oid, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getSiblings", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSiblings(className, oid, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject[] getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getChildrenOfClass", ipAddress, sessionId);
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getChildrenOfClassLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getObject(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObject", ipAddress, sessionId);
            return new RemoteObject(bem.getObject(objectClass, oid));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectLight", ipAddress, sessionId);
            return new RemoteObjectLight(bem.getObjectLight(objectClass, oid));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObject getParent(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getParent", ipAddress, sessionId);
            return new RemoteObject(bem.getParent(objectClass, oid));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getParents(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getParents", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getParents(objectClass, oid));
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getSpecialAttribute(String objectClass, long objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getSpecialAttribute", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(objectClass, objectId, attributeName));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, long oid, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getSpecialAttributes", ipAddress, sessionId);
            HashMap<String, List<RemoteBusinessObjectLight>> relationships = bem.getSpecialAttributes(objectClass, oid);
            RemoteObjectSpecialRelationships res = new RemoteObjectSpecialRelationships(relationships);

            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getParentOfClass", ipAddress, sessionId);
            return new RemoteObject(bem.getParentOfClass(objectClass, oid, parentClass));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }  

    @Override
    public RemoteObjectLight[] getObjectSpecialChildren (String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectSpecialChildren", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(objectClass, objectId));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectsOfClassLight", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsOfClassLight(className, maxResults));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public ClassInfoLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");

        try {
            aem.validateCall("getInstanceableListTypes", ipAddress, sessionId);
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
            ClassInfoLight[] res = new ClassInfoLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new ClassInfoLight(instanceableListTypes.get(i), new Validator[0]);
            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[][] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateCall("createObject", ipAddress, sessionId);
            HashMap<String,List<String>> attributes = new HashMap<>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));
            long newObjectId = bem.createObject(className, parentClassName, parentOid,attributes, template);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newObjectId));
            return newObjectId;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[][] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateCall("createSpecialObject", ipAddress, sessionId);
            HashMap<String,List<String>> attributes = new HashMap<>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            long newSpecialObjectId = bem.createSpecialObject(className, parentClassName, parentOid, attributes, template);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId),
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.valueOf(newSpecialObjectId));
            
            return newSpecialObjectId;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteObjects(String[] classNames, long[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (classNames.length != oids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("deleteObjects", ipAddress, sessionId);
            HashMap<String,long[]> objects = new HashMap<>();
            for (int i = 0; i< classNames.length;i++){
                if (objects.get(classNames[i]) == null)
                    objects.put(classNames[i], new long[]{oids[i]});
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
    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("moveObjects", ipAddress, sessionId);
            HashMap<String,long[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
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
    public long[] copyObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException("Array sizes do not match");
        try {
            aem.validateCall("copyObjects", ipAddress, sessionId);
            HashMap<String,long[]> objects = new HashMap<>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
            }
            long[] newObjects = bem.copyObjects(targetClass, targetOid, objects, recursive);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                    String.format("%s moved to object with id %s of class %s", Arrays.toString(newObjects), targetOid, targetClass));
            return newObjects;
        }catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, long oid, String[] attributeNames, String[][] attributeValues, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        try {
            aem.validateCall("updateObject", ipAddress, sessionId);
            HashMap<String,List<String>> attributes = new HashMap<>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            ChangeDescriptor theChange = bem.updateObject(className, oid, attributes);
            
            if (mem.isSubClass(Constants.CLASS_GENERICOBJECTLIST, className))
                aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_APPLICATION_OBJECT, theChange);
            else
                aem.createObjectActivityLogEntry(getUserNameFromSession(sessionId), className,
                        oid, ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, theChange);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Physical connections
    @Override
    public void connectMirrorPort(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        if (aObjectId == bObjectId)
            throw new ServerSideException("A port can not be mirror to itself");
        
        try {
            aem.validateCall("connectMirrorPort", ipAddress, sessionId);
            
            if (!mem.isSubClass("GenericPort", aObjectClass))
                throw new ServerSideException(String.format("Object %s [%s] is not a port", aObjectId, aObjectClass));
            if (!mem.isSubClass("GenericPort", bObjectClass))
                throw new ServerSideException(String.format("Object %s [%s] is not a port", bObjectId, bObjectClass));
            
            if (bem.hasSpecialRelationship(aObjectClass, aObjectId, "mirror", 1))
                throw new ServerSideException(String.format("Object %s [%s] already has a mirror port", aObjectId, aObjectClass));
            
            if (bem.hasSpecialRelationship(bObjectClass, bObjectId, "mirror", 1))
                throw new ServerSideException(String.format("Object %s [%s] already has a mirror port", bObjectId, bObjectClass));
            
            bem.createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, "mirror", true);
            
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseMirrorPort(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        try {
            aem.validateCall("releaseMirrorPort", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPort", objectClass))
                throw new ServerSideException(String.format("Object %s [%s] is not a port", objectId, objectClass));
                        
            bem.releaseSpecialRelationship(objectClass, objectId, -1, "mirror");
            
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPhysicalConnection(String aObjectClass, long aObjectId,
            String bObjectClass, long bObjectId, String parentClass, long parentId,
            String[] attributeNames, String[][] attributeValues, String connectionClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException("Attribute names and attribute values arrays sizes doesn't match");

        HashMap<String, List<String>> attributes = new HashMap<>();
        for (int i = 0; i < attributeValues.length; i++)
            attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

        long newConnectionId = -1;
        try {
            aem.validateCall("createPhysicalConnection", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass))
                throw new ServerSideException("Class %s is not subclass of GenericPhysicalConnection");

            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubClass("GenericPhysicalLink", connectionClass)){
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, "endpointA").isEmpty())
                    throw new ServerSideException(String.format("The selected endpoint %s [%s] is already connected", aObjectClass, aObjectId));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, "endpointB").isEmpty())
                    throw new ServerSideException(String.format("The selected endpoint %s [%s] is already connected", bObjectClass, bObjectId));
            }

            newConnectionId = bem.createSpecialObject(connectionClass, parentClass, parentId, attributes, 0);
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, "endpointA", true);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, "endpointB", true);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, String.format("New connection of class %s", connectionClass));
            return newConnectionId;
        } catch (InventoryException e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1)
                deleteObjects(new String[]{connectionClass}, new long[]{newConnectionId}, true, ipAddress, sessionId);

            throw new ServerSideException(e.getMessage());
        }
    }
    
    @Override
    public long[] createBulkPhysicalConnections(String connectionClass, int numberOfChildren,
            String parentClass, long parentId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createBulkPhysicalConnections", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass))
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));
            
            long[] newConnections = bem.createBulkSpecialObjects(connectionClass, numberOfChildren, parentClass, parentId);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    String.format("%s new connections  of class %s", numberOfChildren, connectionClass));
            
            return newConnections;

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getConnectionEndpoints(String connectionClass, long connectionId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getConnectionEndpoints", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass))
                throw new ServerSideException(String.format("Class %s is not a physical connection", connectionClass));

            List<RemoteBusinessObjectLight> endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA");
            List<RemoteBusinessObjectLight> endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB");
            return new RemoteObjectLight[]{endpointA.isEmpty() ? null : new RemoteObjectLight(endpointA.get(0)), 
                                            endpointB.isEmpty() ? null : new RemoteObjectLight(endpointB.get(0))};

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, Long[] sideAIds, 
                String[] linksClassNames, Long[] linksIds, String[] sideBClassNames, 
                Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("connectPhysicalLinks", ipAddress, sessionId);
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (linksClassNames[i] != null && !mem.isSubClass("GenericPhysicalLink", linksClassNames[i]))
                    throw new ServerSideException(String.format("Class %s is not a physical link", linksClassNames[i]));
                if (sideAClassNames[i] != null && !mem.isSubClass("GenericPort", sideAClassNames[i]))
                    throw new ServerSideException(String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubClass("GenericPort", sideBClassNames[i]))
                    throw new ServerSideException(String.format("Class %s is not a port", sideBClassNames[i]));
                
                if (Objects.equals(sideAIds[i], sideBIds[i]))
                    throw new ServerSideException("Can not connect a port to itself");
                
                List<RemoteBusinessObjectLight> aEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointA");
                List<RemoteBusinessObjectLight> bEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointB");
                
                if (!aEndpointList.isEmpty()){
                    if (Objects.equals(aEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(aEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at lest one of the endpoints");
                }
                
                if (!bEndpointList.isEmpty()){
                    if (Objects.equals(bEndpointList.get(0).getId(), sideAIds[i]) || Objects.equals(bEndpointList.get(0).getId(), sideBIds[i]))
                        throw new ServerSideException("The link is already related to at lest one of the endpoints");
                }
                
                if (sideAIds[i] != null && sideAClassNames[i] != null){
                    if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointA").isEmpty() || 
                        !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointB").isEmpty())
                        throw new ServerSideException(String.format("The selected endpoint %s [%s] is already connected", sideAClassNames[i], sideAIds[i]));
                    
                    if (aEndpointList.isEmpty())
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i], "endpointA", true);
                    else
                        throw new ServerSideException(String.format("Link %s [%s] already has an aEndpoint", linksIds[i], linksClassNames[i]));
                }
                if (sideBIds[i] != null && sideBClassNames[i] != null){
                    if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointB").isEmpty() || 
                        !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointA").isEmpty())
                        throw new ServerSideException(String.format("The selected endpoint %s [%s] is already connected", sideBClassNames[i], sideBIds[i]));
                    
                    if (bEndpointList.isEmpty())
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i], "endpointB", true);
                    else
                        throw new ServerSideException(String.format("Link %s [%s] already has a bEndpoint", linksIds[i], linksClassNames[i]));
                }
            }
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getPhysicalPath(String objectClassName, long oid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPhysicalPath", ipAddress, sessionId);
            if (!mem.isSubClass("GenericPort", objectClassName))
                throw new ServerSideException(String.format("Class %s is not a port", objectClassName));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getPhysicalPath(objectClassName, oid)); 

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deletePhysicalConnection(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    //Service Manager
    @Override
    public void associateObjectToService(String objectClass, long objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("associateObjectToService", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass))
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, "uses", true);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    @Override
    public void associateObjectsToService(String[] objectClass, long[] objectId, String serviceClass, long serviceId, String ipAddress, String sessionId) 
            throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("associateObjectsToService", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass))
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            for (int i = 0; i < objectId.length; i++) 
                bem.createSpecialRelationship(serviceClass, serviceId, objectClass[i], objectId[i], "uses", true);
            
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromService(String serviceClass, long serviceId, long otherObjectId, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("releaseObjectFromService", ipAddress, sessionId);
            bem.releaseSpecialRelationship(serviceClass, serviceId, otherObjectId, "uses");
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getServiceResources(String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getServiceResources", ipAddress, sessionId);
            if (!mem.isSubClass("GenericService", serviceClass))
                throw new ServerSideException(String.format("Class %s is not a service", serviceClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(serviceClass, serviceId, "uses"));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createService(String serviceClass, String customerClass, 
            long customerId, String[] attributes, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createService", ipAddress, sessionId);
            if (!mem.isSubClass("GenericCustomer", customerClass))
                throw new ServerSideException(String.format("Class %s is not a customer", customerClass));
            if (!mem.isSubClass("GenericService", serviceClass))
                throw new ServerSideException(String.format("Class %s is not a customer", serviceClass));
            
            return bem.createSpecialObject(serviceClass, customerClass, customerId, null, 0);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createCustomer(String customerClass, String[] attributes, 
            String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createCustomer", ipAddress, sessionId);
            if (!mem.isSubClass("GenericCustomer", customerClass))
                throw new ServerSideException(String.format("Class %s is not a customer", customerClass));
            
            return bem.createSpecialObject(customerClass, null, -1, null, 0);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getServices(String customerClass, long customerId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null || aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getServices", ipAddress, sessionId);
            if (!mem.isSubClass("GenericCustomer", customerClass))
                throw new ServerSideException(String.format("Class %s is not a customer", customerClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(customerClass, customerId));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    @Override
    public UserInfo[] getUsers(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getUsers", ipAddress, sessionId);
            List<UserProfile> users = aem.getUsers();

            UserInfo[] usersInfo = new UserInfo[users.size()];
            int i=0;
            for (UserProfile user: users)  {
                usersInfo[i]=(new UserInfo(user));
                i++;
            }
            return usersInfo;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public GroupInfo[] getGroups(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getGroups", ipAddress, sessionId);
            List<GroupProfile> groups = aem.getGroups();

            GroupInfo [] userGroupInfo = new GroupInfo[groups.size()];
            int i=0;
            for (GroupProfile group : groups) {
               userGroupInfo[i] = new GroupInfo(group);
               i++;
            }
            return userGroupInfo;

        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, 
    String firstName, String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("setUserProperties", ipAddress, sessionId);
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, privileges, groups);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createGroup(String groupName, String description, long[] privileges, long[] users, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createGroup", ipAddress, sessionId);
            return aem.createGroup(groupName, description, privileges, users);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createUser(String userName, String password, String firstName, 
    String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createUser", ipAddress, sessionId);
            long newUserId = aem.createUser(userName, password, firstName, lastName, enabled,  privileges, groups);
            
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_CREATE_APPLICATION_OBJECT, String.format("New User ", userName));
            
            return newUserId;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void setGroupProperties(long oid, String groupName, String description, 
        long[] privileges, long[] users, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("setGroupProperties", ipAddress, sessionId);
            aem.setGroupProperties(oid, groupName, description, privileges, users);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(long[] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteUsers", ipAddress, sessionId);
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteGroups", ipAddress, sessionId);
            aem.deleteGroups(oids);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override   
    public long createObjectRelatedView(long objectId, String objectClass, String name, 
        String description, int viewType, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createObjectRelatedView", ipAddress, sessionId);
            return aem.createObjectRelatedView(objectId, objectClass, name, description, viewType, structure, background);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createGeneralView", ipAddress, sessionId);
            return aem.createGeneralView(viewType, name, description, structure, background);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ViewInfo getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectRelatedView", ipAddress, sessionId);
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId);
            if (myView == null)
                return null;
            ViewInfo res = new ViewInfo(myView);
            res.setBackground(myView.getBackground());
            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getObjectRelatedViews(long oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getObjectRelatedViews", ipAddress, sessionId);
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getGeneralView", ipAddress, sessionId);
            return new ViewInfo(aem.getGeneralView(viewId));
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getGeneralViews(int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getGeneralViews", ipAddress, sessionId);
            List<ViewObjectLight> views = aem.getGeneralViews(viewType, limit);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            for (int i = 0; i < views.size(); i++)
                res[i] = new ViewInfoLight(views.get(i));
           return res;
        } catch(InventoryException e) {
            throw new ServerSideException(e.getMessage());
        }
    }

    /**
     * Updates a view directly related to an object
     * @param objectOid
     * @param objectClass
     * @param viewId
     * @param viewName
     * @param viewDescription
     * @param structure
     * @param background If null, the background is removed. If a 0-sized array, it's unmodified
     * @param ipAddress
     * @param sessionId
     * @throws ServerSideException 
     */
    @Override
    public void updateObjectRelatedView(long objectOid, String objectClass, 
        long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("updateObjectRelatedView", ipAddress, sessionId);
            ChangeDescriptor theChange = aem.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), 
                    ActivityLogEntry.ACTIVITY_TYPE_UPDATE_VIEW, theChange);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, 
        byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("updateGeneralView", ipAddress, sessionId);
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteGeneralView", ipAddress, sessionId);
            aem.deleteGeneralViews(oids);
        } catch(InventoryException ie) {
            throw new ServerSideException(ie.getMessage());
        }
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createQuery", ipAddress, sessionId);
            return aem.createQuery(queryName, ownerOid, queryStructure, description);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, 
        String description, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("saveQuery", ipAddress, sessionId);
            aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deleteQuery", ipAddress, sessionId);
            aem.deleteQuery(queryOid);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getQueries", ipAddress, sessionId);
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
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getQuery", ipAddress, sessionId);
            return new RemoteQuery(aem.getQuery(queryOid));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ResultRecord[] executeQuery(TransientQuery query, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("executeQuery", ipAddress, sessionId);
            List<org.kuwaiba.apis.persistence.application.ResultRecord> resultRecordList = aem.executeQuery(transientQuerytoExtendedQuery(query));

            ResultRecord[] resultArray = new ResultRecord[resultRecordList.size()];
            
            for (int i=0;resultRecordList.size() >i; i++)
            {
                RemoteObjectLight rol = new RemoteObjectLight(resultRecordList.get(i).getId(), resultRecordList.get(i).getName(), resultRecordList.get(i).getClassName());
                resultArray[i] = new ResultRecord(rol, (ArrayList<String>) resultRecordList.get(i).getExtraColumns());
            }

            return resultArray;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    //Pools
    @Override
    public long createPool(long parentId, String name, String description, String instancesOfClass, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("createPool", ipAddress, sessionId);
            return aem.createPool(parentId, name, description, instancesOfClass);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String attributeNames[], String attributeValues[][], long templateId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            return bem.createPoolItem(poolId, className, attributeNames, attributeValues, templateId);
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("deletePools", ipAddress, sessionId);
            aem.deletePools(ids);
            aem.createGeneralActivityLogEntry(getUserNameFromSession(sessionId), ActivityLogEntry.ACTIVITY_TYPE_DELETE_APPLICATION_OBJECT, 
                    String.format("%s pools deleted", ids.length));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getPools(int limit, long parentId, String className, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPools", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPools(limit, parentId, className));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getPools(int limit, String className, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPools", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPools(limit, className));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPoolItems(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getPoolItem", ipAddress, sessionId);
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPoolItems(poolId, limit));
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getBusinessObjectAuditTrail", ipAddress, sessionId);
            List<ActivityLogEntry> entries = aem.getBusinessObjectAuditTrail(objectClass, objectId, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.INFO, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getApplicationObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            aem.validateCall("getActivityAuditTrail", ipAddress, sessionId);
            List<ActivityLogEntry> entries = aem.getGeneralActivityAuditTrail(page, limit);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (InventoryException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.INFO, ex.getMessage());
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/Bulk load data methods">
    @Override
    public String bulkUpload(byte[] file, int commitSize, int dataType, String ipAddress, String sessionId) throws ServerSideException{
        if (sync == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        return sync.bulkUploadFromFile(file, commitSize, dataType, ipAddress, sessionId);
    }
    
    @Override
    public byte[] downloadBulkLoadLog(String fileName, String ipAddress, String sessionId) throws ServerSideException{
        if (sync == null)
            throw new ServerSideException("Can't reach the backend. Contact your administrator");
        try {
            return sync.downloadBulkLoadLog(fileName, ipAddress, sessionId);
        } catch (IOException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public boolean isSubclassOf(String className, String subclassOf, String remoteAddress, String sessionId) throws ServerSideException {
        try {
            return mem.isSubClass(subclassOf, className);
        } catch (org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Commercial modules data methods">
        // <editor-fold defaultstate="collapsed" desc="SDH Networks Module">
    @Override
    public long createSDHTransportLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHTransportLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.createSDHTransportLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, defaultName);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public long createSDHContainerLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<SDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHContainerLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.createSDHContainerLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    @Override
    public long createSDHTributaryLink(String classNameEndpointA, long idEndpointA, 
            String classNameEndpointB, long idEndpointB, String linkType, List<SDHPosition> positions, String defaultName, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("createSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.createSDHTributaryLink(classNameEndpointA, idEndpointA, classNameEndpointB, idEndpointB, linkType, positions, defaultName);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHTransportLink(String transportLinkClass, long transportLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHTransportLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            sdhModule.deleteSDHTransportLink(transportLinkClass, transportLinkId, forceDelete);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHContainerLink(String containerLinkClass, long containerLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHContainerLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            sdhModule.deleteSDHContainerLink(containerLinkClass, containerLinkId, forceDelete);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public void deleteSDHTributaryLink(String tributaryLinkClass, long tributaryLinkId, boolean forceDelete, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("deleteSDHTributaryLink", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            sdhModule.deleteSDHTributaryLink(tributaryLinkClass, tributaryLinkId, forceDelete);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLightList> findSDHRouteUsingTransportLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("findSDHRouteUsingTransportLinks", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.findSDHRouteUsingTransportLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLightList> findSDHRouteUsingContainerLinks(String communicationsEquipmentClassA, 
                                            long  communicationsEquipmentIdA, String communicationsEquipmentClassB, 
                                            long  communicationsEquipmentIB, String ipAddress, String sessionId) throws ServerSideException {
        try {
            aem.validateCall("findSDHRouteUsingContainerLinks", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.findSDHRouteUsingContainerLinks(communicationsEquipmentClassA, communicationsEquipmentIdA, communicationsEquipmentClassB, communicationsEquipmentIB);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    @Override
    public List<SDHContainerLinkDefinition> getSDHTransportLinkStructure(String transportLinkClass, long transportLinkId, String ipAddress, String sessionId) 
            throws ServerSideException {
        try {
            aem.validateCall("getSDHTransportLinkStructure", ipAddress, sessionId);
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
            aem.validateCall("getSDHContainerLinkStructure", ipAddress, sessionId);
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            return sdhModule.getSDHContainerLinkStructure(transportLinkClass, transportLinkId);
        } catch (InventoryException ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
        // </editor-fold>    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper methods. Click on the + sign on the left to edit the code.">
    protected final void connect(){
        try {
            PersistenceService persistenceService = PersistenceService.getInstance();
            mem = persistenceService.getMetadataEntityManager();
            bem = persistenceService.getBusinessEntityManager();
            aem = persistenceService.getApplicationEntityManager();
        }catch(Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName() + ": {0}",ex.getMessage()); //NOI18N
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
}
