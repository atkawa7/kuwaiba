/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServicesFrame extends JFrame{
    private JScrollPane pnlScrollMain;
    private JList lstAvailableServices;
    private LocalObjectLight element;

    public ServicesFrame(LocalObjectLight element, LocalObjectLight[] services) {
        this.element = element;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TITLE_AVAILABLE_SERVICES"));
        setSize(300, 700);
        
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_INSTRUCTIONS_SELECT_SERVICE"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(lblInstructions, BorderLayout.NORTH);
        
        lstAvailableServices = new JList(services);
        pnlScrollMain = new JScrollPane();
        pnlScrollMain.setViewportView(lstAvailableServices);
        add(pnlScrollMain, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnConnect = new JButton("Make relationship");
        btnConnect.addActionListener(new BtnConnectActionListener());
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        pnlButtons.add(btnClose);
        add(pnlButtons, BorderLayout.SOUTH);
        
    }
    
    private class BtnConnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAvailableServices.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a service from the list");
            else{
                if (CommunicationsStub.getInstance().associateObjectToService(
                        element.getClassName(), element.getOid(), 
                        ((LocalObjectLight)lstAvailableServices.getSelectedValue()).getClassName(),
                        ((LocalObjectLight)lstAvailableServices.getSelectedValue()).getOid()))
                    JOptionPane.showMessageDialog(null, String.format("%s was related to %s", element, lstAvailableServices.getSelectedValue()));
                else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError());
                dispose();
            }
        }
    }
}