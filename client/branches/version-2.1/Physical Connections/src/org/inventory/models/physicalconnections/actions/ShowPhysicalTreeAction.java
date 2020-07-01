/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.models.physicalconnections.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.models.physicalconnections.windows.PhysicalTreeTopComponent;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ServiceProvider;

/**
 * This action shows the physical trace from a port
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class ShowPhysicalTreeAction extends GenericObjectNodeAction implements Presenter.Popup {
    
    public ShowPhysicalTreeAction() {
        putValue(NAME, I18N.gm("show_physical_tree"));
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String [] {Constants.CLASS_GENERICPORT};
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HashMap<LocalObjectLight, List<LocalObjectLight>> tree = CommunicationsStub.getInstance().getPhysicalTree(selectedObjects.get(0).getClassName(), selectedObjects.get(0).getId());
        if (tree != null) {
            PhysicalTreeTopComponent tc = new PhysicalTreeTopComponent(selectedObjects.get(0), tree);
            tc.open();
            tc.requestActive();
        }
        else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());    
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }
    
}