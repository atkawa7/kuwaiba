/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.inventory.modules.mpls.actions;

import com.neotropic.inventory.modules.mpls.views.MplsLinkEndPointsViewTopComponent;
import com.neotropic.inventory.modules.mpls.views.MplsLinkEndpointsViewScene;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.TopComponent;

/**
 * Opens an end-to-end view of a mpls link, by trying to match the endpoints of the link
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@ActionsGroupType(group=ActionsGroupType.Group.OPEN_VIEW)
@ServiceProvider(service = GenericObjectNodeAction.class)
public class ShowMPLSEndopintsView extends GenericObjectNodeAction {

    public ShowMPLSEndopintsView() {
         putValue(NAME, "MPLS Endpoints View");
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_MPLSLINK};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SERVICE_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         if (selectedObjects.size() != 1)
            JOptionPane.showMessageDialog(null, "Select only one node service.", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        else{
            TopComponent endToEndTC = new MplsLinkEndPointsViewTopComponent(selectedObjects.get(0), new MplsLinkEndpointsViewScene());
            endToEndTC.open();
            endToEndTC.requestActive();
        }
    }
    
}
