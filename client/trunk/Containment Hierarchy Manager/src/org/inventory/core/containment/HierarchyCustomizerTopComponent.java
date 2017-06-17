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
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.JList;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.containment.nodes.ClassMetadataChildren;
import org.inventory.core.containment.nodes.ClassMetadataTransferManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;


/** 
 * Represents the GUI for customizing the container hierarchy
 * @author Charles Edward bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(dtd = "-//org.inventory.core.containment//HierarchyCustomizer//EN",
autostore = false)
public final class HierarchyCustomizerTopComponent extends TopComponent
    implements Refreshable, ExplorerManager.Provider{

    private static HierarchyCustomizerTopComponent instance;
    static final String ICON_PATH = "org/inventory/core/containment/res/icon.png";
    private static final String PREFERRED_ID = "HierarchyCustomizerTopComponent";
    private final ExplorerManager em = new ExplorerManager();
    private HierarchyCustomizerService service;
    private BeanTreeView bTreeView;
    private JList lstClasses;
    private NotificationUtil nu;
    private HierarchyCustomizerConfigurationObject configurationObject;
    
    public HierarchyCustomizerTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "CTL_HierarchyCustomizerTopComponent"));
        setToolTipText(NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HINT_HierarchyCustomizerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    private void initComponentsCustom() {
        configurationObject = Lookup.getDefault().lookup(HierarchyCustomizerConfigurationObject.class);
        configurationObject.setProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL, false);
                
        associateLookup(ExplorerUtils.createLookup(em, new ActionMap()));

        service = new HierarchyCustomizerService(this);

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
        btnContainmentHierarchy = new javax.swing.JToggleButton();
        btnSpecialContainmentHierarchy = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout(10, 10));

        pnlHierarchyManagerMain.setOneTouchExpandable(true);

        pnlLeft.setLayout(new java.awt.BorderLayout());
        pnlHierarchyManagerMain.setLeftComponent(pnlLeft);

        pnlRight.setLayout(new java.awt.BorderLayout());
        pnlRight.add(pnlHierarchyManagerScrollMain, java.awt.BorderLayout.CENTER);

        pnlHierarchyManagerMain.setRightComponent(pnlRight);

        add(pnlHierarchyManagerMain, java.awt.BorderLayout.CENTER);

        org.openide.awt.Mnemonics.setLocalizedText(lblInfo, org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.lblInfo.text")); // NOI18N
        add(lblInfo, java.awt.BorderLayout.PAGE_END);

        toolBarMain.setRollover(true);

        btnContainmentHierarchy.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnContainmentHierarchy, org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.btnContainmentHierarchy.text")); // NOI18N
        btnContainmentHierarchy.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.btnContainmentHierarchy.toolTipText")); // NOI18N
        btnContainmentHierarchy.setFocusable(false);
        btnContainmentHierarchy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnContainmentHierarchy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnContainmentHierarchy.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnContainmentHierarchyMouseClicked(evt);
            }
        });
        toolBarMain.add(btnContainmentHierarchy);

        org.openide.awt.Mnemonics.setLocalizedText(btnSpecialContainmentHierarchy, org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.btnSpecialContainmentHierarchy.text")); // NOI18N
        btnSpecialContainmentHierarchy.setToolTipText(org.openide.util.NbBundle.getMessage(HierarchyCustomizerTopComponent.class, "HierarchyCustomizerTopComponent.btnSpecialContainmentHierarchy.toolTipText")); // NOI18N
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
        btnSpecialContainmentHierarchy.setSelected(true);
        if (!(boolean) configurationObject.getProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL)) {
            btnContainmentHierarchy.setSelected(false);
            configurationObject.setProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL, true);
            componentClosed();
            componentOpened();
        }
    }//GEN-LAST:event_btnSpecialContainmentHierarchyMouseClicked

    private void btnContainmentHierarchyMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnContainmentHierarchyMouseClicked
        btnContainmentHierarchy.setSelected(true);
        if ((boolean) configurationObject.getProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL)) {
            btnSpecialContainmentHierarchy.setSelected(false);
            configurationObject.setProperty(HierarchyCustomizerConfigurationObject.PROPERTY_ENABLE_SPECIAL, false);
            componentClosed();
            componentOpened();
        }
    }//GEN-LAST:event_btnContainmentHierarchyMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnContainmentHierarchy;
    private javax.swing.JToggleButton btnSpecialContainmentHierarchy;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JSplitPane pnlHierarchyManagerMain;
    private javax.swing.JScrollPane pnlHierarchyManagerScrollMain;
    private javax.swing.JPanel pnlLeft;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized HierarchyCustomizerTopComponent getDefault() {
        if (instance == null) {
            instance = new HierarchyCustomizerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the HierarchyCustomizerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized HierarchyCustomizerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(HierarchyCustomizerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof HierarchyCustomizerTopComponent) {
            return (HierarchyCustomizerTopComponent) win;
        }
        Logger.getLogger(HierarchyCustomizerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    public BeanTreeView getbTreeView() {
        return bTreeView;
    }

    public JList getLstClasses() {
        return lstClasses;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
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

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
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
