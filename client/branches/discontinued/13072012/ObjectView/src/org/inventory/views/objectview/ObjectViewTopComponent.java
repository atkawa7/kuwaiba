/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.views.objectview;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JOptionPane;
import org.inventory.communications.SharedInformation;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.behaviors.RefreshableTopComponent;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.ExportSceneAction;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.objectview.dialogs.FormatTextPanel;
import org.inventory.views.objectview.scene.ObjectConnectionWidget;
import org.inventory.views.objectview.scene.ObjectNodeWidget;
import org.inventory.views.objectview.scene.ViewScene;
import org.openide.explorer.ExplorerManager;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * This component renders the views associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ConvertAsProperties(dtd = "-//org.inventory.views.objectview//ObjectView//EN",
autostore = false)
public final class ObjectViewTopComponent extends TopComponent 
        implements Provider, ActionListener,RefreshableTopComponent {

    private static ObjectViewTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/views/objectview/res/icon.png";
    private static final String PREFERRED_ID = "ObjectViewTopComponent";
    public static final int CONNECTION_WIRECONTAINER = 1;
    public static final int CONNECTION_WIRELESSCONTAINER = 2;
    public static final int CONNECTION_ELECTRICALLINK = 3;
    public static final int CONNECTION_OPTICALLINK = 4;
    public static final int CONNECTION_WIRELESSLINK = 5;
    

    private Font currentFont = ObjectNodeWidget.defaultFont;
    private Color currentColor = Color.black;
    private ButtonGroup buttonGroupUpperToolbar;
    private ButtonGroup buttonGroupRightToolbar;
    private NotificationUtil nu;

    private ExplorerManager em = new ExplorerManager();
    private ObjectViewService vrs;
    /**
     * To warn the user if the view has not been saved yet
     */
    private boolean isSaved = true;
    /**
     * Represents the local scene
     */
    private ViewScene scene;

    public ObjectViewTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(ObjectViewTopComponent.class, "CTL_ObjectViewTopComponent"));
        setToolTipText(NbBundle.getMessage(ObjectViewTopComponent.class, "HINT_ObjectViewTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    public final void initCustomComponents(){

        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));

        vrs = new ObjectViewService(this);

        scene = new ViewScene(getNotifier());

        pnlScrollMain.setViewportView(scene.createView());
        add(scene.createSatelliteView(),BorderLayout.SOUTH);

        btnWireContainer.setName(SharedInformation.CLASS_WIRECONTAINER);
        btnWirelessContainer.setName(SharedInformation.CLASS_WIRELESSCONTAINER);

        buttonGroupUpperToolbar = new ButtonGroup();
        buttonGroupUpperToolbar.add(btnSelect);
        buttonGroupUpperToolbar.add(btnConnect);

        buttonGroupRightToolbar = new ButtonGroup();
        buttonGroupRightToolbar.add(btnElectricalLink);
        buttonGroupRightToolbar.add(btnOpticalLink);
        buttonGroupRightToolbar.add(btnWirelessLink);
        buttonGroupRightToolbar.add(btnWireContainer);
        buttonGroupRightToolbar.add(btnWirelessContainer);
        btnSelect.setSelected(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barMain = new javax.swing.JToolBar();
        btnAddBackgroundImage = new javax.swing.JButton();
        btnRemoveBackground = new javax.swing.JButton();
        btnFormatText = new javax.swing.JButton();
        btnShowNodeLabels = new javax.swing.JToggleButton();
        btnSave = new javax.swing.JButton();
        btnSelect = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        btnZoomIn = new javax.swing.JButton();
        btnZoomOut = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        cmbViewType = new javax.swing.JComboBox();
        pnlScrollMain = new javax.swing.JScrollPane();
        pnlRight = new javax.swing.JPanel();
        barContainers = new  javax.swing.JToolBar("Physical Containers",javax.swing.JToolBar.VERTICAL);
        btnWireContainer = new javax.swing.JToggleButton();
        btnWirelessContainer = new javax.swing.JToggleButton();
        barConnections = new  javax.swing.JToolBar("Physical Connections",javax.swing.JToolBar.VERTICAL);
        btnElectricalLink = new javax.swing.JToggleButton();
        btnOpticalLink = new javax.swing.JToggleButton();
        btnWirelessLink = new javax.swing.JToggleButton();

        setLayout(new java.awt.BorderLayout());

        barMain.setRollover(true);

        btnAddBackgroundImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/add-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddBackgroundImage, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnAddBackgroundImage.text")); // NOI18N
        btnAddBackgroundImage.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnAddBackgroundImage.toolTipText")); // NOI18N
        btnAddBackgroundImage.setEnabled(false);
        btnAddBackgroundImage.setFocusable(false);
        btnAddBackgroundImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddBackgroundImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddBackgroundImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBackgroundImageActionPerformed(evt);
            }
        });
        barMain.add(btnAddBackgroundImage);

        btnRemoveBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/remove-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveBackground, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRemoveBackground.text")); // NOI18N
        btnRemoveBackground.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRemoveBackground.toolTipText")); // NOI18N
        btnRemoveBackground.setEnabled(false);
        btnRemoveBackground.setFocusable(false);
        btnRemoveBackground.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveBackground.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveBackgroundActionPerformed(evt);
            }
        });
        barMain.add(btnRemoveBackground);

        btnFormatText.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/format.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFormatText, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnFormatText.text")); // NOI18N
        btnFormatText.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnFormatText.toolTipText")); // NOI18N
        btnFormatText.setEnabled(false);
        btnFormatText.setFocusable(false);
        btnFormatText.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFormatText.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFormatText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFormatTextActionPerformed(evt);
            }
        });
        barMain.add(btnFormatText);

        btnShowNodeLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/hide_node_labels.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowNodeLabels, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnShowNodeLabels.text")); // NOI18N
        btnShowNodeLabels.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnShowNodeLabels.toolTipText")); // NOI18N
        btnShowNodeLabels.setEnabled(false);
        btnShowNodeLabels.setFocusable(false);
        btnShowNodeLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowNodeLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowNodeLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowNodeLabelsActionPerformed(evt);
            }
        });
        barMain.add(btnShowNodeLabels);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barMain.add(btnSave);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/select.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setEnabled(false);
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        barMain.add(btnSelect);

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setEnabled(false);
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barMain.add(btnConnect);

        btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/zoom-in.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnZoomIn, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnZoomIn.text")); // NOI18N
        btnZoomIn.setEnabled(false);
        btnZoomIn.setFocusable(false);
        btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomInActionPerformed(evt);
            }
        });
        barMain.add(btnZoomIn);

        btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/zoom-out.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnZoomOut, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnZoomOut.text")); // NOI18N
        btnZoomOut.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnZoomOut.toolTipText")); // NOI18N
        btnZoomOut.setEnabled(false);
        btnZoomOut.setFocusable(false);
        btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnZoomOutActionPerformed(evt);
            }
        });
        barMain.add(btnZoomOut);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setEnabled(false);
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        barMain.add(btnExport);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setEnabled(false);
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        barMain.add(btnRefresh);

        cmbViewType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Default View" }));
        barMain.add(cmbViewType);

        add(barMain, java.awt.BorderLayout.PAGE_START);
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);

        pnlRight.setLayout(new java.awt.BorderLayout());

        barContainers.setRollover(true);

        btnWireContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/wire-container.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWireContainer, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWireContainer.text")); // NOI18N
        btnWireContainer.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWireContainer.toolTipText")); // NOI18N
        btnWireContainer.setFocusable(false);
        btnWireContainer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWireContainer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWireContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWireContainerActionPerformed(evt);
            }
        });
        barContainers.add(btnWireContainer);

        btnWirelessContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/wireless-container.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWirelessContainer, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWirelessContainer.text")); // NOI18N
        btnWirelessContainer.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWirelessContainer.toolTipText")); // NOI18N
        btnWirelessContainer.setFocusable(false);
        btnWirelessContainer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWirelessContainer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWirelessContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWirelessContainerActionPerformed(evt);
            }
        });
        barContainers.add(btnWirelessContainer);

        pnlRight.add(barContainers, java.awt.BorderLayout.PAGE_START);

        barConnections.setRollover(true);

        btnElectricalLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/electrical_link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnElectricalLink, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnElectricalLink.text")); // NOI18N
        btnElectricalLink.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnElectricalLink.toolTipText")); // NOI18N
        btnElectricalLink.setFocusable(false);
        btnElectricalLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnElectricalLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnElectricalLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnElectricalLinkActionPerformed(evt);
            }
        });
        barConnections.add(btnElectricalLink);

        btnOpticalLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/optical_link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpticalLink, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnOpticalLink.text")); // NOI18N
        btnOpticalLink.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnOpticalLink.toolTipText")); // NOI18N
        btnOpticalLink.setFocusable(false);
        btnOpticalLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpticalLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpticalLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpticalLinkActionPerformed(evt);
            }
        });
        barConnections.add(btnOpticalLink);

        btnWirelessLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/wireless_link.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnWirelessLink, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWirelessLink.text")); // NOI18N
        btnWirelessLink.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnWirelessLink.toolTipText")); // NOI18N
        btnWirelessLink.setFocusable(false);
        btnWirelessLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnWirelessLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnWirelessLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnWirelessLinkActionPerformed(evt);
            }
        });
        barConnections.add(btnWirelessLink);

        pnlRight.add(barConnections, java.awt.BorderLayout.LINE_END);

        add(pnlRight, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(ViewScene.ACTION_SELECT);
        buttonGroupRightToolbar.clearSelection();
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(ViewScene.ACTION_CONNECT);
        btnWireContainer.doClick();
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnAddBackgroundImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBackgroundImageActionPerformed
        vrs.addBackground();
    }//GEN-LAST:event_btnAddBackgroundImageActionPerformed

    private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomInActionPerformed
        scene.zoomIn();
    }//GEN-LAST:event_btnZoomInActionPerformed

    private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnZoomOutActionPerformed
        scene.zoomOut();
    }//GEN-LAST:event_btnZoomOutActionPerformed

    private void btnRemoveBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBackgroundActionPerformed
        vrs.removeBackground();
    }//GEN-LAST:event_btnRemoveBackgroundActionPerformed

    private void btnElectricalLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnElectricalLinkActionPerformed
        scene.getConnectionProvider().setCurrentLineColor(ObjectConnectionWidget.COLOR_ELECTRICALLINK);
        scene.getConnectionProvider().setCurrentConnectionSelection(CONNECTION_ELECTRICALLINK);
    }//GEN-LAST:event_btnElectricalLinkActionPerformed

    private void btnOpticalLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpticalLinkActionPerformed
        scene.getConnectionProvider().setCurrentLineColor(ObjectConnectionWidget.COLOR_OPTICALLINK);
        scene.getConnectionProvider().setCurrentConnectionSelection(CONNECTION_OPTICALLINK);
    }//GEN-LAST:event_btnOpticalLinkActionPerformed

    private void btnWirelessLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWirelessLinkActionPerformed
        scene.getConnectionProvider().setCurrentLineColor(ObjectConnectionWidget.COLOR_WIRELESSLINK);
        scene.getConnectionProvider().setCurrentConnectionSelection(CONNECTION_WIRELESSLINK);
    }//GEN-LAST:event_btnWirelessLinkActionPerformed

    private void btnWireContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWireContainerActionPerformed
        scene.getConnectionProvider().setCurrentLineColor(ObjectConnectionWidget.COLOR_WIRE);
        scene.getConnectionProvider().setCurrentConnectionSelection(CONNECTION_WIRECONTAINER);
    }//GEN-LAST:event_btnWireContainerActionPerformed

    private void btnWirelessContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWirelessContainerActionPerformed
        scene.getConnectionProvider().setCurrentLineColor(ObjectConnectionWidget.COLOR_WIRELESS);
        scene.getConnectionProvider().setCurrentConnectionSelection(CONNECTION_WIRELESSCONTAINER);
    }//GEN-LAST:event_btnWirelessContainerActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        vrs.saveView();
        isSaved = true;
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        vrs.refreshView();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        new ExportSceneAction(scene).actionPerformed(evt);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnFormatTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFormatTextActionPerformed
        final FormatTextPanel pnlFormat = new FormatTextPanel();
        DialogDescriptor dd = new DialogDescriptor(pnlFormat,"Text Settings",true,new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == DialogDescriptor.OK_OPTION){
                    if (pnlFormat.getNodesFontColor() != null)
                        currentColor = pnlFormat.getNodesFontColor();
                    if (pnlFormat.getNodesFontType() != null)
                        currentFont = pnlFormat.getNodesFontType();
                    if (pnlFormat.getNodesFontSize() != -1)
                        currentFont = currentFont.deriveFont(Float.valueOf(pnlFormat.getNodesFontSize()+".0")); //NOI18N

                    for (Widget node : scene.getNodesLayer().getChildren()){
                        if (pnlFormat.getNodesFontColor() != null)
                            ((ObjectNodeWidget)node).getLabelWidget().setForeground(pnlFormat.getNodesFontColor());

                        if (pnlFormat.getNodesFontType() != null){
                            Font newFont = new Font(pnlFormat.getNodesFontType().getFontName(),
                                    ((ObjectNodeWidget)node).getLabelWidget().getFont().getStyle(),
                                    ((ObjectNodeWidget)node).getLabelWidget().getFont().getSize());
                            ((ObjectNodeWidget)node).getLabelWidget().setFont(newFont);
                        }
                        if (pnlFormat.getNodesFontSize() != -1){
                            Font newFont = new Font(((ObjectNodeWidget)node).getLabelWidget().getFont().getFontName(),
                                    ((ObjectNodeWidget)node).getLabelWidget().getFont().getStyle(),
                                    pnlFormat.getNodesFontSize());
                            ((ObjectNodeWidget)node).getLabelWidget().setFont(newFont);
                        }
                    }
//                    for (Widget node : scene.getEdgesLayer().getChildren()){
//                        if (pnlFormat.getEdgesFontColor() != null)
//                            ((ObjectConnectionWidget)node).setForeground(pnlFormat.getNodesFontColor());
//                        if (pnlFormat.getEdgesFontType() != null)
//                            ((ObjectConnectionWidget)node).setFont(pnlFormat.getNodesFontType());
//                        if (pnlFormat.getEdgesFontSize() != null){
//                            Font newFont = new Font(((ObjectConnectionWidget)node).getFont().getFontName(),Font.BOLD,pnlFormat.getNodesFontSize());
//                            ((ObjectConnectionWidget)node).setFont(newFont);
//                        }
//                    }
                }
            }
        });
        DialogDisplayer.getDefault().notify(dd);
    }//GEN-LAST:event_btnFormatTextActionPerformed

    private void btnShowNodeLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowNodeLabelsActionPerformed
        for (Widget node : scene.getNodesLayer().getChildren())
            ((ObjectNodeWidget)node).getLabelWidget().setVisible(!btnShowNodeLabels.isSelected());
        scene.validate();
    }//GEN-LAST:event_btnShowNodeLabelsActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barConnections;
    private javax.swing.JToolBar barContainers;
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnAddBackgroundImage;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JToggleButton btnElectricalLink;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFormatText;
    private javax.swing.JToggleButton btnOpticalLink;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveBackground;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowNodeLabels;
    private javax.swing.JToggleButton btnWireContainer;
    private javax.swing.JToggleButton btnWirelessContainer;
    private javax.swing.JToggleButton btnWirelessLink;
    private javax.swing.JButton btnZoomIn;
    private javax.swing.JButton btnZoomOut;
    private javax.swing.JComboBox cmbViewType;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JScrollPane pnlScrollMain;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ObjectViewTopComponent getDefault() {
        if (instance == null) {
            instance = new ObjectViewTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ObjectViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ObjectViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ObjectViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ObjectViewTopComponent) {
            return (ObjectViewTopComponent) win;
        }
        Logger.getLogger(ObjectViewTopComponent.class.getName()).warning(
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
        vrs.initializeLookListener();
        scene.addActionListener(this);
    }

    @Override
    public void componentClosed() {
        vrs.terminateLookupListener();
        scene.removeActionListener(this);
        scene.clear();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        p.setProperty("fontName", currentFont.getFontName());
        p.setProperty("fontSize", String.valueOf(currentFont.getSize()));
        p.setProperty("fontColor", String.valueOf(currentColor.getRGB()));
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
        currentFont = new Font(p.getProperty("fontName") == null ? ObjectNodeWidget.defaultFont.getFontName() : p.getProperty("fontName"),
                ObjectNodeWidget.defaultFont.getStyle(),
                p.getProperty("fontSize")== null ? ObjectNodeWidget.defaultFont.getSize() : Integer.valueOf(p.getProperty("fontSize")));
        currentColor = p.getProperty("fontColor") == null ? Color.black : new Color(Integer.valueOf(p.getProperty("fontColor")));
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }
    
    @Override
    public boolean canClose(){
        return checkForUnsavedView(true);
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }

    public ViewScene getScene(){
        return scene;
    }

    @Override
    public String getDisplayName(){
        if (super.getDisplayName() == null)
            return "<No Name>";
        return super.getDisplayName().trim().equals("")?"<No Name>":super.getDisplayName();
    }

    public boolean getIsSaved() {
        return isSaved;
    }

    public void setIsSaved(boolean value){
        this.isSaved = value;
        if (value)
            this.setHtmlDisplayName(this.getDisplayName());
        else
            this.setHtmlDisplayName("<html><b>"+ getDisplayName()+" [Modified]</b></html>");
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public Font getCurrentFont() {
        return currentFont;
    }

    public void actionPerformed(ActionEvent e) {
        switch (e.getID()){
            case ViewScene.SCENE_CHANGE:
                this.setIsSaved(false);
                break;
            case ViewScene.SCENE_CHANGETOSAVE:
                btnSaveActionPerformed(e);
                nu.showSimplePopup("Object View", NotificationUtil.INFO, "The view has been saved automatically");
                break;
            case ViewScene.SCENE_OBJECTSELECTED:
                ObjectNode widgetNode = new ObjectNode((LocalObjectLight)e.getSource(),true);
                em.setRootContext(widgetNode);
                setActivatedNodes(new Node[]{widgetNode});
        }
    }

    public boolean checkForUnsavedView(boolean showCancel) {
        if (!isSaved){
            switch (JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?",
                    "Confirmation",showCancel?JOptionPane.YES_NO_CANCEL_OPTION:JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        isSaved = true;
        return true;
    }

    public void refresh() {
        btnRefreshActionPerformed(null);
    }

    public void toggleButtons(boolean enabled) {
        btnAddBackgroundImage.setEnabled(enabled);
        btnRemoveBackground.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnConnect.setEnabled(enabled);
        btnZoomIn.setEnabled(enabled);
        btnZoomOut.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnFormatText.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnShowNodeLabels.setEnabled(enabled);
    }
}
