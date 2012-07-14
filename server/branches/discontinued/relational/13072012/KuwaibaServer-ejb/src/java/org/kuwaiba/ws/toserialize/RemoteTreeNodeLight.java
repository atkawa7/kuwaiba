/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.kuwaiba.ws.toserialize;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD) //Esta anotación le dice al serializador que incluya TODOS
                                      //los atributos sin importar su acceso (public, private, etc)
                                      //Por defecto, él coge sólo los public
public class RemoteTreeNodeLight {
    private RemoteObjectLight root;
    private RemoteObjectLight[] children;

    public RemoteTreeNodeLight(Object object, Object[] children){
        if (object != null) //Es nulo cuando el padre es nulo, es decir, cuando no tiene padre
            this.root = new RemoteObjectLight(object);
        this.children = new RemoteObjectLight[children.length];
        int i = 0;
        for(Object obj : children){
            this.children[i] = new RemoteObjectLight(obj);
            i++;
        }
    }

    /*
     * Por alguna razón en tiempo de ejecución, el servlet que provee el ws requiere que esta clase
     * tenga un constructor sin argumentos. Recuérdese que esta clase es lo que se devuelve en la función getTreeNode
     */
    public RemoteTreeNodeLight(){

    }
}
