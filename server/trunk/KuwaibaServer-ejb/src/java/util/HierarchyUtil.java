/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package util;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class HierarchyUtil {

    /*
     * Este método dice si una clase dada es subclase de otra
     * @return true si es subclase, false si no
     * @param child Clase que se desea probar
     * @param allegedParent Clase presuntamente padre de child
     */
    public static boolean isSubclass (Class child, Class allegedParent){
        Class myClass=child;
        while (!myClass.equals(Object.class)){
            if (myClass.equals(allegedParent))
                return true;
            myClass = myClass.getSuperclass();
        }
        return false;
    }
}
