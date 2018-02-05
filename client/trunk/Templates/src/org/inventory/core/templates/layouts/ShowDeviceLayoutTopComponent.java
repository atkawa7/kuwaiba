/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts;

import java.awt.KeyEventDispatcher;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.event.CurrentKeyEventDispatcher;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.templates.layouts.scene.ShowDeviceLayoutScene;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.TopComponent;

/**
 * Top component which displays model type view for an object.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.core.templates.layouts//ShowDeviceLayout//EN",
        autostore = false
)
public final class ShowDeviceLayoutTopComponent extends TopComponent {
    private ShowDeviceLayoutScene scene;
    private LocalObjectLight objectLight;
    
    KeyEventDispatcher keyEventDispatcher;
        
    private ShowDeviceLayoutTopComponent() {
        initComponents();        
    }

    public ShowDeviceLayoutTopComponent(LocalObjectLight objectLight) {
        this();
        this.objectLight = objectLight;
        
        setName(String.format("Device Layout for %s", objectLight.getName()));
        
        scene = new ShowDeviceLayoutScene();
        associateLookup(scene.getLookup());
        scene.setLayout(LayoutFactory.createAbsoluteLayout());
        pnlScrollPane.setViewportView(scene.createView());
        
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
                CurrentKeyEventDispatcher.getInstance().updateKeyEventDispatcher(ShowDeviceLayoutTopComponent.this);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
    
    @Override
    protected String preferredID() {
        return "ShowDeviceLayoutTopComponent_" + objectLight.getOid(); //NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlScrollPane = new javax.swing.JScrollPane();
        barMain = new javax.swing.JToolBar();
        btnRefresh = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());
        add(pnlScrollPane, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(ShowDeviceLayoutTopComponent.class, "ShowDeviceLayoutTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(ShowDeviceLayoutTopComponent.class, "ShowDeviceLayoutTopComponent.btnRefresh.toolTipText")); // NOI18N
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
        barMain.add(btnRefresh);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(ShowDeviceLayoutTopComponent.class, "ShowDeviceLayoutTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(ShowDeviceLayoutTopComponent.class, "ShowDeviceLayoutTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });
        barMain.add(btnExport);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
        ExportScenePanel exportPanel = new ExportScenePanel(
            new SceneExportFilter[]{ImageFilter.getInstance()}, 
            scene, "DeviceLayoutTo" + objectLight.getName()); // NOI18N
                
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportMouseClicked

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        componentClosed();
        componentOpened();
    }//GEN-LAST:event_btnRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JScrollPane pnlScrollPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        DeviceLayoutRenderer renderDeviceLayout = new DeviceLayoutRenderer(objectLight, scene, 
            new Point(0, 0), new Rectangle(0, 0, 7000, 1000));
        
        if (renderDeviceLayout.getEquipmentModelView() == null && !renderDeviceLayout.hasDefaultDeviceLayout()) {
            close();
            if (renderDeviceLayout.getErrorMessage() != null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, renderDeviceLayout.getErrorMessage());
            }
            return;
        }
        renderDeviceLayout.setOriginalSize(true);
        renderDeviceLayout.render();
        scene.validate();
        scene.paint();
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
}
