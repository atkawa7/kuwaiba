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

package org.inventory.queries.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.SystemColor;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.communications.core.queries.LocalQueryLight;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class QueryListPanel extends javax.swing.JPanel implements ListSelectionListener{

    private LocalQueryLight[] elements;

    /** Creates new form QueryListPanel */
    public QueryListPanel(LocalQueryLight[] queries) {
        elements = queries;
        initComponents();
        lstQueryList.addListSelectionListener(this);
        lstQueryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //Public queries are painted green
        lstQueryList.setCellRenderer(new ListCellRenderer() {

            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel item = new JLabel(((LocalQueryLight)value).getName());
                item.setBackground(SystemColor.controlHighlight);
                if (((LocalQueryLight)value).isPublic()){
                    item.setForeground(Color.blue);
                }

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
        lstQueryList = lstQueryList = new JList(elements);

        pnlDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(QueryListPanel.class, "QueryListPanel.pnlDescription.border.title"))); // NOI18N
        pnlDescription.setName(""); // NOI18N
        pnlDescription.setLayout(new java.awt.BorderLayout());

        txtDescription.setBackground(java.awt.SystemColor.window);
        txtDescription.setColumns(20);
        txtDescription.setEditable(false);
        txtDescription.setRows(5);
        txtDescription.setBorder(null);
        txtDescription.setOpaque(false);
        pnlDescription.add(txtDescription, java.awt.BorderLayout.CENTER);

        pnlScrollQueryList.setViewportView(lstQueryList);

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
    private javax.swing.JList lstQueryList;
    private javax.swing.JPanel pnlDescription;
    private javax.swing.JScrollPane pnlScrollQueryList;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables

    public void valueChanged(ListSelectionEvent e) {
        txtDescription.setText(((LocalQueryLight)lstQueryList.getSelectedValue()).getDescription());
    }

    public LocalQueryLight getSelectedQuery(){
        return (LocalQueryLight)lstQueryList.getSelectedValue();
    }

    public void releaseListeners() {
        lstQueryList.removeListSelectionListener(this);
    }

}
