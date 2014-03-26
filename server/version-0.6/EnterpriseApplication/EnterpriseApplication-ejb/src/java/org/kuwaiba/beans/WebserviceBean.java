/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>
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

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Singleton;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.Session;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.kuwaiba.sync.SyncServicesManager;
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
import org.kuwaiba.ws.toserialize.metadata.CategoryInfo;
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
    private MetadataEntityManagerRemote mem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManagerRemote bem;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManagerRemote aem;
    /**
     * Business rules engine reference
     */
    private TempBusinessRulesEngine bre;
    /**
     * Sync/load data reference
     */
    private SyncServicesManager ssm;
    
    public WebserviceBean() {
        super();
        bre = new TempBusinessRulesEngine();
        ssm = new SyncServicesManager();
        connect();
    }

    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public long createClass(ClassInfo classDefinition, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            ClassMetadata cm = new ClassMetadata();

            cm.setName(classDefinition.getClassName());
            cm.setDisplayName(classDefinition.getDisplayName());
            cm.setDescription(classDefinition.getDescription());
            cm.setParentClassName(classDefinition.getParentClassName());
            cm.setAbstract(classDefinition.isAbstract());
            cm.setColor(0);
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

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteClass(String className, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteClass(className, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteClass(classId, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            ClassMetadata myClass = mem.getClass(className, ipAddress, sessionId);
            List<Validator> validators = new ArrayList<Validator>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, className, ipAddress, sessionId))
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(long classId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            ClassMetadata myClass = mem.getClass(classId, ipAddress, sessionId);
            List<Validator> validators = new ArrayList<Validator>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, myClass.getName(), ipAddress, sessionId)){
                    validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                }
            }
            return new ClassInfo(myClass, validators.toArray(new Validator[0]));

         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getAllClassesLight(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getAllClassesLight(includeListTypes, false, ipAddress, sessionId);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName(), ipAddress, sessionId)){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLight(className, includeAbstractClasses, includeSelf, ipAddress, sessionId);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName(), ipAddress, sessionId)){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf, ipAddress, sessionId);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName(), ipAddress, sessionId)){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfoLight(classMetadataLight, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfo> getAllClasses(boolean includeListTypes, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfo> cml = new ArrayList<ClassInfo>();
            List<ClassMetadata> classMetadataList = mem.getAllClasses(includeListTypes, false, ipAddress, sessionId);

            for (ClassMetadata classMetadata : classMetadataList){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadata.getName(), ipAddress, sessionId)){
                        validators.add(new Validator(bre.getSubclassOfValidators().get(mapping), 1));
                    }
                }
                cml.add(new ClassInfo(classMetadata, validators.toArray(new Validator[0])));
            }
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveClass(String classToMoveName, String targetParentName, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.moveClass(classToMoveName, targetParentName, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveClass(long classToMoveId, long targetParentId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.moveClass(classToMoveId, targetParentId, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void createAttribute(String className, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            AttributeMetadata atm = new AttributeMetadata();

            atm.setName(attributeDefinition.getName());
            atm.setDisplayName(attributeDefinition.getDisplayName());
            atm.setDescription(attributeDefinition.getDescription());
            atm.setReadOnly(attributeDefinition.isReadOnly());
            atm.setType(attributeDefinition.getType());
            atm.setUnique(attributeDefinition.isUnique());
            atm.setVisible(attributeDefinition.isVisible());
            atm.setNoCopy(attributeDefinition.isNoCopy());

            mem.createAttribute(className, atm, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void createAttribute(long classId, AttributeInfo attributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            AttributeMetadata atm = new AttributeMetadata();

            atm.setName(attributeDefinition.getName());
            atm.setDisplayName(attributeDefinition.getDisplayName());
            atm.setDescription(attributeDefinition.getDescription());
            atm.setReadOnly(attributeDefinition.isReadOnly());
            atm.setType(attributeDefinition.getType());
            atm.setUnique(attributeDefinition.isUnique());
            atm.setVisible(attributeDefinition.isVisible());
            atm.setNoCopy(attributeDefinition.isNoCopy());
            
            mem.createAttribute(classId, atm);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setClassProperties(ClassInfo newClassDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
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
            cm.setColor(cm.getColor());
            //cm.setCategory(classDefinition.getCategory());
            
            mem.setClassProperties(cm, ipAddress, sessionId);

         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription());
            return atrbInfo;
         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(long classId, long attributeId, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeId, ipAddress, sessionId);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription());
            return atrbInfo;

         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(long classId, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
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

            mem.setAttributeProperties(classId, attrMtdt, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, AttributeInfo newAttributeDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
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

            mem.setAttributeProperties(className, attrMtdt, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteAttribute(className, attributeName, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(long classId, String attributeName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteAttribute(classId, attributeName, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createCategory(CategoryInfo categoryDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            return mem.createCategory(ctgrMtdt, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public CategoryInfo getCategory(String categoryName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = mem.getCategory(categoryName, ipAddress, sessionId);

            CategoryInfo ctgrInfo = new CategoryInfo(ctgrMtdt.getName(),
                                                     ctgrMtdt.getDisplayName(),
                                                     ctgrMtdt.getDescription(),
                                                     ctgrMtdt.getCreationDate());
            return ctgrInfo;
         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public CategoryInfo getCategory(long categoryId, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = mem.getCategory(categoryId, ipAddress, sessionId);

            CategoryInfo ctgrInfo = new CategoryInfo();

            ctgrInfo.setName(ctgrMtdt.getName());
            ctgrInfo.setDisplayName(ctgrMtdt.getDisplayName());
            ctgrInfo.setDescription(ctgrMtdt.getDescription());
            ctgrInfo.setCreationDate(ctgrMtdt.getCreationDate());

            return ctgrInfo;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setCategoryProperties(CategoryInfo categoryDefinition, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            mem.setCategoryProperties(ctgrMtdt, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }

    }

    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName, ipAddress, sessionId);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public List<ClassInfoLight> getSpecialPossibleChildren(String parentClassName, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        
        List<ClassInfoLight> res = new ArrayList<ClassInfoLight>();
        try{
            for (String aClass : bre.getPossibleChildrenAccordingToModels().keySet()){
                if (mem.isSubClass(aClass, parentClassName, ipAddress, sessionId)){
                    for (String possibleChild : bre.getPossibleChildrenAccordingToModels().get(aClass)){
                        List<ClassMetadataLight> subClasses = mem.getSubClassesLight(possibleChild, false, true, ipAddress, sessionId);
                        for (ClassMetadataLight subClass : subClasses)
                            res.add(new ClassInfoLight(subClass, new Validator[0]));
                    }
                    break;
                }
            }
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
        return res;
    }
        
    @Override
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(String className, boolean recursive, String ipAddress, String sessionId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> res = new ArrayList<ClassInfoLight>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive, ipAddress, sessionId)){
                res.add(new ClassInfoLight(cil, new Validator[]{}));
            }
            return res;

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.addPossibleChildren(parentClassId, possibleChildren, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.addPossibleChildren(parentClassName, possibleChildren, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved, String ipAddress, String sessionId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createListTypeItem(String className, String name, String displayName, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            return aem.createListTypeItem(className, name, displayName, ipAddress ,sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            aem.deleteListTypeItem(className, oid, realeaseRelationships, ipAddress, sessionId);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getListTypeItems(String className, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(className, ipAddress, sessionId);
            RemoteObjectLight[] res = new RemoteObjectLight[listTypeItems.size()];
            for (int i = 0; i < res.length; i++){
                res[i] = new RemoteObjectLight(listTypeItems.get(i));
            }
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public byte[] getClassHierarchy(boolean showAll, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try{
            return aem.getClassHierachy(showAll, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteSession createSession(String user, String password, String IPAddress)
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            Session newSession = aem.createSession(user, password, IPAddress);
            return new RemoteSession(newSession.getToken(), newSession.getUser());
        } catch (Exception ex) {
        Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        } 
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws ServerSideException, NotAuthorizedException {
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            aem.closeSession(sessionId, remoteAddress);
        } catch (org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public UserInfo getUserInSession(String sessionId){
//        Session aSession = sessions.get(sessionId);
//        if (aSession == null){
            return null;
//        }
//        return new UserInfo(aSession.getUser());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteObjectLight[] getObjectChildren(long oid, long objectClassId, int maxResults, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(oid, objectClassId, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSiblings(className, oid, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject[] getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject getObject(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getObject(objectClass, oid, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObjectLight(bem.getObjectLight(objectClass, oid, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject getParent(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getParent(objectClass, oid, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getParents(String objectClass, long oid, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getParents(objectClass, oid, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectSpecialRelationships getSpecialAttributes(String objectClass, long oid, String ipAddress, String sessionId)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            HashMap<String, List<RemoteBusinessObjectLight>> relationships = bem.getSpecialAttributes(objectClass, oid, ipAddress, sessionId);
            RemoteObjectSpecialRelationships res = new RemoteObjectSpecialRelationships(relationships);

            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getParentOfClass(objectClass, oid, parentClass, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getSpecialAttribute(String objectClass, long objectId, String attributeName, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(objectClass, objectId, attributeName, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectSpecialChildren (String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(objectClass, objectId, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[][] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            return bem.createObject(className, parentClassName, parentOid,attributes, template, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[][] attributeValues, long template, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            return bem.createSpecialObject(className, parentClassName, parentOid,attributes, template, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public void deleteObjects(String[] classNames, long[] oids, boolean releaseRelationships, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (classNames.length != oids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try {
            HashMap<String,long[]> objects = new HashMap<String, long[]>();
            for (int i = 0; i< classNames.length;i++){
                if (objects.get(classNames[i]) == null)
                    objects.put(classNames[i], new long[]{oids[i]});
            }

            bem.deleteObjects(objects, releaseRelationships, ipAddress, sessionId);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try {
            HashMap<String,long[]> objects = new HashMap<String, long[]>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
            }

            bem.moveObjects(targetClass, targetOid, objects, ipAddress, sessionId);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long[] copyObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, boolean recursive, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try {
            HashMap<String,long[]> objects = new HashMap<String, long[]>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new long[]{objectOids[i]});
            }

            return bem.copyObjects(targetClass, targetOid, objects, recursive, ipAddress, sessionId);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, long oid, String[] attributeNames, String[][] attributeValues, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            bem.updateObject(className, oid,attributes, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectsOfClassLight(className, maxResults, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfoLight[] getInstanceableListTypes(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        try {
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes(ipAddress, sessionId);
            ClassInfoLight[] res = new ClassInfoLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new ClassInfoLight(instanceableListTypes.get(i), new Validator[0]);
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    /**
     * Models
     */
    //Physical connections
    @Override
    public void connectMirrorPort(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        
        if (aObjectId == bObjectId)
            throw new ServerSideException(Level.INFO, "A port can not be mirror to itself");

        try {
            if (!mem.isSubClass("GenericPort", aObjectClass, ipAddress, sessionId))
                throw new ServerSideException(Level.WARNING, String.format("Object %s [%s] is not a port", aObjectId, aObjectClass));
            if (!mem.isSubClass("GenericPort", bObjectClass, ipAddress, sessionId))
                throw new ServerSideException(Level.WARNING, String.format("Object %s [%s] is not a port", bObjectId, bObjectClass));
            
            if (bem.hasSpecialRelationship(aObjectClass, aObjectId, "mirror", 1, ipAddress, sessionId))
                throw new ServerSideException(Level.INFO, String.format("Object %s [%s] already has a mirror port", aObjectId, aObjectClass));
            
            if (bem.hasSpecialRelationship(bObjectClass, bObjectId, "mirror", 1, ipAddress, sessionId))
                throw new ServerSideException(Level.INFO, String.format("Object %s [%s] already has a mirror port", bObjectId, bObjectClass));
            
            bem.createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, "mirror", ipAddress, sessionId);
            
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public void releaseMirrorPort(String objectClass, long objectId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        
        try {
            if (!mem.isSubClass("GenericPort", objectClass, ipAddress, sessionId))
                throw new ServerSideException(Level.WARNING, String.format("Object %s [%s] is not a port", objectId, objectClass));
                        
            bem.releaseSpecialRelationship(objectClass, objectId, -1, "mirror", ipAddress, sessionId);
            
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createPhysicalConnection(String aObjectClass, long aObjectId,
            String bObjectClass, long bObjectId, String parentClass, long parentId,
            String[] attributeNames, String[][] attributeValues, String connectionClass, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
        for (int i = 0; i < attributeValues.length; i++)
            attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

        long newConnectionId = -1;
        try {
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, "Class %s is not subclass of GenericPhysicalConnection");

            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (mem.isSubClass("GenericPhysicalLink", connectionClass, ipAddress, sessionId)){
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, "endpointA", ipAddress, sessionId).isEmpty())
                    throw new ServerSideException(Level.INFO, String.format("The selected endpoint %s [%s] is already connected", aObjectClass, aObjectId));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, "endpointB", ipAddress, sessionId).isEmpty())
                    throw new ServerSideException(Level.INFO, String.format("The selected endpoint %s [%s] is already connected", bObjectClass, bObjectId));
            }

            newConnectionId = bem.createSpecialObject(connectionClass, parentClass, parentId, attributes, 0, ipAddress, sessionId);
            bem.createSpecialRelationship(connectionClass, newConnectionId, aObjectClass, aObjectId, "endpointA", ipAddress, sessionId);
            bem.createSpecialRelationship(connectionClass, newConnectionId, bObjectClass, bObjectId, "endpointB", ipAddress, sessionId);
            return newConnectionId;
        } catch (Exception e) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1)
                deleteObjects(new String[]{connectionClass}, new long[]{newConnectionId}, true, ipAddress, sessionId);

            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }
    
    @Override
    public long[] createBulkPhysicalConnections(String connectionClass, int numberOfChildren,
            String parentClass, long parentId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a physical connection", connectionClass));
            
            return bem.createBulkSpecialObjects(connectionClass, numberOfChildren, parentClass, parentId, ipAddress, sessionId);

        } catch (Exception ex) {

            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getConnectionEndpoints(String connectionClass, long connectionId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a physical connection", connectionClass));
            List<RemoteBusinessObjectLight> endpointA = bem.getSpecialAttribute(connectionClass, connectionId, "endpointA", ipAddress, sessionId);
            List<RemoteBusinessObjectLight> endpointB = bem.getSpecialAttribute(connectionClass, connectionId, "endpointB", ipAddress, sessionId);
            return new RemoteObjectLight[]{endpointA.isEmpty() ? null : new RemoteObjectLight(endpointA.get(0)), 
                                            endpointB.isEmpty() ? null : new RemoteObjectLight(endpointB.get(0))};

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public void connectPhysicalLinks(String[] sideAClassNames, Long[] sideAIds, 
                String[] linksClassNames, Long[] linksIds, String[] sideBClassNames, 
                Long[] sideBIds, String ipAddress, String sessionId) throws ServerSideException{

        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            for (int i = 0; i < sideAClassNames.length; i++){
                
                if (linksClassNames[i] != null && !mem.isSubClass("GenericPhysicalLink", linksClassNames[i], ipAddress, sessionId))
                    throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a physical link", linksClassNames[i]));
                if (sideAClassNames[i] != null && !mem.isSubClass("GenericPort", sideAClassNames[i], ipAddress, sessionId))
                    throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a port", sideAClassNames[i]));
                if (sideBClassNames[i] != null && !mem.isSubClass("GenericPort", sideBClassNames[i], ipAddress, sessionId))
                    throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a port", sideBClassNames[i]));
                
                if (sideAIds[i] == sideBIds[i])
                    throw new ServerSideException(Level.SEVERE, "Can not connect a port to itself");
                
                List<RemoteBusinessObjectLight> aEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointA", ipAddress, sessionId);
                List<RemoteBusinessObjectLight> bEndpointList = bem.getSpecialAttribute(linksClassNames[i], linksIds[i], "endpointB", ipAddress, sessionId);
                
                if (!aEndpointList.isEmpty()){
                    if (Long.valueOf(aEndpointList.get(0).getId()) == sideAIds[i] || Long.valueOf(aEndpointList.get(0).getId()) == sideBIds[i])
                        throw new ServerSideException(Level.INFO, "The link is already related to at lest one of the endpoints");
                }
                
                if (!bEndpointList.isEmpty()){
                    if (Long.valueOf(bEndpointList.get(0).getId()) == sideAIds[i] || Long.valueOf(bEndpointList.get(0).getId()) == sideBIds[i])
                        throw new ServerSideException(Level.INFO, "The link is already related to at lest one of the endpoints");
                }
                
                if (sideAIds[i] != null && sideAClassNames[i] != null){
                    if (!bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointA", ipAddress, sessionId).isEmpty() || 
                        !bem.getSpecialAttribute(sideAClassNames[i], sideAIds[i], "endpointB", ipAddress, sessionId).isEmpty())
                        throw new ServerSideException(Level.INFO, String.format("The selected endpoint %s [%s] is already connected", sideAClassNames[i], sideAIds[i]));
                    
                    if (aEndpointList.isEmpty())
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideAClassNames[i], sideAIds[i], "endpointA", ipAddress, sessionId);
                    else
                        throw new ServerSideException(Level.INFO, String.format("Link %s [%s] already has an aEndpoint", linksIds[i], linksClassNames[i]));
                }
                if (sideBIds[i] != null && sideBClassNames[i] != null){
                    if (!bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointB", ipAddress, sessionId).isEmpty() || 
                        !bem.getSpecialAttribute(sideBClassNames[i], sideBIds[i], "endpointA", ipAddress, sessionId).isEmpty())
                        throw new ServerSideException(Level.INFO, String.format("The selected endpoint %s [%s] is already connected", sideBClassNames[i], sideBIds[i]));
                    
                    if (bEndpointList.isEmpty())
                        bem.createSpecialRelationship(linksClassNames[i], linksIds[i], sideBClassNames[i], sideBIds[i], "endpointB", ipAddress, sessionId);
                    else
                        throw new ServerSideException(Level.INFO, String.format("Link %s [%s] already has a bEndpoint", linksIds[i], linksClassNames[i]));
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getPhysicalPath(String objectClassName, long oid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericPort", objectClassName, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a port", objectClassName));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getPhysicalPath(objectClassName, oid, ipAddress, sessionId)); 

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
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
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericService", serviceClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a service", serviceClass));
            bem.createSpecialRelationship(serviceClass, serviceId, objectClass, objectId, "uses", ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public void releaseObjectFromService(String serviceClass, long serviceId, long otherObjectId, String ipAddress, String sessionId) 
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            bem.releaseSpecialRelationship(serviceClass, serviceId, otherObjectId, "uses", ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getServiceResources(String serviceClass, long serviceId, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericService", serviceClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a service", serviceClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getSpecialAttribute(serviceClass, serviceId, "uses", ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createService(String serviceClass, String customerClass, 
            long customerId, String[] attributes, String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericCustomer", customerClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a customer", customerClass));
            if (!mem.isSubClass("GenericService", serviceClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a customer", serviceClass));
            
            return bem.createSpecialObject(serviceClass, customerClass, customerId, null, 0, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createCustomer(String customerClass, String[] attributes, 
            String[] attributeValues, String ipAddress, String sessionId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericCustomer", customerClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a customer", customerClass));
            
            return bem.createSpecialObject(customerClass, null, -1, null, 0, ipAddress, sessionId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObjectLight[] getServices(String customerClass, long customerId, String ipAddress, String sessionId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            if (!mem.isSubClass("GenericCustomer", customerClass, ipAddress, sessionId))
                throw new ServerSideException(Level.SEVERE, String.format("Class %s is not a customer", customerClass));
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectSpecialChildren(customerClass, customerId, ipAddress, sessionId));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    @Override
    public UserInfo[] getUsers(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<UserProfile> users = aem.getUsers(ipAddress, sessionId);

            UserInfo[] usersInfo = new UserInfo[users.size()];
            int i=0;
            for (UserProfile user: users)  {
                usersInfo[i]=(new UserInfo(user));
                i++;
            }
            return usersInfo;
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public GroupInfo[] getGroups(String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<GroupProfile> groups = aem.getGroups(ipAddress, sessionId);

            GroupInfo [] userGroupInfo = new GroupInfo[groups.size()];
            int i=0;
            for (GroupProfile group : groups) {
               userGroupInfo[i] = new GroupInfo(group);
               i++;
            }
            return userGroupInfo;

        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, 
    String firstName, String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, privileges, groups, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createGroup(String groupName, String description, long[] privileges, long[] users, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createGroup(groupName, description, privileges, users);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createUser(String userName, String password, String firstName, 
    String lastName, boolean enabled, long[] privileges, long[] groups, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createUser(userName, password, firstName, lastName, enabled,  privileges, groups);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setGroupProperties(long oid, String groupName, String description, 
        long[] privileges, long[] users, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setGroupProperties(oid, groupName, description, privileges, users, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(long[] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteUsers(oids, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteGroups(long[] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteGroups(oids, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createObjectRelatedView(long objectId, String objectClass, String name, 
        String description, int viewType, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createObjectRelatedView(objectId, objectClass, name, description, viewType, structure, background, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createGeneralView(viewType, name, description, structure, background, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ViewInfo getObjectRelatedView(long oid, String objectClass, long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId, ipAddress, sessionId);
            if (myView == null)
                return null;
            ViewInfo res = new ViewInfo(myView);
            res.setBackground(myView.getBackground());
            return res;
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getObjectRelatedViews(long oid, String objectClass, int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<ViewObjectLight> views = aem.getObjectRelatedViews(oid, objectClass, limit, ipAddress, sessionId);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            int i = 0;
            for (ViewObjectLight view : views){
                res[i] = new ViewInfoLight(view);
                i++;
            }
            return res;
        }catch(Exception e){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public ViewInfo getGeneralView(long viewId, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return new ViewInfo(aem.getGeneralView(viewId, ipAddress, sessionId));
        }catch(Exception e){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getGeneralViews(int viewType, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<ViewObjectLight> views = aem.getGeneralViews(viewType, limit, ipAddress, sessionId);
            ViewInfoLight[] res = new ViewInfoLight[views.size()];
            for (int i = 0; i < views.size(); i++)
                res[i] = new ViewInfoLight(views.get(i));
           return res;
        }catch(Exception e){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }

    /**
     * Saves a view
     * @param oid
     * @param objectClass
     * @param viewType
     * @param structure
     * @param background If null, the background is removed. If a 0-sized array, it's unmodified
     * @throws ServerSideException
     */
    @Override
    public void updateObjectRelatedView(long objectOid, String objectClass, 
        long viewId, String viewName, String viewDescription, byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background, ipAddress, sessionId);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, 
        byte[] structure, byte[] background, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.updateGeneralView(viewId, viewName, viewDescription, structure, background, ipAddress, sessionId);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public void deleteGeneralView(long [] oids, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteGeneralViews(oids, ipAddress, sessionId);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createQuery(queryName, ownerOid, queryStructure, description, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName, long ownerOid, byte[] queryStructure, 
        String description, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteQuery(queryOid, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<CompactQuery> queries = aem.getQueries(showPublic, ipAddress, sessionId);
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
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteQuery getQuery(long queryOid, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return new RemoteQuery(aem.getQuery(queryOid, ipAddress, sessionId));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ResultRecord[] executeQuery(TransientQuery query, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<org.kuwaiba.apis.persistence.application.ResultRecord> resultRecordList = aem.executeQuery(transientQuerytoExtendedQuery(query), ipAddress, sessionId);

            ResultRecord[] resultArray = new ResultRecord[resultRecordList.size()];
            
            for (int i=0;resultRecordList.size() >i; i++)
            {
                RemoteObjectLight rol = new RemoteObjectLight(resultRecordList.get(i).getId(), resultRecordList.get(i).getName(), resultRecordList.get(i).getClassName());
                resultArray[i] = new ResultRecord(rol, (ArrayList<String>) resultRecordList.get(i).getExtraColumns());
            }

            return resultArray;
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    //Pools
    @Override
    public long createPool(long parentId, String name, String description, String instancesOfClass, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createPool(parentId, name, description, instancesOfClass, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String attributeNames[], String attributeValues[][], long templateId, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createPoolItem(poolId, className, attributeNames, attributeValues, templateId, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deletePools(long[] ids, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deletePools(ids, ipAddress, sessionId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPools(int limit, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPools(limit, ipAddress, sessionId));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPoolItems(long poolId, int limit, String ipAddress, String sessionId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPoolItems(poolId, limit, ipAddress, sessionId));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getBusinessObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            List<ActivityLogEntry> entries = aem.getBusinessObjectAuditTrail(objectClass, objectId, limit, ipAddress, sessionId);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.INFO, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ApplicationLogEntry[] getApplicationObjectAuditTrail(String objectClass, long objectId, int limit, String ipAddress, String sessionId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public ApplicationLogEntry[] getGeneralActivityAuditTrail(int page, int limit, String ipAddress, String sessionId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            List<ActivityLogEntry> entries = aem.getGeneralActivityAuditTrail(page, limit, ipAddress, sessionId);
            ApplicationLogEntry[] res = new ApplicationLogEntry[entries.size()];
            for (int i = 0; i< entries.size(); i++)
                res[i] = new ApplicationLogEntry(entries.get(i));
            
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.INFO, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/Bulkupload data methods. Click on the + sign on the left to edit the code.">
    @Override
    public String bulkUpload(byte[] choosenFile, long userId) throws ServerSideException{
        if (ssm == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return ssm.loadDataFromFile(choosenFile, userId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public byte[] downloadLog(String fileName) throws ServerSideException{
        if (ssm == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return ssm.downloadLog(fileName);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public byte[] downloadErrors(String fileName) throws ServerSideException{
        if (ssm == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return ssm.downloadErrors(fileName);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper methods. Click on the + sign on the left to edit the code.">
    protected void connect(){
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            bem = (BusinessEntityManagerRemote) registry.lookup(BusinessEntityManagerRemote.REFERENCE_BEM);
            aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
        }catch(Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
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
        List<ExtendedQuery> listeq = new ArrayList<ExtendedQuery>();

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
    // </editor-fold>
}
