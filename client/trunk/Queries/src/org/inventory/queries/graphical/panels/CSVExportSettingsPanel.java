/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical.panels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.queries.graphical.exportfilters.CSVFilter;
import org.openide.DialogDescriptor;

/**
 * Shows the CSV filter settings (separator type)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class CSVExportSettingsPanel extends javax.swing.JPanel implements ActionListener{

    private Character selectedCharacter = CSVFilter.CHARACTER_COMMA;
    /** Creates new form CSVExportSettingsPanel */
    public CSVExportSettingsPanel() {
        initComponents();
        cmbSeparator.addItem("Comma (,)");
        cmbSeparator.addItem("Tab");
        cmbSeparator.addItem("Space");
        cmbSeparator.addItem("Pipe (|)");
        cmbSeparator.addItem("Tilde (~)");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblSeparator = new javax.swing.JLabel();
        cmbSeparator = new javax.swing.JComboBox();

        lblSeparator.setText(org.openide.util.NbBundle.getMessage(CSVExportSettingsPanel.class, "CSVExportSettingsPanel.lblSeparator.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblSeparator)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(107, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblSeparator)
                    .addComponent(cmbSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(43, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox cmbSeparator;
    private javax.swing.JLabel lblSeparator;
    // End of variables declaration//GEN-END:variables

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == DialogDescriptor.OK_OPTION){
            switch (cmbSeparator.getSelectedIndex()){
                case 0:
                    selectedCharacter = CSVFilter.CHARACTER_COMMA;
                    break;
                case 1:
                    selectedCharacter = CSVFilter.CHARACTER_TAB;
                    break;
                case 2:
                    selectedCharacter = CSVFilter.CHARACTER_SPACE;
                    break;
                case 3:
                    selectedCharacter = CSVFilter.CHARACTER_PIPE;
                    break;
                case 4:
                    selectedCharacter = CSVFilter.CHARACTER_TILDE;
                    break;
            }
        }
    }

    public Object getSelectedCharacter() {
        return selectedCharacter;
    }

}
