/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.inventory.objectcache;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalClassMetadata;

/**
 * Esta clase definitivamente evolucionará bastante en la estructura del caché
 * TODO: Evaluar si sería conveniente crear lookups para administrar el caché
 * digamos que sería una forma más natural de hacerlo.
 * TODO: Hacer que los servicios no dependan de la caché, sino que la use a través de su interfaz
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class Cache{
    private static Cache instance;
    private List<LocalObject> objectCache; //Provee caché para objetos (LocalObjects)
    private Dictionary<String,LocalClassMetadata> metaCache; //provee caché para las meta
    private boolean hasAllMeta=false; //Indica si se ha llenado toda la cache de metas para no volver a pedirla
                                      //y asumir que la que se encuentra está completa
                                      //Esto es útil para los casos administrativos en los que se pide
                                      //Toda la caché (digamos para editar atributos) y no pedirla nuevamente
                                      //si se editarán las clases como tal

    private Cache(){
        this.objectCache = new ArrayList<LocalObject>();
        this.metaCache = new Hashtable<String, LocalClassMetadata>();
        this.hasAllMeta = false;
    }

    /**
     * El caché es un singleton también
     * @return
     */
    public static Cache getInstace(){
        if(instance == null) instance = new Cache();
        return instance;
    }

    /*
     * TODO: Implementarlo
     */
    public LocalObject getFirstObject() {
        //return this.objectCache.pop();
        return this.objectCache.get(0);
    }

    public void addObject(LocalObject lo) {
        this.objectCache.add(lo);
    }

    public List<LocalObject> getObjectCache() {
        return this.objectCache;
    }

    public LocalClassMetadata[] getMetaCache(){
        LocalClassMetadata[] res = new LocalClassMetadata[this.metaCache.size()];
        int i = 0;
        Enumeration keys = metaCache.keys();
        while(keys.hasMoreElements()){
            res[i] = metaCache.get(keys.nextElement());
            i++;
        }
        return res;
    }

    public void refresh(){}

    public void resetMetaCache(){
        int i = 0;
        Enumeration keys = metaCache.keys();
        while(keys.hasMoreElements()){
            metaCache.remove(keys.nextElement());
            i++;
        }
    }

    public LocalClassMetadata getMetaForClass(String className) {
        return this.metaCache.get(className);
    }

    public void addMeta(LocalClassMetadata li) {
        this.metaCache.put(li.getClassName(),li);
    }

    public void addMultipleMeta(LocalClassMetadata[] all){
        for (LocalClassMetadata lcmi : all)
            this.addMeta(lcmi);
    }

    public boolean hasAllMeta() {
        return hasAllMeta;
    }

    public void setHasAllMeta(boolean hasAllMeta) {
        this.hasAllMeta = hasAllMeta;
    }

    public LocalClassMetadataLight[] getLightMetaCache() {
        return new LocalClassMetadataLight[0];
    }
}