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
import core.toserialize.View;
import entity.config.User;
import entity.config.UserGroup;
import entity.core.ConfigurationItem;
import entity.core.DummyRoot;
import entity.core.RootObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import entity.location.Country;
import entity.location.StateObject;
import entity.multiple.GenericObjectList;
import entity.multiple.views.ObjectView;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
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
    //application server. If we'd like to do it manually, er show use a EntityManagerFactory
    @PersistenceContext
    private EntityManager em;
    private String error;

    @Override
    public void createInitialDataset() {
        String[] countryNames = new String[]{"Colombia","Brazil","England","Germany","United States"};

        List<StateObject> rl = new ArrayList<StateObject> ();
        List<Country> sl = new ArrayList<Country> ();

        //Let's create the root
        DummyRoot root = new DummyRoot();
        root.setId(RootObject.PARENT_ROOT);
        em.persist(root);

        for (int i=1;i<3;i++){
            StateObject r = new StateObject();
            r.setName("State #"+String.valueOf(i));
            rl.add(r);
        }
        for(String name : countryNames){
            Country country = new Country();
            country.setName(name);
            country.setParent(RootObject.PARENT_ROOT); //Means the parent is the root
            sl.add(country);
        }

        for (Country s : sl){
            em.persist(s);
        }

        for (StateObject r : rl){
            r.setParent(sl.iterator().next().getId());
            em.persist(r);
        }
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
            Dictionary<String, EntityType> alreadyPersisted = new Hashtable<String, EntityType>();

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
    public List getObjectChildren(Long oid, Long objectClassId) {
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTCHILDREN"));
        if (em != null){
           
            ClassMetadata objectClass = em.find(ClassMetadata.class, objectClassId);

            List result = new ArrayList();
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
                    this.error = ex.getMessage();
                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            return result;
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    @Override
    public RemoteObject getObjectInfo(String objectClass,Long oid){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_GETOBJECTINFO"));
        if (em != null){
            String sentence = "SELECT x from "+objectClass+" x WHERE x.id="+String.valueOf(oid);
            Query query = em.createQuery(sentence);
            Object result = query.getSingleResult();
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
                obj = new RemoteObjectUpdate(_obj,em);

                Object myObject = em.find(obj.getObjectClass(), obj.getOid());
                if(myObject == null)
                    throw new ObjectNotFoundException();
                for (int i = 0; i< obj.getNewValues().length; i++)
                    myObject.getClass().getMethod("set"+MetadataUtils.capitalize(obj.getUpdatedAttributes()[i].getName()),
                            obj.getUpdatedAttributes()[i].getType()).invoke(myObject, obj.getNewValues()[i]);
                em.merge(myObject);
                return true;
            } catch (Exception ex) {
                this.error = ex.getClass().toString()+": "+ex.getMessage();
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
    public ObjectList getMultipleChoice(String className){
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
            String sentence = "SELECT x FROM "+className+" x ORDER BY x.name";
            Query q = em.createQuery(sentence,GenericObjectList.class);
            List<GenericObjectList> list = q.getResultList();
            return new ObjectList(className,list);
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
     *
     * @param className
     * @param oid
     * @return
     */
    @Override
    public boolean removeObject(Class className, Long oid){
        System.out.println(java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CALL_REMOVEOBJECT"));

        if (em != null){
            //TODO ¿Será que se deja una relación del objeto a su metadata para
            //hacer más rápida la búsqueda en estos casos?
            RootObject obj = (RootObject)em.find(className, oid);
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
                        //TODO Código de verificación de integridad
                        if (!((RootObject)removable).getIsLocked())
                            em.remove(removable);
                    }
                }
                em.remove(obj);
            }catch (Exception e){
                this.error = e.toString();
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
                return false;
            }
        }
        else {
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, null, this.error);
            return false;
        }
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
     * The default view is composed of only the direct children of a
     * @param oid View owner oid
     * @param className object's class
     * @return A view object representing the default view (the direct children)
     */
    @Override
    public View getDefaultView(Long oid, Class className) {
        if(em != null){
            ConfigurationItem object = (ConfigurationItem)em.find(className, oid);
            List<ObjectView> views = object.getViews();
            if (views == null){
                try{
                    Long classOid = (Long)em.createQuery("SELECT oid FROM ClassMetadata x WHERE x.name='"+
                            className.getSimpleName()+"'").getSingleResult();
                     List elements = getObjectChildren(oid, classOid);
                     ObjectView view = new ObjectView(elements);
                     em.persist(view);
                     return new View(view);
                }catch(NoResultException nre){
                    this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_CLASSNOTFOUND");
                    Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
                    return null;
                }
            }
            else
                return new View(object.getViews().get(0));
        }else{
            this.error = java.util.ResourceBundle.getBundle("internationalization/Bundle").getString("LBL_NO_ENTITY_MANAGER");
            Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, this.error);
            return null;
        }
    }

    @Override
    public View getRoomView(Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public View getRackView(Long oid) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

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