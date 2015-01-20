/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.sync;

import static java.awt.Component.CENTER_ALIGNMENT;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.sync.windows.ExporFilePanel;
import org.inventory.sync.windows.WriteFile;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
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
    private javax.swing.JTextField txtListTypeCommitSize;
    private javax.swing.JTextField txtObjectsCommitSize;
    private javax.swing.JFileChooser fChooser;
    private SyncService ss;
    private byte[] choosenFile = null;

    public BulkUploadFrame() {
        initComponents();
        final String OUTPUT_ID = "output";
//        TopComponent outputWindow = WindowManager.getDefault().findTopComponent(OUTPUT_ID);
//        if (!outputWindow.isOpened())
//            outputWindow.open();
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    public void initComponents(){
        tabPnlBulkUpload = new javax.swing.JTabbedPane();
        pnlListType = new javax.swing.JPanel();
        btnProcessListTypes = new javax.swing.JButton();
        btnFileChooserListTypes = new javax.swing.JButton();
        lblCommitSizeLt = new javax.swing.JLabel();
        lblCommitSizeO = new javax.swing.JLabel();
        txtListTypeCommitSize = new javax.swing.JTextField();
        pnlObject = new javax.swing.JPanel();
        btnFileChooserObjects = new javax.swing.JButton();
        lblMessageListTypes = new javax.swing.JLabel();
        lblMessageObjects = new javax.swing.JLabel();
        txtObjectsCommitSize = new javax.swing.JTextField();
        btnProcessObjects = new javax.swing.JButton();
        
        fChooser = new javax.swing.JFileChooser();
        fChooser.setAcceptAllFileFilterUsed(false);
        fChooser.setFileFilter(new UploadFileFilter());
        fChooser.setMultiSelectionEnabled(false);
                
        setTitle(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_TITLE"));
        
        btnFileChooserListTypes.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if (fChooser.showOpenDialog(fChooser) == JFileChooser.APPROVE_OPTION){
                    try {
                        choosenFile = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                       
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
                        choosenFile = Utils.getByteArrayFromFile(fChooser.getSelectedFile());
                       
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        });

        lblMessageListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_SELECT_LIST_TYPES_FILE")); // NOI18N
        lblMessageListTypes.setAlignmentX(CENTER_ALIGNMENT);
        btnFileChooserListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_SELECT_FILE")); // NOI18N
        btnFileChooserListTypes.setAlignmentX(CENTER_ALIGNMENT);
        lblCommitSizeLt.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_COMMIT_SIZE")); // NOI18N
        txtListTypeCommitSize.setText("1"); // NOI18N
        btnProcessListTypes.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_PROCESS_FILE"));
        
        btnProcessListTypes.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnProcessListTypesActionPerformed(e);
            }
        });
        
        pnlListType.setBorder(new EmptyBorder(10, 10, 10, 10) );
        pnlListType.add(lblMessageListTypes);
        pnlListType.add(btnFileChooserListTypes);
        pnlListType.add(lblCommitSizeLt);
        pnlListType.add(txtListTypeCommitSize);
        pnlListType.add(btnProcessListTypes);

        tabPnlBulkUpload.addTab(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_LIST_TYPES"), pnlListType); // NOI18N
        
        btnFileChooserObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_SELECT_FILE")); // NOI18N
        btnFileChooserObjects.setAlignmentX(CENTER_ALIGNMENT);
        lblMessageObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_SELECT_OBJECTS_FILE")); // NOI18N
        lblCommitSizeO.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_COMMIT_SIZE")); // NOI18N
        lblMessageObjects.setAlignmentX(CENTER_ALIGNMENT);
        txtObjectsCommitSize.setText("1"); // NOI18N
        btnProcessObjects.setText(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_PROCESS_FILE"));
        
        btnProcessObjects.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnProcessObjectsActionPerformed(e);
            }
        });
        
        pnlObject.setBorder(new EmptyBorder(10, 10, 10, 10) );
        pnlObject.add(lblMessageObjects);
        pnlObject.add(btnFileChooserObjects);
        pnlObject.add(lblCommitSizeO);
        pnlObject.add(txtObjectsCommitSize);
        
        pnlObject.add(btnProcessObjects);

        tabPnlBulkUpload.addTab(java.util.ResourceBundle.getBundle("org/inventory/sync/Bundle").getString("LBL_OBJECTS"), pnlObject); // NOI18N
        getContentPane().add(tabPnlBulkUpload);

        pack();
    }
    // </editor-fold> 
   
    private void btnProcessListTypesActionPerformed(java.awt.event.ActionEvent evt) { 
        ss = new SyncService(choosenFile,Integer.parseInt(txtObjectsCommitSize.getText()), 1);
        ss.run();
        NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Your will be being process in background");
        if(choosenFile == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Please select a file");
        if(ss.loadFile()){
            ExporFilePanel efp = new ExporFilePanel();
            if(JOptionPane.showConfirmDialog(null, efp, "Choose a path to save your logs", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                efp.getPath();
                WriteFile wr = new WriteFile();
                ss.downloadLog();
                byte[] logFile = ss.getLogFile();
                try {
                    wr.writeLog(ss.getLogFile(), efp.getPath()+"/"+ss.getFileName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }                                                   

    private void btnProcessObjectsActionPerformed(java.awt.event.ActionEvent evt) {                                                  
        ss = new SyncService(choosenFile,Integer.parseInt(txtObjectsCommitSize.getText()), 2);
        ss.run();
        NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "Your file will be process in background");
        if(choosenFile == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, "Please select a file");
        if(ss.loadFile()){
            ExporFilePanel efp = new ExporFilePanel();
            if(JOptionPane.showConfirmDialog(null, efp, "Choose a path", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                efp.getPath();
                WriteFile wr = new WriteFile();
                ss.downloadLog();
                byte[] logFile = ss.getLogFile();
                try {
                    wr.writeLog(ss.getLogFile(), efp.getPath()+"/"+ss.getFileName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }                                                 
}
