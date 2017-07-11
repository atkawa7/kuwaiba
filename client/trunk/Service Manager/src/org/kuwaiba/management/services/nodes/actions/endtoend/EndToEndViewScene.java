/*
 * Copyright (c) 2017 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org> - initial API and implementation and/or initial documentation
 */

package org.kuwaiba.management.services.nodes.actions.endtoend;

import java.awt.Color;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight>{
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    @Override
    public byte[] getAsXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException { }

    @Override
    public void render(LocalObjectLight selectedService) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(selectedService.getClassName(), selectedService.getOid());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            for (LocalObjectLight serviceResource : serviceResources) {
                
            }
        }
    }

    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) { return Color.BLACK; }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight arg0, LocalObjectLight arg1, LocalObjectLight arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight arg0, LocalObjectLight arg1, LocalObjectLight arg2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
