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
package org.inventory.views.gis;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.views.gis.dialogs.OpenDialog;
import org.inventory.views.gis.dialogs.SaveDialog;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.ObjectNodeWidget;
import org.inventory.views.gis.scene.providers.PhysicalConnectionProvider;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.inventory.views.gis//GISView//EN",
autostore = false)
public final class GISViewTopComponent extends TopComponent implements ExplorerManager.Provider{

    private ButtonGroup aButtonGroup;
    private static GISViewTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/views/gis/res/icon.png";
    private static final String PREFERRED_ID = "GISViewTopComponent";
    private GISViewService gvs;
    private boolean isSaved = false;
    private NotificationUtil nu;
    /**
     * Main scene
     */
    private GISViewScene scene;
    /**
     * TC Explorer Manager
     */
    private ExplorerManager em = new ExplorerManager();

    public GISViewTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(GISViewTopComponent.class, "CTL_GISViewTopComponent"));
        setToolTipText(NbBundle.getMessage(GISViewTopComponent.class, "HINT_GISViewTopComponent"));
        scene = new GISViewScene();
        this.gvs = new GISViewService(scene, this);
        add(scene.createView(), BorderLayout.CENTER);
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        associateLookup(scene.getLookup());
        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                scene.updateMapBounds();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }

    private void initCustomComponents(){
        aButtonGroup = new ButtonGroup();
        aButtonGroup.add(btnWireContainer);
        aButtonGroup.add(btnWirelessContainer);
        aButtonGroup.add(btnElectricalLink);
        aButtonGroup.add(btnOpticalLink);
        aButtonGroup.add(btnWirelessLink);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barToolMain = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnSelect = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        btnZoomIn = new javax.swing.JButton();
        btnZoomOut = new javax.swing.JButton();
        btnShowNodeLabels = new javax.swing.JToggleButton();
        sepMainSeparator = new javax.swing.JToolBar.Separator();
        btnWireContainer = new javax.swing.JToggleButton();
        btnWirelessContainer = new javax.swing.JToggleButton();
        btnOpticalLink = new javax.swing.JToggleButton();
        btnElectricalLink = new javax.swing.JToggleButton();
        btnWirelessLink = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout());

        barToolMain.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnNew.text")); // NOI18N
        btnNew.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnNew.toolTipText")); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        barToolMain.add(btnNew);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnOpen.toolTipText")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        barToolMain.add(btnOpen);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setEnabled(false);
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barToolMain.add(btnDelete);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barToolMain.add(btnSave);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setEnabled(false);
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barToolMain.add(btnExport);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/select.png"))); // NOI18N
        btnSelect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setEnabled(false);
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        barToolMain.add(btnSelect);

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setEnabled(false);
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barToolMain.add(btnConnect);

        btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/zoom-in.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnZoomIn, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnZoomIn.text")); // NOI18N
        btnZoomIn.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnZoomIn.toolTipText")); // NOI18N
        btnZoomIn.setEnabled(false);
        btnZoomIn.setFocusable(false);
        btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomInActionPerformed(evt);
            }
        });
        barToolMain.add(btnZoomIn);

        btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/zoom-out.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnZoomOut, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnZoomOut.text")); // NOI18N
        btnZoomOut.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnZoomOut.toolTipText")); // NOI18N
        btnZoomOut.setEnabled(false);
        btnZoomOut.setFocusable(false);
        btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomOutActionPerformed(evt);
            }
        });
        barToolMain.add(btnZoomOut);

        btnShowNodeLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/hide_node_labels.png"))); // NOI18N
        btnShowNodeLabels.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnShowNodeLabels, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnShowNodeLabels.text")); // NOI18N
        btnShowNodeLabels.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnShowNodeLabels.toolTipText")); // NOI18N
        btnShowNodeLabels.setEnabled(false);
        btnShowNodeLabels.setFocusable(false);
        btnShowNodeLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowNodeLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowNodeLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowNodeLabelsActionPerformed(evt);
            }
        });
        barToolMain.add(btnShowNodeLabels);
        barToolMain.add(sepMainSeparator);

        btnWireContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/wire-container.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWireContainer, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWireContainer.text")); // NOI18N
        btnWireContainer.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWireContainer.toolTipText")); // NOI18N
        btnWireContainer.setEnabled(false);
        btnWireContainer.setFocusable(false);
        btnWireContainer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWireContainer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWireContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWireContainerActionPerformed(evt);
            }
        });
        barToolMain.add(btnWireContainer);

        btnWirelessContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/wireless-container.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWirelessContainer, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWirelessContainer.text")); // NOI18N
        btnWirelessContainer.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWirelessContainer.toolTipText")); // NOI18N
        btnWirelessContainer.setEnabled(false);
        btnWirelessContainer.setFocusable(false);
        btnWirelessContainer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWirelessContainer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWirelessContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWirelessContainerActionPerformed(evt);
            }
        });
        barToolMain.add(btnWirelessContainer);

        btnOpticalLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/optical-link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpticalLink, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnOpticalLink.text")); // NOI18N
        btnOpticalLink.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnOpticalLink.toolTipText")); // NOI18N
        btnOpticalLink.setEnabled(false);
        btnOpticalLink.setFocusable(false);
        btnOpticalLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpticalLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpticalLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpticalLinkActionPerformed(evt);
            }
        });
        barToolMain.add(btnOpticalLink);

        btnElectricalLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/electrical-link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnElectricalLink, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnElectricalLink.text")); // NOI18N
        btnElectricalLink.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnElectricalLink.toolTipText")); // NOI18N
        btnElectricalLink.setEnabled(false);
        btnElectricalLink.setFocusable(false);
        btnElectricalLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnElectricalLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnElectricalLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElectricalLinkActionPerformed(evt);
            }
        });
        barToolMain.add(btnElectricalLink);

        btnWirelessLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/gis/res/wireless-link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWirelessLink, org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWirelessLink.text")); // NOI18N
        btnWirelessLink.setToolTipText(org.openide.util.NbBundle.getMessage(GISViewTopComponent.class, "GISViewTopComponent.btnWirelessLink.toolTipText")); // NOI18N
        btnWirelessLink.setEnabled(false);
        btnWirelessLink.setFocusable(false);
        btnWirelessLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWirelessLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWirelessLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWirelessLinkActionPerformed(evt);
            }
        });
        barToolMain.add(btnWirelessLink);

        add(barToolMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomInActionPerformed
        scene.zoomIn();
    }//GEN-LAST:event_btnZoomInActionPerformed

    private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomOutActionPerformed
        scene.zoomOut();
    }//GEN-LAST:event_btnZoomOutActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(ObjectNodeWidget.ACTION_SELECT);
        btnConnect.setSelected(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(ObjectNodeWidget.ACTION_CONNECT);
        btnSelect.setSelected(false);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnWireContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWireContainerActionPerformed
        scene.getConnectProvider().setCurrentConnectionSelection(PhysicalConnectionProvider.CONNECTION_WIRECONTAINER);
        btnConnect.setSelected(true);
        btnConnectActionPerformed(evt);
    }//GEN-LAST:event_btnWireContainerActionPerformed

    private void btnWirelessContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWirelessContainerActionPerformed
        scene.getConnectProvider().setCurrentConnectionSelection(PhysicalConnectionProvider.CONNECTION_WIRELESSCONTAINER);
        btnConnect.setSelected(true);
        btnConnectActionPerformed(evt);
    }//GEN-LAST:event_btnWirelessContainerActionPerformed

    private void btnOpticalLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpticalLinkActionPerformed
        scene.getConnectProvider().setCurrentConnectionSelection(PhysicalConnectionProvider.CONNECTION_OPTICALLINK);
        btnConnect.setSelected(true);
        btnConnectActionPerformed(evt);
    }//GEN-LAST:event_btnOpticalLinkActionPerformed

    private void btnElectricalLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElectricalLinkActionPerformed
        scene.getConnectProvider().setCurrentConnectionSelection(PhysicalConnectionProvider.CONNECTION_ELECTRICALLINK);
        btnConnect.setSelected(true);
        btnConnectActionPerformed(evt);
    }//GEN-LAST:event_btnElectricalLinkActionPerformed

    private void btnWirelessLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWirelessLinkActionPerformed
        scene.getConnectProvider().setCurrentConnectionSelection(PhysicalConnectionProvider.CONNECTION_WIRELESSLINK);
        btnConnect.setSelected(true);
        btnConnectActionPerformed(evt);
    }//GEN-LAST:event_btnWirelessLinkActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        scene.clear();
        scene.activateMap();
        gvs.setCurrentView(null);
        toggleButtons(true);
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!isSaved){
            SaveDialog saveDialog;
            if (gvs.getCurrentView() == null)
                saveDialog = new SaveDialog(null, null);
            else
                saveDialog = new SaveDialog(gvs.getCurrentView().getName(), gvs.getCurrentView().getDescription());

            if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), saveDialog,"Save GIS view", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION)
                gvs.saveView(saveDialog.getNameInTxt(), saveDialog.getDescriptionInTxt());
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        List<LocalObjectViewLight> views = CommunicationsStub.getInstance().getGeneralViews(LocalObjectViewLight.TYPE_GIS);
        if (views == null)
            JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), "Load View", JOptionPane.ERROR_MESSAGE);
        else{
            OpenDialog openDialog = new OpenDialog(views.toArray(new LocalObjectViewLight[0]));

            if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(), openDialog,"Open GIS view", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
                if (openDialog.getSelectedObject() !=  null)
                    gvs.loadView(openDialog.getSelectedObject().getId());
            }
        }
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        gvs.deleteCurrentView();
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnShowNodeLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowNodeLabelsActionPerformed
        gvs.toggleLabels(!btnShowNodeLabels.isSelected());
    }//GEN-LAST:event_btnShowNodeLabelsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barToolMain;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JButton btnDelete;
    private javax.swing.JToggleButton btnElectricalLink;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JToggleButton btnOpticalLink;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowNodeLabels;
    private javax.swing.JToggleButton btnWireContainer;
    private javax.swing.JToggleButton btnWirelessContainer;
    private javax.swing.JToggleButton btnWirelessLink;
    private javax.swing.JButton btnZoomIn;
    private javax.swing.JButton btnZoomOut;
    private javax.swing.JToolBar.Separator sepMainSeparator;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized GISViewTopComponent getDefault() {
        if (instance == null) {
            instance = new GISViewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the GISViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized GISViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(GISViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof GISViewTopComponent) {
            return (GISViewTopComponent) win;
        }
        Logger.getLogger(GISViewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        scene.paint();
    }

    @Override
    public void componentClosed() {
        toggleButtons(false);
        scene.clear();
    }

    /**
     * Enable or disable buttons massively
     * @param enabled
     */
    public void toggleButtons(boolean enabled) {
        btnConnect.setEnabled(enabled);
        btnElectricalLink.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnOpticalLink.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnWireContainer.setEnabled(enabled);
        btnWirelessContainer.setEnabled(enabled);
        btnWirelessLink.setEnabled(enabled);
        btnZoomIn.setEnabled(enabled);
        btnZoomOut.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnShowNodeLabels.setEnabled(enabled);
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
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);

        return nu;
    }

}
