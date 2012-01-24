/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.kuwaiba.businesslogic;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import org.kuwaiba.ws.todeserialize.TransientQuery;
import org.kuwaiba.ws.toserialize.ClassInfo;
import org.kuwaiba.ws.toserialize.ObjectList;
import org.kuwaiba.ws.toserialize.RemoteObject;
import org.kuwaiba.ws.toserialize.RemoteObjectLight;
import org.kuwaiba.core.annotations.RelatableToService;
import org.kuwaiba.core.exceptions.EntityManagerNotAvailableException;
import org.kuwaiba.core.exceptions.InvalidArgumentException;
import org.kuwaiba.core.exceptions.MiscException;
import org.kuwaiba.core.exceptions.NotAuthorizedException;
import org.kuwaiba.core.exceptions.ObjectNotFoundException;
import org.kuwaiba.core.exceptions.OperationNotPermittedException;
import org.kuwaiba.core.exceptions.InvalidSessionException;
import org.kuwaiba.core.exceptions.UnsupportedPropertyException;
import org.kuwaiba.ws.todeserialize.ObjectUpdate;
import org.kuwaiba.ws.toserialize.ClassInfoLight;
import org.kuwaiba.ws.toserialize.RemoteObjectUpdate;
import org.kuwaiba.ws.toserialize.RemoteQuery;
import org.kuwaiba.ws.toserialize.RemoteQueryLight;
import org.kuwaiba.ws.toserialize.ResultRecord;
import org.kuwaiba.ws.toserialize.UserGroupInfo;
import org.kuwaiba.ws.toserialize.UserInfo;
import org.kuwaiba.ws.toserialize.Validator;
import org.kuwaiba.ws.toserialize.ViewInfo;
import org.kuwaiba.entity.session.User;
import org.kuwaiba.entity.session.UserGroup;
import org.kuwaiba.entity.connections.physical.GenericPhysicalConnection;
import org.kuwaiba.entity.connections.physical.containers.GenericPhysicalContainer;
import org.kuwaiba.entity.core.DummyRoot;
import org.kuwaiba.entity.core.InventoryObject;
import org.kuwaiba.entity.core.RootObject;
import org.kuwaiba.entity.core.ViewableObject;
import org.kuwaiba.entity.core.metamodel.AttributeMetadata;
import org.kuwaiba.entity.core.metamodel.ClassMetadata;
import org.kuwaiba.entity.equipment.ports.GenericPort;
import org.kuwaiba.entity.location.GenericPhysicalNode;
import org.kuwaiba.entity.multiple.GenericObjectList;
import org.kuwaiba.entity.session.UserSession;
import org.kuwaiba.entity.views.GenericView;
import org.kuwaiba.entity.views.DefaultView;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import org.kuwaiba.util.AttributeWrapper;
import org.kuwaiba.util.ClassWrapper;
import org.kuwaiba.util.Constants;
import org.kuwaiba.util.HierarchyUtils;
import org.kuwaiba.util.MetadataUtils;

/**
 * Handles the logic of all calls so far
 * @author Charles Edward bedon Cortazar <charles.bedon@zoho.com>
 */
@Stateless
public class BackendBean implements BackendBeanRemote {
    //We use cointainer managed persistance, which means that we don't handle the
    //access to the database directly, but we use a persistemce unit set by the
    //application server. If we'd like to do it manually, we should use an EntityManagerFactory
    @PersistenceContext
    private EntityManager em;
    /**
     * This is a singleton dictionary used to retrieve classes used later
     * in queries
     */
    private static HashMap<String,Class> classIndex;

