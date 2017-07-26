/*
 * Copyright (c) 2017 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org> - initial API and implementation and/or initial documentation
 */

package org.inventory.models.physicalconnections.actions;

import java.awt.BorderLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * With this action is possible to configure what kind of links can be connected to certain types of ports (e.g. OpticalLinks should be connected only to OpticalPorts) and how to define connectors compatibility
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ActionID(id = "org.inventory.models.physicalconnections.actions.ConfigureConnectorMatchingAction", category = "Tools")
@ActionRegistration(displayName = "Configure Connection Rules")
@ActionReference(path = "Menu/Tools/Advanced", separatorBefore =  29, name = "org-inventory-models-physicalconnections-actions-ConfigureConnectorMatchingAction", position = 30)
public class ConfigureConnectorMatchingAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        JOptionPane.showMessageDialog(null, "Not implemented yet");
    }
    
    private class MatchingRulesFrame extends JFrame {
        private JList lstRules;
        private JButton btnAddRule;
        private JButton btnDeleteRule;
        
        public MatchingRulesFrame() {
            setLayout(new BorderLayout());
        }
        
    }
    
}
