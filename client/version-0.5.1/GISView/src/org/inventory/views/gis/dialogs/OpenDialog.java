/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * OpenDialog.java
 *
 * Created on Oct 10, 2012, 2:50:52 PM
 */

package org.inventory.views.gis.dialogs;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.inventory.core.services.api.visual.LocalObjectViewLight;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OpenDialog extends javax.swing.JPanel {

    /** Creates new form OpenDialog */
    public OpenDialog(LocalObjectViewLight[] views) {
        initComponents();
        lstLeft.setListData(views);
        lstLeft.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (((LocalObjectViewLight)lstLeft.getSelectedValue()).getDescription() != null)
                    txtDescription.setText(((LocalObjectViewLight)lstLeft.getSelectedValue()).getDescription());
            }
        });
    }

    public LocalObjectViewLight getSelectedObject() {
        return (LocalObjectViewLight)lstLeft.getSelectedValue();
    }



    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSplit = new javax.swing.JSplitPane();
        pnlScrollLeft = new javax.swing.JScrollPane();
        lstLeft = new javax.swing.JList();
        pnlRight = new javax.swing.JPanel();
        txtDescription = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

        pnlSplit.setDividerLocation(150);

        pnlScrollLeft.setViewportView(lstLeft);

        pnlSplit.setLeftComponent(pnlScrollLeft);

        pnlRight.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(OpenDialog.class, "OpenDialog.pnlRight.border.title"))); // NOI18N
        pnlRight.setLayout(new java.awt.BorderLayout());

        txtDescription.setBackground(javax.swing.UIManager.getDefaults().getColor("Panel.background"));
        txtDescription.setColumns(20);
        txtDescription.setLineWrap(true);
        txtDescription.setRows(5);
        txtDescription.setText(org.openide.util.NbBundle.getMessage(OpenDialog.class, "OpenDialog.txtDescription.text")); // NOI18N
        txtDescription.setBorder(null);
        txtDescription.setDisabledTextColor(new java.awt.Color(0, 0, 0));
        txtDescription.setEnabled(false);
        txtDescription.setFocusable(false);
        txtDescription.setOpaque(false);
        pnlRight.add(txtDescription, java.awt.BorderLayout.PAGE_START);

        pnlSplit.setRightComponent(pnlRight);

        add(pnlSplit, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList lstLeft;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JScrollPane pnlScrollLeft;
    private javax.swing.JSplitPane pnlSplit;
    private javax.swing.JTextArea txtDescription;
    // End of variables declaration//GEN-END:variables

}
