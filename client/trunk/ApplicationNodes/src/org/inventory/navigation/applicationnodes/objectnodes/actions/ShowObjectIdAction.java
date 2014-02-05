/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Gets the selected object oid
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ShowObjectIdAction extends AbstractAction{
    private long id;
    private String className;

    public ShowObjectIdAction(long id, String className) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_OBJECT_ID_ACTION"));
        this.id  = id;
        this.className = className;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        LocalObjectLight[] parents = CommunicationsStub.getInstance().getParents(className, id);
        String msg = "";
        if (parents != null){
            for (LocalObjectLight parent : parents)
                msg +=  ":" +parent;
        }
        JOptionPane.showMessageDialog(null, 
                new SelectableLabel("<strong>id:</strong> " + id + "<br/><strong>Class: </strong>"+ className +"<br/><strong>Containment Path: </strong>" + msg), //NOI18N
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_OBJECT_ID_ACTION_TITLE"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private class SelectableLabel extends JTextPane {

        public SelectableLabel(String text) {
            setContentType("text/html");
            setText(text);
            setOpaque(false);
            setEditable(false);
            setBorder(null);
        }
    }
}