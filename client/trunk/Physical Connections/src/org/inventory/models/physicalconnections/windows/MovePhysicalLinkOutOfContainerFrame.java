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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.special.children.nodes.ActionlessSpecialOnlyContainersRootNode;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows an editor to selected physical link(s) and move them out of a wire container
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MovePhysicalLinkOutOfContainerFrame extends JFrame{

    private ExplorablePanel pnlexistingWireContainers;
    private BeanTreeView treeWireContainers;
    private JButton btnMoveLinks;

    private List<LocalObjectLight> selectedLinks;
    private LocalObjectLight containerParent;
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    public MovePhysicalLinkOutOfContainerFrame(List<LocalObjectLight> existingPhysicalLinks, LocalObjectLight containerParent) {
        this.containerParent = containerParent;
        
        setLayout(new BorderLayout());
        setTitle(I18N.gm("move_links_into_container"));
        setBounds(80, 80, 450, 550);
        
        JLabel lblInstructions = new JLabel(I18N.gm("instructions_to_move_links_out_of_container"));
        btnMoveLinks = new JButton(I18N.gm("move_links_out_of_container"));
        btnMoveLinks.setEnabled(false);
        btnMoveLinks.addActionListener(new MovePhysicalLinkOutOfContainerFrame.BtnMoveLinksActionListener());
   
        treeWireContainers = new BeanTreeView();
        pnlexistingWireContainers = new ExplorablePanel();
        pnlexistingWireContainers.getExplorerManager().setRootContext(new ActionlessSpecialOnlyContainersRootNode(
                new ActionlessSpecialOnlyContainersRootNode.ActionlessSpecialOnlyContainersRootChildren(existingPhysicalLinks)));
                
        treeWireContainers.setRootVisible(false);
        
        pnlexistingWireContainers.setViewportView(treeWireContainers);
        add(lblInstructions, BorderLayout.NORTH);
        add(pnlexistingWireContainers, BorderLayout.CENTER);
        
        add(btnMoveLinks, BorderLayout.SOUTH);
        init();
    }    
    
    private void init() {
        pnlexistingWireContainers.getLookup().lookupResult(LocalObjectLight.class).addLookupListener(new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                selectedLinks = new ArrayList<>();
                if (((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator().hasNext()){
                   Iterator<? extends LocalObjectLight> iterator = ((Lookup.Result<LocalObjectLight>)ev.getSource()).allInstances().iterator();
                    while (iterator.hasNext()) {
                        selectedLinks.add(iterator.next());
                    }
                   btnMoveLinks.setEnabled(true);
                }
                else
                   selectedLinks = null;
            }
        });
    }
    
    private class BtnMoveLinksActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            List<Refreshable> topComponents = new ArrayList<>();
            
            TopComponent topComponent = WindowManager.getDefault().findTopComponent("ObjectViewTopComponent_" + containerParent.getOid());
            if (topComponent instanceof Refreshable)
                topComponents.add((Refreshable) topComponent);    
            
            if(com.moveSpecialObjects(containerParent.getClassName(),
                    containerParent.getOid(), 
                    selectedLinks)) {
                
                for (Refreshable tc : topComponents)
                    tc.refresh();
                
                JOptionPane.showMessageDialog(null, "The links were moved sucessfully", I18N.gm("success"), JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else
                JOptionPane.showMessageDialog(null, com.getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        }
    
    }
}
