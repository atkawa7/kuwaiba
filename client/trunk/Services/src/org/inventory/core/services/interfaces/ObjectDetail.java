/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.core.services.interfaces;

/**
 *
 * @author dib
 */
public interface ObjectDetail <T extends Object> {
    /*
     * Muestra el editor de objetos
     * @param oid id del objeto
     * @param readOnly indica si debe abirlo en modo de solo lectura
     * @param docked indica si debe mostrarse anclado a la GUI o como una ventana aparte
     */
    public void show(LocalObject lo, boolean docked);

    /*
     * Cambia el nombre de un objeto desde otro módulo (pej.: Dándole F2 en el árbol de navegación)
     * Muestra el editor de objetos
     * @param oid id del objeto
     * @param newName El nuevo nombre del objeto
     */
    public void rename(String newName);

    /*
     * Refresca el propertysheet
     */
    public void refresh();
}
