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
package org.inventory.views.rackview;

import org.inventory.views.rackview.scene.RackViewScene;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.behaviors.CompatibleTopComponent;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.services.event.CurrentKeyEventDispatcher;
import org.inventory.core.services.i18n.I18N;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * TopComponent used to show a simple rack view or an inside rack view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class RackViewTopComponent extends TopComponent implements ActionListener, Refreshable, CompatibleTopComponent {
    private RackViewScene scene;
    private LocalObjectLight rackLight;
    private RackViewService service;
    private JComponent satelliteView;
    
    private KeyEventDispatcher keyEventDispatcher;
        
    public RackViewTopComponent(LocalObjectLight rack) {
        this();
        this.rackLight = rack;
        initCustomComponents();
        btnSelect.setEnabled(false);
        btnConnect.setEnabled(false);
        btnRackTableView.setEnabled(false);
        
        btnSelect.setSelected(true);
        setName("Rack View to " + rack.toString());
    }

    private RackViewTopComponent() {
        initComponents();
    }
    
    @Override
    protected String preferredID() {
        return "RackViewTopComponent_" + rackLight.getOid(); //NOI18N
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initCustomComponents() {
        scene = new RackViewScene(rackLight);
        scene.addChangeListener(this);        
                
        associateLookup(scene.getLookup());
        pnlMainScrollPanel.setViewportView(scene.createView());
                        
        service = new RackViewService(scene, rackLight);
        
        keyEventDispatcher = new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5 && e.getModifiers() == 0) {
                    btnRefreshActionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
                    return true;
                }
                return false;
            }
        };
        CurrentKeyEventDispatcher.getInstance().addKeyEventDispatcher(this, keyEventDispatcher);
        
        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                CurrentKeyEventDispatcher.getInstance().updateKeyEventDispatcher(RackViewTopComponent.this);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMainScrollPanel = new javax.swing.JScrollPane();
        toolBarMain = new javax.swing.JToolBar();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnSelect = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnRackTableView = new javax.swing.JButton();
        btnShowConnections = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();

        setLayout(new java.awt.BorderLayout());
        add(pnlMainScrollPanel, java.awt.BorderLayout.CENTER);

        toolBarMain.setRollover(true);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(392, 38));

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        toolBarMain.add(btnExport);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRefreshMouseClicked(evt);
            }
        });
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        toolBarMain.add(btnRefresh);
        toolBarMain.add(jSeparator2);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/select.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSelectMouseClicked(evt);
            }
        });
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        toolBarMain.add(btnSelect);

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnConnectMouseClicked(evt);
            }
        });
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        toolBarMain.add(btnConnect);
        toolBarMain.add(jSeparator1);

        btnRackTableView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/show_table.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRackTableView, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRackTableView.text")); // NOI18N
        btnRackTableView.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRackTableView.toolTipText")); // NOI18N
        btnRackTableView.setFocusable(false);
        btnRackTableView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRackTableView.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRackTableView.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRackTableViewMouseClicked(evt);
            }
        });
        btnRackTableView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRackTableViewActionPerformed(evt);
            }
        });
        toolBarMain.add(btnRackTableView);

        btnShowConnections.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/show_connection.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowConnections, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnShowConnections.text")); // NOI18N
        btnShowConnections.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnShowConnections.toolTipText")); // NOI18N
        btnShowConnections.setFocusable(false);
        btnShowConnections.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowConnections.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowConnections.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnShowConnectionsMouseClicked(evt);
            }
        });
        btnShowConnections.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowConnectionsActionPerformed(evt);
            }
        });
        toolBarMain.add(btnShowConnections);
        toolBarMain.add(jSeparator3);

        add(toolBarMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
        
    }//GEN-LAST:event_btnExportMouseClicked

    private void btnRackTableViewMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRackTableViewMouseClicked
        
    }//GEN-LAST:event_btnRackTableViewMouseClicked

    private void btnShowConnectionsMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnShowConnectionsMouseClicked
        
    }//GEN-LAST:event_btnShowConnectionsMouseClicked

    private void btnConnectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnConnectMouseClicked
        
    }//GEN-LAST:event_btnConnectMouseClicked

    private void btnSelectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSelectMouseClicked
        
    }//GEN-LAST:event_btnSelectMouseClicked

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        if (!isCompatible()) {
            close();
            return;
        }
        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(String.format("Loading The Rack View to %s", rackLight.toString()));
        
        RequestProcessor.getDefault().post(new Runnable() {
            
            @Override
            public void run() {
                progressHandle.start();
                
                toolBarMain.setVisible(false);
                pnlMainScrollPanel.setVisible(false);
                
                if (btnShowConnections.isSelected())
                    remove(satelliteView);
                
                scene.clear();
                service.shownRack();
                scene.validate();
                
                if (btnShowConnections.isSelected())
                    add(satelliteView, BorderLayout.EAST);
                toolBarMain.setVisible(true);
                pnlMainScrollPanel.setVisible(true);
                
                revalidate();  
                progressHandle.finish();
            }
        });
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnShowConnectionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowConnectionsActionPerformed
        final ProgressHandle progressHandle = ProgressHandleFactory.createHandle(String.format("Loading The Rack View to %s", rackLight.toString()));
        
        RequestProcessor.getDefault().post(new Runnable() {

            @Override
            public void run() {
                progressHandle.start();
                
                toolBarMain.setVisible(false);
                pnlMainScrollPanel.setVisible(false);
                                
                if (satelliteView == null)
                    satelliteView = scene.createSatelliteView();

                if (btnShowConnections.isSelected()) {
                    scene.setShowConnections(true);            
                    scene.clear();

                    service.shownRack();
                    scene.validate();
                    
                    btnSelect.setEnabled(true);
                    btnConnect.setEnabled(true);
                    btnRackTableView.setEnabled(true);
                    
                    add(satelliteView, BorderLayout.EAST);
                    
                } else {
                    remove(satelliteView);
                    
                    scene.setShowConnections(false);
                    scene.clear();
                    
                    service.shownRack();
                    scene.validate();
                    
                    scene.setActiveTool(RackViewScene.ACTION_SELECT);
                    btnSelect.setSelected(true);        
                    btnConnect.setSelected(false);
                    btnSelect.setEnabled(false);
                    btnConnect.setEnabled(false);
                    btnRackTableView.setEnabled(false);
                }
                toolBarMain.setVisible(true);
                pnlMainScrollPanel.setVisible(true);
                
                revalidate();                
                progressHandle.finish();
            }
        });
        
    }//GEN-LAST:event_btnShowConnectionsActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(
            new SceneExportFilter[]{ImageFilter.getInstance()}, 
            scene, rackLight.toString());
                        
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        if (!btnSelect.isEnabled())
            return;
        btnSelect.setSelected(true);        
        btnConnect.setSelected(false);
        scene.setActiveTool(RackViewScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        if (!btnConnect.isEnabled())
            return;
        
        btnSelect.setSelected(false);
        btnConnect.setSelected(true);
        scene.setActiveTool(RackViewScene.ACTION_CONNECT);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnRackTableViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRackTableViewActionPerformed
        if (!btnRackTableView.isEnabled())
            return;
        
        RackTableViewTopComponent rackTable = ((RackTableViewTopComponent) WindowManager.getDefault().findTopComponent("RackTableViewTopComponent_" + rackLight.getOid())); //NOI18N

        if (rackTable == null) {
            rackTable = new RackTableViewTopComponent(rackLight, service);
            rackTable.open();
        } else {
            if (rackTable.isOpened())
                rackTable.requestAttention(true);
            else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                   //so we will reuse the instance, refreshing the vierw first
                rackTable.refresh();
                rackTable.open();
            }
        }
        rackTable.requestActive();
    }//GEN-LAST:event_btnRackTableViewActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRackTableView;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowConnections;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        if (!isCompatible()) {
            close();            
            return;
        }        
        service.shownRack();
    }

    @Override
    public void componentClosed() {
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
    public void actionPerformed(ActionEvent e) {
        if (e.getID() == RackViewScene.SCENE_CHANGE) {
            scene.clear();
            service.shownRack();
        }
    }

    @Override
    public void refresh() {
    }
    
    public void refreshScene() {
        btnRefreshActionPerformed(null);
    }

    @Override
    public boolean isCompatible() {
        boolean isCompatible = false;
        
        String title = I18N.gm("error");
        String message = "";
        int messageType = JOptionPane.ERROR_MESSAGE;
        
        if(CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_CUSTOMSHAPE, true) == null) {
            JOptionPane.showMessageDialog(null, 
                "This database seems outdated. Contact your administrator to apply the necessary patches to add the CustomShape class", 
                I18N.gm("error"), messageType);
            return false;            
        }
        LocalObject rack = CommunicationsStub.getInstance().getObjectInfo(rackLight.getClassName(), rackLight.getOid());
        
        if (rack == null) {
            message = CommunicationsStub.getInstance().getError();
        } else {
            Integer rackUnits = (Integer) rack.getAttribute(Constants.PROPERTY_RACK_UNITS);
            if (rackUnits == null || rackUnits == 0) {
                message += String.format("Attribute %s in rack %s does not exist or is not set correctly\n", Constants.PROPERTY_RACK_UNITS, rack);                                                             
            } else {
                List<LocalObjectLight> devicesLight = CommunicationsStub.getInstance().getObjectChildren(rack.getOid(), rack.getClassName());
                if (devicesLight != null) {
                    List<LocalClassMetadata> objChildrenClasses = new ArrayList<>();

                    for (LocalObjectLight device : devicesLight) {
                        LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(device.getClassName(), true);
                        if (lcm == null) {
                            JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), title, messageType);
                            return false;
                        }
                        if (!objChildrenClasses.contains(lcm))
                            objChildrenClasses.add(lcm);
                    }                            
                    for (LocalClassMetadata lcm : objChildrenClasses) {
                        if (!lcm.hasAttribute(Constants.PROPERTY_POSITION))
                            message += String.format("The %s attribute does not exist in class %s\n", Constants.PROPERTY_POSITION, lcm.toString());
                        else {
                            if (!"Integer".equals(lcm.getTypeForAttribute(Constants.PROPERTY_POSITION)))
                                message += String.format("The %s attribute type in class %s must be an Integer\n", Constants.PROPERTY_POSITION, lcm.toString());
                        }
                        if (!lcm.hasAttribute(Constants.PROPERTY_RACK_UNITS))
                            message += String.format("The %s attribute does not exist in class %s\n", Constants.PROPERTY_RACK_UNITS, lcm.toString());
                        else {
                            if (!"Integer".equals(lcm.getTypeForAttribute(Constants.PROPERTY_RACK_UNITS)))
                                message += String.format("The %s attribute type in class %s must be an Integer\n", Constants.PROPERTY_RACK_UNITS, lcm.toString());
                        }
                    }
                    List<LocalObject> devices = new ArrayList<>();
                    if (message.isEmpty()) {
                        for (LocalObjectLight deviceLight : devicesLight) {
                            LocalObject device = CommunicationsStub.getInstance().getObjectInfo(deviceLight.getClassName(), deviceLight.getOid());
                            if (device != null) {
                                devices.add(device);
                                
                                int devicePosition = device.getAttribute(Constants.PROPERTY_POSITION) != null ? (int) device.getAttribute(Constants.PROPERTY_POSITION) : 0;
                                if (devicePosition <= 0)
                                    message += String.format("The %s in %s must be greater than or equal to zero\n", Constants.PROPERTY_POSITION, device.toString());
                                else {
                                    if (devicePosition > rackUnits)
                                        message += String.format("The %s in %s is greater than the number of rack units\n", Constants.PROPERTY_POSITION, device.toString());
                                }
                                int deviceRackUnits = device.getAttribute(Constants.PROPERTY_RACK_UNITS) != null ? (int) device.getAttribute(Constants.PROPERTY_RACK_UNITS) : 0;
                                
                                if (deviceRackUnits <= 0)
                                    message += String.format("The %s in %s must be greater than or equal to zero\n", Constants.PROPERTY_RACK_UNITS, device.toString());
                                else {
                                    if (deviceRackUnits > rackUnits)
                                        message += String.format("The %s in %s is greater than the number of rack units\n", Constants.PROPERTY_RACK_UNITS, device.toString());
                                }
                            } else {
                                JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), title, messageType);
                                return false;
                            }
                        }
                    }
                    if (message.isEmpty()) {
                        HashMap<Integer, LocalObjectLight> rackUnitsMap = new HashMap();
                        
                        for (LocalObject device : devices) {
                            int devicePosition = (int) device.getAttribute(Constants.PROPERTY_POSITION);
                            int deviceRackUnits = (int) device.getAttribute(Constants.PROPERTY_RACK_UNITS);
                            
                            for (int i = devicePosition; i < devicePosition + deviceRackUnits; i += 1) {
                                if (!rackUnitsMap.containsKey(devicePosition))
                                    rackUnitsMap.put(i, device);
                                else {
                                    LocalObjectLight lol = rackUnitsMap.get(devicePosition);
                                    
                                    if (!lol.equals(device))
                                        message += String.format("The Position %s set in the %s is used by the %s\n", i, device.toString(), lol.toString());
                                }
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, CommunicationsStub.getInstance().getError(), title, messageType);
                    return false;
                }
            }
        }
        if (message.isEmpty())
            isCompatible = true;
        
        if (!isCompatible)
            JOptionPane.showMessageDialog(null, message, title, messageType);
        return isCompatible;
    }
}