    @Override
    public Class getClassFor(String className) throws Exception{
        if (em != null){
            if (classIndex == null)
                generateClassIndex();
            Class res = classIndex.get(className);
            if (res == null)
                throw new ClassNotFoundException(className);
            return res;
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Retrieves a given object's children
     * @param oid Parent object oid
     * @param objectClassId Parent object's class oid
     * @return a list of objects or null if an error ocurred
     */
    @Override
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETOBJECTCHILDREN"));
        if (em != null){
           
            ClassMetadata objectClass = em.find(ClassMetadata.class, objectClassId);

            List<Object> result = new ArrayList<Object>();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            Query subQuery=null;

            for (ClassMetadata possibleChildren : objectClass.getPossibleChildren()){
                CriteriaQuery query = criteriaBuilder.createQuery();
                Root entity = query.from(getClassFor(possibleChildren.getName()));
                if (oid == null)
                    query.where(criteriaBuilder.isNull(entity.get("parent")));
                else
                    query.where(criteriaBuilder.equal(entity.get("parent").get("id"),oid));
                subQuery = em.createQuery(query);
                result.addAll(subQuery.getResultList());
            }

            RemoteObjectLight[] validatedResult = new RemoteObjectLight[result.size()];
            int i = 0;
            for (Object child : result) {
                validatedResult[i] = new RemoteObjectLight(child);
                if (child instanceof GenericPort)
                    validatedResult[i].addValidator(new Validator("isConnected",((GenericPort)child).getConnectedConnection() != null)); //NOI18n
                if (child.getClass().isAnnotationPresent(RelatableToService.class))
                    validatedResult[i].addValidator(new Validator("isRelatableToService",true));
                i++;
            }
            return validatedResult;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Retrieves the children of an object whose class would be the one provided
     * @param parentOid
     * @param myClass
     * @return
     */
    @Override
    public RemoteObject[] getChildrenOfClass(Long parentOid, Class myClass) throws Exception {
        if (em !=null){
            Query query = em.createQuery("SELECT x FROM "+myClass.getSimpleName()+" x WHERE x.parent.id="+parentOid);
            List<Object> res = query.getResultList();
            return RemoteObject.toArray(res);
        }else
            throw new EntityManagerNotAvailableException();

    }

    /**
     * Implementation of the idem method exposed by the webservice
     * @param objectClass
     * @param oid
     * @return
     */
    @Override
    public RemoteObject getObjectInfo(Class objectClass,Long oid) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            Object result = em.find(objectClass, oid);           
            if (result==null)
                throw new ObjectNotFoundException(objectClass,oid);
             else
                return new RemoteObject(result);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Implementation of the idem method exposed by the webservice
     * @param objectClass object class
     * @param oid object id to search for
     * @return
     */
    @Override
    public RemoteObjectLight getObjectInfoLight(Class objectClass, Long oid) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            try{
                String displayName = (String)em.createQuery("SELECT x.name FROM "+objectClass.getSimpleName()+" x WHERE x.id="+oid).getSingleResult();
                return new RemoteObjectLight(oid,objectClass.getSimpleName(), displayName);
            }catch (NoResultException nre){
                throw new ObjectNotFoundException(objectClass,oid);
            }
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Updates an object
     * @param _obj
     * @param constraints Only instances of these classes (or subclasses of them) can be modified using this method. Normally,
     * you provide a list of abstract classes (InventoryObject, GenericObjectList), but you can provide a list of
     * non abstract classes, like "User", for example. If no constraints are specified, the default are InventoryObject and GenericObjectList
     * @return
     * @throws Exception ObjectNotFoundException if the oid provided doesn't exist
     */
    @Override
    public RemoteObject updateObject(ObjectUpdate _obj, Class...constraints) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_UPDATEOBJECT"));

        if (em != null){
            RemoteObjectUpdate obj;
            Class myClass = getClassFor(_obj.getClassname());

            if (myClass == null)
                throw new ClassNotFoundException(_obj.getClassname());

            if (constraints.length == 0) //Default constraints are InventoryObject and GenericObjectList
                constraints = new Class[]{InventoryObject.class, GenericObjectList.class};
            //According to the constraints we decide if it's safe to update
            boolean isSafeToUpdate = false;
            for (Class constraint : constraints){
                if (HierarchyUtils.isSubclass(myClass, constraint)){
                    isSafeToUpdate = true;
                    break;
                }
            }

            if (!isSafeToUpdate)
                throw new NotAuthorizedException("You're not allowed to update an object of class "+myClass.getSimpleName()+" through this method");

            obj = new RemoteObjectUpdate(myClass,_obj,em);

            Object myObject = em.find(obj.getObjectClass(), obj.getOid());
            if(myObject == null)
                throw new ObjectNotFoundException(obj.getObjectClass(),obj.getOid());
            for (int i = 0; i< obj.getNewValues().length; i++){
                myObject.getClass().getMethod("set"+MetadataUtils.capitalize(obj.getUpdatedAttributes()[i].getName()),
                      obj.getUpdatedAttributes()[i].getType()).invoke(myObject, obj.getNewValues()[i]);
            }
            em.merge(myObject);
            return new RemoteObject(myObject);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Locks an object
     * @param oid
     * @param objectClass
     * @param value
     * @return
     */
    @Override
    public boolean setObjectLock(Long oid, String objectClass, Boolean value) throws Exception{
        if (em != null){
            String myClassName = objectClass.substring(objectClass.lastIndexOf("."));
            String sentence = "UPDATE x "+myClassName+" x SET isLocked="+value.toString()+" WHERE x.id="+String.valueOf(oid);
            Query query = em.createQuery(sentence);
            if (query.executeUpdate()==0)
                throw new Exception();
            else
                return true;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Gets all classes whose instances can be contained into the given parent class. This method
     * is recursive, so the result include the possible children in children classes
     * @param parentClass
     * @return an array with the list of classes
     */
    @Override
    public List<ClassInfoLight> getPossibleChildren(Class parentClass) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETPOSSIBLECHILDREN"));
        List<ClassInfoLight> res = new ArrayList();
        if (em != null){
            String sentence;
            Class myClass;
            Query query;

            //Now we have to iterate to find the inherited containing capacity
            myClass = parentClass;
            List<ClassMetadata> allPossibleChildren = new ArrayList<ClassMetadata>(); //This list includes the abstract classes
            while (!myClass.equals(InventoryObject.class) && !myClass.equals(Object.class)){
                sentence = "SELECT x.possibleChildren FROM ClassMetadata x WHERE x.name='"+myClass.getSimpleName()+"'";
                query = em.createQuery(sentence);
                List partialResult = query.getResultList();
                if (partialResult!=null)
                    for (Object obj : partialResult)
                        allPossibleChildren.add((ClassMetadata)obj);
                        
                myClass = myClass.getSuperclass();
            }

            //Now we filter the abstract and expand them to normal ones. This code also remove all repeated classes
            //i.e. if a possible children is "GenericBoard" (abstract), this part will find the instanceable subclasses
            //returning something like IPBoard, SDHBoard and so on
            if (classIndex == null)
                generateClassIndex();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cQuery = cb.createQuery();
            Root entity = cQuery.from(ClassMetadata.class);
            Predicate predicate = null;
            for (ClassMetadata cm : allPossibleChildren){
                if (cm.isAbstract()){
                    List<Class> morePossibleChildren = HierarchyUtils.getInstanceableSubclasses(classIndex.get(cm.getName()), classIndex.values());

                    for (Class moreCm : morePossibleChildren){
                        if (predicate == null)
                            predicate = cb.equal(entity.get("name"), moreCm.getSimpleName());
                        else
                            predicate = cb.or(cb.equal(entity.get("name"), moreCm.getSimpleName()), predicate);
                    }
                }
                else
                    res.add(new ClassInfoLight(cm));
            }
            if (predicate != null){
                cQuery.where(predicate);
                List<ClassMetadata> expandedContainment = em.createQuery(cQuery).getResultList();
                for (ClassMetadata expandedClass : expandedContainment){
                    ClassInfoLight newChild = new ClassInfoLight(expandedClass);
                    if (!res.contains(newChild)) //To avoid duplicating entries
                        res.add(newChild);
                }
            }
            return res;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Same as getPossibleChildren but this one only gets the possible children for the given class,
     * this is, subclasses are not included
     * @param parentClass
     * @return The list of possible children
     */
    @Override
    public List<ClassInfoLight> getPossibleChildrenNoRecursive(Class parentClass) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETPOSSIBLECHILDRENNORECURSIVE"));
        List<ClassInfoLight> res = new ArrayList();
         if (em != null){
             String sentence;
             Class myClass;
             Query query;

             myClass = parentClass;
             while (!myClass.equals(InventoryObject.class) && !myClass.equals(Object.class)){
                 sentence = "SELECT x.possibleChildren FROM ClassMetadata x WHERE x.name='"+myClass.getSimpleName()+"'";
                 query = em.createQuery(sentence);
                 List partialResult = query.getResultList();
                 if (partialResult!=null)
                     for (Object obj : partialResult)
                         res.add(new ClassInfoLight((ClassMetadata)obj));
                 myClass = myClass.getSuperclass();
             }
             return res;
          }
          else throw new EntityManagerNotAvailableException();
    }

    /**
     * Helper that gets the possible children for the root node
     * @return
     */
    @Override
    public List<ClassInfoLight> getRootPossibleChildren() throws Exception{
        return getPossibleChildren(DummyRoot.class);
    }

    /**
     * Creates a new object
     * @param objectClass
     * @param parentOid
     * @param template
     * @return the newly created element
     */
    @Override
    public RemoteObjectLight createObject(Class objectClass, Long parentOid, String template) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_CREATEOBJECT"));
        InventoryObject newObject = null;
        if (em != null){
            if (!HierarchyUtils.isSubclass(objectClass, InventoryObject.class))
                throw new InvalidArgumentException("The class provided is not an InventoryObject subclass: "+ objectClass.getSimpleName(), Level.SEVERE);
            newObject = (InventoryObject)objectClass.newInstance();

            if (parentOid != null){
                InventoryObject parentObject = em.find(InventoryObject.class, parentOid);
                if (parentObject == null)
                    throw new ObjectNotFoundException(InventoryObject.class, parentOid);
                newObject.setParent(parentObject);
            }
            em.persist(newObject);
            return new RemoteObjectLight(newObject);
        }
        else
            throw new EntityManagerNotAvailableException();
    }


        /**
     * Retrieves the simplified list of classes. This list won't include either those classes
     * marked as dummy
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return the list of classes
     * @throws Exception EntityManagerNotAvailableException or something unexpected
     */
    @Override
    public List<ClassInfoLight> getLightMetadata(Boolean includeListTypes) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETLIGHTMETADATA"));
        if (em != null){
            String sentence;
            if (includeListTypes)
                sentence = "SELECT x.id, x.name, x.displayName, x.isAbstract,x.isPhysicalNode, x.isPhysicalConnection, x.isPhysicalEndpoint, x.smallIcon FROM ClassMetadata x WHERE x.isDummy = false ORDER BY x.name";
            else
                sentence = "SELECT x.id, x.name, x.displayName, x.isAbstract,x.isPhysicalNode, x.isPhysicalConnection, x.isPhysicalEndpoint, x.smallIcon FROM ClassMetadata x WHERE x.isDummy = false AND x.isListType=false ORDER BY x.name";
            Query q = em.createQuery(sentence);
            List<Object[]> cr = q.getResultList();
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();

            for (Object[] myRecord : cr)
                cml.add(new ClassInfoLight((Long)myRecord[0], (String)myRecord[1], (String)myRecord[2],
                                            (Boolean)myRecord[3], (Boolean)myRecord[4], (Boolean)myRecord[5],
                                            (Boolean)myRecord[6], (byte[])myRecord[7]));
            return cml;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Retrieves all the class metadata except for classes marked as dummy
     * @param includeListTypes boolean to indicate if the list should include the subclasses of
     * GenericObjectList
     * @return An array of classes
     */
    @Override
    public List<ClassInfo> getMetadata(Boolean includeListTypes) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETMETADATA"));
        if (em != null){
            String sentence;
            if (includeListTypes)
                sentence = "SELECT x FROM ClassMetadata x WHERE x.isDummy=false ORDER BY x.name ";
            else
                sentence = "SELECT x FROM ClassMetadata x WHERE x.isDummy=false AND x.isListType=false ORDER BY x.name ";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            List<ClassInfo> cm = new ArrayList<ClassInfo>();
            for (ClassMetadata myClass : cr)
                cm.add(new ClassInfo(myClass));
            return cm;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Gets the metadata of a single class
     * @param className
     * @return the class
     */
    @Override
    public ClassInfo getMetadataForClass(Class className) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETMETADATAFORCLASS"));
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery myQuery = cb.createQuery();
            Root entity = myQuery.from(ClassMetadata.class);
            myQuery.where(cb.equal(entity.get("name"),className.getSimpleName()));

            Query q = em.createQuery(myQuery);
            ClassMetadata res;
            try{
                res = (ClassMetadata)q.getSingleResult();
            }catch(NoResultException nre){
                throw new ClassNotFoundException("ClassMetadata");
            }
            return new ClassInfo(res);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public byte[] getClassHierarchy(Boolean showAll) throws Exception {
        if (em != null){
            if (classIndex == null)
                generateClassIndex();
            List<Class> remainingClasses = new ArrayList<Class>(classIndex.values());
            List<ClassWrapper> roots = new ArrayList<ClassWrapper>();
            
            remainingClasses.remove(RootObject.class);
            roots.add(HierarchyUtils.createTree(RootObject.class, remainingClasses));
            if (!remainingClasses.isEmpty()){
                for (Class anExtraClass : remainingClasses)
                    roots.add(new ClassWrapper(anExtraClass, ClassWrapper.TYPE_OTHER));
            }
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            WAX xmlWriter = new WAX(bas);
            StartTagWAX rootTag = xmlWriter.start("hierarchy");
            rootTag.attr("documentVersion", Constants.CLASSHIERARCHY_DOCUMENT_VERSION);
            rootTag.attr("serverVersion", Constants.SERVER_VERSION);
            rootTag.attr("date", Calendar.getInstance().getTimeInMillis());
            StartTagWAX inventoryTag = rootTag.start("inventory");
            StartTagWAX classesTag = inventoryTag.start("classes");
            for (ClassWrapper aRoot : roots)
                getXMLNodeForClass(aRoot, classesTag);
            classesTag.end();
            inventoryTag.end();
            rootTag.end().close();
            return bas.toByteArray();
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Gets the items within a multiple choice element (a list)
     * @param className
     * @return the list of items belonging to the given class
     */
    @Override
    public ObjectList getMultipleChoice(Class className) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETMULTIPLECHOICE"));
        if (em != null){
            /*Maybe later, I can fix the method to avoid the cast
             try{
            Class multiObjectClass = Class.forName(className);
            }catch(Exception e){
            e.printStackTrace();
            this.error= e.toString();
            return null;
            }*/
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery query = cb.createQuery();
            Root entity = query.from(className);
            Query q =em.createQuery(query.select(entity).orderBy(cb.desc(entity.get("name"))));
            List<GenericObjectList> list = q.getResultList();
            return new ObjectList(className.getSimpleName(),list);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whose instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     * @return success or failure
     */
    @Override
    public Boolean addPossibleChildren(Long parentClassId, Long[] _possibleChildren) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_ADDPOSSIBLECHILDREN"));

        if (em != null){
            ClassMetadata parentClass;
            
            List<ClassMetadata> currenPossibleChildren;
            boolean alreadyAdded = false;

            parentClass = em.find(ClassMetadata.class, parentClassId);
            if (parentClass == null)
                throw new ObjectNotFoundException(ClassMetadata.class, parentClassId);

            if (!HierarchyUtils.isSubclass(getClassFor(parentClass.getName()),InventoryObject.class) && !getClassFor(parentClass.getName()).equals(DummyRoot.class))
                throw new InvalidArgumentException("Can't perform this operation for classes other than subclasses of InventoryObject", Level.WARNING);

            currenPossibleChildren = new ArrayList<ClassMetadata>(parentClass.getPossibleChildren());

            for (Long possibleChild : _possibleChildren){
                ClassMetadata cm = em.find(ClassMetadata.class, possibleChild);
                if (cm == null)
                    throw new ObjectNotFoundException(ClassMetadata.class, possibleChild);

                Class cmAsClass = getClassFor(cm.getName());

                for (ClassMetadata existingPossibleChild : currenPossibleChildren){
                    Class classA = getClassFor(existingPossibleChild.getName());
                    if (HierarchyUtils.isSubclass(classA,cmAsClass))
                        parentClass.getPossibleChildren().remove(existingPossibleChild);
                    else
                        if (HierarchyUtils.isSubclass(cmAsClass,classA))
                            alreadyAdded = true;
                }

                if (!currenPossibleChildren.contains(cm) && !alreadyAdded) // If the class is already a possible child, it won't add it
                    parentClass.getPossibleChildren().add(cm);
                else
                    throw new InvalidArgumentException("This class has already been added to the containment hierarchy: " + cm.getName(), Level.INFO);
            }
            em.merge(parentClass);
        }
        else
            throw new EntityManagerNotAvailableException();
        return true;
    }

    /**
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     * @return success or failure
     */
    @Override
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_REMOVEPOSSIBLECHILDREN"));

        if (em != null){
            ClassMetadata parent = em.find(ClassMetadata.class, parentClassId);
            for (Long id : childrenToBeRemoved)
                for (ClassMetadata cm :parent.getPossibleChildren())
                    if(cm.getId().equals(id)){
                        parent.getPossibleChildren().remove(cm);
                        break;
                    }

           em.merge(parent);
           return true;
        }else
            throw new EntityManagerNotAvailableException();
        
    }

    /**
     * Removes a given object
     * @param className
     * @param oid
     * @return
     */
    @Override
    public boolean removeObject(Class className, Long oid) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_REMOVEOBJECT"));

        if (em != null){
            RootObject obj = (RootObject)em.find(className, oid);
            if (obj == null)
                throw new ObjectNotFoundException(className, oid);
            if (className.equals(InventoryObject.class)){ // If the object is an inventory object, we have to delete the children first

                if (obj == null)
                    throw new Exception(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+className+java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_WHICHID")+oid.toString());

                if(obj.isLocked())
                    throw  new Exception(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_OBJECTLOCKED"));

                String sentence = "SELECT x FROM ClassMetadata x WHERE x.name ='"+
                        className.getSimpleName()+"'";
                //System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_EXECUTINGSQL")+sentence);
                Query query = em.createQuery(sentence);
                ClassMetadata myClass = (ClassMetadata)query.getSingleResult();
                List<Object> toBeRemoved = new ArrayList<Object>();
                for (ClassMetadata possibleChild : myClass.getPossibleChildren()){
                    sentence = "SELECT x FROM "+possibleChild.getName()+" x WHERE x.parent.id="+obj.getId();
                    //System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_EXECUTINGSQL")+sentence);
                    query = em.createQuery(sentence);
                    for (Object removable : query.getResultList()){
                        InventoryObject myRemovable = (InventoryObject)removable;
                        //If any of the children is locked, throw an exception
                        if (!myRemovable.isLocked())
                            toBeRemoved.add(myRemovable);
                        else
                            throw new OperationNotPermittedException(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").
                                    getString("LBL_OBJECTREMOVAL"),ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_OBJECTLOCKED")+
                                    myRemovable.getId()+" ("+myRemovable.getClass()+")");
                    }
                }


                toBeRemoved.add(obj);
                for (Object removable : toBeRemoved){
                    if (removable instanceof ViewableObject){
                        List<GenericView> views = ((ViewableObject)removable).getViews();
                        if (views != null){
                            for (GenericView view : views)
                                em.remove(view);
                            views.clear();
                        }
                        em.remove(removable);
                     }
                }
            }else em.remove(obj);
        }
        else 
            throw new EntityManagerNotAvailableException();
        
        return true;
    }

    /**
     * To ask for the object classes may seem a bit forced, but keeps the method simple (native types)
     * and efficient. maybe requesting for a RemoteObjectLight[] would be better.
     * We'll try that when we do some code cleanup
     * @param targetOid
     * @param objectOids
     * @param objectClasses
     * @return
     */
    @Override
    public boolean moveObjects(Long targetOid, Long[] objectOids, Class[] objectClasses) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_MOVEOBJECTS"));
        if (em != null){
            if (objectOids.length == objectClasses.length){
                for (int i = 0; i<objectClasses.length;i++){
                    InventoryObject currentObject = (InventoryObject)em.find(objectClasses[i], objectOids[i]);
                    if (currentObject == null)
                        throw new ObjectNotFoundException(objectClasses[i], objectOids[i]);
                    InventoryObject parentObject = (InventoryObject)em.find(InventoryObject.class, targetOid);
                    if (parentObject == null)
                        throw new ObjectNotFoundException(InventoryObject.class, targetOid); //NOI18N
                    currentObject.setParent(parentObject);
                    em.merge(currentObject);
                }
                return true;
            }else
                throw new ArrayIndexOutOfBoundsException(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+"(objectOids, objectClasses)");
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * To ask for the object classes may seem a bit forced, but keeps the method simple (native types)
     * and efficient. maybe requesting for a RemoteObjectLight[] would be better.
     * We'll try that when we do some code cleanup
     * @param targetOid the new parent
     * TODO: Should this use http://www.eclipse.org/eclipselink/api/2.1/org/eclipse/persistence/sessions/Session.html#copy ?
     */
    @Override
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids,
            Class[] objectClasses) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_COPYOBJECTS"));
        if (em != null){
            if (templateOids.length == objectClasses.length){
                RemoteObjectLight[] res = new RemoteObjectLight[objectClasses.length];
                for (int i = 0; i<objectClasses.length;i++){
                    //TODO: A more efficient way? maybe retrieving two or more objects at a time?
                    InventoryObject template = (InventoryObject)em.find(objectClasses[i], templateOids[i]);
                    
                    if (template == null)
                        throw new ObjectNotFoundException(objectClasses[i], templateOids[i]);

                    Object clone = MetadataUtils.clone(new RemoteObject(template),objectClasses[i],em);

                    if (clone == null)
                        throw new MiscException("The object couldn't be cloned");
                    
                    InventoryObject parentObject  = em.find(InventoryObject.class, targetOid);
                    if (parentObject == null)
                        throw new ObjectNotFoundException(InventoryObject.class, targetOid); //NOI18N

                    ((InventoryObject)clone).setParent(parentObject);
                    ((InventoryObject)clone).setLocked(false);
                    em.persist(clone);
                    res[i] = new RemoteObjectLight(clone);
                }
                return res;
            }else
                throw new Exception(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+" (objectOids, objectClasses)");
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Search for objects given some criteria
     * @param searchedClass
     * @param paramNames
     * @param paramTypes
     * @param paramValues
     * @return
     */
    @Override
    public RemoteObjectLight[] searchForObjects(Class searchedClass, String[] paramNames,
            String[] paramTypes, String[] paramValues) throws Exception{
        if (em != null){

            Object[] mappedValues = new Object[paramNames.length];

            for(int i = 0; i<mappedValues.length; i++)
                mappedValues[i] = MetadataUtils.getRealValue(paramTypes[i], paramValues[i],em);

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery query = cb.createQuery();
            Root entity = query.from(searchedClass);
            Predicate predicate = null;
            for (int i = 0; i< paramNames.length; i++){
                if (mappedValues[i] instanceof String)
                    predicate = (predicate == null)?cb.like(cb.lower(entity.get(paramNames[i])),"%"+((String)mappedValues[i]).toLowerCase()+"%"):
                                            cb.and(cb.like(cb.lower(entity.get(paramNames[i])),"%"+((String)mappedValues[i]).toLowerCase()+"%"),predicate);
                else
                    predicate = (predicate == null)?cb.equal(entity.get(paramNames[i]),mappedValues[i]):
                        cb.and(cb.equal(entity.get(paramNames[i]),mappedValues[i]),predicate);
            }
            if (predicate != null)
                query.where(predicate);
            List<Object> result = em.createQuery(query).getResultList();
            RemoteObjectLight[] res = new RemoteObjectLight[result.size()];

            int i = 0;
            for (Object obj: result){
                res[i] = new RemoteObjectLight(obj);
                i++;
            }
            return res;
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    // <editor-fold defaultstate="collapsed" desc="comment">
    /*@Override
    public RemoteObjectLight[] executeQuery(TransientQuery myQuery) throws Exception {
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_EXECUTEQUERY"));
        if (em != null) {
            Class toBeSearched = getClassFor(myQuery.getClassName());
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<InventoryObject> query = cb.createQuery(InventoryObject.class);
            Root entity = query.from(toBeSearched);
            ArrayList<Predicate> predicates = new ArrayList<Predicate>();
            Join<InventoryObject, InventoryObject> finalJoin = null;
            //getAttributeNames() is null only if no attributes were chosen as filters
            if (myQuery.getAttributeNames() != null) {
                //Basic filters (strings, dates, numbers, booleans)
                for (int i = 0; i < myQuery.getAttributeNames().size(); i++) {
                    String attribute = myQuery.getAttributeNames().get(i);
                    Object mappedValue = MetadataUtils.getRealValue(HierarchyUtils.getField(toBeSearched, attribute).toString(),
                            myQuery.getAttributeValues().get(i), em);
                    if (mappedValue == null) { //Look for a join in the getJoins()
                        TransientQuery myJoin = myQuery.getJoins().get(i);//If this is null, we're trying to match what objects has the current attribute set to null
                        if (myJoin == null) {
                            predicates.add(cb.equal(entity.get(attribute), null));

                        } else {
                            Class innerClass = getClassFor(myJoin.getClassName());
                            if (innerClass == null) {
                                throw new ClassNotFoundException(myJoin.getClassName());

                            }
                            if (finalJoin == null) {
                                finalJoin = entity.join(attribute);

                            } else {
                                finalJoin.join(attribute);
                                //Root innerEntity = query.from(innerClass);
                                //joins.add(entity.join(attribute));
                                //query.select(entity);

                            }
                        }
                    } else { //Process a simple value
                        if (mappedValue instanceof String) {
                            switch (myQuery.getConditions().get(i)) {
                                case TransientQuery.EQUAL:
                                    predicates.add(cb.equal(entity.get(attribute), mappedValue));
                                    break;
                                case TransientQuery.LIKE:
                                    //The like here is case-sesitive (?), so we have to lowercase the string
                                    predicates.add(cb.like(cb.lower(entity.get(attribute)), "%" + ((String) mappedValue).toLowerCase() + "%"));
                                    break;
                            }
                        } else {
                            if (mappedValue instanceof Boolean) {
                                predicates.add(cb.equal(entity.get(attribute), mappedValue));

                            } else {
                                if (mappedValue instanceof Integer || mappedValue instanceof Float) {
                                    switch (myQuery.getConditions().get(i)) {
                                        case TransientQuery.EQUAL:
                                            predicates.add(cb.equal(entity.get(attribute), mappedValue));
                                            break;
                                        case TransientQuery.EQUAL_OR_GREATER_THAN:
                                            //The like here is case-sensitive (?), so we have to lowercase the string
                                            predicates.add(cb.greaterThanOrEqualTo(entity.get(attribute), (Comparable) mappedValue));
                                            break;
                                        case TransientQuery.EQUAL_OR_LESS_THAN:
                                            predicates.add(cb.lessThanOrEqualTo(entity.get(attribute), (Comparable) mappedValue));
                                            break;
                                        case TransientQuery.GREATER_THAN:
                                            predicates.add(cb.greaterThan(entity.get(attribute), (Comparable) mappedValue));
                                            break;
                                        case TransientQuery.LESS_THAN:
                                            predicates.add(cb.lessThan(entity.get(attribute), (Comparable) mappedValue));
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (finalJoin != null) {
                query.select(finalJoin);

            }
            if (!predicates.isEmpty()) {
                Predicate finalPredicate = predicates.get(0);
                for (int i = 0; i < predicates.size(); i++) {
                    if (myQuery.getLogicalConnector() == TransientQuery.CONNECTOR_AND) {
                        finalPredicate = cb.and(predicates.get(i), finalPredicate);

                    } else {
                        finalPredicate = cb.or(predicates.get(i), finalPredicate);

                    }
                }
                query.where(finalPredicate);
            }

            List<InventoryObject> result = em.createQuery(query).getResultList();
            RemoteObjectLight[] res = new RemoteObjectLight[result.size()];

            int i = 0;
            for (Object obj : result) {
                res[i] = new RemoteObjectLight(obj);
                i++;
            }
            return res;
        } else {
            throw new EntityManagerNotAvailableException();

        }
    }*/// </editor-fold>
    /**
     * This is the JPQL version of the method used to perform complex queries. Please note
     * that the first record is reserved for the column headers, so and empty result set
     * will have at least one record.
     * @param myQuery The code-friendly representation of the query made using the graphical query builder
     * @return a set of objects matching the specified criteria as ResultRecord array
     * @throws Exception
     */
    @Override
    public ResultRecord[] executeQuery(TransientQuery myQuery) throws Exception {
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_EXECUTEQUERY"));
        if (em != null) {
            String queryText = "SELECT "; //The complete query text //NOI18N
            ArrayList<String> fields = new ArrayList<String>(); //fields to be retrieved
            String from = " FROM "+myQuery.getClassName()+ " x0"; //From clause  //NOI18N
            ArrayList<String> predicates = new ArrayList<String>(); //filters
            ArrayList<String> columnNames = new ArrayList<String>(); // The labels to be used as column headers
            ArrayList<String> joins = new ArrayList<String>();

            //These fields are necessary to build the RemoteObjectLights
            fields.add("x0.id"); //NOI18N
            fields.add("x0.name"); //NOI18N
            fields.add("TYPE(x0)"); //NOI18N

            //The default classIndex is 0 (that's why the main class )
            MetadataUtils.chainVisibleAttributes(myQuery, fields, columnNames, joins,"x0."); //NOI18N

            if (classIndex == null)
                generateClassIndex();
            MetadataUtils.chainPredicates("x0.", myQuery, predicates, em, classIndex); //NOI18N

            for (String myFields : fields)
                queryText += myFields +", ";

            //We remove the last comma
            queryText = queryText.substring(0, queryText.length() - 2);

            //From
            queryText += from;

            //Joins
            for (String myJoin : joins)
                queryText += " JOIN "+myJoin; //Check again!!!!

            if (!predicates.isEmpty()) {
                String finalPredicate = " WHERE ";  //NOI18N
                for (String predicate : predicates)
                    finalPredicate += predicate +
                            ((myQuery.getLogicalConnector() == TransientQuery.CONNECTOR_AND)?" AND ":" OR ");  //NOI18N
                queryText += finalPredicate.substring(0,finalPredicate.length() - 4);
            }

            queryText += " ORDER BY x0.name"; //NOI18N

            //System.out.println("SQL: "+queryText);
            Query query = em.createQuery(queryText);

            //If the page is 0, we show all results
            if (myQuery.getPage() > 0){
                query.setFirstResult(myQuery.getLimit() * (myQuery.getPage()-1));
                query.setMaxResults(myQuery.getLimit());
            }
            List<Object[]> result = query.getResultList();
            ResultRecord[] res = new ResultRecord[result.size() + 1]; //An additional record for column headers
            res[0] = new ResultRecord(null, columnNames);
            for(int i = 0; i < result.size(); i++){
                RemoteObjectLight objectInNewRecord = new RemoteObjectLight(
                        (Long)result.get(i)[0],((Class)result.get(i)[2]).getSimpleName(), (String)result.get(i)[1]);
                ArrayList<String> extraColumns = new ArrayList<String>();
                for (int j = 3; j < result.get(i).length ; j++)
                    extraColumns.add(result.get(i)[j] == null ? "": result.get(i)[j].toString());
                res[i + 1] = new ResultRecord(objectInNewRecord,extraColumns);
            }
            return res;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public org.kuwaiba.entity.queries.Query createQuery(String queryName, Long ownerOid, byte[] queryStructure, String description) throws Exception{
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_CREATEQUERY")); //NOI18N
        if (em != null) {
            User owner = null;
            if (ownerOid != null){
                owner = em.find(User.class, ownerOid);
                if (owner == null)
                    throw new ObjectNotFoundException(User.class, ownerOid); //NOI18N
            }
            org.kuwaiba.entity.queries.Query newQuery = new org.kuwaiba.entity.queries.Query(queryName, owner);
            newQuery.setContent(queryStructure);
            newQuery.setDescription(description);
            em.persist(newQuery);
            return newQuery;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public boolean saveQuery(Long queryOid, String queryName, Long ownerOid, byte[] queryStructure, String queryDescription) throws Exception{
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_SAVEQUERY")); //NOI18N
        if (em != null) {
            org.kuwaiba.entity.queries.Query myQuery = em.find(org.kuwaiba.entity.queries.Query.class, queryOid);
            if (myQuery == null)
                throw new ObjectNotFoundException(Query.class, queryOid); //NOI18N

            User owner = null;
            if (ownerOid != null){
                owner = em.find(User.class, ownerOid);
                if (owner == null)
                    throw new ObjectNotFoundException(User.class, ownerOid); //NOI18N
            }
            myQuery.setContent(queryStructure);
            myQuery.setName(queryName);
            myQuery.setDescription(queryDescription);
            em.merge(myQuery);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public boolean deleteQuery(Long queryOid) throws Exception{
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_DELETEQUERY")); //NOI18N
        if (em != null) {
            org.kuwaiba.entity.queries.Query toBeDeleted = em.find(org.kuwaiba.entity.queries.Query.class, queryOid);
            if (toBeDeleted == null)
                throw new ObjectNotFoundException(Query.class, queryOid); //NOI18N
            em.remove(toBeDeleted);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public RemoteQueryLight[] getQueries(Long ownerId, boolean showPublic) throws Exception {
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETQUERIES"));
        if (em != null){
            //String sentence = "SELECT x.id, x.name, x.description, x.owner FROM Query x WHERE x.owner.id="+ownerId; //NOI18N
            //For now, since the JPQL did not work and I'm having delays in the deliverables. JPQL Gurus Needed!
            String sentence = "SELECT robject.id, robject.name, q.description, q.owner_id FROM rootobject robject, query q WHERE q.owner_id="+ownerId+" AND robject.id=q.id ORDER BY robject.name"; //NOI18N
            if (showPublic)
                //sentence = "SELECT x.id, x.name, x.description, x.owner FROM Query x WHERE x.owner.id="+ownerId+" OR x.owner IS NULL"; //NOI18N
                //For now, since the JPQL did not work and I'm having delays in the deliverables. JPQL Gurus Needed!
                sentence = "SELECT robject.id, robject.name, q.description, q.owner_id FROM rootobject robject, query q WHERE q.id=robject.id AND (q.owner_id="+ownerId+" OR q.owner_id isnull) ORDER BY robject.name"; //NOI18N

            List<Object[]> allQueries = em.createNativeQuery(sentence).getResultList();
            RemoteQueryLight[] res = new RemoteQueryLight[allQueries.size()];
            for (int i = 0; i < allQueries.size(); i++){
                res[i] = new RemoteQueryLight((Long)allQueries.get(i)[0],
                                              (String)allQueries.get(i)[1],
                                              (String)allQueries.get(i)[2],
                                              allQueries.get(i)[3] == null);
            }
            return res;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public RemoteQuery getQuery(Long queryOid) throws Exception {
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_GETQUERY")); //NOI18N
        if (em != null){
            org.kuwaiba.entity.queries.Query query = em.find(org.kuwaiba.entity.queries.Query.class, queryOid);
            if (query == null)
                throw new ObjectNotFoundException(Query.class, queryOid); //NOI18
            return new RemoteQuery(query);
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public Boolean setAttributePropertyValue(Long classId, String attributeName, 
            String propertyName, String propertyValue) throws Exception{
        System.out.println(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_SETATTRIBUTEPROPERTYVALUE"));
        if (em != null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (myClass == null)
                throw new ClassNotFoundException("Class with id "+classId+" not found");
                
            for (AttributeMetadata att : myClass.getAttributes())
                if(att.getName().equals(attributeName)){
                    if (propertyName.equals("displayName"))
                        att.setDisplayName(propertyValue);
                    else
                        if (propertyName.equals("description"))
                            att.setDescription(propertyValue);
                        else
                            if (propertyName.equals("isVisible"))
                                att.setVisible(Boolean.valueOf(propertyValue));
                            else
                                throw new UnsupportedPropertyException(propertyName);
                    em.merge(att);
                    return true;
                }
            throw new MiscException("Attribute "+attributeName+" in class with id "+classId+" not found");
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Sets a given attribute for a class metadata
     * @param classId
     * @param attributeName
     * @param attributeValue
     * @return
     * @throws Exception
     */
    @Override
    public Boolean setClassPlainAttribute(Long classId, String attributeName, 
            String attributeValue) throws Exception{
        if(em !=null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (myClass ==null)
                throw new ClassNotFoundException("Class Id "+classId);

            if (attributeName.equals("displayName"))
                myClass.setDisplayName(attributeValue);
            else
                if (attributeName.equals("description"))
                    myClass.setDescription(attributeValue);
                else
                    throw new MiscException("Attribute "+attributeName+" in class with id "+classId+" not found");

            em.merge(myClass);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Set a class icon (big or small)
     * @param classId
     * @param attributeName
     * @param iconImage
     * @return
     */
    @Override
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) throws Exception{
        if(em !=null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (em ==null)
                throw new ClassNotFoundException("Class Id "+classId);

            if (attributeName.equals("smallIcon"))
                myClass.setSmallIcon(iconImage);
            else{
                if (attributeName.equals("icon"))
                    myClass.setIcon(iconImage);
                else
                    throw new MiscException("Attribute "+attributeName+" in class with id "+classId+" not found");
            }
            em.merge(myClass);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * TYPES
     */
     @Override
    public RemoteObjectLight createListType(Class objectClass) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_CREATELISTTYPE"));
        if (em != null){
            if (!HierarchyUtils.isSubclass(objectClass, GenericObjectList.class))
                throw new InvalidArgumentException("The class provided is not an GenericObjectList subclass: "+ objectClass.getSimpleName(), Level.SEVERE);
            GenericObjectList newListType = null;
            newListType = (GenericObjectList)objectClass.newInstance();

            em.persist(newListType);
            return new RemoteObjectLight(newListType);
        }
        else
            throw new EntityManagerNotAvailableException();
    }
    /**
     * Gets the possible list types (Classes that represent a list o something)
     * @return List of possible types
     */
    @Override
    public List<ClassInfoLight> getInstanceableListTypes() throws Exception{
        if (em != null){
            String sentence = "SELECT x.id, x.name, x.displayName, x.isAbstract,x.isPhysicalNode, x.isPhysicalConnection, x.isPhysicalEndpoint, x.smallIcon FROM ClassMetadata x WHERE x.isListType=true AND x.isAbstract=false ORDER BY x.name";
            Query q = em.createQuery(sentence);
            List<Object[]> cr = q.getResultList();
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();

            for (Object[] myRecord : cr)
                cml.add(new ClassInfoLight((Long)myRecord[0], (String)myRecord[1], (String)myRecord[2],
                                            (Boolean)myRecord[3], (Boolean)myRecord[4], (Boolean)myRecord[5],
                                            (Boolean)myRecord[6], (byte[])myRecord[7]));
            return cml;

        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Authenticate the user and creates a session if the login was successful
     * @param username
     * @param password
     * @return
     */
    @Override
    public UserSession createSession(String username, String password, String remoteAddress) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_CREATESESSION"));
        if (em != null){
            if (classIndex == null)
                generateClassIndex();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cQuery = cb.createQuery();
            Root entity = cQuery.from(User.class);
            Predicate predicate = cb.equal(entity.get("username"), username);
            predicate = cb.and(cb.equal(entity.get("password"), MetadataUtils.
                    getMD5Hash(password)),predicate);
            cQuery.where(predicate);
            try{
                User user = (User)em.createQuery(cQuery).getSingleResult();
                if (user.isEnabled()){
                    UserSession mySession = new UserSession(user);
                    mySession.setIpAddress(remoteAddress);
                    em.persist(mySession);
                    return mySession;
                }else
                    throw new InvalidSessionException("The user "+user.getUsername()+" is disabled");
            }catch(NoResultException e){
                throw new NotAuthorizedException(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_BADLOGIN"));
            }
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Gets an open session given the session id (session token)
     * @param sessionId
     * @return
     * @throws Exception
     */
    @Override
    public UserSession getSession(String sessionId) throws Exception{
        if (em != null){
            try{
                Object session = em.createQuery("SELECT x FROM UserSession x WHERE x.token = '"+
                    MetadataUtils.convertSpecialCharacters(sessionId)+"'").getSingleResult();
                return (UserSession)session;
            }catch(NoResultException nre){
                return null;
            }
        }else
            throw new EntityManagerNotAvailableException();
    }


    /**
     * Authenticate the user and creates a session if the login was successful
     * @param username
     * @param password
     * @return
     */
    @Override
    public Boolean closeSession(String sessionId, String remoteAddress) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_CALL_CLOSESESSION"));
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cQuery = cb.createQuery();
            Root entity = cQuery.from(UserSession.class);
            Predicate predicate = cb.equal(entity.get("token"), sessionId);
            predicate = cb.and(cb.equal(entity.get("ipAddress"), remoteAddress),predicate);
            cQuery.where(predicate);
            List result = em.createQuery(cQuery).getResultList();
            if (!result.isEmpty()){
                em.remove(result.get(0));
                return true;
            }
            else
                throw new Exception(ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NOACTIVESESSION"));

        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Methods associated to Views
     */

    /**
     * Built-in views
     */
    /**
     * The default view is composed of only the direct children of a
     * @param oid ViewInfo owner oid
     * @param className object's class
     * @return A view object representing the default view (the direct children)
     */
    @Override
    public ViewInfo getDefaultView(Long oid, Class myClass) throws Exception{
        if(em != null){
            Object obj = em.find(myClass, oid);
            if (obj == null)
                throw new Exception(java.util.ResourceBundle.getBundle("org/kuwaiba/internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER"));

            List<GenericView> views = ((ViewableObject)obj).getViews();
            if (views.isEmpty())
                return null;

            for (GenericView myView : views){
                if (myView instanceof DefaultView)
                    return new ViewInfo(myView);
            }
        }else
            throw new EntityManagerNotAvailableException();
        return null;
    }

    @Override
    public ViewInfo getRoomView(Long oid) throws Exception{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ViewInfo getRackView(Long oid)  throws Exception{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean saveObjectView(Long oid, Class myClass, ViewInfo view) throws Exception{
        if (em != null){
            Class viewClass = getClassFor(view.getViewClass());
            if (viewClass == null)
                throw new ClassNotFoundException(view.getViewClass());

            if (!HierarchyUtils.isSubclass(viewClass, GenericView.class))
                throw new InvalidArgumentException("The class provided is not a view: "+viewClass.getSimpleName(), Level.WARNING);

            Object obj = em.find(myClass, oid);
            if (obj == null)
                throw new ObjectNotFoundException(myClass,oid);
                
            List<GenericView> views = ((ViewableObject)obj).getViews();
            GenericView myView = null;
            for (GenericView eachView : views){
                if (eachView.getClass().equals(viewClass))
                    myView = eachView;
            }
            GenericView newView;
            if (myView == null){
                newView = (GenericView)viewClass.newInstance();
                newView.setViewStructure(view.getStructure());
                newView.setBackground(view.getBackground());
                newView.setDescription(view.getDescription());
                em.persist(newView);
                ((ViewableObject)obj).addView(newView);
            }
            else{
                myView.setViewStructure(view.getStructure());
                myView.setBackground(view.getBackground());
                myView.setDescription(view.getDescription());
                em.merge(myView);
            }
            em.merge(obj);
            return true;

        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Methods associated to Physical Connections
     */

    /**
     * Creates a new connection (WirelessLink, ElectricalLink, OpticalLink)
     * @param connectionClass
     * @param nodeA
     * @param nodeB
     * @return and RemoteObject with the newly created connection
     */
    @Override
    public RemoteObject createPhysicalConnection(Long endpointA, Long endpointB,
            Class connectionClass, Long parent) throws Exception{
        if (em != null){

            InventoryObject parentObject  = em.find(InventoryObject.class, parent);
            if (parentObject == null)
                throw new ObjectNotFoundException(InventoryObject.class, parent); //NOI18N

            GenericPort portA = em.find(GenericPort.class, endpointA);
            if (portA == null)
                throw new ObjectNotFoundException(GenericPort.class,endpointA);//NOI18N

            if (portA.getConnectedConnection() != null)
                throw new MiscException("Port A is already connnected");

            GenericPort portB = em.find(GenericPort.class, endpointB);
            if (portB == null)
                throw new ObjectNotFoundException(GenericPort.class,endpointB);//NOI18N

            if (portB.getConnectedConnection() != null)
                throw new MiscException("Port B is already connnected");

            GenericPhysicalConnection conn = (GenericPhysicalConnection) connectionClass.newInstance();
            conn.setEndpointA(portA);
            conn.setEndpointB(portB);


            conn.setParent(parentObject);

            portA.setConnectedConnection(conn);
            portB.setConnectedConnection(conn);

            em.persist(portA);
            em.persist(portB);
            em.persist(conn);
            return new RemoteObject(conn);

        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Creates a new container (Conduit, cable ditch)
     * @param containerClass
     * @param nodeA
     * @param nodeB
     * @return
     */
    @Override
    public RemoteObject createPhysicalContainerConnection(Long sourceNode, Long targetNode, 
            Class containerClass, Long parentNode) throws Exception{
        if (em != null){

            InventoryObject parentObject  = em.find(InventoryObject.class, parentNode);
            if (parentObject == null)
                throw new ObjectNotFoundException(InventoryObject.class, parentNode); //NOI18N

            GenericPhysicalNode nodeA = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, sourceNode);
            if (nodeA ==null)
                throw new ObjectNotFoundException(GenericPhysicalNode.class,sourceNode); //NOI18N

            GenericPhysicalNode nodeB = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, targetNode);
            if (nodeB ==null)
                throw new ObjectNotFoundException(GenericPhysicalNode.class,targetNode); //NOI18N

            GenericPhysicalContainer conn = (GenericPhysicalContainer) containerClass.newInstance();
            conn.setNodeA(nodeA);
            conn.setNodeB(nodeB);
            conn.setParent(parentObject);
            nodeA.getContainers().add(conn);
            nodeB.getContainers().add(conn);
            em.persist(conn);
            return new RemoteObject(conn);

        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * User/group management
     */

    @Override
    public UserInfo[] getUsers() throws Exception{
        if (em != null){
            UserInfo[] res;
            List<Object> users = em.createQuery("SELECT x FROM User x").getResultList();

            res = new UserInfo[users.size()];
            int i = 0;
            for(Object user: users){
                res[i] = new UserInfo((User)user);
                i++;
            }
            return res;
        }else
            throw new EntityManagerNotAvailableException();
    }

    @Override
    public UserGroupInfo[] getGroups() throws Exception{
        if (em != null){
            List<UserGroup> groups = em.createQuery("SELECT x FROM UserGroup x").getResultList();
            UserGroupInfo[] res = new UserGroupInfo[groups.size()];
            int i = 0;
            for (UserGroup group : groups){
                res[i] = new UserGroupInfo(group);
                i++;
            }
                
            return res;
        }else
            throw new EntityManagerNotAvailableException();
    }

    
    @Override
    public Boolean setUserProperties(Long oid, String[] propertiesNames, 
            String[] propertiesValues) throws Exception{
        User user = em.find(User.class, oid);
        if (user == null)
            throw new ObjectNotFoundException(User.class, oid);

        updateObject(new ObjectUpdate());
        //We can change username, firstName, lastName
        for (int i = 0; i<propertiesNames.length; i++){

        }
        return true;
    }

    @Override
    public Boolean removeUsersFromGroup(Long[] usersOids, Long groupOid) throws Exception{
        UserGroup group = em.find(UserGroup.class, groupOid);
        if (group == null)
            throw new ObjectNotFoundException(UserGroup.class,groupOid); //I18N

        for (Long oid : usersOids){
            User user = em.find(User.class,oid);
            group.getUsers().remove(user);
            //TODO: This is redundant if a bidirectional relationship is defined
            user.getGroups().remove(group);
            em.merge(user);
        }

        em.merge(group);

        return true;
    }

    @Override
    public Boolean addUsersToGroup(Long[] usersOids, Long groupOid) throws Exception{
        UserGroup group = em.find(UserGroup.class, groupOid);
        if (group == null)
            throw new ObjectNotFoundException(UserGroup.class,groupOid); //I18N
            
        for (Long oid : usersOids){
            User user = em.find(User.class,oid);
            if (!group.getUsers().contains(user))
                group.getUsers().add(user);
            if (!user.getGroups().contains(group))
                //TODO: This is redundant if a bidirectional relationship is defined
                user.getGroups().add(group);
            em.merge(user);
        }

        em.merge(group);
        return true;
    }

    /**
     * Creates a user. Uses a random name as default
     * @return
     */
    @Override
    public UserInfo createUser() throws Exception{
        User newUser = new User();
        Random random = new Random();
        newUser.setUsername("user"+random.nextInt(10000));
        em.persist(newUser);
        return new UserInfo(newUser);
    }

    /**
     * Removes a list of users
     * TODO: Check existing sessions and historic entries associated to this user
     * @param oids Oids for the users to be deleted
     * @return Success or failure
     */
    @Override
    public Boolean deleteUsers(Long[] oids) throws Exception{
        if (em !=null){
            for (Long oid :oids){
                User anUser = em.find(User.class, oid);
                List<UserGroup> groups = anUser.getGroups();
                if (groups != null){
                    for (UserGroup group : groups){
                        group.getUsers().remove(anUser);
                    }
                }
                em.remove(anUser);
            }
        }else throw new EntityManagerNotAvailableException();
        return true;
    }

    /**
     * Creates a group
     * @return
     */
    @Override
    public UserGroupInfo createGroup() throws Exception{
        UserGroup newGroup = new UserGroup();
        if (em != null){
            Random random = new Random();
            newGroup.setName("group"+random.nextInt(10000));
            em.persist(newGroup);
        }else throw new EntityManagerNotAvailableException();
        return new UserGroupInfo(newGroup);
    }

    /**
     * Deletes a list of groups
     * TODO: Check existing sessions and historic entries associated to this user
     * @param oids Oids for the groups to be deleted
     * @return Success or failure
     */
    @Override
    public Boolean deleteGroups(Long[] oids) throws Exception{
        if (em != null){
            for (Long oid :oids){
                UserGroup aGroup = em.find(UserGroup.class, oid);
                List<User> users = aGroup.getUsers();
                if (users != null){
                    for (User user : users){
                        user.getGroups().remove(aGroup);
                    }
                }
                em.remove(aGroup);
            }
        }else throw new EntityManagerNotAvailableException();
        return true;
    }

    /**
     * Associates a user to an user
     * @param groupsOids
     * @param userOid
     * @return
     */
    @Override
    public Boolean addGroupsToUser(Long[] groupsOids, Long userOid) throws Exception{
        if (em != null){
            User user = em.find(User.class, userOid);
            if (user == null)
                throw new ObjectNotFoundException(User.class,userOid);

            for (Long oid : groupsOids){
                UserGroup group = em.find(UserGroup.class,oid);
                if (group.getUsers() != null)
                    if (!group.getUsers().contains(user)) //Ignores the addition if the user already belongs to the group
                        group.getUsers().add(user);

                if(user.getGroups() != null)
                    if(!user.getGroups().contains(group))
                        //TODO: This is redundant if a bidirectional relationship is defined
                        user.getGroups().add(group);

                em.merge(group);
            }

            em.merge(user);

            return true;
        }else throw new EntityManagerNotAvailableException();

    }

    @Override
    public Boolean removeGroupsFromUser(Long[] groupsOids, Long userOid) throws Exception{
        if (em != null){
            User user = em.find(User.class, userOid);
            if (user == null)
                throw new ObjectNotFoundException(User.class, userOid);

            UserGroup group = null;

            for (Long oid : groupsOids){
                group = em.find(UserGroup.class,oid);
                if (group.getUsers() != null)
                    group.getUsers().remove(user); //No matter if the user is not included, since the method call will not throw any exception
                if (user.getGroups() != null)
                    //TODO: This is redundant if a bidirectional relationship is defined
                    user.getGroups().remove(group);

                em.merge(group);
            }

            em.merge(user);

            return true;
        }else throw new EntityManagerNotAvailableException();
    }

    /**
     * Session management
     */

     /**
      * @param method the method to be validated
      * @param username the user that tries to invoke the method
      * @param ipAddress the ip address to avoid [somehow] a session hijack
      * @param token the session ID
      * @return success or failure
      * @throws NotAuthorizedException if the user tries to call a method which he/she's not supposed to, and a generic Exception if something happens with the database
      */
    @Override
    public boolean validateCall(String method, String ipAddress,
            String token) throws Exception{
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery myQuery = cb.createQuery();
            Root entity = myQuery.from(UserSession.class);
            myQuery.where(cb.and(cb.equal(entity.get("token"), token), //NOI18N
                    cb.equal(entity.get("ipAddress"), ipAddress)     //NOI18N
                    ));        //NOI18N

            List result = em.createQuery(myQuery).getResultList();
            if (result.isEmpty())
                throw new InvalidSessionException("The session expired or is not valid anymore");
            //TODO: Check for the allowed methods
            return true;
        }else throw new EntityManagerNotAvailableException();
    }

    /**
     * HELPERS
     */
    /**
     * Fills the entity class list used as cache by various methods
     */
    private void generateClassIndex(){
        classIndex = new HashMap<String, Class>();
        Set<EntityType<?>> allEntities = em.getMetamodel().getEntities();
        for (EntityType ent : allEntities)
            classIndex.put(ent.getJavaType().getSimpleName(), ent.getJavaType());
    }

    /**
     * recursive method used to generate a single "class" node (see the <a href="http://sourceforge.net/apps/mediawiki/kuwaiba/index.php?title=XML_Documents#To_describe_the_data_model">wiki</a> for details)
     * @param root the class to be added
     * @param rootTag the root tag (node) to attach the new class node
     */
    private void getXMLNodeForClass(ClassWrapper root, StartTagWAX rootTag) {
        StartTagWAX currentTag = rootTag.start("class"); //NOI18N
        currentTag.attr("name", root.getName());
        currentTag.attr("javaModifiers",root.getJavaModifiers());
        currentTag.attr("applicationModifiers",root.getApplicationModifiers());
        currentTag.attr("classType",root.getClassType());

        StartTagWAX attributesTag = currentTag.start("attributes");
        for (AttributeWrapper myAttribute : root.getAttributes()){
            StartTagWAX attributeTag = attributesTag.start("attribute");
            attributeTag.attr("name", myAttribute.getName());
            attributeTag.attr("type", myAttribute.getType().getSimpleName());
            attributeTag.attr("javaModifiers", myAttribute.getJavaModifiers());
            attributeTag.attr("applicationModifiers", myAttribute.getApplicationModifiers());
            attributeTag.end();
        }
        attributesTag.end();

        StartTagWAX subclassesTag = currentTag.start("subclasses");
        for (ClassWrapper subClass: root.getDirectSubClasses())
            getXMLNodeForClass(subClass, currentTag);

        subclassesTag.end();
        currentTag.end();
    }
}
