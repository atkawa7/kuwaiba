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

package org.inventory.views.graphical.dialogs;

import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.communications.core.views.LocalObjectViewLight;

/**
 * List of available topologies
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TopologyListPanel extends JPanel implements ListSelectionListener {

    private LocalObjectViewLight[] elements;

    /** Creates new form TopologyListPanel */
    public TopologyListPanel(LocalObjectViewLight[] views) {
        elements = views;
        initComponents();
        lstViewList.addListSelectionListener(this);
        lstViewList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //Public queries are painted green
        lstViewList.setCellRenderer(new ListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel item = new JLabel(((LocalObjectViewLight)value).getName());
                item.setBackground(SystemColor.controlHighlight);
                if (isSelected)
                    item.setOpaque(true);
                else
                    item.setOpaque(false);
                return item;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlDescription = new javax.swing.JPanel();
        txtDescription = new javax.swing.JTextArea();
        pnlScrollQueryList = new javax.swing.JScrollPane();
        lstViewList = lstViewList = new JList(elements);

        pnlDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(TopologyListPanel.class, "TopologyListPanel.pnlDescription.border.title"))); // NOI18N
        pnlDescription.setName(""); // NOI18N
        pnlDescription.setLayout(new java.awt.BorderLayout());

        txtDescription.setBackground(java.awt.SystemColor.window);
        txtDescription.setColumns(20);
        txtDescription.setEditable(false);
        txtDescription.setRows(5);
        txtDescription.setBorder(null);
        txtDescription.setOpaque(false);
        pnlDescription.add(txtDescription, java.awt.BorderLayout.CENTER);

        pnlScrollQueryList.setViewportView(lstViewList);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlScrollQueryList, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(pnlDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 170, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlScrollQueryList, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .addComponent(pnlDescription, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList lstViewList;
    private javax.swing.JPanel pnlDescription;
    private javax.swing.JScrollPane pnlScrollQueryList;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables

    @Override
    public void valueChanged(ListSelectionEvent e) {
        txtDescription.setText(((LocalObjectViewLight)lstViewList.getSelectedValue()).getDescription());
    }

    public LocalObjectViewLight getSelectedView(){
        return (LocalObjectViewLight)lstViewList.getSelectedValue();
    }

    public void releaseListeners() {
        lstViewList.removeListSelectionListener(this);
    }

}
