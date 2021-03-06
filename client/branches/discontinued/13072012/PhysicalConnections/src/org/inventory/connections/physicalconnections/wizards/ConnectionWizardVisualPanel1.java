/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.connections.physicalconnections.wizards;

import javax.swing.JPanel;
import org.inventory.connections.physicalconnections.wizards.custompanels.ExplorablePanel;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.explorer.view.BeanTreeView;

public final class ConnectionWizardVisualPanel1 extends JPanel{

    private BeanTreeView treeLeft;
    private BeanTreeView treeRight;
    private LocalObjectLight aSide;
    private LocalObjectLight bSide;
    private ExplorablePanel pnlLeft;
    private ExplorablePanel pnlRight;

    ConnectionWizardVisualPanel1(LocalObjectLight aSide, LocalObjectLight bSide) {
        this.aSide = aSide;
        this.bSide = bSide;
        initComponents();
        initCustomComponents();
    }

    public void initCustomComponents(){
        treeLeft = new BeanTreeView();
        treeRight = new BeanTreeView();

        pnlLeft = new ExplorablePanel();
        pnlRight = new ExplorablePanel();
                  
        pnlLeft.getExplorerManager().setRootContext(new ObjectNode(aSide));
        pnlRight.getExplorerManager().setRootContext(new ObjectNode(bSide));
        pnlLeft.add(treeLeft);
        pnlRight.add(treeRight);
        pnlScrollLeft.setViewportView(pnlLeft);
        pnlScrollRight.setViewportView(pnlRight);
    }

    @Override
    public String getName() {
        return "Choose endpoints";
    }

    public ExplorablePanel getPnlLeft() {
        return pnlLeft;
    }

    public ExplorablePanel getPnlRight() {
        return pnlRight;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblInstructions = new javax.swing.JLabel();
        pnlScrollRight = new javax.swing.JScrollPane();
        pnlScrollLeft = new javax.swing.JScrollPane();

        lblInstructions.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lblInstructions, org.openide.util.NbBundle.getMessage(ConnectionWizardVisualPanel1.class, "ConnectionWizardVisualPanel1.lblInstructions.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblInstructions)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlScrollLeft, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(pnlScrollRight, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblInstructions)
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(pnlScrollRight)
                    .addComponent(pnlScrollLeft, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE))
                .addContainerGap(33, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel lblInstructions;
    private javax.swing.JScrollPane pnlScrollLeft;
    private javax.swing.JScrollPane pnlScrollRight;
    // End of variables declaration//GEN-END:variables
}
