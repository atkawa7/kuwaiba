package org.inventory.core.services.interfaces;

/**
 * Esta es la interfaz que expone un objeto local pero sólo de despliegue, es decir
 * finalmente se ve reflejado en un nodo del árbol de navegación
 * 
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalObjectLight {
    public String getClassName();
    public Long getOid();
    public Boolean hasChildren();
    public String getDisplayname();
    public String getPackageName();
}
