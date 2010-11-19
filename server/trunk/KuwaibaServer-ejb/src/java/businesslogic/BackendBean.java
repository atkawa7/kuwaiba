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
import core.exceptions.ObjectNotFoundException;
import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectUpdate;
import core.toserialize.UserGroupInfo;
import core.toserialize.UserInfo;
import core.toserialize.Validator;
import core.toserialize.ViewInfo;
import entity.adapters.ObjectViewAdapter;
import entity.config.User;
import entity.config.UserGroup;
import entity.connections.physical.GenericPhysicalConnection;
import entity.connections.physical.containers.GenericPhysicalContainer;
import entity.core.DummyRoot;
import entity.core.RootObject;
import entity.core.ViewableObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import entity.equipment.physicallayer.parts.ports.GenericPort;
import entity.location.Country;
import entity.location.GenericPhysicalNode;
import entity.location.StateObject;
import entity.multiple.GenericObjectList;
import entity.views.GenericView;
import entity.views.DefaultView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
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
    private String error;
    private HashMap<String,Class> classIndex;

    public Class getClassFor(String className){
        if (classIndex == null){
            classIndex = new HashMap<String, Class>();
            Set<EntityType<?>> allEntities = em.getMetamodel().getEntities();
            for (EntityType ent : allEntities)
                classIndex.put(ent.getJavaType().getSimpleName(), ent.getJavaType());
        }
        return classIndex.get(className);
    }
    /**
     * This method resets class metadata information
     *
     */
    @Override
    public void buildMetaModel(){
        
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
        else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
        }

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
    public RemoteObjectLight[] getObjectChildren(Long oid, Long objectClassId) {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTCHILDREN"));
        if (em != null){
           
            ClassMetadata objectClass = em.find(ClassMetadata.class, objectClassId);

            List<Object> result = new ArrayList<Object>();
            CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
            Query subQuery=null;

            for (ClassMetadata possibleChildren : objectClass.getPossibleChildren()){
                try {
                    CriteriaQuery query = criteriaBuilder.createQuery();
                    Root entity = query.from(Class.forName(possibleChildren.getPackageInfo().getName() + "." + possibleChildren.getName()));
                    query.where(criteriaBuilder.equal(entity.get("parent"),oid));
                    subQuery = em.createQuery(query);
                    result.addAll(subQuery.getResultList());
                } catch (ClassNotFoundException ex) {
                    this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CLASSNOTFOUND")+ possibleChildren.getName();
                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                    return null;
                }
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
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     * Retrieves the children of an object whose class would be the one provided
     * @param parentOid
     * @param myClass
     * @return
     */
    @Override
    public RemoteObjectLight[] getChildrenOfClass(Long parentOid, Class myClass) {
        if (em !=null){
            Query query = em.createNamedQuery("SELECT x FROM "+myClass.getSimpleName()+" x WHERE x.parent="+parentOid);
            List<Object> res = query.getResultList();
            return RemoteObjectLight.toArray(res);
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     * Implementation of the idem method exposed by the webservice
     * @param objectClass
     * @param oid
     * @return
     */
    @Override
    public RemoteObject getObjectInfo(Class objectClass,Long oid){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            Object result = em.find(objectClass, oid);           
            if (result==null){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return null;
            }else
                return new RemoteObject(result);
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     * Implementation of the idem method exposed by the webservice
     * TODO: This implementation is inefficient and should be corrected
     * @param objectClass
     * @param oid
     * @return
     */
    @Override
    public RemoteObjectLight getObjectInfoLight(Class objectClass, Long oid){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            Object result = em.find(objectClass, oid);
            if (result==null){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return null;
            }else
                return new RemoteObjectLight(result);
            
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }


    /**
     *
     * @param _obj
     * @return
     * @throws ObjectNotFoundException if the oid provided doesn't exist
     */
    @Override
    public boolean updateObject(ObjectUpdate _obj){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_UPDATEOBJECT"));

        if (em != null){
            RemoteObjectUpdate obj;
            try {
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
            } catch (Exception ex) {
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CLASSNOTFOUND")+ ex.getMessage();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return false;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
    }

    /**
     *
     * @param oid
     * @param objectClass
     * @param value
     * @return
     */
    @Override
    public boolean setObjectLock(Long oid, String objectClass, Boolean value){
        if (em != null){
            String myClassName = objectClass.substring(objectClass.lastIndexOf("."));
            String sentence = "UPDATE x "+myClassName+" x SET isLocked="+value.toString()+" WHERE x.id="+String.valueOf(oid);
            Query query = em.createQuery(sentence);
            if (query.executeUpdate()==0){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+objectClass+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }else
                return true;
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
    }

    /**
     *
     * @return
     */
    @Override
     public String getError(){
        return this.error;
    }

    /**
     *
     * @param parentClass
     * @return
     */
    @Override
    public ClassInfoLight[] getPossibleChildren(Class parentClass) {
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
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     *
     * @param parentClass
     * @return
     */
    @Override
    public ClassInfoLight[] getPossibleChildrenNoRecursive(Class parentClass) {
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
          else {
              this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
              Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
              return null;
          }
    }

    /**
     *
     * @return
     */
    @Override
    public ClassInfoLight[] getRootPossibleChildren(){
        return getPossibleChildren(RootObject.ROOT_CLASS);
    }

    /**
     *
     * @param objectClass
     * @param parentOid
     * @param template
     * @return
     */
    @Override
    public RemoteObjectLight createObject(String objectClass, Long parentOid, String template){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_CREATEOBJECT"));
        Object newObject = null;
        if (em != null){
            try{
                newObject = Class.forName(objectClass).newInstance();
                if (parentOid != null)
                    newObject.getClass().getMethod("setParent", Long.class).
                            invoke(newObject, parentOid);
                em.persist(newObject);
            }catch(Exception e){
                this.error = e.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return null;
            }
            return new RemoteObjectLight(newObject);
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    @Override
    public ClassInfo[] getMetadata(){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETMETADATA"));
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x WHERE x.isAdministrative=false ORDER BY x.name ";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            ClassInfo[] cm = new ClassInfo[cr.size()];
            int i=0;
            for (ClassMetadata myClass : cr){
                cm[i] = new ClassInfo(myClass);
                i++;
            }
            return cm;
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    @Override
    public ClassInfo getMetadataForClass(String className){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETMETADATAFORCLASS"));
        if (em != null){
            try{
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery myQuery = cb.createQuery();
                Root entity = myQuery.from(ClassMetadata.class);
                myQuery.where(cb.equal(entity.get("name"),className));

                Query q = em.createQuery(myQuery);
                ClassMetadata res;
            
                res = (ClassMetadata)q.getSingleResult();
                return new ClassInfo(res);
            }catch (Exception e){
                this.error = e.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return null;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    /**
     *
     * @param className
     * @return
     */
    @Override
    public ObjectList getMultipleChoice(Class className){
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
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    /**
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     * @return success or failure
     */
    @Override
    public Boolean addPossibleChildren(Long parentClassId, Long[] _possibleChildren) {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_ADDPOSSIBLECHILDREN"));

        if (em != null){
            ClassMetadata parentClass;
            
            List<ClassMetadata> currenPossibleChildren;
            Query q;

            parentClass = em.find(ClassMetadata.class, parentClassId);
            currenPossibleChildren = parentClass.getPossibleChildren();

            for (Long possibleChild : _possibleChildren){
                ClassMetadata cm = em.find(ClassMetadata.class, possibleChild);

                if (!currenPossibleChildren.contains(cm)) // If the class is already a possible child, it won't add it
                    parentClass.getPossibleChildren().add(cm);
            }
            em.merge(parentClass);
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
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
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) {
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
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
        
    }

    /**
     * Removes a given object
     * @param className
     * @param oid
     * @return
     */
    @Override
    public boolean removeObject(Class className, Long oid){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_REMOVEOBJECT"));

        if (em != null){

            //em.getTransaction().begin();

            RootObject obj = (RootObject)em.find(className, oid);
            if (obj == null){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOSUCHOBJECT")+className+java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return false;
            }

            if(obj.getIsLocked()){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_OBJECTLOCKED");
                return false;
            }
            try{
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
            }catch (Exception e){
                this.error = e.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                //em.getTransaction().rollback();
                return false;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
        //em.getTransaction().commit();
        return true;
    }

    @Override
    public ClassInfoLight[] getLightMetadata() {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETLIGHTMETADATA"));
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x ORDER BY x.name";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            ClassInfoLight[] cml = new ClassInfoLight[cr.size()];
            int i=0;
            for (ClassMetadata myClass : cr){
                cml[i] = new ClassInfoLight(myClass);
                i++;
            }
            return cml;
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    /*
     * To ask for the object classes may seem a bit forced, but keeps the method simple (native types)
     * and efficiente. maybe requesting for a RemoteObjectLight[] would be better.
     * We'll try that when we do some code cleanup
     */
    @Override
    public boolean moveObjects(Long targetOid, Long[] objectOids, String[] objectClasses){
        if (em != null){
            if (objectOids.length == objectClasses.length){
                for (int i = 0; i<objectClasses.length;i++){
                    String sentence = "UPDATE "+objectClasses[i]+" x SET x.parent="+targetOid+" WHERE x.id="+objectOids[i];
                    Query q = em.createQuery(sentence);
                    q.executeUpdate();
                }
                return true;
            }else{
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+"(objectOids, objectClasses)";
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
    }

    /**
     * To ask for the object classes may seem a bit forced, but keeps the method simple (native types)
     * and efficient. maybe requesting for a RemoteObjectLight[] would be better.
     * We'll try that when we do some code cleanup
     * @param targetOid the new parent
     */
    @Override
    public RemoteObjectLight[] copyObjects(Long targetOid, Long[] templateOids, String[] objectClasses){
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
            }else{
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NOTMATCHINGARRAYSIZES")+" (objectOids, objectClasses)";
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return null;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    @Override
    public RemoteObjectLight[] searchForObjects(Class searchedClass, String[] paramNames,
            String[] paramTypes, String[] paramValues) {
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
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    @Override
    public Boolean setAttributePropertyValue(Long classId, String attributeName, String propertyName, String propertyValue) {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_SETATTRIBUTEPROPERTYVALUE"));
        if (em != null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (myClass == null){
                this.error = "Class with Id "+classId+" not found";
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }

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
                                    this.error = "Property "+propertyName+" not supported";
                                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                                    return false;
                                }
                    em.merge(att);
                    return true;
                }
            this.error = "Attribute "+attributeName+" in class with id "+classId+" not found";
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
    }

    @Override
    public Boolean setClassPlainAttribute(Long classId, String attributeName, String attributeValue) {
        if(em !=null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (em ==null){
                this.error = "Class with id "+classId+" not found";
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }
            if (attributeName.equals("displayName"))
                myClass.setDisplayName(attributeValue);
            else
                if (attributeName.equals("description"))
                    myClass.setDescription(attributeValue);
                else{
                    error = "Attribute "+attributeName+" in class with id "+classId+" not found";
                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                    return false;
                }

            em.merge(myClass);
            return true;
        }else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
    }

    @Override
    public Boolean setClassIcon(Long classId, String attributeName, byte[] iconImage) {
        if(em !=null){
            ClassMetadata myClass = em.find(ClassMetadata.class, classId);
            if (em ==null){
                this.error = "Class with id "+classId+" not found";
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }
            if (attributeName.equals("smallIcon"))
                myClass.setSmallIcon(iconImage);
            else
                if (attributeName.equals("icon"))
                    myClass.setIcon(iconImage);
                else{
                    this.error = "Attribute "+attributeName+" in class with id "+classId+" not found";
                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                    return false;
                }

            em.merge(myClass);
            return true;
        }else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
    }

    @Override
    public ClassInfoLight[] getInstanceableListTypes() {
        if (em != null){
            Long id = (Long) em.createQuery("SELECT x.id FROM ClassMetadata x WHERE x.name ='GenericObjectList' ORDER BY x.name").getSingleResult();
            List<ClassMetadata> listTypes =HierarchyUtils.getInstanceableSubclasses(id, em);
            ClassInfoLight[] res = new ClassInfoLight[listTypes.size()];

            int i=0;
            for (ClassMetadata cm : listTypes){
                res[i] = new ClassInfoLight(cm);
                i++;
            }
            return res;

        }else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return null;
        }
    }

    @Override
    public boolean createSession(String username, String password) {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_CREATESESSION"));
        if (em != null){
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cQuery = cb.createQuery();
            Root entity = cQuery.from(User.class);
            Predicate predicate = cb.equal(entity.get("username"), username);
            predicate = cb.and(cb.equal(entity.get("password"), MetadataUtils.
                    getMD5Hash(password)),predicate);
            cQuery.where(predicate);
            if (!em.createQuery(cQuery).getResultList().isEmpty())
                return true;
            else{
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_BADLOGIN");
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                return false;
            }
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
    }

    /**
     * Views
     */

    /**
     * The default view is composed of only the direct children of a
     * @param oid ViewInfo owner oid
     * @param className object's class
     * @return A view object representing the default view (the direct children)
     */
    @Override
    public ViewInfo getDefaultView(Long oid, Class myClass) {
        if(em != null){
            Object obj = em.find(myClass, oid);
            if (obj == null){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
                Logger.getLogger(BackendBean.class.getName()).log(Level.WARNING, this.error);
                return null;
            }
            List<ObjectViewAdapter> viewAdapters = ((ViewableObject)obj).getViews();
            if (viewAdapters.isEmpty())
                return null;

            for (ObjectViewAdapter myViewAdapter : viewAdapters){
                try{
                    GenericView myView = (GenericView)em.createQuery("SELECT x FROM "+myViewAdapter.getaSideClass()+" x WHERE x.id="+myViewAdapter.getaSide()).getSingleResult();
                    if (myView instanceof DefaultView)
                        return new ViewInfo(myView);
                }catch (NoResultException nre){
                }
            }

        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
        return null;
    }

    @Override
    public ViewInfo getRoomView(Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ViewInfo getRackView(Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean saveObjectView(Long oid, Class myClass, ViewInfo view){
        if (em != null){
            Object obj = em.find(myClass, oid);
            if (obj == null){
                this.error = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" "+myClass.getSimpleName()+" "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+oid;
                Logger.getLogger(BackendBean.class.getName()).log(Level.WARNING, this.error);
                return false;
            }
            List<ObjectViewAdapter> viewAdapters = ((ViewableObject)obj).getViews();
            if (!viewAdapters.isEmpty()){
                for (ObjectViewAdapter myViewAdapter : viewAdapters){
                    //TODO: Only change the fields that have been updated
                    try{
                        GenericView myView = (GenericView)em.createQuery("SELECT x FROM "+myViewAdapter.getaSideClass()+" x WHERE x.id="+myViewAdapter.getaSide()).getSingleResult();
                        if(myView.getClass().getName().equals(view.getViewClass())) //If there's one already, replace it
                        em.remove(myView);
                    }catch (NoResultException nre){
                    }
                }
                em.merge(obj);
            }else 
                ((ViewableObject)obj).setViews(new ArrayList<ObjectViewAdapter>());

            try{
                DefaultView newView = new DefaultView(view);
                em.persist(newView);
                ObjectViewAdapter newViewAdapter = new ObjectViewAdapter();
                newViewAdapter.setaSide(newView.getId());
                newViewAdapter.setaSideClass(newView.getClass().getSimpleName());
                em.persist(newViewAdapter);
                ((ViewableObject)obj).getViews().add(newViewAdapter);
                em.merge(obj);
                return true;
            }catch(UnsupportedOperationException uso){
                this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CANT_CREATE_VIEW")+view.getViewClass();
                Logger.getLogger(BackendBean.class.getName()).log(Level.WARNING, this.error);
                return false;
            }
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
    }

    /**
     * Physical Connections
     */

    /**
     * Creates a new connection (WirelessLink, ElectricalLink, OpticalLink)
     * @param connectionClass
     * @param nodeA
     * @param nodeB
     * @return and RemoteObject with the newly created connection
     */
    @Override
    public RemoteObject createPhysicalConnection(Long endpointA, Long endpointB, Class connectionClass, Long parent){
        if (em != null){
            try {

                GenericPort portA = em.find(GenericPort.class, endpointA);
                if (portA == null){
                    return null;
                }

                if (portA.getConnectedConnection() != null){
                    this.error = "Port A is already connnected";
                    return null;
                }

                GenericPort portB = em.find(GenericPort.class, endpointB);
                if (portB == null){
                    return null;
                }
                
                if (portB.getConnectedConnection() != null){
                    this.error = "Port B is already connnected";
                    return null;
                }

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
            } catch (InstantiationException ex) {
                this.error = ex.getClass().toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return null;
            } catch (IllegalAccessException ex) {
                this.error = ex.getClass().toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return null;
            }
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     * Gets the connections (container or single connections) for a given parent
     * @param oid parent oid
     * @param className class name
     * @return List of connections or null on error
     */
    @Override
    public RemoteObject[] getConnectionsForParent(Long oid, String className){

        //TODO: Check that this class represents some kind of connection/container
        if (em!=null){
            List<Object> res = em.createQuery("SELECT x FROM "+className+" x WHERE x.parent="+oid).getResultList();
            return RemoteObject.toArray(res);
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }
    /**
     * Creates a new container (Conduit, cable ditch)
     * @param containerClass
     * @param nodeA
     * @param nodeB
     * @return
     */
    @Override
    public RemoteObject createPhysicalContainerConnection(Long sourceNode, Long targetNode, Class containerClass, Long parentNode){
        if (em != null){

            GenericPhysicalNode nodeA = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, sourceNode);
            if (nodeA ==null){
                return null;
            }

            GenericPhysicalNode nodeB = (GenericPhysicalNode)em.find(GenericPhysicalNode.class, targetNode);
            if (nodeB ==null){
                return null;
            }

            try {
                GenericPhysicalContainer conn = (GenericPhysicalContainer) containerClass.newInstance();
                conn.setNodeA(nodeA);
                conn.setNodeB(nodeB);
                conn.setParent(parentNode);
                nodeA.getContainers().add(conn);
                nodeB.getContainers().add(conn);
                em.persist(conn);
                return new RemoteObject(conn);
            } catch (InstantiationException ex) {
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, ex.getClass());
                return null;
            } catch (IllegalAccessException ex) {
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, ex.getClass());
                return null;
            }
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    /**
     * User/group management
     */

    @Override
    public UserInfo[] getUsers() {
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
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    @Override
    public UserGroupInfo[] getGroups() {
        if (em != null){
            List<UserGroup> groups = em.createQuery("SELECT x FROM UserGroup x").getResultList();
            UserGroupInfo[] res = new UserGroupInfo[groups.size()];
            int i = 0;
            for (UserGroup group : groups){
                res[i] = new UserGroupInfo(group);
                i++;
            }
                
            return res;
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    //Use updateObject instead
    @Override
    public Boolean setUserProperties(Long oid, String[] propertiesNames, String[] propertiesValues) {
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
    public Boolean setGroupProperties(Long oid, String[] propertiesNames, String[] propertiesValues) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Boolean removeUsersFromGroup(Long[] usersOids, Long groupOid) {
        UserGroup group = em.find(UserGroup.class, groupOid);
        if (group == null){
            this.error = this.error = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" UserGroup "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+groupOid;
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }

        User user=null;

        for (Long oid : usersOids){
            user = em.find(User.class,oid);
            group.getUsers().remove(user);
            //TODO: This is redundant if a bidirectional relationship is defined
            user.getGroups().remove(group);
            em.merge(user);
        }

        em.merge(group);

        return true;
    }

    @Override
    public Boolean addUsersToGroup(Long[] usersOids, Long groupOid) {
        UserGroup group = em.find(UserGroup.class, groupOid);
        if (group == null){
            this.error = this.error = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" UserGroup "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+groupOid;
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }

        User user=null;

        for (Long oid : usersOids){
            user = em.find(User.class,oid);
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

    @Override
    public UserInfo createUser() {
        User newUser = new User();
        try{
            Random random = new Random();
            newUser.setUsername("user"+random.nextInt(10000));
            em.persist(newUser);
        }catch(Exception e){
            this.error = e.toString();
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
        return new UserInfo(newUser);
    }

    /**
     * Removes a list of users
     * TODO: Check existing sessions and historic entries associated to this user
     * @param oids Oids for the users to be deleted
     * @return Success or failure
     */
    @Override
    public Boolean deleteUsers(Long[] oids) {
        try{
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
        }catch(Exception e){
            this.error = e.toString();
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
        return true;
    }

    @Override
    public UserGroupInfo createGroup() {
        UserGroup newGroup = new UserGroup();
        try{
            Random random = new Random();
            newGroup.setName("group"+random.nextInt(10000));
            em.persist(newGroup);
        }catch(Exception e){
            this.error = e.toString();
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
        return new UserGroupInfo(newGroup);
    }

    /**
     * Deletes a list of groups
     * TODO: Check existing sessions and historic entries associated to this user
     * @param oids Oids for the groups to be deleted
     * @return Success or failure
     */
    @Override
    public Boolean deleteGroups(Long[] oids) {
        try{
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
        }catch(Exception e){
            this.error = e.toString();
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }
        return true;
    }

    @Override
    public Boolean addGroupsToUser(Long[] groupsOids, Long userOid) {
        User user = em.find(User.class, userOid);
        if (user == null){
            this.error = this.error = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" User "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+userOid;
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }

        UserGroup group = null;

        for (Long oid : groupsOids){
            group = em.find(UserGroup.class,oid);
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

    }

    @Override
    public Boolean removeGroupsFromUser(Long[] groupsOids, Long userOid) {
        User user = em.find(User.class, userOid);
        if (user == null){
            this.error = this.error = java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").
                    getString("LBL_NOSUCHOBJECT")+" User "+java.util.ResourceBundle.
                    getBundle("internationalization/Bundle").getString("LBL_WHICHID")+userOid;
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return false;
        }

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
    }
}
