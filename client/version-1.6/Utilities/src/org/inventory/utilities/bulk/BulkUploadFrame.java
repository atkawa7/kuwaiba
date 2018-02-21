/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.utilities.bulk;

import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * JFrame to select csv files for bulk load
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class BulkUploadFrame extends javax.swing.JFrame {

    private javax.swing.JButton btnFileChooserListTypes;
    private javax.swing.JButton btnFileChooserObjects;
    private javax.swing.JButton btnProcessListTypes;
    private javax.swing.JButton btnProcessObjects;
    private javax.swing.JLabel lblCommitSizeLt;
    private javax.swing.JLabel lblCommitSizeO;
    private javax.swing.JLabel lblMessageListTypes;
    private javax.swing.JLabel lblMessageObjects;
    private javax.swing.JPanel pnlListType;
    private javax.swing.JPanel pnlObject;
    private javax.swing.JTabbedPane tabPnlBulkUpload;
    private javax.swing.JSpinner spnListTypeCommitSize;
    private javax.swing.JSpinner spnObjectsCommitSize;
    private javax.swing.JFileChooser fChooser;
    private BulkUploadService ss;
    private byte[] chosenFile;
    private static BulkUploadFrame instance; //Singleton

    public static BulkUploadFrame getInstance() {
        if (instance == null) 
            instance = new BulkUploadFrame();

        return instance;
    }
    
    private BulkUploadFrame() {
        initComponents();
        TopComponent outputWindow = WindowManager.getDefault().findTopComponent("output");
        if (!outputWindow.isOpened())
            outputWindow.open();
        outputWindow.requestAttention(true);
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    public final void initComponents(){
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        tabPnlBulkUpload = new javax.swing.JTabbedPane();
        pnlListType = new javax.swing.JPanel();
        btnProcessListTypes = new javax.swing.JButton();
        btnFileChooserListTypes = new javax.swing.JButton();
        lblCommitSizeLt = new javax.swing.JLabel();
        lblCommitSizeO = new javax.swing.JLabel();
        spnListTypeCommitSize = new javax.swing.JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
        pnlObject = new javax.swing.JPanel();
        btnFileChooserObjects = new javax.swing.JButton();
        lblMessageListTypes = new javax.swing.JLabel();
        lblMessageObjects = new javax.swing.JLabel();
        spnObjectsCommitSize = new javax.swing.JSpinner(new SpinnerNumberModel(1, 0, 10000, 1));
        btnProcessObjects = new javax.swing.JButton();
        
        fChooser = Utils.getGlobalFileChooser();
        fChooser.setAcceptAllFileFilterUsed(false);
        fChooser.setFileFilter(new UploadFileFilter());
        fChooser.setMultiSelectionEnabled(false);
                
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_TITLE"));
        
        btnFileChooserListTypes.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (fChooser.showOpenDialog(fChooser) == JFileChooser.APPROVE_OPTION){
                    try {
                        chosenFile = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });
        
        btnFileChooserObjects.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (fChooser.showOpenDialog(fChooser) == JFileChooser.APPROVE_OPTION){
                    try {
                        chosenFile = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        lblMessageListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_SELECT_LIST_TYPES_FILE")); // NOI18N
        lblMessageListTypes.setAlignmentX(CENTER_ALIGNMENT);
        btnFileChooserListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_SELECT_FILE")); // NOI18N
        btnFileChooserListTypes.setAlignmentX(CENTER_ALIGNMENT);
        lblCommitSizeLt.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_COMMIT_SIZE")); // NOI18N
        btnProcessListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_PROCESS_FILE"));
        
        btnProcessListTypes.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnProcessListTypesActionPerformed(e);
            }
        });
        
        pnlListType.setBorder(new EmptyBorder(10, 10, 10, 10) );
        pnlListType.add(lblMessageListTypes);
        pnlListType.add(btnFileChooserListTypes);
        pnlListType.add(btnProcessListTypes);

        //Upload objects tab
        btnFileChooserObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_SELECT_FILE")); // NOI18N
        btnFileChooserObjects.setAlignmentX(CENTER_ALIGNMENT);
        lblMessageObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_SELECT_OBJECTS_FILE")); // NOI18N
        lblCommitSizeO.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_COMMIT_SIZE")); // NOI18N
        lblMessageObjects.setAlignmentX(CENTER_ALIGNMENT);
        btnProcessObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_PROCESS_FILE"));
        
        btnProcessObjects.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnProcessObjectsActionPerformed(e);
            }
        });
        
        pnlObject.setBorder(new EmptyBorder(10, 10, 10, 10) );
        pnlObject.add(lblMessageObjects);
        pnlObject.add(btnFileChooserObjects);
        pnlObject.add(btnProcessObjects);

        tabPnlBulkUpload.addTab(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_OBJECTS"), pnlObject); // NOI18N
        tabPnlBulkUpload.addTab(java.util.ResourceBundle.getBundle("org/inventory/utilities/Bundle").getString("LBL_LIST_TYPES"), pnlListType); // NOI18N

        getContentPane().add(tabPnlBulkUpload);

        pack();
    }
    // </editor-fold> 
   
    private void btnProcessListTypesActionPerformed(java.awt.event.ActionEvent evt) { 
        if (chosenFile == null) {
            JOptionPane.showMessageDialog(null, "No file has been selected yet", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ss = new BulkUploadService(chosenFile, (Integer)spnListTypeCommitSize.getValue(), 1);
        ss.run();
        NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Your will be being process in background");   
    }                                                   

    private void btnProcessObjectsActionPerformed(java.awt.event.ActionEvent evt) {
        if (chosenFile == null) {
            JOptionPane.showMessageDialog(null, "No file has been selected yet", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        ss = new BulkUploadService(chosenFile,(Integer)spnObjectsCommitSize.getValue(), 2);
        ss.run();
    }
}
