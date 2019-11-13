/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.navtree.dashboard;

import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboard;
import org.kuwaiba.apis.web.gui.dashboards.layouts.ShelfDashboardLayout;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class NavigationTreeDashboard extends AbstractDashboard {
    
    public NavigationTreeDashboard(RemoteObjectLight selectedObject, WebserviceBean webserviceBean) {
        super(selectedObject.toString(), new ShelfDashboardLayout(selectedObject.toString(), String.format("Object id: %s", selectedObject.getId())));
    }
    
}
