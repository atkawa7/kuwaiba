package core.toserialize;

import core.todeserialize.ObjectUpdate;
import entity.multiple.GenericObjectList;
import entity.relations.GenericRelation;
import java.util.Set;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import util.HierarchyUtil;

/**
 * Representa una actualización, pero deserializada (desde el punto de vista de aplicación)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class RemoteObjectUpdate {
    private Class objectClass = null;
    private Long oid;
    private Attribute[] updatedAttributes;
    private Object[] newValues;

    public RemoteObjectUpdate(ObjectUpdate object, Set<EntityType<?>> classSpace)
            throws ClassNotFoundException{

        EntityType ent = null;

        this.oid=object.getOid();

        for(EntityType entity : classSpace)
                            if(entity.getJavaType().getSimpleName().equals(object.getClassname())){
                                this.objectClass = entity.getJavaType();
                                ent =entity;
                                break;
                            }
                
        if(objectClass == null)
            throw new ClassNotFoundException("No se encontró la clase "+object.getClassname());

        newValues = new Object[object.getUpdatedAttributes().length];
        updatedAttributes = new Attribute[object.getUpdatedAttributes().length];
        
        for (int i = 0; i < object.getNewValues().length;i++){
            updatedAttributes[i] = ent.getAttribute(object.getUpdatedAttributes()[i]);
            if (updatedAttributes[i].getJavaType().equals(String.class))
                this.newValues[i] = object.getNewValues()[i];
            else
                if(updatedAttributes[i].getJavaType().equals(Boolean.class))
                    this.newValues[i] = Boolean.valueOf(object.getNewValues()[i]);
                else
                    if(updatedAttributes[i].getJavaType().equals(Long.class))
                        this.newValues[i] = Long.valueOf(object.getNewValues()[i]);
                    else
                        if(updatedAttributes[i].getJavaType().equals(Integer.class))
                            this.newValues[i] = Integer.valueOf(object.getNewValues()[i]);
                        else
                            if(HierarchyUtil.
                                    isSubclass(updatedAttributes[i].getJavaType(),GenericObjectList.class) ||
                               HierarchyUtil.
                                    isSubclass(updatedAttributes[i].getJavaType(),GenericRelation.class))
                                this.newValues[i] = Long.valueOf(object.getNewValues()[i]);
        }
    }

    public Object[] getNewValues() {
        return newValues;
    }

    public Class getObjectClass() {
        return objectClass;
    }

    public Long getOid() {
        return oid;
    }

    public Attribute[] getUpdatedAttributes() {
        return updatedAttributes;
    }

     /*
     * Genera el texto de la consulta que actualiza el objeto asociado
     * @return El texto del query para ser ejecutado
     */
    public String generateQueryText(){
    String query="UPDATE "+this.objectClass.getSimpleName()+" obj SET ";
    for (int i=0; i<this.updatedAttributes.length;i++){
        String value = "";
        String att="";
        if(this.updatedAttributes[i].getJavaType().equals(String.class))
            value="'"+(String)this.newValues[i]+"'";
        else
            value = this.newValues[i].toString();
        if (HierarchyUtil.isSubclass(updatedAttributes[i].getJavaType(),GenericObjectList.class) ||
                           HierarchyUtil.
                                isSubclass(updatedAttributes[i].getJavaType(),GenericRelation.class)){
            att= this.updatedAttributes[i].getName()+"_id";
            if (value.equals("0")) //Si es relación, y el id es 0, es porque en la lista se escogió "<ninguno>"
                value="NULL";
        }
        else
            att= this.updatedAttributes[i].getName();
        query+=att+"="+value+",";
    }
    query = query.substring(0, query.length()-1);
    query +=" WHERE obj.id="+this.oid;
    return query;
    }
}
