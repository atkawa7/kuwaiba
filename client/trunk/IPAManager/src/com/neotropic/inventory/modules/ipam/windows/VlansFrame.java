/*
 * Copyright (c) 2016 adrian.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    adrian - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.ipam.windows;

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
 * Show the existing VLANS that can be associated to a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class VlansFrame extends JFrame{
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAvailableVlans;
    private LocalObjectLight[] selectedSubnets;
    private LocalObjectLight[] vlans;
    
    public VlansFrame(LocalObjectLight[] selectedSubnets, LocalObjectLight[] vlans) {
        this.selectedSubnets = selectedSubnets;
        this.vlans = vlans;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_VLANS"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_VLAN"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableVlans = new JList<>(vlans);
        lstAvailableVlans.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        pnlScrollMain.setViewportView(lstAvailableVlans);
        add(lstAvailableVlans, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new VlansFrame.BtnConnectActionListener());
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
            if (lstAvailableVlans.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a service from the list");
            else{
                Long [] objectsId = new Long[selectedSubnets.length];
                String [] objectsNames = new String[selectedSubnets.length];
                for(int i=0; i<selectedSubnets.length; i++){
                    objectsId [i] = selectedSubnets[i].getOid();
                    objectsNames[i] = selectedSubnets[i].getName();
                }
                if (CommunicationsStub.getInstance().relateSubnetToVLAN(
                        objectsId[0], ((LocalObjectLight)lstAvailableVlans.getSelectedValue()).getOid())){
                    JOptionPane.showMessageDialog(null, String.format("The %s subnet was related to VLAN %s", objectsNames[0], 
                            ((LocalObjectLight)lstAvailableVlans.getSelectedValue()).getName()));
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
        for(LocalObjectLight vlan : vlans){
            if(vlan.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || vlan.getName().toLowerCase().contains(text.toLowerCase()))
                filteredServices.add(vlan);
        }
        LocalObjectLight[] toArray = filteredServices.toArray(new LocalObjectLight[filteredServices.size()]);
        lstAvailableVlans.setListData(toArray);
    }
}
