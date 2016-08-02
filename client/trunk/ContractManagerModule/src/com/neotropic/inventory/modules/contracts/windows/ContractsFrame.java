/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.contracts.windows;

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
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ContractsFrame extends JFrame {
    
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAvailableContracts;
    private LocalObjectLight[] selectedObjects;
    private List<LocalObjectLight> contracts;
    
    
    public ContractsFrame(LocalObjectLight[] selectedObjects, List<LocalObjectLight> contracts) {
        this.selectedObjects = selectedObjects;
        this.contracts = contracts;
        setLayout(new BorderLayout());
        setTitle("Available Contracts");
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel("Select a contract from the list");
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableContracts = new JList<>(contracts.toArray(new LocalObjectLight[0]));
        lstAvailableContracts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        pnlScrollMain.setViewportView(lstAvailableContracts);
        add(lstAvailableContracts, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create relationship");
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
            if (lstAvailableContracts.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a contract from the list");
            else{
                String [] objectsClassName = new String[selectedObjects.length];
                Long [] objectsId = new Long[selectedObjects.length];
                for(int i = 0; i < selectedObjects.length; i++){
                    objectsClassName[i] = selectedObjects[i].getClassName();
                    objectsId [i] = selectedObjects[i].getOid();
                }
                
                if (CommunicationsStub.getInstance().associateObjectsToContract(
                    objectsClassName, objectsId, 
                    ((LocalObjectLight)lstAvailableContracts.getSelectedValue()).getClassName(),
                    ((LocalObjectLight)lstAvailableContracts.getSelectedValue()).getOid())){
                        JOptionPane.showMessageDialog(null, String.format("%s object was related to %s", selectedObjects.length, lstAvailableContracts.getSelectedValue()));
                        dispose();
                }
                else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void servicesFilter(String text){
        List<LocalObjectLight> filteredContracts = new ArrayList<>();
        for(LocalObjectLight service : contracts){
            if(service.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || service.getName().toLowerCase().contains(text.toLowerCase()))
                filteredContracts.add(service);
        }
        lstAvailableContracts.setListData(filteredContracts.toArray(new LocalObjectLight[0]));
    }
}