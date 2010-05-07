/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package core.toserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //Esta anotación le dice al serializador que incluya TODOS
                                      //los atributos sin importar su acceso (public, private, etc)
                                      //Por defecto, él coge sólo los public
public class RemoteTreeNode {
    private RemoteObject root;
    private RemoteObject[] children;

    public RemoteTreeNode(Object object, Object[] children){
        if (object != null) //Es nulo cuando el padre es nulo, es decir, cuando no tiene padre
            this.root = new RemoteObject(object);
        this.children = new RemoteObject[children.length];
        int i = 0;

        for(Object obj : children){
            this.children[i] = new RemoteObject(obj);
            i++;
        }
    }

    /*
     * Por alguna razón en tiempo de ejecución, el servlet que provee el ws requiere que esta clase
     * tenga un constructor sin argumentos. Recuérdese que esta clase es lo que se devuelve en la función getTreeNode
     */
    public RemoteTreeNode(){

    }
}
