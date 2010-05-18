package org.inventory.navigation.navigationtree;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.util.Lookup;

/**
 * Provides the business logic for the related TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class NavigationTreeService {
    private NavigationTreeTopComponent component;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public NavigationTreeService(NavigationTreeTopComponent _component){
        this.component = _component;
    }
    public LocalObjectLight[] getRootChildren(){
        LocalObjectLight[] rootChildren = com.getRootNodeChildren();
        if(rootChildren != null)
            return rootChildren;
        else{
            NotificationUtil nu = Lookup.getDefault().
                lookup(NotificationUtil.class);
            if (nu == null)
                System.out.println(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("DBG_CREATION_ERROR")+com.getError());
            else
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_TITLE_CREATION"), NotificationUtil.ERROR, com.getError());
            return null;
        }
    }
}
