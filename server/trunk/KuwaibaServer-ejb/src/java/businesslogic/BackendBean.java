package businesslogic;

import core.toserialize.ClassInfo;
import core.toserialize.ObjectList;
import core.toserialize.RemoteObject;
import core.toserialize.RemoteObjectLight;
import core.toserialize.RemoteTreeNode;
import core.annotations.Administrative;
import core.annotations.Dummy;
import core.annotations.Metadata;
import core.exceptions.ObjectNotFoundException;
import core.todeserialize.ObjectUpdate;
import core.toserialize.ClassInfoLight;
import core.toserialize.RemoteObjectUpdate;
import entity.core.DummyRoot;
import entity.core.RootObject;
import entity.core.metamodel.AttributeMetadata;
import entity.core.metamodel.ClassMetadata;
import entity.core.metamodel.PackageMetadata;
import entity.location.Country;
import entity.location.StateObject;
import entity.multiple.GenericObjectList;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateful;
//import javax.ejb.Stateless;
import javax.persistence.PersistenceContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;

/**
 *
 * @author Charles Edward bedon Cortazar <charles.bedon@zoho.com>
 */
@Stateful
public class BackendBean implements BackendBeanRemote {
    //En una aplicación J2EE el EM se inserta automáticamente, para eso es la anotación
    //y no es necesario instanciarlo, porque el container lo maneja, sin embargo en una aplicación
    //J2SE o que use un contenedor no J2EE como Tomcat o Jetty, toca hacerlo manualmente, probablemente con un EM Factory
    @PersistenceContext
    private EntityManager em;
    private String error;

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

    /*
     * This method resets class metadata information
     *
     */
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
            Dictionary<String, PackageMetadata> packages = new Hashtable<String, PackageMetadata>();

