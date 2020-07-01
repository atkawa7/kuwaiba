/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.models.physicalconnections.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalBusinessRule;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;

/**
 * With this action is possible to configure what kind of links can be connected to certain types of ports (e.g. OpticalLinks should be connected only to OpticalPorts) and how to define connectors compatibility
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ActionID(id = "org.inventory.models.physicalconnections.actions.ConfigureConnectorMatchingAction", category = "Tools")
@ActionRegistration(displayName = "Configure Connection Rules")
@ActionReference(path = "Menu/Tools/Advanced", separatorBefore =  29, name = "org-inventory-models-physicalconnections-actions-ConfigureConnectorMatchingAction", position = 30)
public class ConfigureConnectorMatchingAction implements ActionListener {
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    @Override
    public void actionPerformed(ActionEvent e) {
        new MatchingRulesFrame().setVisible(true);
    }
    
    private class MatchingRulesFrame extends JFrame {
        private JList<LocalBusinessRule> lstRules;
        private JButton btnAddRule;
        private JButton btnDeleteRule;
        List<LocalBusinessRule> matchingRules;
        
        public MatchingRulesFrame() {
            setLayout(new BorderLayout());
            //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
            matchingRules = com.getBusinessRules(LocalBusinessRule.TYPE_RELATIONSHIP_BY_ATTRIBUTE_VALUE);
            if (matchingRules == null) {
                dispose();
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            } else {
                lstRules = new JList<>(matchingRules.toArray(new LocalBusinessRule[0]));
                lstRules.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                add(lstRules);
                btnAddRule = new JButton("Add Rule");
                btnAddRule.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        /**
                         * Link information
                         */
                        List<LocalClassMetadataLight> linkSubclasses = com.
                                getLightSubclasses(Constants.CLASS_GENERICPHYSICALLINK, false, false);
                        
                        if (linkSubclasses == null) {
                            JOptionPane.showMessageDialog(null, "Make sure you have upgraded your database before running this action:\n" + com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            return;
                        }
                        
                        JComboBox<LocalClassMetadataLight> cmbLinkSubclasses = new JComboBox<>(linkSubclasses.toArray(new LocalClassMetadataLight[0]));
                        cmbLinkSubclasses.setName("cmbLinkSubclasses");
                        List<LocalObjectListItem> linkConnectorTypes = com.getList("LinkConnectorType", true, false);
                        
                        if (linkConnectorTypes == null) {
                            JOptionPane.showMessageDialog(null, "Make sure you have upgraded your database before running this action:\n" + com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            return;
                        }
                        
                        JComboBox<LocalObjectListItem> cmbLinkConnectorTypes = new JComboBox<>(linkConnectorTypes.toArray(new LocalObjectListItem[0]));
                        cmbLinkConnectorTypes.setName("cmbLinkConnectorTypes");
                        /**
                         * Port information
                         */
                        List<LocalClassMetadataLight> portSubclasses = com.
                                getLightSubclasses(Constants.CLASS_GENERICPORT, false, false);
                        
                        if (portSubclasses == null) {
                            JOptionPane.showMessageDialog(null, "Make sure you have upgraded your database before running this action:\n" + com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            return;
                        }
                        
                        JComboBox<LocalClassMetadataLight> cmbPortSubclasses = new JComboBox<>(portSubclasses.toArray(new LocalClassMetadataLight[0]));
                        cmbPortSubclasses.setName("cmbPortSubclasses");
                        List<LocalObjectListItem> portConnectorTypes = com.getList("PortConnectorType", true, false);
                        
                        if (portConnectorTypes == null) {
                            JOptionPane.showMessageDialog(null, "Make sure you have upgraded your database before running this action:\n" + com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                            dispose();
                            return;
                        }
                        
                        JComboBox<LocalObjectListItem> cmbPortConnectorTypes = new JComboBox<>(portConnectorTypes.toArray(new LocalObjectListItem[0]));
                        cmbPortConnectorTypes.setName("cmbPortConnectorTypes");
                        
                        JComplexDialogPanel pnlRuleInformation = new JComplexDialogPanel(new String[] { "Link Type", "Link Connector Type", "Port Type", "Port Connector Type" }, 
                                new JComponent[] { cmbLinkSubclasses, cmbLinkConnectorTypes, cmbPortSubclasses, cmbPortConnectorTypes });
                        
                        if (JOptionPane.showConfirmDialog(null, pnlRuleInformation, "New Rule", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                            if (cmbPortSubclasses.getSelectedItem() == null || cmbLinkSubclasses.getSelectedItem() == null)
                                JOptionPane.showMessageDialog(null, "Invalid selection", "Error", JOptionPane.ERROR_MESSAGE);
                            else {
                                String linkConnector = cmbLinkConnectorTypes.getSelectedItem() == null || ((LocalObjectListItem)cmbLinkConnectorTypes.getSelectedItem()).getId().equals(LocalObjectListItem.NULL_ID) ? "" : ((LocalObjectListItem)cmbLinkConnectorTypes.getSelectedItem()).getName();
                                String portConnector = cmbPortConnectorTypes.getSelectedItem() == null || ((LocalObjectListItem)cmbPortConnectorTypes.getSelectedItem()).getId().equals(LocalObjectListItem.NULL_ID) ? "" : ((LocalObjectListItem)cmbPortConnectorTypes.getSelectedItem()).getName();
                                
                                LocalBusinessRule newBusinessRule = com.createBusinessRule(cmbLinkSubclasses.getSelectedItem() + ":" + linkConnector + " - " + cmbPortSubclasses.getSelectedItem() + ":" + portConnector, 
                                    String.format("%s and %s", cmbLinkSubclasses.getSelectedItem(), cmbPortSubclasses.getSelectedItem()),
                                    LocalBusinessRule.TYPE_RELATIONSHIP_BY_ATTRIBUTE_VALUE, LocalBusinessRule.SCOPE_GLOBAL, ((LocalClassMetadataLight)cmbLinkSubclasses.getSelectedItem()).getClassName(), 
                                    "0.1", Arrays.asList(((LocalClassMetadataLight)cmbPortSubclasses.getSelectedItem()).getClassName(), 
                                                          "connectorType", "connectorType", linkConnector, portConnector  )); //Here we tell the new rule the classes that can be connected and the connectors allowed
                                if (newBusinessRule == null)
                                    JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                                else {
                                    matchingRules.add(newBusinessRule);                                    
                                    lstRules.setListData(matchingRules.toArray(new LocalBusinessRule[0]));
                                    lstRules.setSelectedValue(newBusinessRule, true);
                                }
                            }
                        }
                    }
                });
                btnDeleteRule = new JButton("Delete Rule");
                
                btnDeleteRule.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LocalBusinessRule selectedRule = lstRules.getSelectedValue();
                        if (selectedRule == null)
                            JOptionPane.showMessageDialog(null, "You have to select at least one rule", "Error", JOptionPane.ERROR_MESSAGE);
                        else {
                            if (com.deleteBusinessRule(selectedRule.getId())) {
                                matchingRules.remove(selectedRule);
                                lstRules.setListData(matchingRules.toArray(new LocalBusinessRule[0]));
                            }
                            else
                                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                });
                
                JPanel pnlButtons = new JPanel();
                pnlButtons.add(btnAddRule);
                pnlButtons.add(btnDeleteRule);
                add(pnlButtons, BorderLayout.SOUTH);
                setSize(300, 500);
                setLocationRelativeTo(null);
            }
        }    
    }
}
