/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.explorer.view.BeanTreeView;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConnectLinksFrame extends JFrame{
    private JButton btnConnect;
    private JButton btnClose;
    
    private JScrollPane pnlScrollLeft;
    private JScrollPane pnlScrollRight;
    private JScrollPane pnlScrollCenter;
    private JList lstAvailableConnections;
    private BeanTreeView leftTree;
    private BeanTreeView rightTree;

    public ConnectLinksFrame(LocalObjectLight aSideRoot, LocalObjectLight bSideRoot, LocalObjectLight[] connections) {
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_TITLE_CONNECT_LINKS"));
        setSize(1000, 700);
        
        JLabel instructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_INSTRUCTIONS_CONNECT_LINKS"));
        instructions.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        add(instructions, BorderLayout.NORTH);
        
        ExplorablePanel pnlLeft = new ExplorablePanel();
        ExplorablePanel pnlRight = new ExplorablePanel();
                  
        pnlLeft.getExplorerManager().setRootContext(new ObjectNode(aSideRoot));
        pnlRight.getExplorerManager().setRootContext(new ObjectNode(bSideRoot));
        
        rightTree = new BeanTreeView();
        rightTree.setSize(400, 0);
        pnlRight.add(rightTree);
        pnlScrollRight = new JScrollPane();
        pnlScrollRight.setSize(400, 700);
        pnlScrollRight.setViewportView(pnlRight);
        add(pnlScrollRight, BorderLayout.EAST);
        
        JPanel centralPanel = new JPanel();
        centralPanel.setLayout(new BorderLayout());
        lstAvailableConnections = new JList(connections);
        centralPanel.add(lstAvailableConnections,BorderLayout.CENTER);
        centralPanel.add(new JButton("Connect"), BorderLayout.SOUTH);
        pnlScrollCenter = new JScrollPane();
        pnlScrollCenter.setViewportView(centralPanel);
        add(pnlScrollCenter, BorderLayout.CENTER);
        
        leftTree = new BeanTreeView();
        leftTree.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        leftTree.setSize(400, 0);
        pnlLeft.add(leftTree);
        pnlScrollLeft = new JScrollPane();
        pnlScrollLeft.setViewportView(pnlLeft);
        add(pnlScrollLeft, BorderLayout.WEST);
    }
}