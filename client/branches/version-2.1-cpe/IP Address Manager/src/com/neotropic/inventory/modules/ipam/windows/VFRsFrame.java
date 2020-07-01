/**
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * Show the existing VFRs that can be associated to a subnet
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */

public class VFRsFrame  extends JFrame{
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList<LocalObjectLight> lstAvailableVFRs;
    private List<LocalObjectLight> selectedSubnet;
    private List<LocalObjectLight> vfrs;

    public VFRsFrame(List<LocalObjectLight> selectedSubnet, List<LocalObjectLight> vrfs) {
        this.selectedSubnet = selectedSubnet;
        this.vfrs = vrfs;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_VFRS"));
        setSize(400, 650);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_VFR"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableVFRs = new JList<>(vrfs.toArray(new LocalObjectLight[0]));
        lstAvailableVFRs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        pnlScrollMain.setViewportView(lstAvailableVFRs);
        add(lstAvailableVFRs, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create Relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new VFRsFrame.BtnConnectActionListener());
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
            if (lstAvailableVFRs.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a VFR from the list");
            else {
                if (CommunicationsStub.getInstance().relateSubnetToVFR(
                        selectedSubnet.get(0).getId(), selectedSubnet.get(0).getClassName(), lstAvailableVFRs.getSelectedValue().getId())){
                    JOptionPane.showMessageDialog(null, String.format("The %s subnet was related to VFR %s", selectedSubnet.get(0).getName(), 
                            lstAvailableVFRs.getSelectedValue().getName()));
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
        for(LocalObjectLight vrf : vfrs){
            if(vrf.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || vrf.getName().toLowerCase().contains(text.toLowerCase()))
                filteredServices.add(vrf);
        }
        LocalObjectLight[] toArray = filteredServices.toArray(new LocalObjectLight[filteredServices.size()]);
        lstAvailableVFRs.setListData(toArray);
    }

}
