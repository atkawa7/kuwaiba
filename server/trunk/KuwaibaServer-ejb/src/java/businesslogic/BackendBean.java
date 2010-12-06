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
package businesslogic;

import core.toserialize.ClassInfo;
import core.toserialize.ObjectList;
import core.toserialize.RemoteObject;
import core.toserialize.RemoteObjectLight;
import core.annotations.Metadata;
import core.exceptions.EntityManagerNotAvailableException;
import core.exceptions.NotAuthorizedException;
import core.exceptions.ObjectNotFoundException;
import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectUpdate;
import core.toserialize.UserGroupInfo;
import core.toserialize.UserInfo;
import core.toserialize.Validator;
import core.toserialize.ViewInfo;
import entity.config.User;
import entity.config.UserGroup;
import entity.connections.physical.GenericPhysicalConnection;
import entity.connections.physical.containers.GenericPhysicalContainer;
import entity.core.RootObject;
import entity.core.ViewableObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import entity.equipment.physicallayer.parts.ports.GenericPort;
import entity.location.GenericPhysicalNode;
import entity.multiple.GenericObjectList;
import entity.session.UserSession;
import entity.views.GenericView;
import entity.views.DefaultView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.ejb.Stateful;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.EntityType;
import util.HierarchyUtils;
import util.MetadataUtils;

/**
 * Handles the logic of all calls so far
 * @author Charles Edward bedon Cortazar <charles.bedon@zoho.com>
 */
@Stateful
public class BackendBean implements BackendBeanRemote {
    //We use cointainer managed persistance, which means that we don't handle the
    //access to the database directly, but we use a persistemce unit set by the
    //application server. If we'd like to do it manually, we should use an EntityManagerFactory
    @PersistenceContext
    private EntityManager em;
    private HashMap<String,Class> classIndex;

    @Override
    public Class getClassFor(String className) throws Exception{
        if (em != null){
            if (classIndex == null){
                classIndex = new HashMap<String, Class>();
                Set<EntityType<?>> allEntities = em.getMetamodel().getEntities();
                for (EntityType ent : allEntities)
                    classIndex.put(ent.getJavaType().getSimpleName(), ent.getJavaType());
            }
            Class myClass = classIndex.get(className);
            if (myClass != null)
                return classIndex.get(className);
            else throw new ClassNotFoundException(className);
        }else
            throw new EntityManagerNotAvailableException();
    }
    /**
     * This method resets class metadata information
     *
     */
    @Override
    public void buildMetaModel() throws Exception{
        
        if (em != null){

            //Delete existing class metadata
            Query query = em.createNamedQuery("flushClassMetadata");
            query.executeUpdate();

            //Delete existing attribute metadata
            query = em.createNamedQuery("flushAttributeMetadata");
            query.executeUpdate();

            //Delete existing package metadata
            query = em.createNamedQuery("flushPackageMetadata");
            query.executeUpdate();

            Set<EntityType<?>> ent = em.getMetamodel().getEntities();
            HashMap<String, EntityType> alreadyPersisted = new HashMap<String, EntityType>();

            for (EntityType entity : ent){
                if(entity.getJavaType().getAnnotation(Metadata.class)!=null)
                        continue;
                if (alreadyPersisted.get(entity.getJavaType().getSimpleName())!=null)
                    continue;
                HierarchyUtils.persistClass(entity,em);
            }
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Returns the id that will be use to reference the root object
     * @return the id assigned to the dummy root
     */
    @Override
    public Long getDummyRootId(){
        return RootObject.PARENT_ROOT;
    }

    /**
     * Retrieves a given object's children
     * @param oid Parent object oid
     * @param objectClassId Parent object's class oid
     * @return a list of objects or null if an error ocurred
     */
    @Override
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTCHILDREN"));
        if (em != null){
           
            ClassMetadata objectClass = em.find(ClassMetadata.class, objectClassId);

            List<Object> result = new ArrayList<Object>();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            Query subQuery=null;

            for (ClassMetadata possibleChildren : objectClass.getPossibleChildren()){
                CriteriaQuery query = criteriaBuilder.createQuery();
                Root entity = query.from(Class.forName(possibleChildren.getPackageInfo().getName() + "." + possibleChildren.getName()));
                query.where(criteriaBuilder.equal(entity.get("parent"),oid));
                subQuery = em.createQuery(query);
                result.addAll(subQuery.getResultList());
            }

            RemoteObjectLight[] validatedResult = new RemoteObjectLight[result.size()];
            int i = 0;
            for (Object child : result) {
                validatedResult[i] = new RemoteObjectLight(child);
                if (child instanceof GenericPort)
                    validatedResult[i].addValidator(new Validator("isConnected",((GenericPort)child).getConnectedConnection() != null)); //NOI18n
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
            Query query = em.createQuery("SELECT x FROM "+myClass.getSimpleName()+" x WHERE x.parent="+parentOid);
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            Object result = em.find(objectClass, oid);           
            if (result==null)
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString());
             else
                return new RemoteObject(result);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Implementation of the idem method exposed by the webservice
     * TODO: This implementation is inefficient and should be corrected
     * @param objectClass
     * @param oid
     * @return
     */
    @Override
    public RemoteObjectLight getObjectInfoLight(Class objectClass, Long oid) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            Object result = em.find(objectClass, oid);
            if (result==null)
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString());
            else
                return new RemoteObjectLight(result);
            
        }
        else
            throw new EntityManagerNotAvailableException();
    }


