/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.nodes.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static javax.swing.Action.NAME;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.core.LocalReportLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.actions.ComposedAction;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.SubMenuDialog;
import org.inventory.core.services.utils.SubMenuItem;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Shows the class reports available for the selected node (if any) and run any of them
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExecuteClassLevelReportAction extends GenericObjectNodeAction implements ComposedAction, Presenter.Popup {
    private static ExecuteClassLevelReportAction instance;
    
    private ExecuteClassLevelReportAction() {
        putValue(NAME, "Reports...");
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/inventory/navigation/navigationtree/res/show_reports.png", false));
    }
    
    public static ExecuteClassLevelReportAction getInstance() {
        return instance == null ? instance = new ExecuteClassLevelReportAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        if (selectedNodes.hasNext()) {
            LocalObjectLight selectedObject = ((ObjectNode) selectedNodes.next()).getObject();
            List<LocalReportLight> reportDescriptors = CommunicationsStub.getInstance().getClassLevelReports(selectedObject.getClassName(), true, false);
            
            if (reportDescriptors == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.WARNING_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                if (!reportDescriptors.isEmpty()) {
                    List<SubMenuItem> subMenuItems = new ArrayList<>();
                    for (LocalReportLight reportDescriptor : reportDescriptors) {
                        SubMenuItem subMenuItem = new SubMenuItem(reportDescriptor.getName());
                        subMenuItem.setToolTipText(reportDescriptor.getDescription());
                        subMenuItem.addProperty(Constants.PROPERTY_ID, reportDescriptor.getId());
                        subMenuItems.add(subMenuItem);
                    }
                    SubMenuDialog.getInstance((String) getValue(NAME), this).showSubmenu(subMenuItems);
                } else {
                    JOptionPane.showMessageDialog(null, "There are no reports related to the selected object", 
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }
    }
    
    public void actionPerformed(LocalObjectLight theObject, long reportId) {
        
        byte[] theReport = CommunicationsStub.getInstance().executeClassLevelReport(theObject.getClassName(),
                theObject.getOid(), reportId);
        
        if (theReport == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {           
            try {
                File tempFile = File.createTempFile("class_report_" + theObject.getClassName() + "_" + theObject.getOid(), ".html"); //NOI18N
                
                try (FileOutputStream faos = new FileOutputStream(tempFile)) {
                    faos.write(theReport);
                    faos.flush();
                }
                if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){ 
                    try {
                        Desktop.getDesktop().browse(Utilities.toURI(tempFile));
                    } catch (IOException | UnsupportedOperationException ex) {
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                    }
                } else {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, String.format("asda %s", Utilities.toURI(tempFile)) );
                }
                
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }            
        }
    }
    
    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_REPORTS, LocalPrivilege.ACCESS_LEVEL_READ);
    }

    @Override
    public void finalActionPerformed(ActionEvent e) {
        if (e != null && e.getSource() instanceof SubMenuDialog) {
            Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();

            if (!selectedNodes.hasNext()) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.WARNING_MESSAGE, "You have to select a node");
                return;
            }

            LocalObjectLight theObject = ((ObjectNode)selectedNodes.next()).getObject();
            Long reportId = (Long) ((SubMenuDialog) e.getSource()).getSelectedSubMenuItem().getProperty(Constants.PROPERTY_ID);

            actionPerformed(theObject, reportId);
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return null;
    }

    @Override
    public int numberOfNodes() {
        return 1;
    }
}