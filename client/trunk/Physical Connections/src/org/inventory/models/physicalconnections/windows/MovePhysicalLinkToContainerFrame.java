/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 * Shows an editor to move a selected physical link into an existing wire container
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MovePhysicalLinkToContainerFrame  extends JFrame {

    private ExplorablePanel pnlexistingWireContainers;
    private BeanTreeView treeWireContainers;
    private JButton btnMoveLinks;
    private static List<LocalObjectLight> existintWireContainersList;

    private  LocalObjectLight selectedContainer;
    private static List<LocalObjectLight> linksToMove;
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    private static MovePhysicalLinkToContainerFrame instance = null;
    
    public static MovePhysicalLinkToContainerFrame getInstance(List<LocalObjectLight> linksToMove, List<LocalObjectLight> existintWireContainersList) {
      if(instance == null) {
         instance = new MovePhysicalLinkToContainerFrame(linksToMove, existintWireContainersList);
      }
      else{
          MovePhysicalLinkToContainerFrame.linksToMove = linksToMove;
          MovePhysicalLinkToContainerFrame.existintWireContainersList = existintWireContainersList;
      }
      return instance;
   }
    
    private MovePhysicalLinkToContainerFrame(List<LocalObjectLight> linksToMove, List<LocalObjectLight> existintWireContainersList) {
        MovePhysicalLinkToContainerFrame.linksToMove = linksToMove;
        MovePhysicalLinkToContainerFrame.existintWireContainersList = existintWireContainersList;
        
        init();
    }
    
    private void init() {
        setLayout(new BorderLayout());
        setTitle(I18N.gm("move_links_into_container"));
        setBounds(80, 80, 450, 550);
        
        JLabel lblInstructions = new JLabel(I18N.gm("instructions_to_move_links_into_container"));
        btnMoveLinks = new JButton(I18N.gm("move_links_into_container"));
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
            List<Refreshable> topComponents = new ArrayList<>();
            
            LocalObjectLight parent = com.getParent(linksToMove.get(0).getClassName(), linksToMove.get(0).getId());
            TopComponent topComponent = WindowManager.getDefault().findTopComponent("ObjectViewTopComponent_" + parent.getId().split("-")[0]);

            if (topComponent instanceof Refreshable)
                topComponents.add((Refreshable) topComponent);
            
            if(com.moveSpecialObjects(selectedContainer.getClassName(),
                    selectedContainer.getId(), 
                    linksToMove)) {
                
                JOptionPane.showMessageDialog(null, I18N.gm("links_moved_successfully"), I18N.gm("success"), JOptionPane.INFORMATION_MESSAGE);
                dispose();
                
                for (Refreshable tc : topComponents)
                    tc.refresh();
            } else
                JOptionPane.showMessageDialog(null, com.getError(), I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
        }
    
    }
}
