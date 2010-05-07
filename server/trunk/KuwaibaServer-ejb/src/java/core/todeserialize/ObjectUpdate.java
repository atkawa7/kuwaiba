package core.todeserialize;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;


/**
 * Esta clase representa una actualización del objeto
 *
 * TODO: Por el momento la dejo separada porque intuyo que será necesario, pero así de
 * primerazo parece que usar un RemoteObject podría servir igual
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //Esta anotación le dice al serializador que incluya TODOS
                                      //los atributos sin importar su acceso (public, private, etc)
                                      //Por defecto, él coge sólo los public
public class ObjectUpdate {
    private Long oid;
    private String classname;
    private String[] updatedAttributes;
    private String[] newValues;

    public ObjectUpdate() {
    }

    public String getClassname() {
        return classname;
    }

   public String[] getNewValues() {
        return newValues;
    }

   public Long getOid() {
        return oid;
    }


    public String[] getUpdatedAttributes() {
        return updatedAttributes;
    }
}