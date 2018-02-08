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
import org.inventory.core.services.i18n.I18N;

/**
 * Show the existing bridge domains interfaces that can be associated to 
 * services instances
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class BDIsInterfaceFrame extends JFrame{
    private JTextField txtField;
    private JScrollPane pnlScrollMain;
    private JList<LocalObjectLight> lstAvailableDevices;
    private List<LocalObjectLight> selectedObjects;
    private List<LocalObjectLight> devices;
    
    public BDIsInterfaceFrame(List<LocalObjectLight> selectedObjects, List<LocalObjectLight> devices) {
        this.selectedObjects = selectedObjects;
        this.devices = devices;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_TITLE_AVAILABLE_BDIS"));
        setSize(400, 650);
        setLocationRelativeTo(null);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("com/neotropic/inventory/modules/ipam/Bundle").getString("LBL_INSTRUCTIONS_SELECT_BDI"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
                
        JPanel pnlSearch = new JPanel();
        pnlSearch.setLayout(new GridLayout(1, 2));
        lstAvailableDevices = new JList<>(devices.toArray(new LocalObjectLight[0]));
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
        JButton btnRelate = new JButton(I18N.gm("create_relationship"));
        pnlButtons.add(btnRelate);
        btnRelate.addActionListener(new BDIsInterfaceFrame.BtnConnectActionListener());
        JButton btnClose = new JButton(I18N.gm("close"));
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
                JOptionPane.showMessageDialog(null, I18N.gm("select_a_bdi"));
            else {                
                if (CommunicationsStub.getInstance().relatePortToInterface(selectedObjects.get(0).getOid(), 
                        selectedObjects.get(0).getClassName(), 
                        lstAvailableDevices.getSelectedValue().getClassName(),
                        lstAvailableDevices.getSelectedValue().getOid())){
                    JOptionPane.showMessageDialog(null, String.format(I18N.gm("was_S_releated_to_S"), 
                            selectedObjects.get(0).toString(), lstAvailableDevices.getSelectedValue().getName()));
                        dispose();
                }else 
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
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
