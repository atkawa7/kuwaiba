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

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
//import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
//import org.kuwaiba.apis.persistence.metadata.CategoryMetadata;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.exceptions.InvalidSessionException;
import org.kuwaiba.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;
import org.kuwaiba.ws.toserialize.application.RemoteSession;
import org.kuwaiba.ws.toserialize.business.RemoteObject;
import org.kuwaiba.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.ws.toserialize.metadata.ClassInfo;
import org.kuwaiba.ws.toserialize.metadata.AttributeInfo;

/**
 * Session bean to implement the logic for webservice calls
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@Stateless
public class WebServiceBean implements WebServiceBeanRemote {

    private MetadataEntityManagerRemote mem;
    
    private MetadataEntityManagerRemote getMemInstance(){
        try{
            if (mem == null) {
                Registry registry = LocateRegistry.getRegistry("localhost", 1099);
                mem = (MetadataEntityManagerRemote) registry.lookup(MetadataEntityManagerRemote.REFERENCE_MEM);
            }
        }
        catch(Exception ex){
            Logger.getLogger(WebServiceBean.class.getName()).log(Level.SEVERE,
                    ex.getClass().getSimpleName()+": {0}",ex.getMessage()); //NOI18N
            mem = null;
        }
        return mem;
    }


    // <editor-fold defaultstate="collapsed" desc="Metadata methods. Click on the + sign on the left to edit the code.">
    @Override
    public Long createClass(ClassInfo classDefinition) throws Exception
    {
        System.out.println("Creating class");
        
        ClassMetadata cm = new ClassMetadata();

        cm.setName(classDefinition.getClassName());
        cm.setDisplayName(classDefinition.getDisplayName());
        cm.setDescription(classDefinition.getDescription());
        cm.setParentClassName(classDefinition.getParentClassName());
        cm.setAbstractClass(classDefinition.getAbstractClass());
        //TODO decode flags, set catogry
        //cm.setCategory(classDefinition.getCategory());
        cm.setColor(0);
        cm.setCountable(false);
        //cm.setCreationDate(null);
        cm.setIcon(classDefinition.getIcon());
        cm.setSmallIcon(classDefinition.getSmallIcon());
        cm.setCustom(false);
        cm.setDummy(false);
        cm.setLocked(true);

        return getMemInstance().createClass(cm);
    }

    @Override
    public boolean deleteClass(String className) throws Exception
    {
        return getMemInstance().deleteClass(className);
    }

    @Override
    public boolean deleteClass(Long classId) throws Exception {
        return getMemInstance().deleteClass(classId);
    }

    @Override
    public ClassInfo getClass(String className) throws Exception
    {
        ClassInfo ci= new ClassInfo(getMemInstance().getClass(className), 0, false);
        return ci;
    }

    @Override
    public ClassInfo getClass(Long classId) throws Exception
    {
        ClassInfo ci= new ClassInfo(getMemInstance().getClass(classId), 0, false);
        return ci;
    }

    @Override
    public boolean moveClass(String classToMoveName, String targetParentName) throws Exception{
        return getMemInstance().moveClass(classToMoveName, targetParentName);
    }

    @Override
    public boolean moveClass(Long classToMoveId, Long targetParentId) throws Exception{
        return getMemInstance().moveClass(classToMoveId, targetParentId);
    }

    @Override
    public boolean addAttribute(String className, AttributeInfo attributeDefinition) throws Exception{

        AttributeMetadata atm = new AttributeMetadata();

        atm.setName(attributeDefinition.getName());
        atm.setDisplayName(attributeDefinition.getDisplayName());
        atm.setDescription(attributeDefinition.getDescription());
        atm.setMapping(attributeDefinition.getMapping());
        atm.setReadOnly(attributeDefinition.isReadOnly());
        atm.setType(attributeDefinition.getType());
        atm.setUnique(attributeDefinition.isUnique());
        atm.setVisible(attributeDefinition.isVisible());
        
        return getMemInstance().addAttribute(className, atm);
    }

    @Override
    public boolean addAttribute(Long classId, AttributeInfo attributeDefinition) throws Exception{
        AttributeMetadata atm = new AttributeMetadata();

        atm.setName(attributeDefinition.getName());
        atm.setDisplayName(attributeDefinition.getDisplayName());
        atm.setDescription(attributeDefinition.getDescription());
        atm.setMapping(attributeDefinition.getMapping());
        atm.setReadOnly(attributeDefinition.isReadOnly());
        atm.setType(attributeDefinition.getType());
        atm.setUnique(attributeDefinition.isUnique());
        atm.setVisible(attributeDefinition.isVisible());

        return getMemInstance().addAttribute(classId, atm);
    }
    // </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Session methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteSession createSession(String user, String password, String IPAddress) throws NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean closeSession(String sessionId, String remoteAddress) throws InvalidSessionException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }// </editor-fold>


    // <editor-fold defaultstate="collapsed" desc="Business methods. Click on the + sign on the left to edit the code.">
    @Override
    public RemoteObject[] getChildrenOfClass(Long parentOid, String parentClass, String myClass) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RemoteObject getObjectInfo(String objectClass, Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RemoteObjectLight getObjectInfoLight(String objectClass, Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RemoteObject updateObject(String className, Long oid, HashMap<String, String> attributes) {
        throw new UnsupportedOperationException("Not supported yet.");
    }// </editor-fold>


}