            for (EntityType entity : ent){
                if(entity.getJavaType().getAnnotation(Metadata.class)!=null ||
                        entity.getJavaType().getAnnotation(Administrative.class)!=null)
                        continue;
                List<AttributeMetadata> atts = new ArrayList<AttributeMetadata>();
                Set<Attribute> metaAtts = entity.getAttributes();
                PackageMetadata pm;
                for(Attribute att : metaAtts)
                    atts.add(new AttributeMetadata(att));


                pm = packages.get(entity.getJavaType().getPackage().getName());
                if (pm == null){
                    pm = new PackageMetadata(entity.getJavaType().getPackage().getName(),"");
                    packages.put(entity.getJavaType().getPackage().getName(),pm);
                    em.persist(pm);
                }

                em.persist(new ClassMetadata(entity.getJavaType().getSimpleName(),
                                             pm,
                                             "Class "+entity.getJavaType().getSimpleName(),
                                             false,Modifier.isAbstract(entity.getJavaType().getModifiers()),
                                             (entity.getJavaType().getAnnotation(Dummy.class)!=null),
                                             null,atts,null
                                             )
                          );

            }
        }
        else this.error = "No EntityManager available";

    }

    /*
     * Returns the id that will be use to reference the root object
     */
    public Long getDummyRootId(){
        return RootObject.PARENT_ROOT;
    }

    /*
     * Return the class used to represent the root node
     */
    public String getDummyRootClass(){
        return RootObject.ROOT_CLASS;
    }

    public RemoteTreeNode getObjectInmediateHierarchy(Long oid, String objectClass) {
        if (em != null){
            String sentence = "SELECT x from "+ objectClass +" x WHERE x.id="+oid.toString();
            Query query = em.createQuery(sentence);
            
            List result = query.getResultList();
            if (result.size()==0){
                this.error = "No existe un objeto de la clase "+objectClass+" cuyo oid sea "+oid.toString();
                return null;
            }else
                return new RemoteTreeNode(result.iterator().next(),new Object[0]);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public RemoteObjectLight[] getObjectChildren(Long oid, String objectClass) {
        System.out.println("[getObject] Llamado");
        if (em != null){
            String sentence;
            Query query;
            List partialResult;
            List result = new ArrayList();
            //Find out which class instances can be put into objectClass instances
            sentence = "SELECT x.possibleChildren FROM ClassMetadata x WHERE x.name='"+
                    objectClass+"'";
            query = em.createQuery(sentence);
            partialResult = query.getResultList();//Instances of this class can
                                                  //be contained within the instances of the given class
            for (Object res : partialResult){
                String perClassQuery = "SELECT x FROM "+((ClassMetadata)res).getName()+" x WHERE x.parent="
                        +String.valueOf(oid);
                query = em.createQuery(perClassQuery);
                result.addAll(query.getResultList());
            }

            return RemoteObjectLight.toArray(result);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public RemoteObject getObjectInfo(String objectClass,Long oid){
        System.out.println("[getObjectInfo]: Llamado");
        if (em != null){
            //String myClassName = objectClass.substring(objectClass.lastIndexOf("."));
            String sentence = "SELECT x from "+objectClass+" x WHERE x.id="+String.valueOf(oid);
            Query query = em.createQuery(sentence);
            Object result = query.getSingleResult();
            if (result==null){
                this.error = "No existe un objeto de la clase "+objectClass+" cuyo oid sea "+oid.toString();
                return null;
            }else
                return new RemoteObject(result);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public boolean updateObject(ObjectUpdate _obj) throws ObjectNotFoundException{
        System.out.println("[updateObject]: Llamado");

        if (em != null){
            Set <EntityType<?>> set = em.getMetamodel().getEntities();
            RemoteObjectUpdate obj;
            try {
                obj = new RemoteObjectUpdate(_obj, set);

                /*Object myObject = em.find(obj.getObjectClass(), obj.getOid());
                if(myObject == null)
                throw new ObjectNotFoundException();*/
                String sentence = obj.generateQueryText();
                Logger.getLogger(BackendBean.class.getName()).log(Level.INFO, sentence);
                Query query = em.createNativeQuery(sentence);
                query.executeUpdate();
                return true;
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BackendBean.class.getName()).log(Level.SEVERE, ex.getMessage());
                return false;
            }
        }
        else {
            this.error = "El EntityManager no existe";
            return false;
        }
    }

    public boolean setObjectLock(Long oid, String objectClass, Boolean value){
        if (em != null){
            String myClassName = objectClass.substring(objectClass.lastIndexOf("."));
            String sentence = "UPDATE x "+myClassName+" x SET isLocked="+value.toString()+" WHERE x.id="+String.valueOf(oid);
            Query query = em.createQuery(sentence);
            if (query.executeUpdate()==0){
                this.error = "No existe un objeto de la clase "+objectClass+" cuyo oid sea "+oid.toString();
                return false;
            }else
                return true;
        }
        else {
            this.error = "El EntityManager no existe";
            return false;
        }
    }

     public String getError(){
        return this.error;
    }

    public ClassInfoLight[] getPossibleChildren(Class parentClass) {
        System.out.println("[getPossibleChildren] Llamado");
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
                        //res.add(((ClassMetadata)obj).getPackageName().getName()+"."+((ClassMetadata)obj).getName());
                        res.add(new ClassInfoLight(((ClassMetadata)obj).getId(),
                                                     ((ClassMetadata)obj).getName(),
                                                     ((ClassMetadata)obj).getPackageInfo().getName()));
                myClass = myClass.getSuperclass();
            }
            return res.toArray(new ClassInfoLight[0]);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public RemoteObjectLight createObject(String objectClass, Long parentOid, String template){
        System.out.println("[getPossibleChildren] Llamado");
        Object newObject = null;
        if (em != null){
            try{
                System.out.println("padre:"+parentOid);
                newObject = Class.forName(objectClass).newInstance();
                //Recordar que invoke recibe como parámetros null y los parámtros del método si es estático
                //y el objetos y los parámetros del método si es 
                newObject.getClass().getMethod("setParent", Long.class).
                        invoke(newObject, parentOid);
                em.persist(newObject);
            }catch(Exception e){
                this.error = e.getMessage();
                e.printStackTrace();
                return null;
            }
            return new RemoteObjectLight(newObject);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public ClassInfo[] getMetadata(){
        System.out.println("[getMetadata] Llamado");
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x ORDER BY x.name";
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
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public ClassInfo getMetadataForClass(String className){
        System.out.println("[getMetadata] Llamado");
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x WHERE x.name='"+className+"'";
            Query q = em.createQuery(sentence);
            ClassMetadata res;
            try{
                res = (ClassMetadata)q.getSingleResult();
            }catch (Exception e){
                this.error = e.getMessage();
                return null;
            }
            return new ClassInfo(res);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    public ObjectList getMultipleChoice(String className){
        System.out.println("[getMetadata] Llamado");
        if (em != null){
            /*Maybe later, I can fix the method to avoid the cast
             try{
            Class multiObjectClass = Class.forName(className);
            }catch(Exception e){
            e.printStackTrace();
            this.error= e.getMessage();
            return null;
            }*/
            String sentence = "SELECT x FROM "+className+" x ORDER BY x.name";
            Query q = em.createQuery(sentence,GenericObjectList.class);
            List<GenericObjectList> list = q.getResultList();
            return new ObjectList(className,list);
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }

    /*
     * Adds to a given class a list of possible children classes whose instances can be contained
     *
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param _possibleChildren ids of the candidates to be contained
     * @return success or failure
     */
    public Boolean addPossibleChildren(Long parentClassId, Long[] _possibleChildren) {
        System.out.println("[addPossibleChildren] Llamado");

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
            this.error = "El EntityManager no existe";
            return false;
        }
        return true;
    }

    /*
     * The opposite of addPossibleChildren. It removes the given possible children
     * TODO: Make this method safe. This is, check if there's already intances of the given
     * "children to be deleted" with parentClass as their parent
     * @param parentClassId Id of the class whos instances can contain the instances of the next param
     * @param childrenTBeRemoved ids of the candidates to be deleted
     * @return success or failure
     */
    public Boolean removePossibleChildren(Long parentClassId, Long[] childrenToBeRemoved) {
        System.out.println("[removeObject] Called");

        if (em != null){
        }else{

        }
        return false;
    }

    public boolean removeObject(Class className, Long oid){
        System.out.println("[removeObject] Called");

        if (em != null){
            //TODO ¿Será que se deja una relación del objeto a su metadata para
            //hacer más rápida la búsqueda en estos casos?
            RootObject obj = (RootObject)em.find(className, oid);
            if(obj.getIsLocked()){
                this.error = "El objeto está bloqueado";
                return false;
            }
            try{
                String sentence = "SELECT x FROM ClassMetadata x WHERE x.name ='"+
                        className.getSimpleName()+"'";
                System.out.println("[removeObject] Ejecutando "+sentence);
                Query query = em.createQuery(sentence);
                ClassMetadata myClass = (ClassMetadata)query.getSingleResult();
                for (ClassMetadata possibleChild : myClass.getPossibleChildren()){
                    sentence = "SELECT x FROM "+possibleChild.getName()+" x WHERE x.parent="+obj.getId();
                    System.out.println("[removeObject] Ejecutando "+sentence);
                    query = em.createQuery(sentence);
                    for (Object removable : query.getResultList()){
                        //TODO Código de verificación de integridad
                        if (!((RootObject)removable).getIsLocked())
                            em.remove(removable);
                    }
                }
                em.remove(obj);
            }catch (Exception e){
                this.error = e.getMessage();
                return false;
            }
        }
        else {
            this.error = "El EntityManager no existe";
            return false;
        }
        return true;
    }

    public ClassInfoLight[] getLightMetadata() {
        System.out.println("[getLightMetadata] Llamado");
        if (em != null){
            String sentence = "SELECT x FROM ClassMetadata x ORDER BY x.name";
            Query q = em.createQuery(sentence);
            List<ClassMetadata> cr = q.getResultList();
            ClassInfoLight[] cml = new ClassInfoLight[cr.size()];
            int i=0;
            for (ClassMetadata myClass : cr){
                cml[i] = new ClassInfoLight(myClass.getId(),myClass.getName(),myClass.getPackageInfo().getName());
                i++;
            }
            return cml;
        }
        else {
            this.error = "El EntityManager no existe";
            return null;
        }
    }
}