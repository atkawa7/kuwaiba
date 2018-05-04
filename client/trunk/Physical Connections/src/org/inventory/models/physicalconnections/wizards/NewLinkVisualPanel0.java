/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.models.physicalconnections.wizards;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.utils.ExplorablePanel;
import org.inventory.navigation.special.children.nodes.ActionlessSpecialOnlyContainersRootNode;
import org.openide.explorer.view.BeanTreeView;

/**
 * GUI components of the pre-step of the New Link wizard (show the existing 
 * containers between the two nodes that you are trying to connect)
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public final class NewLinkVisualPanel0 extends JPanel {
    
    private ExplorablePanel pnlexistingWireContainers;
    private final JCheckBox chkNoContainer;
    /**
     * Creates new form NewLinkVisualPanel0
     * @param existintWireContainersList a list with the existing containers 
     * between the two nodes you are trying to connect with a link.
     */
    public NewLinkVisualPanel0(List<LocalObjectLight> existintWireContainersList) {
        initComponents();
        final BeanTreeView treeWireContainers = new BeanTreeView();
        chkNoContainer = new JCheckBox("Do not use any container");
        JLabel lblInstructions = new JLabel("Would you want to create your link inside an existing wire container.");
        
        setLayout(new BorderLayout());
        
        pnlexistingWireContainers = new ExplorablePanel();
        pnlexistingWireContainers.getExplorerManager().setRootContext(new ActionlessSpecialOnlyContainersRootNode(
                new ActionlessSpecialOnlyContainersRootNode.ActionlessSpecialOnlyContainersRootChildren(existintWireContainersList)));
                
        treeWireContainers.setRootVisible(false);
        
        pnlexistingWireContainers.setViewportView(treeWireContainers);
        
        add(lblInstructions, BorderLayout.NORTH);
        add(pnlexistingWireContainers, BorderLayout.CENTER);
        add(chkNoContainer, BorderLayout.SOUTH);
        
        chkNoContainer.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                treeWireContainers.setEnabled(!chkNoContainer.isSelected());
            }
        });
    }
        
    public boolean noContainer(){
        return chkNoContainer.isSelected();
    }
    
    public LocalObjectLight getSelectedContainer() {
        return pnlexistingWireContainers.getLookup().lookup(LocalObjectLight.class);
    }
        
    @Override
    public String getName() {
        return "Existing wire containers";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 485, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 276, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
