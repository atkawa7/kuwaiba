/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalReportDescriptor;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.pools.PoolNode;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * Shows the class reports available for the selected node (if any) and run any of them
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExecuteClassReportAction extends AbstractAction implements Presenter.Popup {
    private static ExecuteClassReportAction instance;
    
    private ExecuteClassReportAction() {
        putValue(NAME, "Reports");
    }
    
    public static ExecuteClassReportAction createExecuteReportAction() {
        if (instance == null)
            instance = new ExecuteClassReportAction();
        return instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (!selectedNodes.hasNext()) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.WARNING_MESSAGE, "You have to select a node");
            return;
        }
        
        LocalObjectLight theObject = ((ObjectNode)selectedNodes.next()).getObject();
        JMenuItem mniReport = (JMenuItem)ev.getSource();
        
        actionPerformed(theObject, Long.valueOf(mniReport.getName()));
        
    }
    
    public void actionPerformed(LocalObjectLight theObject, long reportId) {
        
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("objectId", theObject.getOid());
        arguments.put("objectClass", theObject.getClassName());
        
        byte[] theReport = CommunicationsStub.getInstance().executeReport(reportId, arguments);
        
        if (theReport == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {           
            try {
                File tempFile = File.createTempFile("class_report_" + theObject.getClassName() + "_" + theObject.getOid(), ".html"); //NOI18N
                
                try (FileOutputStream faos = new FileOutputStream(tempFile)) {
                    faos.write(theReport);
                    faos.flush();
                }
                if(Desktop.isDesktopSupported()) 
                try {
                    Desktop.getDesktop().browse(Utilities.toURI(tempFile));
                } catch (IOException ex) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
                }
                
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }            
        }
    }
    
    public JMenuItem getPopupPresenter (LocalObjectLight theObject) {
        JMenu mnuPossibleChildren = new JMenu("Reports");
        mnuPossibleChildren.setEnabled(false);
        List<LocalReportDescriptor> reportDescriptors = CommunicationsStub.getInstance().
                    getReportsForClass(theObject.getClassName(), -1);
            
        if (reportDescriptors == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.WARNING_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            if (!reportDescriptors.isEmpty()) {
                for (LocalReportDescriptor reportDescriptor : reportDescriptors) {
                    JMenuItem mnuReport = new JMenuItem(reportDescriptor.getName());
                    mnuReport.setToolTipText(reportDescriptor.getDescription());
                    mnuReport.setName(String.valueOf(reportDescriptor.getId()));
                    mnuReport.addActionListener(this);
                    mnuPossibleChildren.add(mnuReport);
                }
                mnuPossibleChildren.setEnabled(true);
                MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
            } 
        }
        
        return mnuPossibleChildren;
    }

    @Override
    public JMenuItem getPopupPresenter() {

        Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class).allInstances().iterator();
        
        if (selectedNodes.hasNext()) {
            ObjectNode selectedNode = (ObjectNode)selectedNodes.next();
            return getPopupPresenter(selectedNode.getObject());
        } else {
            JMenu mnuPossibleChildren = new JMenu("Reports");
            mnuPossibleChildren.setEnabled(false);
            return mnuPossibleChildren;
        }
    }
    
    public static class ExecutePoolReportAction extends ExecuteClassReportAction {
        private static ExecutePoolReportAction instance;
        
        private ExecutePoolReportAction() {
            putValue(NAME, "Pool Reports");
        }
        
        public static ExecutePoolReportAction createExecutePoolReportAction() {
            if (instance == null)
                instance = new ExecutePoolReportAction();
            return instance;
        }
        
        @Override
        public JMenuItem getPopupPresenter() {

            Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(PoolNode.class).allInstances().iterator();

            if (selectedNodes.hasNext()) {
                PoolNode selectedNode = (PoolNode)selectedNodes.next();
                return getPopupPresenter(selectedNode.getPool());
            } else {
                JMenu mnuPossibleChildren = new JMenu("Reports");
                mnuPossibleChildren.setEnabled(false);
                return mnuPossibleChildren;
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent ev) {
            Iterator selectedNodes = Utilities.actionsGlobalContext().lookupResult(PoolNode.class).allInstances().iterator();

            if (!selectedNodes.hasNext()) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.WARNING_MESSAGE, "You have to select a node");
                return;
            }

            LocalObjectLight theObject = ((PoolNode)selectedNodes.next()).getPool();
            JMenuItem mniReport = (JMenuItem)ev.getSource();

            actionPerformed(theObject, Long.valueOf(mniReport.getName()));

        }
    } 
}