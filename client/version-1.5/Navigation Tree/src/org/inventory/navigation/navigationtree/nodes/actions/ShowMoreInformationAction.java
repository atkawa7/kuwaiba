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
package org.inventory.navigation.navigationtree.nodes.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;

/**
 * Shows the database id of the selected object. Useful for troubleshooting purposes. It will also show the object's complete containment structure.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ShowMoreInformationAction extends GenericObjectNodeAction {
    private static ShowMoreInformationAction instance;
    private long id;
    private String className;

    private ShowMoreInformationAction() {
        putValue(NAME, "Show More Information");
    }
    
    public static ShowMoreInformationAction getInstance(long id, String className) {
        if (instance == null)        
            instance = new ShowMoreInformationAction();
        instance.setId(id);
        instance.setClassName(className);
        return instance;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        List<LocalObjectLight> parents = CommunicationsStub.getInstance().getParents(className, id);
        String msg = "";
        if (parents != null){
            for (LocalObjectLight parent : parents)
                msg +=  ":" +parent;
        }
        JOptionPane.showMessageDialog(null, 
                new SelectableLabel("<strong>id:</strong> " + id + "<br/><strong>Class: </strong>"+ className +"<br/><strong>Containment Path: </strong>" + msg), //NOI18N
                "Extra Information",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public String getValidator() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
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