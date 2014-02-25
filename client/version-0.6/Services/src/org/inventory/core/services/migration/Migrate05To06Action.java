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
package org.inventory.core.services.migration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Triggers a migration from version 0.5 to 0.6
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionID(category = "Window", id = "org.inventory.core.services.migration.Migrate05To06Action")
@ActionRegistration(displayName = "#CTL_Migrate05To06")
@ActionReference(path = "Menu/Tools/Migration")
@NbBundle.Messages({"CTL_Migrate05To06=v0.5.1 to v0.6"})
public class Migrate05To06Action implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Not implemented yet");
    }
    
}
