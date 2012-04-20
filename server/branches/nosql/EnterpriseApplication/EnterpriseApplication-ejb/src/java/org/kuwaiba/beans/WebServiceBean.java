/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.beans;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.exceptions.InvalidSessionException;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.beans.sessions.Session;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.Validator;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.CategoryInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;

/**
 * Session bean to implement the logic for webservice calls
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class WebServiceBean implements WebServiceBeanRemote {

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

    public WebServiceBean() {
        super();
        sessions = new HashMap<String, Session>();
        try{
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            bem = (BusinessEntityManagerRemote) registry.lookup(BusinessEntityManagerRemote.REFERENCE_BEM);
            aem = (ApplicationEntityManagerRemote) registry.lookup(ApplicationEntityManagerRemote.REFERENCE_AEM);
        }catch(Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            mem = null;
            bem = null;
            aem = null;
        }
    }


    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public Long createClass(ClassInfo classDefinition) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try{

            ClassMetadata cm = new ClassMetadata();

            cm.setName(classDefinition.getClassName());
            cm.setDisplayName(classDefinition.getDisplayName());
            cm.setDescription(classDefinition.getDescription());
            cm.setParentClassName(classDefinition.getParentClassName());
            cm.setAbstractClass(classDefinition.getAbstractClass());
            //TODO decode flags, set category
            //cm.setCategory(classDefinition.getCategory());
            cm.setColor(0);
            cm.setCountable(false);
            //cm.setCreationDate(null);
            cm.setIcon(classDefinition.getIcon());
            cm.setSmallIcon(classDefinition.getSmallIcon());
            cm.setCustom(false);
            cm.setDummy(false);

            return mem.createClass(cm);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyValue) throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void setClassPlainAttribute(Long classId, String attributeName,
            String attributeValue) throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.setClassPlainAttribute(classId, attributeName, attributeValue);
            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void setClassIcon(Long classId, String attributeName, byte[] iconImage)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.setClassIcon(classId, attributeName, iconImage);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void deleteClass(String className)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.deleteClass(className);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void deleteClass(Long classId)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.deleteClass(classId);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public ClassInfo getMetadataForClass(String className) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            return new ClassInfo(mem.getClass(className), new Validator[0]);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfo getMetadataForClass(Long classId) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            return new ClassInfo(mem.getClass(classId), new Validator[0]);

         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public List<ClassInfoLight> getLightMetadata(Boolean includeListTypes) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getLightMetadata(includeListTypes);

            for (ClassMetadataLight classMetadataLight : classLightMetadata) 
                cml.add(new ClassInfoLight(classMetadataLight, new Validator[0]));
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public List<ClassInfo> getMetadata(Boolean includeListTypes) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            List<ClassInfo> cml = new ArrayList<ClassInfo>();
            List<ClassMetadata> classMetadataList = mem.getMetadata(includeListTypes);

            for (ClassMetadata classMetadata : classMetadataList)
                cml.add(new ClassInfo(classMetadata, new Validator[0]));
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void moveClass(String classToMoveName, String targetParentName)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.moveClass(classToMoveName, targetParentName);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void moveClass(Long classToMoveId, Long targetParentId)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.moveClass(classToMoveId, targetParentId);
            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void addAttribute(String className, AttributeInfo attributeDefinition)
            throws ServerSideException
    {

        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            AttributeMetadata atm = new AttributeMetadata();

            atm.setName(attributeDefinition.getName());
            atm.setDisplayName(attributeDefinition.getDisplayName());
            atm.setDescription(attributeDefinition.getDescription());
            atm.setMapping(attributeDefinition.getMapping());
            atm.setReadOnly(attributeDefinition.isReadOnly());
            atm.setType(attributeDefinition.getType());
            atm.setUnique(attributeDefinition.isUnique());
            atm.setVisible(attributeDefinition.isVisible());

            mem.addAttribute(className, atm);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void addAttribute(Long classId, AttributeInfo attributeDefinition)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            AttributeMetadata atm = new AttributeMetadata();

            atm.setName(attributeDefinition.getName());
            atm.setDisplayName(attributeDefinition.getDisplayName());
            atm.setDescription(attributeDefinition.getDescription());
            atm.setMapping(attributeDefinition.getMapping());
            atm.setReadOnly(attributeDefinition.isReadOnly());
            atm.setType(attributeDefinition.getType());
            atm.setUnique(attributeDefinition.isUnique());
            atm.setVisible(attributeDefinition.isVisible());

            mem.addAttribute(classId, atm);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void changeClassDefinition(ClassInfo newClassDefinition)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            ClassMetadata cm = new ClassMetadata();

            cm.setName(newClassDefinition.getClassName());
            cm.setDisplayName(newClassDefinition.getDisplayName());
            cm.setDescription(newClassDefinition.getDescription());
            cm.setParentClassName(newClassDefinition.getParentClassName());
            cm.setAbstractClass(newClassDefinition.getAbstractClass());
            //TODO decode flags, set category
            //cm.setCategory(classDefinition.getCategory());
            cm.setColor(0);
            cm.setCountable(false);
            //cm.setCreationDate(null);
            cm.setIcon(newClassDefinition.getIcon());
            cm.setSmallIcon(newClassDefinition.getSmallIcon());
            cm.setCustom(false);
            cm.setDummy(false);

            mem.changeClassDefinition(cm);

         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            AttributeMetadata atrbMtdt = mem.getAttribute(className, attributeName);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription(),
                                                       atrbMtdt.getMapping());
            return atrbInfo;
         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public AttributeInfo getAttribute(Long classId, String attributeName) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            AttributeMetadata atrbMtdt = mem.getAttribute(classId, attributeName);

            AttributeInfo atrbInfo = new AttributeInfo(atrbMtdt.getName(),
                                                       atrbMtdt.getDisplayName(),
                                                       atrbMtdt.getType(),
                                                       atrbMtdt.isAdministrative(),
                                                       atrbMtdt.isVisible(),
                                                       atrbMtdt.getDescription(),
                                                       atrbMtdt.getMapping());
            return atrbInfo;

         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void changeAttributeDefinition(Long ClassId, AttributeInfo newAttributeDefinition)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            AttributeMetadata attrMtdt = new AttributeMetadata();

            attrMtdt.setName(newAttributeDefinition.getName());
            attrMtdt.setDisplayName(newAttributeDefinition.getDisplayName());
            attrMtdt.setDescription(newAttributeDefinition.getDescription());
            attrMtdt.setType(newAttributeDefinition.getType());
            attrMtdt.setMapping(newAttributeDefinition.getMapping());
            attrMtdt.setAdministrative(newAttributeDefinition.isAdministrative());
            attrMtdt.setUnique(newAttributeDefinition.isUnique());
            attrMtdt.setVisible(newAttributeDefinition.isVisible());
            attrMtdt.setReadOnly(newAttributeDefinition.isReadOnly());

            mem.changeAttributeDefinition(ClassId, attrMtdt);
            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.deleteAttribute(className, attributeName);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void deleteAttribute(Long classId, String attributeName)
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.deleteAttribute(classId, attributeName);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public Long createCategory(CategoryInfo categoryDefinition) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            return mem.createCategory(ctgrMtdt);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public CategoryInfo getCategory(String categoryName) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();
            ctgrMtdt = mem.getCategory(categoryName);

            CategoryInfo ctgrInfo = new CategoryInfo(ctgrMtdt.getName(),
                                                     ctgrMtdt.getDisplayName(),
                                                     ctgrMtdt.getDescription(),
                                                     ctgrMtdt.getCreationDate());
            return ctgrInfo;
         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public CategoryInfo getCategory(Integer categoryId) 
            throws ServerSideException
    {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            CategoryMetadata ctgrMtdt = new CategoryMetadata();
            ctgrMtdt = mem.getCategory(categoryId);

            CategoryInfo ctgrInfo = new CategoryInfo();

            ctgrInfo.setName(ctgrMtdt.getName());
            ctgrInfo.setDisplayName(ctgrMtdt.getDisplayName());
            ctgrInfo.setDescription(ctgrMtdt.getDescription());
            ctgrInfo.setCreationDate(ctgrMtdt.getCreationDate());

            return ctgrInfo;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void changeCategoryDefinition(CategoryInfo categoryDefinition)
            throws ServerSideException
    {
        try{
            assert mem == null : "Can't reach the Metadata Entity Manager";

            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            mem.changeCategoryDefinition(ctgrMtdt);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName) throws ServerSideException {

        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildren(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }

    }

    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName) throws ServerSideException {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classMetadataList = mem.getPossibleChildrenNoRecursive(parentClassName);

            for (ClassMetadataLight clMtLg : classMetadataList) {
                ClassInfoLight ci =  new ClassInfoLight(clMtLg, new Validator[0]);
                cml.add(ci);
            }
            return cml;

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws ServerSideException {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.addPossibleChildren(parentClassId, _possibleChildren);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public void removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws ServerSideException {
        assert mem == null : "Can't reach the Metadata Entity Manager";
        try
        {
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public Long createListTypeItem(String className, String name, String displayName) throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";
        try
        {
            return bem.createListTypeItem(className, name, displayName);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException {
        assert bem == null : "Can't reach the Business Entity Manager";
        try
        {
            List<RemoteBusinessObjectLight> listTypeItems = bem.getListTypeItems(className);
            RemoteObjectLight[] res = new RemoteObjectLight[listTypeItems.size()];

            for (int i = 0; i < res.length; i++)
                res[i] = new RemoteObjectLight(listTypeItems.get(i));

            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteSession createSession(String user, String password, String IPAddress)
            throws NotAuthorizedException {
        assert aem == null : "Can't reach the Application Entity Manager";
        try {

            for (Session aSession : sessions.values()){
                if (aSession.getUser().getUserName().equals(user))
                    throw new NotAuthorizedException("There's already an active session associated to that user");
            }

            UserProfile currentUser = aem.login(user, password);
            if (currentUser == null)
                throw new NotAuthorizedException("User or password incorrect");
            Session newSession = new Session(currentUser, IPAddress);
            sessions.put(newSession.getToken(), newSession);
            return new RemoteSession(newSession.getToken(), currentUser);

        } catch (RemoteException ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Override
    public void closeSession(String sessionId, String remoteAddress) throws InvalidSessionException {
        Session aSession = sessions.get(sessionId);
        if (aSession == null)
            throw new InvalidSessionException("The provided session ID is not valid");
        if (!aSession.getIpAddress().equals(remoteAddress))
            throw new InvalidSessionException("This IP is not allowed to close the current session");
        sessions.remove(sessionId);
    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws ServerSideException {
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(oid, objectClassId));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(String className, Long oid) 
            throws ServerSideException {
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass, String classToFilter)
            throws ServerSideException {
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public RemoteObjectLight[] getChildrenOfClassLight(Long parentOid, String parentClass, String classToFilter)
            throws ServerSideException {
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public RemoteObject getObjectInfo(String objectClass, Long oid) throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            RemoteObject res = new RemoteObject(bem.getObjectInfo(objectClass, oid));
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";
        try {
            return new RemoteObjectLight(bem.getObjectInfoLight(objectClass, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    @Override
    public Long createObject(String className, String parentClassName, Long parentOid, String[] attributeNames,
            String[] attributeValues, Long template) throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,String> attributes = new HashMap<String, String>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], attributeValues[i]);

            return bem.createObject(className, parentClassName, parentOid,attributes, template);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, Long oid, String[] attributeNames, String[][] attributeValues) throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";
        if (attributeNames.length != attributeValues.length)
            throw new ServerSideException(Level.SEVERE, "Attribute names and attribute values arrays sizes doesn't match");

        try {
            HashMap<String,List<String>> attributes = new HashMap<String, List<String>>();
            for (int i = 0; i < attributeNames.length; i++)
                attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));

            bem.updateObject(className, oid,attributes);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfoLight[] getInstanceableListTypes() throws ServerSideException{
        assert bem == null : "Can't reach the Business Entity Manager";

        try {
            List<ClassMetadataLight> instanceableListTypes = bem.getInstanceableListTypes();
            ClassInfoLight[] res = new ClassInfoLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new ClassInfoLight(instanceableListTypes.get(i), new Validator[0]);
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend");
        }
    }

    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Helper methods. Click on the + sign on the left to edit the code.">

    // </editor-fold>
}
