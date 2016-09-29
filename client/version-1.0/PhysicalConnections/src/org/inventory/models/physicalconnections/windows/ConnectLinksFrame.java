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
package org.inventory.models.physicalconnections.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConnectLinksFrame extends JFrame {
    JLabel lblResults;
    private JScrollPane pnlScrollLeft;
    private JScrollPane pnlScrollRight;
    private JScrollPane pnlScrollCenter;
    private JList lstAvailableConnections;
    private BeanTreeView leftTree;
    private BeanTreeView rightTree;
    private LocalObjectLight aSelectedObject;
    private LocalObjectLight bSelectedObject;
    private ExplorablePanel pnlLeft;
    private ExplorablePanel pnlRight;
    private LocalObjectLight aSideRoot;
    private LocalObjectLight bSideRoot;

    public ConnectLinksFrame(LocalObjectLight aSideRoot, LocalObjectLight bSideRoot, List<LocalObjectLight> connections) {
        this.aSideRoot = aSideRoot;
        this.bSideRoot = bSideRoot;
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_TITLE_CONNECT_LINKS"));
        setSize(1000, 700);
        
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_INSTRUCTIONS_CONNECT_LINKS"));
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(lblInstructions, BorderLayout.NORTH);
        
        lblResults = new JLabel();
        lblResults.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        lblResults.setForeground(Color.RED);
        add(lblResults, BorderLayout.SOUTH);
        
        pnlLeft = new ExplorablePanel();
        pnlRight = new ExplorablePanel();
        
        rightTree = new BeanTreeView();
        rightTree.setSize(400, 0);
        pnlRight.add(rightTree);
        pnlScrollRight = new JScrollPane();
        pnlScrollRight.setViewportView(pnlRight);
        add(pnlScrollRight, BorderLayout.EAST);
        
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BorderLayout());
        lstAvailableConnections = new JList(connections.toArray(new LocalObjectLight[0]));
        lstAvailableConnections.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateConnectionDetails();
            }
        });
        centralPanel.add(lstAvailableConnections,BorderLayout.CENTER);
        JButton btnConnect = new JButton("Connect");
        btnConnect.addActionListener(new BtnConnectActionListener());
        centralPanel.add(btnConnect, BorderLayout.SOUTH);
        
        pnlScrollCenter = new JScrollPane();
        pnlScrollCenter.setViewportView(centralPanel);
        pnlScrollCenter.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(pnlScrollCenter, BorderLayout.CENTER);
        
        leftTree = new BeanTreeView();
        leftTree.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        leftTree.setSize(400, 0);
        pnlLeft.add(leftTree);
        pnlScrollLeft = new JScrollPane();
        pnlScrollLeft.setViewportView(pnlLeft);
        add(pnlScrollLeft, BorderLayout.WEST);
        init();
    }
    
    private class BtnConnectActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (lstAvailableConnections.getSelectedValue() == null){
                lblResults.setForeground(Color.RED);
                lblResults.setText("Select a link from the list");
            }
            else{
                if (CommunicationsStub.getInstance().connectPhysicalLinks(new String[]{aSelectedObject == null ? null :aSelectedObject.getClassName()}, 
                        new Long[]{aSelectedObject == null ? null : aSelectedObject.getOid()}, 
                        new String[]{((LocalObjectLight)lstAvailableConnections.getSelectedValue()).getClassName()}, 
                        new Long[]{((LocalObjectLight)lstAvailableConnections.getSelectedValue()).getOid()}, 
                        new String[]{bSelectedObject == null ? null :bSelectedObject.getClassName()}, 
                        new Long[]{bSelectedObject == null ? null : bSelectedObject.getOid()})){
                    lblResults.setForeground(Color.MAGENTA);
                    lblResults.setText("Connection was made successfully");
                } else{
                    lblResults.setForeground(Color.RED);
                    lblResults.setText(CommunicationsStub.getInstance().getError());
                }
            }
        }
    }

    private void init() {
        pnlLeft.getExplorerManager().setRootContext(new ObjectNode(aSideRoot));
        pnlRight.getExplorerManager().setRootContext(new ObjectNode(bSideRoot));
        
        Result<LocalObjectLight> aResult = pnlLeft.getLookup().lookupResult(LocalObjectLight.class);
        aResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext())
                    aSelectedObject = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                else
                    aSelectedObject = null;
                
                updateConnectionDetails();
            }
        });
        
        Result<LocalObjectLight> bResult = pnlRight.getLookup().lookupResult(LocalObjectLight.class);
        bResult.addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext())
                   bSelectedObject = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                else
                    bSelectedObject = null;
                
                updateConnectionDetails();
            }
        });
    }
    
    private void updateConnectionDetails(){
        lblResults.setForeground(Color.BLUE);
        lblResults.setText((aSelectedObject == null ? "Free" : aSelectedObject) + " <-> " +
                (lstAvailableConnections.getSelectedValue() ==  null ? "No connection" : lstAvailableConnections.getSelectedValue()) + " <-> " +
                (bSelectedObject == null ? "Free" : bSelectedObject));
    }
}