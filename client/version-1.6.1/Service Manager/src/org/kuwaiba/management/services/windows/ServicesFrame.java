/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.management.services.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;

/**
 * Shows the available services to associate to a resource
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ServicesFrame extends JFrame {
    
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList<LocalObjectLight> lstAvailableServices;
    private List<LocalObjectLight> selectedObjects;
    private List<LocalObjectLight> services;
    
    
    public ServicesFrame(List<LocalObjectLight> selectedObjects, List<LocalObjectLight> services) {
        this.selectedObjects = selectedObjects;
        this.services = services;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_TITLE_AVAILABLE_SERVICES"));
        setSize(400, 650);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/kuwaiba/management/services/Bundle").getString("LBL_INSTRUCTIONS_SELECT_SERVICE"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableServices = new JList<>(services.toArray(new LocalObjectLight[0]));
        lstAvailableServices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pnlScrollMain = new JScrollPane();
        txtField = new JTextField();
        txtField.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2));
        txtField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                servicesFilter(txtField.getText());
            }
        });
        
        pnlSearch.add(lblInstructions);
        pnlSearch.add(txtField);
        add(pnlSearch, BorderLayout.NORTH);
        
        pnlScrollMain.setViewportView(lstAvailableServices);
        add(lstAvailableServices, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create Relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BtnConnectActionListener());
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
                List<String> classNames = new ArrayList<>();
                List<Long> objectIds = new ArrayList<>();
                for(LocalObjectLight selectedObject : selectedObjects){
                    classNames.add(selectedObject.getClassName());
                    objectIds.add(selectedObject.getOid());
                }
                
                if (CommunicationsStub.getInstance().associateObjectsToService(
                    classNames, objectIds, 
                    (lstAvailableServices.getSelectedValue()).getClassName(),
                    (lstAvailableServices.getSelectedValue()).getOid())){
                        JOptionPane.showMessageDialog(null, String.format(selectedObjects.size() > 1 ? 
                                "%s obejcts were related to service %s" : "%s object was related to service %s", selectedObjects.size(), lstAvailableServices.getSelectedValue()));
                        dispose();
                }
                else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void servicesFilter(String text){
        List<LocalObjectLight> filteredServices = new ArrayList<>();
        for(LocalObjectLight service : services){
            if(service.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || service.getName().toLowerCase().contains(text.toLowerCase()))
                filteredServices.add(service);
        }
        LocalObjectLight[] toArray = filteredServices.toArray(new LocalObjectLight[filteredServices.size()]);
        lstAvailableServices.setListData(toArray);
    }
}