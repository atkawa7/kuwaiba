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
package org.inventory.customization.classhierarchy;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.customization.classhierarchy.scene.ClassHierarchyScene;
import org.inventory.customization.classhierarchy.scene.actions.HideSubclassAction;
import org.inventory.customization.classhierarchy.scene.actions.ShowSubclassAction;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.widget.Widget;
import org.openide.*;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
/**
 * Top component to display the Class Hierarchy in a scene.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.customization.classhierarchy//ClassHierarchy//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ClassHierarchyTopComponent",
        iconBase="org/inventory/customization/classhierarchy/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@Messages({
    "CTL_ClassHierarchyTopComponent=Class Hierarchy View",
    "HINT_ClassHierarchyTopComponent=Update Class Hierarchy View"
})
public final class ClassHierarchyTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable, ActionListener {
    
    private ExplorerManager em;
    private ClassHierarchyScene scene;
    private ClassHierarchyService service;
    
    public ClassHierarchyTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(Bundle.CTL_ClassHierarchyTopComponent());
        setToolTipText(Bundle.HINT_ClassHierarchyTopComponent());
    }
    
    private void initComponentsCustom() {
        em = new ExplorerManager();
        
        scene = new ClassHierarchyScene();
        service = new ClassHierarchyService(scene);
                                
        associateLookup(scene.getLookup());
        pnlMainScrollPane.setViewportView(scene.createView());
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolMain = new javax.swing.JToolBar();
        btnExpand = new javax.swing.JButton();
        btnCollapse = new javax.swing.JButton();
        btnShowAttributes = new javax.swing.JButton();
        btnOrganize = new javax.swing.JButton();
        btnExportAsImage = new javax.swing.JButton();
        btnExportAsXML = new javax.swing.JButton();
        layeredPaneLbl = new javax.swing.JLayeredPane();
        lblLocate = new javax.swing.JLabel();
        layeredPaneCmb = new javax.swing.JLayeredPane();
        cmbClassList = new javax.swing.JComboBox();
        pnlMainScrollPane = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        toolMain.setRollover(true);
        toolMain.setAlignmentY(0.5F);
        toolMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolMain.setPreferredSize(new java.awt.Dimension(326, 33));

        btnExpand.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/expand.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExpand, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExpand.text_1")); // NOI18N
        btnExpand.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExpand.toolTipText_1")); // NOI18N
        btnExpand.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExpand.actionCommand")); // NOI18N
        btnExpand.setFocusable(false);
        btnExpand.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExpand.setMaximumSize(new java.awt.Dimension(34, 34));
        btnExpand.setMinimumSize(new java.awt.Dimension(34, 34));
        btnExpand.setPreferredSize(new java.awt.Dimension(34, 34));
        btnExpand.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExpand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExpandActionPerformed(evt);
            }
        });
        toolMain.add(btnExpand);
        btnExpand.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExpand.AccessibleContext.accessibleName")); // NOI18N

        btnCollapse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/collapse.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCollapse, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnCollapse.text_1")); // NOI18N
        btnCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnCollapse.toolTipText")); // NOI18N
        btnCollapse.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnCollapse.actionCommand")); // NOI18N
        btnCollapse.setFocusable(false);
        btnCollapse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCollapse.setMaximumSize(new java.awt.Dimension(34, 34));
        btnCollapse.setMinimumSize(new java.awt.Dimension(34, 34));
        btnCollapse.setPreferredSize(new java.awt.Dimension(34, 34));
        btnCollapse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCollapseActionPerformed(evt);
            }
        });
        toolMain.add(btnCollapse);

        btnShowAttributes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/showAttributes.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowAttributes, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnShowAttributes.text_1")); // NOI18N
        btnShowAttributes.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnShowAttributes.toolTipText")); // NOI18N
        btnShowAttributes.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnShowAttributes.actionCommand")); // NOI18N
        btnShowAttributes.setFocusable(false);
        btnShowAttributes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowAttributes.setMaximumSize(new java.awt.Dimension(34, 34));
        btnShowAttributes.setMinimumSize(new java.awt.Dimension(34, 34));
        btnShowAttributes.setPreferredSize(new java.awt.Dimension(34, 34));
        btnShowAttributes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowAttributes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowAttributesActionPerformed(evt);
            }
        });
        toolMain.add(btnShowAttributes);

        btnOrganize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/organize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOrganize, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnOrganize.text_1")); // NOI18N
        btnOrganize.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnOrganize.toolTipText")); // NOI18N
        btnOrganize.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnOrganize.actionCommand")); // NOI18N
        btnOrganize.setFocusable(false);
        btnOrganize.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOrganize.setMaximumSize(new java.awt.Dimension(34, 34));
        btnOrganize.setMinimumSize(new java.awt.Dimension(34, 34));
        btnOrganize.setPreferredSize(new java.awt.Dimension(34, 34));
        btnOrganize.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOrganize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOrganizeActionPerformed(evt);
            }
        });
        toolMain.add(btnOrganize);

        btnExportAsImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/exportAsImg.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExportAsImage, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsImage.text_1")); // NOI18N
        btnExportAsImage.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsImage.toolTipText")); // NOI18N
        btnExportAsImage.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsImage.actionCommand")); // NOI18N
        btnExportAsImage.setFocusable(false);
        btnExportAsImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportAsImage.setMaximumSize(new java.awt.Dimension(34, 34));
        btnExportAsImage.setMinimumSize(new java.awt.Dimension(34, 34));
        btnExportAsImage.setPreferredSize(new java.awt.Dimension(34, 34));
        btnExportAsImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportAsImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportAsImageActionPerformed(evt);
            }
        });
        toolMain.add(btnExportAsImage);

        btnExportAsXML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/classhierarchy/res/exportAsXML.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExportAsXML, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsXML.text_1")); // NOI18N
        btnExportAsXML.setToolTipText(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsXML.toolTipText")); // NOI18N
        btnExportAsXML.setActionCommand(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.btnExportAsXML.actionCommand")); // NOI18N
        btnExportAsXML.setFocusable(false);
        btnExportAsXML.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportAsXML.setMaximumSize(new java.awt.Dimension(34, 34));
        btnExportAsXML.setMinimumSize(new java.awt.Dimension(34, 34));
        btnExportAsXML.setPreferredSize(new java.awt.Dimension(34, 34));
        btnExportAsXML.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportAsXML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportAsXMLActionPerformed(evt);
            }
        });
        toolMain.add(btnExportAsXML);

        layeredPaneLbl.setLayout(new java.awt.BorderLayout());

        org.openide.awt.Mnemonics.setLocalizedText(lblLocate, org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.lblLocate.text")); // NOI18N
        layeredPaneLbl.add(lblLocate, java.awt.BorderLayout.EAST);

        toolMain.add(layeredPaneLbl);

        layeredPaneCmb.setPreferredSize(new java.awt.Dimension(32, 24));
        layeredPaneCmb.setLayout(new java.awt.BorderLayout());

        layeredPaneCmb.add(cmbClassList, java.awt.BorderLayout.CENTER);

        toolMain.add(layeredPaneCmb);

        add(toolMain, java.awt.BorderLayout.PAGE_START);
        toolMain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(ClassHierarchyTopComponent.class, "ClassHierarchyTopComponent.toolMain.AccessibleContext.accessibleDescription")); // NOI18N

        add(pnlMainScrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnExpandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExpandActionPerformed
        service.expandClassHierarchy();
    }//GEN-LAST:event_btnExpandActionPerformed

    private void btnOrganizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrganizeActionPerformed
        scene.reorganizeNodes();
    }//GEN-LAST:event_btnOrganizeActionPerformed

    private void btnExportAsImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportAsImageActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ ImageFilter.getInstance() }, 
                scene, "class_hierarchy");
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options", true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportAsImageActionPerformed

    private void btnCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCollapseActionPerformed
        service.collapseClassHierarchy();
        listOfClasses();
    }//GEN-LAST:event_btnCollapseActionPerformed

    private void btnExportAsXMLActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportAsXMLActionPerformed
        final JTextField txtClassHierarchyName = new JTextField();
        txtClassHierarchyName.setName("txtClassHierarchyName");
        txtClassHierarchyName.setColumns(15);
                
        final JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fChooser.setDialogTitle("Select a directory");
        
        final JButton btnFileChooser = new JButton();
        btnFileChooser.setAction(new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String classHierarchyName = txtClassHierarchyName.getText();
                if (!classHierarchyName.isEmpty()) {
                    if (!classHierarchyName.contains(".xml"))
                        classHierarchyName += ".xml";
                    }
                    else {
                        classHierarchyName  = "classHierarchy" + Calendar.getInstance().get(Calendar.DAY_OF_YEAR) + //NOI18N
                                              "-"+Calendar.getInstance().get(Calendar.MINUTE)+ //NOI18N
                                              ".xml";
                    }
                if (fChooser.showSaveDialog(btnFileChooser) == JFileChooser.APPROVE_OPTION) {
                    txtClassHierarchyName.setText(fChooser.getSelectedFile().getAbsolutePath() + 
                    File.separator + classHierarchyName);
                }
            }
        });
        btnFileChooser.setText("...");
        btnFileChooser.setToolTipText("Select a directory");
        btnFileChooser.setSize(34, 34);
        
        JPanel pnl = new JPanel();
        pnl.add(txtClassHierarchyName);
        pnl.add(btnFileChooser);
        
        JComplexDialogPanel exportAsXMLDialog = new JComplexDialogPanel(
                new String[] {"Name "}, 
                new JComponent[] {pnl});
        
        if(JOptionPane.showConfirmDialog(null, exportAsXMLDialog, "Export AS XML", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(txtClassHierarchyName.getText()), "utf-8"))) {
                writer.write(service.getHierarchyAsString());
            } catch(Exception ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                        NotificationUtil.ERROR_MESSAGE, 
                        "Can not create the file");
            }
        }
    }//GEN-LAST:event_btnExportAsXMLActionPerformed

    private void btnShowAttributesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowAttributesActionPerformed
        service.showAllAttributes();
    }//GEN-LAST:event_btnShowAttributesActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnExpand;
    private javax.swing.JButton btnExportAsImage;
    private javax.swing.JButton btnExportAsXML;
    private javax.swing.JButton btnOrganize;
    private javax.swing.JButton btnShowAttributes;
    private javax.swing.JComboBox cmbClassList;
    private javax.swing.JLayeredPane layeredPaneCmb;
    private javax.swing.JLayeredPane layeredPaneLbl;
    private javax.swing.JLabel lblLocate;
    private javax.swing.JScrollPane pnlMainScrollPane;
    private javax.swing.JToolBar toolMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        scene.render(service.getRootClass());
        scene.addChangeListener(this);
        
        cmbClassList.addActionListener(this);               
        listOfClasses();
    }
    
    @Override
    public void componentClosed() {
        scene.removeAllListeners();
        scene.clear();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ShowSubclassAction.COMMAND)) {
            service.addSubclasses((LocalClassMetadata) e.getSource(), false);
            listOfClasses();
        }
        
        if (e.getActionCommand().equals(HideSubclassAction.COMMAND)) {
            service.removeSubclasses((LocalClassMetadata) e.getSource());
            listOfClasses();
        }
        
        if (e.getActionCommand().equals(ClassHierarchyScene.COMMAND_EXPAND)) {
            service.addSubclasses((LocalClassMetadata) e.getSource(), true);
            listOfClasses();
        }
        
        if ("comboBoxChanged".equals(e.getActionCommand()))
            comboBoxChanged(e);
    }

    @Override
    public void refresh() {
    }
    
    private void listOfClasses() {
        List<LocalClassMetadata> classes = new ArrayList();
                
        for (Object object : scene.getObjects()) {
            if (object instanceof LocalClassMetadata) {
                classes.add((LocalClassMetadata) object);
            }
        }
        
        Collections.sort(classes, new Comparator<LocalClassMetadata>() {

            @Override
            public int compare(LocalClassMetadata lcm1, LocalClassMetadata lcm2) {
                return lcm1.getClassName().compareTo(lcm2.getClassName());
            }
            
        });
        cmbClassList.removeActionListener(this);
        cmbClassList.removeAllItems();
        for (LocalClassMetadata aClass : classes)
                cmbClassList.addItem(aClass);
        cmbClassList.addActionListener(this);
    }
    
    public void comboBoxChanged(ActionEvent e) {
        LocalClassMetadataLight selectedItem = (LocalClassMetadataLight) ((JComboBox)e.getSource()).getSelectedItem();

        Widget widget = scene.findWidget(selectedItem);
        if (widget != null) {
            //See: https://netbeans.org/projects/platform/lists/graph/archive/2007-09/message/5
            scene.getView().scrollRectToVisible(widget.getScene().convertSceneToView(widget.convertLocalToScene(widget.getBounds())));

            scene.userSelectionSuggested (Collections.emptySet (), false);
            scene.userSelectionSuggested (Collections.singleton (selectedItem), true);

            //Updates the lookup so that other modules are aware of this selection
            ((AbstractScene.SceneLookup)scene.getLookup()).updateLookup(scene.findWidget(selectedItem));
        }
    }
}
