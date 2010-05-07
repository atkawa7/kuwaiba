package core.toserialize;

import entity.core.RootObject;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Este clase representa los elementos que aparecen en los árboles de navegación
 * es sólo información de despliegue (sin detalle)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteObjectLight {
    private String displayName; //Es privado porque no se hereda al RemoteObject ya que él arma su displayname a partir de sus atributos
    protected Long oid;
    protected String className;
    protected String packageName;
    protected Boolean hasChildren;
    

    public RemoteObjectLight(){} //Requerido por el webservice
    /*public RemoteObjectLight(RootObject ro, EntityManager em){
        this.className = ro.getClass().getSimpleName();
        this.displayName = ro.getDisplayName(); //TODO: Corregir
        this.oid = ro.getId();
        this.hasChildren = false; //Sólo para inicializarlo

        //TODO: Cómo hacer para que este resultado quede cacheado y no preguntarlo cada vez?
        Query q = em.createQuery("SELECT ch FROM ContainerHierarchy ch WHERE ch.parentClass='"+this.className+"'");
        try{
            ContainerHierarchy hierarchy = (ContainerHierarchy)q.getSingleResult();
            ClassRegistry[] possibleChildren = hierarchy.getPossibleChildren();
            for(int i = 0; i<possibleChildren.length;i++){
                q = em.createQuery("SELECT ch FROM "+possibleChildren[i]+" ch WHERE ch.parent ="+this.getOid());
                if(q.getResultList().size()>0){
                    this.hasChildren = true;
                    break;
                }
            }

        }catch (NoResultException nre){
            this.hasChildren = false; //De hecho, esta clase no puede tener hijos
        }
        //Esto es erróneo. Debe haber un problema con el meta, ya que no puede haber
        //más de una entrada para un parentClass dado.
        catch (NonUniqueResultException nure){
            this.hasChildren = true;
        }
    }*/

    //Esto funciona suponiendo que el Object es realmente un RemoteObject
    //No se coloca realmente el Remote Object para esconderle al ws lo que hay por detrás
    public RemoteObjectLight(Object obj){
        this.className = obj.getClass().getSimpleName();
        this.packageName = obj.getClass().getPackage().getName();
        this.displayName = ((RootObject)obj).getName(); //TODO: El displayName customizado debería salir de una regla de negocio
        this.oid = ((RootObject)obj).getId();
        this.hasChildren = true;
    }

    public String getClassName() {
        return className;
    }

    //No se debe heredar, ya que el atributo es privado, el RemoteObject no lo tiene
    public final String getDisplayName() {
        return displayName;
    }

    public Boolean hasChildren() {
        return hasChildren;
    }

    public Long getOid() {
        return oid;
    }

    /*
     * Útil para transformar las respuestas de las consultas, que son objetos de
     * diferentes clases a respuestas serializables del ws
     * @param objs los objetos que serán transformados en ROL
     * @return un arreglo de ROL ya convertido
     */
    public static RemoteObjectLight[] toArray(List objs){
        RemoteObjectLight[] res = new RemoteObjectLight[objs.size()];
        int i=0;
        for (Object obj : objs){
            res[i] = new RemoteObjectLight(obj);
            i++;
        }
        return res;
    }
}