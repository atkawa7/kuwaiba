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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.special.children.nodes.ActionlessSpecialOnlyContainersRootNode;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * Shows an editor to move a selected physical link into an existing wire container
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MovePhysicalLinkToContainerFrame  extends JFrame {

    private ExplorablePanel pnlexistingWireContainers;
    private BeanTreeView treeWireContainers;
    private JButton btnMoveLinks;

    private LocalObjectLight selectedContainer;
    private List<LocalObjectLight> linksToMove;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public MovePhysicalLinkToContainerFrame(List<LocalObjectLight> linksToMove, List<LocalObjectLight> existintWireContainersList) {
        this.linksToMove = linksToMove;
        
        setLayout(new BorderLayout());
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_MOVE_LINKS"));
        setSize(450, 550);
        
        JLabel lblInstructions = new JLabel(java.util.ResourceBundle.getBundle("org/inventory/models/physicalconnections/Bundle").getString("LBL_INSTRUCTIONS_MOVE_LINKS_INTO_CONTAINER"));
        btnMoveLinks = new JButton("Move Links");
        btnMoveLinks.setEnabled(false);
        btnMoveLinks.addActionListener(new MovePhysicalLinkToContainerFrame.BtnMoveLinksActionListener());
   
        treeWireContainers = new BeanTreeView();
        pnlexistingWireContainers = new ExplorablePanel();
        pnlexistingWireContainers.getExplorerManager().setRootContext(new ActionlessSpecialOnlyContainersRootNode(
                new ActionlessSpecialOnlyContainersRootNode.ActionlessSpecialOnlyContainersRootChildren(existintWireContainersList)));
                
        treeWireContainers.setRootVisible(false);
        
        pnlexistingWireContainers.setViewportView(treeWireContainers);
        add(lblInstructions, BorderLayout.NORTH);
        add(pnlexistingWireContainers, BorderLayout.CENTER);
        
        add(btnMoveLinks, BorderLayout.SOUTH);
        init();
    }
    
    private void init(){
        pnlexistingWireContainers.getLookup().lookupResult(LocalObjectLight.class).addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext()){
                   selectedContainer = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().next();
                   btnMoveLinks.setEnabled(true);
                }
                else
                   selectedContainer = null;
            }
        });
    }
    
    private class BtnMoveLinksActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if(com.moveSpecialObjects(selectedContainer.getClassName(),
                    selectedContainer.getOid(), 
                    linksToMove.toArray(new LocalObjectLight[linksToMove.size()])))
                JOptionPane.showMessageDialog(null, "The link(s) was moved sucessfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(null, com.getError(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    
    }
}
