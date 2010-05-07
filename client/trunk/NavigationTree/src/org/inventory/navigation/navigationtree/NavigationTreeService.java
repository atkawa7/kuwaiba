package org.inventory.navigation.navigationtree;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.util.Lookup;

/**
 * Contiene la lógica de negocio para el componente principal
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class NavigationTreeService {
    private NavigationTreeTopComponent component;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public NavigationTreeService(NavigationTreeTopComponent _component){
        this.component = _component;
    }
    public LocalObjectLight[] getRootChildren(){
        if(com.getRootNode())
            return com.getContextChildren();
        else{
            NotificationUtil nu = Lookup.getDefault().
                lookup(NotificationUtil.class);
            if (nu == null)
                System.out.println("[TreeManager:Constructor] Error: "+com.getError());
            else
                nu.showSimplePopup("Error en la Creación de Árbol", NotificationUtil.ERROR, com.getError());
            return null;
        }
    }
}