    /**
     *
     * @param _obj
     * @return
     * @throws ObjectNotFoundException if the oid provided doesn't exist
     */
    @Override
    public boolean updateObject(ObjectUpdate _obj) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_UPDATEOBJECT"));

        if (em != null){
            RemoteObjectUpdate obj;
            Class myClass = getClassFor(_obj.getClassname());
            if (myClass == null)
                throw new ClassNotFoundException(_obj.getClassname());
            obj = new RemoteObjectUpdate(myClass,_obj,em);

            Object myObject = em.find(obj.getObjectClass(), obj.getOid());
            if(myObject == null)
                throw new ObjectNotFoundException();
            for (int i = 0; i< obj.getNewValues().length; i++)
                myObject.getClass().getMethod("set"+MetadataUtils.capitalize(obj.getUpdatedAttributes()[i].getName()),
                        obj.getUpdatedAttributes()[i].getType()).invoke(myObject, obj.getNewValues()[i]);
            em.merge(myObject);
            return true;
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
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString());
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
    public ClassInfoLight[] getPossibleChildren(Class parentClass) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETPOSSIBLECHILDREN"));
        List<ClassInfoLight> res = new ArrayList();
        if (em != null){
            String sentence;
            Class myClass;
            Query query;

            //Now we have to iterate to find the inherited containing capacity
            myClass = parentClass;
            List<ClassMetadata> allPossibleChildren = new ArrayList<ClassMetadata>(); //This list includes the abstract classes
            while (!myClass.equals(RootObject.class) && !myClass.equals(Object.class)){
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
            for (ClassMetadata cm : allPossibleChildren){
                if (cm.getIsAbstract()){
                    List<ClassMetadata> morePossibleChildren =
                            HierarchyUtils.getInstanceableSubclasses(cm.getId(), em);
                    for (ClassMetadata moreCm : morePossibleChildren)
                        res.add(new ClassInfoLight(moreCm));
                }
                else
                    res.add(new ClassInfoLight(cm));
            }
            return res.toArray(new ClassInfoLight[0]);
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
    public ClassInfoLight[] getPossibleChildrenNoRecursive(Class parentClass) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETPOSSIBLECHILDRENNORECURSIVE"));
        List<ClassInfoLight> res = new ArrayList();
         if (em != null){
             String sentence;
             Class myClass;
             Query query;

             myClass = parentClass;
             while (!myClass.equals(RootObject.class) && !myClass.equals(Object.class)){
                 sentence = "SELECT x.possibleChildren FROM ClassMetadata x WHERE x.name='"+myClass.getSimpleName()+"'";
                 query = em.createQuery(sentence);
                 List partialResult = query.getResultList();
                 if (partialResult!=null)
                     for (Object obj : partialResult)
                         res.add(new ClassInfoLight((ClassMetadata)obj));
                 myClass = myClass.getSuperclass();
             }
             return res.toArray(new ClassInfoLight[0]);
          }
          else throw new EntityManagerNotAvailableException();
    }

    /**
     * Helper that gets the possible children for the root node
     * @return
     */
    @Override
    public ClassInfoLight[] getRootPossibleChildren() throws Exception{
        return getPossibleChildren(RootObject.ROOT_CLASS);
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_CREATEOBJECT"));
        Object newObject = null;
        if (em != null){
            newObject = objectClass.newInstance();
            if (parentOid != null)
                newObject.getClass().getMethod("setParent", Long.class).
                        invoke(newObject, parentOid);
            em.persist(newObject);
            return new RemoteObjectLight(newObject);
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Retrieves all the class metadata
     * @return An array of classes
     */
    @Override
    public List<ClassInfo> getMetadata() throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETMETADATA"));
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x WHERE x.isAdministrative=false ORDER BY x.name ";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            List<ClassInfo> cm = new ArrayList<ClassInfo>();
            int i=0;
            for (ClassMetadata myClass : cr){
                if (myClass.getIsHidden() || myClass.getIsDummy())
                    continue;
                cm.add(new ClassInfo(myClass));
            }
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETMETADATAFORCLASS"));
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery myQuery = cb.createQuery();
            Root entity = myQuery.from(ClassMetadata.class);
            myQuery.where(cb.equal(entity.get("name"),className.getSimpleName()));

            Query q = em.createQuery(myQuery);
            ClassMetadata res;

            res = (ClassMetadata)q.getSingleResult();
            return new ClassInfo(res);
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETMULTIPLECHOICE"));
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_ADDPOSSIBLECHILDREN"));

        if (em != null){
            ClassMetadata parentClass;
            
            List<ClassMetadata> currenPossibleChildren;

            parentClass = em.find(ClassMetadata.class, parentClassId);
            currenPossibleChildren = parentClass.getPossibleChildren();

            for (Long possibleChild : _possibleChildren){
                ClassMetadata cm = em.find(ClassMetadata.class, possibleChild);

                if (!currenPossibleChildren.contains(cm)) // If the class is already a possible child, it won't add it
                    parentClass.getPossibleChildren().add(cm);
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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_REMOVEPOSSIBLECHILDREN"));

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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_REMOVEOBJECT"));

        if (em != null){

            //em.getTransaction().begin();

            RootObject obj = (RootObject)em.find(className, oid);
            if (obj == null)
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+className+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString());

            if(obj.getIsLocked())
                throw  new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_OBJECTLOCKED"));
                       
            String sentence = "SELECT x FROM ClassMetadata x WHERE x.name ='"+
                    className.getSimpleName()+"'";
            System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_EXECUTINGSQL")+sentence);
            Query query = em.createQuery(sentence);
            ClassMetadata myClass = (ClassMetadata)query.getSingleResult();
            for (ClassMetadata possibleChild : myClass.getPossibleChildren()){
                sentence = "SELECT x FROM "+possibleChild.getName()+" x WHERE x.parent="+obj.getId();
                System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_EXECUTINGSQL")+sentence);
                query = em.createQuery(sentence);
                for (Object removable : query.getResultList()){
                    RootObject myRemovable = (RootObject)removable;
                    //If any of the children is locked, throw an exception
                    if (!myRemovable.getIsLocked())
                        em.remove(myRemovable);
                    else
                        throw new Exception("An object within the hierarchy is locked: "+
                                myRemovable.getId()+" ("+myRemovable.getClass()+")");
                }
            }
            em.remove(obj);
            
        }
        else //*************em.getTransaction().commit();**************
            throw new EntityManagerNotAvailableException();
        
        return true;
    }

    @Override
    public List<ClassInfoLight> getLightMetadata() throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETLIGHTMETADATA"));
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x ORDER BY x.name";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            List<ClassInfoLight> cml = new ArrayList<ClassInfoLight>();

            for (ClassMetadata myClass : cr){
                if (myClass.getIsHidden() || myClass.getIsDummy())
                    continue;
                cml.add(new ClassInfoLight(myClass));
            }
            return cml;
        }
        else
            throw new EntityManagerNotAvailableException();
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
    public boolean moveObjects(Long targetOid, Long[] objectOids, String[] objectClasses) throws Exception{
        if (em != null){
            if (objectOids.length == objectClasses.length){
                for (int i = 0; i<objectClasses.length;i++){
                    String sentence = "UPDATE "+objectClasses[i]+" x SET x.parent="+targetOid+" WHERE x.id="+objectOids[i];
                    Query q = em.createQuery(sentence);
                    q.executeUpdate();
                }
                return true;
            }else
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+"(objectOids, objectClasses)");
        }
        else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * To ask for the object classes may seem a bit forced, but keeps the method simple (native types)
     * and efficient. maybe requesting for a RemoteObjectLight[] would be better.
     * We'll try that when we do some code cleanup
     * @param targetOid the new parent
     */
    @Override
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids,
            String[] objectClasses) throws Exception{
        if (em != null){
            if (templateOids.length == objectClasses.length){
                RemoteObjectLight[] res = new RemoteObjectLight[objectClasses.length];
                for (int i = 0; i<objectClasses.length;i++){
                    //TODO: A more efficient way? maybe retrieving two or more objects at a time?
                    String sentence = "SELECT x FROM "+objectClasses[i]+" x WHERE x.id="+templateOids[i];
                    Query q = em.createQuery(sentence);
                    Object obj = q.getSingleResult(), clone;
                    
                    clone = MetadataUtils.clone(obj);
                    ((RootObject)clone).setParent(targetOid);
                    ((RootObject)clone).setIsLocked(false);
                    //Nice trick to generate an Id
                    ((RootObject)clone).setId((new RootObject() {}).getId());
                    

                    em.persist(clone);
                    res[i] = new RemoteObjectLight(clone);
                }
                return res;
            }else
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+" (objectOids, objectClasses)");
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

    @Override
    public Boolean setAttributePropertyValue(Long classId, String attributeName, 
            String propertyName, String propertyValue) throws Exception{
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_SETATTRIBUTEPROPERTYVALUE"));
        if (em != null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (myClass == null)
                throw new Exception("Class with id "+classId+" not found");
                
            for (AttributeMetadata att : myClass.getAttributes())
                if(att.getName().equals(attributeName)){
                    if (propertyName.equals("displayName"))
                        att.setDisplayName(propertyValue);
                    else
                        if (propertyName.equals("description"))
                            att.setDescription(propertyValue);
                        else
                            if (propertyName.equals("isVisible"))
                                att.setIsVisible(Boolean.valueOf(propertyValue));
                            else
                                if (propertyName.equals("isAdministrative"))
                                    att.setIsAdministrative(Boolean.valueOf(propertyValue));
                                else{
                                    throw new Exception("Property "+propertyName+" not supported");
                                }
                    em.merge(att);
                    return true;
                }
            throw new Exception("Attribute "+attributeName+" in class with id "+classId+" not found");
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
                throw new Exception("Class with id "+classId+" not found");

            if (attributeName.equals("displayName"))
                myClass.setDisplayName(attributeValue);
            else
                if (attributeName.equals("description"))
                    myClass.setDescription(attributeValue);
                else
                    throw new Exception("Attribute "+attributeName+" in class with id "+classId+" not found");

            em.merge(myClass);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Set a class' icon (big or small)
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
                throw new Exception("Class with id "+classId+" not found");

            if (attributeName.equals("smallIcon"))
                myClass.setSmallIcon(iconImage);
            else{
                if (attributeName.equals("icon"))
                    myClass.setIcon(iconImage);
                else
                    throw new Exception("Attribute "+attributeName+" in class with id "+classId+" not found");
            }
            em.merge(myClass);
            return true;
        }else
            throw new EntityManagerNotAvailableException();
    }

    /**
     * Gets the possible list types (Classes that represent a list o something)
     * @return List of possible types
     */
    @Override
    public ClassInfoLight[] getInstanceableListTypes() throws Exception{
        if (em != null){
            Long id = (Long) em.createQuery("SELECT x.id FROM ClassMetadata x WHERE x.name ='GenericObjectList'").getSingleResult();
            List<ClassMetadata> listTypes =HierarchyUtils.getInstanceableSubclasses(id, em);
            ClassInfoLight[] res = new ClassInfoLight[listTypes.size()];

            int i=0;
            for (ClassMetadata cm : listTypes){
                res[i] = new ClassInfoLight(cm);
                i++;
            }
            return res;

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
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_CREATESESSION"));
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cQuery = cb.createQuery();
            Root entity = cQuery.from(User.class);
            Predicate predicate = cb.equal(entity.get("username"), username);
            predicate = cb.and(cb.equal(entity.get("password"), MetadataUtils.
                    getMD5Hash(password)),predicate);
            cQuery.where(predicate);
            List result = em.createQuery(cQuery).getResultList();
            if (!result.isEmpty()){
                UserSession mySession = new UserSession((User)result.get(0));
                mySession.setIpAddress(remoteAddress);
                em.persist(mySession);
                return mySession;
            }
            else
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_BADLOGIN"));
                
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
                throw new Exception(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER"));

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
                throw new Exception("View class not valid: "+ view.getViewClass());

            Object obj = em.find(myClass, oid);
            if (obj == null)
                throw new Exception (java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" "+myClass.getSimpleName()+" "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid);
                
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

            GenericPort portA = em.find(GenericPort.class, endpointA);
            if (portA == null)
                throw new Exception("Port A does not exist");

            if (portA.getConnectedConnection() != null)
                throw new Exception("Port A is already connnected");

            GenericPort portB = em.find(GenericPort.class, endpointB);
            if (portB == null)
                throw new Exception("Port B is already connnected");


            if (portB.getConnectedConnection() != null)
                throw new Exception("Port B is already connnected");

            GenericPhysicalConnection conn = (GenericPhysicalConnection) connectionClass.newInstance();
            conn.setEndpointA(portA);
            conn.setEndpointB(portB);
            conn.setParent(parent);

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

            GenericPhysicalNode nodeA = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, sourceNode);
            if (nodeA ==null)
                throw new Exception("Node A does not exist");

            GenericPhysicalNode nodeB = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, targetNode);
            if (nodeB ==null)
                throw new Exception("Node B does not exist");

            GenericPhysicalContainer conn = (GenericPhysicalContainer) containerClass.newInstance();
            conn.setNodeA(nodeA);
            conn.setNodeB(nodeB);
            conn.setParent(parentNode);
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

    //Use updateObject instead
    @Override
    public Boolean setUserProperties(Long oid, String[] propertiesNames, 
            String[] propertiesValues) throws Exception{
        /*User user = em.find(User.class, oid);
        if (user == null){
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_USERNOTFOUND")+oid.toString();
            return false;
        }

        updateObject(new ObjectUpdate());
        //We can change username, firstName, lastName
        for (int i = 0; i<propertiesNames.length; i++){

        }*/
        return true;
    }

    @Override
    public Boolean removeUsersFromGroup(Long[] usersOids, Long groupOid) throws Exception{
        UserGroup group = em.find(UserGroup.class, groupOid);
        if (group == null)
            throw new Exception(java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" UserGroup "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+groupOid);

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
            throw new Exception(java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" UserGroup "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+groupOid);
            
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
                        //anUser.getGroups().remove(group);
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
                        //aGroup.getUsers().remove(user);
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
                throw new Exception (java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").
                        getString("LBL_NOSUCHOBJECT")+" User "+java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").getString("LBL_WHICHID")+userOid);

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
                throw new Exception(java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").
                        getString("LBL_NOSUCHOBJECT")+" User "+java.util.ResourceBundle.
                        getBundle("internationalization/Bundle").getString("LBL_WHICHID")+userOid);

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
                throw new NotAuthorizedException("No session active for this user");
            //TODO: Check for the allowed methods
            return true;
        }else throw new EntityManagerNotAvailableException();
    }
}
