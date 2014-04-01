/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.navigation.navigationtree.windows.SpecialRelationshipsTopComponent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Gets the selected object special relationships
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionID(category = "Tools", id = "org.inventory.navigation.navigationtree.actions.ShowObjectSpecialRelationshipsAction")
@ActionRegistration(iconBase="org/inventory/navigation/navigationtree/res/special_relationships_explorer.png", displayName = "#CTL_ShowSpecialRelationships")
@ActionReference(path = "Menu/Tools/Navigation")
@NbBundle.Messages({"CTL_ShowSpecialRelationships=Show special relationships"})
public final class ShowObjectSpecialRelationshipsAction extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent ev) {
        SpecialRelationshipsTopComponent tc = SpecialRelationshipsTopComponent.getInstance();
        tc.open();
        tc.requestActive();
    }
}