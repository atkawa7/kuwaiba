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
 *  under the License.
 */
package org.inventory.core.containment;

import java.awt.BorderLayout;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import javax.swing.ActionMap;
import javax.swing.JList;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.containment.nodes.ClassMetadataChildren;
import org.inventory.core.containment.nodes.ClassMetadataSpecialChildren;
import org.inventory.core.containment.nodes.ClassMetadataTransferManager;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;

/** 
 * The main GUI component for customizing the standard and special container hierarchy
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(dtd = "-//org.inventory.core.containment//ContainmentManager//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ContainmentManagerTopComponent",
iconBase = "org/inventory/core/containment/res/icon.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.core.containment.ContainmentManagerTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools/Administration"),
    @ActionReference(path = "Toolbars/04_Customization", position = 2)})
@TopComponent.OpenActionRegistration(
    displayName = "Containment Manager",
preferredID = "ContainmentManagerTopComponent")
public final class ContainmentManagerTopComponent extends TopComponent
    implements Refreshable, ExplorerManager.Provider{

    private final ExplorerManager em = new ExplorerManager();
    private ContainmentManagerService service;
    private BeanTreeView bTreeView;
    private JList lstClasses;
    private NotificationUtil nu;
    
    public ContainmentManagerTopComponent() {
        initComponents();
        initComponentsCustom();
        setName("Containment Manager");
        setToolTipText("Manage the Standard and Special Containment Hierarchies");
    }

    private void initComponentsCustom() {
        associateLookup(ExplorerUtils.createLookup(em, new ActionMap()));

        service = new ContainmentManagerService(this);

        bTreeView = new BeanTreeView();
        lstClasses = new JList();

        bTreeView.setRootVisible(false);

        pnlLeft.add(bTreeView,BorderLayout.CENTER);

        //For now, due to tranferable constraints (I'd have to create a Tranferable List to support multiple selections)
        //lstClasses.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        lstClasses.setDragEnabled(true);

        ClassMetadataTransferManager tm = new ClassMetadataTransferManager(lstClasses);

        DragSource.getDefaultDragSource().createDefaultDragGestureRecognizer(lstClasses,DnDConstants.ACTION_MOVE, tm);
        lstClasses.setTransferHandler(tm);

        DragSource.getDefaultDragSource().addDragSourceListener(tm);

        pnlHierarchyManagerScrollMain.setViewportView(lstClasses);
        pnlHierarchyManagerMain.setDividerLocation(0.5);

    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlHierarchyManagerMain = new javax.swing.JSplitPane();
        pnlLeft = new javax.swing.JPanel();
        pnlRight = new javax.swing.JPanel();
        pnlHierarchyManagerScrollMain = new javax.swing.JScrollPane();
        lblInfo = new javax.swing.JLabel();
        toolBarMain = new javax.swing.JToolBar();
        btnStandartContainmentHierarchy = new javax.swing.JToggleButton();
        btnSpecialContainmentHierarchy = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout(10, 10));

        pnlHierarchyManagerMain.setOneTouchExpandable(true);

        pnlLeft.setLayout(new java.awt.BorderLayout());
        pnlHierarchyManagerMain.setLeftComponent(pnlLeft);

        pnlRight.setLayout(new java.awt.BorderLayout());
        pnlRight.add(pnlHierarchyManagerScrollMain, java.awt.BorderLayout.CENTER);

        pnlHierarchyManagerMain.setRightComponent(pnlRight);

        add(pnlHierarchyManagerMain, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(lblInfo, org.openide.util.NbBundle.getMessage(ContainmentManagerTopComponent.class, "ContainmentManagerTopComponent.lblInfo.text")); // NOI18N
        add(lblInfo, java.awt.BorderLayout.PAGE_END);

        toolBarMain.setRollover(true);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(326, 33));

        btnStandartContainmentHierarchy.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnStandartContainmentHierarchy, org.openide.util.NbBundle.getMessage(ContainmentManagerTopComponent.class, "ContainmentManagerTopComponent.btnStandartContainmentHierarchy.text")); // NOI18N
        btnStandartContainmentHierarchy.setToolTipText(org.openide.util.NbBundle.getMessage(ContainmentManagerTopComponent.class, "ContainmentManagerTopComponent.btnStandartContainmentHierarchy.toolTipText")); // NOI18N
        btnStandartContainmentHierarchy.setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        btnStandartContainmentHierarchy.setFocusable(false);
        btnStandartContainmentHierarchy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStandartContainmentHierarchy.setMargin(new java.awt.Insets(20, 20, 20, 20));
        btnStandartContainmentHierarchy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStandartContainmentHierarchy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnStandartContainmentHierarchyMouseClicked(evt);
            }
        });
        toolBarMain.add(btnStandartContainmentHierarchy);

        org.openide.awt.Mnemonics.setLocalizedText(btnSpecialContainmentHierarchy, org.openide.util.NbBundle.getMessage(ContainmentManagerTopComponent.class, "ContainmentManagerTopComponent.btnSpecialContainmentHierarchy.text")); // NOI18N
        btnSpecialContainmentHierarchy.setToolTipText(org.openide.util.NbBundle.getMessage(ContainmentManagerTopComponent.class, "ContainmentManagerTopComponent.btnSpecialContainmentHierarchy.toolTipText")); // NOI18N
        btnSpecialContainmentHierarchy.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 3, 3, 3));
        btnSpecialContainmentHierarchy.setFocusable(false);
        btnSpecialContainmentHierarchy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSpecialContainmentHierarchy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSpecialContainmentHierarchy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSpecialContainmentHierarchyMouseClicked(evt);
            }
        });
        toolBarMain.add(btnSpecialContainmentHierarchy);

        add(toolBarMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSpecialContainmentHierarchyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSpecialContainmentHierarchyMouseClicked
        if (!btnSpecialContainmentHierarchy.isSelected())
            btnSpecialContainmentHierarchy.setSelected(true);
        em.setRootContext(new AbstractNode(new ClassMetadataSpecialChildren(service.getTreeModel()))); 
        btnStandartContainmentHierarchy.setSelected(false);
    }//GEN-LAST:event_btnSpecialContainmentHierarchyMouseClicked

    private void btnStandartContainmentHierarchyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnStandartContainmentHierarchyMouseClicked
        if (!btnStandartContainmentHierarchy.isSelected())
            btnStandartContainmentHierarchy.setSelected(true);
        em.setRootContext(new AbstractNode(new ClassMetadataChildren(service.getTreeModel())));
        btnSpecialContainmentHierarchy.setSelected(false);
    }//GEN-LAST:event_btnStandartContainmentHierarchyMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnSpecialContainmentHierarchy;
    private javax.swing.JToggleButton btnStandartContainmentHierarchy;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JSplitPane pnlHierarchyManagerMain;
    private javax.swing.JScrollPane pnlHierarchyManagerScrollMain;
    private javax.swing.JPanel pnlLeft;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables


    public BeanTreeView getbTreeView() {
        return bTreeView;
    }

    public JList getLstClasses() {
        return lstClasses;
    }

    @Override
    public void componentOpened() {
        service.updateModels();
        em.setRootContext(new AbstractNode(new ClassMetadataChildren(service.getTreeModel())));
        lstClasses.setListData(service.getListModel().toArray());
    }

    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        lstClasses.removeAll();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) { }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    @Override
    public void refresh() {
        componentClosed();
        componentOpened();
    }
    
    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}