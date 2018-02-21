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

package org.inventory.utilities.bulk.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.utilities.bulk.BulkUploadFrame;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 * Allow the load a csv file to bulk upload of objects and list types
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */

@ActionID(category = "Tools", id = "org.inventory.utilities.actions.UploadFilesAction")
@ActionRegistration(iconBase="org/inventory/utilities/res/bulk.png", displayName = "#CTL_Uploadfile")
@ActionReference(path = "Menu/Tools")
@NbBundle.Messages({"CTL_Uploadfile=Bulk Import"})
public class UploadFilesAction  extends AbstractAction{

    @Override
    public void actionPerformed(ActionEvent e) {
        BulkUploadFrame uf = BulkUploadFrame.getInstance();
        uf.setLocationRelativeTo(null);
        uf.setVisible(true);
    }
}
