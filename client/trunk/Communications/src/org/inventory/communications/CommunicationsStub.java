package org.inventory.communications;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.core.LocalClassMetadataImpl;
import org.inventory.communications.core.LocalClassMetadataLightImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.communications.core.LocalObjectListItemImpl;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.LocalObjectListItem;
import org.inventory.webservice.ClassInfo;
import org.inventory.webservice.ClassInfoLight;
import org.inventory.webservice.KuwaibaWebservice;
import org.inventory.webservice.KuwaibaWebserviceService;
import org.inventory.webservice.ObjectList;
import org.inventory.webservice.ObjectList.List.Entry;
import org.inventory.webservice.ObjectUpdate;
import org.inventory.webservice.RemoteObjectLight;
import org.inventory.webservice.RemoteTreeNodeLight;

/**
 * Clase singleton que le permite a todos los módulos comunicarse con el server
 * TODO: hacerla Threadeada para los casos en los que se requieran comunicaciones simultáneas de objetos
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class CommunicationsStub {
    private static CommunicationsStub instance=null;
    private KuwaibaWebserviceService service;
    private KuwaibaWebservice port;
    private String error="No se ha definido errores";
    private LocalObjectLightImpl context;
    private LocalObjectLightImpl[] contextChildren;
    
    //Implementaremos un patrón de diseño singleton
    private CommunicationsStub(){
        this.service = new KuwaibaWebserviceService();
        this.port = service.getKuwaibaWebservicePort();
    }

    public static CommunicationsStub getInstance(){
            if(instance==null) instance = new CommunicationsStub();
            return instance;
    }

    public boolean getRootNode(){
        try{
            RemoteTreeNodeLight result = port.getRootNodeLight();
            if(result ==null){
                error = port.getLastErr();
                return false;
            }
        
            LocalObjectLightImpl[] children = new LocalObjectLightImpl[result.getChildren().size()];
            int i = 0;
            for (RemoteObjectLight obj : result.getChildren()){
                children[i] = new LocalObjectLightImpl(obj);
                i++;
            }

            contextChildren = children;
            return true;
        }catch(Exception connectException){ //TODO Averiguar por qué no deja que se capture una ConnectException
            this.error = "No fue posible establecer comunicación con el servidor";
            return false;
        }
    }

    public LocalObjectLightImpl[] getObjectChildren(Long oid, String objectClass){
        List <RemoteObjectLight> children = port.getObjectChildren(oid, objectClass);
        LocalObjectLightImpl[] res = new LocalObjectLightImpl[children.size()];
        int i = 0;
        for (RemoteObjectLight rol : children){
            res[i] = new LocalObjectLightImpl(rol);
            i++;
        }
        return res;
    }
    /*
     * @param obj contiene el objeto que s actualizará. Nótese que ese objeto no es el "original",
     * sino sólo parte de él. Sólo se colocan los atributos que han cambiado
     */
    public boolean saveObject(LocalObject obj){
        ObjectUpdate update = new ObjectUpdate();
        List<String> atts = new ArrayList<String>();
        List<String> vals = new ArrayList<String>();

        update.setClassname(obj.getClassName());
        update.setOid(obj.getOid());

        for (String key : obj.getAttributes().keySet()){
            atts.add(key);
            vals.add(obj.getAttribute(key).toString());
        }

        update.setUpdatedAttributes(atts);
        update.setNewValues(vals);

        boolean res = port.updateObject(update);
        if (!res) this.error = port.getLastErr();
        return res;
    }

    /*
     * Wrapper para la función ídem en el webservice que se encarga de bloquear
     * un objeto para sólo lectura ya que se está editando
     * @param oid El oid del objeto (identificador único)
     * @param objectClass Clase del objeto
     * @param value Valor al cual se fijará el lock (readonly o permiso completo)
     */
    public boolean setObjectLock(Long oid, String objectClass,Boolean value){
        return true;
    }

    /*
     * TODO: Hacer que se acceda al caché por medio del communications
     */
    public LocalObject getObjectInfo(String objectClass, Long oid, LocalClassMetadata lcmd){
        //En este caso sí es necesario conocer el meta, pero con el fin de no
        //tener una dependencia del communicationsstub a objectcache (quiero que
        //sea un módulo completamente stand-alone). Se delega entonce fijar el neta
        //en quien hace uso de este método
        LocalObjectImpl res = new LocalObjectImpl(port.getObjectInfo(objectClass, oid),lcmd);
        if (res == null){
            this.error = "No se debe haber encontrado el objeto";
        }
        return res;
    }

    public LocalObjectLightImpl[] getContextChildren(){
        return this.contextChildren;
    }

    public String getError() {
        return error;
    }

    public List<LocalClassMetadataLight> getPossibleChildren(String className) {
        List<ClassInfoLight> resAsRemote = port.getPossibleChildren(className);
        List<LocalClassMetadataLight> resAsLocal = new ArrayList<LocalClassMetadataLight>();
        if (port == null){
           this.error = port.getLastErr();
           return null;
        }
        for (ClassInfoLight cil : resAsRemote)
            resAsLocal.add(new LocalClassMetadataLightImpl(cil));
         return resAsLocal;
    }

    public LocalObjectLight createObject(String objectClass, Long parentOid, String template){
        return new LocalObjectLightImpl(port.createObject(objectClass, template,parentOid));
    }

    public LocalClassMetadata[] getAllMeta() {

        List<ClassInfo> metas;
        try{
            metas= port.getMetadata();
        }catch(Exception connectException){
            this.error = "No se pudo conectar con el servidor";
            return null;
        }
        LocalClassMetadata[] lm = new LocalClassMetadata[metas.size()];
        int i=0;
        for (ClassInfo cm : metas){
            lm[i] = new LocalClassMetadataImpl(cm);
            i++;
        }
        return lm;
    }

    /*
     * Trae la metainformación de una clase dada
     */
    public LocalClassMetadata getMetaForClass(String className){
        ClassInfo cm = port.getMetadataForClass(className);
        if (cm ==null){
            this.error = port.getLastErr();
            return null;
        }
        return new LocalClassMetadataImpl(cm);
    }

    //TODO: Por el momento, el valor es un string, pero es posible que en el futuro
    //evolucione a un objeto completo
    public LocalObjectListItem[] getList(String className){

        ObjectList list = port.getMultipleChoice(className);
        LocalObjectListItemImpl[] res=null;
        if (list == null)
            return res;

        //El +1 se debe a que se adiciona un ítem: el vació o nulo
        res = new LocalObjectListItemImpl[list.getList().getEntry().size() + 1];
        res[0] = new LocalObjectListItemImpl(LocalObjectListItem.NULL_ID,"","None");
        int i = 1;
        for(Entry entry : list.getList().getEntry()){
            res[i] = new LocalObjectListItemImpl(entry.getKey(),entry.getValue(),entry.getValue());
            i++;
        }

        return res;
    }

    public boolean addPossibleChildren(Long parentClassId, List<String> possibleChildren){
        boolean res = port.addPossibleChildren(parentClassId, possibleChildren);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }

    /*
     * Elimina un objeto dado
     * @param className Nombre de la clase a la que pertenece el objeto (incluyendo el paquete)
     * @param oid id del objeto
     * @return Éxito o fracaso
     */
    public boolean removeObject(String className, Long oid){
        boolean res = port.removeObject(className,oid);
        if (!res)
            this.error = port.getLastErr();
        return res;
    }
}
