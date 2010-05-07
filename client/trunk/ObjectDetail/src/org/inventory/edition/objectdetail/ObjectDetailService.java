package org.inventory.edition.objectdetail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.ObjectDetail;
import org.inventory.objectcache.Cache;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;

/**
 * Implementa la lógica de negocio del componente asociado
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectDetailService
        implements PropertyChangeListener, ObjectDetail, LookupListener{

    private ObjectDetailTopComponent component;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private Cache cache = Cache.getInstace();
    private LocalObject currentObject;
    private Result lr;

    public ObjectDetailService(ObjectDetailTopComponent _component){
        //lr = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        lr = Utilities.actionsGlobalContext().lookupResult(Node.class);
        lr.addLookupListener(this);
        this.component = _component;
    }

    //Métodos de la interfaz de cambio de propiedades
    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Métodos de la interfaz ObjectDetail
    public void show(LocalObject lo, boolean docked) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void rename(String newName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //Métodos de la interfaz LookupListener
     public void resultChanged(LookupEvent le) {
        System.out.println("[resultChanged]: Llamado");
        if(lr.allInstances().isEmpty())
            return;

        component.getpSheetView().setNodes((Node[]) lr.allInstances().
                toArray(new Node[0]));
        component.revalidate();
    }
}
