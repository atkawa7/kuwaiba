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

import java.io.FileOutputStream;
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
import org.kuwaiba.apis.persistence.application.CompactQuery;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.GroupProfile;
import org.kuwaiba.apis.persistence.application.UserProfile;
import org.kuwaiba.apis.persistence.application.View;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.exceptions.InvalidSessionException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.beans.sessions.Session;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQuery;
import org.kuwaiba.ws.toserialize.application.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.application.UserGroupInfo;
import org.kuwaiba.ws.toserialize.application.UserInfo;
import org.kuwaiba.ws.toserialize.application.Validator;
import org.kuwaiba.ws.toserialize.application.ViewInfo;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.CategoryInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;
import org.kuwaiba.ws.toserialize.metadata.ClassInfoLight;
import org.kuwaiba.ws.toserialize.application.ResultRecord;

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
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setAttributePropertyValue(Long classId, String attributeName,
            String propertyName, String propertyValue) throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.setAttributePropertyValue(classId, attributeName, propertyName, propertyValue);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setClassPlainAttribute(Long classId, String attributeName,
            String attributeValue) throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.setClassPlainAttribute(classId, attributeName, attributeValue);
            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setClassIcon(Long classId, String attributeName, byte[] iconImage)
            throws ServerSideException
    {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.setClassIcon(classId, attributeName, iconImage);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteClass(String className)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.deleteClass(className);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteClass(Long classId)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.deleteClass(classId);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfo getMetadataForClass(String className) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            return new ClassInfo(mem.getClass(classId), new Validator[0]);

         } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getLightMetadata(Boolean includeListTypes) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();
            List<ClassMetadataLight> classLightMetadata = mem.getLightMetadata(includeListTypes);

            for (ClassMetadataLight classMetadataLight : classLightMetadata) 
                cml.add(new ClassInfoLight(classMetadataLight, new Validator[0]));
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfo> getMetadata(Boolean includeListTypes) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            List<ClassInfo> cml = new ArrayList<ClassInfo>();
            List<ClassMetadata> classMetadataList = mem.getMetadata(includeListTypes);

            for (ClassMetadata classMetadata : classMetadataList)
                cml.add(new ClassInfo(classMetadata, new Validator[0]));
            return cml;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveClass(String classToMoveName, String targetParentName)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.moveClass(classToMoveName, targetParentName);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveClass(Long classToMoveId, Long targetParentId)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.moveClass(classToMoveId, targetParentId);
            
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addAttribute(String className, AttributeInfo attributeDefinition)
            throws ServerSideException
    {

        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addAttribute(Long classId, AttributeInfo attributeDefinition)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void changeClassDefinition(ClassInfo newClassDefinition)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(String className, String attributeName) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public AttributeInfo getAttribute(Long classId, String attributeName) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void changeAttributeDefinition(Long ClassId, AttributeInfo newAttributeDefinition)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(String className, String attributeName)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.deleteAttribute(className, attributeName);
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteAttribute(Long classId, String attributeName)
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.deleteAttribute(classId, attributeName);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long createCategory(CategoryInfo categoryDefinition) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public CategoryInfo getCategory(String categoryName) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public CategoryInfo getCategory(Integer categoryId) 
            throws ServerSideException
    {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void changeCategoryDefinition(CategoryInfo categoryDefinition)
            throws ServerSideException
    {
        try{
         if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

            CategoryMetadata ctgrMtdt = new CategoryMetadata();

            ctgrMtdt.setName(categoryDefinition.getName());
            ctgrMtdt.setDisplayName(categoryDefinition.getDisplayName());
            ctgrMtdt.setDescription(categoryDefinition.getDescription());
            ctgrMtdt.setCreationDate(categoryDefinition.getCreationDate());

            mem.changeCategoryDefinition(ctgrMtdt);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public List<ClassInfoLight> getPossibleChildren(String parentClassName) throws ServerSideException {

        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }

    }

    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(String parentClassName) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.addPossibleChildren(parentClassId, _possibleChildren);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws ServerSideException {
        if (mem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            mem.removePossibleChildren(parentClassId, childrenToBeRemoved);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long createListTypeItem(String className, String name, String displayName) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            return aem.createListTypeItem(className, name, displayName);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteListTypeItem(String className, Long oid, boolean realeaseRelationships) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            aem.deleteListTypeItem(className, oid, realeaseRelationships);

        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getListTypeItems(String className) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try
        {
            List<RemoteBusinessObjectLight> listTypeItems = aem.getListTypeItems(className);
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
            throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        try {

            for (Session aSession : sessions.values()){
                if (aSession.getUser().getUserName().equals(user))
                    throw new ServerSideException(Level.INFO,"There's already an active session associated to that user");
            }

            UserProfile currentUser = aem.login(user, password);
            if (currentUser == null)
                throw new ServerSideException(Level.INFO,"User or password incorrect");
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
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId, int maxResults) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(oid, objectClassId, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(String className, Long oid, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getObjectChildren(className, oid, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass, String classToFilter, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObject.toRemoteObjectArray(bem.getChildrenOfClass(parentOid, parentClass,classToFilter, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight[] getChildrenOfClassLight(Long parentOid, String parentClass, String classToFilter, int maxResults)
            throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return RemoteObjectLight.toRemoteObjectLightArray(bem.getChildrenOfClassLight(parentOid, parentClass,classToFilter, maxResults));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObject getObjectInfo(String objectClass, Long oid) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            RemoteObject res = new RemoteObject(bem.getObjectInfo(objectClass, oid));
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try {
            return new RemoteObjectLight(bem.getObjectInfoLight(objectClass, oid));
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long createObject(String className, String parentClassName, Long parentOid, String[] attributeNames,
            String[] attributeValues, Long template) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
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
    public void deleteObjects(String[] classNames, Long[] oids, boolean releaseRelationships) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (classNames.length != oids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try{
            HashMap<String,List<Long>> objects = new HashMap<String, List<Long>>();
            for (int i = 0; i< classNames.length;i++){
                if (objects.get(classNames[i]) == null)
                    objects.put(classNames[i], new ArrayList<Long>());
                objects.get(classNames[i]).add(oids[i]);
            }

            bem.deleteObjects(objects, releaseRelationships);
        }catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void moveObjects(String targetClass, Long targetOid, String[] objectClasses, Long[] objectOids) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try{
            HashMap<String,List<Long>> objects = new HashMap<String, List<Long>>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new ArrayList<Long>());
                objects.get(objectClasses[i]).add(objectOids[i]);
            }

            bem.moveObjects(targetClass, targetOid, objects);
        }catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long[] copyObjects(String targetClass, Long targetOid, String[] objectClasses, Long[] objectOids, boolean recursive) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        if (objectClasses.length != objectOids.length)
            throw new ServerSideException(Level.SEVERE, "Array sizes do not match");
        try{
            HashMap<String,List<Long>> objects = new HashMap<String, List<Long>>();
            for (int i = 0; i< objectClasses.length;i++){
                if (objects.get(objectClasses[i]) == null)
                    objects.put(objectClasses[i], new ArrayList<Long>());
                objects.get(objectClasses[i]).add(objectOids[i]);
            }

            return bem.copyObjects(targetClass, targetOid, objects, recursive).toArray(new Long[0]);
        }catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, Long oid, String[] attributeNames, String[][] attributeValues) throws ServerSideException{
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
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ClassInfoLight[] getInstanceableListTypes() throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");

        try {
            List<ClassMetadataLight> instanceableListTypes = aem.getInstanceableListTypes();
            ClassInfoLight[] res = new ClassInfoLight[instanceableListTypes.size()];
            for (int i = 0; i < instanceableListTypes.size(); i++)
                res[i] = new ClassInfoLight(instanceableListTypes.get(i), new Validator[0]);
            return res;
        } catch (Exception ex) {
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
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
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setUserProperties(Long oid, String userName, String password, String firstName,
            String lastName, Boolean enabled, Integer[] privileges, Long[] groups) throws ServerSideException
    {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setUserProperties(oid, userName, password, firstName, lastName, enabled, Arrays.asList(privileges), Arrays.asList(groups));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }



    @Override
    public Long createGroup(String groupName, String description, Integer[] privileges, Long[] users) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createGroup(groupName, description, Arrays.asList(privileges), Arrays.asList(users));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long createUser(String userName, String password, String firstName, String lastName, Boolean enabled, Integer[] privileges, Long[] groups) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createUser(userName, password, firstName, lastName, enabled,  Arrays.asList(privileges), Arrays.asList(groups));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void setGroupProperties(Long oid, String groupName, String description, Integer[] privileges, Long[] users) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.setGroupProperties(oid, groupName, description, Arrays.asList(privileges), Arrays.asList(users));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteUsers(Long[] oids) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteUsers(Arrays.asList(oids));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void deleteGroups(Long[] oids) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteGroups(Arrays.asList(oids));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ViewInfo getView(Long oid, String objectClass, Integer viewType) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            View myView =  aem.getView(oid, objectClass, viewType);
            if (myView == null)
                return null;
            return new ViewInfo(myView);
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void saveView(Long oid, String objectClass, int viewType, byte[] structure, byte[] background) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            String backgroundPath = null;
            if (background != null){
                FileOutputStream fos = new FileOutputStream("aaa.jpg");
                fos.write(background);
                fos.close();
                backgroundPath = "aaa.jpg";
            }
            aem.saveView(oid, objectClass, viewType, structure, backgroundPath);
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public Long createQuery(String queryName, Long ownerOid, byte[] queryStructure,
            String description) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return aem.createQuery(queryName, ownerOid, queryStructure, description);
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public void saveQuery(Long queryOid, String queryName,
            Long ownerOid, byte[] queryStructure, String description) throws ServerSideException{
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.saveQuery(queryOid, queryName, ownerOid, queryStructure, description);
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }

    }

    @Override
    public void deleteQuery(Long queryOid) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            aem.deleteQuery(queryOid);
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public RemoteQuery getQuery(Long queryOid) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            return new RemoteQuery(aem.getQuery(queryOid));
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }

    @Override
    public ResultRecord[] executeQuery(TransientQuery query) throws ServerSideException {
        if (aem == null)
            throw new ServerSideException(Level.SEVERE, "Can't reach the backend. Contact your administrator");
        try{
            ExtendedQuery eq = new ExtendedQuery(query.getClassName(), query.getLogicalConnector(),
                    query.getAttributeNames(), query.getVisibleAttributeNames(), query.getAttributeValues(), query.getConditions(), null);

            
            List<org.kuwaiba.apis.persistence.application.ResultRecord> rrList = aem.executeQuery(eq);
            ResultRecord[] rrArray = new ResultRecord[rrList.size()];
            int i = 0;
            for (org.kuwaiba.apis.persistence.application.ResultRecord rrApi : rrList)
            {
                RemoteObjectLight rol = new RemoteObjectLight(rrApi.getId(), rrApi.getName(), rrApi.getClassName());
                rrArray[i] = new ResultRecord(rol, (ArrayList<String>) rrApi.getExtraColumns());
                i++;
            }
            return rrArray;
        }catch (Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE, null, ex);
            throw new ServerSideException(Level.SEVERE, ex.getMessage());
        }
    }



    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Helper methods. Click on the + sign on the left to edit the code.">

    // </editor-fold>
}
