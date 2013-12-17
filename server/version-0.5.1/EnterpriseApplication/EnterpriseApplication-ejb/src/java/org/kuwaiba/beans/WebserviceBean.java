/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.application.ViewObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.beans.sessions.Session;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.kuwaiba.sync.SyncServicesManager;
import org.kuwaiba.util.Util;
import org.kuwaiba.util.bre.TempBusinessRulesEngine;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.ResultRecord;
import org.kuwaiba.ws.toserialize.application.UserGroupInfo;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.Validator;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
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
     * Hashmap with the current sessions. The key is the username, the value is the respective session object
     */
    private HashMap<String, Session> sessions;
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
        sessions = new HashMap<String, Session>();
        bre = new TempBusinessRulesEngine();
        ssm = new SyncServicesManager();
        connect();
    }


    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public long createClass(ClassInfo classDefinition) throws ServerSideException{
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
    public void deleteClass(String className) throws ServerSideException
    {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteClass(className);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteClass(long classId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteClass(classId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfo getClass(String className) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            ClassMetadata myClass = mem.getClass(className);
            List<Validator> validators = new ArrayList<Validator>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, className)){
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
    public ClassInfo getClass(long classId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            ClassMetadata myClass = mem.getClass(classId);
            List<Validator> validators = new ArrayList<Validator>();
            for (String mapping : bre.getSubclassOfValidators().keySet()){
                if (mem.isSubClass(mapping, myClass.getName())){
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
    public List<ClassInfoLight> getAllClassesLight(boolean includeListTypes) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getAllClassesLight(includeListTypes, false);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName())){
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
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLight(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName())){
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
    public List<ClassInfoLight> getSubClassesLightNoRecursive(String className, boolean includeAbstractClasses, boolean includeSelf) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getSubClassesLightNoRecursive(className, includeAbstractClasses, includeSelf);

            for (ClassMetadataLight classMetadataLight : classLightMetadata){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadataLight.getName())){
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
    public List<ClassInfo> getAllClasses(boolean includeListTypes) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfo> cml = new ArrayList<ClassInfo>();
            List<ClassMetadata> classMetadataList = mem.getAllClasses(includeListTypes, false);

            for (ClassMetadata classMetadata : classMetadataList){
                List<Validator> validators = new ArrayList<Validator>();
                for (String mapping : bre.getSubclassOfValidators().keySet()){
                    if (mem.isSubClass(mapping, classMetadata.getName())){
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
    public void moveClass(String classToMoveName, String targetParentName) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.moveClass(classToMoveName, targetParentName);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveClass(long classToMoveId, long targetParentId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.moveClass(classToMoveId, targetParentId);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void createAttribute(String className, AttributeInfo attributeDefinition) throws ServerSideException {
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

            mem.createAttribute(className, atm);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void createAttribute(long classId, AttributeInfo attributeDefinition) throws ServerSideException {
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
    public void setClassProperties(ClassInfo newClassDefinition) throws ServerSideException {
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
            
            mem.setClassProperties(cm);

         } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName) throws ServerSideException {
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
    public AttributeInfo getAttribute(long classId, long attributeId) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeId);

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
    public void setAttributeProperties(long classId, AttributeInfo newAttributeDefinition) throws ServerSideException {
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

            mem.setAttributeProperties(classId, attrMtdt);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setAttributeProperties(String className, AttributeInfo newAttributeDefinition) throws ServerSideException {
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

            mem.setAttributeProperties(className, attrMtdt);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteAttribute(className, attributeName);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(long classId, String attributeName) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.deleteAttribute(classId, attributeName);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createCategory(CategoryInfo categoryDefinition) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            return mem.createCategory(ctgrMtdt);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public CategoryInfo getCategory(String categoryName) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = mem.getCategory(categoryName);

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
    public CategoryInfo getCategory(long categoryId) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = mem.getCategory(categoryId);

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
    public void setCategoryProperties(CategoryInfo categoryDefinition) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            mem.setCategoryProperties(ctgrMtdt);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName) throws ServerSideException {
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
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

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
    public List<ClassInfoLight> getUpstreamContainmentHierarchy(String className, boolean recursive) throws ServerSideException{
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<ClassInfoLight> res = new ArrayList<ClassInfoLight>();
            for (ClassMetadataLight cil : mem.getUpstreamContainmentHierarchy(className, recursive)){
                res.add(new ClassInfoLight(cil, new Validator[]{}));
            }
            return res;

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(long parentClassId, long[] possibleChildren) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.addPossibleChildren(parentClassId, possibleChildren);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(String parentClassName, String[] possibleChildren) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.addPossibleChildren(parentClassName, possibleChildren);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildren(long parentClassId, long[] childrenToBeRemoved) throws ServerSideException {
        if (mem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createListTypeItem(String className, String name, String displayName) throws ServerSideException{
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            return aem.createListTypeItem(className, name, displayName);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, long oid, boolean realeaseRelationships) throws ServerSideException {
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            aem.deleteListTypeItem(className, oid, realeaseRelationships);

        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException {
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {
            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
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
    
    /**
     * Get the whole class hierarchy as an XML document
     * @param showAll
     * @return The resulting XML document
     * @throws ServerSideException
     */
    @Override
    public byte[] getClassHierarchy(boolean showAll) throws ServerSideException{
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try{
            return aem.getClassHierachy(showAll);
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
        if (aem == null){
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        }
        try {

            UserProfile currentUser = aem.login(user, password);
            if (currentUser == null){
                throw new ServerSideException(Level.INFO,"User or password incorrect");
            }
            for (Session aSession : sessions.values()){
                if (aSession.getUser().getUserName().equals(user)){
                    Logger.getLogger(WebserviceBean.class.getName()).log(Level.INFO, Util.formatString("An existing session for user %1s has been dropped", aSession.getUser().getUserName()));
                    sessions.remove(aSession.getToken());
                    break;
                }
            }

            Session newSession = new Session(currentUser, IPAddress);
            sessions.put(newSession.getToken(), newSession);
            return new RemoteSession(newSession.getToken(), currentUser);

        } catch (RemoteException ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            return null;
        }
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws NotAuthorizedException {
        Session aSession = sessions.get(sessionId);
        if (aSession == null)
            throw new NotAuthorizedException("The session token provided is not valid");
        if (!aSession.getIpAddress().equals(remoteAddress))
            throw new NotAuthorizedException("This IP is not allowed to close the current session");
        sessions.remove(sessionId);
    }
    
    @Override
    public UserInfo getUserInSession(String sessionId){
        Session aSession = sessions.get(sessionId);
        if (aSession == null){
            return null;
        }
        return new UserInfo(aSession.getUser());
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteObjectLight[] getObjectChildren(long oid, long objectClassId, int maxResults) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(oid, objectClassId, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(String className, long oid, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject[] getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject getObject(String objectClass, long oid) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getObject(objectClass, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getObjectLight(String objectClass, long oid) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObjectLight(bem.getObjectLight(objectClass, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject getParent(String objectClass, long oid) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getParent(objectClass, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public RemoteObject getParentOfClass(String objectClass, long oid, String parentClass) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObject(bem.getParentOfClass(objectClass, oid, parentClass));
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public String[] getSpecialAttribute(String objectClass, long objectId, String attributeName) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return bem.getSpecialAttribute(objectClass, objectId, attributeName).toArray(new String[0]);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createObject(String className, String parentClassName, long parentOid, String[] attributeNames,
            String[][] attributeValues, long template) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            return bem.createObject(className, parentClassName, parentOid,attributes, template);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteObjects(String[] classNames, long[] oids, boolean releaseRelationships) throws ServerSideException{
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

            bem.deleteObjects(objects, releaseRelationships);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids) throws ServerSideException {
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

            bem.moveObjects(targetClass, targetOid, objects);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long[] copyObjects(String targetClass, long targetOid, String[] objectClasses, long[] objectOids, boolean recursive) throws ServerSideException {
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

            return bem.copyObjects(targetClass, targetOid, objects, recursive);
        }catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, long oid, String[] attributeNames, String[][] attributeValues) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            bem.updateObject(className, oid,attributes);
        } catch (Exception ex) {
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfoLight[] getInstanceableListTypes() throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        try {
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
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
    public long createPhysicalConnection(String aObjectClass, long aObjectId,
            String bObjectClass, long bObjectId, String parentClass, long parentId,
            String[] attributeNames, String[][] attributeValues, String connectionClass) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();
        for (int i = 0; i < attributeValues.length; i++)
            attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

        long newConnectionId = -1;
        try {
            if (!mem.isSubClass("GenericPhysicalConnection", connectionClass))
                throw new ServerSideException(Level.SEVERE, "Class %1s is not subclass of GenericPhysicalConnection");

            String aSideString, bSideString;
            boolean isLink = false;

            if (mem.isSubClass("GenericPhysicalContainer", connectionClass)){
                aSideString = "nodeA";
                bSideString = "nodeB";
            }else{
                aSideString = "endpointA";
                bSideString = "endpointB";
                isLink = true;
            }

            //Check if the endpoints are already connected, but only if the connection is a link (the endpoints are ports)
            if (isLink){
                if (!bem.getSpecialAttribute(aObjectClass, aObjectId, aSideString).isEmpty())
                    throw new ServerSideException(Level.INFO, Util.formatString("The selected endpoint (%1s, %2s) is already connected", aObjectClass, aObjectId));

                if (!bem.getSpecialAttribute(bObjectClass, bObjectId, bSideString).isEmpty())
                    throw new ServerSideException(Level.INFO, Util.formatString("The selected endpoint (%1s, %2s) is already connected", bObjectClass, bObjectId));
            }

            newConnectionId = bem.createSpecialObject(connectionClass, parentClass, parentId, attributes, 0);
            bem.createSpecialRelationship(aObjectClass, aObjectId, connectionClass, newConnectionId, aSideString);
            bem.createSpecialRelationship(bObjectClass, bObjectId, connectionClass, newConnectionId, bSideString);
            return newConnectionId;
        } catch (Exception ex) {
            //If the new connection was successfully created, but there's a problem creating the relationships,
            //delete the connection and throw an exception
            if (newConnectionId != -1)
                deleteObjects(new String[]{connectionClass}, new long[]{newConnectionId}, true);

            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deletePhysicalConnection(String objectClass, long objectId) throws ServerSideException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Application methods. Click on the + sign on the left to edit the code.">
    @Override
    public UserInfo[] getUsers() throws ServerSideException
    {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<UserProfile> users = aem.getUsers();

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
    public UserGroupInfo[] getGroups() throws ServerSideException
    {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<GroupProfile> groups = aem.getGroups();

            UserGroupInfo [] userGroupInfo = new UserGroupInfo[groups.size()];
            int i=0;
            for (GroupProfile group : groups) {
               userGroupInfo[i] = new UserGroupInfo(group);
               i++;
            }
            return userGroupInfo;

        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(long oid, String userName, String password, String firstName, String lastName, boolean enabled, int[] privileges, long[] groups) throws ServerSideException
    {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, privileges, groups);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }



    @Override
    public long createGroup(String groupName, String description, int[] privileges, long[] users) throws ServerSideException {
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
    public long createUser(String userName, String password, String firstName, String lastName, boolean enabled, int[] privileges, long[] groups) throws ServerSideException {
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
    public void setGroupProperties(long oid, String groupName, String description, int[] privileges, long[] users) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setGroupProperties(oid, groupName, description, privileges, users);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(long[] oids) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteUsers(oids);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteGroups(long[] oids) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteGroups(oids);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createObjectRelatedView(long objectId, String objectClass, String name, String description, int viewType, byte[] structure, byte[] background) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createObjectRelatedView(objectId, objectClass, name, description, viewType, structure, background);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public long createGeneralView(int viewType, String name, String description, byte[] structure, byte[] background) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createGeneralView(viewType, name, description, structure, background);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ViewInfo getObjectRelatedView(long oid, String objectClass, long viewId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            ViewObject myView =  aem.getObjectRelatedView(oid, objectClass, viewId);
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
    public ViewInfoLight[] getObjectRelatedViews(long oid, String objectClass, int viewType, int limit) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<ViewObjectLight> views = aem.getObjectRelatedViews(oid, objectClass, limit);
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
    public ViewInfo getGeneralView(long viewId) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return new ViewInfo(aem.getGeneralView(viewId));
        }catch(Exception e){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, e.getMessage());
            throw new ServerSideException(Level.SEVERE, e.getMessage());
        }
    }

    @Override
    public ViewInfoLight[] getGeneralViews(int viewType, int limit) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<ViewObjectLight> views = aem.getGeneralViews(viewType, limit);
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
    public void updateObjectRelatedView(long objectOid, String objectClass, long viewId, String viewName, String viewDescription, byte[] structure, byte[] background) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.updateObjectRelatedView(objectOid, objectClass, viewId, viewName, viewDescription, structure, background);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public void updateGeneralView(long viewId, String viewName, String viewDescription, byte[] structure, byte[] background) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.updateGeneralView(viewId, viewName, viewDescription, structure, background);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public void deleteGeneralView(long [] oids) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteGeneralViews(oids);
        }catch(InventoryException ie){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ie.getMessage());
        }catch(IOException ioe){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ioe.getMessage());
            throw new ServerSideException(Level.SEVERE, ioe.getMessage());
        }
    }

    @Override
    public long createQuery(String queryName, long ownerOid, byte[] queryStructure,
            String description) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createQuery(queryName, ownerOid, queryStructure, description);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void saveQuery(long queryOid, String queryName,
            long ownerOid, byte[] queryStructure, String description) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(long queryOid) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteQuery(queryOid);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteQueryLight[] getQueries(boolean showPublic) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
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
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteQuery getQuery(long queryOid) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return new RemoteQuery(aem.getQuery(queryOid));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ResultRecord[] executeQuery(TransientQuery query) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            List<org.kuwaiba.apis.persistence.application.ResultRecord> resultRecordList = aem.executeQuery(transientQuerytoExtendedQuery(query));

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
    public long createPool(String name, String description, String instancesOfClass, long owner) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createPool(name, description, instancesOfClass, owner);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String attributeNames[], String attributeValues[][], long templateId) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createPoolItem(poolId, className, attributeNames, attributeValues, templateId);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deletePools(long[] ids) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deletePools(ids);
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPools(int limit) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPools(limit));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getPoolItems(long poolId, int limit) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return RemoteObjectLight.toRemoteObjectLightArray(aem.getPoolItems(poolId, limit));
        }catch (Exception ex){
            Logger.getLogger(WebserviceBean.class.getName()).log(Level.SEVERE, ex.getMessage());
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    /**
     * For now, everyone can do everything unless the credentials are invalid
     * @param methodName
     * @param ipAddress
     * @param sessionId
     */
    @Override
    public Session validateCall(String methodName, String ipAddress, String sessionId) throws NotAuthorizedException{
        Session aSession = sessions.get(sessionId);
        if (aSession == null)
            throw new NotAuthorizedException(Util.formatString("The session token provided to call %1s is not valid",methodName));

        if (!aSession.getIpAddress().equals(ipAddress))
            throw new NotAuthorizedException(Util.formatString("This IP is not allowed to perform this operation: %1s", methodName));
        
        return aSession;
    }

    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Sync/Load data methods. Click on the + sign on the left to edit the code.">
    @Override
    public String loadDataFromFile(byte[] choosenFile, long userId) throws ServerSideException{
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
