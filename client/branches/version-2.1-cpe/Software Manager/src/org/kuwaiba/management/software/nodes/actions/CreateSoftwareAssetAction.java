/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.software.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalValidator;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.special.children.SpecialChildrenTopComponent;
import org.openide.util.lookup.ServiceProvider;

/**
 * Creates a software asset and relates it to an element
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ServiceProvider(service=GenericObjectNodeAction.class)
public class CreateSoftwareAssetAction extends GenericObjectNodeAction {

    public CreateSoftwareAssetAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/kuwaiba/management/software/Bundle").getString("LBL_CREATE_SOFTWARE_ASSET"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        HashMap<String, Object> attributes = new HashMap<>();
        LocalObjectLight newLicense = CommunicationsStub.getInstance().createSpecialObject("SoftwareLicense", selectedObjects.get(0).getClassName(), 
                selectedObjects.get(0).getId(), attributes, null);
        if (newLicense == null)
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else{
            SpecialChildrenTopComponent explorer = SpecialChildrenTopComponent.getInstance();
            if (!explorer.isOpen())
                explorer.open();
            else
                explorer.refresh();
            explorer.requestActive();
        }
    }

    @Override
    public LocalValidator[] getValidators() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_SOFTWARE_ASSETS_MANAGER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public String[] appliesTo() {
        return new String[] {Constants.CLASS_GENERICAPPLICATIONELEMENT};
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}
