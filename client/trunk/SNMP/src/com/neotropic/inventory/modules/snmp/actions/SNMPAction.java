/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.inventory.modules.snmp.actions;

import com.neotropic.inventory.modules.snmp.ConfigDataSourceFrame;
import com.neotropic.inventory.modules.snmp.DataSourceFrame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;


/**
 * Allow the load a csv file to bulk upload of objects and list types
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */

@ActionID(category = "Tools", id = "com.neotropic.inventory.modules.snmp.actions.SNMPAction")
@ActionRegistration(iconBase="com/neotropic/inventory/modules/snmp/res/sync.png", displayName = "#CTL_SNMPSync")
@ActionReference(path = "Menu/Tools")
@NbBundle.Messages({"CTL_SNMPSync=SNMP Sync"})
public class SNMPAction  extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent e) {
        DataSourceFrame uf = DataSourceFrame.getInstance();
        uf.setLocationRelativeTo(null);
        uf.setVisible(true);
    }
}
