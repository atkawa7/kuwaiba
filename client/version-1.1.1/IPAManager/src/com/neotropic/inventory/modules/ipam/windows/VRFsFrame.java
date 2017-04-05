/**
 * Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 * Show the existing VRFs that can be associated to a subnet
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */

public class VRFsFrame  extends JFrame{
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList<LocalObjectLight> lstAvailableVRFs;
    private List<LocalObjectLight> selectedSubnet;
    private List<LocalObjectLight> vrfs;

        public VRFsFrame(List<LocalObjectLight> selectedSubnet, List<LocalObjectLight> vrfs) {
        this.selectedSubnet = selectedSubnet;
        this.vrfs = vrfs;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_VRFS"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_VRF"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableVRFs = new JList<>(vrfs.toArray(new LocalObjectLight[0]));
        lstAvailableVRFs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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
        
        pnlScrollMain.setViewportView(lstAvailableVRFs);
        add(lstAvailableVRFs, BorderLayout.CENTER);
        
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton btnRelate = new JButton("Create relationship");
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new VRFsFrame.BtnConnectActionListener());
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
            if (lstAvailableVRFs.getSelectedValue() == null)
                JOptionPane.showMessageDialog(null, "Select a VRF from the list");
            else {
                if (CommunicationsStub.getInstance().relateSubnetToVRF(
                        selectedSubnet.get(0).getOid(), selectedSubnet.get(0).getClassName(), lstAvailableVRFs.getSelectedValue().getOid())){
                    JOptionPane.showMessageDialog(null, String.format("The %s subnet was related to VRF %s", selectedSubnet.get(0).getName(), 
                            lstAvailableVRFs.getSelectedValue().getName()));
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
        for(LocalObjectLight vrf : vrfs){
            if(vrf.getClassName().toLowerCase().contains(text.toLowerCase()) 
                    || vrf.getName().toLowerCase().contains(text.toLowerCase()))
                filteredServices.add(vrf);
        }
        LocalObjectLight[] toArray = filteredServices.toArray(new LocalObjectLight[filteredServices.size()]);
        lstAvailableVRFs.setListData(toArray);
    }

}
