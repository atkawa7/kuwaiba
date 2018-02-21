/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.management.services.views.topology.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.management.services.views.topology.TopologyViewScene;

/**
 * Replaces the selected transport link with the containers within (if any). If there isn't any, a message will be displayed.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DisaggregateTransportLinkAction extends GenericInventoryAction {
    private TopologyViewScene scene;
    public DisaggregateTransportLinkAction(TopologyViewScene scene) {
        this.putValue(NAME, "Disaggregate Transport Link"); 
        this.scene = scene;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Object selectedObject : scene.getSelectedObjects()) {
            LocalObjectLight castedTransportLink = (LocalObjectLight)selectedObject;
            if (!CommunicationsStub.getInstance().isSubclassOf(castedTransportLink.getClassName(), "GenericSDHTransportLink")) {
                JOptionPane.showMessageDialog(null, String.format("%s is not a transport link", castedTransportLink), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            
            List<LocalObjectLight> containerLinks = CommunicationsStub.getInstance().
                    getSpecialAttribute(castedTransportLink.getClassName(), castedTransportLink.getOid(), "sdhTransports"); //NOI18N
            
            if (containerLinks != null) {
                if (!containerLinks.isEmpty()) {
                    scene.getExpandedTransportLinks().put(castedTransportLink, containerLinks); //This map will be used to gracefully collapse all the STMX that will be expanded in this operation

                    for (LocalObjectLight containerLink : containerLinks) {
                        if (scene.findWidget(containerLink) == null) { //This validation should not be necessary, but just in case
                            scene.addEdge(containerLink);
                            scene.setEdgeSource(containerLink, scene.getEdgeSource(castedTransportLink));
                            scene.setEdgeTarget(containerLink, scene.getEdgeTarget(castedTransportLink));
                        }
                    }
                    //The STMX is only set invisible if it transports something
                    scene.findWidget(castedTransportLink).setVisible(false);
                    scene.validate();
                } else
                    JOptionPane.showMessageDialog(null, "The selected transport link is not transporting any container");
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            
        }
    }
}