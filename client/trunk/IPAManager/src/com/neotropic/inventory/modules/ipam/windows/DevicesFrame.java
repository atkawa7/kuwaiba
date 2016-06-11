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
 * Show the existing generic communications elements that can be associated to 
 * an IP Address
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DevicesFrame extends JFrame{
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList lstAvailableDevices;
    private LocalObjectLight[] selectedIps;
    private LocalObjectLight[] devices;
    
    public DevicesFrame(LocalObjectLight[] selectedIps, LocalObjectLight[] devices) {
        this.selectedIps = selectedIps;
        this.devices = devices;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_DEVICES"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_DEVICE"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableDevices = new JList<>(devices);
        lstAvailableDevices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        pnlScrollMain.setViewportView(lstAvailableDevices);
        add(lstAvailableDevices, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new DevicesFrame.BtnConnectActionListener());
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
            if (lstAvailableDevices.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a service from the list");
            else{
                Long [] objectsId = new Long[selectedIps.length];
                String [] objectsName = new String[selectedIps.length];
                for(int i=0; i<selectedIps.length; i++){
                    objectsId [i] = selectedIps[i].getOid();
                    objectsName[i] = selectedIps[i].getName();
                }
                
                if (CommunicationsStub.getInstance().relateIPtoDevice(objectsId[0], 
                        ((LocalObjectLight)lstAvailableDevices.getSelectedValue()).getClassName(),
                        ((LocalObjectLight)lstAvailableDevices.getSelectedValue()).getOid())){
                    JOptionPane.showMessageDialog(null, String.format("The IP: %s  was related to %s", 
                            objectsName[0], ((LocalObjectLight)lstAvailableDevices.getSelectedValue()).getName()));
                        dispose();
                }else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void servicesFilter(String text){
        List<LocalObjectLight> filteredServices = new ArrayList<>();
        for(LocalObjectLight device : devices){
            if(device.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || device.getName().toLowerCase().contains(text.toLowerCase()))
                filteredServices.add(device);
        }
        LocalObjectLight[] toArray = filteredServices.toArray(new LocalObjectLight[filteredServices.size()]);
        lstAvailableDevices.setListData(toArray);
    }
}
