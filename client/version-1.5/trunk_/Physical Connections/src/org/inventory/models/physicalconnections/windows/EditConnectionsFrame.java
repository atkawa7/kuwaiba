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
package org.inventory.models.physicalconnections.windows;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.navigationtree.nodes.ActionlessObjectNode;
import org.inventory.navigation.special.children.nodes.ActionlessSpecialObjectNode;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EditConnectionsFrame extends JFrame {
    private JLabel lblASide;
    private JLabel lblBSide;
    private JButton btnASideDisconnect;
    private JButton btnBSideDisconnect;
    private BeanTreeView leftTree;
    private BeanTreeView centerTree;
    private BeanTreeView rightTree;
    private LocalObjectLight aSelectedObject;
    private LocalObjectLight centerSelectedObject;
    private LocalObjectLight bSelectedObject;
    private ExplorablePanel pnlLeft;
    private ExplorablePanel pnlCenter;
    private ExplorablePanel pnlRight;
    private LocalObjectLight aSideRoot;
    private LocalObjectLight centerRoot;
    private LocalObjectLight bSideRoot;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public EditConnectionsFrame(LocalObjectLight parentContainer, LocalObjectLight aSideRoot, LocalObjectLight bSideRoot) {
        this.aSideRoot = aSideRoot;
        this.centerRoot = parentContainer;
        this.bSideRoot = bSideRoot;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_TITLE_CONNECT_LINKS"));
        setSize(1200, 700);
        
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_INSTRUCTIONS_CONNECT_LINKS"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(lblInstructions, BorderLayout.NORTH);

        JPanel pnlConnectionConfiguration = new JPanel(new GridBagLayout());
        GridBagConstraints pnlConnectionConfigurationGridConstraints = new GridBagConstraints();
        
        lblASide = new JLabel();
        btnASideDisconnect = new JButton("Disconnect A Side");
        btnASideDisconnect.setEnabled(false);
        btnASideDisconnect.addActionListener(new BtnDisconnectASide());
        pnlConnectionConfigurationGridConstraints.gridx = 0;
        pnlConnectionConfigurationGridConstraints.gridy = 0;
        pnlConnectionConfigurationGridConstraints.gridwidth = 3;
        pnlConnectionConfigurationGridConstraints.weightx = 0.75;
        pnlConnectionConfigurationGridConstraints.fill = GridBagConstraints.BOTH;
        pnlConnectionConfigurationGridConstraints.anchor = GridBagConstraints.LINE_START;
        pnlConnectionConfiguration.add(lblASide, pnlConnectionConfigurationGridConstraints);
        pnlConnectionConfigurationGridConstraints.gridx = 3;
        pnlConnectionConfigurationGridConstraints.gridy = 0;
        pnlConnectionConfigurationGridConstraints.gridwidth = 1;
        pnlConnectionConfigurationGridConstraints.weightx = 0.25;
        pnlConnectionConfigurationGridConstraints.fill = GridBagConstraints.NONE;
        pnlConnectionConfigurationGridConstraints.anchor = GridBagConstraints.LINE_END;
        pnlConnectionConfiguration.add(btnASideDisconnect, pnlConnectionConfigurationGridConstraints);
        
        lblBSide = new JLabel();
        btnBSideDisconnect = new JButton("Disconnect B Side");
        btnBSideDisconnect.setEnabled(false);
        btnBSideDisconnect.addActionListener(new BtnDisconnectBSide());
        pnlConnectionConfigurationGridConstraints.gridx = 0;
        pnlConnectionConfigurationGridConstraints.gridy = 1;
        pnlConnectionConfigurationGridConstraints.gridwidth = 3;
        pnlConnectionConfigurationGridConstraints.fill = GridBagConstraints.BOTH;
        pnlConnectionConfigurationGridConstraints.anchor = GridBagConstraints.LINE_START;
        pnlConnectionConfigurationGridConstraints.weightx = 0.75;
        pnlConnectionConfiguration.add(lblBSide, pnlConnectionConfigurationGridConstraints);
        pnlConnectionConfigurationGridConstraints.gridx = 3;
        pnlConnectionConfigurationGridConstraints.gridy = 1;
        pnlConnectionConfigurationGridConstraints.gridwidth = 1;
        pnlConnectionConfigurationGridConstraints.fill = GridBagConstraints.NONE;
        pnlConnectionConfigurationGridConstraints.anchor = GridBagConstraints.LINE_END;
        pnlConnectionConfigurationGridConstraints.weightx = 0.25;
        pnlConnectionConfiguration.add(btnBSideDisconnect, pnlConnectionConfigurationGridConstraints);
        pnlConnectionConfiguration.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(pnlConnectionConfiguration, BorderLayout.SOUTH);
        
        pnlLeft = new ExplorablePanel();
        pnlCenter = new ExplorablePanel();
        pnlRight = new ExplorablePanel();
        
        rightTree = new BeanTreeView();
        pnlRight.setViewportView(rightTree);
        add(pnlRight, BorderLayout.EAST);
        
        JPanel pnlParentCentral = new JPanel();
        pnlParentCentral.setLayout(new BorderLayout());
        centerTree = new BeanTreeView();
        pnlCenter.setViewportView(centerTree);
        pnlParentCentral.add(pnlCenter);
        JPanel pnlConnectDisconnectButtons = new JPanel(new GridLayout(2, 1));
        JButton btnConnect = new JButton("Connect Selected Endpoints");
        btnConnect.addActionListener(new BtnConnectActionListener());
        JButton btnDisconnect = new JButton("Disconnect Both Sides");
        btnDisconnect.addActionListener(new BtnDisconnectActionListener());
        pnlConnectDisconnectButtons.add(btnConnect);
        pnlConnectDisconnectButtons.add(btnDisconnect);
        pnlParentCentral.add(pnlConnectDisconnectButtons, BorderLayout.SOUTH);
        add(pnlParentCentral);
        
        leftTree = new BeanTreeView();
        pnlLeft.setViewportView(leftTree);
        add(pnlLeft, BorderLayout.WEST);
        init();
    }
    
    private void init() {
        pnlLeft.getExplorerManager().setRootContext(new ActionlessObjectNode(aSideRoot));
        pnlCenter.getExplorerManager().setRootContext(new ActionlessSpecialObjectNode(centerRoot));
        pnlRight.getExplorerManager().setRootContext(new ActionlessObjectNode(bSideRoot));
        
        pnlLeft.getLookup().lookupResult(LocalObjectLight.class).addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext())
                    aSelectedObject = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                else
                    aSelectedObject = null;
            }
        });
        
        pnlCenter.getLookup().lookupResult(LocalObjectLight.class).addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext())
                    centerSelectedObject = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                else
                    centerSelectedObject = null;
                
                updateConnectionDetails();
            }
        });
        
        pnlRight.getLookup().lookupResult(LocalObjectLight.class).addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext())
                   bSelectedObject = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                else
                    bSelectedObject = null;
                
            }
        });
    }
    
    private void updateConnectionDetails() {
        
        if (centerSelectedObject == null) {
            lblASide.setText(""); //NOI18N
            lblBSide.setText(""); //NOI18N
            lblASide.setToolTipText(""); //NOI18N
            lblBSide.setToolTipText(""); //NOI18N
            return;
        }
        
        LocalObjectLight[] connectionEndpoints = com.getConnectionEndpoints(centerSelectedObject.getClassName(), centerSelectedObject.getOid());
        if (connectionEndpoints == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            if (connectionEndpoints[0] == null) { //A side is disconnected
                lblASide.setText("<html><b>Disconnected</b></html>");
                btnASideDisconnect.setEnabled(false);
            } else {
                List<LocalObjectLight> aSideParents = com.getParents(connectionEndpoints[0].getClassName(), connectionEndpoints[0].getOid());
                if (aSideParents == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    lblASide.setText("<html><b>" + connectionEndpoints[0] + "</b> / " +  Utils.formatObjectList(aSideParents, false, 4) + "</html>");
                    lblASide.setToolTipText(lblASide.getText());
                    btnASideDisconnect.setEnabled(true);
                }
            }
            if (connectionEndpoints[1] == null) { //B side is disconnected
                lblBSide.setText("<html><b>Disconnected</b></html>");
                btnBSideDisconnect.setEnabled(false);
            } else {
                List<LocalObjectLight> bSideParents = com.getParents(connectionEndpoints[1].getClassName(), connectionEndpoints[1].getOid());
                if (bSideParents == null)
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    lblBSide.setText("<html><b>" + connectionEndpoints[1] + "</b> / " +  Utils.formatObjectList(bSideParents, false, 4) + "</html>");
                    lblBSide.setToolTipText(lblBSide.getText());
                    btnBSideDisconnect.setEnabled(true);
                }
            }
        }
    }
    
    private class BtnConnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Node[] selectedConnections = pnlCenter.getExplorerManager().getSelectedNodes();
            
            if (selectedConnections.length != 1)
                JOptionPane.showMessageDialog(null, "Choose a single connection (link or container) from the central panel", "Error", JOptionPane.ERROR_MESSAGE);
            else {
                if (com.isSubclassOf(centerSelectedObject.getClassName(), "GenericPhysicalLink")) {
                    if (com.connectPhysicalLinks(Arrays.asList(aSelectedObject == null ? null :aSelectedObject.getClassName()),
                                Arrays.asList(aSelectedObject == null ? null : aSelectedObject.getOid()),
                                Arrays.asList(centerSelectedObject.getClassName()),
                                Arrays.asList(centerSelectedObject.getOid()),
                                Arrays.asList(bSelectedObject == null ? null :bSelectedObject.getClassName()), 
                                Arrays.asList(bSelectedObject == null ? null : bSelectedObject.getOid()))) {
                        JOptionPane.showMessageDialog(null, aSelectedObject != null && bSelectedObject != null ? 
                                "Endpoints connected sucessfully" : "Endpoint connected sucessfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                        updateConnectionDetails();
                    } else
                        JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    if (com.isSubclassOf(centerSelectedObject.getClassName(), "GenericPhysicalContainer")) {
                        if (com.connectPhysicalContainers(Arrays.asList(aSelectedObject == null ? null : aSelectedObject.getClassName()),
                                    Arrays.asList(aSelectedObject == null ? null : aSelectedObject.getOid()),
                                    Arrays.asList(centerSelectedObject.getClassName()),
                                    Arrays.asList(centerSelectedObject.getOid()),
                                    Arrays.asList(bSelectedObject == null ? null :bSelectedObject.getClassName()), 
                                    Arrays.asList(bSelectedObject == null ? null : bSelectedObject.getOid()))) {
                            JOptionPane.showMessageDialog(null, aSelectedObject != null && bSelectedObject != null ? 
                                "Endpoints connected sucessfully" : "Endpoint connected sucessfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                            updateConnectionDetails();
                        } else
                            JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
                    } else
                        JOptionPane.showMessageDialog(null, "The object selected in the central panel is not a link or a container", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private class BtnDisconnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Node[] selectedConnections = pnlCenter.getExplorerManager().getSelectedNodes();
            
            if (selectedConnections.length != 1)
                JOptionPane.showMessageDialog(null, "Choose a single connection (link or container) from the central panel", "Error", JOptionPane.ERROR_MESSAGE);
            else {
                if (com.disconnectPhysicalConnection(centerSelectedObject.getClassName(), centerSelectedObject.getOid(), 3)) {
                    JOptionPane.showMessageDialog(null, "Endpoint disconnected successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                    updateConnectionDetails();
                }
                else
                    JOptionPane.showMessageDialog(null, "The object selected in the central panel is not a link or a container", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class BtnDisconnectASide implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(com.disconnectPhysicalConnection(centerSelectedObject.getClassName(), centerSelectedObject.getOid(), 1)) { // "1" means release only aSide
                JOptionPane.showMessageDialog(null, "Endpoint released sucessfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateConnectionDetails();
            } else
                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class BtnDisconnectBSide implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(com.disconnectPhysicalConnection(centerSelectedObject.getClassName(), centerSelectedObject.getOid(), 2)) { // "1" means release only aSide
                JOptionPane.showMessageDialog(null, "Endpoint released sucessfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                updateConnectionDetails();
            } else
                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}